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
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.agnitas.beans.ExportPredef;
import org.agnitas.beans.impl.ExportPredefImpl;
import org.agnitas.dao.ExportPredefDao;
import org.agnitas.dao.impl.mapper.IntegerRowMapper;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.util.AgnUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;

import com.agnitas.dao.DaoUpdateReturnValueCheck;

public class ExportPredefDaoImpl extends BaseDaoImpl implements ExportPredefDao {
	private static final transient Logger logger = Logger.getLogger(ExportPredefDaoImpl.class);
	
	@Override
	public ExportPredef get(int id, @VelocityCheck int companyID) {
		if (companyID != 0 && id != 0) {
			return selectObjectDefaultNull(logger, "SELECT * FROM export_predef_tbl WHERE export_predef_id = ? AND company_id = ?", new ExportPredefRowMapper(isOracleDB()), id, companyID);
		} else {
			return null;
		}
	}

	@Override
	public ExportPredef create(@VelocityCheck int companyID) {
		if (companyID == 0) {
			return null;
		} else {
			ExportPredef exportPredef = new ExportPredefImpl();
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

			if (exists) {
				update(logger, "UPDATE export_predef_tbl SET charset = ?, columns = ?, shortname = ?, description = ?, mailinglists = ?, mailinglist_id = ?, delimiter_char = ?, always_quote = ?, separator_char = ?, target_id = ?, user_type = ?, user_status = ?, deleted = ?, timestamp_start = ?, timestamp_end = ?, timestamp_lastdays = ?, creation_date_start = ?, creation_date_end = ?, creation_date_lastdays = ?, mailinglist_bind_start = ?, mailinglist_bind_end = ?, mailinglist_bind_lastdays = ? WHERE export_predef_id = ? AND company_id = ?",
					exportPredef.getCharset(),
					exportPredef.getColumns(),
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
					exportPredef.getCreationDateStart(),
					exportPredef.getCreationDateEnd(),
					exportPredef.getCreationDateLastDays(),
					exportPredef.getMailinglistBindStart(),
					exportPredef.getMailinglistBindEnd(),
					exportPredef.getMailinglistBindLastDays(),
					exportPredef.getId(),
					exportPredef.getCompanyID());
			} else {
				if (isOracleDB()) {
					int newExportPredefID = selectInt(logger, "SELECT export_predef_tbl_seq.NEXTVAL FROM DUAL");
					update(logger, "INSERT INTO export_predef_tbl (export_predef_id, company_id, charset, columns, shortname, description, mailinglists, mailinglist_id, delimiter_char, always_quote, separator_char, target_id, user_type, user_status, deleted, timestamp_start, timestamp_end, timestamp_lastdays, creation_date_start, creation_date_end, creation_date_lastdays, mailinglist_bind_start, mailinglist_bind_end, mailinglist_bind_lastdays) VALUES (" + AgnUtils.repeatString("?", 24, ", ") + ")",
						newExportPredefID,
						exportPredef.getCompanyID(),
						exportPredef.getCharset(),
						exportPredef.getColumns(),
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
						exportPredef.getCreationDateStart(),
						exportPredef.getCreationDateEnd(),
						exportPredef.getCreationDateLastDays(),
						exportPredef.getMailinglistBindStart(),
						exportPredef.getMailinglistBindEnd(),
						exportPredef.getMailinglistBindLastDays());
					exportPredef.setId(newExportPredefID);
				} else {
					int newExportPredefID = insertIntoAutoincrementMysqlTable(logger, "export_predef_id", "INSERT INTO export_predef_tbl (company_id, charset, columns, shortname, description, mailinglists, mailinglist_id, delimiter_char, always_quote, separator_char, target_id, user_type, user_status, deleted, timestamp_start, timestamp_end, timestamp_lastdays, creation_date_start, creation_date_end, creation_date_lastdays, mailinglist_bind_start, mailinglist_bind_end, mailinglist_bind_lastdays) VALUES (" + AgnUtils.repeatString("?", 23, ", ") + ")",
						exportPredef.getCompanyID(),
						exportPredef.getCharset(),
						exportPredef.getColumns(),
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
						exportPredef.getCreationDateStart(),
						exportPredef.getCreationDateEnd(),
						exportPredef.getCreationDateLastDays(),
						exportPredef.getMailinglistBindStart(),
						exportPredef.getMailinglistBindEnd(),
						exportPredef.getMailinglistBindLastDays());
					exportPredef.setId(newExportPredefID);
				}
			}
		}
		
		return exportPredef.getId();
	}

