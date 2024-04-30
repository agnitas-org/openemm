/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.beans;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.antlr.v4.runtime.misc.Nullable;

import com.agnitas.beans.Admin;
import com.agnitas.dao.ComBindingEntryDao;

/**
 * Bean representing the Status of a recipient on a mailinglist
 */
public interface BindingEntry extends Serializable {

	enum UserType {
		/** deprecated, do not use **/
		ALL("E", "All", false),
		
    	Admin("A", "Administrator", false),
    	TestUser("T", "Test recipient", false),
    	TestVIP("t", "Test VIP", true),
    	World("W", "Normal recipient", false),
    	WorldVIP("w", "Normal VIP recipient", true);
    	
    	private String typeCode;
		private String readableName;
		private boolean isVip;

    	UserType(String typeCode, String readableName, boolean isVip) {
    		this.typeCode = typeCode;
			this.readableName = readableName;
			this.isVip = isVip;
    	}
    	
    	public String getTypeCode() {
    		return typeCode;
    	}

		public String getReadableName() {
			return readableName;
		}

		public boolean isVip() {
			return isVip;
		}

		public static UserType getUserTypeByString(String typeCode) throws Exception {
    		for (UserType userType : UserType.values()) {
    			if (userType.typeCode.equals(typeCode)) {
    				return userType;
    			}
    		}
    		throw new Exception("Invalid UserType: " + typeCode);
    	}

    	@Nullable
		public static String getReadableNameByCode(String typeCode){
			Optional<UserType> readableNameOptional = Arrays.stream(UserType.values())
					.filter(recipientType -> recipientType.getTypeCode().equals(typeCode))
					.findFirst();

			return readableNameOptional.isPresent() ? readableNameOptional.get().getReadableName() : null;
		}

		public static List<UserType> getVipTypes() {
    		return Stream.of(UserType.values())
					.filter(UserType::isVip)
					.collect(Collectors.toList());
		}
    }

    /**
     * Getter for property customerID.
     *
     * @return Value of property customerID.
     */
	int getCustomerID();

    /**
     * Getter for property mailinglistID.
     *
     * @return Value of property mailinglistID.
     */
	int getMailinglistID();

    /**
     * Getter for property userType.
     * 
     * @return Value of property userType.
     */
	String getUserType();

    /**
     * Getter for property userStatus.
     * 
     * @return Value of property userStatus.
     */
	int getUserStatus();		// TODO Change return type to org.agnitas.dao.UserStatus

    /**
     * Getter for property userRemark.
     * 
     * @return Value of property userRemark.
     */
	String getUserRemark();

    /**
     * Getter for property changeDate.
     * 
     * @return Value of property changeDate.
     */
	Date getChangeDate();

    /**
     * Getter for property exitMailingID.
     * 
     * @return Value of property exitMailingID.
     */
	int getExitMailingID();
	
	int getEntryMailingID();

    /**
     * Getter for property mediaType.
     * 
     * @return Value of property mediaType.
     */
	int getMediaType();

    /**
     * Setter for property customerID.
     * 
     * @param ci New value of property customerID.
     */
	void setCustomerID(int customerID);

    /**
     * Setter for property exitMailingID.
     * 
     * @param mi New value of property exitMailingID.
     */
	void setExitMailingID(int mailingID);
	
	void setEntryMailingID(int mailingID);

    /**
     * Setter for property mailinglistID.
     * 
     * @param ml New value of property mailinglistID.
     */
	void setMailinglistID(int mailinglistID);

    /**
     * Setter for property mediaType.
     * 
     * @param mediaType New value of property mediaType.
     */
	void setMediaType(int mediaType);

    /**
     * Setter for property userRemark.
     * 
     * @param remark New value of property userRemark.
     */
	void setUserRemark(String remark);

    /**
     * Setter for property changeDate.
     * 
     * @param ts New value of property changeDate.
     */
	void setChangeDate(Date ts);

    /**
     * Setter for property userStatus.
     *
     * @param us New value of property userStatus.
     */
	void setUserStatus(int us);	// TODO Change parameter to org.agnitas.dao.UserStatus

    /**
     * Setter for property userType.
     * 
     * @param ut New value of property userType.
     */
	void setUserType(String ut);

    /**
     * Inserts this Binding in the Database
     * 
     * @param companyID The company ID of the Binding
     * @return true on Sucess, false otherwise.
     */
	boolean insertNewBindingInDB(int companyID);

    /**
     * Updates this Binding in the Database
     * 
     * @param companyID The company ID of the Binding
     * @return true on Sucess, false otherwise.
     */
	boolean updateBindingInDB(int companyID);

    /**
     * Updates or Creates this Binding in the Database
     * 
     * @param companyID The company ID of the Binding
     * @param allCustLists bindings to check for save/update.
     * @return true on Sucess, false otherwise.
     * @throws Exception
     */
	boolean saveBindingInDB(int companyID, Map<Integer, Map<Integer, BindingEntry>> allCustLists, Admin admin) throws Exception;
    
    /**
     * Mark binding as opted out.
     * 
     * @param email Emailaddress to set inactive.
     * @param companyID The company ID of the Binding
     * @return true if binding is active on the mailinglist, false otherwise.
     */
	boolean optOutEmailAdr(String email, int companyID);

	boolean getUserBindingFromDB(int companyID);

    void setBindingEntryDao(ComBindingEntryDao bindingEntryDao);

    ComBindingEntryDao getBindingEntryDao();

    Date getCreationDate();

    void setCreationDate(Date creationDate);

	String getReferrer();

	void setReferrer(String referrer);

	/**
	 * @Deprecated: Only used by Velocity scripts. To be replaced by some ScriptHelper method.
	 */
	@Deprecated
	boolean updateStatusInDB(int companyID);
}
