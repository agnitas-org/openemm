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
import com.agnitas.emm.core.mediatypes.common.MediaTypes;

/**
 * DAO handler for ImportProfile-Objects
 * This class is compatible with oracle and mysql datasources and databases
 */
public class ImportProfileDaoImpl extends BaseDaoImpl implements ImportProfileDao {
	private static final transient Logger logger = Logger.getLogger(ImportProfileDaoImpl.class);

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
			profileId = selectInt(logger, "SELECT import_profile_tbl_seq.nextval FROM DUAL");
	        
			update(logger,
				"INSERT INTO import_profile_tbl (id, company_id, admin_id, shortname, column_separator, text_delimiter"
					+ ", file_charset, date_format, import_mode, null_values_action, key_column, report_email, error_email, check_for_duplicates"
					+ ", mail_type, update_all_duplicates, pre_import_action, decimal_separator, action_for_new_recipients, noheaders, zip, zip_password_encr, automapping, mediatype, datatype)"
					+ " VALUES (" + AgnUtils.repeatString("?", 25, ", ") + ")",
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
				importProfile.isAutoMapping() ? 1 : 0,
				importProfile.getMediatype() == null ? MediaTypes.EMAIL.getMediaCode() : importProfile.getMediatype().getMediaCode(),
				importProfile.getDatatype()
			);
		} else {
			profileId = insertIntoAutoincrementMysqlTable(logger, "id",
				"INSERT INTO import_profile_tbl (company_id, admin_id, shortname, column_separator, text_delimiter"
					+ ", file_charset, date_format, import_mode, null_values_action, key_column, report_email, error_email, check_for_duplicates"
					+ ", mail_type, update_all_duplicates, pre_import_action, decimal_separator, action_for_new_recipients, noheaders, zip, zip_password_encr, automapping, mediatype, datatype)"
					+ " VALUES (" + AgnUtils.repeatString("?", 24, ", ") + ")",
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
				importProfile.getMediatype() == null ? MediaTypes.EMAIL.getMediaCode() : importProfile.getMediatype().getMediaCode(),
				importProfile.getDatatype()
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
			"UPDATE import_profile_tbl SET company_id = ?, admin_id = ?, shortname = ?, column_separator = ?, text_delimiter = ?, file_charset = ?, date_format = ?, import_mode = ?, null_values_action = ?, key_column = ?, report_email = ?, error_email = ?, check_for_duplicates = ?, mail_type = ?, update_all_duplicates = ?, pre_import_action = ?, decimal_separator = ?, action_for_new_recipients = ?, noheaders = ?, zip = ?, zip_password_encr = ?, automapping = ?, mediatype = ?, datatype = ? WHERE id = ?",
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
			importProfile.getMediatype() == null ? MediaTypes.EMAIL.getMediaCode() : importProfile.getMediatype().getMediaCode(),
			importProfile.getDatatype(),
			importProfile.getId()
		);

		update(logger, "DELETE FROM import_gender_mapping_tbl WHERE profile_id = ?", importProfile.getId());
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
			return selectObjectDefaultNull(logger, "SELECT * FROM import_profile_tbl WHERE id = ? AND deleted != 1", new ImportProfileRowMapper(), id);
		} catch (DataAccessException e) {
			// No ImportProfile found
			return null;
		}
    }

	@Override
    public ImportProfile getImportProfileByShortname(String shortname) {
		return selectObjectDefaultNull(logger, "SELECT * FROM import_profile_tbl WHERE UPPER(shortname) = UPPER(?) AND deleted != 1", new ImportProfileRowMapper(), shortname);
    }

	@Override
    public List<ImportProfile> getImportProfilesByCompanyId( @VelocityCheck int companyId) {
		return select(logger, "SELECT * FROM import_profile_tbl WHERE company_id = ? AND deleted != 1 ORDER BY LOWER(shortname) ASC", new ImportProfileRowMapper(), companyId);
    }

	@Override
    public List<ImportProfile> getAllImportProfilesByCompanyId( @VelocityCheck int companyId) {
		return select(logger, "SELECT * FROM import_profile_tbl WHERE company_id = ?", new ImportProfileRowMapper(), companyId);
    }

	@Override
	@DaoUpdateReturnValueCheck
    public boolean deleteImportProfileById(int profileId) {
    	try {
    		update(logger, "DELETE FROM import_profile_mlist_bind_tbl WHERE import_profile_id = ?", profileId);
    		update(logger, "DELETE FROM import_profile_tbl WHERE id = ?", profileId);
    		update(logger, "DELETE FROM import_column_mapping_tbl WHERE profile_id = ?", profileId);
    		update(logger, "DELETE FROM import_gender_mapping_tbl WHERE profile_id = ?", profileId);
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
				insertStatementString = "INSERT INTO import_column_mapping_tbl (id, profile_id, file_column, db_column, mandatory, encrypted, default_value) VALUES (import_column_mapping_tbl_seq.NEXTVAL, ?, ?, ?, ?, ?, ?)";
			} else {
				insertStatementString = "INSERT INTO import_column_mapping_tbl (profile_id, file_column, db_column, mandatory, encrypted, default_value) VALUES (?, ?, ?, ?, ?, ?)";
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
			String updateStatement = "UPDATE import_column_mapping_tbl SET profile_id = ?, file_column = ?, db_column = ?, mandatory = ?, encrypted = ?, default_value = ? WHERE id = ?";

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
	        	insertStatementString = "INSERT INTO import_gender_mapping_tbl (id, profile_id, int_gender, string_gender) VALUES (import_gender_mapping_tbl_seq.nextval, ?, ?, ?)";
	        } else {
	        	insertStatementString = "INSERT INTO import_gender_mapping_tbl (profile_id, int_gender, string_gender) VALUES (?, ?, ?)";
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
	            profile.setId(resultSet.getInt("id"));
	            profile.setName(resultSet.getString("shortname"));
	            profile.setCompanyId(resultSet.getInt("company_id"));
	            profile.setAdminId(resultSet.getInt("admin_id"));
	            profile.setSeparator(resultSet.getInt("column_separator"));
	            profile.setTextRecognitionChar(resultSet.getInt("text_delimiter"));
	            profile.setCharset(resultSet.getInt("file_charset"));
	            profile.setDateFormat(resultSet.getInt("date_format"));
	            profile.setImportMode(resultSet.getInt("import_mode"));
	            profile.setKeyColumns(AgnUtils.splitAndTrimList(resultSet.getString("key_column")));
	            profile.setCheckForDuplicates(resultSet.getInt("check_for_duplicates"));
	            profile.setNullValuesAction(resultSet.getInt("null_values_action"));
	            profile.setMailForReport(resultSet.getString("report_email"));
	            profile.setMailForError(resultSet.getString("error_email"));
	            profile.setDefaultMailType(resultSet.getInt("mail_type"));
	            profile.setUpdateAllDuplicates(resultSet.getBoolean("update_all_duplicates"));
	            
	            if (resultSet.getObject("pre_import_action") == null) {
	            	profile.setImportProcessActionID(0);
	            } else {
	            	profile.setImportProcessActionID(resultSet.getInt("pre_import_action"));
	            }
	            
	            if (resultSet.getObject("action_for_new_recipients") == null) {
	            	profile.setActionForNewRecipients(0);
	            } else {
	            	profile.setActionForNewRecipients(resultSet.getInt("action_for_new_recipients"));
	            }
	            
	            // Read additional data
	            
	            // Read ColumnMappings
	            profile.setColumnMapping(select(logger, "SELECT * FROM import_column_mapping_tbl WHERE profile_id = ? AND deleted != 1", new ColumnMappingRowMapper(), profile.getId()));
	            
	            // Read GenderMappings
	            List<Map<String, Object>> queryResult = select(logger, "SELECT * FROM import_gender_mapping_tbl WHERE profile_id = ? AND deleted != 1 ORDER BY id", profile.getId());
	            Map<String, Integer> genderMappings = new HashMap<>();
	            for (Map<String, Object> resultSetRow : queryResult) {
	            	genderMappings.put((String) resultSetRow.get("string_gender"), ((Number)resultSetRow.get("int_gender")).intValue());
	            }
	            profile.setGenderMapping(genderMappings);
	            
	            String decimalSeparator = resultSet.getString("decimal_separator");
	            if (StringUtils.isNotEmpty(decimalSeparator)) {
	            	profile.setDecimalSeparator(decimalSeparator.charAt(0));
	        	}
	            
	            profile.setNoHeaders(resultSet.getBoolean("noheaders"));
	            
	            profile.setZipped(resultSet.getBoolean("zip"));
	            
	            profile.setAutoMapping(resultSet.getBoolean("automapping"));
	            
	            String zipPasswordEncrypted = resultSet.getString("zip_password_encr");
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
	        	
	            profile.setMediatype(MediaTypes.getMediaTypeForCode(resultSet.getInt("mediatype")));
	            
	            profile.setDatatype(resultSet.getString("datatype"));
	            
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
            mapping.setId(resultSet.getInt("id"));
            mapping.setProfileId(resultSet.getInt("profile_id"));
            mapping.setMandatory(resultSet.getBoolean("mandatory"));
            mapping.setEncrypted(resultSet.getBoolean("encrypted"));
            mapping.setDatabaseColumn(resultSet.getString("db_column"));
            mapping.setFileColumn(resultSet.getString("file_column"));
            String defaultValue = resultSet.getString("default_value");
            if (StringUtils.isNotEmpty(defaultValue)) {
                mapping.setDefaultValue(defaultValue);
            }
            return mapping;
    	}
    }
}
