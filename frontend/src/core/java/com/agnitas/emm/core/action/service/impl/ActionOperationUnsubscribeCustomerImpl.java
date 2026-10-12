/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.action.service.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.agnitas.beans.BindingEntry;
import com.agnitas.beans.BindingEntry.UserType;
import com.agnitas.dao.MailingDao;
import com.agnitas.emm.common.UserStatus;
import com.agnitas.emm.core.action.operations.ActionOperationType;
import com.agnitas.emm.core.action.operations.ActionOperationUnsubscribeCustomerParameters;
import com.agnitas.emm.core.action.service.EmmActionOperation;
import com.agnitas.emm.core.action.service.EmmActionOperationErrors;
import com.agnitas.emm.core.action.service.EmmActionOperationErrors.ErrorCode;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.emm.core.recipient.service.RecipientService;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ActionOperationUnsubscribeCustomerImpl implements EmmActionOperation<ActionOperationUnsubscribeCustomerParameters> {

    private static final Logger LOGGER = LogManager.getLogger(ActionOperationUnsubscribeCustomerImpl.class);

    private final RecipientService recipientService;
    private final MailingDao mailingDao;

    public ActionOperationUnsubscribeCustomerImpl(RecipientService recipientService, MailingDao mailingDao) {
        this.recipientService = recipientService;
        this.mailingDao = mailingDao;
    }

    @Override
    public boolean execute(
            ActionOperationUnsubscribeCustomerParameters operation,
            Map<String, Object> params,
            EmmActionOperationErrors errors
    ) {
        int companyId = operation.getCompanyId();

        if (!params.containsKey("customerID")) {
            errors.addErrorCode(ErrorCode.MISSING_CUSTOMER_ID);
        }

        Integer recipientId = null;
        try {
            recipientId = parseIntegerParam(params.get("customerID"));
        } catch (Exception e) {
            errors.addErrorCode(ErrorCode.INVALID_CUSTOMER_ID);
        }

        if (recipientId != null && recipientId <= 0) {
            errors.addErrorCode(ErrorCode.INVALID_CUSTOMER_ID);
        }

        if (!params.containsKey("mailingID")) {
            errors.addErrorCode(ErrorCode.MISSING_MAILING_ID);
        }

        Integer mailingId = null;
        try {
            mailingId = parseIntegerParam(params.get("mailingID"));
        } catch (Exception e) {
            errors.addErrorCode(ErrorCode.INVALID_MAILING_ID);
        }

        if (mailingId != null && mailingId <= 0) {
            errors.addErrorCode(ErrorCode.INVALID_MAILING_ID);
        }

        if (!errors.isEmpty()) {
            return false;
        }

        return unsubscribeRecipientFromMailingLists(
                getMailinglistIdsToUnsubscribe(operation, companyId, recipientId, mailingId),
                errors, mailingId, recipientId, companyId, params
        );
    }

    @Override
    public ActionOperationType processedType() {
        return ActionOperationType.UNSUBSCRIBE_CUSTOMER;
    }

    private boolean unsubscribeRecipientFromMailingLists(Set<Integer> mailinglistIds, EmmActionOperationErrors errors,
                                                         Integer exitMailingId, Integer recipientId, int companyId,
                                                         Map<String, Object> params) {
        List<BindingEntry> bindings = getBindingsToUnsubscribe(mailinglistIds, companyId, recipientId);
        if (bindings.isEmpty()) {
            return true;
        }
        for (BindingEntry binding : bindings) {
            if (!unsubscribe(binding, exitMailingId, params, errors)) {
                return false;
            }
        }
        return tryUpdateBindings(bindings, companyId, errors);
    }

    private boolean unsubscribe(BindingEntry binding, int exitMailingId, Map<String, Object> params, EmmActionOperationErrors errors) {
        switch (UserStatus.getByCode(binding.getUserStatus())) {
            case Active, Bounce, Suspend:
                if (!UserType.TestVIP.getTypeCode().equals(binding.getUserType())
                        && !UserType.WorldVIP.getTypeCode().equals(binding.getUserType())) {
                    markBindingAsOptOut(binding, exitMailingId, params);
                }
                return true;
            case AdminOut, UserOut:
                // next Event-Mailing goes to a user with status 4
                params.put("__agn_USER_STATUS", "4");
                return true;
            default:
                errors.addErrorCode(ErrorCode.RECIPIENT_NOT_ACTIVE);
                return false;
        }
    }

    private boolean tryUpdateBindings(List<BindingEntry> bindings, int companyId, EmmActionOperationErrors errors) {
        try {
            recipientService.updateBindings(bindings, companyId);
            return true;
        } catch (Exception e) {
            LOGGER.error("Can't update bindings. {}", e.getMessage(), e);
            errors.addErrorCode(ErrorCode.GENERAL_ERROR);
            return false;
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

    private String getRemoteAddrFromParams(Map<String, Object> params) {
        try {
            HttpServletRequest request = (HttpServletRequest) params.get("_request");
            return request.getRemoteAddr();
        } catch (Exception e) {
            return "IP unknown";
        }
    }

}
