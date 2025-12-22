/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.dao.EmmActionDao;
import com.agnitas.dao.impl.mapper.IntegerRowMapper;
import com.agnitas.dao.impl.mapper.StringRowMapper;
import com.agnitas.emm.core.action.bean.EmmAction;
import com.agnitas.emm.core.action.bean.EmmActionImpl;
import com.agnitas.emm.core.action.operations.ActionOperationType;
import com.agnitas.util.DbUtilities;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.RowMapper;

public class EmmActionDaoImpl  extends PaginatedBaseDaoImpl implements EmmActionDao {
	
	@Override
	@DaoUpdateReturnValueCheck
	public int saveEmmAction(EmmAction action) {
		if (action == null || action.getCompanyID() == 0) {
			throw new IllegalArgumentException();
		}

		if (action.getId() == 0) {
			if (isOracleDB()) {
				action.setId(selectInt("SELECT rdir_action_tbl_seq.NEXTVAL FROM DUAL"));

				String sql = "INSERT INTO rdir_action_tbl (action_id, company_id, description, shortname, creation_date, change_date, action_type, active, advertising)"
					+ " VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, ?, ?, ?)";

				update(
					sql,
					action.getId(),
					action.getCompanyID(),
					action.getDescription(),
					action.getShortname(),
					action.getType(),
					action.getIsActive() ? 1 : 0,
					action.isAdvertising() ? 1 : 0
				);
			} else {
				String sql = "INSERT INTO rdir_action_tbl (company_id, description, shortname, creation_date, change_date, action_type, active, advertising)"
					+ " VALUES (?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, ?, ?, ?)";
				int newID = insert("action_id", sql,
					action.getCompanyID(),
					action.getDescription(),
					action.getShortname(),
					action.getType(),
					action.getIsActive() ? 1 : 0,
					action.isAdvertising() ? 1 : 0);

				// set the new id to refresh the item
				action.setId(newID);
			}
		} else {
			String updateSql = "UPDATE rdir_action_tbl SET description = ?, shortname = ?, change_date = CURRENT_TIMESTAMP, action_type = ?, active = ?, advertising = ? WHERE action_id = ? AND company_id = ?";
			update(updateSql, action.getDescription(), action.getShortname(), action.getType(), action.getIsActive() ? 1 : 0, action.isAdvertising() ? 1 : 0, action.getId(), action.getCompanyID());
		}

		return action.getId();
	}
	
	@Override
	public boolean actionExists(int actionID, int companyID) {
		int count = selectInt("SELECT COUNT(action_id) FROM rdir_action_tbl WHERE action_id=? AND company_id=?", actionID, companyID);
		return count > 0;
	}
	
	@Override
	public EmmAction getEmmAction(int actionID, int companyID) {
		if (actionID == 0 || companyID == 0) {
			return null;
		}
		String sql = "SELECT action_id, company_id, description, shortname, action_type, active, creation_date, change_date, advertising FROM rdir_action_tbl WHERE action_id = ? AND company_id = ?";
		return selectObjectDefaultNull(sql, new EmmAction_RowMapper(), actionID, companyID);
	}

	@Override
	@DaoUpdateReturnValueCheck
	public boolean markAsDeleted(int actionID, int companyID) {
		int touchedLines = update("UPDATE rdir_action_tbl SET active = 0, deleted = 1, change_date = CURRENT_TIMESTAMP WHERE action_id = ? AND company_id = ?", actionID, companyID);
		return touchedLines > 0;
	}
	
	@Override
	@DaoUpdateReturnValueCheck
	public boolean deleteEmmActionReally(int actionID, int companyID) {
		int touchedLines = update("DELETE FROM rdir_action_tbl WHERE action_id = ? AND company_id = ?", actionID, companyID);
		return touchedLines > 0;
	}

