package org.sagebionetworks.client;


import static org.sagebionetworks.client.SynapseClientImpl.ASYNC_GET;
import static org.sagebionetworks.client.SynapseClientImpl.ASYNC_START;
import static org.sagebionetworks.client.SynapseClientImpl.TABLE_DOWNLOAD_CSV;
import static org.sagebionetworks.client.SynapseClientImpl.TABLE_QUERY;
import static org.sagebionetworks.client.SynapseClientImpl.TABLE_QUERY_NEXTPAGE;
import static org.sagebionetworks.client.SynapseClientImpl.TABLE_UPLOAD_CSV;
import static org.sagebionetworks.client.SynapseClientImpl.TABLE_UPLOAD_CSV_PREVIEW;

import org.sagebionetworks.repo.model.asynch.AsynchronousResponseBody;
import org.sagebionetworks.repo.model.table.DownloadFromTableResult;
import org.sagebionetworks.repo.model.table.QueryResult;
import org.sagebionetworks.repo.model.table.QueryResultBundle;
import org.sagebionetworks.repo.model.table.UploadToTablePreviewResult;
import org.sagebionetworks.repo.model.table.UploadToTableResult;
/**
 * Maps job types to the URL prefix needed for each type.
 * 
 * @author John
 *
 */
public enum AsynchJobType {
	
	TableQuery(TABLE_QUERY, QueryResultBundle.class),
	TableQueryNextPage(TABLE_QUERY_NEXTPAGE, QueryResult.class),
	TableCSVUpload(TABLE_UPLOAD_CSV, UploadToTableResult.class),
	TableCSVUploadPreview(TABLE_UPLOAD_CSV_PREVIEW, UploadToTablePreviewResult.class),
	TableCSVDownload(TABLE_DOWNLOAD_CSV, DownloadFromTableResult.class);
	
	String prefix;
	Class<? extends AsynchronousResponseBody> responseClass;
	
	AsynchJobType(String prefix, Class<? extends AsynchronousResponseBody> responseClass){
		this.prefix = prefix;
		this.responseClass = responseClass;
	}
	
	/**
	 * Get the URL used to start this job type.
	 */
	public  String getStartUrl(){
		return prefix+ASYNC_START;
	}

	/**
	 * Get the URL used to get the results for this job type.
	 * @param token
	 * @return
	 */
	public String getResultUrl(String token){
		return prefix+ASYNC_GET + token;
	}
	
	/**
	 * Get the response class.
	 * @return
	 */
	public Class<? extends AsynchronousResponseBody> getReponseClass(){
		return responseClass;
	}
}
