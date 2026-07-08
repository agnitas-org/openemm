/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.wsmanager.service;

import java.util.List;
import java.util.Optional;

import com.agnitas.beans.Admin;
import com.agnitas.beans.PaginatedList;
import com.agnitas.emm.core.wsmanager.dto.WebserviceUserDto;
import com.agnitas.emm.core.wsmanager.dto.WebserviceUserEntryDto;
import com.agnitas.emm.core.wsmanager.form.WebserviceUserOverviewFilter;
import com.agnitas.emm.wsmanager.bean.WebserviceUserSettings;
import com.agnitas.emm.wsmanager.exception.WebserviceUserCreateException;
import com.agnitas.emm.wsmanager.exception.WebserviceUserUpdateException;

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
	 */
	WebserviceUserDto getWebserviceUserByUserName(String username);

	/**
	 * Paginated list of all webservice users (active and non-active).
	 */
    PaginatedList<WebserviceUserEntryDto> getPaginatedWSUserList(int companyID, String sort, String direction, int page, int rownums, boolean masterView);

    PaginatedList<WebserviceUserEntryDto> getPaginatedWSUserList(WebserviceUserOverviewFilter filter, Admin admin);

    /**
	 * Create or update webservice user.
	 *
	 * @param user webservice user to update
	 * @param isNew is webservice doesn't exist
	 *
	 * @throws WebserviceUserUpdateException on errors concerning the webservice user itself (like unknown username)
	 * @throws WebserviceUserCreateException on errors concerning the webservice user itself (like unknown username)
	 */
	void saveWebServiceUser(Admin admin, WebserviceUserDto user, boolean isNew);

	int getNumberOfWebserviceUsers(int companyID);
	
	Optional<WebserviceUserSettings> findSettingsForWebserviceUser(String username);

	boolean deleteWebserviceUser(String username);

	boolean webserviceUserExists(String userName);

	List<String> getUsernames(Integer companyId);

}
