/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.beans;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.Map;

import org.agnitas.emm.core.velocity.VelocityCheck;

public interface Recipient {
	int MAILTYPE_TEXT = 0;
	int MAILTYPE_HTML = 1;
	int MAILTYPE_HTML_OFFLINE = 2;
	
	int GENDER_FEMALE = 1;
	int GENDER_MALE = 0;
	int GENDER_UNKNOWN = 2;
	
	int getGender();
	String getFirstname();
	String getLastname();
	String getEmail();
	Timestamp getTimestamp();
	
	/**
     * Checks if E-Mail-Adress given in customerData-Map is valid.
     *
     * @return true if E-Mail-Adress is valid
	 *
	 * @deprecated use {@link org.agnitas.util.AgnUtils#isEmailValid(String)}
	 *
     */
	@Deprecated
	boolean emailValid();

	DateFormat getDateFormat();

    /**
     * Checks if E-Mail-Adress given in customerData-Map is registered in blacklist(s)
     *
     * @return true if E-Mail-Adress is blacklisted
     */
	boolean blacklistCheck();

    /**
     * Find Subscriber by providing a column-name and a value. Only exact machtes possible.
     *
     * @return customerID or 0 if no matching record found
     * @param col Column-Name
     * @param value Value to search for in col
     */
	int findByKeyColumn(String col, String value);

    /**
     * Find Subscriber by providing a column-name and a value. Only exact machtes possible.
     *
     * @return customerID or 0 if no matching record found
     * @param col Column-Name
     * @param value Value to search for in col
     */
	int findByColumn(String col, String value);

    /**
     * Find Subscriber by providing a username and password. Only exact machtes possible.
     *
     * @return customerID or 0 if no matching record found
     * @param userCol Column-Name for Username
     * @param userValue Value for Username
     * @param passCol Column-Name for Password
     * @param passValue Value for Password
     */
	int findByUserPassword(String userCol, String userValue, String passCol, String passValue);

    /**
     * Getter for property companyID.
     *
     * @return Value of property companyID.
     */
	int getCompanyID();

    /**
     * Getter for property custDBStructure.
     *
     * @return Value of property custDBStructure.
     */
	Map<String, String> getCustDBStructure();

    /**
     * Getter for property custParameters.
     *
     * @return Value of property custParameters.
     */
	Map<String, Object> getCustParameters();

    /**
     * Indexed getter for property custParameters.
     * If profile field is unknown, an empty string is returned.
     *
     * @return Value of the property at <CODE>key</CODE>.
     * @param key Name of Database-Field
     */
	String getCustParametersNotNull(String key);

	@Deprecated
	String getCustParameters(String key);

	/**
	 * Checks, if customer has given profile field.
	 *
	 * @param key name of profileField
	 *
	 * @return true, if customer has profile field set
	 */
	boolean hasCustParameter(String key);

    /**
     * Load complete Subscriber-Data from DB. customerID must be set first for this method.
     *
     * @return Map with Key/Value-Pairs of customer data
     */
	Map<String, Object> getCustomerDataFromDb();

    /**
     * Delete complete Subscriber-Data from DB. customerID must be set first for this method.
     */
	void deleteCustomerDataFromDb();

    /**
     * Getter for property customerID.
     *
     * @return Value of property customerID.
     */
	int getCustomerID();

    /**
     * Getter for property listBindings.
     *
     * @return Value of property listBindings.
     */
	Map<Integer, Map<Integer, BindingEntry>> getListBindings();

    /**
     * Updates customer data by analyzing given HTTP-Request-Parameters.
     *
     * @return true on success
	 * @param requestParameters Map containing all HTTP-Request-Parameters as key-value-pair.
	 * @param suffix Suffix appended to Database-Column-Names when searching for corresponding request parameters
     */
	boolean importRequestParameters(Map<String, Object> requestParameters, String suffix);

    /**
     * Inserts new customer record in Database with a fresh customer-id.
     * Use {@value #insertNewCustWithException()} instead.
     *
     * @return true on success
     */
	/*
	 * TODO Move to Velocity API class (EMM-8360) and remove from this class.
	 * 
	 * Do not modify signature. This is part of the API used by Velocity scripts.
	 * Do not remove method before completing EMM-8360!!!
	 */
	@Deprecated // Do not remove method before completing EMM-8360!!!
	int insertNewCust();
	
    /**
     * Inserts new customer record in Database with a fresh customer-id.
     *
     * @return true on success
     * 
     * @throws Exception on inserting new customer
     */
	int insertNewCustWithException() throws Exception;

