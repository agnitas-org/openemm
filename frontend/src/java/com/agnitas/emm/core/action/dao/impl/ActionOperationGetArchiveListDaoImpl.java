/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.action.dao.impl;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.emm.core.action.operations.ActionOperationGetArchiveListParameters;

public class ActionOperationGetArchiveListDaoImpl extends AbstractActionOperationDaoImpl<ActionOperationGetArchiveListParameters> {
	/** The logger. */
	private static final transient Logger logger = LogManager.getLogger(ActionOperationGetArchiveListDaoImpl.class);
	
	@Override
	protected void processGetOperation(ActionOperationGetArchiveListParameters operation) {
		Map<String, Object> row = selectSingleRow(logger, "select campaign_id from actop_get_archive_list_tbl where action_operation_id=?", operation.getId());
		operation.setCampaignID(((Number) row.get("campaign_id")).intValue());
	}

	@Override
	@DaoUpdateReturnValueCheck
	protected void processSaveOperation(ActionOperationGetArchiveListParameters operation) {
		update(logger, "insert into actop_get_archive_list_tbl (action_operation_id, campaign_id) values (?,?)",
				operation.getId(),
				operation.getCampaignID());
	}

	@Override
	@DaoUpdateReturnValueCheck
	protected void processUpdateOperation(ActionOperationGetArchiveListParameters operation) {
		update(logger, "update actop_get_archive_list_tbl set campaign_id=? where action_operation_id=?",
				operation.getCampaignID(),
				operation.getId());
	}

	@Override
	@DaoUpdateReturnValueCheck
	protected void processDeleteOperation(ActionOperationGetArchiveListParameters operation) {
		update(logger, "delete from actop_get_archive_list_tbl where action_operation_id=?", operation.getId());
	}
}
