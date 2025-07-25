/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.import_profile.dao.impl;

import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.dao.MailingDao;
import com.agnitas.emm.core.JavaMailService;
import com.agnitas.emm.core.action.operations.AbstractActionOperationParameters;
import com.agnitas.emm.core.action.operations.ActionOperationSendMailingParameters;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.beans.ColumnMapping;
import com.agnitas.beans.ImportProfile;
import com.agnitas.beans.impl.ColumnMappingImpl;
import com.agnitas.beans.impl.ImportProfileImpl;
import com.agnitas.emm.core.action.dao.EmmActionOperationDao;
import com.agnitas.emm.core.import_profile.dao.ImportProfileDao;
import com.agnitas.dao.impl.BaseDaoImpl;
import com.agnitas.dao.impl.mapper.IntegerRowMapper;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import com.agnitas.util.AgnUtils;
import com.agnitas.util.DataEncryptor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.util.CollectionUtils;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * DAO handler for ImportProfile-Objects
 * This class is compatible with oracle and mysql datasources and databases
 */
public class ImportProfileDaoImpl extends BaseDaoImpl implements ImportProfileDao {

	private final DataEncryptor dataEncryptor;
	private final EmmActionOperationDao emmActionOperationDao;
    private final MailingDao mailingDao;
    private final ConfigService configService;

    public ImportProfileDaoImpl(DataSource dataSource, JavaMailService javaMailService, DataEncryptor dataEncryptor, EmmActionOperationDao emmActionOperationDao,
								MailingDao mailingDao, ConfigService configService) {
    	super(dataSource, javaMailService);
    	
		this.dataEncryptor = dataEncryptor;
		this.emmActionOperationDao = emmActionOperationDao;
		this.mailingDao = mailingDao;
		this.configService = configService;
	}

	@Override
	@DaoUpdateReturnValueCheck
	public int insertImportProfile(ImportProfile importProfile) {
		int profileId;
		if (isOracleDB()) {
			profileId = selectInt("SELECT import_profile_tbl_seq.nextval FROM DUAL");
	        
			update(
				"INSERT INTO import_profile_tbl (id, company_id, admin_id, shortname, column_separator, text_delimiter"
					+ ", file_charset, date_format, import_mode, null_values_action, key_column, report_email, error_email, check_for_duplicates"
					+ ", mail_type, update_all_duplicates, pre_import_action, decimal_separator, action_for_new_recipients, noheaders, zip_password_encr, automapping, datatype, mailinglists_all"
					+ ", report_locale_lang, report_locale_country, report_timezone, creation_date, change_date)"
					+ " VALUES (" + AgnUtils.repeatString("?", 27, ", ") + ", CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
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
				StringUtils.isEmpty(importProfile.getZipPassword()) ? null : dataEncryptor.encrypt(importProfile.getZipPassword()),
				importProfile.isAutoMapping() ? 1 : 0,
				importProfile.getDatatype(),
				importProfile.isMailinglistsAll() ? 1 : 0,
				importProfile.getReportLocale().getLanguage(),
				importProfile.getReportLocale().getCountry(),
				importProfile.getReportTimezone()
			);
		} else {
			profileId = insertIntoAutoincrementMysqlTable("id",
				"INSERT INTO import_profile_tbl (company_id, admin_id, shortname, column_separator, text_delimiter"
					+ ", file_charset, date_format, import_mode, null_values_action, key_column, report_email, error_email, check_for_duplicates"
					+ ", mail_type, update_all_duplicates, pre_import_action, decimal_separator, action_for_new_recipients, noheaders, zip_password_encr, automapping, datatype, mailinglists_all"
					+ ", report_locale_lang, report_locale_country, report_timezone, creation_date, change_date)"
					+ " VALUES (" + AgnUtils.repeatString("?", 26, ", ") + ", CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
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
				StringUtils.isEmpty(importProfile.getZipPassword()) ? null : dataEncryptor.encrypt(importProfile.getZipPassword()),
				importProfile.isAutoMapping() ? 1 : 0,
				importProfile.getDatatype(),
				importProfile.isMailinglistsAll() ? 1 : 0,
				importProfile.getReportLocale().getLanguage(),
				importProfile.getReportLocale().getCountry(),
				importProfile.getReportTimezone()
			);
		}

		importProfile.setId(profileId);
		insertGenderMappings(importProfile.getGenderMapping(), importProfile.getId());
		updateMailinglists(importProfile);
		updateMediatypes(importProfile.getId(), importProfile.getMediatypes());

		return importProfile.getId();
	}

