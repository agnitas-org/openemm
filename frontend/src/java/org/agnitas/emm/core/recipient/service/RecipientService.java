/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.recipient.service;

import java.util.List;
import java.util.Map;

import org.agnitas.beans.Recipient;
import org.agnitas.emm.core.recipient.service.impl.ProfileFieldNotExistException;
import org.agnitas.emm.core.useractivitylog.UserAction;
import org.agnitas.emm.core.velocity.VelocityCheck;

import com.agnitas.beans.ComAdmin;
import com.agnitas.beans.ComProfileField;
import com.agnitas.beans.impl.ComRecipientLiteImpl;
import com.agnitas.emm.core.commons.uid.ComExtensibleUID;
import com.agnitas.emm.core.recipient.dto.RecipientFieldDto;
import com.agnitas.emm.core.recipient.service.FieldsSaveResults;
import com.agnitas.service.ServiceResult;

public interface RecipientService {

	int findSubscriber(@VelocityCheck int companyId, String keyColumn, String value);

	void checkColumnsAvailable(RecipientModel model) throws ProfileFieldNotExistException;

	int addSubscriber(RecipientModel model, String username, final int companyID, List<UserAction> userActions) throws Exception;
	
	Map<String, Object> getSubscriber(RecipientModel model);
	
    Recipient getRecipient(final RecipientModel model) throws RecipientNotExistException;
	
    Recipient getRecipient(final int companyID, final int customerID) throws RecipientNotExistException;
	
	List<Integer> getSubscribers(RecipientsModel model);
	
	void deleteSubscriber(RecipientModel model, List<UserAction> userActions);

	boolean updateSubscriber(RecipientModel model, String username) throws Exception;
	
    ServiceResult<FieldsSaveResults> saveBulkRecipientFields(ComAdmin admin, int targetId, int mailinglistId, Map<String, RecipientFieldDto> fieldChanges);
    
    List<ComRecipientLiteImpl> getAdminAndTestRecipients(@VelocityCheck int companyId, int mailinglistId);
    
    void supplySourceID(Recipient recipient, int defaultId);

	int getSubscribersSize(RecipientsModel model);

	List<Map<String, Object>> getSubscriberMailings(RecipientModel model);

	boolean isMailTrackingEnabled(int companyId);
	
	void updateRecipientWithEmailChangeConfiguration(final Recipient recipient, final int mailingID, final String profileFieldForConfirmationCode) throws Exception;

	void confirmEmailAddressChange(ComExtensibleUID uid, String confirmationCode) throws Exception;
	
	List<ComProfileField> getRecipientBulkFields(@VelocityCheck int companyId);
    
    int calculateRecipient(ComAdmin admin, int targetId, int mailinglistId);
}
