package org.sagebionetworks.repo.model.dbo.dao.semaphore;

import static org.sagebionetworks.repo.model.query.jdo.SqlConstants.COL_COUNTING_SEMAPHORE_EXPIRES;
import static org.sagebionetworks.repo.model.query.jdo.SqlConstants.COL_COUNTING_SEMAPHORE_KEY;
import static org.sagebionetworks.repo.model.query.jdo.SqlConstants.COL_COUNTING_SEMAPHORE_LOCK_TOKEN;
import static org.sagebionetworks.repo.model.query.jdo.SqlConstants.COL_LOCK_MASTER_KEY;
import static org.sagebionetworks.repo.model.query.jdo.SqlConstants.TABLE_COUNTING_SEMAPHORE;
import static org.sagebionetworks.repo.model.query.jdo.SqlConstants.TABLE_LOCK_MASTER;

import java.util.UUID;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.sagebionetworks.repo.model.dao.semaphore.CountingSemaphoreDao;
import org.sagebionetworks.repo.model.dbo.DBOBasicDao;
import org.sagebionetworks.repo.model.dbo.persistence.DBOCountingSemaphore;
import org.sagebionetworks.repo.model.dbo.persistence.DBOLockMaster;
import org.sagebionetworks.repo.model.exception.LockReleaseFailedException;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.dao.DeadlockLoserDataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Basic database backed implementation of the CountingSemaphoreDao.
 * 
 */
public class CountingSemaphoreDaoImpl implements CountingSemaphoreDao, BeanNameAware {

	private static final String SQL_COUNT_LOCKS = "SELECT COUNT(*) FROM " + TABLE_COUNTING_SEMAPHORE + " WHERE " + COL_COUNTING_SEMAPHORE_KEY
			+ " = ?";
	private static final String SQL_FORCE_RELEASE_EXPIRED_LOCKS = "DELETE FROM " + TABLE_COUNTING_SEMAPHORE + " WHERE "
			+ COL_COUNTING_SEMAPHORE_KEY + " = ? AND " + COL_COUNTING_SEMAPHORE_EXPIRES + " < ?";
	private static final String SQL_DELETE_ALL_LOCKS = "DELETE FROM " + TABLE_COUNTING_SEMAPHORE + " WHERE " + COL_COUNTING_SEMAPHORE_KEY
			+ " IS NOT NULL";
	private static final String SQL_SELECT_EXCLUSIVE_FOR_UPDATE = "SELECT " + COL_LOCK_MASTER_KEY + " FROM " + TABLE_LOCK_MASTER + " WHERE "
			+ COL_LOCK_MASTER_KEY + " = ? FOR UPDATE";
	private static final String SQL_RELEASE_LOCK = "DELETE FROM " + TABLE_COUNTING_SEMAPHORE + " WHERE " + COL_COUNTING_SEMAPHORE_KEY
			+ " = ? AND " + COL_COUNTING_SEMAPHORE_LOCK_TOKEN + " = ?";

	static private Logger log = LogManager.getLogger(CountingSemaphoreDaoImpl.class);

	@Autowired
	private DBOBasicDao basicDao;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	private long lockTimeoutMS;
	private String key;
	private int maxCount;

	@Required
	public void setLockTimeoutMS(long lockTimeoutMS) {
		if (lockTimeoutMS < 300) {
			throw new IllegalArgumentException("lockTimeoutMS should be greater than 300 ms");
		}
		this.lockTimeoutMS = lockTimeoutMS;
	}

	@Override
	public void setBeanName(String key) {
		if (StringUtils.isBlank(key)) {
			throw new IllegalArgumentException("bean name should not be an empty or blank string");
		}
		this.key = key;
	}

	@Required
	public void setMaxCount(int maxCount) {
		if (maxCount < 1) {
			throw new IllegalArgumentException("maxCount should be 1 or more, but was: " + maxCount);
		}
		this.maxCount = maxCount;
	}

	@PostConstruct
	public void init() {
		if (StringUtils.isBlank(key)) {
			throw new IllegalArgumentException("bean name should be set");
		}
		// create the exclusive lock entry
		DBOLockMaster lockMaster = new DBOLockMaster();
		lockMaster.setKey(key);
		try {
			log.info("Creating counting semaphore " + key);
			basicDao.createNew(lockMaster);
		} catch (IllegalArgumentException e) {
			if (e.getCause() != null && e.getCause().getClass() != DuplicateKeyException.class) {
				// we expect this one to happen on restart or during testing
				throw e;
			}
		}
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW, noRollbackFor = DeadlockLoserDataAccessException.class)
	@Override
	public String attemptToAcquireLock() {
		// insert a new lock entry, retry on contention
		for (int i = 0; i < 3; i++) {
			try {
				// First lock on the exclusive table entry
				try {
					jdbcTemplate.queryForObject(SQL_SELECT_EXCLUSIVE_FOR_UPDATE, String.class, key);
				} catch (EmptyResultDataAccessException e) {
					// if the semaphore was removed from the table due to testing, recreate it here and retry
					init();
					jdbcTemplate.queryForObject(SQL_SELECT_EXCLUSIVE_FOR_UPDATE, String.class, key);
				}

				// Count the number of current locks
				long count = countOutstandingNonExpiredLocks();
				if (count >= this.maxCount) {
					// all locks taken!
					return null;
				}

				DBOCountingSemaphore shared = new DBOCountingSemaphore();
				shared.setKey(key);
				shared.setToken(UUID.randomUUID().toString());
				shared.setExpires(System.currentTimeMillis() + lockTimeoutMS);
				basicDao.createNew(shared);
				return shared.getToken();
			} catch (DeadlockLoserDataAccessException e) {
			}
		}
		// could not acquire due to too many deadlocks
		return null;
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
	@Override
	public void releaseLock(String token) {
		int update = jdbcTemplate.update(SQL_RELEASE_LOCK, key, token);
		if (update < 1) {
			throw new LockReleaseFailedException("Failed to release the lock for key: " + key + " and token: " + token
					+ ".  Expired locks can be forcibly removed.");
		}
	}

	private long countOutstandingNonExpiredLocks() {
		// First delete all expired shared locks for this resource.
		this.jdbcTemplate.update(SQL_FORCE_RELEASE_EXPIRED_LOCKS, key, System.currentTimeMillis());
		// Now count the remaining locks
		return this.jdbcTemplate.queryForObject(SQL_COUNT_LOCKS, Long.class, key);
	}

	public void forceReleaseAllLocks() {
		// Force the delete of all locks
		// This is basically a truncate table with cascade deletes cleaning up the read locks.
		this.jdbcTemplate.update(SQL_DELETE_ALL_LOCKS);
	}
}