	@Override
	@DaoUpdateReturnValueCheck
	public void updateImportProfile(ImportProfile importProfile) {
		update(
			"UPDATE import_profile_tbl SET company_id = ?, admin_id = ?, shortname = ?, column_separator = ?, text_delimiter = ?, file_charset = ?, date_format = ?, import_mode = ?"
					+ ", null_values_action = ?, key_column = ?, report_email = ?, error_email = ?, check_for_duplicates = ?, mail_type = ?, update_all_duplicates = ?, pre_import_action = ?"
					+ ", decimal_separator = ?, action_for_new_recipients = ?, noheaders = ?, zip_password_encr = ?, automapping = ?, datatype = ?, mailinglists_all = ?"
					+ ", report_locale_lang = ?, report_locale_country = ?, report_timezone = ?, change_date = CURRENT_TIMESTAMP WHERE id = ?",
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
			StringUtils.isEmpty(importProfile.getZipPassword()) ? null : dataEncryptor.encrypt(importProfile.getZipPassword()),
			importProfile.isAutoMapping() ? 1 : 0,
			importProfile.getDatatype(),
			importProfile.isMailinglistsAll() ? 1 : 0,
			importProfile.getReportLocale().getLanguage(),
			importProfile.getReportLocale().getCountry(),
			importProfile.getReportTimezone(),
			importProfile.getId()
		);

		update("DELETE FROM import_gender_mapping_tbl WHERE profile_id = ?", importProfile.getId());
		insertGenderMappings(importProfile.getGenderMapping(), importProfile.getId());
		updateMailinglists(importProfile);
		updateMediatypes(importProfile.getId(), importProfile.getMediatypes());
	}

	private void updateMailinglists(ImportProfile profile) {
		// Keep old mailing list bindings untouched if EMM action is selected and company has no force sending active.
		if (profile.getActionForNewRecipients() == 0
			|| configService.getBooleanValue(ConfigValue.ForceSending, profile.getCompanyId())) {
			updateMailinglists(profile.getMailinglistIds(), profile.getId(), profile.getCompanyId());
		}
	}

	private void updateMailinglists(List<Integer> mailinglists, int profileId, int companyId) {
		String cleanMailinglistsSql = "DELETE FROM import_profile_mlist_bind_tbl WHERE company_id = ? AND import_profile_id = ?";
		update(cleanMailinglistsSql, companyId, profileId);

		String insertMailinglists = "INSERT INTO import_profile_mlist_bind_tbl(import_profile_id, mailinglist_id, company_id) VALUES (?, ?, ?)";
		List<Object[]> params = new ArrayList<>();
		for(int mailinglistId : mailinglists) {
			params.add(new Object[]{profileId, mailinglistId, companyId});
		}

		batchupdate(insertMailinglists, params);
	}

	private void updateMediatypes(int profileId, Set<MediaTypes> mediatypes) {
		String cleanMediatypesSql = "DELETE FROM import_profile_mediatype_tbl WHERE import_profile_id = ?";
		update(cleanMediatypesSql, profileId);

		String insertMediatypes = "INSERT INTO import_profile_mediatype_tbl(import_profile_id, mediatype) VALUES (?, ?)";
		List<Object[]> params = new ArrayList<>();
		for(MediaTypes mediatype : mediatypes) {
			params.add(new Object[]{profileId, mediatype.getMediaCode()});
		}

		batchupdate(insertMediatypes, params);
	}

