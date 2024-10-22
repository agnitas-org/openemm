/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao;

import java.text.DateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import javax.sql.DataSource;

import com.agnitas.emm.core.recipient.dto.RecipientSalutationDto;
import org.agnitas.beans.BindingEntry;
import org.agnitas.beans.Recipient;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.dao.UserStatus;
import org.agnitas.emm.core.recipient.service.RecipientsModel.CriteriaEquals;
import org.agnitas.util.CsvColInfo;
import org.agnitas.util.DbColumnType;
import org.agnitas.util.SqlPreparedStatementManager;
import org.agnitas.util.Tuple;
import org.apache.commons.collections4.map.CaseInsensitiveMap;

import com.agnitas.beans.Admin;
import com.agnitas.beans.ComRecipientHistory;
import com.agnitas.beans.ComRecipientMailing;
import com.agnitas.beans.ComRecipientReaction;
import com.agnitas.beans.ComTarget;
import com.agnitas.beans.WebtrackingHistoryEntry;
import com.agnitas.beans.impl.ComRecipientLiteImpl;
import com.agnitas.beans.impl.RecipientDates;
import com.agnitas.emm.core.mailing.bean.MailingRecipientStatRow;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.emm.core.recipient.RecipientException;
import com.agnitas.emm.core.recipient.service.RecipientType;
import com.agnitas.emm.core.service.RecipientFieldDescription;

public interface ComRecipientDao {
    String SUPPLEMENTAL_DATECOLUMN_SUFFIX_FORMAT = "_FORMAT";
    
    String SUPPLEMENTAL_DATECOLUMN_SUFFIX_DAY = "_DAY_DATE";
    String SUPPLEMENTAL_DATECOLUMN_SUFFIX_MONTH = "_MONTH_DATE";
    String SUPPLEMENTAL_DATECOLUMN_SUFFIX_YEAR = "_YEAR_DATE";
    String SUPPLEMENTAL_DATECOLUMN_SUFFIX_HOUR = "_HOUR_DATE";
    String SUPPLEMENTAL_DATECOLUMN_SUFFIX_MINUTE = "_MINUTE_DATE";
    String SUPPLEMENTAL_DATECOLUMN_SUFFIX_SECOND = "_SECOND_DATE";

	Tuple<Integer, String> findSpecificCustomerColumn(int companyID, String firstname, String lastname, String email, String fieldname);
    
    boolean updateDataSource(Recipient cust);

    void deleteAllNoBindings(int companyID, String toBeDeletedTable);

    String createTmpTableByMailinglistID(int companyID, int mailinglistID);
	
	CaseInsensitiveMap<String, Object> getCustomerDataFromDb(int companyID, int customerID, boolean respectHideSignIfSet);

	List<ComRecipientLiteImpl> getAdminAndTestRecipients(int companyID, int mailinglistID);

    List<Integer> getAdminAndTestRecipientIds(int companyID, int mailinglistID);

    boolean isMailtrackingEnabled(int companyID);

    List<ComRecipientHistory> getRecipientBindingHistory(int recipientID, int companyID);

    List<Map<String, Object>> getRecipientClicksHistory(int recipientId, int mailingId, int companyId);

    List<ComRecipientHistory> getRecipientProfileHistory(int recipientID, int companyID);

    List<ComRecipientReaction> getRecipientReactionsHistory(int recipientID, int companyID);

    PaginatedListImpl<ComRecipientReaction> getRecipientReactionsHistory(int recipientID, int companyID, int pageNumber, int rowsPerPage, String sortCriterion, boolean sortAscending);

    List<Recipient> getDuplicateRecipients(int companyId, String email, String select, Object[] queryParams) throws Exception;

    PaginatedListImpl<Map<String, Object>> getPaginatedRecipientsData(int companyID, Set<String> columns, String sqlStatementForData, Object[] sqlParametersForData, String sortCriterion, boolean sortedAscending, int pageNumber, int rownums) throws Exception;
    int getIntResult(SqlPreparedStatementManager statementManager) throws Exception;

