/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.agnitas.beans.ColumnMapping;
import org.agnitas.beans.ImportProfile;
import org.agnitas.beans.impl.ColumnMappingImpl;
import org.agnitas.beans.impl.ImportProfileImpl;
import org.agnitas.dao.EmmActionOperationDao;
import org.agnitas.dao.ImportProfileDao;
import org.agnitas.dao.impl.mapper.IntegerRowMapper;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.DataEncryptor;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.util.CollectionUtils;

import com.agnitas.dao.ComMailingDao;
import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.emm.core.action.operations.AbstractActionOperationParameters;
import com.agnitas.emm.core.action.operations.ActionOperationSendMailingParameters;

/**
 * DAO handler for ImportProfile-Objects
 * This class is compatible with oracle and mysql datasources and databases
 */
public class ImportProfileDaoImpl extends BaseDaoImpl implements ImportProfileDao {
	private static final transient Logger logger = Logger.getLogger(ImportProfileDaoImpl.class);

	private static final String TABLE = "import_profile_tbl";
	
	private static final String FIELD_ID = "id";
	private static final String FIELD_SHORTNAME = "shortname";
	private static final String FIELD_COMPANY_ID = "company_id";
	private static final String FIELD_ADMIN_ID = "admin_id";
	private static final String FIELD_COLUMN_SEPARATOR = "column_separator";
	private static final String FIELD_TEXT_DELIMITER = "text_delimiter";
	private static final String FIELD_FILE_CHARSET = "file_charset";
	private static final String FIELD_DATE_FORMAT = "date_format";
	private static final String FIELD_IMPORT_MODE = "import_mode";
	private static final String FIELD_KEY_COLUMN = "key_column";
	private static final String FIELD_CHECK_FOR_DUPLICATES = "check_for_duplicates";
	private static final String FIELD_NULL_VALUES_ACTION = "null_values_action";
	private static final String FIELD_REPORT_EMAIL = "report_email";
	private static final String FIELD_ERROR_EMAIL = "error_email";
	private static final String FIELD_MAIL_TYPE = "mail_type";
	private static final String FIELD_UPDATE_ALL_DUPLICATES = "update_all_duplicates";
	private static final String FIELD_PRE_IMPORT_ACTION = "pre_import_action";
	private static final String FIELD_DECIMAL_SEPARATOR = "decimal_separator";
	private static final String FIELD_NEW_RECIPIENTS_ACTION = "action_for_new_recipients";
	private static final String FIELD_DELETED = "deleted";
	private static final String FIELD_NOHEADERS = "noheaders";
	private static final String FIELD_ZIP = "zip";
	private static final String FIELD_ZIP_PASSWORD_ENCRYPTED = "zip_password_encr";

