/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailtracking.service;

import com.agnitas.emm.core.commons.uid.ComExtensibleUID;
import com.agnitas.emm.core.mobile.bean.DeviceClass;

/**
 * Service for tracking openings of mailings.
 */
public interface OpenTrackingService {

	/**
	 * Track opening by given UID.
	 * 
	 * @param uid UID
	 * @param remoteAddr client IP address
	 * @param deviceClass device class
	 * @param deviceID device type
	 * @param clientID client type 
	 */
	public void trackOpening(final ComExtensibleUID uid, final boolean doNotTrackRecipient, final String remoteAddr, final DeviceClass deviceClass, final int deviceID, final int clientID);

	/**
	 * Track opening by given IDs.
	 * 
	 * @param companyID company ID 
	 * @param customerID customer ID
	 * @param mailingID mailing ID
	 * 
	 * @param remoteAddr client IP address
	 * @param deviceClass device class
	 * @param deviceID device type
	 * @param clientID client type 
	 */
	public void trackOpening(final int companyID, final int customerID, final int mailingID, final String remoteAddr, final DeviceClass deviceClass, final int deviceID, final int clientID);
}