	@Override
	public List<Integer> findActionsUsingProfileField(String fieldName, int companyId) {
		String query = """
				SELECT DISTINCT(a.action_id)
				FROM rdir_action_tbl a
				         JOIN actop_tbl s on a.action_id = s.action_id
				         LEFT JOIN actop_update_customer_tbl auct ON auct.action_operation_id = s.action_operation_id
				         LEFT JOIN actop_identify_customer_tbl aict ON aict.action_operation_id = s.action_operation_id
				         LEFT JOIN actop_subscribe_customer_tbl asct ON asct.action_operation_id = s.action_operation_id
				WHERE a.deleted = 0 AND a.company_id = ?
				  AND (auct.column_name = ? OR aict.key_column = ? OR aict.pass_column = ? OR asct.key_column = ?)
				""".stripIndent();
		return select(query, IntegerRowMapper.INSTANCE, companyId, fieldName, fieldName, fieldName, fieldName);
	}

	@Override
	public void restore(Set<Integer> ids, int companyId) {
		update(
			"UPDATE rdir_action_tbl SET deleted = 0, change_date = CURRENT_TIMESTAMP WHERE company_id = ? AND "
				+ makeBulkInClauseForInteger("action_id", ids), companyId);
	}

	@Override
	public List<Integer> getMarkedAsDeletedBefore(Date date, int companyId) {
		return select(
			"SELECT action_id FROM rdir_action_tbl WHERE company_id = ? AND deleted = 1 AND change_date < ?",
			IntegerRowMapper.INSTANCE, companyId, date);
	}

	@Override
	public List<EmmAction> getEmmActions(int companyID) {
		return getEmmActions(companyID, false);
	}

	@Override
	public List<EmmAction> getEmmActions(int companyID, boolean includeDeleted) {
		String sql = "SELECT action_id, company_id, description, shortname, action_type, active, creation_date, change_date, advertising, deleted FROM rdir_action_tbl WHERE company_id = ?" +
				(includeDeleted ? "" : " AND deleted = 0") + " ORDER BY shortname";
		return select(sql, new EmmAction_RowMapper(), companyID);
	}

	@Override
	public List<EmmAction> getActiveEmmActionsByOperationType(int companyID, ActionOperationType... actionTypes) {
		if (actionTypes.length == 0 || companyID == 0) {
			return Collections.emptyList();
		}

		try {
			String sql = """
					SELECT action_id,
					       company_id,
					       description,
					       shortname,
					       action_type,
					       active,
					       creation_date,
					       change_date,
					       advertising
					FROM rdir_action_tbl
					WHERE company_id = ?
					  AND deleted = 0
					  AND active = 1
					""";

            List<Object> params = new ArrayList<>();
			params.add(companyID);
            for (ActionOperationType type : actionTypes) {
                sql += " AND EXISTS (SELECT 1 FROM actop_tbl WHERE rdir_action_tbl.action_id = actop_tbl.action_id AND actop_tbl.type = ?)";
                params.add(type.getName());
            }
			sql += " ORDER BY shortname";

			return select(sql, new EmmAction_RowMapper(), params.toArray());
		} catch (Exception e) {
			return Collections.emptyList();
		}
	}

	@Override
	public List<EmmAction> getEmmActionsByName(int companyID, String shortName) {
		String query = "SELECT action_id, company_id, description, shortname, action_type, active, creation_date, change_date, advertising FROM rdir_action_tbl WHERE company_id = ? AND deleted = 0 AND shortname = ?";
		return select(query, new EmmAction_RowMapper(), companyID, shortName);
	}

	@Override
	public String getEmmActionName(int actionId, int companyId) {
		String sql = "SELECT shortname FROM rdir_action_tbl WHERE action_id = ? AND company_id = ?";
		return selectStringDefaultNull(sql, actionId, companyId);
	}

	@Override
	public Map<Integer, String> getEmmActionNames(int companyID, List<Integer> actionIds) {
		Map<Integer, String> actionNames = new HashMap<>();
		if (companyID > 0 && CollectionUtils.isNotEmpty(actionIds)) {
			String sql = "SELECT action_id, shortname FROM rdir_action_tbl WHERE company_id = ? AND action_id IN (" +
					StringUtils.join(actionIds, ",")
					+ ")";
			query(sql, rs -> actionNames.put(rs.getInt("action_id"), rs.getString("shortname")),  companyID);
		}
		return actionNames;
	}

	@Override
	public List<EmmAction> getEmmNotFormActions(int companyID) {
		return getEmmNotFormActions(companyID, true);
	}

