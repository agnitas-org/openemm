/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.service;

import java.util.List;

import com.agnitas.beans.MailingSendOptions;
import com.agnitas.messages.Message;
import org.agnitas.emm.core.useractivitylog.UserAction;
import org.agnitas.emm.core.velocity.VelocityCheck;

public interface ComMailingSendService {
	enum DeliveryType {
		WORLD,
		TEST,
		ADMIN
	}

	/**
	 * Schedule a mailing referenced by {@code mailingId} to be sent (also triggers mailing via {@link com.agnitas.beans.impl.ComMailingImpl#triggerMailing(int)}
	 * to be processed immediately if required).
	 *
	 * @param mailingId an identifier of a mailing to be scheduled.
	 * @param companyId an identifier of a company that the referenced mailing belongs to.
	 * @param options a bundle of delivery options.
	 * @param warnings a bundle to store warnings (if any).
	 * @param errors a bundle to store errors (if any).
	 * @param userActions a list of user actions to store one if succeeded (for UAL).
	 */
    void sendMailing(int mailingId, @VelocityCheck int companyId, MailingSendOptions options, List<Message> warnings, List<Message> errors, List<UserAction> userActions);

	void deactivateMailing(int mailingId, int companyId);
}
