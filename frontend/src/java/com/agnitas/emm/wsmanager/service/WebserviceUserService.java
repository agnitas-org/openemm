/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.wsmanager.service;

import org.agnitas.beans.impl.PaginatedListImpl;

import com.agnitas.beans.ComAdmin;
import com.agnitas.emm.core.wsmanager.dto.WebserviceUserDto;
import com.agnitas.emm.core.wsmanager.dto.WebserviceUserEntryDto;
import com.agnitas.emm.wsmanager.bean.WebserviceUserSettings;
import com.agnitas.emm.wsmanager.common.WebserviceUserException;

/**
 * Service-layer interface for webservice users.
 */
public interface WebserviceUserService {
	
	/**
	 * Returns the data of given webservice user.
	 *
	 * @param username username
	 *
	 * @return user WebserviceUserDto
	 *
	 * @throws WebserviceUserServiceException on errors concerning the webservice user itself (like unknown username)
	 * @throws WebserviceUserException on errors during processing the request
	 */
	WebserviceUserDto getWebserviceUserByUserName(String username) throws WebserviceUserServiceException, WebserviceUserException;

	/**
	 * Creates a new webservice user.
	 *
	 * @param user WebserviceUserDto data
	 *
	 * @throws WebserviceUserServiceException on errors concerning the webservice user itself (like duplicate username)
	 * @throws WebserviceUserException on errors during processing the request
	 */
    void createWebserviceUser(WebserviceUserDto user) throws WebserviceUserException, WebserviceUserServiceException;

	/**
	 * Updates given webservice user.
	 * 
	 * @param user webservice user to update
	 *
	 * @throws WebserviceUserServiceException on errors concerning the webservice user itself (like unknown username)
	 * @throws WebserviceUserException on errors during processing the request
	 */
    void updateWebserviceUser(WebserviceUserDto user) throws WebserviceUserException, WebserviceUserServiceException;

	/**
	 * Paginated list of all webservice users (active and non-active).
	 *
	 * @throws WebserviceUserServiceException on errors processing the request
	 */
    PaginatedListImpl<WebserviceUserEntryDto> getPaginatedWSUserList(int companyID, String sort, String direction, int page, int rownums, boolean masterView) throws WebserviceUserServiceException;

    /**
	 * Create or update webservice user.
	 *
	 * @param user webservice user to update
	 * @param isNew is webservice doesn't exist
	 *
	 * @throws WebserviceUserServiceException on errors concerning the webservice user itself (like unknown username)
	 * @throws WebserviceUserException on errors during processing the request
	 */
	void saveWebServiceUser(ComAdmin admin, WebserviceUserDto user, boolean isNew) throws WebserviceUserException, WebserviceUserServiceException;

	int getNumberOfWebserviceUsers();
	
	public WebserviceUserSettings findSettingsForWebserviceUser(final String username) throws WebserviceUserException, WebserviceUserServiceException;

	public void updateLastLoginDate(final String username);
}
