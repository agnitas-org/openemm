/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.action.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;

import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.emm.core.action.dao.ActionOperationUpdateCustomerDao;
import com.agnitas.emm.core.action.operations.ActionOperationUpdateCustomerParameters;

public class ActionOperationUpdateCustomerDaoImpl extends AbstractActionOperationDaoImpl<ActionOperationUpdateCustomerParameters> implements ActionOperationUpdateCustomerDao {
	/** The logger. */
	private static final transient Logger logger = LogManager.getLogger(ActionOperationUpdateCustomerDaoImpl.class);

	@Override
	protected void processGetOperation(ActionOperationUpdateCustomerParameters operation) {
		Map<String, Object> row = selectSingleRow(logger, "SELECT column_name, update_type, update_value, trackpoint_id FROM actop_update_customer_tbl WHERE action_operation_id = ?", operation.getId());
		operation.setColumnName((String) row.get("column_name"));
		operation.setUpdateType(((Number) row.get("update_type")).intValue());
		operation.setUpdateValue((String) row.get("update_value"));
		Number tpId = (Number) row.get("trackpoint_id");
		if (tpId != null && tpId.intValue() != -1 && selectInt(logger, "SELECT COUNT(*) FROM trackpoint_def_tbl WHERE trackpoint_id = ?", tpId.intValue()) == 0) {
			// Also pass 'predefined' revenue trackpoint referenced id (-1)
			tpId = null;
			operation.setUseTrack(false);
			processUpdateOperation(operation);
		}
		operation.setTrackingPointId((tpId != null) ? tpId.intValue() : 0);
		operation.setUseTrack(tpId != null);
	}

	@Override
	@DaoUpdateReturnValueCheck
	protected void processSaveOperation(ActionOperationUpdateCustomerParameters operation) {
		update(logger, "INSERT INTO actop_update_customer_tbl (action_operation_id, column_name, update_type, update_value, trackpoint_id) VALUES (?, ?, ?, ?, ?)",
			operation.getId(),
			operation.getColumnName(),
			operation.getUpdateType(),
			operation.getUpdateValue(),
			operation.isUseTrack() ? operation.getTrackingPointId() : null);
	}

	@Override
	@DaoUpdateReturnValueCheck
	protected void processUpdateOperation(ActionOperationUpdateCustomerParameters operation) {
		update(logger, "UPDATE actop_update_customer_tbl SET column_name = ?, update_type = ?, update_value = ?, trackpoint_id = ? where action_operation_id = ?",
			operation.getColumnName(),
			operation.getUpdateType(),
			operation.getUpdateValue(),
			operation.isUseTrack() ? operation.getTrackingPointId() : null,
			operation.getId());
	}

	@Override
	@DaoUpdateReturnValueCheck
	protected void processDeleteOperation(ActionOperationUpdateCustomerParameters operation) {
		update(logger, "DELETE FROM actop_update_customer_tbl WHERE action_operation_id = ?", operation.getId());
	}

	@Override
	public List<ActionOperationUpdateCustomerParameters> getByTrackingPointId(int trackingPointId) {
		return select(logger,
			"SELECT ao.action_operation_id, company_id, column_name, update_type, update_value, trackpoint_id, company_id FROM actop_update_customer_tbl uu INNER JOIN actop_tbl ao ON uu.action_operation_id = ao.action_operation_id WHERE trackpoint_id = ?",
			new RowMapper<ActionOperationUpdateCustomerParameters>() {
				@Override
				public ActionOperationUpdateCustomerParameters mapRow(ResultSet row, int rowNum) throws SQLException {
					ActionOperationUpdateCustomerParameters operation = new ActionOperationUpdateCustomerParameters();
					operation.setId(row.getInt("action_operation_id"));
					operation.setCompanyId(row.getInt("company_id"));
					operation.setColumnName(row.getString("column_name"));
					operation.setUpdateType(row.getInt("update_type"));
					operation.setUpdateValue(row.getString("update_value"));
					Number tpId = row.getInt("trackpoint_id");
					operation.setTrackingPointId(tpId == null ? 0 : tpId.intValue());
					operation.setUseTrack(tpId != null);
					return operation;
				}
			}, trackingPointId);
	}
}
