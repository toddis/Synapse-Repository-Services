package org.sagebionetworks.repo.manager.asynch;

import org.sagebionetworks.repo.model.DatastoreException;
import org.sagebionetworks.repo.model.UserInfo;
import org.sagebionetworks.repo.model.asynch.AsynchronousJobStatus;
import org.sagebionetworks.repo.model.asynch.AsynchronousRequestBody;
import org.sagebionetworks.repo.model.asynch.AsynchronousResponseBody;
import org.sagebionetworks.repo.web.NotFoundException;

/**
 * Abstraction for an AsynchronousJobStatus CRUD.
 * 
 * @author John
 *
 */
public interface AsynchJobStatusManager {
	
	/**
	 * Start a new job.
	 * 
	 * @param user
	 * @param body
	 * @return
	 * @throws NotFoundException 
	 * @throws DatastoreException 
	 */
	public AsynchronousJobStatus startJob(UserInfo user, AsynchronousRequestBody body) throws DatastoreException, NotFoundException;
	
	/**
	 * Get the current status of a job.
	 * 
	 * @param user
	 * @param jobId
	 * @return
	 * @throws NotFoundException 
	 * @throws DatastoreException 
	 */
	public AsynchronousJobStatus getJobStatus(UserInfo user, String jobId) throws DatastoreException, NotFoundException;
	
	/**
	 * Update the progress of a job.
	 * This method should only be called by a worker.
	 * 
	 * @param jobId
	 * @param progressCurrent
	 * @param progressTotal
	 * @param progressMessage
	 * @return
	 */
	public String updateJobProgress(String jobId, Long progressCurrent, Long progressTotal, String progressMessage);
	
	
	/**
	 * Set a job to failed.
	 * This method should only be called by a worker.
	 * 
	 * @param jobId
	 * @param error
	 * @return
	 */
	public String setJobFailed(String jobId, Throwable error);
	
	/**
	 * Set a job to complete.
	 * This method should only be called by a worker.
	 * 
	 * @param body The final body of the job.
	 * @return
	 * @throws NotFoundException 
	 * @throws DatastoreException 
	 */
	public String setComplete(String jobId, AsynchronousResponseBody body) throws DatastoreException, NotFoundException;

	public void emptyAllQueues();

}
