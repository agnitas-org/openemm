/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

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

import javax.sql.DataSource;

import org.agnitas.beans.BindingEntry;
import org.agnitas.beans.CustomerImportStatus;
import org.agnitas.beans.ProfileField;
import org.agnitas.beans.Recipient;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.emm.core.binding.service.BindingService;
import org.agnitas.emm.core.recipient.service.RecipientsModel.CriteriaEquals;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.util.CsvColInfo;
import org.agnitas.util.Tuple;
import org.apache.commons.collections4.map.CaseInsensitiveMap;

import com.agnitas.beans.ComRecipientHistory;
import com.agnitas.beans.ComRecipientMailing;
import com.agnitas.beans.ComRecipientReaction;
import com.agnitas.beans.WebtrackingHistoryEntry;
import com.agnitas.beans.impl.ComRecipientLiteImpl;
import com.agnitas.beans.impl.RecipientDates;
import com.agnitas.emm.core.mailing.bean.MailingRecipientStatRow;

public interface ComRecipientDao {
    String SUPPLEMENTAL_DATECOLUMN_SUFFIX_DAY = "_DAY_DATE";
    String SUPPLEMENTAL_DATECOLUMN_SUFFIX_MONTH = "_MONTH_DATE";
    String SUPPLEMENTAL_DATECOLUMN_SUFFIX_YEAR = "_YEAR_DATE";
    String SUPPLEMENTAL_DATECOLUMN_SUFFIX_HOUR = "_HOUR_DATE";
    String SUPPLEMENTAL_DATECOLUMN_SUFFIX_MINUTE = "_MINUTE_DATE";
    String SUPPLEMENTAL_DATECOLUMN_SUFFIX_SECOND = "_SECOND_DATE";

	Tuple<Integer, String> findSpecificCustomerColumn(@VelocityCheck int companyID, String firstname, String lastname, String email, String fieldname);
    
    boolean updateDataSource(Recipient cust);

    void deleteAllNoBindings(@VelocityCheck int companyID, String toBeDeletedTable);

    String createTmpTableByMailinglistID(@VelocityCheck int companyID, int mailinglistID);
	
	CaseInsensitiveMap<String, Object> getCustomerDataFromDb(@VelocityCheck int companyID, int customerID, boolean respectHideSignIfSet);

	List<ComRecipientLiteImpl> getAdminAndTestRecipients(@VelocityCheck int companyID, int mailinglistID);

    List<Integer> getAdminAndTestRecipientIds(@VelocityCheck int companyID, int mailinglistID);

	List<Integer> filterRecipientsByMailinglistAndTarget(List<Integer> recipientIds, int companyId, int mailinglistId, String sqlTargetExpression, boolean allRecipients);

	List<Integer> filterRecipientsByMailinglistAndTarget(List<Integer> recipientIds, int companyId, int mailinglistId, String sqlTargetExpression, boolean allRecipients, boolean shouldBeActive);

	List<Integer> getDateMatchingRecipients(int companyId, List<Date> allDates, String dateProfileField, String dateFieldOperator, String dateFieldValue, String dateFormat);

    boolean successTableActivated(@VelocityCheck int companyID);

    boolean isMailtrackingEnabled(@VelocityCheck int companyID);

    List<ComRecipientMailing> getMailingsSentToRecipient(int recipientID, int companyID);

    PaginatedListImpl<ComRecipientMailing> getMailingsSentToRecipient(int customerID, int companyID, int pageNumber, int rowsPerPage, String sortCriterion, boolean sortAscending);

    List<ComRecipientHistory> getRecipientBindingHistory(int recipientID, int companyID);

    List<ComRecipientHistory> getRecipientProfileHistory(int recipientID, int companyID);

    List<ComRecipientReaction> getRecipientReactionsHistory(int recipientID, int companyID);

    PaginatedListImpl<ComRecipientReaction> getRecipientReactionsHistory(int recipientID, int companyID, int pageNumber, int rowsPerPage, String sortCriterion, boolean sortAscending);

