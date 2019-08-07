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

import org.agnitas.beans.Mailing;
import org.agnitas.dao.MailingDao;
import org.agnitas.dao.UserStatus;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.agnitas.emm.core.action.operations.AbstractActionOperationParameters;
import com.agnitas.emm.core.action.operations.ActionOperationSendMailingParameters;
import com.agnitas.emm.core.action.service.EmmActionOperation;
import com.agnitas.emm.core.action.service.EmmActionOperationErrors;
import com.agnitas.emm.core.mailing.service.MailgunOptions;
import com.agnitas.emm.core.mailing.service.SendActionbasedMailingException;
import com.agnitas.emm.core.mailing.service.SendActionbasedMailingService;
import com.agnitas.emm.core.mailing.service.impl.UnableToSendActionbasedMailingException;

public class ActionOperationSendMailingImpl implements EmmActionOperation{

	/**
	 * The logger.
	 */
	private static final Logger logger = Logger.getLogger(ActionOperationSendMailingImpl.class);

	/**
	 * DAO for accessing mailing data.
	 */
	private MailingDao mailingDao;

	@Autowired
	private SendActionbasedMailingService sendActionbasedMailingService;
	/**
	 * Create list containing all user status, that are allowed to send mailings to.
	 *
	 * @return list of user status
	 */
	private static List<Integer> createUserStatusList() {
		final List<Integer> list = new Vector<>();

		list.add(UserStatus.Active.getStatusCode());
		list.add(UserStatus.WaitForConfirm.getStatusCode());

		return list;
	}

	@Override
	public boolean execute(final AbstractActionOperationParameters operation, final Map<String, Object> params, final EmmActionOperationErrors errors) {

		final ActionOperationSendMailingParameters op =(ActionOperationSendMailingParameters) operation;
		final int companyID = op.getCompanyId();
		final int mailingID = op.getMailingID();

        int customerID=0;
        Integer tmpNum=null;
        Mailing mailing=null;
        boolean exitValue=false;

        if(params.get("customerID")==null) {
            return false;
        }
        tmpNum=(Integer)params.get("customerID");
        customerID= tmpNum;

        mailing=mailingDao.getMailing(mailingID, companyID);
        if(mailing!=null) {
        	final List<Integer> userStatusList = createUserStatusList();
			try {
				final MailgunOptions mailgunOptions = new MailgunOptions();
				if (userStatusList != null) {
					mailgunOptions.withAllowedUserStatus(userStatusList);
				}
				try {
					if (StringUtils.isNotBlank(op.getBcc())) {
						mailgunOptions.withBccEmails(op.getBcc());
					}

					sendActionbasedMailingService.sendActionbasedMailing(mailing.getCompanyID(), mailing.getId(), customerID, op.getDelayMinutes(), mailgunOptions);
				} catch(final Exception e) {
					logger.error("Cannot fire campaign-/event-mail", e);

					throw new UnableToSendActionbasedMailingException(mailing.getId(), customerID, e);
				}

				if (logger.isInfoEnabled()) {
					logger.info("executeOperation: Mailing " + mailingID + " to " + customerID + " sent");
				}
				exitValue = true;
			} catch (final SendActionbasedMailingException e) {
				logger.error("executeOperation: Mailing "+mailingID+" to "+customerID+" failed");
			}
        }
        return exitValue;
	}

	public void setMailingDao(final MailingDao mailingDao) {
		this.mailingDao = mailingDao;
	}

}
