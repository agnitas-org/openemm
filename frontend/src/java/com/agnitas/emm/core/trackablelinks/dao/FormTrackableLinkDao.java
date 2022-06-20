/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.trackablelinks.dao;

import java.util.List;
import java.util.Map;

import org.agnitas.emm.core.velocity.VelocityCheck;

import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.emm.core.mobile.bean.DeviceClass;
import com.agnitas.userform.trackablelinks.bean.ComTrackableUserFormLink;

public interface FormTrackableLinkDao {

	boolean existsDummyFormLink(@VelocityCheck int companyId, int userFormId);

	@DaoUpdateReturnValueCheck
	void saveUserFormTrackableLinks(int userFormId, int companyId, List<ComTrackableUserFormLink> trackableLinks);

	@DaoUpdateReturnValueCheck
	int saveUserFormTrackableLink(int userFormId, int companyId, ComTrackableUserFormLink trackableLink);

	@DaoUpdateReturnValueCheck
	void storeUserFormTrackableLinkProperties(ComTrackableUserFormLink link);

	ComTrackableUserFormLink getUserFormTrackableLink(int linkID) throws Exception;

	ComTrackableUserFormLink getUserFormTrackableLink(@VelocityCheck int companyId, int formId, int linkId);

	ComTrackableUserFormLink getDummyUserFormTrackableLinkForStatisticCount(@VelocityCheck int companyID, int formID) throws Exception;

	Map<String, ComTrackableUserFormLink> getUserFormTrackableLinks(int formID, @VelocityCheck int companyID);

	List<ComTrackableUserFormLink> getUserFormTrackableLinkList(int formID, @VelocityCheck int companyID);

	@DaoUpdateReturnValueCheck
	boolean deleteUserFormTrackableLink(int linkID, @VelocityCheck int companyID);

	/**
	 * Logs a click for trackable link in rdir_log_userform_tbl
	 *
	 * @param link the link which was clicked.
	 * @param customerID the id of the recipient who clicked the link.
	 * @param remoteAddr the ip address of the recipient.
	 * @return True on success.
	 */
	boolean logUserFormTrackableLinkClickInDB(ComTrackableUserFormLink link, Integer customerID, Integer mailingID, String remoteAddr, DeviceClass deviceClass, int deviceID, int clientID);

	boolean logUserFormCallInDB(@VelocityCheck int companyID, int formID, int linkID, Integer mailingID, Integer customerID, String remoteAddr, DeviceClass deviceClass, int deviceID, int clientID);
}
