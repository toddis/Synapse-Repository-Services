package org.sagebionetworks.repo.model;

import java.util.List;

import org.sagebionetworks.repo.model.project.ProjectSetting;
import org.sagebionetworks.repo.web.NotFoundException;

public interface ProjectSettingsDAO {

	public String create(ProjectSetting settings) throws DatastoreException, InvalidModelException;

	public ProjectSetting get(String id) throws DatastoreException, NotFoundException;

	public ProjectSetting get(String projectId, String type) throws DatastoreException;

	public List<ProjectSetting> getAllForProject(String projectId) throws DatastoreException, NotFoundException;

	public ProjectSetting update(ProjectSetting settings) throws DatastoreException, InvalidModelException, NotFoundException,
			ConflictingUpdateException;

	public void delete(String id) throws DatastoreException, NotFoundException;
}
