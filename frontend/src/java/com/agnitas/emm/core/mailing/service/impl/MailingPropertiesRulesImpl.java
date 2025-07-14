/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailing.service.impl;

import java.util.Objects;

import com.agnitas.emm.common.MailingStatus;
import com.agnitas.beans.Admin;
import com.agnitas.beans.Mailing;
import com.agnitas.dao.MailingDao;
import com.agnitas.emm.common.MailingType;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.maildrop.service.MaildropService;
import com.agnitas.emm.core.mailing.service.MailingPropertiesRules;

public final class MailingPropertiesRulesImpl implements MailingPropertiesRules {
	
	private MaildropService maildropService;
	private MailingDao mailingDao;
	
	@Override
	public final boolean isMailingContentEditable(final int mailingID, final Admin admin) {
		final Mailing mailing = mailingDao.getMailing(mailingID, admin.getCompanyID());
		
		return isMailingContentEditable(mailing, admin);
	}

	@Override
	public final boolean isMailingContentEditable(final Mailing mailing, final Admin admin) {
		return !mailingIsWorldSentOrActive(mailing)
				|| (mailing.getMailingType() == MailingType.NORMAL && admin.permissionAllowed(Permission.MAILING_CONTENT_CHANGE_ALWAYS));
	}
	
	@Override
	public final boolean mailingIsWorldSentOrActive(final Mailing mailing) {
		if (mailing.getMailingType() == MailingType.INTERVAL) {
			final MailingStatus workStatus = mailingDao.getStatus(mailing.getCompanyID(), mailing.getId());
			
			return workStatus == MailingStatus.ACTIVE;
		}

		return maildropService.isActiveMailing(mailing.getId(), mailing.getCompanyID());
	}
	@Override
	public final boolean mailingIsWorldSentOrActive(final int mailingId, final int companyId) {
		final Mailing mailing = this.mailingDao.getMailing(mailingId, companyId);

		return mailingIsWorldSentOrActive(mailing);
	}

	public final void setMaildropService(final MaildropService service) {
		this.maildropService = Objects.requireNonNull(service, "MaildropService is null");
	}
	
	public final void setMailingDao(final MailingDao dao) {
		this.mailingDao = Objects.requireNonNull(dao, "MailingDao is null");
	}
}