	@Override
	@DaoUpdateReturnValueCheck
	public boolean delete(ExportPredef exportPredef) {
		int touchedLines = update(logger, "DELETE FROM export_predef_tbl WHERE export_predef_id = ? AND company_id = ?", exportPredef.getId(), exportPredef.getCompanyID());
		return touchedLines > 0;
	}
	
	@Override
	public boolean deleteAllByCompanyID(@VelocityCheck int companyID) {
		int touchedLines = update(logger, "DELETE FROM export_predef_tbl WHERE company_id = ?", companyID);
		return touchedLines > 0;
	}

	@Override
	public boolean delete(int id, @VelocityCheck int companyID) {
		ExportPredef exportPredef = get(id, companyID);

		if (exportPredef != null) {
			return delete(exportPredef);
		} else {
			return false;
		}
	}

	@Override
	public List<ExportPredef> getAllByCompany(@VelocityCheck int companyId) {
		return select(logger, "SELECT * FROM export_predef_tbl WHERE deleted = 0 AND company_id = ?", new ExportPredefRowMapper(isOracleDB()), companyId);
	}

	@Override
	public List<ExportPredef> getAllByCompany(@VelocityCheck int companyId, Collection<Integer> disabledMailingListIds) {
		if (CollectionUtils.isEmpty(disabledMailingListIds)) {
			return getAllByCompany(companyId);
		}

		String sqlGetAll = "SELECT * FROM export_predef_tbl " +
				"WHERE deleted = 0 AND company_id = ? " +
				"AND mailinglist_id NOT IN (" + StringUtils.join(disabledMailingListIds, ',') + ")";

		List<ExportPredef> profiles = new ArrayList<>();
		RowMapper<ExportPredef> rowMapper = new ExportPredefRowMapper(isOracleDB());

		query(logger, sqlGetAll, rs -> {
			if (validateMailingListIds(rs.getString("mailinglists"), disabledMailingListIds)) {
				profiles.add(rowMapper.mapRow(rs, 0));
			}
		}, companyId);

		return profiles;
	}

	@Override
	public List<Integer> getAllIdsByCompany(@VelocityCheck int companyId) {
		String sqlGetIds = "SELECT export_predef_id FROM export_predef_tbl WHERE deleted = 0 AND company_id = ?";
		return select(logger, sqlGetIds, new IntegerRowMapper(), companyId);
	}

	@Override
	public List<Integer> getAllIdsByCompany(@VelocityCheck int companyId, Collection<Integer> disabledMailingListIds) {
		if (CollectionUtils.isEmpty(disabledMailingListIds)) {
			return getAllIdsByCompany(companyId);
		}

		String sqlGetIds = "SELECT export_predef_id, mailinglists FROM export_predef_tbl " +
				"WHERE deleted = 0 AND company_id = ? " +
				"AND mailinglist_id NOT IN (" + StringUtils.join(disabledMailingListIds, ',') + ")";

		List<Integer> ids = new ArrayList<>();

		query(logger, sqlGetIds, rs -> {
			if (validateMailingListIds(rs.getString("mailinglists"), disabledMailingListIds)) {
				ids.add(rs.getInt("export_predef_id"));
			}
		}, companyId);

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

    private static class ExportPredefRowMapper implements RowMapper<ExportPredef> {
		private boolean isOracle;

		public ExportPredefRowMapper(boolean isOracle) {
			this.isOracle = isOracle;
		}

		@Override
		public ExportPredef mapRow(ResultSet resultSet, int row) throws SQLException {
			ExportPredef readItem = new ExportPredefImpl();
			
			readItem.setId(resultSet.getBigDecimal("export_predef_id").intValue());
			readItem.setCompanyID(resultSet.getBigDecimal("company_id").intValue());
			readItem.setCharset(resultSet.getString("charset"));
			readItem.setColumns(resultSet.getString("columns"));
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
			readItem.setCreationDateStart(resultSet.getTimestamp("creation_date_start"));
			readItem.setCreationDateEnd(resultSet.getTimestamp("creation_date_end"));
			readItem.setCreationDateLastDays(resultSet.getInt("creation_date_lastdays"));
			readItem.setMailinglistBindStart(resultSet.getTimestamp("mailinglist_bind_start"));
			readItem.setMailinglistBindEnd(resultSet.getTimestamp("mailinglist_bind_end"));
			readItem.setMailinglistBindLastDays(resultSet.getInt("mailinglist_bind_lastdays"));
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
