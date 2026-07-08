/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.action.service.impl;

import java.util.Map;

import com.agnitas.beans.IntEnum;
import com.agnitas.emm.core.action.bean.ActionSendMailingToUserStatus;
import com.agnitas.emm.common.UserStatus;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.agnitas.dao.MailingDao;
import com.agnitas.emm.core.action.operations.AbstractActionOperationParameters;
import com.agnitas.emm.core.action.operations.ActionOperationSendMailingParameters;
import com.agnitas.emm.core.action.operations.ActionOperationType;
import com.agnitas.emm.core.action.service.EmmActionOperation;
import com.agnitas.emm.core.action.service.EmmActionOperationErrors;
import com.agnitas.emm.core.action.service.EmmActionOperationErrors.ErrorCode;
import com.agnitas.emm.core.mailing.service.MailgunOptions;
import com.agnitas.emm.core.mailing.service.SendActionbasedMailingException;
import com.agnitas.emm.core.mailing.service.SendActionbasedMailingService;
import com.agnitas.emm.core.mailing.service.impl.UnableToSendActionbasedMailingException;

public class ActionOperationSendMailingImpl implements EmmActionOperation {

    private static final Logger logger = LogManager.getLogger(ActionOperationSendMailingImpl.class);

    private MailingDao mailingDao;
    private SendActionbasedMailingService sendActionbasedMailingService;

    public void setMailingDao(final MailingDao mailingDao) {
        this.mailingDao = mailingDao;
    }

    public void setSendActionbasedMailingService(final SendActionbasedMailingService sendActionbasedMailingService) {
        this.sendActionbasedMailingService = sendActionbasedMailingService;
    }

    @Override
    public boolean execute(final AbstractActionOperationParameters operation, final Map<String, Object> params, final EmmActionOperationErrors errors) {
        final ActionOperationSendMailingParameters operationSendMailingParams = (ActionOperationSendMailingParameters) operation;
        final int companyID = operationSendMailingParams.getCompanyId();
        final int mailingID = operationSendMailingParams.getMailingID();

        if (params.get("customerID") == null) {
            errors.addErrorCode(ErrorCode.MISSING_CUSTOMER_ID);
            return false;
        }

        final int customerID = (Integer) params.get("customerID");
        if (customerID == 0) {
            errors.addErrorCode(ErrorCode.INVALID_CUSTOMER_ID);
            return false;
        }

        if (!mailingDao.exist(mailingID, companyID)) {
            errors.addErrorCode(ErrorCode.MAILING_NOT_FOUND);
            return false;
        }

        try {
            final MailgunOptions mailgunOptions = new MailgunOptions();

            UserStatus[] statuses = IntEnum.fromId(ActionSendMailingToUserStatus.class, operationSendMailingParams.getUserStatusesOption())
                    .getStatuses();

            mailgunOptions.withAllowedUserStatus(statuses);

            try {
                if (StringUtils.isNotBlank(operationSendMailingParams.getBcc())) {
                    mailgunOptions.withBccEmails(operationSendMailingParams.getBcc());
                }

                sendActionbasedMailingService.sendActionbasedMailing(
                        companyID,
                        mailingID,
                        customerID,
                        operationSendMailingParams.getDelayMinutes(),
                        mailgunOptions
                );
            } catch (final Exception e) {
                logger.error("Cannot fire campaign-/event-mail", e);
                throw new UnableToSendActionbasedMailingException(mailingID, customerID, e);
            }

            if (logger.isInfoEnabled()) {
                logger.info("executeOperation: Mailing {} to {} sent", mailingID, customerID);
            }
            return true;
        } catch (final SendActionbasedMailingException e) {
            logger.error("executeOperation: Mailing {} to {} failed", mailingID, customerID);
            return false;
        }
    }

    @Override
    public ActionOperationType processedType() {
        return ActionOperationType.SEND_MAILING;
    }
}