	private static final String SELECT_NEXT_PROFILEID = "SELECT import_profile_tbl_seq.nextval FROM DUAL";
	private static final String SELECT_BY_ID = "SELECT * FROM " + TABLE + " WHERE " + FIELD_ID + " = ? AND " + FIELD_DELETED + " != 1";
	private static final String SELECT_BY_SHORTNAME = "SELECT * FROM " + TABLE + " WHERE UPPER(" + FIELD_SHORTNAME + ") = UPPER(?) AND " + FIELD_DELETED + " != 1";
	private static final String SELECT_BY_COMPANYID = "SELECT * FROM " + TABLE + " WHERE " + FIELD_COMPANY_ID + " = ? AND " + FIELD_DELETED + " != 1 ORDER BY LOWER(" + FIELD_SHORTNAME + ") ASC";
	private static final String DELETE = "DELETE FROM " + TABLE + " WHERE " + FIELD_ID + " = ?";
	private static final String UPDATE = "UPDATE " + TABLE + " SET " + FIELD_COMPANY_ID + " = ?, " + FIELD_ADMIN_ID + " = ?, " + FIELD_SHORTNAME + " = ?, " + FIELD_COLUMN_SEPARATOR + " = ?, " + FIELD_TEXT_DELIMITER + " = ?, " + FIELD_FILE_CHARSET + " = ?, " + FIELD_DATE_FORMAT + " = ?, " + FIELD_IMPORT_MODE + " = ?, " + FIELD_NULL_VALUES_ACTION + " = ?, " + FIELD_KEY_COLUMN + " = ?, " + FIELD_REPORT_EMAIL + " = ?, " + FIELD_ERROR_EMAIL + " = ?, " + FIELD_CHECK_FOR_DUPLICATES + " = ?, " + FIELD_MAIL_TYPE + " = ?, " + FIELD_UPDATE_ALL_DUPLICATES + " = ?, " + FIELD_PRE_IMPORT_ACTION + " = ?, " + FIELD_DECIMAL_SEPARATOR + " = ?, " + FIELD_NEW_RECIPIENTS_ACTION + " = ?, " + FIELD_NOHEADERS + " = ?, " + FIELD_ZIP + " = ?, " + FIELD_ZIP_PASSWORD_ENCRYPTED + " = ?, automapping = ? WHERE " + FIELD_ID + " = ?";
	private static final String INSERT_ORACLE = "INSERT INTO " + TABLE + " (" + FIELD_ID + ", " + FIELD_COMPANY_ID + ", " + FIELD_ADMIN_ID + ", " + FIELD_SHORTNAME + ", " + FIELD_COLUMN_SEPARATOR + ", " + FIELD_TEXT_DELIMITER + ", " + FIELD_FILE_CHARSET + ", " + FIELD_DATE_FORMAT + ", " + FIELD_IMPORT_MODE + ", " + FIELD_NULL_VALUES_ACTION + ", " + FIELD_KEY_COLUMN + ", " + FIELD_REPORT_EMAIL + ", " + FIELD_ERROR_EMAIL + ", " + FIELD_CHECK_FOR_DUPLICATES + ", " + FIELD_MAIL_TYPE + ", " + FIELD_UPDATE_ALL_DUPLICATES + ", " + FIELD_PRE_IMPORT_ACTION + ", " + FIELD_DECIMAL_SEPARATOR + ", " + FIELD_NEW_RECIPIENTS_ACTION + ", " + FIELD_NOHEADERS + ", " + FIELD_ZIP + ", " + FIELD_ZIP_PASSWORD_ENCRYPTED + ", automapping) VALUES(" + AgnUtils.repeatString("?", 23, ", ") + ")";
	private static final String INSERT_MYSQL = "INSERT INTO " + TABLE + " (" + FIELD_COMPANY_ID + ", " + FIELD_ADMIN_ID + ", " + FIELD_SHORTNAME + ", " + FIELD_COLUMN_SEPARATOR + ", " + FIELD_TEXT_DELIMITER + ", " + FIELD_FILE_CHARSET + ", " + FIELD_DATE_FORMAT + ", " + FIELD_IMPORT_MODE + ", " + FIELD_NULL_VALUES_ACTION + ", " + FIELD_KEY_COLUMN + ", " + FIELD_REPORT_EMAIL + ", " + FIELD_ERROR_EMAIL + ", "  + FIELD_CHECK_FOR_DUPLICATES + ", " + FIELD_MAIL_TYPE + ", " + FIELD_UPDATE_ALL_DUPLICATES + ", " + FIELD_PRE_IMPORT_ACTION + ", " + FIELD_DECIMAL_SEPARATOR + ", " + FIELD_NEW_RECIPIENTS_ACTION + ", " + FIELD_NOHEADERS + ", " + FIELD_ZIP + ", " + FIELD_ZIP_PASSWORD_ENCRYPTED + ", automapping) VALUES(" + AgnUtils.repeatString("?", 22, ", ") + ")";
	
