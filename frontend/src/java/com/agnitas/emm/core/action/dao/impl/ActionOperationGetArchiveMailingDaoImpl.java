/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.action.dao.impl;

import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.emm.core.action.operations.ActionOperationGetArchiveMailingParameters;

import java.util.Map;

public class ActionOperationGetArchiveMailingDaoImpl extends AbstractActionOperationDaoImpl<ActionOperationGetArchiveMailingParameters> {

	@Override
	protected void processGetOperation(ActionOperationGetArchiveMailingParameters operation) {
		Map<String, Object> row = selectSingleRow("select expire_day, expire_month, expire_year from actop_get_archive_mailing_tbl where action_operation_id=?", operation.getId());
		operation.setExpireDay(((Number) row.get("expire_day")).intValue());
		operation.setExpireMonth(((Number) row.get("expire_month")).intValue());
		operation.setExpireYear(((Number) row.get("expire_year")).intValue());
	}

	@Override
	@DaoUpdateReturnValueCheck
	protected void processSaveOperation(ActionOperationGetArchiveMailingParameters operation) {
		update("insert into actop_get_archive_mailing_tbl (action_operation_id, expire_day, expire_month, expire_year) values (?,?,?,?)",
				operation.getId(),
				operation.getExpireDay(),
				operation.getExpireMonth(),
				operation.getExpireYear());
	}

	@Override
	@DaoUpdateReturnValueCheck
	protected void processUpdateOperation(ActionOperationGetArchiveMailingParameters operation) {
		update("update actop_get_archive_mailing_tbl set expire_day=?, expire_month=?, expire_year=? where action_operation_id=?",
				operation.getExpireDay(),
				operation.getExpireMonth(),
				operation.getExpireYear(),
				operation.getId());
	}

	@Override
	@DaoUpdateReturnValueCheck
	protected void processDeleteOperation(ActionOperationGetArchiveMailingParameters operation) {
		update("delete from actop_get_archive_mailing_tbl where action_operation_id=?", operation.getId());
	}

}
