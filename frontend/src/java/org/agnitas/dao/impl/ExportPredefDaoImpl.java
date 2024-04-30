/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.agnitas.beans.ExportColumnMapping;
import org.agnitas.beans.ExportPredef;
import org.agnitas.dao.ExportPredefDao;
import org.agnitas.dao.impl.mapper.IntegerRowMapper;
import org.agnitas.util.AgnUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;

import com.agnitas.dao.DaoUpdateReturnValueCheck;

public class ExportPredefDaoImpl extends BaseDaoImpl implements ExportPredefDao {

	private static final Logger logger = LogManager.getLogger(ExportPredefDaoImpl.class);
	
	@Override
	public ExportPredef get(int id, int companyID) {
		if (companyID != 0 && id != 0) {
			return selectObjectDefaultNull(logger, "SELECT * FROM export_predef_tbl WHERE export_predef_id = ? AND company_id = ?", new ExportPredefRowMapper(isOracleDB()), id, companyID);
		} else {
			return null;
		}
	}

	@Override
	public ExportPredef create(int companyID) {
		if (companyID == 0) {
			return null;
		} else {
			ExportPredef exportPredef = new ExportPredef();
			exportPredef.setId(0);
			exportPredef.setCompanyID(companyID);

			save(exportPredef);

			return exportPredef;
		}
	}

	@Override
	@DaoUpdateReturnValueCheck
	public int save(ExportPredef exportPredef) {
		if (exportPredef == null || exportPredef.getCompanyID() == 0) {
			return 0;
		} else {
			boolean exists = false;

			if (exportPredef.getId() != 0) {
				exists = selectInt(logger, "SELECT COUNT(*) FROM export_predef_tbl WHERE export_predef_id = ? AND company_id = ?", exportPredef.getId(), exportPredef.getCompanyID()) > 0;
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

			if (exists) {
				update(logger, "UPDATE export_predef_tbl SET charset = ?, columns = ?, shortname = ?, description = ?, mailinglists = ?, mailinglist_id = ?, delimiter_char = ?, always_quote = ?, separator_char = ?, target_id = ?, user_type = ?, user_status = ?, deleted = ?, timestamp_start = ?, timestamp_end = ?, timestamp_lastdays = ?, timestamp_includecurrent = ?, creation_date_start = ?, creation_date_end = ?, creation_date_lastdays = ?, creation_date_includecurrent = ?, mailinglist_bind_start = ?, mailinglist_bind_end = ?, mailinglist_bind_lastdays = ?, ml_bind_includecurrent = ?, dateformat = ?, datetimeformat = ?, timezone = ?, locale_lang = ?, locale_country = ?, decimalseparator = ?, limits_linked_by_and = ? WHERE export_predef_id = ? AND company_id = ?",
					exportPredef.getCharset(),
					columnsListString,
					exportPredef.getShortname(),
					exportPredef.getDescription(),
					exportPredef.getMailinglists(),
					exportPredef.getMailinglistID(),
					isOracleDB() ? getDelimiterValueForOracle(exportPredef) : exportPredef.getDelimiter(),
					exportPredef.isAlwaysQuote() ? 1 : 0,
					isOracleDB() ? getSeparatorValueForOracle(exportPredef) : exportPredef.getSeparator(),
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
					exportPredef.isTimeLimitsLinkedByAnd() ? 1 : 0,
					exportPredef.getId(),
					exportPredef.getCompanyID());
			} else {
				if (isOracleDB()) {
					int newExportPredefID = selectInt(logger, "SELECT export_predef_tbl_seq.NEXTVAL FROM DUAL");
					update(logger, "INSERT INTO export_predef_tbl (export_predef_id, company_id, charset, columns, shortname, description, mailinglists, mailinglist_id, delimiter_char, always_quote, separator_char, target_id, user_type, user_status, deleted, timestamp_start, timestamp_end, timestamp_lastdays, timestamp_includecurrent, creation_date_start, creation_date_end, creation_date_lastdays, creation_date_includecurrent, mailinglist_bind_start, mailinglist_bind_end, mailinglist_bind_lastdays, ml_bind_includecurrent, dateformat, datetimeformat, timezone, locale_lang, locale_country, decimalseparator, limits_linked_by_and) VALUES (" + AgnUtils.repeatString("?", 34, ", ") + ")",
						newExportPredefID,
						exportPredef.getCompanyID(),
						exportPredef.getCharset(),
						columnsListString,
						exportPredef.getShortname(),
						exportPredef.getDescription(),
						exportPredef.getMailinglists(),
						exportPredef.getMailinglistID(),
						getDelimiterValueForOracle(exportPredef),
						exportPredef.isAlwaysQuote() ? 1 : 0,
						getSeparatorValueForOracle(exportPredef),
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
						exportPredef.isTimeLimitsLinkedByAnd() ? 1 : 0);
					exportPredef.setId(newExportPredefID);
				} else {
					int newExportPredefID = insertIntoAutoincrementMysqlTable(logger, "export_predef_id", "INSERT INTO export_predef_tbl (company_id, charset, columns, shortname, description, mailinglists, mailinglist_id, delimiter_char, always_quote, separator_char, target_id, user_type, user_status, deleted, timestamp_start, timestamp_end, timestamp_lastdays, timestamp_includecurrent, creation_date_start, creation_date_end, creation_date_lastdays, creation_date_includecurrent, mailinglist_bind_start, mailinglist_bind_end, mailinglist_bind_lastdays, ml_bind_includecurrent, dateformat, datetimeformat, timezone, locale_lang, locale_country, decimalseparator, limits_linked_by_and) VALUES (" + AgnUtils.repeatString("?", 33, ", ") + ")",
						exportPredef.getCompanyID(),
						exportPredef.getCharset(),
						columnsListString,
						exportPredef.getShortname(),
						exportPredef.getDescription(),
						exportPredef.getMailinglists(),
						exportPredef.getMailinglistID(),
						exportPredef.getDelimiter(),
						exportPredef.isAlwaysQuote() ? 1 : 0,
						exportPredef.getSeparator(),
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
						exportPredef.isTimeLimitsLinkedByAnd() ? 1 : 0);
					exportPredef.setId(newExportPredefID);
				}
			}

			update(logger, "DELETE FROM export_column_mapping_tbl WHERE export_predef_id = ?", exportPredef.getId());
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
		            batchupdate(logger, "INSERT INTO export_column_mapping_tbl (id, export_predef_id, db_column, file_column, default_value, encrypted) VALUES (export_column_mapping_tbl_seq.NEXTVAL, ?, ?, ?, ?, ?)", parameterList);
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
		            batchupdate(logger, "INSERT INTO export_column_mapping_tbl (export_predef_id, db_column, file_column, default_value, encrypted) VALUES (?, ?, ?, ?, ?)", parameterList);
				}
	        }
		}

