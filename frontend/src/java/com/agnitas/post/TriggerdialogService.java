/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.post;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.agnitas.beans.DeliveryStat;
import com.agnitas.beans.Mailing;

public interface TriggerdialogService {

	String MASCAMPAIGNID_PREFIX = "EMM_Mailing_";
	String SALUTATION_PREFIX = "salutation_";
	String GENDER_PREFIX = "gender_";

	boolean isPostMailing(Mailing mailing);

	boolean existsCampaign(int companyID, int mailingID, String shortname) throws Exception;

	void createExternalMailing(Mailing mailing) throws Exception;

	void updateExternalMailing(Mailing mailing) throws Exception;

	void createTriggerdialogDelivery(int companyID, int mailingID, Date sendDate) throws Exception;

	boolean createPostTrigger(Mailing mailing, Date sendDate) throws Exception;

	String createSsoUrl(String triggerDialogFrontendUrl, String ssoSharedSecret, int triggerDialogMasId, String triggerDialogMasClientId, String ssoUsername, String ssoEmail, String ssoFirstname, String ssoLastname, int validityInMinutes) throws Exception;

	List<TriggerdialogField> getExternalMailingFields(int companyID, int mailingID);

	Map<String, String[]> createFieldTypesMap(List<TriggerdialogField> fields);

	List<String> getCustomerFields(int companyId, int mailingId);

	List<String> getReferenceFields(int companyId, int mailingId);

	void updatePostalMailing(Mailing mailing, int companyID, String genderLang, Map<String, String[]> variableTypes, String salutationType, String salutationTagType) throws Exception;

	void storeExternalMailingFields(int companyID, int mailingID, List<TriggerdialogField> fields);

	void storeExternalMailingField(TriggerdialogField field);

	void removeExternalMailingField(String fieldName, int companyId, int mailingId);

	DeliveryStat getTriggerdialogDeliveryStatus(int mailingID) throws Exception;

	boolean canStopMailing(int companyID, int mailingID) throws Exception;

	void updateTriggerdialogDeliveryStatusByMailingID(int mailingID, TriggerdialogDeliveryStatus newStatus);

	void storeMandatoryFieldsIfNotExists(int mailingId, int companyID);

	void storeNewExternalFields(List<TriggerdialogField> fields);

	List<String> getExternalFieldsNames(List<TriggerdialogField> externalFields);

	TriggerdialogDeliveryStatus getTriggerdialogDeliveryStatusByMailingID(int mailingID) throws Exception;
}
