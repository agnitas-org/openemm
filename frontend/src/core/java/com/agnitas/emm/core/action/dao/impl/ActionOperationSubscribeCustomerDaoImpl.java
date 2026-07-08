/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.action.dao.impl;

import java.util.HashMap;
import java.util.Map;

import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.emm.core.action.operations.ActionOperationSubscribeCustomerParameters;

public class ActionOperationSubscribeCustomerDaoImpl extends AbstractActionOperationDaoImpl<ActionOperationSubscribeCustomerParameters> {

	@Override
	protected void processGetOperation(ActionOperationSubscribeCustomerParameters operation) {
		Map<String, Object> row = selectObject("SELECT double_check, key_column, double_opt_in FROM actop_subscribe_customer_tbl WHERE action_operation_id = ?", (resultSet, rowIndex) -> {
            Map<String, Object> map = new HashMap<>(3);
            map.put("double_check", resultSet.getBoolean("double_check"));
            map.put("key_column", resultSet.getString("key_column"));
            map.put("double_opt_in", resultSet.getBoolean("double_opt_in"));
            return map;
        }, operation.getId());
		operation.setDoubleCheck((Boolean) row.get("double_check"));
		operation.setKeyColumn((String) row.get("key_column"));
		operation.setDoubleOptIn((Boolean) row.get("double_opt_in"));
	}

	@Override
	@DaoUpdateReturnValueCheck
	protected void processSaveOperation(ActionOperationSubscribeCustomerParameters operation) {
		update("INSERT INTO actop_subscribe_customer_tbl (action_operation_id, double_check, key_column, double_opt_in) VALUES (?,?,?,?)",
				operation.getId(),
				operation.isDoubleCheck() ? 1 : 0,
				operation.getKeyColumn(),
				operation.isDoubleOptIn() ? 1 : 0
		);
	}

	@Override
	@DaoUpdateReturnValueCheck
	protected void processUpdateOperation(ActionOperationSubscribeCustomerParameters operation) {
		update("UPDATE actop_subscribe_customer_tbl SET double_check = ?, key_column = ?, double_opt_in = ? WHERE action_operation_id = ?",
				operation.isDoubleCheck() ? 1 : 0,
				operation.getKeyColumn(),
				operation.isDoubleOptIn() ? 1 : 0,
				operation.getId()
		);
	}

	@Override
	@DaoUpdateReturnValueCheck
	protected void processDeleteOperation(ActionOperationSubscribeCustomerParameters operation) {
		update("DELETE FROM actop_subscribe_customer_tbl WHERE action_operation_id = ?", operation.getId());
	}

}
