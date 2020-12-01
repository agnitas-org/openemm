/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.action.validators;

import org.agnitas.util.AgnUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.agnitas.beans.ComAdmin;
import com.agnitas.emm.core.action.operations.ActionOperationParameters;
import com.agnitas.emm.core.action.operations.ActionOperationServiceMailParameters;
import com.agnitas.emm.core.blacklist.dao.ComBlacklistDao;
import com.agnitas.messages.Message;
import com.agnitas.service.SimpleServiceResult;

@Component
public class ServiceMailingValidator implements ActionOperationValidator {

    private ComBlacklistDao blacklistService;

    public ServiceMailingValidator(ComBlacklistDao blacklistService) {
        this.blacklistService = blacklistService;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return ActionOperationServiceMailParameters.class.isAssignableFrom(clazz);
    }

    @Override
    public SimpleServiceResult validate(ComAdmin admin, ActionOperationParameters target) {
        ActionOperationServiceMailParameters operation = (ActionOperationServiceMailParameters) target;

        String toAddress = operation.getToAddress();
        if (StringUtils.isNotBlank(toAddress)) {
            for (String exactAddress : toAddress.split(";|,| ")) {
                exactAddress = AgnUtils.normalizeEmail(exactAddress);
                if (StringUtils.isNotBlank(exactAddress) && blacklistService.blacklistCheckCompanyOnly(exactAddress, admin.getCompanyID())) {
                    return new SimpleServiceResult(false, Message.of("error.action.blacklistedTo", exactAddress));
                }
            }
        }

        String fromAddress = operation.getFromAddress();
        if (StringUtils.isNotBlank(fromAddress)) {
            if (!fromAddress.toLowerCase().startsWith("$requestparameters.")
                    && !fromAddress.toLowerCase().startsWith("$customerdata.")) {
                fromAddress = AgnUtils.normalizeEmail(fromAddress);
                if (StringUtils.isNotBlank(fromAddress) && blacklistService.blacklistCheckCompanyOnly(fromAddress, admin.getCompanyID())) {
                    return new SimpleServiceResult(false, Message.of("error.action.blacklistedFrom", fromAddress));
                }
            }
        }

        String replyAddress = operation.getReplyAddress();
        if (StringUtils.isNotBlank(replyAddress)) {
            if (!replyAddress.toLowerCase().startsWith("$requestparameters.")
                    && !replyAddress.toLowerCase().startsWith("$customerdata.")) {
                replyAddress = AgnUtils.normalizeEmail(replyAddress);
                if (StringUtils.isNotBlank(replyAddress) && blacklistService.blacklistCheckCompanyOnly(replyAddress, admin.getCompanyID())) {
                    return new SimpleServiceResult(false, Message.of("error.action.blacklistedReply", replyAddress));
                }
            }
        }

        return new SimpleServiceResult(true);
    }
}
