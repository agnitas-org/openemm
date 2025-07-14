/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.velocity.emmapi;

import java.util.Date;

public interface VelocityBindingEntry {

    /**
     * Getter for property mailinglistID.
     *
     * @return Value of property mailinglistID.
     */
	int getMailinglistID();

    /**
     * Setter for property mailinglistID.
     * 
     * @param ml New value of property mailinglistID.
     */
	void setMailinglistID(final int mailinglistID);

    /**
     * Getter for property customerID.
     *
     * @return Value of property customerID.
     */
	int getCustomerID();
	
    /**
     * Setter for property customerID.
     * 
     * @param ci New value of property customerID.
     */
	void setCustomerID(final int customerID);
	
    /**
     * Getter for property mediaType.
     * 
     * @return Value of property mediaType.
     */
	int getMediaType();

    /**
     * Setter for property mediaType.
     * 
     * @param mediaType New value of property mediaType.
     */
	void setMediaType(final int mediaType);

    /**
     * Getter for property exitMailingID.
     * 
     * @return Value of property exitMailingID.
     */
	int getExitMailingID();

    /**
     * Setter for property exitMailingID.
     * 
     * @param mi New value of property exitMailingID.
     */
	void setExitMailingID(final int mailingID);

    /**
     * Getter for property userType.
     * 
     * @return Value of property userType.
     */
	String getUserType();

    /**
     * Setter for property userType.
     * 
     * @param ut New value of property userType.
     */
	void setUserType(final String ut);

    /**
     * Getter for property userStatus.
     * 
     * @return Value of property userStatus.
     */
	int getUserStatus();

    /**
     * Setter for property userStatus.
     *
     * @param us New value of property userStatus.
     */
	void setUserStatus(final int us);

    /**
     * Getter for property userRemark.
     * 
     * @return Value of property userRemark.
     */
	String getUserRemark();

    /**
     * Setter for property userRemark.
     * 
     * @param remark New value of property userRemark.
     */
	void setUserRemark(final String remark);

    /**
     * Getter for property changeDate.
     * 
     * @return Value of property changeDate.
     */
	Date getChangeDate();
	
	boolean getUserBindingFromDB(final int companyID);

    /**
     * Inserts this Binding in the Database
     * 
     * @param companyID The company ID of the Binding
     * @return true on Sucess, false otherwise.
     */
	boolean insertNewBindingInDB(final int companyID);
    
    /**
     * Mark binding as opted out.
     * 
     * @param email Emailaddress to set inactive.
     * @param companyID The company ID of the Binding
     * @return true if binding is active on the mailinglist, false otherwise.
     */
	boolean optOutEmailAdr(final String email, final int companyID);

    /**
     * Setter for property changeDate.
     * 
     * @param ts New value of property changeDate.
     */
	void setChangeDate(final Date ts);

    /**
     * Updates this Binding in the Database
     * 
     * @param companyID The company ID of the Binding
     * @return true on Sucess, false otherwise.
     */
	boolean updateBindingInDB(final int companyID);

	boolean updateStatusInDB(int companyID);

}
