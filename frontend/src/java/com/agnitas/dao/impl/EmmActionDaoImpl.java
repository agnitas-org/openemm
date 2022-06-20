/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.agnitas.actions.EmmAction;
import org.agnitas.actions.impl.EmmActionImpl;
import org.agnitas.dao.EmmActionDao;
import org.agnitas.dao.impl.PaginatedBaseDaoImpl;
import org.agnitas.dao.impl.mapper.StringRowMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;

import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.emm.core.action.operations.ActionOperationType;

public class EmmActionDaoImpl  extends PaginatedBaseDaoImpl implements EmmActionDao {
	
	/** The logger. */
	private static final transient Logger logger = LogManager.getLogger(EmmActionDaoImpl.class);
	
	private final static List<String> VARCHAR_COLUMNS = Arrays.asList("description", "shortname");

	@Override
	@DaoUpdateReturnValueCheck
	public int saveEmmAction(EmmAction action) {
		if (action == null || action.getCompanyID() == 0) {
			return 0;
		} else {
			try {
				if (action.getId() == 0) {
					if (isOracleDB()) {
						action.setId(selectInt(logger, "SELECT rdir_action_tbl_seq.NEXTVAL FROM DUAL"));
						
						String sql = "INSERT INTO rdir_action_tbl (action_id, company_id, description, shortname, creation_date, change_date, action_type, active)"
							+ " VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, ?, ?)";
								
						update(logger,
							sql,
							action.getId(),
							action.getCompanyID(),
							action.getDescription(),
							action.getShortname(),
							action.getType(),
							action.getIsActive() ? 1 : 0);
					} else {
						String sql = "INSERT INTO rdir_action_tbl (company_id, description, shortname, creation_date, change_date, action_type, active)"
							+ " VALUES (?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, ?, ?)";
						int newID = insertIntoAutoincrementMysqlTable(logger, "action_id", sql, 
							action.getCompanyID(),
							action.getDescription(),
							action.getShortname(),
							action.getType(),
							action.getIsActive() ? 1 : 0);

						// set the new id to refresh the item
						action.setId(newID);
					}
				} else {
					String updateSql = "UPDATE rdir_action_tbl SET description = ?, shortname = ?, change_date = CURRENT_TIMESTAMP, action_type = ?, active = ? WHERE action_id = ? AND company_id = ?";
					update(logger, updateSql, action.getDescription(), action.getShortname(), action.getType(), action.getIsActive() ? 1 : 0, action.getId(), action.getCompanyID());
				}

				return action.getId();
			} catch (Exception e) {
				logger.error("Couldn't save EMM action: " + e.getMessage(), e);
				throw new RuntimeException(e);
			}
		}
	}
	
	@Override
	public boolean actionExists(final int actionID, final int companyID) {
		int count = selectInt(logger, "SELECT COUNT(action_id) FROM rdir_action_tbl WHERE action_id=? AND company_id=?", actionID, companyID);
		
		return count > 0;
	}
	
	@Override
	public EmmAction getEmmAction(int actionID, int companyID) {
		if (actionID == 0 || companyID == 0) {
			return null;
		} else {
			try {
				String sql = "SELECT action_id, company_id, description, shortname, action_type, active, creation_date, change_date FROM rdir_action_tbl WHERE action_id = ? AND company_id = ?";
				List<EmmAction> actions = select(logger, sql, new EmmAction_RowMapper(), actionID, companyID);
				if (actions != null && actions.size() != 0) {
					return actions.get(0);
				} else {
					return null;
				}
			} catch (Exception e) {
				return null;
			}
		}
	}

	@Override
	@DaoUpdateReturnValueCheck
	public boolean deleteEmmAction(int actionID, int companyID) {
		try {
			int touchedLines = update(logger, "UPDATE rdir_action_tbl SET deleted = 1 WHERE action_id = ? AND company_id = ?", actionID, companyID);
			return touchedLines > 0;
		} catch (Exception e) {
			return false;
		}
	}
	
