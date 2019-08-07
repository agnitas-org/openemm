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
import com.agnitas.emm.core.action.operations.ActionOperationServiceMailParameters;

public class ActionOperationServiceMailDaoImpl extends AbstractActionOperationDaoImpl<ActionOperationServiceMailParameters> {
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(ActionOperationServiceMailDaoImpl.class);
	
	@Override
	protected void processGetOperation(ActionOperationServiceMailParameters operation) {
		Map<String, Object> row = selectSingleRow(logger, "SELECT text_mail, subject_line, to_addr, from_address, reply_address, mailtype, html_mail FROM actop_service_mail_tbl WHERE action_operation_id = ?", operation.getId());
		operation.setTextMail((String) row.get("text_mail"));
		operation.setSubjectLine((String) row.get("subject_line"));
		operation.setToAddress((String) row.get("to_addr"));
		operation.setFromAddress((String) row.get("from_address"));
		operation.setReplyAddress((String) row.get("reply_address"));
		operation.setMailtype(((Number) row.get("mailtype")).intValue());
		operation.setHtmlMail((String) row.get("html_mail"));
	}

	@Override
	@DaoUpdateReturnValueCheck
	protected void processSaveOperation(ActionOperationServiceMailParameters operation) {
		update(logger, "INSERT INTO actop_service_mail_tbl (action_operation_id, text_mail, subject_line, to_addr, from_address, reply_address, mailtype, html_mail) VALUES (?, ?, ?, ?, ?, ?, ?, ?)", 
				operation.getId(), 
				operation.getTextMail(),
				operation.getSubjectLine(),
				operation.getToAddress(),
				operation.getFromAddress(),
				operation.getReplyAddress(),
				operation.getMailtype(),
				operation.getHtmlMail());
	}

	@Override
	@DaoUpdateReturnValueCheck
	protected void processUpdateOperation(ActionOperationServiceMailParameters operation) {
		update(logger, "UPDATE actop_service_mail_tbl SET text_mail = ?, subject_line = ?, to_addr = ?, from_address = ?, reply_address = ?, mailtype = ?, html_mail = ? WHERE action_operation_id = ?", 
				operation.getTextMail(),
				operation.getSubjectLine(),
				operation.getToAddress(),
				operation.getFromAddress(),
				operation.getReplyAddress(),
				operation.getMailtype(),
				operation.getHtmlMail(),
				operation.getId());
	}

	@Override
	@DaoUpdateReturnValueCheck
	protected void processDeleteOperation(ActionOperationServiceMailParameters operation) {
		update(logger, "DELETE FROM actop_service_mail_tbl WHERE action_operation_id = ?", operation.getId());
	}
}
