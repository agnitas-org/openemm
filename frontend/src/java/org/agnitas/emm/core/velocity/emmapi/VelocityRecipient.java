/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.velocity.emmapi;

import java.util.Map;

import com.agnitas.beans.BindingEntry;

/**
 * This interface represents an API for use in Velocity.
 * 
 * Modify with care!
 * 
 * <b>DO NOT BREAK VELOCITY FUNCTIONALITY!</b>
 */
// NOTE getNewCustomerID was listed in manual, but method does not exist
public interface VelocityRecipient {
	
	int getCustomerID();
	
	int getGender();
	
	String getFirstname();
	
	String getLastname();
	
	String getEmail();
	
	/**
	 * @throws AccessToCompanyDeniedException if running company ID is not allowed to access company of this recipient.
	 */
	int findByKeyColumn(final String col, final String value);
	
	/**
	 * @throws AccessToCompanyDeniedException if running company ID is not allowed to access company of this recipient.
	 */
	Map<String, Object> getCustomerDataFromDb();
	
	/**
	 * @throws AccessToCompanyDeniedException if running company ID is not allowed to access company of this recipient.
	 */
	int insertNewCust();
	
	boolean importRequestParameters(final Map<String, Object> requestParameters, final String suffix);
	
	/**
	 * @throws AccessToCompanyDeniedException if running company ID is not allowed to access company of this recipient.
	 */
	Map<Integer, Map<Integer, BindingEntry>> loadAllListBindings();
	
	/**
	 * @throws AccessToCompanyDeniedException if running company ID is not allowed to access company of this recipient.
	 */
	boolean loadCustDBStructure();
	
	void resetCustParameters();
	
	void setCompanyID(final int companyID);
	
	void setCustomerID(int customerID);
	
	/**
	 * @throws AccessToCompanyDeniedException if running company ID is not allowed to access company of this recipient.
	 */
	boolean updateInDB(); 

    /**
     * Delete complete Subscriber-Data from DB. customerID must be set first for this method.
     */
	void deleteCustomerDataFromDb();
	
	String getCustParameters(String key);
	Map<String, Object> getCustParameters();

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

	@Override
	String toString();
}
