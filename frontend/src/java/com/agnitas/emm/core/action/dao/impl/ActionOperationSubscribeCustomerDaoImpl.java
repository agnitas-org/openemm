/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.action.dao.impl;

import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.emm.core.action.operations.ActionOperationSubscribeCustomerParameters;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class ActionOperationSubscribeCustomerDaoImpl extends AbstractActionOperationDaoImpl<ActionOperationSubscribeCustomerParameters> {

	@Override
	protected void processGetOperation(ActionOperationSubscribeCustomerParameters operation) {
		Map<String, Object> row = selectObject("select double_check, key_column, double_opt_in from actop_subscribe_customer_tbl where action_operation_id=?", new RowMapper<Map<String, Object>>() {
			@Override
			public Map<String, Object> mapRow(ResultSet resultSet, int rowIndex) throws SQLException {
				Map<String, Object> map = new HashMap<>(3);
				map.put("double_check", resultSet.getBoolean("double_check"));
				map.put("key_column", resultSet.getString("key_column"));
				map.put("double_opt_in", resultSet.getBoolean("double_opt_in"));
				return map;
			}
		}, operation.getId());
		operation.setDoubleCheck((Boolean) row.get("double_check"));
		operation.setKeyColumn((String) row.get("key_column"));
		operation.setDoubleOptIn((Boolean) row.get("double_opt_in"));
	}

	@Override
	@DaoUpdateReturnValueCheck
	protected void processSaveOperation(ActionOperationSubscribeCustomerParameters operation) {
		update("insert into actop_subscribe_customer_tbl (action_operation_id, double_check, key_column, double_opt_in) values (?,?,?,?)",
				operation.getId(),
				operation.isDoubleCheck(),
				operation.getKeyColumn(),
				operation.isDoubleOptIn());
	}

	@Override
	@DaoUpdateReturnValueCheck
	protected void processUpdateOperation(ActionOperationSubscribeCustomerParameters operation) {
		update("update actop_subscribe_customer_tbl set double_check=?, key_column=?, double_opt_in=? where action_operation_id=?",
				operation.isDoubleCheck(),
				operation.getKeyColumn(),
				operation.isDoubleOptIn(),
				operation.getId());
	}

	@Override
	@DaoUpdateReturnValueCheck
	protected void processDeleteOperation(ActionOperationSubscribeCustomerParameters operation) {
		update("delete from actop_subscribe_customer_tbl where action_operation_id=?", operation.getId());
	}

}
