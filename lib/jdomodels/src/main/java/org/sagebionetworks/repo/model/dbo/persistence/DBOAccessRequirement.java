/**
 * 
 */
package org.sagebionetworks.repo.model.dbo.persistence;

import static org.sagebionetworks.repo.model.query.jdo.SqlConstants.COL_ACCESS_REQUIREMENT_ACCESS_TYPE;
import static org.sagebionetworks.repo.model.query.jdo.SqlConstants.COL_ACCESS_REQUIREMENT_CREATED_BY;
import static org.sagebionetworks.repo.model.query.jdo.SqlConstants.COL_ACCESS_REQUIREMENT_CREATED_ON;
import static org.sagebionetworks.repo.model.query.jdo.SqlConstants.COL_ACCESS_REQUIREMENT_ETAG;
import static org.sagebionetworks.repo.model.query.jdo.SqlConstants.COL_ACCESS_REQUIREMENT_ID;
import static org.sagebionetworks.repo.model.query.jdo.SqlConstants.COL_ACCESS_REQUIREMENT_MODIFIED_BY;
import static org.sagebionetworks.repo.model.query.jdo.SqlConstants.COL_ACCESS_REQUIREMENT_MODIFIED_ON;
import static org.sagebionetworks.repo.model.query.jdo.SqlConstants.COL_ACCESS_REQUIREMENT_SERIALIZED_ENTITY;
import static org.sagebionetworks.repo.model.query.jdo.SqlConstants.DDL_FILE_ACCESS_REQUIREMENT;
import static org.sagebionetworks.repo.model.query.jdo.SqlConstants.TABLE_ACCESS_REQUIREMENT;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.sagebionetworks.repo.model.ACTAccessRequirement;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.DatastoreException;
import org.sagebionetworks.repo.model.RestrictableObjectDescriptor;
import org.sagebionetworks.repo.model.RestrictableObjectType;
import org.sagebionetworks.repo.model.TermsOfUseAccessRequirement;
import org.sagebionetworks.repo.model.dbo.FieldColumn;
import org.sagebionetworks.repo.model.dbo.MigratableDatabaseObject;
import org.sagebionetworks.repo.model.dbo.TableMapping;
import org.sagebionetworks.repo.model.dbo.migration.MigratableTableTranslation;
import org.sagebionetworks.repo.model.jdo.JDOSecondaryPropertyUtils;
import org.sagebionetworks.repo.model.migration.MigrationType;
import org.sagebionetworks.util.Pair;

/**
 * @author brucehoff
 *
 */
public class DBOAccessRequirement implements MigratableDatabaseObject<DBOAccessRequirement, DBOAccessRequirementBackup> {
	private Long id;
	private String eTag;
	private Long createdBy;
	private long createdOn;
	private Long modifiedBy;
	private long modifiedOn;
	private String accessType;
	private byte[] serializedEntity;
	
	private static FieldColumn[] FIELDS = new FieldColumn[] {
		new FieldColumn("id", COL_ACCESS_REQUIREMENT_ID, true).withIsBackupId(true),
		new FieldColumn("eTag", COL_ACCESS_REQUIREMENT_ETAG).withIsEtag(true),
		new FieldColumn("createdBy", COL_ACCESS_REQUIREMENT_CREATED_BY),
		new FieldColumn("createdOn", COL_ACCESS_REQUIREMENT_CREATED_ON),
		new FieldColumn("modifiedBy", COL_ACCESS_REQUIREMENT_MODIFIED_BY),
		new FieldColumn("modifiedOn", COL_ACCESS_REQUIREMENT_MODIFIED_ON),
		new FieldColumn("accessType", COL_ACCESS_REQUIREMENT_ACCESS_TYPE),
		new FieldColumn("serializedEntity", COL_ACCESS_REQUIREMENT_SERIALIZED_ENTITY)
		};


	@Override
	public TableMapping<DBOAccessRequirement> getTableMapping() {
		return new TableMapping<DBOAccessRequirement>() {
			// Map a result set to this object
			@Override
			public DBOAccessRequirement mapRow(ResultSet rs, int rowNum) throws SQLException {
				DBOAccessRequirement ar = new DBOAccessRequirement();
				ar.setId(rs.getLong(COL_ACCESS_REQUIREMENT_ID));
				ar.seteTag(rs.getString(COL_ACCESS_REQUIREMENT_ETAG));
				ar.setCreatedBy(rs.getLong(COL_ACCESS_REQUIREMENT_CREATED_BY));
				ar.setCreatedOn(rs.getLong(COL_ACCESS_REQUIREMENT_CREATED_ON));
				ar.setModifiedBy(rs.getLong(COL_ACCESS_REQUIREMENT_MODIFIED_BY));
				ar.setModifiedOn(rs.getLong(COL_ACCESS_REQUIREMENT_MODIFIED_ON));
				ar.setAccessType(rs.getString(COL_ACCESS_REQUIREMENT_ACCESS_TYPE));
				java.sql.Blob blob = rs.getBlob(COL_ACCESS_REQUIREMENT_SERIALIZED_ENTITY);
				if(blob != null){
					ar.setSerializedEntity(blob.getBytes(1, (int) blob.length()));
				}
				return ar;
			}

			@Override
			public String getTableName() {
				return TABLE_ACCESS_REQUIREMENT;
			}

			@Override
			public String getDDLFileName() {
				return DDL_FILE_ACCESS_REQUIREMENT;
			}

			@Override
			public FieldColumn[] getFieldColumns() {
				return FIELDS;
			}

			@Override
			public Class<? extends DBOAccessRequirement> getDBOClass() {
				return DBOAccessRequirement.class;
			}
		};
	}


