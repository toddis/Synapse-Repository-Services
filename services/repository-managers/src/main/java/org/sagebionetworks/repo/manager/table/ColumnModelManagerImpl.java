package org.sagebionetworks.repo.manager.table;

import java.util.List;
import java.util.Set;

import org.sagebionetworks.repo.manager.AuthorizationManager;
import org.sagebionetworks.repo.manager.AuthorizationManagerUtil;
import org.sagebionetworks.repo.model.ACCESS_TYPE;
import org.sagebionetworks.repo.model.DatastoreException;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.UnauthorizedException;
import org.sagebionetworks.repo.model.UserInfo;
import org.sagebionetworks.repo.model.dao.table.ColumnModelDAO;
import org.sagebionetworks.repo.model.dao.table.TableStatusDAO;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.PaginatedColumnModels;
import org.sagebionetworks.repo.model.table.PaginatedIds;
import org.sagebionetworks.repo.model.table.TableConstants;
import org.sagebionetworks.repo.web.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;

/**
 * Basic implementation of the ColumnModelManager.
 * 
 * @author John
 *
 */
public class ColumnModelManagerImpl implements ColumnModelManager {

	@Autowired
	ColumnModelDAO columnModelDao;
	@Autowired
	TableStatusDAO tableStatusDAO;
	
	@Autowired
	AuthorizationManager authorizationManager;

	@Override
	public PaginatedColumnModels listColumnModels(UserInfo user, String namePrefix, long limit, long offset) {
		if(user == null) throw new IllegalArgumentException("User cannot be null");
		validateLimitOffset(limit, offset);
		// First call is to list the columns.
		List<ColumnModel> list = columnModelDao.listColumnModels(namePrefix, limit, offset);
		// second to get the total number of results with this prefix
		long totalNumberOfResults = columnModelDao.listColumnModelsCount(namePrefix);
		PaginatedColumnModels pcm = new PaginatedColumnModels();
		pcm.setResults(list);
		pcm.setTotalNumberOfResults(totalNumberOfResults);
		return pcm;
	}

	private void validateLimitOffset(long limit, long offset) {
		if(limit < 0) throw new IllegalArgumentException("Limit cannot be less than zero");
		if(limit > 100) throw new IllegalArgumentException("Limit cannot be greater than 100");
		if(offset < 0) throw new IllegalArgumentException("Offset cannot be less than zero");
	}
	
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	@Override
	public ColumnModel createColumnModel(UserInfo user, ColumnModel columnModel) throws UnauthorizedException, DatastoreException, NotFoundException{
		if(user == null) throw new IllegalArgumentException("User cannot be null");
		// Must login to create a column model.
		if(authorizationManager.isAnonymousUser(user)){
			throw new UnauthorizedException("You must login to create a ColumnModel");
		}
		checkColumnNaming(columnModel);
		// Pass it along to the DAO.
		return columnModelDao.createColumnModel(columnModel);
	}
	
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	@Override
	public List<ColumnModel> createColumnModels(UserInfo user, List<ColumnModel> columnModels) throws DatastoreException, NotFoundException {
		if (user == null)
			throw new IllegalArgumentException("User cannot be null");
		// Must login to create a column model.
		if (authorizationManager.isAnonymousUser(user)) {
			throw new UnauthorizedException("You must login to create a ColumnModel");
		}
		// first quickly check for naming errors
		for (ColumnModel columnModel : columnModels) {
			checkColumnNaming(columnModel);
		}
		// the create all column models
		List<ColumnModel> results = Lists.newArrayListWithCapacity(columnModels.size());
		for (ColumnModel columnModel : columnModels) {
			// Pass it along to the DAO.
			results.add(columnModelDao.createColumnModel(columnModel));
		}
		return results;
	}

	private void checkColumnNaming(ColumnModel columnModel) {
		// Validate the name
		if (TableConstants.isReservedColumnName(columnModel.getName())) {
			throw new IllegalArgumentException("The column name: " + columnModel.getName() + " is a system reserved column name.");
		}
		// Is the name a key word?
		if (TableConstants.isKeyWord(columnModel.getName())) {
			throw new IllegalArgumentException("The name: " + columnModel.getName()
					+ " is a SQL key word and cannot be used as a column name.");
		}
	}

	@Override
	public ColumnModel getColumnModel(UserInfo user, String columnId) throws DatastoreException, NotFoundException {
		if(user == null) throw new IllegalArgumentException("User cannot be null");
		if(columnId == null) throw new IllegalArgumentException("ColumnId cannot be null");
		return columnModelDao.getColumnModel(columnId);
	}

	@Override
	public List<ColumnModel> getColumnModel(UserInfo user, List<String> ids, boolean keepOrder)
			throws DatastoreException, NotFoundException {
		if(user == null) throw new IllegalArgumentException("User cannot be null");
		if(ids == null) throw new IllegalArgumentException("ColumnModel IDs cannot be null");
		return columnModelDao.getColumnModel(ids, keepOrder);
	}
	
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	@Override
	public boolean bindColumnToObject(UserInfo user, List<String> columnIds, String objectId, boolean isNew) throws DatastoreException, NotFoundException {
		if(user == null) throw new IllegalArgumentException("User cannot be null");
		// pass it along to the DAO.
		long count = columnModelDao.bindColumnToObject(columnIds, objectId);
		// If there was an actual change we need change the status of the table.
		if(count > 0 || isNew){
			// The table has change so we must rest the state to processing.
			tableStatusDAO.resetTableStatusToProcessing(objectId);
			return true;
		}else{
			return false;
		}
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	@Override
	public void unbindAllColumnsAndOwnerFromObject(String objectId) {
		columnModelDao.unbindAllColumnsFromObject(objectId);
		columnModelDao.deleteOwner(objectId);
	}

	@Override
	public PaginatedIds listObjectsBoundToColumn(UserInfo user,	Set<String> columnIds, boolean currentOnly, long limit, long offset) {
		if(user == null) throw new IllegalArgumentException("User cannot be null");
		validateLimitOffset(limit, offset);
		if(columnIds == null) throw new IllegalArgumentException("ColumnModel IDs cannot be null");
		List<String> results = columnModelDao.listObjectsBoundToColumn(columnIds, currentOnly, limit, offset);
		long totalCount = columnModelDao.listObjectsBoundToColumnCount(columnIds, currentOnly);
		PaginatedIds page = new PaginatedIds();
		page.setResults(results);
		page.setTotalNumberOfResults(totalCount);
		return page;
	}

	@Override
	public boolean truncateAllColumnData(UserInfo user) {
		if(user == null) throw new IllegalArgumentException("User cannot be null");
		if(!user.isAdmin()) throw new UnauthorizedException("Only an Administrator can call this method");
		return this.columnModelDao.truncateAllColumnData();
	}

	@Override
	public List<ColumnModel> getColumnModelsForTable(UserInfo user,
			String tableId) throws DatastoreException, NotFoundException {
		// The user must be granted read permission on the table to get the columns.
		AuthorizationManagerUtil.checkAuthorizationAndThrowException(
				authorizationManager.canAccess(user, tableId, ObjectType.ENTITY, ACCESS_TYPE.READ));
		return columnModelDao.getColumnModelsForObject(tableId);
	}
	
	
}
