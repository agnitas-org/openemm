/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/


package com.agnitas.emm.core.binding.service.event;

import org.agnitas.dao.UserStatus;

import com.agnitas.emm.core.mediatypes.common.MediaTypes;

/**
 * Callback for changes on binding states of recipients.
 */
public interface OnBindingChangedHandler {

	/**
	 * Called, when a new binding has been created.
	 * 
	 * @param companyID company ID
	 * @param recipientID recipient ID
	 * @param mailinglistID mailinglist ID
	 * @param mediatype media type
	 * @param userStatus users tatus
	 */
	void bindingCreated(final int companyID, final int recipientID, final int mailinglistID, final MediaTypes mediatype, final UserStatus userStatus);

	/**
	 * Called, when a new binding has been changed.
	 * 
	 * @param companyID company ID
	 * @param recipientID recipient ID
	 * @param mailinglistID mailinglist ID
	 * @param mediatype media type
	 * @param userStatus users tatus
	 */
	void bindingChanged(final int companyID, final int recipientID, final int mailinglistID, final MediaTypes mediatype, final UserStatus userStatus);
	
}