    /**
     *  Select only a certain page of recipients with all available fields, used for dynamic paging in list views
     *
     * @param companyID an identifier of a company of current user.
     * @param columns set of columns are to be selected.
     * @param sqlStatementForData the basic sql statement to be used as a sub-query to retrieve customers.
     * @param sqlParametersForData sql parameters for the basic sql statement.
     * @param sortCriterion name of the column which is the sort criterion.
     * @param sortedAscending whether ({@code true}) or not ({@code false}) a sorting order is ascending (descending otherwise).
     * @param pageNumber the 1-based page index.
     * @param rownums number of rows per page.
     * @return a list of recipients.
     * @throws Exception 
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    PaginatedListImpl<Recipient> getRecipients(int companyID, Set<String> columns, String sqlStatementForData, Object[] sqlParametersForData, String sortCriterion, boolean sortedAscending, int pageNumber, int rownums) throws Exception;

    PaginatedListImpl<MailingRecipientStatRow> getMailingRecipients(int mailingId, @VelocityCheck int companyId, int filterType, int pageNumber, int rowsPerPage, String sortCriterion, boolean sortAscending, List<String> columns);

	/**
     *  Select number of recipients with specific attributes
     */
	int getNumberOfRecipients(@VelocityCheck int companyID, String sqlStatementForData, Object[] parametersForData);

	int getNumberOfRecipients(@VelocityCheck int companyId, int mailingListId, String sqlConditions, Object... sqlConditionParameters) throws Exception;

	/**
	 * For bulk insert of new recipients only. 
	 * There is no check for CompanyId == 0 and different CompanyId inside.
	 * 
	 * @param custParameters
	 * @param companyID CompanyId should be the same for all recipients in list.
	 * @return list of recipient ID's or empty list in case of errors
	 */
	void checkParameters(CaseInsensitiveMap<String, Object> custParameters, int companyID);

	List<Object> updateCustomers(@VelocityCheck int companyID, List<Recipient> recipients);

	List<Object> insertCustomers(@VelocityCheck int companyID, List<Recipient> recipients, List<Boolean> doubleCheck, List<Boolean> overwrite, List<String> keyFields);

	List<CaseInsensitiveMap<String, Object>> getCustomers(List<Integer> customerIDs, int companyID);

    /**
     * Check whether it is allowed to add the given number of recipients.
     * The maximum number of recipients/company is defined in
     * emm.properties with recipient.maxRows.
     *
     * @param companyID The id of the company to check.
     * @param count the number of recipients that should be added.
     * @return true if it is allowed to add the given number of recipients.
     */
    boolean mayAdd(@VelocityCheck int companyID, int count);

    /**
     * Check whether the number of recipients is not critical after adding
     * the given number of recipients.
     *
     * @param companyID The id of the company to check.
     * @param count the number of recipients that should be added.
     * @return true if it is allowed to add the given number of recipients.
     */
    boolean isNearLimit(@VelocityCheck int companyID, int count);

    /**
     * Inserts new customer record in Database with a fresh customer-id
     *
     * @return true on success
     */
	int insertNewCust(Recipient cust);
	
	/**
     * Updates Customer in DB. customerID must be set to a valid id, customer-data is taken from this.customerData
     * Profile fields not listed in customer are set to <code>null</code> in database.
     *
     * @param cust customer data to be saved to database
     * 
     * @return true on success
     */
	boolean updateInDB(Recipient cust);


	/**
     * Updates Customer in DB. customerID must be set to a valid id, customer-data is taken from this.customerData
     * Depending on <code>missingFieldsToNull</code>, profile fields not listed in given customer are either untouched in 
     * database (<code>missingFieldsToNull</code> is set to <code>false</code>) or the fields are set to <code>null</code> 
     * in database (<code>missingFieldsToNull</code> is set to <code>true</code>).
     *
     * @param cust customer data to be saved to database
     * @param missingFieldsToNull control flag for handling profile fields that are not listed in given customer

     * @return true on success
     */
	boolean updateInDB(final Recipient cust, final boolean missingFieldsToNull);

    int getAllRecipientsCount(int companyID);

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
     */
    void updateStatusByColumn(@VelocityCheck int companyId, String columnName, String columnValue, int newStatus, String remark);

    /**
     * Update binding status of all subscribers with matching email address.
     * 
     * <b>Note: This method duplicates {@link BindingService#updateBindingStatusByEmailPattern(int, String, int, String)}</b>
     * 
     * @param companyId company ID
     * @param emailPattern emailPattern
     * @param newStatus new status
     * @param remark remark for status update
     * 
     * @see BindingService#updateBindingStatusByEmailPattern(int, String, int, String)
     */
    void updateStatusByEmailPattern(@VelocityCheck int companyId, String emailPattern, int newStatus, String remark);
    
