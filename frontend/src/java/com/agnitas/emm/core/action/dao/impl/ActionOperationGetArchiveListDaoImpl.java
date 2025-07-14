/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.action.dao.impl;

import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.emm.core.action.operations.ActionOperationGetArchiveListParameters;

import java.util.Map;

public class ActionOperationGetArchiveListDaoImpl extends AbstractActionOperationDaoImpl<ActionOperationGetArchiveListParameters> {

	@Override
	protected void processGetOperation(ActionOperationGetArchiveListParameters operation) {
		Map<String, Object> row = selectSingleRow("SELECT campaign_id, limit_type, limit_value FROM actop_get_archive_list_tbl WHERE action_operation_id = ?", operation.getId());
		operation.setCampaignID(((Number) row.get("campaign_id")).intValue());

		Number limitType = (Number) row.get("limit_type");
		Number limitValue = (Number) row.get("limit_value");

		if (limitType != null && limitValue != null) {
			operation.setLimitType(limitType.intValue());
			operation.setLimitValue(limitValue.intValue());
		}
	}

	@Override
	@DaoUpdateReturnValueCheck
	protected void processSaveOperation(ActionOperationGetArchiveListParameters operation) {
		update(
				"INSERT INTO actop_get_archive_list_tbl (action_operation_id, campaign_id, limit_type, limit_value) VALUES (?, ?, ?, ?)",
				operation.getId(),
				operation.getCampaignID(),
				operation.getLimitType(),
				operation.getLimitValue()
		);
	}

	@Override
	@DaoUpdateReturnValueCheck
	protected void processUpdateOperation(ActionOperationGetArchiveListParameters operation) {
		update(
				"UPDATE actop_get_archive_list_tbl SET campaign_id = ?, limit_type = ?, limit_value = ? WHERE action_operation_id = ?",
				operation.getCampaignID(),
				operation.getLimitType(),
				operation.getLimitValue(),
				operation.getId()
		);
	}

	@Override
	@DaoUpdateReturnValueCheck
	protected void processDeleteOperation(ActionOperationGetArchiveListParameters operation) {
		update("DELETE FROM actop_get_archive_list_tbl WHERE action_operation_id = ?", operation.getId());
	}
}