	// COLUMN_MAPPING Table
	private static final String COLUMN_MAPPING_TABLE = "import_column_mapping_tbl";
	private static final String COLUMN_MAPPING_ID = "id";
	private static final String COLUMN_MAPPING_PROFILE_ID = "profile_id";
	private static final String COLUMN_MAPPING_MANDATORY = "mandatory";
	private static final String COLUMN_MAPPING_ENCRYPTED = "encrypted";
	private static final String COLUMN_MAPPING_DB_COLUMN = "db_column";
	private static final String COLUMN_MAPPING_FILE_COLUMN = "file_column";
	private static final String COLUMN_MAPPING_DEFAULT_VALUE = "default_value";
	private static final String SELECT_COLUMN_MAPPINGS = "SELECT * FROM " + COLUMN_MAPPING_TABLE + " WHERE " + COLUMN_MAPPING_PROFILE_ID + " = ? AND " + FIELD_DELETED + " != 1";
	private static final String DELETE_COLUMN_MAPPINGS = "DELETE FROM " + COLUMN_MAPPING_TABLE + " WHERE " + COLUMN_MAPPING_PROFILE_ID + " = ?";
	private static final String INSERT_COLUMN_MAPPINGS_ORACLE = "INSERT INTO " + COLUMN_MAPPING_TABLE + " (" + COLUMN_MAPPING_ID + ", " + COLUMN_MAPPING_PROFILE_ID + ", " + COLUMN_MAPPING_FILE_COLUMN + ", " + COLUMN_MAPPING_DB_COLUMN + ", " + COLUMN_MAPPING_MANDATORY + ", " + COLUMN_MAPPING_ENCRYPTED + ", " + COLUMN_MAPPING_DEFAULT_VALUE + ") VALUES (import_column_mapping_tbl_seq.NEXTVAL, ?, ?, ?, ?, ?, ?)";
	private static final String INSERT_COLUMN_MAPPINGS_MYSQL = "INSERT INTO " + COLUMN_MAPPING_TABLE + " (" + COLUMN_MAPPING_PROFILE_ID + ", " + COLUMN_MAPPING_FILE_COLUMN + ", " + COLUMN_MAPPING_DB_COLUMN + ", " + COLUMN_MAPPING_MANDATORY + ", " + COLUMN_MAPPING_ENCRYPTED + ", " + COLUMN_MAPPING_DEFAULT_VALUE + ") VALUES (?, ?, ?, ?, ?, ?)";
	private static final String UPDATE_COLUMN_MAPPINGS = "UPDATE " + COLUMN_MAPPING_TABLE + " SET " + COLUMN_MAPPING_PROFILE_ID + " = ?, " + COLUMN_MAPPING_FILE_COLUMN + " = ?, " + COLUMN_MAPPING_DB_COLUMN + " = ?, " + COLUMN_MAPPING_MANDATORY + " = ?, " + COLUMN_MAPPING_ENCRYPTED + " = ?, " + COLUMN_MAPPING_DEFAULT_VALUE + " = ? WHERE " + COLUMN_MAPPING_ID + " = ?";

	// GENDER_MAPPING Table
	private static final String GENDER_MAPPING_TABLE = "import_gender_mapping_tbl";
	private static final String GENDER_MAPPING_ID = "id";
	private static final String GENDER_MAPPING_PROFILE_ID = "profile_id";
	private static final String GENDER_MAPPING_STRING_GENDER = "string_gender";
	private static final String GENDER_MAPPING_INT_GENDER = "int_gender";
	private static final String SELECT_GENDER_MAPPINGS = "SELECT * FROM " + GENDER_MAPPING_TABLE + " WHERE " + GENDER_MAPPING_PROFILE_ID + " = ? AND " + FIELD_DELETED + " != 1 ORDER BY " + GENDER_MAPPING_ID;
	private static final String DELETE_GENDER_MAPPINGS = "DELETE FROM " + GENDER_MAPPING_TABLE + " WHERE " + GENDER_MAPPING_PROFILE_ID + " = ?";
	private static final String INSERT_GENDER_MAPPINGS_ORACLE = "INSERT INTO " + GENDER_MAPPING_TABLE + " (" + GENDER_MAPPING_ID + ", " + GENDER_MAPPING_PROFILE_ID + ", " + GENDER_MAPPING_INT_GENDER + ", " + GENDER_MAPPING_STRING_GENDER + ") VALUES (import_gender_mapping_tbl_seq.nextval, ?, ?, ?)";
	private static final String INSERT_GENDER_MAPPINGS_MYSQL = "INSERT INTO " + GENDER_MAPPING_TABLE + " (" + GENDER_MAPPING_PROFILE_ID + ", " + GENDER_MAPPING_INT_GENDER + ", " + GENDER_MAPPING_STRING_GENDER + ") VALUES (?, ?, ?)";

	protected DataEncryptor dataEncryptor;

	private EmmActionOperationDao emmActionOperationDao;
	
    private ComMailingDao mailingDao;

