/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.export.dao.impl;

import com.agnitas.beans.Admin;
import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.emm.core.JavaMailService;
import com.agnitas.beans.ExportColumnMapping;
import com.agnitas.beans.ExportPredef;
import com.agnitas.emm.core.export.dao.ExportPredefDao;
import com.agnitas.dao.impl.BaseDaoImpl;
import com.agnitas.dao.impl.mapper.IntegerRowMapper;
import com.agnitas.util.AgnUtils;
import com.agnitas.util.DbColumnType.SimpleDataType;
import com.agnitas.util.DbUtilities;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component("ExportPredefDao")
public class ExportPredefDaoImpl extends BaseDaoImpl implements ExportPredefDao {

    private static final List<String> COLUMNS = List.of("export_predef_id", "company_id", "charset", "columns",
            "shortname", "description", "mailinglists", "mailinglist_id", "target_id", "user_type", "user_status",
            "deleted", "timestamp_start", "timestamp_end", "timestamp_lastdays", "timestamp_includecurrent",
            "creation_date_start", "creation_date_end", "creation_date_lastdays", "creation_date_includecurrent",
            "mailinglist_bind_start", "mailinglist_bind_end", "mailinglist_bind_lastdays", "ml_bind_includecurrent",
            "always_quote", "delimiter_char", "separator_char", "dateformat", "datetimeformat", "timezone",
            "locale_lang", "locale_country", "decimalseparator", "use_decoded_values", "limits_linked_by_and");
    protected static final String COLUMNS_CSV = StringUtils.join(COLUMNS, ", ");

    public ExportPredefDaoImpl(@Qualifier("dataSource") DataSource dataSource, JavaMailService javaMailService) {
        super(dataSource, javaMailService);
    }
    
	@Override
	public ExportPredef get(int id, int companyID) {
		if (companyID <= 0 || id <= 0) {
            return null;
        }
        return selectObjectDefaultNull(
                "SELECT " + COLUMNS_CSV + " FROM export_predef_tbl WHERE export_predef_id = ? AND company_id = ?",
                getExportRowMapper(), id, companyID);
	}

