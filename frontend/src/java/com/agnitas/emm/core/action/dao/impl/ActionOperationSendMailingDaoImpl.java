/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.action.dao.impl;

import java.util.Map;

import org.apache.log4j.Logger;

import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.emm.core.action.operations.ActionOperationSendMailingParameters;

public class ActionOperationSendMailingDaoImpl extends AbstractActionOperationDaoImpl<ActionOperationSendMailingParameters> {
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(ActionOperationSendMailingDaoImpl.class);
	
	@Override
	protected void processGetOperation(ActionOperationSendMailingParameters operation) {
		Map<String, Object> row = selectSingleRow(logger, "select mailing_id, delay_minutes, bcc from actop_send_mailing_tbl where action_operation_id=?", operation.getId());
		operation.setMailingID(((Number) row.get("mailing_id")).intValue());
		operation.setDelayMinutes(((Number) row.get("delay_minutes")).intValue());
		operation.setBcc((String) row.get("bcc"));
	}

	@Override
	@DaoUpdateReturnValueCheck
	protected void processSaveOperation(ActionOperationSendMailingParameters operation) {
		update(logger, "insert into actop_send_mailing_tbl (action_operation_id, mailing_id, delay_minutes, bcc) values (?,?,?,?)",
				operation.getId(),
				operation.getMailingID(),
				operation.getDelayMinutes(),
                operation.getBcc());
	}

	@Override
	@DaoUpdateReturnValueCheck
	protected void processUpdateOperation(ActionOperationSendMailingParameters operation) {
		update(logger, "update actop_send_mailing_tbl set mailing_id=?, delay_minutes=?, bcc=? where action_operation_id=?",
				operation.getMailingID(),
				operation.getDelayMinutes(),
				operation.getBcc(),
				operation.getId());
	}

	@Override
	@DaoUpdateReturnValueCheck
	protected void processDeleteOperation(ActionOperationSendMailingParameters operation) {
		update(logger, "delete from actop_send_mailing_tbl where action_operation_id=?", operation.getId());
	}

}
