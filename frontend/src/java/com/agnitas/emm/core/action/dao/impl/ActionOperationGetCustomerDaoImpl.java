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
import com.agnitas.emm.core.action.operations.ActionOperationGetCustomerParameters;
import org.apache.commons.lang3.BooleanUtils;

public class ActionOperationGetCustomerDaoImpl extends AbstractActionOperationDaoImpl<ActionOperationGetCustomerParameters> {

	@Override
	protected void processGetOperation(ActionOperationGetCustomerParameters operation) {
		Map<String, Object> row = selectObject("SELECT load_always FROM actop_get_customer_tbl WHERE action_operation_id = ?", (resultSet, rowIndex) -> {
            Map<String, Object> map = new HashMap<>(1);
            map.put("load_always", resultSet.getBoolean("load_always"));
            return map;
        }, operation.getId());
		operation.setLoadAlways((Boolean) row.get("load_always"));
	}

	@Override
	@DaoUpdateReturnValueCheck
	protected void processSaveOperation(ActionOperationGetCustomerParameters operation) {
		update("INSERT INTO actop_get_customer_tbl (action_operation_id, load_always) VALUES (?,?)",
				operation.getId(), BooleanUtils.toInteger(operation.isLoadAlways()));
	}

	@Override
	@DaoUpdateReturnValueCheck
	protected void processUpdateOperation(ActionOperationGetCustomerParameters operation) {
		update("UPDATE actop_get_customer_tbl SET load_always = ? WHERE action_operation_id = ?",
				BooleanUtils.toInteger(operation.isLoadAlways()), operation.getId());
	}

	@Override
	@DaoUpdateReturnValueCheck
	protected void processDeleteOperation(ActionOperationGetCustomerParameters operation) {
		update("DELETE FROM actop_get_customer_tbl WHERE action_operation_id = ?", operation.getId());
	}

}
