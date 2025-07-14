/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.wsmanager.common;

import com.agnitas.emm.wsmanager.service.WebserviceUserService;

/**
 * Interface for webservice user list items. This interface provides only information, that
 * are required in list view.
 * 
 * <b>Note:</b> For security reasons, do not add a property to hold the password or secured
 * password. For modifying the users password, 
 * use{@link WebserviceUserService#updateWebserviceUser(com.agnitas.emm.core.wsmanager.dto.WebserviceUserDto)} with non-empty
 * password property defined.

 */
public interface WebserviceUserListItem {

	/**
	 * Returns the company ID of the webservice user.
	 * 
	 * @return company ID of the webservice user
	 */
	int getCompanyID();
	
	/**
	 * Returns the username of the webservice user.
	 * 
	 * @return username of the webservice user
	 */
	String getUsername();
	
	/**
	 * Returns the default datasource ID of the webservice user.
	 * 
	 * @return default datasource ID of the webservice user
	 */
	int getDefaultDatasourceID();
	
	/**
	 * Returns the <i>active</i>-flag of the webservice user.
	 * If it is <code>null</code> login is not possible for this user.
	 * 
	 * @return <i>active</i>-flag of the webservice user
	 */
	boolean isActive();

	String getClientName();
	
}
