/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.post;

import java.util.Date;
import java.util.List;

import com.agnitas.beans.ComMailing;

public interface TriggerdialogService {
	public static final String MASCAMPAIGNID_PREFIX = "EMM_Mailing_";
	
	boolean isPostMailing(ComMailing mailing);
	boolean existsCampaign(int companyID, int mailingID, String shortname) throws Exception;
	void createExternalMailing(ComMailing mailing) throws Exception;
	void updateExternalMailing(ComMailing mailing) throws Exception;
	void createTriggerdialogDelivery(int companyID, int mailingID, Date sendDate);
	String createSsoUrl(String triggerDialogBasicUrl, String ssoSharedSecret, int triggerDialogMasId, String triggerDialogMasClientId, String ssoUsername, String ssoEmail, String ssoFirstname, String ssoLastname, int validityInMinutes) throws Exception;
	List<String> getExternalMailingFields(int companyID, int mailingID);
	void storeExternalMailingFields(int companyID, int mailingID, List<String> fields);
}