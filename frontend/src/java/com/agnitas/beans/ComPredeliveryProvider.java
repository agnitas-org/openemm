/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.beans;

import java.io.Serializable;

public interface ComPredeliveryProvider extends Serializable {
	/**
	 * Getter for property providerID.
	 * 
	 * @return Value of property providerID.
	 */
	String	getProviderID();

	/**
	 * Setter for property providerID.
	 * 
	 * @param id New value of property providerID.
	 */
	void setProviderID(String id);

	/**
	 * Getter for property email.
	 * 
	 * @return Value of property email.
	 */
	String	getEmail();

	/**
	 * Setter for property email.
	 * 
	 * @param email New value of property email.
	 */
	void setEmail(String email);

	/**
	 * Getter for property active.
	 * 
	 * @return Value of property active.
	 */
	boolean	getActive();

	/**
	 * Setter for property active.
	 * 
	 * @param active New value of property active.
	 */
	void setActive(boolean active);

	/**
	 * Getter for property received.
	 * 
	 * @return Value of property received.
	 */
	java.util.Date	getReceived();

	/**
	 * Setter for property received.
	 * 
	 * @param received New value of property received.
	 */
	void setReceived(java.util.Date received);

	/**
	 * Getter for property spam.
	 * 
	 * @return Value of property spam.
	 */
	boolean	getSpam();

	/**
	 * Setter for property spam.
	 * 
	 * @param spam New value of property spam.
	 */
	void setSpam(boolean spam);


}