	@Override
	@DaoUpdateReturnValueCheck
	public int save(ExportPredef exportPredef) {
		if (exportPredef == null || exportPredef.getCompanyID() == 0) {
			return 0;
		} else {
			boolean exists = false;

			if (exportPredef.getId() != 0) {
				exists = selectInt("SELECT COUNT(*) FROM export_predef_tbl WHERE export_predef_id = ? AND company_id = ?", exportPredef.getId(), exportPredef.getCompanyID()) > 0;
			}

			String columnsListString = "";
			if (exportPredef.getExportColumnMappings() != null) {
				for (ExportColumnMapping exportPredefMapping : exportPredef.getExportColumnMappings()) {
					if (columnsListString.length() > 0) {
						columnsListString += ";";
					}
					columnsListString += exportPredefMapping.getDbColumn();
				}
			}
			
			Object delimiterParameter = exportPredef.getDelimiter();
			Object separatorParameter = exportPredef.getSeparator();
			if (isOracleDB()) {
				if (DbUtilities.getColumnDataType(getDataSource(), "export_predef_tbl", "delimiter_char").getSimpleDataType() != SimpleDataType.Characters) {
					delimiterParameter = getDelimiterValueForOracle(exportPredef);
				}
				if (DbUtilities.getColumnDataType(getDataSource(), "export_predef_tbl", "separator_char").getSimpleDataType() != SimpleDataType.Characters) {
					separatorParameter = getSeparatorValueForOracle(exportPredef);
				}
			}

			if (exists) {
				update("UPDATE export_predef_tbl SET charset = ?, columns = ?, shortname = ?, description = ?, mailinglists = ?, mailinglist_id = ?, delimiter_char = ?, always_quote = ?, separator_char = ?, target_id = ?, user_type = ?, user_status = ?, deleted = ?, timestamp_start = ?, timestamp_end = ?, timestamp_lastdays = ?, timestamp_includecurrent = ?, creation_date_start = ?, creation_date_end = ?, creation_date_lastdays = ?, creation_date_includecurrent = ?, mailinglist_bind_start = ?, mailinglist_bind_end = ?, mailinglist_bind_lastdays = ?, ml_bind_includecurrent = ?, dateformat = ?, datetimeformat = ?, timezone = ?, locale_lang = ?, locale_country = ?, decimalseparator = ?, use_decoded_values = ?, limits_linked_by_and = ? WHERE export_predef_id = ? AND company_id = ?",
					exportPredef.getCharset(),
					columnsListString,
					exportPredef.getShortname(),
					exportPredef.getDescription(),
					exportPredef.getMailinglists(),
					exportPredef.getMailinglistID(),
					delimiterParameter,
					exportPredef.isAlwaysQuote() ? 1 : 0,
					separatorParameter,
					exportPredef.getTargetID(),
					exportPredef.getUserType(),
					exportPredef.getUserStatus(),
					exportPredef.getDeleted(),
					exportPredef.getTimestampStart(),
					exportPredef.getTimestampEnd(),
					exportPredef.getTimestampLastDays(),
					exportPredef.isTimestampIncludeCurrentDay() ? 1 : 0,
					exportPredef.getCreationDateStart(),
					exportPredef.getCreationDateEnd(),
					exportPredef.getCreationDateLastDays(),
					exportPredef.isCreationDateIncludeCurrentDay() ? 1 : 0,
					exportPredef.getMailinglistBindStart(),
					exportPredef.getMailinglistBindEnd(),
					exportPredef.getMailinglistBindLastDays(),
					exportPredef.isMailinglistBindIncludeCurrentDay() ? 1 : 0,
					exportPredef.getDateFormat(),
					exportPredef.getDateTimeFormat(),
					exportPredef.getTimezone(),
					exportPredef.getLocale().getLanguage(),
					exportPredef.getLocale().getCountry(),
					exportPredef.getDecimalSeparator(),
					exportPredef.isUseDecodedValues() ? 1 : 0,
					exportPredef.isTimeLimitsLinkedByAnd() ? 1 : 0,
					exportPredef.getId(),
					exportPredef.getCompanyID());
			} else {
				if (isOracleDB()) {
					int newExportPredefID = selectInt("SELECT export_predef_tbl_seq.NEXTVAL FROM DUAL");
					update("INSERT INTO export_predef_tbl (export_predef_id, company_id, charset, columns, shortname, description, mailinglists, mailinglist_id, delimiter_char, always_quote, separator_char, target_id, user_type, user_status, deleted, timestamp_start, timestamp_end, timestamp_lastdays, timestamp_includecurrent, creation_date_start, creation_date_end, creation_date_lastdays, creation_date_includecurrent, mailinglist_bind_start, mailinglist_bind_end, mailinglist_bind_lastdays, ml_bind_includecurrent, dateformat, datetimeformat, timezone, locale_lang, locale_country, decimalseparator, use_decoded_values, limits_linked_by_and) VALUES (" + AgnUtils.repeatString("?", 35, ", ") + ")",
						newExportPredefID,
						exportPredef.getCompanyID(),
						exportPredef.getCharset(),
						columnsListString,
						exportPredef.getShortname(),
						exportPredef.getDescription(),
						exportPredef.getMailinglists(),
						exportPredef.getMailinglistID(),
						delimiterParameter,
						exportPredef.isAlwaysQuote() ? 1 : 0,
						separatorParameter,
						exportPredef.getTargetID(),
						exportPredef.getUserType(),
						exportPredef.getUserStatus(),
						exportPredef.getDeleted(),
						exportPredef.getTimestampStart(),
						exportPredef.getTimestampEnd(),
						exportPredef.getTimestampLastDays(),
						exportPredef.isTimestampIncludeCurrentDay() ? 1 : 0,
						exportPredef.getCreationDateStart(),
						exportPredef.getCreationDateEnd(),
						exportPredef.getCreationDateLastDays(),
						exportPredef.isCreationDateIncludeCurrentDay() ? 1 : 0,
						exportPredef.getMailinglistBindStart(),
						exportPredef.getMailinglistBindEnd(),
						exportPredef.getMailinglistBindLastDays(),
						exportPredef.isMailinglistBindIncludeCurrentDay() ? 1 : 0,
						exportPredef.getDateFormat(),
						exportPredef.getDateTimeFormat(),
						exportPredef.getTimezone(),
						exportPredef.getLocale().getLanguage(),
						exportPredef.getLocale().getCountry(),
						exportPredef.getDecimalSeparator(),
						exportPredef.isUseDecodedValues() ? 1 : 0,
						exportPredef.isTimeLimitsLinkedByAnd() ? 1 : 0);
					exportPredef.setId(newExportPredefID);
				} else {
					int newExportPredefID = insertIntoAutoincrementMysqlTable("export_predef_id", "INSERT INTO export_predef_tbl (company_id, charset, columns, shortname, description, mailinglists, mailinglist_id, delimiter_char, always_quote, separator_char, target_id, user_type, user_status, deleted, timestamp_start, timestamp_end, timestamp_lastdays, timestamp_includecurrent, creation_date_start, creation_date_end, creation_date_lastdays, creation_date_includecurrent, mailinglist_bind_start, mailinglist_bind_end, mailinglist_bind_lastdays, ml_bind_includecurrent, dateformat, datetimeformat, timezone, locale_lang, locale_country, decimalseparator, use_decoded_values, limits_linked_by_and) VALUES (" + AgnUtils.repeatString("?", 34, ", ") + ")",
						exportPredef.getCompanyID(),
						exportPredef.getCharset(),
						columnsListString,
						exportPredef.getShortname(),
						exportPredef.getDescription(),
						exportPredef.getMailinglists(),
						exportPredef.getMailinglistID(),
						delimiterParameter,
						exportPredef.isAlwaysQuote() ? 1 : 0,
						separatorParameter,
						exportPredef.getTargetID(),
						exportPredef.getUserType(),
						exportPredef.getUserStatus(),
						exportPredef.getDeleted(),
						exportPredef.getTimestampStart(),
						exportPredef.getTimestampEnd(),
						exportPredef.getTimestampLastDays(),
						exportPredef.isTimestampIncludeCurrentDay() ? 1 : 0,
						exportPredef.getCreationDateStart(),
						exportPredef.getCreationDateEnd(),
						exportPredef.getCreationDateLastDays(),
						exportPredef.isCreationDateIncludeCurrentDay() ? 1 : 0,
						exportPredef.getMailinglistBindStart(),
						exportPredef.getMailinglistBindEnd(),
						exportPredef.getMailinglistBindLastDays(),
						exportPredef.isMailinglistBindIncludeCurrentDay() ? 1 : 0,
						exportPredef.getDateFormat(),
						exportPredef.getDateTimeFormat(),
						exportPredef.getTimezone(),
						exportPredef.getLocale().getLanguage(),
						exportPredef.getLocale().getCountry(),
						exportPredef.getDecimalSeparator(),
						exportPredef.isUseDecodedValues() ? 1 : 0,
						exportPredef.isTimeLimitsLinkedByAnd() ? 1 : 0);
					exportPredef.setId(newExportPredefID);
				}
			}

			update("DELETE FROM export_column_mapping_tbl WHERE export_predef_id = ?", exportPredef.getId());
			if (CollectionUtils.isNotEmpty(exportPredef.getExportColumnMappings())){
				if (isOracleDB()) {
					List<Object[]> parameterList = new ArrayList<>();
		            for (ExportColumnMapping exportColumnMapping : exportPredef.getExportColumnMappings()) {
						parameterList.add(new Object[] {
								exportPredef.getId(),
								exportColumnMapping.getDbColumn(),
								exportColumnMapping.getFileColumn(),
								exportColumnMapping.getDefaultValue(),
								exportColumnMapping.isEncrypted() ? 1 : 0
						});
		            }
		            batchupdate("INSERT INTO export_column_mapping_tbl (id, export_predef_id, db_column, file_column, default_value, encrypted) VALUES (export_column_mapping_tbl_seq.NEXTVAL, ?, ?, ?, ?, ?)", parameterList);
				} else {
					List<Object[]> parameterList = new ArrayList<>();
		            for (ExportColumnMapping exportColumnMapping : exportPredef.getExportColumnMappings()) {
						parameterList.add(new Object[] {
								exportPredef.getId(),
								exportColumnMapping.getDbColumn(),
								exportColumnMapping.getFileColumn(),
								exportColumnMapping.getDefaultValue(),
								exportColumnMapping.isEncrypted() ? 1 : 0
						});
		            }
		            batchupdate("INSERT INTO export_column_mapping_tbl (export_predef_id, db_column, file_column, default_value, encrypted) VALUES (?, ?, ?, ?, ?)", parameterList);
				}
	        }
		}

		return exportPredef.getId();
	}

