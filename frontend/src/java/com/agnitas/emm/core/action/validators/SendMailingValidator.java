/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.action.validators;

import java.util.Locale;

import org.agnitas.util.AgnUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.agnitas.beans.ComAdmin;
import com.agnitas.emm.core.action.operations.ActionOperationParameters;
import com.agnitas.emm.core.action.operations.ActionOperationSendMailingParameters;
import com.agnitas.emm.core.blacklist.dao.ComBlacklistDao;
import com.agnitas.messages.I18nString;
import com.agnitas.messages.Message;
import com.agnitas.service.SimpleServiceResult;

@Component
public class SendMailingValidator implements ActionOperationValidator {

    private ComBlacklistDao blacklistService;

    public SendMailingValidator(ComBlacklistDao blacklistService) {
        this.blacklistService = blacklistService;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return ActionOperationSendMailingParameters.class.isAssignableFrom(clazz);
    }

    @Override
    public SimpleServiceResult validate(ComAdmin admin, ActionOperationParameters target) {
        String bcc = ((ActionOperationSendMailingParameters) target).getBcc();

        Locale locale = admin.getLocale();
        if (StringUtils.isNotBlank(bcc) && !AgnUtils.isEmailsListValid(bcc)) {
            return new SimpleServiceResult(false,
                    Message.exact(I18nString.getLocaleString("action.address.bcc", locale) + ": " + I18nString.getLocaleString("error.email.invalid", locale)));
        }

        if (StringUtils.isNotBlank(bcc) && blacklistService.blacklistCheckCompanyOnly(bcc, admin.getCompanyID())) {
            return new SimpleServiceResult(false, Message.of("error.action.blacklistedBcc", bcc));
        }

        return new SimpleServiceResult(true);
    }
}