    // TODO: EMMGUI-714: remove when old design will be removed
    PaginatedListImpl<MailingRecipientStatRow> getMailingRecipients(int mailingId, int companyId, int filterType, int pageNumber, int rowsPerPage, String sortCriterion, boolean sortAscending, List<String> columns) throws Exception;

    int getNumberOfRecipients(int companyId);

    int getNumberOfRecipients(int companyId, boolean ignoreBounceLoadValue);

    boolean isColumnsIndexed(List<String> columns, int companyId);

	/**
     *  Select number of recipients with specific attributes
     */
	int getNumberOfRecipients(int companyID, String sqlStatementForData, Object[] parametersForData);

	int getNumberOfRecipients(int companyId, int mailingListId, String sqlConditions, Object... sqlConditionParameters) throws Exception;

	int getNumberOfRecipients(int companyId, int mailingListId, List<MediaTypes> mediaTypes, String sqlConditions, Object... sqlConditionParameters) throws Exception;

	/**
	 * For bulk insert of new recipients only.
	 * There is no check for CompanyId == 0 and different CompanyId inside.
	 * 
	 * @param custParameters
	 * @param companyID CompanyId should be the same for all recipients in list.
	 * @return list of recipient ID's or empty list in case of errors
	 */
	void checkParameters(CaseInsensitiveMap<String, Object> custParameters, int companyID);

	List<Object> updateCustomers(int companyID, List<Recipient> recipients);

	List<Object> insertCustomers(int companyID, List<Recipient> recipients, List<Boolean> doubleCheck, List<Boolean> overwrite, List<String> keyFields);

	List<CaseInsensitiveMap<String, Object>> getCustomers(List<Integer> customerIDs, int companyID);

    int getAdminOrTestRecipientId(int companyID, int adminId);

    /**
     * Inserts new customer record in Database with a fresh customer-id
     *
     * @param cust recipient to insert
     *
     * @return true on success
     * 
     * @throws Exception on errors writing data
     */
    int insertNewCustWithException(Recipient cust) throws Exception;
	
	/**
     * Updates Customer in DB. customerID must be set to a valid id, customer-data is taken from this.customerData
     * Profile fields not listed in customer are set to <code>null</code> in database.
     *
     * @param cust customer data to be saved to database
     * 
     * @return true on success
     * 
     * @throws Exception on errors writing data
     */
	boolean updateInDbWithException(final Recipient cust) throws Exception;

	/**
     * Updates Customer in DB. customerID must be set to a valid id, customer-data is taken from this.customerData
     * Depending on <code>missingFieldsToNull</code>, profile fields not listed in given customer are either untouched in
     * database (<code>missingFieldsToNull</code> is set to <code>false</code>) or the fields are set to <code>null</code>
     * in database (<code>missingFieldsToNull</code> is set to <code>true</code>).
     *
     * @param cust customer data to be saved to database
     * @param missingFieldsToNull control flag for handling profile fields that are not listed in given customer

     * @return true on success
     * 
     * @throws Exception on errors writing data
     */
	boolean updateInDbWithException(final Recipient cust, final boolean missingFieldsToNull) throws Exception;

    Map<String, Object> getRecipientData(int companyId, int recipientId, boolean respectHideSignIfSet);

    /**
     * Find Subscriber by providing a column-name and a value. Only exact matches possible.
     *
     * @param col Column name
     * @param value Value to search for in col
     * @return customerID or 0 if no matching record found
     */
    int findByKeyColumn(Recipient cust, String col, String value);
    
    /**
     * Update binding status of all subscribers by column.
     * (For example, this method call can update all recipients with a specific email address.)
     * 
     * @param companyId company ID
     * @param columnName name of column to check
     * @param columnValue column value
     * @param newStatus new status
     * @param remark remark for status update
     * @throws Exception
     */
    void updateStatusByColumn(int companyId, String columnName, String columnValue, int newStatus, String remark) throws Exception;

