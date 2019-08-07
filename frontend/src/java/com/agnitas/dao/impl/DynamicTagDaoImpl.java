/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.agnitas.beans.DynamicTag;
import com.agnitas.beans.impl.DynamicTagImpl;
import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.dao.DynamicTagDao;
import org.agnitas.dao.impl.BaseDaoImpl;
import org.agnitas.dao.impl.mapper.StringRowMapper;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;

public class DynamicTagDaoImpl extends BaseDaoImpl implements DynamicTagDao {
	private static final transient Logger logger = Logger.getLogger(DynamicTagDaoImpl.class);

	@Override
	public int getIdForName(int mailingID, String name) {
		try {
			return selectInt(logger, "SELECT dyn_name_id FROM dyn_name_tbl WHERE mailing_id = ? AND dyn_name = ?", mailingID, name);
		} catch (Exception e) {
			logger.error("Error getting ID for tag: " + name, e);

			return 0;
		}
	}
	
	@Override
	public List<DynamicTag> getNameList(@VelocityCheck int companyId, int mailingId) {
		return select(logger, "SELECT company_id, dyn_name_id, dyn_name FROM dyn_name_tbl WHERE mailing_id = ? AND company_id = ? AND deleted = 0", new DynamicTag_RowMapper(), mailingId,
				companyId);
	}

	private class DynamicTag_RowMapper implements RowMapper<DynamicTag> {
		@Override
		public DynamicTag mapRow(ResultSet resultSet, int row) throws SQLException {
			DynamicTag dynamicTag = new DynamicTagImpl();

			dynamicTag.setCompanyID(resultSet.getInt("company_id"));
			dynamicTag.setId(resultSet.getInt("dyn_name_id"));
			dynamicTag.setDynName(resultSet.getString("dyn_name"));

			return dynamicTag;
		}
	}
	
	@Override
	public void markNameAsDeleted(int mailingID, String name) {
		setDynNameDeletionMark(mailingID, true, Collections.singletonList(name));
	}
	
	@Override
	public void markNamesAsDeleted(int mailingID, List<String> names) {
		setDynNameDeletionMark(mailingID, true, names);
	}
	
	@Override
	public void markNameAsUsed(int mailingID, String name) {
		setDynNameDeletionMark(mailingID, false, Collections.singletonList(name));
	}

	@Override
	public void markNamesAsUsed(int mailingID, List<String> names) {
		setDynNameDeletionMark(mailingID, false, names);
	}
	
	@DaoUpdateReturnValueCheck
	protected void setDynNameDeletionMark(int mailingID, boolean setDeleted, List<String> nameList) {
		if (nameList.isEmpty()) {
			return;
		}
		
		String updateSql = "";
		if (setDeleted) {
			String deletionDateValue = isOracleDB() ? "SYSDATE" : "CURRENT_TIMESTAMP";
			updateSql = "UPDATE dyn_name_tbl SET change_date = current_timestamp, deleted = 1, deletion_date = " + deletionDateValue;
		} else {
			updateSql = "UPDATE dyn_name_tbl SET change_date = current_timestamp, deleted = 0, deletion_date = null";
		}
		
		updateSql += " WHERE mailing_id = ? AND " + makeBulkInClauseForString("dyn_name", nameList);
		
		update(logger, updateSql, mailingID);
	}

	@Override
	@DaoUpdateReturnValueCheck
	public void deleteDynamicTagsMarkAsDeleted(int retentionTime) {
		// Determine threshold for date
		GregorianCalendar threshold = new GregorianCalendar();
		threshold.add(Calendar.DAY_OF_MONTH, -retentionTime);

		// Deleted marked and outdated records
		update(logger, "DELETE FROM dyn_content_tbl WHERE dyn_name_id IN (SELECT dyn_name_id FROM dyn_name_tbl WHERE deleted = 1 AND deletion_date IS NOT NULL AND deletion_date < ?)", threshold);
		update(logger, "DELETE FROM dyn_name_tbl WHERE deleted = 1 AND deletion_date IS NOT NULL AND deletion_date < ?", threshold);
	}
	
	@Override
	public boolean deleteDynamicTagsByCompany(@VelocityCheck int companyID) {
		try {
			update(logger, "DELETE FROM dyn_content_tbl WHERE company_id = ?", companyID);
			update(logger, "DELETE FROM dyn_name_tbl WHERE company_id = ?", companyID);
			return true; 
		} catch (Exception e) {
			logger.error("Error deleting content data (company ID: " + companyID + ")", e);
			return false;
		}
	}

	@Override
	public String getDynamicTagInterestGroup(@VelocityCheck int companyId, int mailingId, int dynTagId) {
		return select(logger, "SELECT interest_group FROM dyn_name_tbl WHERE mailing_id = ? AND company_id = ? AND dyn_name_id = ?", String.class, mailingId, companyId, dynTagId);
	}

	@Override
	public int getId(@VelocityCheck int companyId, int mailingId, String dynTagName) {
		String sqlGetId = "SELECT dyn_name_id FROM dyn_name_tbl WHERE company_id = ? AND mailing_id = ? AND dyn_name = ?";
		return selectInt(logger, sqlGetId, companyId, mailingId, dynTagName);
	}

	@Override
	public String getDynamicTagName(int companyId, int mailingId, int dynTagId) {
		String sqlGetDynName = "SELECT dyn_name FROM dyn_name_tbl WHERE company_id = ? AND mailing_id = ? AND dyn_name_id = ?";
		if (isOracleDB()) {
			sqlGetDynName += " AND ROWNUM = 1";
		} else {
			sqlGetDynName += " AND LIMIT 1";
		}

		List<String> nameResults = select(logger, sqlGetDynName, new StringRowMapper(), companyId, mailingId, dynTagId);

		if (nameResults.size() > 0) {
			return nameResults.get(0);
		} else {
			return null;
		}
	}
    
    @Override
    public Map<String, Integer> getDynTagIdsByName(int companyId, int mailingId, List<String> dynNames) {
		Map<String, Integer> dynNameIds = new HashMap<>();
		if (dynNames.isEmpty()) {
			return dynNameIds;
		}
		
		String sql = "SELECT dyn_name, dyn_name_id FROM dyn_name_tbl WHERE company_id = ? AND mailing_id = ? " +
				"AND " + makeBulkInClauseForString("dyn_name", dynNames);
		query(logger, sql, new DynNamesMapCallback(dynNameIds), companyId, mailingId);
		return dynNameIds;
    }
    
    private static class DynNamesMapCallback implements RowCallbackHandler {
		private Map<String, Integer> dynNamesIdsMap;

		public DynNamesMapCallback(Map<String, Integer> dynNamesIdsMap) {
			this.dynNamesIdsMap = Objects.requireNonNull(dynNamesIdsMap);
		}

		@Override
		public void processRow(ResultSet rs) throws SQLException {
			dynNamesIdsMap.put(rs.getString("dyn_name"), rs.getInt("dyn_name_id"));
		}
	}
}
