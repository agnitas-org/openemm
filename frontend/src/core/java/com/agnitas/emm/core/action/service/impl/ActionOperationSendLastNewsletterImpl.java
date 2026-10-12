/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.action.service.impl;

import java.util.Date;
import java.util.Map;
import java.util.Objects;

import com.agnitas.beans.MaildropEntry;
import com.agnitas.beans.impl.MaildropEntryImpl;
import com.agnitas.dao.MailingDao;
import com.agnitas.emm.core.action.operations.AbstractActionOperationParameters;
import com.agnitas.emm.core.action.operations.ActionOperationSendLastNewsletterParameters;
import com.agnitas.emm.core.action.operations.ActionOperationType;
import com.agnitas.emm.core.action.service.EmmActionOperation;
import com.agnitas.emm.core.action.service.EmmActionOperationErrors;
import com.agnitas.emm.core.action.service.EmmActionOperationErrors.ErrorCode;
import com.agnitas.emm.core.commons.uid.ExtensibleUID;
import com.agnitas.emm.core.maildrop.MaildropStatus;
import com.agnitas.emm.core.maildrop.service.MaildropService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class ActionOperationSendLastNewsletterImpl implements EmmActionOperation<ActionOperationSendLastNewsletterParameters> {

	private static final Logger LOGGER = LogManager.getLogger(ActionOperationSendLastNewsletterImpl.class);
	
	private final MailingDao mailingDao;
	private final MaildropService maildropService;
	
	public ActionOperationSendLastNewsletterImpl(MailingDao mailingDao, MaildropService maildropService) {
		this.mailingDao = Objects.requireNonNull(mailingDao, "mailing DAO");
		this.maildropService = Objects.requireNonNull(maildropService, "maildrop service");
	}
	
	@Override
	public boolean execute(
			ActionOperationSendLastNewsletterParameters operation,
			Map<String, Object> params,
			EmmActionOperationErrors errors
	) {
		final ExtensibleUID uid = (ExtensibleUID) params.get("_uid");

		if (uid == null) {
			LOGGER.error("SendLastNewsletter: Missing agn UID (action ID {})", operation.getActionId());
			errors.addErrorCode(ErrorCode.MISSING_UID);
			return false;
		}

        int mailinglistId = mailingDao.getMailinglistId(uid.getMailingID(), uid.getCompanyID());
		if (mailinglistId <= 0) {
			LOGGER.warn("SendLastNewsletter: Mailinglist ID is 0. (action ID {})", operation.getActionId());
			errors.addErrorCode(ErrorCode.INVALID_MAILINGLIST_ID);
			return false;
		}

		return sendLastNewsletter(operation, mailinglistId, uid.getCustomerID());
	}

	private boolean sendLastNewsletter(AbstractActionOperationParameters operation, int mailinglistId, int customerId) {
		final int lastNewsletterMailingID = mailingDao.findLastNewsletter(customerId, operation.getCompanyId(), mailinglistId);
		
		if (lastNewsletterMailingID == 0) {
			LOGGER.warn("SendLastNewsletter: No previous newsletter on mailinglist ID {}. (action ID {}, operation ID {})",
					mailinglistId, operation.getActionId(), operation.getId());
			return true;
		}

		final MaildropEntry drop = new MaildropEntryImpl();

		drop.setStatus(MaildropStatus.ACTION_BASED.getCode());
		drop.setSendDate(new Date());
		drop.setGenDate(new Date());
		drop.setGenStatus(1);
		drop.setGenChangeDate(new Date());
		drop.setMailingID(lastNewsletterMailingID);
		drop.setCompanyID(operation.getCompanyId());

		maildropService.saveMaildropEntry(drop);
		return true;
	}

	@Override
	public ActionOperationType processedType() {
		return ActionOperationType.SEND_LAST_NEWSLETTER;
	}
}