    /**
     * Find Subscriber by providing the id of the company, a column-name and a value.
     *
      * @param companyID The id of the company
     * @param col Column name
     * @param value Value to search for in col
     * @return customerID or 0 if no matching record found
     */
    int findByColumn(@VelocityCheck int companyID, String col, String value);

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
    int findByUserPassword(@VelocityCheck int companyID, String userCol, String userValue, String passCol, String passValue);
    
    /**
     * Load complete Subscriber data from DB. customerID must be set first for this method.
     *
     * @param companyID The id of the company
     * @param customerID The id of the customer
     * @return Map with Key/Value-Pairs of customer data
     */
    CaseInsensitiveMap<String, Object> getCustomerDataFromDb(@VelocityCheck int companyID, int customerID);
    
    CaseInsensitiveMap<String, Object> getCustomerDataFromDb(@VelocityCheck int companyID, int customerID, final DateFormat dateFormat);

    /**
     * Load selective Subscriber data from DB. customerID must be set first for this method.
     *
     * @param companyID The id of the company
     * @param customerID The id of the customer
     * @param columns The collection of DB columns to load
     * @return Map with Key/Value-Pairs of customer data
     */
    CaseInsensitiveMap<String, Object> getCustomerDataFromDb(@VelocityCheck int companyID, int customerID, Collection<String> columns);

    /**
     * Delete complete Subscriber-Data from DB. customerID must be set first for this method.
     *
     * @param companyID The id of the company
     * @param customerID The id of the customer
     */
    void deleteCustomerDataFromDb(@VelocityCheck int companyID, int customerID);
    
    /**
     * Loads complete Mailinglist-Binding-Information for given customer-id from Database
     *
     * @param companyID The id of the company
     * @param customerID The id of the customer
     * @return Map with key/value-pairs as combinations of mailinglist-id and BindingEntry-Objects
     */
    Map<Integer, Map<Integer, BindingEntry>> loadAllListBindings(@VelocityCheck int companyID, int customerID);
    

    /**
     * Loads value of given column for given recipient
     *
     * @param selectVal The name of column in database
     * @param recipientID The id of the recipient
     * @param companyID The id of the company
     * @return String value of column named selectVal or empty String if there is not value
     */
    String getField(String selectVal, int recipientID, @VelocityCheck int companyID);

    /**
     * Loads all mailing lists for given customer
     *
     * @param customerID The id of the customer
     * @param companyID The id of the company
     * @return Map with key/value-pairs as combinations of mailinglist-id and map with key/value-pairs as combinations of mediatype and BindingEntry-Objects
     */
    Map<Integer, Map<Integer, BindingEntry>>	getAllMailingLists(int customerID, @VelocityCheck int companyID);

    /**
     * Create an empty temporary table for the given customer.
     * The table can then be used for import operations.
     *
     * @param companyID the id of the company.
     * @param datasourceID the unique id for the import operation.
     * @param status the object of the column that should be use as unique key.
     * @return true on success.
     */
    boolean createImportTables(@VelocityCheck int companyID, int datasourceID, CustomerImportStatus status);

	/**
     * Delete a temporary table that was created with createImportTables.
     *
     * @param companyID the id of the company.
     * @param datasourceID the unique id for the import operation.
     * @return true on success.
     */
    boolean deleteImportTables(@VelocityCheck int companyID, int datasourceID);

    /**
     * Load number of recipients for given condition
     *
     * @param companyID the id of the company.
     * @param target the condition for select to database
     * @return number of recipients or 0 if error
     */
    int sumOfRecipients(@VelocityCheck int companyID, String target);

    /**
     * Delete recipients from database for given condition
     *
     * @param companyID the id of the company
     * @param target the condition for delete to database
     * @return true if success or false if error
     */
    boolean deleteRecipients(@VelocityCheck int companyID, String target);

    /**
     * Delete recipients from database for given list of customer id
     *
     * @param companyID the id of the company
     * @param list the list of customer id
     */
    void deleteRecipients(@VelocityCheck int companyID, List<Integer> list);

