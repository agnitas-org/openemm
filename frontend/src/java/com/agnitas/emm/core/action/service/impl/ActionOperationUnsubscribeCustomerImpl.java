/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.action.service.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.agnitas.beans.BindingEntry;
import org.agnitas.beans.BindingEntry.UserType;
import org.agnitas.dao.UserStatus;
import org.agnitas.dao.exception.UnknownUserStatusException;
import org.agnitas.emm.core.recipient.service.RecipientService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.dao.MailingDao;
import com.agnitas.emm.core.action.operations.AbstractActionOperationParameters;
import com.agnitas.emm.core.action.operations.ActionOperationType;
import com.agnitas.emm.core.action.operations.ActionOperationUnsubscribeCustomerParameters;
import com.agnitas.emm.core.action.service.EmmActionOperation;
import com.agnitas.emm.core.action.service.EmmActionOperationErrors;
import com.agnitas.emm.core.action.service.EmmActionOperationErrors.ErrorCode;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;

import jakarta.servlet.http.HttpServletRequest;

public class ActionOperationUnsubscribeCustomerImpl implements EmmActionOperation {

	/** The logger. */
    private static final Logger LOGGER = LogManager.getLogger(ActionOperationUnsubscribeCustomerImpl.class);

    private RecipientService recipientService;
    private MailingDao mailingDao;

    @Override
    public boolean execute(AbstractActionOperationParameters operation, Map<String, Object> params, EmmActionOperationErrors errors) throws Exception {
        // GWUA-4782: Expand unsubscribe action
        return newExecute(operation, params, errors);
    }

    @Override
    public ActionOperationType processedType() {
        return ActionOperationType.UNSUBSCRIBE_CUSTOMER;
    }

    private boolean newExecute(AbstractActionOperationParameters operation, Map<String, Object> params, EmmActionOperationErrors errors) throws UnknownUserStatusException {
        ActionOperationUnsubscribeCustomerParameters op = (ActionOperationUnsubscribeCustomerParameters) operation;
        int companyId = op.getCompanyId();
        int mailingId = getMailingIdFromParams(params);
        int recipientId = getRecipientIdFromParams(params);

        validateIds(recipientId, mailingId, errors);

        return errors.isEmpty() && unsubscribeRecipientFromMailingLists(
                getMailinglistIdsToUnsubscribe(op, companyId, recipientId, mailingId),
                errors, mailingId, recipientId, companyId, params);
    }

    private boolean unsubscribeRecipientFromMailingLists(Set<Integer> mailinglistIds, EmmActionOperationErrors errors,
                                                         int exitMailingId, int recipientId, int companyId,
                                                         Map<String, Object> params) throws UnknownUserStatusException {
        List<BindingEntry> bindings = getBindingsToUnsubscribe(mailinglistIds, companyId, recipientId);
        if (bindings.isEmpty()) {
            return true;
        }
        for (BindingEntry binding : bindings) {
            if (!unsubscribe(binding, exitMailingId, params)) {
                return false;
            }
        }
        return tryUpdateBindings(bindings, companyId, errors);
    }

    private boolean unsubscribe(BindingEntry binding, int exitMailingId, Map<String, Object> params) throws UnknownUserStatusException {
        switch (UserStatus.getUserStatusByID(binding.getUserStatus())) {
            case Active:
            case Bounce:
            case Suspend:
                if (!UserType.TestVIP.getTypeCode().equals(binding.getUserType())
                        && !UserType.WorldVIP.getTypeCode().equals(binding.getUserType())) {
                    markBindingAsOptOut(binding, exitMailingId, params);
                }
                return true;
            case AdminOut:
            case UserOut:
                // next Event-Mailing goes to a user with status 4
                params.put("__agn_USER_STATUS", "4");
                return true;
            default:
                return false;
        }
    }

    private boolean tryUpdateBindings(List<BindingEntry> bindings, int companyId, EmmActionOperationErrors errors) {
        try {
            recipientService.updateBindings(bindings, companyId);
            return true;
        } catch (Exception e) {
            LOGGER.error("Can't update bindings. " + e.getMessage(), e);
            errors.addErrorCode(ErrorCode.GENERAL_ERROR);
            return false;
        }
    }

    private void validateIds(int recipientId, int mailingId, EmmActionOperationErrors errors) {
        if (recipientId <= 0) {
            errors.addErrorCode(ErrorCode.MISSING_CUSTOMER_ID);
        }
        if (mailingId <= 0) {
            errors.addErrorCode(ErrorCode.MISSING_MAILING_ID);
        }
    }

    private void markBindingAsOptOut(BindingEntry binding, int exitMailingId, Map<String, Object> params) {
        binding.setUserStatus(UserStatus.UserOut.getStatusCode());
        binding.setUserRemark("User-Opt-Out: " + getRemoteAddrFromParams(params));
        binding.setExitMailingID(exitMailingId);
        // next Event-Mailing goes to a user with status 4
        params.put("__agn_USER_STATUS", "4");
    }

    private Set<Integer> getMailinglistIdsToUnsubscribe(ActionOperationUnsubscribeCustomerParameters op,
                                                        int companyId, int recipientId, int mailingId) {
        Set<Integer> mailinglistIds = op.isAllMailinglistsSelected()
                ? new HashSet<>(recipientService.getMailinglistBindings(companyId, recipientId).keySet())
                : op.getMailinglistIds();

        mailinglistIds.add(mailingDao.getMailinglistId(mailingId, companyId));
        return mailinglistIds;
    }
    
    private List<BindingEntry> getBindingsToUnsubscribe(Set<Integer> mailinglistIds, int companyId, int recipientId) {
        return recipientService.getMailinglistBindings(companyId, recipientId).entrySet().stream()
                .filter(entry -> mailinglistIds.contains(entry.getKey()))
                .flatMap(entry -> entry.getValue().entrySet().stream())
                .filter(entry -> MediaTypes.EMAIL.getMediaCode() == entry.getKey())
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }

    private int getRecipientIdFromParams(Map<String, Object> params) {
        Object customerIdParam = params.get("customerID");
        return customerIdParam != null ? (int) customerIdParam : 0;
    }

    private int getMailingIdFromParams(Map<String, Object> params) {
        Object mailingIdParam = params.get("mailingID");
        return mailingIdParam != null ? (int) mailingIdParam : 0;
    }

    private String getRemoteAddrFromParams(Map<String, Object> params) {
        try {
            HttpServletRequest request = (HttpServletRequest) params.get("_request");
            return request.getRemoteAddr();
        } catch (Exception e) {
            return "IP unknown";
        }
    }

    @Required
    public void setMailingDao(MailingDao mailingDao) {
        this.mailingDao = mailingDao;
    }

    @Required
    public void setRecipientService(RecipientService recipientService) {
        this.recipientService = recipientService;
    }
}
