/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailtracking.service;

import com.agnitas.emm.core.commons.uid.ExtensibleUID;
import com.agnitas.emm.core.mobile.bean.DeviceClass;

/**
 * Service for tracking link clicks in mailings.
 */
public interface ClickTrackingService {

	/**
	 * Track link click.
	 * 
	 * @param uid UID
	 * @param remoteAddress client IP address
	 * @param deviceClass client device class
	 * @param deviceID device ID
	 * @param clientID client ID
	 */
	public void trackLinkClick(final ExtensibleUID uid, final String remoteAddress, final DeviceClass deviceClass, final int deviceID, final int clientID);

}