	@Override
	@DaoUpdateReturnValueCheck
	public boolean delete(ExportPredef exportPredef) {
		update("DELETE FROM export_column_mapping_tbl WHERE export_predef_id = ? AND export_predef_id IN (SELECT export_predef_id FROM export_predef_tbl WHERE company_id = ?)", exportPredef.getId(), exportPredef.getCompanyID());
		int touchedLines = update("DELETE FROM export_predef_tbl WHERE export_predef_id = ? AND company_id = ?", exportPredef.getId(), exportPredef.getCompanyID());
		return touchedLines > 0;
	}

	@Override
	public boolean deleteAllByCompanyID(int companyID) {
		update("DELETE FROM export_column_mapping_tbl WHERE export_predef_id IN (SELECT export_predef_id FROM export_predef_tbl WHERE company_id = ?)", companyID);
		int touchedLines = update("DELETE FROM export_predef_tbl WHERE company_id = ?", companyID);
		return touchedLines > 0;
	}

	@Override
	public boolean delete(int id, int companyID) {
		ExportPredef exportPredef = get(id, companyID);

		if (exportPredef != null) {
			return delete(exportPredef);
		} else {
			return false;
		}
	}

	@Override
	public String findName(int id, int companyId) {
		String query = "SELECT shortname FROM export_predef_tbl WHERE export_predef_id = ? AND company_id = ?";
		return selectWithDefaultValue(query, String.class, "", id, companyId);
	}