    /**
     * Find Subscriber by providing the id of the company, a column-name and a value.
     *
      * @param companyID The id of the company
     * @param col Column name
     * @param value Value to search for in col
     * @return customerID or 0 if no matching record found
     */
    int findByColumn(int companyID, String col, String value);

    /**
     * Find Subscriber by providing a username and password. Only exact machtes possible.
     *
     * @param companyID The id of the company
     * @param userCol Column name for Username
     * @param userValue Value for Username
     * @param passCol Column name for Password
     * @param passValue Value for Password
     * @return customerID or 0 if no matching record found
     */
    int findByUserPassword(int companyID, String userCol, String userValue, String passCol, String passValue);
    
    /**
     * Load complete Subscriber data from DB. customerID must be set first for this method.
     *
     * @param companyID The id of the company
     * @param customerID The id of the customer
     * @return Map with Key/Value-Pairs of customer data
     */
    CaseInsensitiveMap<String, Object> getCustomerDataFromDb(int companyID, int customerID);
    
    CaseInsensitiveMap<String, Object> getCustomerDataFromDb(int companyID, int customerID, final DateFormat dateFormat);

    /**
     * Load selective Subscriber data from DB. customerID must be set first for this method.
     *
     * @param companyID The id of the company
     * @param customerID The id of the customer
     * @param columns The collection of DB columns to load
     * @return Map with Key/Value-Pairs of customer data
     */
    CaseInsensitiveMap<String, Object> getCustomerDataFromDb(int companyID, int customerID, Collection<String> columns);

    /**
     * Delete complete Subscriber-Data from DB. customerID must be set first for this method.
     *
     * @param companyID The id of the company
     * @param customerID The id of the customer
     */
    void deleteCustomerDataFromDb(int companyID, int customerID);
    
    /**
     * Loads complete Mailinglist-Binding-Information for given customer-id from Database
     *
     * @param companyID The id of the company
     * @param customerID The id of the customer
     * @return Map with key/value-pairs as combinations of mailinglist-id and BindingEntry-Objects
     */
    Map<Integer, Map<Integer, BindingEntry>> loadAllListBindings(int companyID, int customerID);
    

    /**
     * Loads value of given column for given recipient
     *
     * @param selectVal The name of column in database
     * @param recipientID The id of the recipient
     * @param companyID The id of the company
     * @return String value of column named selectVal or empty String if there is not value
     */
    String getField(String selectVal, int recipientID, int companyID);

    /**
     * Loads all mailing lists for given customer
     *
     * @param customerID The id of the customer
     * @param companyID The id of the company
     * @return Map with key/value-pairs as combinations of mailinglist-id and map with key/value-pairs as combinations of mediatype and BindingEntry-Objects
     */
    Map<Integer, Map<Integer, BindingEntry>> getAllMailingLists(int customerID, int companyID);

    /**
     * Load number of recipients for given condition
     *
     * @param companyID the id of the company.
     * @param target the condition for select to database
     * @return number of recipients or 0 if error
     */
    int sumOfRecipients(int companyID, String target);

    /**
     * Delete recipients from database for given condition
     *
     * @param companyID the id of the company
     * @param target the condition for delete to database
     * @return true if success or false if error
     */
    boolean deleteRecipients(int companyID, String target);

    /**
     * Delete recipients from database for given list of customer id
     *
     * @param companyID the id of the company
     * @param list the list of customer id
     * @return 
     */
    int deleteRecipients(int companyID, List<Integer> list);

    /**
     * Loads meta information for all columns from database for given customer
     *
     * @param companyID the id of the company
     * @return  Map with key/value-pairs as combinations of column name and CsvColInfo Objects
     */
	CaseInsensitiveMap<String, CsvColInfo> readDBColumns(int companyID, int adminID, List<String> keyColumns);
	
    /**
     * Method gets a list of test/admin recipients for preview drop-down list
     *
     * @param companyId the id of company
     * @param mailingId id of mailing
     * @return Map in a format "recipient id" -> "recipient description (name, lastname, email)"
     */
	Map<Integer, String> getAdminAndTestRecipientsDescription(int companyId, int mailingId);