	@Required
	public void setDataEncryptor(DataEncryptor dataEncryptor) {
		this.dataEncryptor = dataEncryptor;
	}
	
	@Required
	public void setEmmActionOperationDao(EmmActionOperationDao emmActionOperationDao) {
		this.emmActionOperationDao = emmActionOperationDao;
	}
	
	@Required
	public void setMailingDao(ComMailingDao mailingDao) {
		this.mailingDao = mailingDao;
	}

	@Override
	@DaoUpdateReturnValueCheck
	public int insertImportProfile(ImportProfile importProfile) throws Exception {
		int profileId;
		if (isOracleDB()) {
			profileId = selectInt(logger, SELECT_NEXT_PROFILEID);
	        
			update(logger, 
				INSERT_ORACLE,
				profileId,
				importProfile.getCompanyId(),
				importProfile.getAdminId(),
				importProfile.getName(),
				importProfile.getSeparator(),
				importProfile.getTextRecognitionChar(),
				importProfile.getCharset(),
				importProfile.getDateFormat(),
				importProfile.getImportMode(),
				importProfile.getNullValuesAction(),
				StringUtils.join(importProfile.getKeyColumns(), ", "),
				importProfile.getMailForReport(),
				importProfile.getMailForError(),
				importProfile.getCheckForDuplicates(),
				importProfile.getDefaultMailType(),
				importProfile.getUpdateAllDuplicates() ? 1 : 0,
				importProfile.getImportProcessActionID(),
				Character.toString(importProfile.getDecimalSeparator()),
				importProfile.getActionForNewRecipients(),
				importProfile.isNoHeaders() ? 1 : 0,
				importProfile.isZipped() ? 1 : 0,
				StringUtils.isEmpty(importProfile.getZipPassword()) ? null : dataEncryptor.encrypt(importProfile.getZipPassword()),
				importProfile.isAutoMapping() ? 1 : 0
			);
		} else {
			profileId = insertIntoAutoincrementMysqlTable(logger, FIELD_ID, INSERT_MYSQL, 
        		importProfile.getCompanyId(),
				importProfile.getAdminId(),
				importProfile.getName(),
				importProfile.getSeparator(),
				importProfile.getTextRecognitionChar(),
				importProfile.getCharset(),
				importProfile.getDateFormat(),
				importProfile.getImportMode(),
				importProfile.getNullValuesAction(),
				StringUtils.join(importProfile.getKeyColumns(), ", "),
				importProfile.getMailForReport(),
				importProfile.getMailForError(),
				importProfile.getCheckForDuplicates(),
				importProfile.getDefaultMailType(),
				importProfile.getUpdateAllDuplicates() ? 1 : 0,
				importProfile.getImportProcessActionID(),
				Character.toString(importProfile.getDecimalSeparator()),
				importProfile.getActionForNewRecipients(),
				importProfile.isNoHeaders() ? 1 : 0,
				importProfile.isZipped() ? 1 : 0,
				StringUtils.isEmpty(importProfile.getZipPassword()) ? null : dataEncryptor.encrypt(importProfile.getZipPassword()),
				importProfile.isAutoMapping() ? 1 : 0
			);
		}

		importProfile.setId(profileId);
		insertGenderMappings(importProfile.getGenderMapping(), importProfile.getId());
		updateMailinglists(importProfile);

		return importProfile.getId();
	}

	@Override
	@DaoUpdateReturnValueCheck
	public void updateImportProfile(ImportProfile importProfile) throws Exception {
		update(logger, 
			UPDATE, 
			importProfile.getCompanyId(),
			importProfile.getAdminId(),
			importProfile.getName(),
			importProfile.getSeparator(),
			importProfile.getTextRecognitionChar(),
			importProfile.getCharset(),
			importProfile.getDateFormat(),
			importProfile.getImportMode(),
			importProfile.getNullValuesAction(),
			StringUtils.join(importProfile.getKeyColumns(), ", "),
			importProfile.getMailForReport(),
			importProfile.getMailForError(),
			importProfile.getCheckForDuplicates(),
			importProfile.getDefaultMailType(),
			importProfile.getUpdateAllDuplicates() ? 1 : 0,
			importProfile.getImportProcessActionID(),
			Character.toString(importProfile.getDecimalSeparator()),
			importProfile.getActionForNewRecipients(),
			importProfile.isNoHeaders() ? 1 : 0,
			importProfile.isZipped() ? 1 : 0,
			StringUtils.isEmpty(importProfile.getZipPassword()) ? null : dataEncryptor.encrypt(importProfile.getZipPassword()),
			importProfile.isAutoMapping() ? 1 : 0,
			importProfile.getId()
		);

		update(logger, DELETE_GENDER_MAPPINGS, importProfile.getId());
		insertGenderMappings(importProfile.getGenderMapping(), importProfile.getId());
		updateMailinglists(importProfile);
	}