    /**
     * Iterates through already loaded Mailinglist-Informations and checks if subscriber is active on at least one mailinglist.
     *
     * @return true if subscriber is active on a mailinglist
     */
	boolean isActiveSubscriber();

    /**
     * Loads complete Mailinglist-Binding-Information for given customer-id from Database.
     *
     * @return Map with key/value-pairs as combinations of mailinglist-id and BindingEntry-Objects
     */
	Map<Integer, Map<Integer, BindingEntry>> loadAllListBindings();

    /**
     * Load structure of Customer-Table for the given Company-ID in member variable "companyID".
     * Has to be done before working with customer-data in class instance.
     *
     * @return true on success
     */
	boolean loadCustDBStructure();

    /**
     * resets internal customer-parameter hashmap.
     */
	void resetCustParameters();

    /**
     * Setter for property companyID.
     *
     * @param companyID New value of property companyID.
     */
	void setCompanyID(@VelocityCheck int companyID);

    /**
     * Setter for property custDBStructure.
     *
     * @param custDBStructure New value of property custDBStructure.
     */
	void setCustDBStructure(Map<String, String> custDBStructure);

    /**
     * Setter for property custParameters.
     *
     * @param custParameters New value of property custParameters.
     */
	void setCustParameters(Map<String, Object> custParameters);

    /**
     * Indexed setter for property custParameters.
     *
     * @param aKey identifies field in customer-record, must be the same like in Database
     * @param custParameters New value of the property at <CODE>aKey</CODE>.
     */
	void setCustParameters(String aKey, String custParameters);

    /**
     * Setter for property customerID.
     *
     * @param customerID New value of property customerID.
     */
	void setCustomerID(int customerID);

    /**
     * Setter for property listBindings.
     *
     * @param listBindings New value of property listBindings.
     */
	void setListBindings(Map<Integer, Map<Integer, BindingEntry>> listBindings);

    /**
     * Updates internal Datastructure for Mailinglist-Bindings of this customer
     * by analyzing HTTP-Request-Parameters.
     *
     * @param params Map containing all HTTP-Request-Parameters as
     *               key-value-pair.
     * @param doubleOptIn true means use Double-Opt-In
     * @param remoteAddr IP-address of the client when subscribing.
     * @return true on success
     * @throws Exception
     */
	void updateBindingsFromRequest(Map<String, Object> params, boolean doubleOptIn, String remoteAddr, String referrer) throws Exception;

	void updateBindingsFromRequest(Map<String, Object> params, boolean doubleOptIn) throws Exception;

	/**
 	 * function of tafWriteBack was removed with TellaFriend feature (EMM-5308)
	 *
	 * @deprecated useupdateBindingsFromRequest(Map<String, Object> params, boolean doubleOptIn, String remoteAddr) instead.
	 */
	@Deprecated
	void updateBindingsFromRequest(Map<String, Object> params, boolean doubleOptIn, boolean tafWriteBack, String remoteAddr, String referrer) throws Exception;
	/**
 	 * function of tafWriteBack was removed with TellaFriend feature (EMM-5308)
	 *
	 * @deprecated updateBindingsFromRequest(Map<String, Object> params, boolean doubleOptIn) instead.
	 */
	@Deprecated
	void updateBindingsFromRequest(Map<String, Object> params, boolean doubleOptIn, boolean tafWriteBack) throws Exception;

    /**
     * Updates Customer in DB. customerID must be set to a valid id, customer-data is taken from this.customerData.
     * Use {@link #updateInDbWithException()} instead.
     * 
     * @return true on success
     * @throws Exception
     * 
     * @see #updateInDbWithException()
     */
	/*
	 * TODO Move to Velocity API class (EMM-8360) and remove from this class.
	 * 
	 * Do not modify signature. This is part of the API used by Velocity scripts.
	 * Do not remove method before completing EMM-8360!!!
	 */
	@Deprecated
	boolean updateInDB(); 
	
	/**
     * Updates customer data in DB.
     * customerID must be set to a valid id.
     * 
     * @return <code>true</code> on success
     * 
     * @throws Exception on errors updating data
     */
	boolean updateInDbWithException() throws Exception;

	Map<Integer, Map<Integer, BindingEntry>> getAllMailingLists();

	boolean isSecure(String value);

	boolean copyDateFromRequest(Map<String, Object> req, String name, String suffix);

	boolean isAllowedName(String name);

	boolean isChangeFlag();

	void setChangeFlag(boolean changeFlag);

	void setDateFormatForProfileFieldConversion(final DateFormat ddMmYyyyHhMmSs);

	boolean isDoNotTrackMe();

	void setDoNotTrackMe(final boolean doNotTrack);

	BindingEntry getBindingsByMailinglist(int mailinglistId, int index);
}
