/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

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
import java.util.TimeZone;

import com.agnitas.emm.core.recipient.dto.RecipientSalutationDto;
import org.agnitas.beans.BindingEntry;
import org.agnitas.beans.Recipient;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.dao.UserStatus;
import org.agnitas.emm.core.recipient.dto.RecipientLightDto;
import org.agnitas.emm.core.recipient.service.impl.ProfileFieldNotExistException;
import org.agnitas.emm.core.useractivitylog.UserAction;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.springframework.cache.annotation.Cacheable;

import com.agnitas.beans.Admin;
import com.agnitas.beans.ProfileField;
import com.agnitas.beans.impl.ComRecipientLiteImpl;
import com.agnitas.emm.core.commons.uid.ComExtensibleUID;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.emm.core.recipient.RecipientException;
import com.agnitas.emm.core.recipient.dto.BindingAction;
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

	int findSubscriber(int companyId, String keyColumn, String value);

	void checkColumnsAvailable(RecipientModel model) throws ProfileFieldNotExistException;

	int addSubscriber(RecipientModel model, String username, final int companyID, List<UserAction> userActions) throws Exception;
	
	Map<String, Object> getSubscriber(RecipientModel model);
	
    Recipient getRecipient(final RecipientModel model) throws RecipientNotExistException;
	
    Recipient getRecipient(final int companyID, final int customerID) throws RecipientNotExistException;
	
	List<Integer> getSubscribers(RecipientsModel model) throws RecipientException;
	
	void deleteSubscriber(RecipientModel model, List<UserAction> userActions);

	boolean updateSubscriber(RecipientModel model, String username) throws Exception;

	// TODO: remove after EMMGUI-714 will be finished and old design will be removed
	List<RecipientLightDto> getDuplicateRecipients(Admin admin, String fieldName, int recipientId) throws Exception;

	ServiceResult<FieldsSaveResults> saveBulkRecipientFields(Admin admin, int targetId, int mailinglistId, Map<String, RecipientFieldDto> fieldChanges);
	ServiceResult<Integer> getAffectedRecipientsCountForBulkSaveFields(Admin admin, int targetId, int mailinglistId, Map<String, RecipientFieldDto> fieldChanges);

	File getDuplicateAnalysisCsv(Admin admin, String searchFieldName, Map<String, String> fieldsMap, Set<String> selectedColumns, String sort, String order) throws Exception;

    Set<ProfileField> getRecipientColumnInfos(Admin admin);

	RecipientLightDto getRecipientLightDto(int companyId, int recipientId);

	RecipientDto getRecipientDto(Admin admin, int recipientId);

    List<ComRecipientLiteImpl> getAdminAndTestRecipients(int companyId, int mailinglistId);

	List<RecipientSalutationDto> getAdminAndTestRecipientsSalutation(Admin admin);

    void supplySourceID(Recipient recipient, int defaultId);

	int getSubscribersSize(RecipientsModel model);

	int getNumberOfRecipients(int companyId);

	boolean hasBeenReachedLimitOnNonIndexedImport(int companyId);

	List<Map<String, Object>> getSubscriberMailings(RecipientModel model);

	boolean isMailTrackingEnabled(int companyId);
	
	void updateRecipientWithEmailChangeConfiguration(final Recipient recipient, final int mailingID, final String profileFieldForConfirmationCode) throws Exception;

	void confirmEmailAddressChange(ComExtensibleUID uid, String confirmationCode) throws Exception;
	
	List<ProfileField> getRecipientBulkFields(int companyID, int adminID);
    
    int calculateRecipient(Admin admin, int targetId, int mailinglistId);
	
	boolean deleteRecipients(Admin admin, Set<Integer> bulkIds);

	PaginatedListImpl<RecipientDto> getPaginatedRecipientList(Admin admin, RecipientSearchParamsDto searchParams, String sort, String order, int page, int rownums, Map<String, String> fields) throws Exception;

	PaginatedListImpl<RecipientDto> getPaginatedDuplicateList(Admin admin, String searchFieldName, boolean caseSensitive, String sort, String order, int page, int rownums, Map<String, String> fields) throws Exception;
	
	List<Integer> listRecipientIdsByTargetGroup(final int targetId, final int companyId);

	JSONArray getDeviceHistoryJson(int companyId, int recipientId);

	JSONArray getWebtrackingHistoryJson(Admin admin, int recipientId);

	JSONArray getContactHistoryJson(int companyId, int recipientId);

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

    List<Recipient> findAllByEmailPart(String email, List<Integer> companiesIds);
    List<Recipient> findRecipientByData(int companyID, Map<String, Object> dataMap) throws Exception;

	BindingEntry getBindingsByMailinglistId(int companyID, int customerID, int mailinglistID, int mediaType);

	List<Integer> getRecipientIds(int companyID, String recipientEmail, String customerEmail);

    void deleteRecipient(int companyId, int recipientId);

    void updateBindings(List<BindingEntry> bindings, int companyId) throws Exception;

	@Cacheable(cacheManager = "requestCacheManager", cacheNames = "editableFields")
	Map<String, ProfileField> getEditableColumns(Admin admin);

	ServiceResult<Integer> saveRecipient(Admin admin, SaveRecipientDto recipient, List<UserAction> userActions);

	SimpleServiceResult isRecipientMatchAltgTarget(Admin admin, SaveRecipientDto recipient);

	ServiceResult<List<BindingAction>> saveRecipientBindings(Admin admin, int recipientId, RecipientBindingsDto bindings, UserStatus newStatusForUnsubscribing);

	int getRecipientIdByAddress(Admin admin, int recipientId, String email);

	JSONArray getRecipientStatusChangesHistory(Admin admin, int recipientId);

	BindingEntry getMailinglistBinding(int companyID, int customerID, int mailinglistID, int mediaCode) throws Exception;

	List<ComRecipientLiteImpl> listAdminAndTestRecipients(final Admin admin);

	int getMinimumCustomerId(int companyID);
	
	boolean isRecipientTrackingAllowed(final int companyID, final int recipientID);
	
	int countSubscribers(final int companyID);

	boolean isColumnsIndexed(List<String> columns, int companyId);

	List<String> fetchRecipientNames(Set<Integer> bulkIds, int companyID);

    JSONArray getClicksJson(int recipientId, int mailingId, int companyId);

    String getEmail(int recipientId, int companyId);

	boolean recipientExists(int companyID, int customerID);

	List<CaseInsensitiveMap<String, Object>> getMailinglistRecipients(int companyID, int id, MediaTypes email, String targetSql, List<String> profileFieldsToShow, List<UserStatus> userstatusList, TimeZone timeZone) throws Exception;

    ServiceResult<List<RecipientLightDto>> getAllowedForDeletion(Set<Integer> ids, Admin admin);
	ServiceResult<UserAction> delete(Set<Integer> ids, Admin admin);
	SimpleServiceResult delete(int id, int companyId, Admin admin);

	void updateEmail(String newEmail, int id, int companyId);

	List<Integer> findIdsByEmail(String email, int companyId);
}