	private void updateMailinglists(ImportProfile profile) {
		// Keep old mailing list bindings untouched if EMM action is selected.
		if (profile.getActionForNewRecipients() == 0) {
			updateMailinglists(profile.getMailinglistIds(), profile.getId(), profile.getCompanyId());
		}
	}

	private void updateMailinglists(List<Integer> mailinglists, int profileId, int companyId) {
		String cleanMailinglistsSql = "DELETE FROM import_profile_mlist_bind_tbl WHERE company_id = ? AND import_profile_id = ?";
		update(logger, cleanMailinglistsSql, companyId, profileId);

		String insertMailinglists = "INSERT INTO import_profile_mlist_bind_tbl(import_profile_id, mailinglist_id, company_id) VALUES (?, ?, ?)";
		List<Object[]> params = new ArrayList<>();
		for(int mailinglistId : mailinglists) {
			params.add(new Object[]{profileId, mailinglistId, companyId});
		}

		batchupdate(logger, insertMailinglists, params);
	}

	@Override
    public ImportProfile getImportProfileById(int id) {
		try {
			return selectObjectDefaultNull(logger, SELECT_BY_ID, new ImportProfileRowMapper(), id);
		} catch (DataAccessException e) {
			// No ImportProfile found
			return null;
		}
    }

	@Override
    public ImportProfile getImportProfileByShortname(String shortname) {
		return selectObjectDefaultNull(logger, SELECT_BY_SHORTNAME, new ImportProfileRowMapper(), shortname);
    }

	@Override
    public List<ImportProfile> getImportProfilesByCompanyId( @VelocityCheck int companyId) {
		return select(logger, SELECT_BY_COMPANYID, new ImportProfileRowMapper(), companyId);
    }

	@Override
    public List<ImportProfile> getAllImportProfilesByCompanyId( @VelocityCheck int companyId) {
		return select(logger, "SELECT * FROM " + TABLE + " WHERE " + FIELD_COMPANY_ID + " = ?", new ImportProfileRowMapper(), companyId);
    }

	@Override
	@DaoUpdateReturnValueCheck
    public boolean deleteImportProfileById(int profileId) {
    	try {
    		update(logger, "DELETE FROM import_profile_mlist_bind_tbl WHERE import_profile_id = ?", profileId);
    		update(logger, DELETE, profileId);
    		update(logger, DELETE_COLUMN_MAPPINGS, profileId);
    		update(logger, DELETE_GENDER_MAPPINGS, profileId);
    	} catch (Exception e) {
    		return false;
    	}
    	return true;
    }

	@Override
	public void insertColumnMappings(List<ColumnMapping> columnMappings) {
		if (columnMappings != null && !columnMappings.isEmpty()){
			String insertStatementString;
			if (isOracleDB()) {
				insertStatementString = INSERT_COLUMN_MAPPINGS_ORACLE;
			} else {
				insertStatementString = INSERT_COLUMN_MAPPINGS_MYSQL;
			}

			List<Object[]> parameterList = new ArrayList<>();
			for (ColumnMapping mapping : columnMappings) {
				parameterList.add(new Object[] {
						mapping.getProfileId(),
						mapping.getFileColumn(),
						mapping.getDatabaseColumn(),
						mapping.isMandatory(),
						mapping.isEncrypted(),
						mapping.getDefaultValue() });
			}
			batchupdate(logger, insertStatementString, parameterList);
		}
	}

