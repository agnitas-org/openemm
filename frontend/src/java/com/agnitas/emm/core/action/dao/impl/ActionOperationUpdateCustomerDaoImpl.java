/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.action.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;

import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.emm.core.action.dao.ActionOperationUpdateCustomerDao;
import com.agnitas.emm.core.action.operations.ActionOperationUpdateCustomerParameters;

public class ActionOperationUpdateCustomerDaoImpl extends AbstractActionOperationDaoImpl<ActionOperationUpdateCustomerParameters> implements ActionOperationUpdateCustomerDao {
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(ActionOperationUpdateCustomerDaoImpl.class);
	
	@Override
	protected void processGetOperation(ActionOperationUpdateCustomerParameters operation) {
		Map<String, Object> row = selectSingleRow(logger, "select column_name, update_type, update_value, trackpoint_id from actop_update_customer_tbl where action_operation_id=?", operation.getId());
		operation.setColumnName((String) row.get("column_name"));
		operation.setUpdateType(((Number) row.get("update_type")).intValue());
		operation.setUpdateValue((String) row.get("update_value"));
		Number tpId = (Number) row.get("trackpoint_id");
		if (tpId != null && tpId.intValue() != -1 // Also pass 'predefined' revenue trackpoint referenced id (-1) 
				&& selectInt(logger, "SELECT COUNT(*) FROM trackpoint_def_tbl WHERE trackpoint_id = ?", Integer.class, tpId) == 0) {
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
		update(logger, "insert into actop_update_customer_tbl (action_operation_id, column_name, update_type, update_value, trackpoint_id) values (?,?,?,?,?)", 
				operation.getId(), 
				operation.getColumnName(),
				operation.getUpdateType(),
				operation.getUpdateValue(),
				operation.isUseTrack() ? operation.getTrackingPointId() : null);
	}

	@Override
	@DaoUpdateReturnValueCheck
	protected void processUpdateOperation(ActionOperationUpdateCustomerParameters operation) {
		update(logger, "update actop_update_customer_tbl set column_name=?, update_type=?, update_value=?, trackpoint_id=? where action_operation_id=?", 
				operation.getColumnName(),
				operation.getUpdateType(),
				operation.getUpdateValue(),
				operation.isUseTrack() ? operation.getTrackingPointId() : null,
				operation.getId());
	}

	@Override
	@DaoUpdateReturnValueCheck
	protected void processDeleteOperation(ActionOperationUpdateCustomerParameters operation) {
		update(logger, "delete from actop_update_customer_tbl where action_operation_id=?", operation.getId());
	}

	@Override
	public List<ActionOperationUpdateCustomerParameters> getByTrackingPointId(int trackingPointId) {
		return select(logger, "select ao.action_operation_id, company_id, column_name, update_type, update_value, trackpoint_id, company_id from actop_update_customer_tbl uu inner join actop_tbl ao on uu.action_operation_id = ao.action_operation_id where trackpoint_id=?",
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
		        },
		        trackingPointId);
	}
}