	@Override
	public List<EmmAction> getEmmNotFormActions(int companyID, boolean includeInactive) {
		String sqlGetActions = "SELECT action_id, company_id, description, shortname, action_type, active, creation_date, change_date, advertising FROM rdir_action_tbl " +
				"WHERE company_id = ? AND action_type <> ? AND deleted = 0 " + (includeInactive ? "" : "AND active = 1 ") +
				"ORDER BY shortname";

		return select(sqlGetActions, new EmmAction_RowMapper(), companyID, EmmAction.TYPE_FORM);
	}

	@Override
	public List<EmmAction> getEmmNotLinkActions(int companyID) {
		return getEmmNotLinkActions(companyID, true);
	}

	@Override
	public List<EmmAction> getEmmNotLinkActions(int companyID, boolean includeInactive) {
		String sqlGetActions = "SELECT action_id, company_id, description, shortname, action_type, active, creation_date, change_date, advertising FROM rdir_action_tbl " +
				"WHERE company_id = ? AND action_type <> ? AND deleted = 0 " + (includeInactive ? "" : "AND active = 1 ") +
				"ORDER BY shortname";

		return select(sqlGetActions, new EmmAction_RowMapper(), companyID, EmmAction.TYPE_LINK);
	}

	@Override
	public List<String> getActionUserFormNames(int actionId, int companyId) {
		return select("SELECT formname FROM userform_tbl WHERE company_id = ? AND deleted = 0 AND (startaction_id = ? OR endaction_id = ?) ORDER BY formname", StringRowMapper.INSTANCE,
				 companyId, actionId, actionId);
	}

	@Override
	public void setActiveness(Collection<Integer> actionIds, boolean active, int companyId) {
		if (CollectionUtils.isEmpty(actionIds) || companyId <= 0) {
			return;
		}

		String sqlSetActiveness = "UPDATE rdir_action_tbl SET active = ?, change_date = CURRENT_TIMESTAMP " +
				"WHERE company_id = ? AND action_id IN (" + StringUtils.join(actionIds, ',') + ")";

		update(sqlSetActiveness, BooleanUtils.toInteger(active), companyId);
	}

	@Override
	public List<EmmAction> getActionListBySendMailingId(int companyId, int mailingId) {
		String sqlGetActionsBySendMailingId = "SELECT ra.action_id, ra.company_id, ra.description, ra.shortname, "
				+ "ra.action_type, ra.active, ra.creation_date, ra.change_date, ra.advertising "
				+ "FROM rdir_action_tbl ra JOIN actop_tbl at ON ra.action_id = at.action_id "
				+ "JOIN actop_send_mailing_tbl atsm ON atsm.action_operation_id = at.action_operation_id "
				+ "WHERE atsm.mailing_id = ? AND ra.company_id = ?";

		return select(sqlGetActionsBySendMailingId, new EmmAction_RowMapper(), mailingId, companyId);
	}

	@Override
	public boolean isAdvertising(int id, int companyId) {
		String query = "SELECT advertising FROM rdir_action_tbl WHERE action_id = ? AND company_id = ?";
		return selectInt(query, id, companyId) > 0;
	}

	@Override
	public boolean isActive(int id) {
		return selectIntWithDefaultValue("SELECT active FROM rdir_action_tbl WHERE action_id = ?", 0, id) > 0;
	}

	private static class EmmAction_RowMapper implements RowMapper<EmmAction> {
		@Override
		public EmmAction mapRow(ResultSet resultSet, int row) throws SQLException {
			EmmAction readItem = new EmmActionImpl();

			readItem.setId(resultSet.getInt("action_id"));
			readItem.setCompanyID(resultSet.getInt("company_id"));
			readItem.setShortname(resultSet.getString("shortname"));
			readItem.setDescription(resultSet.getString("description"));
			readItem.setType(resultSet.getInt("action_type"));
			readItem.setIsActive(BooleanUtils.toBoolean(resultSet.getInt("active")));
			readItem.setAdvertising(BooleanUtils.toBoolean(resultSet.getInt("advertising")));
			readItem.setCreationDate(resultSet.getTimestamp("creation_date"));
			readItem.setChangeDate(resultSet.getTimestamp("change_date"));
			if (DbUtilities.resultsetHasColumn(resultSet, "deleted")) {
				readItem.setDeleted(resultSet.getBoolean("deleted"));
			}

			return readItem;
		}
	}

}
