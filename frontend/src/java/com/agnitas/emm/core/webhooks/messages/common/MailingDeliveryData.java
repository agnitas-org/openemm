/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.webhooks.messages.common;

import java.time.ZonedDateTime;
import java.util.Objects;

public final class MailingDeliveryData {

	private final int companyID;
	private final int mailingID;
	private final int customerID;
	private final ZonedDateTime timestamp;
	
	public MailingDeliveryData(final int companyID, final int mailingID, final int customerID, final ZonedDateTime timestamp) {
		this.companyID = companyID;
		this.mailingID = mailingID;
		this.customerID = customerID;
		this.timestamp = Objects.requireNonNull(timestamp, "Timestamp is null");
	}
	
	public final int getCompanyID() {
		return this.companyID;
	}

	public final int getMailingID() {
		return this.mailingID;
	}

	public final int getCustomerID() {
		return this.customerID;
	}
	
	public final ZonedDateTime getTimestamp() {
		return this.timestamp;
	}

}
