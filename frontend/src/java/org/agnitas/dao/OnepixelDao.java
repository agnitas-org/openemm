/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.dao;

import com.agnitas.emm.core.mailtracking.service.OpenTrackingService;
import com.agnitas.emm.core.mobile.bean.DeviceClass;
import org.agnitas.emm.core.velocity.VelocityCheck;

public interface OnepixelDao	{

    /**
     *  Increases count of mailing openings.
     *
     * @param companyID
     *          The id of mailing company.
     * @param recipientID
     *          The id of opener.
     * @param mailingID
     *          The id of opened mailing.
     * @param remoteAddr
     *          The ip address of opener.
     * @return  true on success.
     */

	boolean	writePixel( @VelocityCheck int companyID, int recipientID, int mailingID, String remoteAddr);
	
	/**
	 * Do not call this method directly. User {@link OpenTrackingService#trackOpening(com.agnitas.emm.core.commons.uid.ComExtensibleUID, String, DeviceClass, int, int)} instead.
	 *
	 * @see OpenTrackingService#trackOpening(com.agnitas.emm.core.commons.uid.ComExtensibleUID, String, DeviceClass, int, int)
	 */
	boolean writePixel(@VelocityCheck int companyID, int recipientID, int mailingID, String remoteAddr, DeviceClass deviceClass, int deviceID, int clientID);

    void deleteAdminAndTestOpenings(int mailingId, @VelocityCheck int companyId);

}