	public Long getId() {
		return id;
	}


	public void setId(Long id) {
		this.id = id;
	}


	public String geteTag() {
		return eTag;
	}


	public void seteTag(String eTag) {
		this.eTag = eTag;
	}


	public Long getCreatedBy() {
		return createdBy;
	}


	public void setCreatedBy(Long createdBy) {
		this.createdBy = createdBy;
	}



	public Long getModifiedBy() {
		return modifiedBy;
	}


	public void setModifiedBy(Long modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	public String getAccessType() {
		return accessType;
	}


	public void setAccessType(String accessType) {
		this.accessType = accessType;
	}

	public long getCreatedOn() {
		return createdOn;
	}


	public void setCreatedOn(long createdOn) {
		this.createdOn = createdOn;
	}


	public long getModifiedOn() {
		return modifiedOn;
	}


	public void setModifiedOn(long modifiedOn) {
		this.modifiedOn = modifiedOn;
	}

	public byte[] getSerializedEntity() {
		return serializedEntity;
	}


	public void setSerializedEntity(byte[] serializedEntity) {
		this.serializedEntity = serializedEntity;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((accessType == null) ? 0 : accessType.hashCode());
		result = prime * result
				+ ((createdBy == null) ? 0 : createdBy.hashCode());
		result = prime * result + (int) (createdOn ^ (createdOn >>> 32));
		result = prime * result + ((eTag == null) ? 0 : eTag.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result
				+ ((modifiedBy == null) ? 0 : modifiedBy.hashCode());
		result = prime * result + (int) (modifiedOn ^ (modifiedOn >>> 32));
		result = prime * result + Arrays.hashCode(serializedEntity);
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DBOAccessRequirement other = (DBOAccessRequirement) obj;
		if (accessType == null) {
			if (other.accessType != null)
				return false;
		} else if (!accessType.equals(other.accessType))
			return false;
		if (createdBy == null) {
			if (other.createdBy != null)
				return false;
		} else if (!createdBy.equals(other.createdBy))
			return false;
		if (createdOn != other.createdOn)
			return false;
		if (eTag == null) {
			if (other.eTag != null)
				return false;
		} else if (!eTag.equals(other.eTag))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (modifiedBy == null) {
			if (other.modifiedBy != null)
				return false;
		} else if (!modifiedBy.equals(other.modifiedBy))
			return false;
		if (modifiedOn != other.modifiedOn)
			return false;
		if (!Arrays.equals(serializedEntity, other.serializedEntity))
			return false;
		return true;
	}


	@Override
	public String toString() {
		return "DBOAccessRequirement [id=" + id + ", eTag=" + eTag
				+ ", createdBy=" + createdBy + ", createdOn=" + createdOn
				+ ", modifiedBy=" + modifiedBy + ", modifiedOn=" + modifiedOn
				+ ", accessType=" + accessType + ", serializedEntity="
				+ Arrays.toString(serializedEntity) + "]";
	}


	@Override
	public MigrationType getMigratableTableType() {
		return MigrationType.ACCESS_REQUIREMENT;
	}
	
	public static void copyEntityIdsToAccessRequirement(List<String> entityIds, AccessRequirement ar) {
		if (entityIds==null) return;
		if (ar.getSubjectIds()==null) ar.setSubjectIds(new ArrayList<RestrictableObjectDescriptor>());
		for (String entityId : entityIds) {
			RestrictableObjectDescriptor subjectId = new RestrictableObjectDescriptor();
			subjectId.setId(entityId);
			subjectId.setType(RestrictableObjectType.ENTITY);
			if (!ar.getSubjectIds().contains(subjectId)) ar.getSubjectIds().add(subjectId);
		}	
	}

	public static TermsOfUseAccessRequirement translateDto(TermsOfUseAccessRequirementLegacy origDto) {
		TermsOfUseAccessRequirement newDto = new TermsOfUseAccessRequirement();
		newDto.setAccessType(origDto.getAccessType());
		newDto.setConcreteType(TermsOfUseAccessRequirement.class.getName());
		newDto.setCreatedBy(origDto.getCreatedBy());
		newDto.setCreatedOn(origDto.getCreatedOn());
		newDto.setEtag(origDto.getEtag());
		newDto.setId(origDto.getId());
		newDto.setModifiedBy(origDto.getModifiedBy());
		newDto.setModifiedOn(origDto.getModifiedOn());
		newDto.setSubjectIds(origDto.getSubjectIds());
		newDto.setTermsOfUse(origDto.getTermsOfUse());
		return newDto;
	}

	public static ACTAccessRequirement translateDto(ACTAccessRequirementLegacy origDto) {
		ACTAccessRequirement newDto = new ACTAccessRequirement();
		newDto.setAccessType(origDto.getAccessType());
		newDto.setConcreteType(ACTAccessRequirement.class.getName());
		newDto.setCreatedBy(origDto.getCreatedBy());
		newDto.setCreatedOn(origDto.getCreatedOn());
		newDto.setEtag(origDto.getEtag());
		newDto.setId(origDto.getId());
		newDto.setModifiedBy(origDto.getModifiedBy());
		newDto.setModifiedOn(origDto.getModifiedOn());
		newDto.setSubjectIds(origDto.getSubjectIds());
		newDto.setActContactInfo(origDto.getActContactInfo());
		return newDto;
	}

	@Override
	public MigratableTableTranslation<DBOAccessRequirement, DBOAccessRequirementBackup> getTranslator() {
		return new MigratableTableTranslation<DBOAccessRequirement, DBOAccessRequirementBackup>(){

			@Override
			public DBOAccessRequirement createDatabaseObjectFromBackup(
					DBOAccessRequirementBackup backup) {
				DBOAccessRequirement dbo = new DBOAccessRequirement();
				dbo.setId(backup.getId());
				dbo.seteTag(backup.geteTag());
				dbo.setCreatedBy(backup.getCreatedBy());
				dbo.setCreatedOn(backup.getCreatedOn());
				dbo.setModifiedBy(backup.getModifiedBy());
				dbo.setModifiedOn(backup.getModifiedOn());
				dbo.setAccessType(backup.getAccessType());
				try {
					byte[] serialized = backup.getSerializedEntity();
					List<Pair<String,Class>> aliases = new ArrayList<Pair<String,Class>>();
					aliases.add(Pair.create("org.sagebionetworks.repo.model.TermsOfUseAccessRequirement", (Class)TermsOfUseAccessRequirementLegacy.class));
					aliases.add(Pair.create("org.sagebionetworks.repo.model.ACTAccessRequirement", (Class)ACTAccessRequirementLegacy.class));
					Object origDto = JDOSecondaryPropertyUtils.decompressedObject(serialized, aliases);
					// convert old dto to new dto
					if (origDto instanceof TermsOfUseAccessRequirementLegacy) {
						TermsOfUseAccessRequirement newDto = translateDto((TermsOfUseAccessRequirementLegacy)origDto);
						dbo.setSerializedEntity(JDOSecondaryPropertyUtils.compressObject(newDto));
					} else if (origDto instanceof ACTAccessRequirementLegacy) {
						ACTAccessRequirement newDto = translateDto((ACTAccessRequirementLegacy)origDto);
						dbo.setSerializedEntity(JDOSecondaryPropertyUtils.compressObject(newDto));
					} else {
						// nothing to translate
						dbo.setSerializedEntity(serialized);
					}
				} catch (IOException e) {
					throw new DatastoreException(e);
				}
				return dbo;
			}
			
			@Override
			public DBOAccessRequirementBackup createBackupFromDatabaseObject(
					DBOAccessRequirement dbo) {
				DBOAccessRequirementBackup backup = new DBOAccessRequirementBackup();
				backup.setAccessType(dbo.getAccessType());
				backup.setCreatedBy(dbo.getCreatedBy());
				backup.setCreatedOn(dbo.getCreatedOn());
				backup.seteTag(dbo.geteTag());
				backup.setId(dbo.getId());
				backup.setModifiedBy(dbo.getModifiedBy());
				backup.setModifiedOn(dbo.getModifiedOn());
				backup.setSerializedEntity(dbo.getSerializedEntity());
				return backup;
			}};
	}

	@Override
	public Class<? extends DBOAccessRequirementBackup> getBackupClass() {
		return DBOAccessRequirementBackup.class;
	}


	@Override
	public Class<? extends DBOAccessRequirement> getDatabaseObjectClass() {
		return DBOAccessRequirement.class;
	}


	@Override
	public List<MigratableDatabaseObject> getSecondaryTypes() {
		List<MigratableDatabaseObject> list = new LinkedList<MigratableDatabaseObject>();
		list.add(new DBOSubjectAccessRequirement());
		return list;
	}
}
