/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.action.dao.impl;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.emm.core.action.operations.ActionOperationUnsubscribeCustomerParameters;

public class ActionOperationUnsubscribeCustomerDaoImpl
        extends AbstractActionOperationDaoImpl<ActionOperationUnsubscribeCustomerParameters> {

    private static final Logger LOGGER = Logger.getLogger(ActionOperationUnsubscribeCustomerDaoImpl.class);

    @Override
    protected void processGetOperation(ActionOperationUnsubscribeCustomerParameters operation) {
        operation.setAllMailinglistsSelected(isAllMailinglistsSelected(operation.getId()));
        operation.setMailinglistIds(getMailinglistIds(operation.getId()));
    }

    @Override
    @DaoUpdateReturnValueCheck
    protected void processSaveOperation(ActionOperationUnsubscribeCustomerParameters operation) {
        update(LOGGER, "INSERT INTO actop_unsubscribe_customer_tbl " +
                        "(action_operation_id, all_mailinglists_selected) VALUES (?,?)",
                operation.getId(), operation.isAdditionalMailinglists() && operation.isAllMailinglistsSelected());
        if (operation.isAdditionalMailinglists() && !operation.isAllMailinglistsSelected()) {
            saveSelectedMailinglistsToOperation(operation);
        }
    }

    @Override
    @DaoUpdateReturnValueCheck
    protected void processUpdateOperation(ActionOperationUnsubscribeCustomerParameters operation) {
        processDeleteOperation(operation);
        processSaveOperation(operation);
    }

    @Override
    @DaoUpdateReturnValueCheck
    protected void processDeleteOperation(ActionOperationUnsubscribeCustomerParameters operation) {
        update(LOGGER, "DELETE FROM actop_unsubscribe_customer_tbl WHERE action_operation_id = ?", operation.getId());
    }

    private void saveSelectedMailinglistsToOperation(ActionOperationUnsubscribeCustomerParameters operation) {
        List<Object[]> params = operation.getMailinglistIds().stream().map(mailinglistId -> new Object[]{
                operation.getId(),
                mailinglistId
        }).collect(Collectors.toList());
        batchupdate(LOGGER, "INSERT INTO actop_unsubscribe_mlist_tbl " +
                "(action_operation_id, mailinglist_id) VALUES (?, ?)", params);
    }

    private boolean isAllMailinglistsSelected(int operationId) {
        return selectInt(LOGGER,
                "SELECT all_mailinglists_selected FROM actop_unsubscribe_customer_tbl WHERE action_operation_id = ?", operationId) > 0;
    }

    private Set<Integer> getMailinglistIds(int operationId) {
        return select(LOGGER,
                "SELECT mailinglist_id FROM actop_unsubscribe_mlist_tbl " +
                        "WHERE action_operation_id = ?", operationId).stream()
                .map(row -> ((Number) row.get("mailinglist_id")).intValue())
                .collect(Collectors.toSet());
    }
}