	@Override
	public List<Integer> findTargetDependentExportProfiles(int targetGroupId, int companyId) {
		String query = "SELECT export_predef_id FROM export_predef_tbl WHERE company_id = ? AND target_id = ? AND deleted = 0";
		return select(query, IntegerRowMapper.INSTANCE, companyId, targetGroupId);
	}

	@Override
	public List<ExportPredef> getAllExports(Admin admin) {
		return getAllExports(admin.getCompanyID());
	}

	@Override
	public List<ExportPredef> getAllExports(int companyId) {
		return select(
                "SELECT " + COLUMNS_CSV + " FROM export_predef_tbl WHERE deleted = 0 AND company_id = ?",
                getExportRowMapper(), companyId);
	}

	@Override
	public List<Integer> getAllExportIds(Admin admin) {
		return select(
                "SELECT export_predef_id FROM export_predef_tbl WHERE deleted = 0 AND company_id = ?",
                IntegerRowMapper.INSTANCE, admin.getCompanyID());
	}
	
	@Override
	public List<Integer> getExportsContainingProfileField(int companyID, String profileFieldName) {
		return select("SELECT DISTINCT(export_predef_id) FROM export_column_mapping_tbl WHERE export_predef_id IN (SELECT export_predef_id FROM export_predef_tbl WHERE company_id = ?) AND LOWER(db_column) = ? ORDER BY export_predef_id", IntegerRowMapper.INSTANCE, companyID, profileFieldName.toLowerCase());
	}

    protected class ExportPredefRowMapper implements RowMapper<ExportPredef> {
		private boolean isOracle;

		public ExportPredefRowMapper(boolean isOracle) {
			this.isOracle = isOracle;
		}

