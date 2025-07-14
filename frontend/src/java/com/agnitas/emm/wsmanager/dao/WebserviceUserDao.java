/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.wsmanager.dao;

import com.agnitas.emm.core.wsmanager.form.WebserviceUserOverviewFilter;
import com.agnitas.beans.impl.PaginatedListImpl;

import com.agnitas.emm.wsmanager.common.WebserviceUser;
import com.agnitas.emm.wsmanager.common.WebserviceUserCredential;
import com.agnitas.emm.wsmanager.common.WebserviceUserException;
import com.agnitas.emm.wsmanager.common.WebserviceUserListItem;

/**
 * DAO interface for accessing webservice user data.
 */
public interface WebserviceUserDao {

	/**
	 * Checks, if a webservice user with given name (case-sensitive) exists.
	 * 
	 * @param username name of webservice user
	 * 
	 * @return <code>true</code> if name is already in use.
	 * 
	 */
	boolean webserviceUserExists(final String username);

	/**
	 * Returns the data of given webservice user.
	 * 
	 * @param username name of webservice user
	 * 
	 * @return {@link WebserviceUser} for given username
	 * 
	 * @throws WebserviceUserException on errors concerning the webservice user itself (like unknown name)
	 */
	WebserviceUser getWebserviceUser(final String username) throws WebserviceUserException;

	/**
	 * Updates the given webservice user. This method does neither update the password hash nor the username!
	 * 
	 * @param user new webservice user data
	 * 
	 * @throws WebserviceUserDaoException on any errors accessing user data
	 */
	void updateUser(final WebserviceUser user) throws WebserviceUserDaoException;

	/**
	 * Updates the password hash of the given webservice user.
	 * 
	 * @param username username of webservice user
	 * @param passwordHash new password hash
	 * 
	 * @throws WebserviceUserDaoException on any errors accessing user data
	 */
	void updatePasswordHash(String username, String passwordHash) throws WebserviceUserDaoException;

	/**
	 * Creates a new webservice user with given data.
	 * 
	 * @param user WebserviceUserCredential
	 * @param dataSourceId default datasource ID
	 * @param bulkSizeLimit size limit for bulk webservices
	 * 
	 * @throws WebserviceUserDaoException on any errors accessing user data
	 * @throws WebserviceUserException on errors concerning the webservice user itself (like duplicate names)
	 */
    void createWebserviceUser(WebserviceUserCredential user, int dataSourceId, int bulkSizeLimit) throws WebserviceUserException, WebserviceUserDaoException;

	/**
	 * Lists all webservice user (active and non-active users).
	 *
	 * @throws WebserviceUserDaoException on any errors accessing user data
	 */
	PaginatedListImpl<WebserviceUserListItem> getWebserviceUserList(int companyID, String sortColumn, boolean sortDirectionAscending, int pageNumber, int pageSize) throws WebserviceUserDaoException;
	PaginatedListImpl<WebserviceUserListItem> getWebserviceUserMasterList(String sortColumn, boolean sortDirectionAscending, int pageNumber, int pageSize) throws WebserviceUserDaoException;

	PaginatedListImpl<WebserviceUserListItem> getWebserviceUserList(WebserviceUserOverviewFilter filter) throws WebserviceUserDaoException;

	int getNumberOfWebserviceUsers(int companyID);

	void saveGrantedPermissionsAndGroups(WebserviceUser user);

	boolean deleteWebserviceUser(String username);
}