    /**
     * Loads meta information for all columns from database for given customer
     *
     * @param companyID the id of the company
     * @return  Map with key/value-pairs as combinations of column name and CsvColInfo Objects
     */
	CaseInsensitiveMap<String, CsvColInfo> readDBColumns(@VelocityCheck int companyID);
	
    /**
     * Method gets a list of test/admin recipients for preview drop-down list
     *
     * @param companyId the id of company
     * @param mailingId id of mailing
     * @return Map in a format "recipient id" -> "recipient description (name, lastname, email)"
     */
	Map<Integer, String> getAdminAndTestRecipientsDescription(@VelocityCheck int companyId, int mailingId);

    /**
     * Method gets the first available test/admin recipient for a mailing preview generation.
     *
     * @param companyId an identifier of a company that owns referenced mailing.
     * @param mailingId an identifier of a mailing (to retrieve a recipient from its mailing list).
     * @return an identifier of the first available test/admin recipient or zero.
     */
    int getPreviewRecipient(@VelocityCheck int companyId, int mailingId);

    /**
     * Loads the list of bounced recipients for given mailing
     *
     * @param companyId the id of company
     * @param mailingId the id of mailing
     * @return the list of  Recipient objects
     */
    List<Recipient> getBouncedMailingRecipients(@VelocityCheck int companyId, int mailingId);

    /**
     * Check of existence of customer in database for given id
     *
     * @param companyId the id of company
     * @param customerId the id of customer
     * @return true if customer exist or false if not
     */
    boolean exist(int customerId, @VelocityCheck int companyId);

    void deleteRecipientsBindings(int mailinglistID, @VelocityCheck int companyID, boolean activeOnly, boolean noAdminsAndTests);

    int getCustomerIdWithEmailInMailingList(@VelocityCheck int companyId, int mailingId, String email);
    
	int getDefaultDatasourceID(String username, int companyID);
	
	void lockCustomers(int companyId, List<Integer> ids);
	
    List<Integer> getCustomerDataFromDb(int companyId, boolean matchAll, List<CriteriaEquals> criteriaEquals);
    
    int getSizeOfCustomerDataFromDbList(int companyId, boolean matchAll, List<CriteriaEquals> criteriaEquals);

    CaseInsensitiveMap<String, ProfileField> getAvailableProfileFields(@VelocityCheck int companyID) throws Exception;

	List<WebtrackingHistoryEntry> getRecipientWebtrackingHistory(int companyID, int recipientID);

	void updateForActionOperationUpdateCustomer(int companyID, String columnName, int updateType, String generateUpdateValue, int customerID) throws Exception;

	String selectCustomerValue(String selectValue, int companyID, int customerId);

    int bulkUpdateEachRecipientsFields(@VelocityCheck int companyId, int adminId, int mailingListId, String sqlTargetExpression, Map<String, Object> updateValues) throws Exception;
    
    int getRecipientsAmountForTargetGroup(int companyId, int adminId, int mailingListId, String sqlTargetExpression);

	List<Map<String, Object>> getBouncedRecipients(int companyId, Date fromDate);

	List<Map<String, Object>> getUnsubscribedRecipients(int companyID, Date fromDate);

	RecipientDates getRecipientDates(@VelocityCheck int companyId, int recipientId);

	DataSource getDataSource();

    List<Integer> insertTestRecipients(@VelocityCheck int companyId, int mailingListId, int userStatus, List<String> addresses);

    String getEmail(@VelocityCheck int companyId, int customerId);

    /**
     * Check whether a given email address in used by some recipient other that given {@code recipientId}.
     *
     * @param email an address to check.
     * @param recipientId an identifier of a recipient to ignore (or 0).
     * @param companyId an identifier of a current user's company.
     * @return {@code true} if there's a recipient (other that given recipientId) having given email or {@code false} otherwise.
     */
    boolean checkAddressInUse(String email, int recipientId, @VelocityCheck int companyId);

	void writeEmailAddressChangeRequest(final int companyID, final int customerID, final String newEmailAddress, final String confirmationCode);

	String readEmailAddressForPendingChangeRequest(final int companyID, final int customerID, final String confirmationCode);

	void deletePendingEmailAddressChangeRequest(final int companyID, final int customerID, final String confirmationCode);
}
