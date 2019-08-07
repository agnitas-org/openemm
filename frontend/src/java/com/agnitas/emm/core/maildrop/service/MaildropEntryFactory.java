/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.maildrop.service;

import java.util.Date;

import com.agnitas.beans.MaildropEntry;
import com.agnitas.beans.impl.MaildropEntryImpl;
import com.agnitas.emm.core.maildrop.MaildropStatus;

public final class MaildropEntryFactory {

	public static MaildropEntry newAdminMaildrop(final int mailingID, final int companyID, final int adminTestTargetID) {
		return newTestAdminMaildrop(mailingID, companyID, MaildropStatus.ADMIN.getCode(), adminTestTargetID);
	}

	public static MaildropEntry newTestMaildrop(final int mailingID, final int companyID, final int adminTestTargetID) {
		return newTestAdminMaildrop(mailingID, companyID, MaildropStatus.TEST.getCode(), adminTestTargetID);
	}
	
	private static MaildropEntry newTestAdminMaildrop(final int mailingID, final int companyID, final char status, final int adminTestTargetID) {
		final Date now = new Date();
		
		final MaildropEntry entry = new MaildropEntryImpl();
		entry.setAdminTestTargetID(adminTestTargetID);
		entry.setBlocksize(0);
		entry.setCompanyID(companyID);
		entry.setGenChangeDate(now);
		entry.setGenDate(now);
		entry.setGenStatus(1);
		entry.setId(0);
		entry.setMailGenerationOptimization(null);
		entry.setMailingID(mailingID);
		entry.setMaxRecipients(0);
		entry.setSendDate(now);
		entry.setStatus(status);
		entry.setStepping(0);

		return entry;
	}
}
