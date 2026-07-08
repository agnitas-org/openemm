/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.unsubscribe;

import java.util.Objects;

import com.agnitas.beans.BindingEntry;
import com.agnitas.emm.common.UserStatus;

import com.agnitas.dao.BindingEntryDao;
import com.agnitas.dao.MailingDao;
import com.agnitas.emm.core.commons.uid.ExtensibleUID;

final class Unsubscription {
	/** DAO accessing mailing data. */
	private final MailingDao mailingDao;
	
	/** DAO accessing subscriber bindings. */
	private final BindingEntryDao bindingEntryDao;
	
	public Unsubscription(final MailingDao mailingDao, final BindingEntryDao bindingEntryDao) {
		this.mailingDao = Objects.requireNonNull(mailingDao, "Mailing DAO is null");
		this.bindingEntryDao = Objects.requireNonNull(bindingEntryDao, "Binding entry DAO is null");
	}

	public void performUnsubscription(final ExtensibleUID uid, final String remark) {
		final int mailinglistID = mailingDao.getMailinglistId(uid.getMailingID(), uid.getCompanyID());
		if (mailinglistID > 0) {
			final BindingEntry entry = bindingEntryDao.get(uid.getCustomerID(), uid.getCompanyID(), mailinglistID, 0);

			if (entry.getUserStatus() != UserStatus.Blacklisted.getStatusCode()) {
				entry.setUserStatus(UserStatus.UserOut.getStatusCode());
				entry.setExitMailingID(uid.getMailingID());

				entry.setUserRemark(remark);
				bindingEntryDao.updateBinding(entry, uid.getCompanyID());
			}
		}
	}
}
