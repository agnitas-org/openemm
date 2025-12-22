/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.trackablelinks.dao;

import java.util.List;
import java.util.Map;

import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.emm.core.mobile.bean.DeviceClass;
import com.agnitas.userform.trackablelinks.bean.TrackableUserFormLink;

public interface FormTrackableLinkDao {

	boolean existsDummyFormLink(int companyId, int userFormId);

	@DaoUpdateReturnValueCheck
	void saveUserFormTrackableLinks(int userFormId, int companyId, List<TrackableUserFormLink> trackableLinks);

	@DaoUpdateReturnValueCheck
	int saveUserFormTrackableLink(int userFormId, int companyId, TrackableUserFormLink trackableLink);

	TrackableUserFormLink getUserFormTrackableLink(int linkID);

	TrackableUserFormLink getUserFormTrackableLink(int companyId, int formId, int linkId);

	TrackableUserFormLink getDummyUserFormTrackableLinkForStatisticCount(int companyID, int formID);

	Map<String, TrackableUserFormLink> getUserFormTrackableLinks(int formID, int companyID);

	List<TrackableUserFormLink> getUserFormTrackableLinkList(int formID, int companyID);

	/**
	 * Logs a click for trackable link in rdir_log_userform_tbl
	 *
	 * @param link the link which was clicked.
	 * @param customerID the id of the recipient who clicked the link.
	 * @param remoteAddr the ip address of the recipient.
	 * @return True on success.
	 */
	boolean logUserFormTrackableLinkClickInDB(TrackableUserFormLink link, Integer customerID, Integer mailingID, String remoteAddr, DeviceClass deviceClass, int deviceID, int clientID);

	boolean logUserFormCallInDB(int companyID, int formID, int linkID, Integer mailingID, Integer customerID, String remoteAddr, DeviceClass deviceClass, int deviceID, int clientID);
}
