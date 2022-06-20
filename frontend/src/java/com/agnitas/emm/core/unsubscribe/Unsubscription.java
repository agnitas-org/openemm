/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.unsubscribe;

import java.util.Objects;

import org.agnitas.beans.BindingEntry;
import org.agnitas.dao.UserStatus;

import com.agnitas.dao.ComBindingEntryDao;
import com.agnitas.dao.ComMailingDao;
import com.agnitas.emm.core.commons.uid.ComExtensibleUID;

final class Unsubscription {
	/** DAO accessing mailing data. */
	private final ComMailingDao mailingDao;
	
	/** DAO accessing subscriber bindings. */
	private final ComBindingEntryDao bindingEntryDao;
	
	public Unsubscription(final ComMailingDao mailingDao, final ComBindingEntryDao bindingEntryDao) {
		this.mailingDao = Objects.requireNonNull(mailingDao, "Mailing DAO is null");
		this.bindingEntryDao = Objects.requireNonNull(bindingEntryDao, "Binding entry DAO is null");
	}

	public final void performUnsubscription(final ComExtensibleUID uid, final String remark) {
		final int mailinglistID = mailingDao.getMailinglistId(uid.getMailingID(), uid.getCompanyID());
		if (mailinglistID > 0) {
			final BindingEntry entry = bindingEntryDao.get(uid.getCustomerID(), uid.getCompanyID(), mailinglistID, 0);
			entry.setUserStatus(UserStatus.UserOut.getStatusCode());
			entry.setExitMailingID(uid.getMailingID());
	
			entry.setUserRemark(remark);
			bindingEntryDao.updateBinding(entry, uid.getCompanyID());
		}
	}
}
