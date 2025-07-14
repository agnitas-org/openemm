/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao;


import com.agnitas.emm.core.commons.uid.ExtensibleUID;
import com.agnitas.emm.core.mailtracking.service.OpenTrackingService;
import com.agnitas.emm.core.mobile.bean.DeviceClass;

public interface OnepixelDao	{
 	
	/**
	 * Do not call this method directly. Use {@link OpenTrackingService#trackOpening(ExtensibleUID, String, DeviceClass, int, int)} instead.
	 *
	 * @see OpenTrackingService#trackOpening(ExtensibleUID, String, DeviceClass, int, int)
	 */
	boolean writePixel(int companyID, int recipientID, int mailingID, String remoteAddr, DeviceClass deviceClass, int deviceID, int clientID);

    void deleteAdminAndTestOpenings(int mailingId, int companyId);

}