	@Override
	@DaoUpdateReturnValueCheck
	public boolean deleteEmmActionReally(int actionID, int companyID) {
		try {
			int touchedLines = update(logger, "DELETE FROM rdir_action_tbl WHERE action_id = ? AND company_id = ?", actionID, companyID);
			return touchedLines > 0;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public List<EmmAction> getEmmActions(int companyID) {
		return getEmmActions(companyID, false);
	}

	@Override
	public List<EmmAction> getEmmActions(int companyID, boolean includeDeleted) {
		if (companyID == 0) {
			return null;
		} else {
			try {
				String sql = "SELECT action_id, company_id, description, shortname, action_type, active, creation_date, change_date FROM rdir_action_tbl WHERE company_id = ?" +
						(includeDeleted ? "" : " AND deleted = 0") + " ORDER BY shortname";
				return select(logger,  sql, new EmmAction_RowMapper(), companyID);
			} catch (Exception e) {
				return null;
			}
		}
	}

	/**
	 * Select EMM actions with at least the specified types of actionoperations
	 */
	@Override
	public List<EmmAction> getEmmActionsByOperationType(final int companyID, final boolean includeInactive,
			final ActionOperationType... actionTypes) {
		if (companyID == 0) {
			return null;
		} else {
			try {
				String sql = "SELECT action_id, company_id, description, shortname, action_type, active, creation_date, change_date FROM rdir_action_tbl WHERE company_id = ? AND deleted = 0";
				if(!includeInactive) {
					sql += " AND active = 1";
				}
				Object[] sqlParameters = new Object[actionTypes.length + 1];
				sqlParameters[0] = companyID;
				if (actionTypes != null) {
					for (int i = 0; i < actionTypes.length; i++) {
						ActionOperationType actionOperationType = actionTypes[i];
						sql += " AND EXISTS (SELECT 1 FROM actop_tbl WHERE rdir_action_tbl.action_id = actop_tbl.action_id AND actop_tbl.type = ?)";
						sqlParameters[i + 1] = actionOperationType.getName();
					}
				}
				sql += " ORDER BY shortname";
				return select(logger, sql, new EmmAction_RowMapper(), sqlParameters);
			} catch (Exception e) {
				return null;
			}
		}
	}

	@Override
	public List<EmmAction> getEmmActionsByName(int companyID, String shortName) {
		if (companyID == 0) {
			return null;
		} else {
			try {
				return select(logger, "SELECT action_id, company_id, description, shortname, action_type, active, creation_date, change_date FROM rdir_action_tbl WHERE company_id = ? AND deleted = 0 AND shortname = ?", new EmmAction_RowMapper(), companyID, shortName);
			} catch (Exception e) {
				return null;
			}
		}
	}

	@Override
	public String getEmmActionName(int actionId, int companyId) {
		if (actionId > 0 && companyId > 0) {
			String sql = "SELECT shortname FROM rdir_action_tbl WHERE action_id = ? AND company_id = ?";
			return selectObjectDefaultNull(logger, sql, (rs, index) -> rs.getString("shortname"), actionId, companyId);
		} else {
			return null;
		}
	}

	@Override
	public Map<Integer, String> getEmmActionNames(int companyID, List<Integer> actionIds) {
		Map<Integer, String> actionNames = new HashMap<>();
		if (companyID > 0 && CollectionUtils.isNotEmpty(actionIds)) {
			String sql = "SELECT action_id, shortname FROM rdir_action_tbl WHERE company_id = ? AND action_id IN (" +
					StringUtils.join(actionIds, ",")
					+ ")";
			query(logger, sql, rs -> actionNames.put(rs.getInt("action_id"), rs.getString("shortname")),  companyID);
		}
		return actionNames;
	}

	@Override
	public List<EmmAction> getEmmNotFormActions(int companyID) {
		return getEmmNotFormActions(companyID, true);
	}

	@Override
	public List<EmmAction> getEmmNotFormActions(int companyID, boolean includeInactive) {
		String sqlGetActions = "SELECT action_id, company_id, description, shortname, action_type, active, creation_date, change_date FROM rdir_action_tbl " +
				"WHERE company_id = ? AND action_type <> ? AND deleted = 0 " + (includeInactive ? "" : "AND active = 1 ") +
				"ORDER BY shortname";

		return select(logger, sqlGetActions, new EmmAction_RowMapper(), companyID, EmmAction.TYPE_FORM);
	}

	@Override
	public List<EmmAction> getEmmNotLinkActions(int companyID) {
		return getEmmNotLinkActions(companyID, true);
	}

	@Override
	public List<EmmAction> getEmmNotLinkActions(int companyID, boolean includeInactive) {
		String sqlGetActions = "SELECT action_id, company_id, description, shortname, action_type, active, creation_date, change_date FROM rdir_action_tbl " +
				"WHERE company_id = ? AND action_type <> ? AND deleted = 0 " + (includeInactive ? "" : "AND active = 1 ") +
				"ORDER BY shortname";

		return select(logger, sqlGetActions, new EmmAction_RowMapper(), companyID, EmmAction.TYPE_LINK);
	}

	@Override
	public Map<Integer, Integer> loadUsed(int companyID) {
		try {
			List<Map<String, Object>> result = select(logger,
				"SELECT r.action_id, COUNT(u.form_id) used"
				+ " FROM rdir_action_tbl r"
					+ " LEFT JOIN userform_tbl u ON ((u.startaction_id = r.action_id OR u.endaction_id = r.action_id) AND r.company_id = u.company_id)"
				+ " WHERE r.company_id = ?"
				+ " GROUP BY r.action_id",
				companyID);
			Map<Integer, Integer> used = new HashMap<>();
			for (Map<String, Object> row : result) {
				int action_id = ((Number) row.get("action_id")).intValue();
				int count = ((Number) row.get("used")).intValue();
				used.put(action_id, count);
			}
			return used;
		} catch (Exception e) {
			return new HashMap<>();
		}
	}

	@Override
	public List<String> getActionUserFormNames(int actionId, int companyId) {
		return select(logger, "SELECT formname FROM userform_tbl WHERE company_id = ? AND (startaction_id = ? OR endaction_id = ?) ORDER BY formname", StringRowMapper.INSTANCE,
				 companyId, actionId, actionId);
	}

	@Override
	public List<EmmAction> getActionList(int companyID, String sortBy, boolean order) {
		return getActionList(companyID, sortBy, order, null);
	}

	@Override
	public List<EmmAction> getActionList(int companyID, String sortBy, boolean order, Boolean activenessFilter) {
		String sortColumn;

		if (StringUtils.isEmpty(sortBy)) {
			sortBy = "shortname";
		}

		sortColumn = "r." + sortBy;
		if (VARCHAR_COLUMNS.contains(sortBy)) {
			sortColumn = "UPPER( " + sortColumn + " )";
		}

		String sqlStatement = "SELECT r.company_id, r.action_id, r.shortname, r.description, r.creation_date, r.change_date, r.active, count(u.form_id) used"
				+ " FROM rdir_action_tbl r"
					+ " LEFT JOIN userform_tbl u ON (u.startaction_id = r.action_id OR u.endaction_id = r.action_id)"
				+ " WHERE r.company_id = ? AND r.deleted = 0" + (activenessFilter == null ? "" : " AND r.active = " + BooleanUtils.toInteger(activenessFilter))
				+ " GROUP BY r.company_id, r.action_id, r.shortname, r.description, r.creation_date, r.change_date, r.active"
				+ " ORDER BY " + sortColumn + " " + (order ? "ASC" : "DESC") + ", r.action_id ASC";

		return select(logger, sqlStatement, (resultSet, i) -> {
			EmmAction newBean = new EmmActionImpl();
			newBean.setId(resultSet.getInt("action_id"));
			newBean.setCompanyID(resultSet.getInt("company_id"));
			newBean.setShortname(resultSet.getString("shortname"));
			newBean.setDescription(resultSet.getString("description"));
			newBean.setCreationDate(resultSet.getTimestamp("creation_date"));
			newBean.setChangeDate(resultSet.getTimestamp("change_date"));
			newBean.setIsActive(resultSet.getInt("active") == 1);
			if (resultSet.getInt("used") > 0) {
				newBean.setFormNameList(getActionUserFormNames(newBean.getId(), companyID));
			}
			return newBean;
		}, companyID);
	}

	@Override
	public Map<Integer, Boolean> getActivenessMap(Collection<Integer> actionIds, int companyId) {
		if (CollectionUtils.isEmpty(actionIds) || companyId <= 0) {
			return Collections.emptyMap();
		}

		String sqlGetActiveness = "SELECT action_id, active FROM rdir_action_tbl " +
				"WHERE company_id = ? AND action_id IN (" + StringUtils.join(actionIds, ',') + ")";

		Map<Integer, Boolean> activenessMap = new HashMap<>();
		query(logger, sqlGetActiveness, new ActivenessMapCallback(activenessMap), companyId);
		return activenessMap;
	}

	@Override
	public void setActiveness(Collection<Integer> actionIds, boolean active, int companyId) {
		if (CollectionUtils.isEmpty(actionIds) || companyId <= 0) {
			return;
		}

		String sqlSetActiveness = "UPDATE rdir_action_tbl SET active = ? " +
				"WHERE company_id = ? AND action_id IN (" + StringUtils.join(actionIds, ',') + ")";

		update(logger, sqlSetActiveness, BooleanUtils.toInteger(active), companyId);
	}

	@Override
	public List<EmmAction> getActionListBySendMailingId(int companyId, int mailingId) {
		String sqlGetActionsBySendMailingId = "SELECT ra.action_id, ra.company_id, ra.description, ra.shortname, "
				+ "ra.action_type, ra.active, ra.creation_date, ra.change_date "
				+ "FROM rdir_action_tbl ra JOIN actop_tbl at ON ra.action_id = at.action_id "
				+ "JOIN actop_send_mailing_tbl atsm ON atsm.action_operation_id = at.action_operation_id "
				+ "WHERE atsm.mailing_id = ? AND ra.company_id = ?";

		return select(logger, sqlGetActionsBySendMailingId, new EmmAction_RowMapper(), mailingId, companyId);
	}

	protected static class EmmAction_RowMapper implements RowMapper<EmmAction> {
		@Override
		public EmmAction mapRow(ResultSet resultSet, int row) throws SQLException {
			EmmAction readItem = new EmmActionImpl();

			readItem.setId(resultSet.getInt("action_id"));
			readItem.setCompanyID(resultSet.getInt("company_id"));
			readItem.setShortname(resultSet.getString("shortname"));
			readItem.setDescription(resultSet.getString("description"));
			readItem.setType(resultSet.getInt("action_type"));
			readItem.setIsActive(BooleanUtils.toBoolean(resultSet.getInt("active")));
			readItem.setCreationDate(resultSet.getTimestamp("creation_date"));
			readItem.setChangeDate(resultSet.getTimestamp("change_date"));

			return readItem;
		}
	}

	protected static class ActivenessMapCallback implements RowCallbackHandler {
		private Map<Integer, Boolean> map;

		public ActivenessMapCallback(Map<Integer, Boolean> map) {
			this.map = Objects.requireNonNull(map);
		}

		@Override
		public void processRow(ResultSet rs) throws SQLException {
			map.put(rs.getInt("action_id"), BooleanUtils.toBoolean(rs.getInt("active")));
		}
	}
}