    List<ComRecipientLiteImpl> getMailingAdminAndTestRecipients(int mailingId, int companyId);

    /**
     * Method gets the first available test/admin recipient for a mailing preview generation.
     *
     * @param companyId an identifier of a company that owns referenced mailing.
     * @param mailingId an identifier of a mailing (to retrieve a recipient from its mailing list).
     * @return an identifier of the first available test/admin recipient or zero.
     */
    int getPreviewRecipient(int companyId, int mailingId);

    /**
     * Loads the list of bounced recipients for given mailing
     *
     * @param companyId the id of company
     * @param mailingId the id of mailing
     * @return the list of  Recipient objects
     */
    List<Recipient> getBouncedMailingRecipients(int companyId, int mailingId);

    /**
     * Check of existence of customer in database for given id
     *
     * @param companyId the id of company
     * @param customerId the id of customer
     * @return true if customer exist or false if not
     */
    boolean exist(int customerId, int companyId);

    void deleteRecipientsBindings(int mailinglistID, int companyID, boolean activeOnly, boolean noAdminsAndTests);

    int getCustomerIdWithEmailInMailingList(int companyId, int mailingId, String email);

    int getDefaultDatasourceID(String username, int companyID);
	
	void lockCustomers(int companyId, List<Integer> ids);

	/**
	 * Use EQL variant instead.
	 * 
	 * @see #getCustomerDataFromDb(int, boolean, List)
	 */
	@Deprecated
    List<Integer> getCustomerDataFromDb(int companyId, boolean matchAll, List<CriteriaEquals> criteriaEquals);
    
	/**
	 * Use EQL variant instead.
	 * 
	 * @see #getSizeOfCustomerDataFromDbList(int, boolean, List)
	 */
	@Deprecated
    int getSizeOfCustomerDataFromDbList(int companyId, boolean matchAll, List<CriteriaEquals> criteriaEquals);

    CaseInsensitiveMap<String, RecipientFieldDescription> getAvailableProfileFields(int companyID) throws Exception;

	List<WebtrackingHistoryEntry> getRecipientWebtrackingHistory(int companyID, int recipientID);

	void updateForActionOperationUpdateCustomer(int companyID, String columnName, int updateType, String generateUpdateValue, int customerID) throws Exception;

	String selectCustomerValue(String selectValue, int companyID, int customerId);

    int bulkUpdateEachRecipientsFields(int companyId, int adminId, int mailingListId, String sqlTargetExpression, Map<String, Object> updateValues) throws Exception;
    int getAffectedRecipientsForBulkUpdateFields(int companyId, int adminId, int mailingListId, String targetExpression, Map<String, Object> updateValues) throws Exception;

    int getRecipientsAmountForTargetGroup(int companyId, int adminId, int mailingListId, String sqlTargetExpression);

	List<Map<String, Object>> getBouncedRecipients(int companyId, Date fromDate);

	List<Map<String, Object>> getUnsubscribedRecipients(int companyID, Date fromDate);

	RecipientDates getRecipientDates(int companyId, int recipientId);

	DataSource getDataSource();
	
	boolean isOracleDB();

    List<Integer> insertTestRecipients(int companyId, int mailingListId, UserStatus userStatus, String remark, List<String> addresses) throws Exception;

    String getEmail(int companyId, int customerId);

    /**
     * Check whether a given email address in used by some recipient other that given {@code recipientId}.
     *
     * @param email an address to check.
     * @param recipientId an identifier of a recipient to ignore (or 0).
     * @param companyId an identifier of a current user's company.
     * @return {@code true} if there's a recipient (other that given recipientId) having given email or {@code false} otherwise.
     */
    boolean checkAddressInUse(String email, int recipientId, int companyId);
    
    /**
     * Find existing recipient ID other that given {@code recipientId} by a given email address in used.
     *
     * @param email an address to check.
     * @param recipientId an identifier of a recipient to ignore (or 0).
     * @param companyId an identifier of a current user's company.
     * @return {@code int} recipient ID if there's a recipient (other that given recipientId) having given email or {@code 0} otherwise.
     */
    int getRecipientIdByAddress(String email, int recipientId, int companyId);
    
