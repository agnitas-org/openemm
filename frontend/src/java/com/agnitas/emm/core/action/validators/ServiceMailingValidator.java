/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.action.validators;

import java.util.ArrayList;
import java.util.List;

import com.agnitas.util.AgnUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.action.operations.ActionOperationParameters;
import com.agnitas.emm.core.action.operations.ActionOperationServiceMailParameters;
import com.agnitas.emm.core.blacklist.dao.BlacklistDao;
import com.agnitas.messages.Message;
import com.agnitas.service.SimpleServiceResult;

@Component
public class ServiceMailingValidator implements ActionOperationValidator {

    private BlacklistDao blacklistService;

    public ServiceMailingValidator(BlacklistDao blacklistService) {
        this.blacklistService = blacklistService;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return ActionOperationServiceMailParameters.class.isAssignableFrom(clazz);
    }

    @Override
    public SimpleServiceResult validate(Admin admin, ActionOperationParameters target) {
        ActionOperationServiceMailParameters operation = (ActionOperationServiceMailParameters) target;
        List<Message> errors = new ArrayList<>();
        int companyId = admin.getCompanyID();

        validateToAddress(operation.getToAddress(), errors, companyId);
        validateFromAddress(operation.getFromAddress(), errors, companyId);
        validateReplyAddress(operation.getReplyAddress(), errors, companyId);
        validateSubject(operation.getSubjectLine(), errors);

        return errors.isEmpty() ? new SimpleServiceResult(true) : new SimpleServiceResult(false, errors);
    }

    private void validateFromAddress(String fromAddress, List<Message> errors, int companyId) {
        if (StringUtils.isBlank(fromAddress)) {
            errors.add(Message.of("error.mailing.sender_adress"));
        } else if (!isRequestParamOrCustomerData(fromAddress)) {
            checkEmailValid(fromAddress, errors, "error.action.blacklistedFrom", companyId);
        }
    }

    private void validateToAddress(String toAddress, List<Message> errors, int companyId) {
        List<String> emails = AgnUtils.splitAndTrimList(toAddress);
        emails.removeIf(StringUtils::isBlank);
        if (StringUtils.isBlank(toAddress) || CollectionUtils.isEmpty(emails)) {
            errors.add(Message.of("error.mailing.recipient_adress"));
        } else if (!isRequestParamOrCustomerData(toAddress)) {
            for (String email : emails) {
                checkEmailValid(email, errors, "error.action.blacklistedTo", companyId);
            }
        }
    }

    private void validateReplyAddress(String replyAddress, List<Message> errors, int companyId) {
        if (StringUtils.isBlank(replyAddress)) {
            errors.add(Message.of("error.mailing.reply_adress"));
        } else if (!isRequestParamOrCustomerData(replyAddress)) {
            checkEmailValid(replyAddress, errors, "error.action.blacklistedReply", companyId);
        }
    }

    private void validateSubject(String subject, List<Message> errors) {
        if (StringUtils.isBlank(subject) || StringUtils.length(subject) < 3) {
            errors.add(Message.of("error.subjectToShort"));
        } else if (StringUtils.length(subject) > 200) {
            errors.add(Message.of("error.subjectToLong"));
        }
    }

    private void checkEmailValid(String email, List<Message> errors, String blacklistedErrorMsg, int companyId) {
        email = AgnUtils.normalizeEmail(email);
        if (!AgnUtils.isEmailValid(email)) {
            errors.add(Message.of("error.invalid.email", email));
        } else if (blacklistService.blacklistCheckCompanyOnly(email, companyId)) {
            errors.add(Message.of(blacklistedErrorMsg, email));
        }
    }

    private boolean isRequestParamOrCustomerData(String fromAddress) {
        return fromAddress.toLowerCase().startsWith("$requestparameters.")
                || fromAddress.toLowerCase().startsWith("$customerdata.");
    }
}
