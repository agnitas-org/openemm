/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.action.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.agnitas.dao.UserStatus;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.dao.ComMailingDao;
import com.agnitas.emm.core.action.operations.AbstractActionOperationParameters;
import com.agnitas.emm.core.action.operations.ActionOperationSendMailingParameters;
import com.agnitas.emm.core.action.operations.ActionOperationType;
import com.agnitas.emm.core.action.service.EmmActionOperation;
import com.agnitas.emm.core.action.service.EmmActionOperationErrors;
import com.agnitas.emm.core.mailing.service.MailgunOptions;
import com.agnitas.emm.core.mailing.service.SendActionbasedMailingException;
import com.agnitas.emm.core.mailing.service.SendActionbasedMailingService;
import com.agnitas.emm.core.mailing.service.impl.UnableToSendActionbasedMailingException;

public class ActionOperationSendMailingImpl implements EmmActionOperation {
	/**
	 * The logger.
	 */
	private static final Logger logger = Logger.getLogger(ActionOperationSendMailingImpl.class);

	/**
	 * DAO for accessing mailing data.
	 */
	private ComMailingDao mailingDao;

	/**
	 * Service for sending event based mailings
	 */
	private SendActionbasedMailingService sendActionbasedMailingService;

	@Required
	public void setMailingDao(final ComMailingDao mailingDao) {
		this.mailingDao = mailingDao;
	}

	@Required
	public void setSendActionbasedMailingService(final SendActionbasedMailingService sendActionbasedMailingService) {
		this.sendActionbasedMailingService = sendActionbasedMailingService;
	}

	@Override
	public boolean execute(final AbstractActionOperationParameters operation, final Map<String, Object> params, final EmmActionOperationErrors errors) {
		final ActionOperationSendMailingParameters actionOperationSendMailingParameters = (ActionOperationSendMailingParameters) operation;
		final int companyID = actionOperationSendMailingParameters.getCompanyId();
		final int mailingID = actionOperationSendMailingParameters.getMailingID();

		if (params.get("customerID") == null) {
			return false;
		} else {
			final int customerID = (Integer) params.get("customerID");
			if (customerID == 0) {
				return false;
			} else {
				if (mailingDao.exist(mailingID, companyID)) {
					try {
						final MailgunOptions mailgunOptions = new MailgunOptions();
						final List<Integer> userStatusList = new Vector<>();
						userStatusList.add(UserStatus.Active.getStatusCode());
						userStatusList.add(UserStatus.WaitForConfirm.getStatusCode());
						mailgunOptions.withAllowedUserStatus(userStatusList);
						try {
							if (StringUtils.isNotBlank(actionOperationSendMailingParameters.getBcc())) {
								mailgunOptions.withBccEmails(actionOperationSendMailingParameters.getBcc());
							}

							sendActionbasedMailingService.sendActionbasedMailing(companyID, mailingID, customerID, actionOperationSendMailingParameters.getDelayMinutes(),
									mailgunOptions);
						} catch (final Exception e) {
							logger.error("Cannot fire campaign-/event-mail", e);
							throw new UnableToSendActionbasedMailingException(mailingID, customerID, e);
						}

						if (logger.isInfoEnabled()) {
							logger.info("executeOperation: Mailing " + mailingID + " to " + customerID + " sent");
						}
						return true;
					} catch (final SendActionbasedMailingException e) {
						logger.error("executeOperation: Mailing " + mailingID + " to " + customerID + " failed");
						return false;
					}
				} else {
					return false;
				}
			}
		}
	}

    @Override
    public ActionOperationType processedType() {
        return ActionOperationType.SEND_MAILING;
    }
}