		@Override
		public ExportPredef mapRow(ResultSet resultSet, int row) throws SQLException {
			ExportPredef readItem = new ExportPredef();
			
			readItem.setId(resultSet.getBigDecimal("export_predef_id").intValue());
			readItem.setCompanyID(resultSet.getBigDecimal("company_id").intValue());
			readItem.setCharset(resultSet.getString("charset"));
			
			List<ExportColumnMapping> exportColumnMappings = select("SELECT id, db_column, file_column, default_value, encrypted FROM export_column_mapping_tbl WHERE export_predef_id = ? ORDER BY id", new ExportColumnMappingRowMapper(), readItem.getId());
			if (exportColumnMappings != null && exportColumnMappings.size() > 0) {
				readItem.setExportColumnMappings(exportColumnMappings);
			} else if (resultSet.getString("columns") != null) {
				exportColumnMappings = new ArrayList<>();
				for (String dbColumnName : AgnUtils.splitAndTrimList(resultSet.getString("columns"))) {
					ExportColumnMapping exportColumnMapping = new ExportColumnMapping();
					exportColumnMapping.setDbColumn(dbColumnName.toLowerCase());
					exportColumnMappings.add(exportColumnMapping);
				}
				readItem.setExportColumnMappings(exportColumnMappings);
			}
			
			readItem.setShortname(resultSet.getString("shortname"));
			readItem.setDescription(resultSet.getString("description"));
			readItem.setMailinglists(resultSet.getString("mailinglists"));
			readItem.setMailinglistID(resultSet.getBigDecimal("mailinglist_id").intValue());
			readItem.setTargetID(resultSet.getBigDecimal("target_id").intValue());
			readItem.setUserType(resultSet.getString("user_type"));
			readItem.setUserStatus(resultSet.getBigDecimal("user_status").intValue());
			readItem.setDeleted(resultSet.getBigDecimal("deleted").intValue());
			readItem.setTimestampStart(resultSet.getTimestamp("timestamp_start"));
			readItem.setTimestampEnd(resultSet.getTimestamp("timestamp_end"));
			readItem.setTimestampLastDays(resultSet.getInt("timestamp_lastdays"));
			readItem.setTimestampIncludeCurrentDay(resultSet.getInt("timestamp_includecurrent") > 0);
			readItem.setCreationDateStart(resultSet.getTimestamp("creation_date_start"));
			readItem.setCreationDateEnd(resultSet.getTimestamp("creation_date_end"));
			readItem.setCreationDateLastDays(resultSet.getInt("creation_date_lastdays"));
			readItem.setCreationDateIncludeCurrentDay(resultSet.getInt("creation_date_includecurrent") > 0);
			readItem.setMailinglistBindStart(resultSet.getTimestamp("mailinglist_bind_start"));
			readItem.setMailinglistBindEnd(resultSet.getTimestamp("mailinglist_bind_end"));
			readItem.setMailinglistBindLastDays(resultSet.getInt("mailinglist_bind_lastdays"));
			readItem.setMailinglistBindIncludeCurrentDay(resultSet.getInt("ml_bind_includecurrent") > 0);
			readItem.setAlwaysQuote(resultSet.getInt("always_quote") > 0);
			
			if (isOracle) {
				if (DbUtilities.getColumnTypeByName(resultSet.getMetaData(), "delimiter_char") == Types.VARCHAR) {
					readItem.setDelimiter(resultSet.getString("delimiter_char"));
					readItem.setSeparator(resultSet.getString("separator_char"));
				} else {
					int delimiterInt = resultSet.getBigDecimal("delimiter_char").intValue();
					String delimiterString;
					switch (delimiterInt) {
						case 1:
							delimiterString = "'";
							break;
						default:
							delimiterString = "\"";
					}
					readItem.setDelimiter(delimiterString);
					
					int separatorInt = resultSet.getBigDecimal("separator_char").intValue();
					String separatorString;
					switch (separatorInt) {
						case 1:
							separatorString = ",";
							break;
						case 2:
							separatorString = "|";
							break;
						case 3:
							separatorString = "\t";
							break;
						default:
							separatorString = ";";
					}
					readItem.setSeparator(separatorString);
				}
			} else {
				readItem.setDelimiter(resultSet.getString("delimiter_char"));
				readItem.setSeparator(resultSet.getString("separator_char"));
			}
			
			readItem.setDateFormat(resultSet.getInt("dateformat"));
			readItem.setDateTimeFormat(resultSet.getInt("datetimeformat"));
			readItem.setTimezone(resultSet.getString("timezone"));
			if (StringUtils.isNotBlank(resultSet.getString("locale_lang"))) {
				readItem.setLocale(new Locale(resultSet.getString("locale_lang"), resultSet.getString("locale_country")));
			}
			readItem.setDecimalSeparator(resultSet.getString("decimalseparator"));
			readItem.setUseDecodedValues(resultSet.getInt("use_decoded_values") > 0);
			readItem.setTimeLimitsLinkedByAnd(resultSet.getInt("limits_linked_by_and") > 0);

			return readItem;
		}
	}

    protected ExportPredefRowMapper getExportRowMapper() {
        return new ExportPredefRowMapper(isOracleDB());
    }
    
    private class ExportColumnMappingRowMapper implements RowMapper<ExportColumnMapping> {
		@Override
		public ExportColumnMapping mapRow(ResultSet resultSet, int row) throws SQLException {
			ExportColumnMapping readItem = new ExportColumnMapping();
			
			readItem.setId(resultSet.getBigDecimal("id").intValue());
			readItem.setDbColumn(resultSet.getString("db_column"));
			readItem.setFileColumn(resultSet.getString("file_column"));
			readItem.setDefaultValue(resultSet.getString("default_value"));
			readItem.setEncrypted(resultSet.getBigDecimal("encrypted").intValue() > 0);
			
			return readItem;
		}
    }
    
    public int getDelimiterValueForOracle(ExportPredef item) {
		if ("'".equals(item.getDelimiter())) {
			return 1;
		} else {
			return 0;
		}
    }
    
    public int getSeparatorValueForOracle(ExportPredef item) {
		if (",".equals(item.getSeparator())) {
			return 1;
		} else if ("|".equals(item.getSeparator())) {
			return 2;
		} else if ("\t".equals(item.getSeparator())) {
			return 3;
		} else {
			return 0;
		}
    }
}