    void writeEmailAddressChangeRequest(final int companyID, final int customerID, final String newEmailAddress, final String confirmationCode);

	String readEmailAddressForPendingChangeRequest(final int companyID, final int customerID, final String confirmationCode);

	void deletePendingEmailAddressChangeRequest(final int companyID, final int customerID, final String confirmationCode);

	List<Integer> getMailingRecipientIds(int companyID, int mailinglistID, MediaTypes mediaTypes, String fullTargetSql, List<UserStatus> userstatusList);

	void logMailingDelivery(int companyID, int maildropStatusID, int customerID, int mailingID);

	DbColumnType getColumnDataType(int companyId, String columnName) throws Exception;

	List<Integer> getRecipientIDs(int companyID, String keyColumn, String keyValue);

	CaseInsensitiveMap<String, Object> getCustomerData(int companyID, int customerID);

    boolean isRecipientMatchTarget(int companyId, String targetExpression, int customerId);

    boolean isNotSavedRecipientDataMatchTarget(int companyId, int recipientId, String targetExpression, Map<String, Object> entry) throws Exception;

    List<Integer> listRecipientIdsByTargetGroup(final int companyId, final ComTarget target);

    int saveRecipient(int companyId, int recipientId, Map<String, Object> recipientValues) throws Exception;

	List<Recipient> findByData(int companyID, Map<String, Object> searchDataMap) throws Exception;

	BindingEntry getMailinglistBinding(int companyID, int customerID, int mailinglistId, int mediaType) throws Exception;

	List<CaseInsensitiveMap<String, Object>> getMailinglistRecipients(int companyID, int mailinglistID, MediaTypes mediaTypes, String targetSql, List<String> profileFieldsList, List<UserStatus> userstatusList, TimeZone timeZone) throws Exception;

    List<RecipientSalutationDto> getAdminAndTestRecipientsSalutation(int companyId, int adminId);

	List<ComRecipientLiteImpl> listAdminAndTestRecipientsByAdmin(int companyID, int adminID);
	
	/**
	 * Get data of several recipients<br />
	 * Keeps datatypes in Object class (differs from method "getCustomers")<br />
	 * Dates are returned as items of class Date
	 */
	List<CaseInsensitiveMap<String, Object>> getCustomersData(List<Integer> customerIDs, int companyID);

	int getMinimumCustomerId(int companyID);

	boolean isRecipientTrackingAllowed(int companyID, int recipientID);

	List<CaseInsensitiveMap<String, Object>> getTargetRecipients(int companyID, String targetSql, List<String> profileFieldsList, TimeZone timeZone) throws Exception;

	List<ComRecipientMailing> getMailingsDeliveredToRecipient(int customerID, int companyID);

	List<Integer> getCustomerDataFromDb(int companyId, String eql) throws RecipientException;

	int getSizeOfCustomerDataFromDbList(int companyId, String eql);

    int getOrCreateRecipientOfAdmin(Admin admin) throws Exception;

	List<Integer> getFilteredRecipientIDs(int companyID, CaseInsensitiveMap<String, RecipientFieldDescription> recipientFieldsMap, Map<String, String> recipientFilters);

	int countFilteredRecipientIDs(int companyID, CaseInsensitiveMap<String, RecipientFieldDescription> recipientFieldsMap, Map<String, String> recipientFilters);

    List<String> fetchRecipientNames(Set<Integer> ids, int companyID);

    int getBounceDetail(int mailingId, int recipientId, int companyId);

	int getNumberOfRecipients(int companyID, int mailinglistID, RecipientType... recipientTypes);

    List<Recipient> findAllByEmailPart(String email, int companyId);

    boolean tableExists(int companyId);

    void updateEmail(String email, int id, int companyId);

    List<Integer> findIdsByEmail(String email, int companyId);
}
