/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.recipient.service;

import java.io.File;
import java.text.DateFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import jakarta.servlet.http.HttpServletRequest;

import org.agnitas.beans.BindingEntry;
import org.agnitas.beans.Recipient;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.dao.UserStatus;
import org.agnitas.emm.core.recipient.dto.RecipientLightDto;
import org.agnitas.emm.core.recipient.service.impl.ProfileFieldNotExistException;
import org.agnitas.emm.core.useractivitylog.UserAction;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.web.RecipientForm;
import org.apache.commons.beanutils.DynaBean;
import org.springframework.cache.annotation.Cacheable;

import com.agnitas.beans.ComAdmin;
import com.agnitas.beans.ProfileField;
import com.agnitas.beans.impl.ComRecipientLiteImpl;
import com.agnitas.emm.core.commons.uid.ComExtensibleUID;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.emm.core.recipient.dto.RecipientBindingsDto;
import com.agnitas.emm.core.recipient.dto.RecipientDto;
import com.agnitas.emm.core.recipient.dto.RecipientFieldDto;
import com.agnitas.emm.core.recipient.dto.RecipientSearchParamsDto;
import com.agnitas.emm.core.recipient.dto.SaveRecipientDto;
import com.agnitas.emm.core.recipient.service.FieldsSaveResults;
import com.agnitas.service.ServiceResult;
import com.agnitas.service.SimpleServiceResult;

import net.sf.json.JSONArray;

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

	List<RecipientLightDto> getDuplicateRecipients(ComAdmin admin, String fieldName, int recipientId) throws Exception;

	ServiceResult<FieldsSaveResults> saveBulkRecipientFields(ComAdmin admin, int targetId, int mailinglistId, Map<String, RecipientFieldDto> fieldChanges);
	
	File getDuplicateAnalysisCsv(ComAdmin admin, String searchFieldName, Map<String, String> fieldsMap, Set<String> selectedColumns, String sort, String order) throws Exception;

    Set<ProfileField> getRecipientColumnInfos(ComAdmin admin);

	RecipientLightDto getRecipientLightDto(@VelocityCheck int companyId, int recipientId);

	RecipientDto getRecipientDto(ComAdmin admin, int recipientId);

    List<ComRecipientLiteImpl> getAdminAndTestRecipients(@VelocityCheck int companyId, int mailinglistId);
    
    void supplySourceID(Recipient recipient, int defaultId);

	int getSubscribersSize(RecipientsModel model);

	int getNumberOfRecipients(int companyId);

	List<Map<String, Object>> getSubscriberMailings(RecipientModel model);

	boolean isMailTrackingEnabled(int companyId);
	
	void updateRecipientWithEmailChangeConfiguration(final Recipient recipient, final int mailingID, final String profileFieldForConfirmationCode) throws Exception;

	void confirmEmailAddressChange(ComExtensibleUID uid, String confirmationCode) throws Exception;
	
	List<ProfileField> getRecipientBulkFields(@VelocityCheck int companyId);
    
    int calculateRecipient(ComAdmin admin, int targetId, int mailinglistId);
	
	boolean deleteRecipients(ComAdmin admin, Set<Integer> bulkIds);

	Callable<PaginatedListImpl<DynaBean>> getRecipientWorker(HttpServletRequest request, RecipientForm form, Set<String> recipientDbColumns, String sort, String direction, int pageNumber, int rownums) throws Exception;

	PaginatedListImpl<RecipientDto> getPaginatedRecipientList(ComAdmin admin, RecipientSearchParamsDto searchParams, String sort, String order, int page, int rownums, Map<String, String> fields) throws Exception;

	PaginatedListImpl<DynaBean> getPaginatedDuplicateList(ComAdmin admin, String searchFieldName, String sort, String order, int page, int rownums, Map<String, String> fields) throws Exception;
	
	List<Integer> listRecipientIdsByTargetGroup(final int targetId, final int companyId);

	JSONArray getDeviceHistoryJson(@VelocityCheck int companyId, int recipientId);

	JSONArray getWebtrackingHistoryJson(ComAdmin admin, int recipientId);

	JSONArray getContactHistoryJson(@VelocityCheck int companyId, int recipientId);

    void updateDataSource(Recipient recipient);

	int findByKeyColumn(Recipient recipient, String keyColumn, String keyVal);

	Map<String, String> getRecipientDBStructure(int companyId);

	int findByUserPassword(int companyId, String keyColumn, String keyVal, String passColumn, String passVal);

	boolean importRequestParameters(Recipient recipient, Map<String, Object> requestParameters, String suffix);

	void updateBindingsFromRequest(Recipient recipient, Map<String, Object> params, boolean doubleOptIn) throws Exception;

	void updateBindingsFromRequest(Recipient recipient, Map<String, Object> params, boolean doubleOptIn, String remoteAddr, String referrer) throws Exception;

	Map<String, Object> getCustomerDataFromDb(int companyId, int customerId, DateFormat dateFormat);

	Map<Integer, Map<Integer, BindingEntry>> getMailinglistBindings(int companyId, int customerId);

	String getRecipientField(String value, int customerId, int companyId);

	List<Integer> getMailingRecipientIds(int companyID, int mailinglistID, MediaTypes post, String fullTargetSql, List<UserStatus> userstatusList);

	void logMailingDelivery(int companyId, int id, int customerId, int mailingId);

	boolean updateRecipientInDB(Recipient recipient) throws Exception;

	void deleteCustomerDataFromDb(int companyId, int customerId);

	int saveNewCustomer(Recipient recipient) throws Exception;

    List<Recipient> findRecipientByData(int companyID, Map<String, Object> dataMap) throws Exception;

	BindingEntry getBindingsByMailinglistId(int companyID, int customerID, int mailinglistID, int mediaType);

	List<Integer> getRecipientIds(int companyID, String recipientEmail, String customerEmail);

    void deleteRecipient(@VelocityCheck int companyId, int recipientId);

    void updateBindings(List<BindingEntry> bindings, int companyId) throws Exception;

	@Cacheable(cacheManager = "requestCacheManager", cacheNames = "editableFields")
	Map<String, ProfileField> getEditableColumns(ComAdmin admin);

	ServiceResult<Integer> saveRecipient(ComAdmin admin, SaveRecipientDto recipient, List<UserAction> userActions);

	SimpleServiceResult isRecipientMatchAltgTarget(ComAdmin admin, SaveRecipientDto recipient);

	ServiceResult<List<String>> saveRecipientBindings(ComAdmin admin, int recipientId, RecipientBindingsDto bindings);

	int getRecipientIdByAddress(ComAdmin admin, int recipientId, String email);

	JSONArray getRecipientStatusChangesHistory(ComAdmin admin, int recipientId);

	BindingEntry getMailinglistBinding(int companyID, int customerID, int mailinglistID, int mediaCode) throws Exception;

	List<ComRecipientLiteImpl> listAdminAndTestRecipients(final ComAdmin admin);
}