	@Override
    public void updateColumnMappings(List<ColumnMapping> mappings) {
		if (!CollectionUtils.isEmpty(mappings)) {
			String updateStatement;
			if (isOracleDB()) {
				updateStatement = UPDATE_COLUMN_MAPPINGS;
			} else {
				updateStatement = UPDATE_COLUMN_MAPPINGS;
			}

			List<Object[]> parameterListForUpdate = new ArrayList<>();
			for (ColumnMapping mapping : mappings) {
				parameterListForUpdate.add(new Object[] {
						mapping.getProfileId(),
						mapping.getFileColumn(),
						mapping.getDatabaseColumn(),
						mapping.isMandatory(),
						mapping.isEncrypted(),
						mapping.getDefaultValue(),
						mapping.getId()});
			}
			batchupdate(logger, updateStatement, parameterListForUpdate);
		}
	}

	@Override
	public void deleteColumnMappings(List<Integer> ids) {
		if (!CollectionUtils.isEmpty(ids)) {
			String sql = "DELETE FROM import_column_mapping_tbl WHERE id = ?";
			batchupdate(logger, sql, ids.stream()
					.map(id -> new Object[] {id})
					.collect(Collectors.toList()));
		}
	}

	@Override
	public List<Integer> getSelectedMailingListIds(int id, @VelocityCheck int companyId) {
		String sqlGetMailingListIds = "SELECT mailinglist_id FROM import_profile_mlist_bind_tbl WHERE import_profile_id = ? AND company_id = ?";
		return select(logger, sqlGetMailingListIds, new IntegerRowMapper(), id, companyId);
	}

	@DaoUpdateReturnValueCheck
    private void insertGenderMappings(Map<String, Integer> mappings, int importProfileId) {
    	if (mappings != null && !mappings.isEmpty()){
        	String insertStatementString;
	        if (isOracleDB()) {
	        	insertStatementString = INSERT_GENDER_MAPPINGS_ORACLE;
	        } else {
	        	insertStatementString = INSERT_GENDER_MAPPINGS_MYSQL;
	        }
	        
			List<Object[]> parameterList = new ArrayList<>();
            for (Entry<String, Integer> entry : mappings.entrySet()) {
				parameterList.add(new Object[] { importProfileId, entry.getValue(), entry.getKey() });           
            }
            batchupdate(logger, insertStatementString, parameterList);
        }
    }
    