	@Override
    public ImportProfile getImportProfileById(int id) {
		try {
			return selectObjectDefaultNull("SELECT * FROM import_profile_tbl WHERE id = ? AND deleted != 1", new ImportProfileRowMapper(), id);
		} catch (DataAccessException e) {
			return null; // No ImportProfile found
		}
    }

	@Override
    public ImportProfile getImportProfileByShortname(String shortname) {
		return selectObjectDefaultNull("SELECT * FROM import_profile_tbl WHERE UPPER(shortname) = UPPER(?) AND deleted != 1", new ImportProfileRowMapper(), shortname);
    }

	@Override
    public List<ImportProfile> getImportProfilesByCompanyId( int companyId) {
		return select("SELECT * FROM import_profile_tbl WHERE company_id = ? AND deleted != 1 ORDER BY LOWER(shortname) ASC", new ImportProfileRowMapper(), companyId);
    }

	@Override
    public List<ImportProfile> getAllImportProfilesByCompanyId( int companyId) {
		return select("SELECT * FROM import_profile_tbl WHERE company_id = ?", new ImportProfileRowMapper(), companyId);
    }

	@Override
	public List<ImportProfile> findAllByEmailPart(String email, int companyID) {
		String query = "SELECT *  FROM import_profile_tbl WHERE company_id = ? AND ("
				+ getPartialSearchFilter("report_email") + " OR " + getPartialSearchFilter("error_email") + ")";
		return select(query, new ImportProfileRowMapper(), companyID, email, email);
	}

	@Override
	public List<ImportProfile> findAllByEmailPart(String email) {
		String query = "SELECT * FROM import_profile_tbl WHERE " + getPartialSearchFilter("report_email")
				+ " OR " + getPartialSearchFilter("error_email");
		return select(query, new ImportProfileRowMapper(), email, email);
	}

	@Override
	@DaoUpdateReturnValueCheck
    public boolean deleteImportProfileById(int profileId) {
    	try {
    		update("DELETE FROM import_profile_mediatype_tbl WHERE import_profile_id = ?", profileId);
    		update("DELETE FROM import_profile_mlist_bind_tbl WHERE import_profile_id = ?", profileId);
    		update("DELETE FROM import_profile_tbl WHERE id = ?", profileId);
    		update("DELETE FROM import_column_mapping_tbl WHERE profile_id = ?", profileId);
    		update("DELETE FROM import_gender_mapping_tbl WHERE profile_id = ?", profileId);
    	} catch (@SuppressWarnings("unused") Exception e) {
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
			batchupdate(insertStatementString, parameterList);
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
			batchupdate(updateStatement, parameterListForUpdate);
		}
	}

	@Override
	public void deleteColumnMappings(List<Integer> ids) {
		if (!CollectionUtils.isEmpty(ids)) {
			String sql = "DELETE FROM import_column_mapping_tbl WHERE id = ?";
			batchupdate(sql, ids.stream()
					.map(id -> new Object[] {id})
					.collect(Collectors.toList()));
		}
	}

	@Override
	public List<Integer> getSelectedMailingListIds(int id, int companyId) {
		String sqlGetMailingListIds = "SELECT mailinglist_id FROM import_profile_mlist_bind_tbl WHERE import_profile_id = ? AND company_id = ?";
		return select(sqlGetMailingListIds, IntegerRowMapper.INSTANCE, id, companyId);
	}

