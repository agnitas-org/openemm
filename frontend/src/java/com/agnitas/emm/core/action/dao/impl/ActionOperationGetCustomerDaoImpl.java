/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.action.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;

import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.emm.core.action.operations.ActionOperationGetCustomerParameters;

public class ActionOperationGetCustomerDaoImpl extends AbstractActionOperationDaoImpl<ActionOperationGetCustomerParameters> {
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(ActionOperationGetCustomerDaoImpl.class);
	
	@Override
	protected void processGetOperation(ActionOperationGetCustomerParameters operation) {
		Map<String, Object> row = selectObject(logger, "select load_always from actop_get_customer_tbl where action_operation_id=?", new RowMapper<Map<String, Object>>() {
			@Override
			public Map<String, Object> mapRow(ResultSet resultSet, int row) throws SQLException {
				Map<String, Object> map = new HashMap<>(1);
				map.put("load_always", resultSet.getBoolean("load_always"));
				return map;
			}
		}, operation.getId());
		operation.setLoadAlways((Boolean) row.get("load_always"));
	}

	@Override
	@DaoUpdateReturnValueCheck
	protected void processSaveOperation(ActionOperationGetCustomerParameters operation) {
		update(logger, "insert into actop_get_customer_tbl (action_operation_id, load_always) values (?,?)", 
				operation.getId(), 
				operation.isLoadAlways());
	}

	@Override
	@DaoUpdateReturnValueCheck
	protected void processUpdateOperation(ActionOperationGetCustomerParameters operation) {
		update(logger, "update actop_get_customer_tbl set load_always=? where action_operation_id=?", 
				operation.isLoadAlways(),
				operation.getId());
	}

	@Override
	@DaoUpdateReturnValueCheck
	protected void processDeleteOperation(ActionOperationGetCustomerParameters operation) {
		update(logger, "delete from actop_get_customer_tbl where action_operation_id=?", operation.getId());
	}

}
