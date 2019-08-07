/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.wsmanager.common.impl;

import com.agnitas.emm.wsmanager.common.WebserviceUserListItem;

/**
 * Implementation of the {@link WebserviceUserListItem} interface.
 * 
 * The <i>active</i>-flag indicates, whether the webservice user can login or not. 
 */
public class WebserviceUserListItemImpl implements WebserviceUserListItem {

	/** Company ID of webservice user. */
	private int companyID;
	
	/** Default datasource ID: */
	private int defaultDatasourceID;
	
	/** Username. */
	private String username;
	
	/** <i>active</i>-flag of user. */
	private boolean active;
	
	@Override
	public int getCompanyID() {
		return this.companyID;
	}
	
	/**
	 * Set company ID of webservice user.
	 * 
	 * @param companyID company ID of webservice user
	 */
	public void setCompanyID(final int companyID) {
		this.companyID = companyID;
	}

	@Override
	public String getUsername() {
		return this.username;
	}
	
	/**
	 * Set username of webservice user.
	 * @param username username of webservice user
	 */
	public void setUsername(final String username) {
		this.username = username;
	}

	@Override
	public int getDefaultDatasourceID() {
		return this.defaultDatasourceID;
	}

	/**
	 * Set default datasource ID of webservice user.
	 * 
	 * @param defaultDatasourceID default datasource ID of webservice user
	 */
	public void setDefaultDatasourceID(final int defaultDatasourceID) {
		this.defaultDatasourceID = defaultDatasourceID;
	}
	
	@Override
	public boolean isActive() {
		return this.active;
	}
	
	/**
	 * Set <i>active</i>-flag of webservice user.
	 * 
	 * @param active <i>active</i>-flag of webservice user
	 */
	public void setActive(final boolean active) {
		this.active = active;
	}
}