	private Set<MediaTypes> getMediatypes(int profileId) {
		Set<MediaTypes> returnSet = new HashSet<>();
		for (int mediatypeCode : select("SELECT mediatype FROM import_profile_mediatype_tbl WHERE import_profile_id = ?", IntegerRowMapper.INSTANCE, profileId)) {
			returnSet.add(MediaTypes.getMediaTypeForCode(mediatypeCode));
		}
		if (returnSet.isEmpty()) {
			returnSet.add(MediaTypes.EMAIL);
		}
		return returnSet;
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
            batchupdate(insertStatementString, parameterList);
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
	            profile.setColumnMapping(select("SELECT * FROM import_column_mapping_tbl WHERE profile_id = ? AND deleted != 1", new ColumnMappingRowMapper(), profile.getId()));
	            
	            // Read GenderMappings
	            List<Map<String, Object>> queryResult = select("SELECT * FROM import_gender_mapping_tbl WHERE profile_id = ? AND deleted != 1 ORDER BY id", profile.getId());
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
	            
	            profile.setAutoMapping(resultSet.getBoolean("automapping"));
	            
	            String zipPasswordEncrypted = resultSet.getString("zip_password_encr");
	            if (StringUtils.isNotEmpty(zipPasswordEncrypted)) {
	            	profile.setZipPassword(dataEncryptor.decrypt(zipPasswordEncrypted));
	            } else {
	            	profile.setZipPassword(null);
	            }

	        	if (profile.getActionForNewRecipients() != 0 && !isClientForceSendingActive(profile.getCompanyId())) {
		            List<Integer> mailinglistIDs = new ArrayList<>();
	        		List<AbstractActionOperationParameters> operations = emmActionOperationDao.getOperations(profile.getActionForNewRecipients(), profile.getCompanyId());
	        		for (AbstractActionOperationParameters operation : operations) {
	        			if (operation instanceof ActionOperationSendMailingParameters sendMailingParamsOperation) {
	        				int mailingID = sendMailingParamsOperation.getMailingID();
	        				mailinglistIDs.add(mailingDao.getMailinglistId(mailingID, profile.getCompanyId()));
	        			}
	    			}
	        		profile.setMailinglists(mailinglistIDs);
	        	} else {
					profile.setMailinglists(getSelectedMailingListIds(profile.getId(), profile.getCompanyId()));
	        	}
	        	
	            profile.setMediatypes(getMediatypes(profile.getId()));
	            
	            profile.setDatatype(resultSet.getString("datatype"));
	            
	            profile.setMailinglistsAll(resultSet.getInt("mailinglists_all") > 0);
	            
	            if (StringUtils.isNotBlank(resultSet.getString("report_locale_lang"))) {
	            	profile.setReportLocale(new Locale(resultSet.getString("report_locale_lang"), resultSet.getString("report_locale_country")));
	            }
	            profile.setReportTimezone(resultSet.getString("report_timezone"));
	            
	            return profile;
			} catch (Exception e) {
				throw new SQLException("Error in ImportProfile data: " + e.getMessage(), e);
			}
    	}
    }

	private boolean isClientForceSendingActive(int companyId) {
		return configService.getBooleanValue(ConfigValue.ForceSending, companyId);
	}
    
    private static class ColumnMappingRowMapper implements RowMapper<ColumnMapping> {
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

	@Override
	public int findImportProfileIdByName(String name, int companyId) {
		return selectIntWithDefaultValue("SELECT id FROM import_profile_tbl WHERE shortname = ? AND company_id = ?", -1, name, companyId);
	}

	@Override
	public boolean isColumnWasImported(String columnName, int id) {
		String query = "SELECT 1 FROM import_column_mapping_tbl WHERE db_column = ? AND profile_id = ? AND deleted != 1";
		return selectIntWithDefaultValue(query, 0, columnName, id) > 0;
	}
	
	@Override
	public List<Integer> getImportsContainingProfileField(int companyID, String profileFieldName) {
		return select("SELECT DISTINCT(profile_id) FROM import_column_mapping_tbl WHERE profile_id IN (SELECT id FROM import_profile_tbl WHERE company_id = ?) AND LOWER(db_column) = ? ORDER BY profile_id", IntegerRowMapper.INSTANCE, companyID, profileFieldName.toLowerCase());
	}

	@Override
	public void updateEmails(String emailForError, String emailForReport, int id) {
		String query = "UPDATE import_profile_tbl SET error_email = ?, report_email = ? WHERE id = ?";
		update(query, emailForError, emailForReport, id);
	}
}