    private class ImportProfileRowMapper implements RowMapper<ImportProfile> {
        @Override
        public ImportProfile mapRow(ResultSet resultSet, int row) throws SQLException {
        	try {
	            ImportProfile profile = new ImportProfileImpl();
	            profile.setId(resultSet.getInt(FIELD_ID));
	            profile.setName(resultSet.getString(FIELD_SHORTNAME));
	            profile.setCompanyId(resultSet.getInt(FIELD_COMPANY_ID));
	            profile.setAdminId(resultSet.getInt(FIELD_ADMIN_ID));
	            profile.setSeparator(resultSet.getInt(FIELD_COLUMN_SEPARATOR));
	            profile.setTextRecognitionChar(resultSet.getInt(FIELD_TEXT_DELIMITER));
	            profile.setCharset(resultSet.getInt(FIELD_FILE_CHARSET));
	            profile.setDateFormat(resultSet.getInt(FIELD_DATE_FORMAT));
	            profile.setImportMode(resultSet.getInt(FIELD_IMPORT_MODE));
	            profile.setKeyColumns(AgnUtils.splitAndTrimList(resultSet.getString(FIELD_KEY_COLUMN)));
	            profile.setCheckForDuplicates(resultSet.getInt(FIELD_CHECK_FOR_DUPLICATES));
	            profile.setNullValuesAction(resultSet.getInt(FIELD_NULL_VALUES_ACTION));
	            profile.setMailForReport(resultSet.getString(FIELD_REPORT_EMAIL));
	            profile.setMailForError(resultSet.getString(FIELD_ERROR_EMAIL));
	            profile.setDefaultMailType(resultSet.getInt(FIELD_MAIL_TYPE));
	            profile.setUpdateAllDuplicates(resultSet.getBoolean(FIELD_UPDATE_ALL_DUPLICATES));
	            
	            if (resultSet.getObject(FIELD_PRE_IMPORT_ACTION) == null) {
	            	profile.setImportProcessActionID(0);
	            } else {
	            	profile.setImportProcessActionID(resultSet.getInt(FIELD_PRE_IMPORT_ACTION));
	            }
	            
	            if (resultSet.getObject(FIELD_NEW_RECIPIENTS_ACTION) == null) {
	            	profile.setActionForNewRecipients(0);
	            } else {
	            	profile.setActionForNewRecipients(resultSet.getInt(FIELD_NEW_RECIPIENTS_ACTION));
	            }
	            
	            // Read additional data
	            
	            // Read ColumnMappings
	            profile.setColumnMapping(select(logger, SELECT_COLUMN_MAPPINGS, new ColumnMappingRowMapper(), profile.getId()));
	            
	            // Read GenderMappings
	            List<Map<String, Object>> queryResult = select(logger, SELECT_GENDER_MAPPINGS, profile.getId());
	            Map<String, Integer> genderMappings = new HashMap<>();
	            for (Map<String, Object> resultSetRow : queryResult) {
	            	genderMappings.put((String) resultSetRow.get(GENDER_MAPPING_STRING_GENDER), ((Number)resultSetRow.get(GENDER_MAPPING_INT_GENDER)).intValue());
	            }
	            profile.setGenderMapping(genderMappings);
	            
	            String decimalSeparator = resultSet.getString(FIELD_DECIMAL_SEPARATOR);
	            if (StringUtils.isNotEmpty(decimalSeparator)) {
	            	profile.setDecimalSeparator(decimalSeparator.charAt(0));
	        	}
	            
	            profile.setNoHeaders(resultSet.getBoolean(FIELD_NOHEADERS));
	            
	            profile.setZipped(resultSet.getBoolean(FIELD_ZIP));
	            
	            profile.setAutoMapping(resultSet.getBoolean("automapping"));
	            
	            String zipPasswordEncrypted = resultSet.getString(FIELD_ZIP_PASSWORD_ENCRYPTED);
	            if (StringUtils.isNotEmpty(zipPasswordEncrypted)) {
	            	profile.setZipPassword(dataEncryptor.decrypt(zipPasswordEncrypted));
	            } else {
	            	profile.setZipPassword(null);
	            }

	        	if (profile.getActionForNewRecipients() != 0) {
		            List<Integer> mailinglistIDs = new ArrayList<>();
	        		List<AbstractActionOperationParameters> operations = emmActionOperationDao.getOperations(profile.getActionForNewRecipients(), profile.getCompanyId());
	        		for (AbstractActionOperationParameters operation : operations) {
	        			if (operation instanceof ActionOperationSendMailingParameters) {
	        				int mailingID = ((ActionOperationSendMailingParameters) operation).getMailingID();
	        				mailinglistIDs.add(mailingDao.getMailinglistId(mailingID, profile.getCompanyId()));
	        			}
	    			}
	        		profile.setMailinglists(mailinglistIDs);
	        	} else {
					profile.setMailinglists(getSelectedMailingListIds(profile.getId(), profile.getCompanyId()));
	        	}
	            
	            return profile;
			} catch (Exception e) {
				throw new SQLException("Error in ImportProfile data: " + e.getMessage(), e);
			}
    	}
    }
    
    private class ColumnMappingRowMapper implements RowMapper<ColumnMapping> {
    	@Override
        public ColumnMapping mapRow(ResultSet resultSet, int row) throws SQLException {
            ColumnMapping mapping = new ColumnMappingImpl();
            mapping.setId(resultSet.getInt(COLUMN_MAPPING_ID));
            mapping.setProfileId(resultSet.getInt(COLUMN_MAPPING_PROFILE_ID));
            mapping.setMandatory(resultSet.getBoolean(COLUMN_MAPPING_MANDATORY));
            mapping.setEncrypted(resultSet.getBoolean(COLUMN_MAPPING_ENCRYPTED));
            mapping.setDatabaseColumn(resultSet.getString(COLUMN_MAPPING_DB_COLUMN));
            mapping.setFileColumn(resultSet.getString(COLUMN_MAPPING_FILE_COLUMN));
            String defaultValue = resultSet.getString(COLUMN_MAPPING_DEFAULT_VALUE);
            if (StringUtils.isNotEmpty(defaultValue)) {
                mapping.setDefaultValue(defaultValue);
            }
            return mapping;
    	}
    }
}
