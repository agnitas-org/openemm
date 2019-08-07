/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.wsmanager.common;

import com.agnitas.emm.wsmanager.service.WebserviceUserService;

/**
 * Interface for webservice userdata. Extends the smaller list item interface.
 * 
 * <b>Note:</b> For security reasons, do not add a property to hold the password or secured
 * password. For modifying the users password, 
 * use{@link WebserviceUserService#updateWebserviceUser(com.agnitas.emm.core.wsmanager.dto.WebserviceUserDto)} with non-empty
 * password property defined.
 */
public interface WebserviceUser extends WebserviceUserListItem {

	/**
	 * Sets <i>active</i>-flag of webservice user. If user is not active, login is impossible.
	 * 
	 * @param active <i>active</i>-flag of user
	 */
	void setActive(final boolean active);
	
	/**
	 * Returns the contact information of the webservice user.
	 * 
	 * @return contact information
	 */
	String getContact();
	
	/**
	 * Set contact information of webservice user.
	 * 
	 * @param contact contact information
	 */
	void setContact(final String contact);
	/**
	 * Returns the contact information of the webservice user.
	 * 
	 * @return contact information
	 */
	String getContactEmail();
	
	/**
	 * Set contact information of webservice user.
	 * 
	 * @param contactEmail contact information
	 */
	void setContactEmail(final String contactEmail);
	
	
}
