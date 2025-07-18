/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.action.dao.impl;

import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.emm.core.action.operations.ActionOperationIdentifyCustomerParameters;

import java.util.Map;

public class ActionOperationIdentifyCustomerDaoImpl extends AbstractActionOperationDaoImpl<ActionOperationIdentifyCustomerParameters> {

	@Override
	protected void processGetOperation(ActionOperationIdentifyCustomerParameters operation) {
		Map<String, Object> row = selectSingleRow("select key_column, pass_column from actop_identify_customer_tbl where action_operation_id=?", operation.getId());
		operation.setKeyColumn((String) row.get("key_column"));
		operation.setPassColumn((String) row.get("pass_column"));
	}

	@Override
	@DaoUpdateReturnValueCheck
	protected void processSaveOperation(ActionOperationIdentifyCustomerParameters operation) {
		update("insert into actop_identify_customer_tbl (action_operation_id, key_column, pass_column) values (?,?,?)",
				operation.getId(),
				operation.getKeyColumn(),
				operation.getPassColumn());
	}

	@Override
	@DaoUpdateReturnValueCheck
	protected void processUpdateOperation(ActionOperationIdentifyCustomerParameters operation) {
		update("update actop_identify_customer_tbl set key_column=?, pass_column=? where action_operation_id=?",
				operation.getKeyColumn(),
				operation.getPassColumn(),
				operation.getId());
	}

	@Override
	@DaoUpdateReturnValueCheck
	protected void processDeleteOperation(ActionOperationIdentifyCustomerParameters operation) {
		update("delete from actop_identify_customer_tbl where action_operation_id=?", operation.getId());
	}

}