		return exportPredef.getId();
	}

	@Override
	@DaoUpdateReturnValueCheck
	public boolean delete(ExportPredef exportPredef) {
		update(logger, "DELETE FROM export_column_mapping_tbl WHERE export_predef_id = ? AND export_predef_id IN (SELECT export_predef_id FROM export_predef_tbl WHERE company_id = ?)", exportPredef.getId(), exportPredef.getCompanyID());
		int touchedLines = update(logger, "DELETE FROM export_predef_tbl WHERE export_predef_id = ? AND company_id = ?", exportPredef.getId(), exportPredef.getCompanyID());
		return touchedLines > 0;
	}

	@Override
	public boolean deleteAllByCompanyID(int companyID) {
		update(logger, "DELETE FROM export_column_mapping_tbl WHERE export_predef_id IN (SELECT export_predef_id FROM export_predef_tbl WHERE company_id = ?)", companyID);
		int touchedLines = update(logger, "DELETE FROM export_predef_tbl WHERE company_id = ?", companyID);
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
		return selectWithDefaultValue(logger, query, String.class, "", id, companyId);
	}

	@Override
	public List<Integer> findTargetDependentExportProfiles(int targetGroupId, int companyId) {
		String query = "SELECT export_predef_id FROM export_predef_tbl WHERE company_id = ? AND target_id = ? AND deleted = 0";
		return select(logger, query, IntegerRowMapper.INSTANCE, companyId, targetGroupId);
	}

	@Override
	public List<ExportPredef> getAllByCompany(int companyId) {
		return getAllByCompany(companyId, null, 0);
	}

	@Override
	public List<ExportPredef> getAllByCompany(int companyId, final Collection<Integer> disabledMailingListIds, final int targetId) {
		final List<Object> params = new ArrayList<>();
		String sql = "SELECT * FROM export_predef_tbl WHERE deleted = 0 AND company_id = ?";
		params.add(companyId);
		
		if (targetId > 0) {
			sql += " AND target_id = ? ";
			params.add(targetId);
		}
		
		if (CollectionUtils.isNotEmpty(disabledMailingListIds)) {
			sql += " AND mailinglist_id > 0 AND mailinglist_id NOT IN (" + StringUtils.join(disabledMailingListIds, ',') + ") ";
		}

		final List<ExportPredef> profiles = new ArrayList<>();
		final RowMapper<ExportPredef> rowMapper = new ExportPredefRowMapper(isOracleDB());
		final RowCallbackHandler rowCallbackHandler;

		if(CollectionUtils.isNotEmpty(disabledMailingListIds)) {
			rowCallbackHandler = rs -> {
				if (validateMailingListIds(rs.getString("mailinglists"), disabledMailingListIds)) {
					profiles.add(rowMapper.mapRow(rs, 0));
				}
			};
		} else {
			rowCallbackHandler = rs -> profiles.add(rowMapper.mapRow(rs, 0));
		}

		query(logger, sql, rowCallbackHandler, params.toArray());

		return profiles;
	}

	@Override
	public List<Integer> getAllIdsByCompany(int companyId) {
		return getAllIdsByCompany(companyId, null, 0);
	}

	@Override
	public List<Integer> getAllIdsByCompany(int companyId, Collection<Integer> disabledMailingListIds, final int targetId) {
		final List<Object> params = new ArrayList<>();
		String sqlGetIds = "SELECT export_predef_id, mailinglists FROM export_predef_tbl " +
				"WHERE deleted = 0 AND company_id = ? ";
		params.add(companyId);

		if (targetId > 0) {
			sqlGetIds += " AND target_id = ? ";
			params.add(targetId);
		}
		
		if (CollectionUtils.isNotEmpty(disabledMailingListIds)) {
			sqlGetIds += " AND mailinglist_id > 0 AND mailinglist_id NOT IN (" + StringUtils.join(disabledMailingListIds, ',') + ") ";
		}

		final List<Integer> ids = new ArrayList<>();
		final RowCallbackHandler rowCallbackHandler;
		if(CollectionUtils.isNotEmpty(disabledMailingListIds)) {
			rowCallbackHandler = rs -> {
				if (validateMailingListIds(rs.getString("mailinglists"), disabledMailingListIds)) {
					ids.add(rs.getInt("export_predef_id"));
				}
			};
		} else {
			rowCallbackHandler = rs -> ids.add(rs.getInt("export_predef_id"));
		}

		query(logger, sqlGetIds, rowCallbackHandler, params.toArray());

		return ids;
	}

	private static boolean validateMailingListIds(String ids, Collection<Integer> disabledMailingListIds) {
		if (StringUtils.isBlank(ids)) {
			return true;
		}

		List<Integer> mailingListIds = AgnUtils.splitAndTrimList(ids).stream().map(NumberUtils::toInt)
				.filter(id -> id > 0)
				.collect(Collectors.toList());

		return mailingListIds.isEmpty() || !disabledMailingListIds.containsAll(mailingListIds);
	}

    private class ExportPredefRowMapper implements RowMapper<ExportPredef> {
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
			
			List<ExportColumnMapping> exportColumnMappings = select(logger, "SELECT id, db_column, file_column, default_value, encrypted FROM export_column_mapping_tbl WHERE export_predef_id = ? ORDER BY id", new ExportColumnMappingRowMapper(), readItem.getId());
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
			
			readItem.setTimeLimitsLinkedByAnd(resultSet.getInt("limits_linked_by_and") > 0);

			return readItem;
		}
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
