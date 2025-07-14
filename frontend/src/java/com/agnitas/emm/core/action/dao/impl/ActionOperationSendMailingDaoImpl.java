/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.action.dao.impl;

import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.emm.core.action.operations.ActionOperationSendMailingParameters;

import java.util.Map;

public class ActionOperationSendMailingDaoImpl extends AbstractActionOperationDaoImpl<ActionOperationSendMailingParameters> {

    @Override
    protected void processGetOperation(ActionOperationSendMailingParameters operation) {
        Map<String, Object> row = selectSingleRow("SELECT mailing_id, delay_minutes, bcc, for_active_recipients FROM actop_send_mailing_tbl WHERE action_operation_id = ?", operation.getId());
        operation.setMailingID(((Number) row.get("mailing_id")).intValue());
        operation.setDelayMinutes(((Number) row.get("delay_minutes")).intValue());
        operation.setBcc((String) row.get("bcc"));
        operation.setUserStatusesOption(((Number) row.get("for_active_recipients")).intValue());
    }

    @Override
    @DaoUpdateReturnValueCheck
    protected void processSaveOperation(ActionOperationSendMailingParameters operation) {
        update("INSERT INTO actop_send_mailing_tbl (action_operation_id, mailing_id, delay_minutes, bcc, for_active_recipients) VALUES (?,?,?,?,?)",
                operation.getId(),
                operation.getMailingID(),
                operation.getDelayMinutes(),
                operation.getBcc(),
                operation.getUserStatusesOption());
    }

    @Override
    @DaoUpdateReturnValueCheck
    protected void processUpdateOperation(ActionOperationSendMailingParameters operation) {
        update("UPDATE actop_send_mailing_tbl SET mailing_id=?, delay_minutes=?, bcc=?, for_active_recipients=? WHERE action_operation_id = ?",
                operation.getMailingID(),
                operation.getDelayMinutes(),
                operation.getBcc(),
                operation.getUserStatusesOption(),
                operation.getId());
    }

    @Override
    @DaoUpdateReturnValueCheck
    protected void processDeleteOperation(ActionOperationSendMailingParameters operation) {
        update("DELETE FROM actop_send_mailing_tbl WHERE action_operation_id = ?", operation.getId());
    }
}
