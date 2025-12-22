/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailing.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.agnitas.beans.PaginatedList;
import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.dao.impl.PaginatedBaseDaoImpl;
import com.agnitas.emm.core.mailing.bean.MailingParameter;
import com.agnitas.emm.core.mailing.dao.MailingParameterDao;
import com.agnitas.emm.core.mailing.forms.MailingParamOverviewFilter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.RowMapper;

public class MailingParameterDaoImpl extends PaginatedBaseDaoImpl implements MailingParameterDao {
	
	private static final String INSERT_ORACLE = "INSERT INTO mailing_info_tbl (mailing_info_id, mailing_id, company_id, name, value, description, creation_date, change_date, creation_admin_id, change_admin_id) VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, ?, ?)";
	private static final String INSERT_MYSQL = "INSERT INTO mailing_info_tbl (mailing_id, company_id, name, value, description, creation_date, change_date, creation_admin_id, change_admin_id) VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, ?, ?)";
	private static final String UPDATE = "UPDATE mailing_info_tbl SET mailing_id = ?, company_id = ?, name = ?, value = ?, description = ?, change_date = CURRENT_TIMESTAMP, change_admin_id = ? WHERE mailing_info_id = ?";
	private static final String DELETE_BY_MAILINGINFOID = "DELETE FROM mailing_info_tbl WHERE mailing_info_id = ?";
	private static final String DELETE_BY_COMPANYID = "DELETE FROM mailing_info_tbl WHERE company_id = ?";
		
	/** Query for selecting mailing parameter by name. */
	private static final String SELECT_BY_NAME = "SELECT mailing_info_id, mailing_id, company_id, name, value, description, change_date, change_admin_id, creation_date, creation_admin_id FROM mailing_info_tbl WHERE company_id = ? AND mailing_id = ? and name = ?";
	
	@Override
	public List<MailingParameter> getAllParameters(int companyID) {
		return select("SELECT mailing_info_id, mailing_id, company_id, name, value, description, change_date, change_admin_id, creation_date, creation_admin_id FROM mailing_info_tbl WHERE company_id = ? ORDER BY creation_date", new MailingParameter_RowMapper(), companyID);
	}
	
	@Override
	public List<MailingParameter> getMailingParameters(int companyID, int mailingID) {
		return select("SELECT mailing_info_id, mailing_id, company_id, name, value, description, change_date, change_admin_id, creation_date, creation_admin_id FROM mailing_info_tbl WHERE company_id = ? AND mailing_id = ? ORDER BY creation_date", new MailingParameter_RowMapper(), companyID, mailingID);
	}

	@Override
	public PaginatedList<MailingParameter> getParameters(MailingParamOverviewFilter filter, int companyID) {
		StringBuilder query = new StringBuilder("SELECT mailing_info_id, mailing_id, company_id, name, value, description, change_date, change_admin_id, creation_date, creation_admin_id FROM mailing_info_tbl info");
		List<Object> params = applyOverviewFilter(filter, companyID, query);

		PaginatedList<MailingParameter> list = selectPaginatedList(query.toString(), "mailing_info_tbl", filter.getSortOrDefault("creation_date"),
				filter.ascending(), filter.getPage(), filter.getNumberOfRows(), new MailingParameter_RowMapper(), params.toArray());

		if (filter.isUiFiltersSet()) {
			list.setNotFilteredFullListSize(getTotalUnfilteredCountForOverview(companyID));
		}

		return list;
	}

	private List<Object> applyOverviewFilter(MailingParamOverviewFilter filter, int companyId, StringBuilder query) {
		List<Object> params = applyRequiredOverviewFilter(query, companyId);

		if (StringUtils.isNotBlank(filter.getName())) {
			query.append(getPartialSearchFilterWithAnd("info.name"));
			params.add(filter.getName());
		}

		if (StringUtils.isNotBlank(filter.getDescription())) {
			query.append(getPartialSearchFilterWithAnd("info.description"));
			params.add(filter.getDescription());
		}

		if (filter.getMailingId() != null) {
			query.append(getPartialSearchFilterWithAnd("info.mailing_id", filter.getMailingId(), params));
		}

		query.append(getDateRangeFilterWithAnd("info.change_date", filter.getChangeDate(), params));

		return params;
	}

	private int getTotalUnfilteredCountForOverview(int companyId) {
		StringBuilder query = new StringBuilder("SELECT COUNT(*) FROM mailing_info_tbl info");
		List<Object> params = applyRequiredOverviewFilter(query, companyId);

		return selectIntWithDefaultValue(query.toString(), 0, params.toArray());
	}

	private List<Object> applyRequiredOverviewFilter(StringBuilder query, int companyId) {
		query.append(" WHERE info.company_id = ?");
		return new ArrayList<>(List.of(companyId));
	}

	@Override
	public MailingParameter getParameter(int mailingInfoID) {
		return selectObjectDefaultNull("SELECT mailing_info_id, mailing_id, company_id, name, value, description, change_date, change_admin_id, creation_date, creation_admin_id FROM mailing_info_tbl WHERE mailing_info_id = ?", new MailingParameter_RowMapper(), mailingInfoID);
	}
	
	@Override
	@DaoUpdateReturnValueCheck
	public boolean insertParameter(MailingParameter parameter) {
		logDebugStmt(INSERT_ORACLE);
		int touchedLines;
		int newId;
		if (isOracleDB()) {
			newId = selectInt("SELECT mailing_info_tbl_seq.NEXTVAL FROM DUAL");
			touchedLines = update(
				INSERT_ORACLE,
				newId,
				parameter.getMailingID(),
				parameter.getCompanyID(),
				parameter.getName(),
				parameter.getValue(),
				parameter.getDescription(),
				parameter.getCreationAdminID(),
				parameter.getChangeAdminID());
			
			if (touchedLines != 1) {
				return false;
			}
		} else {
	        Object[] values = new Object[] {
        		parameter.getMailingID(),
        		parameter.getCompanyID(),
				parameter.getName(),
				parameter.getValue(),
				parameter.getDescription(),
				parameter.getCreationAdminID(),
				parameter.getChangeAdminID()};

	        newId = insert("mailing_info_id", INSERT_MYSQL, values);
		}
		
		// set the new id to refresh
		parameter.setMailingInfoID(newId);
		return true;
	}
	
	@Override
	@DaoUpdateReturnValueCheck
	public boolean updateParameter(MailingParameter parameter) {
		logDebugStmt(UPDATE);
		int touchedLines = update(
				UPDATE,
				parameter.getMailingID(),
				parameter.getCompanyID(),
				parameter.getName(),
				parameter.getValue(),
				parameter.getDescription(),
				parameter.getChangeAdminID(),
				parameter.getMailingInfoID());

		return touchedLines == 1;
	}

	@Override
	@DaoUpdateReturnValueCheck
	public boolean updateParameters(int companyID, int mailingID, List<MailingParameter> parameterList, int adminId) {
		boolean success = true;

		List<MailingParameter> parametersBeforeUpdate = getMailingParameters(companyID, mailingID);

		List<Integer> existingIds = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(parameterList)) {
			for (MailingParameter parameter : parameterList) {
				if (parameter.getMailingInfoID() > 0) {
					existingIds.add(parameter.getMailingInfoID());
				}
			}
		}

		if (existingIds.isEmpty()) {
			update("DELETE FROM mailing_info_tbl WHERE company_id = ? AND mailing_id = ?", companyID, mailingID);
		} else {
			update("DELETE FROM mailing_info_tbl WHERE company_id = ? AND mailing_id = ? AND mailing_info_id NOT IN (" + StringUtils.join(existingIds, ", ") + ")", companyID, mailingID);
		}

		if (CollectionUtils.isNotEmpty(parameterList)) {
			for (MailingParameter parameter : parameterList) {
				if (StringUtils.isNotBlank(parameter.getName())) {
					parameter.setCompanyID(companyID);
					parameter.setMailingID(mailingID);
					parameter.setChangeAdminID(adminId);

					if (parameter.getMailingInfoID() <= 0 || !updateParameter(parameter)) {
						parameter.setCreationAdminID(adminId);
						success = insertParameter(parameter) && success;
					}
				}
			}
		}

		if (!parametersBeforeUpdate.isEmpty()) {
			List<MailingParameter> parametersAfterUpdate = getMailingParameters(companyID, mailingID);

			if (parametersAfterUpdate.isEmpty()) {
				try {
					throw new IllegalStateException(String.format("Possible loss of mailing parameters. Had %d parameters when entering method, having none when leaving", parametersBeforeUpdate.size()));
				} catch (Exception e) {
					logger.fatal("Possible loss of mailing parameters!", e);
				}
			}
		}

		return success;
	}

	@Override
	@DaoUpdateReturnValueCheck
	public boolean deleteParameter(int mailingInfoID) {
		logDebugStmt(DELETE_BY_MAILINGINFOID);
		int touchedLines = update(DELETE_BY_MAILINGINFOID, mailingInfoID);
		return touchedLines == 1;
	}
	
	@Override
	@DaoUpdateReturnValueCheck
	public int deleteParameterByCompanyID(int companyID) {
		logDebugStmt(DELETE_BY_COMPANYID);

		// Return a number of affected rows.
		return update(DELETE_BY_COMPANYID, companyID);
	}
	
	private class MailingParameter_RowMapper implements RowMapper<MailingParameter> {
		@Override
		public MailingParameter mapRow(ResultSet resultSet, int row) throws SQLException {
			MailingParameter parameter = new MailingParameter();
			parameter.setMailingInfoID(resultSet.getInt("mailing_info_id"));
			parameter.setMailingID(resultSet.getInt("mailing_id"));
			parameter.setCompanyID(resultSet.getInt("company_id"));
			parameter.setName(resultSet.getString("name"));
			parameter.setValue(resultSet.getString("value"));
			parameter.setDescription(resultSet.getString("description"));
			parameter.setChangeDate(resultSet.getTimestamp("change_date"));
			parameter.setChangeAdminID(resultSet.getInt("change_admin_id"));
			parameter.setCreationDate(resultSet.getTimestamp("creation_date"));
			parameter.setCreationAdminID(resultSet.getInt("creation_admin_id"));
						
			return parameter;
		}
	}

	@Override
	public MailingParameter getParameterByName(String parameterName, int mailingId, int companyId) throws MailingParameterNotFoundException {
		MailingParameter parameter = getParameterByNameDefaultNull(parameterName, mailingId, companyId);
		if(parameter == null){
			throw new MailingParameterNotFoundException(parameterName, mailingId);
		}
		return parameter;
	}

	/**
	 * Doesn't throw exception if there is no {@link MailingParameter}
	 */
	private MailingParameter getParameterByNameDefaultNull(String parameterName, int mailingId, int companyId){
		List<MailingParameter> list = select(SELECT_BY_NAME, new MailingParameter_RowMapper(), companyId, mailingId, parameterName);

		if (list.isEmpty()) {
			list = select(SELECT_BY_NAME, new MailingParameter_RowMapper(), companyId, 0, parameterName);
		}

		if (list.isEmpty()) {
			return null;
		}

		return list.get(0);
	}

    private void logDebugStmt(String stmt) {
        if (logger.isDebugEnabled()) {
            logger.debug("stmt:{}", stmt);
        }
    }
}
