/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.wsmanager.common.impl;

import java.util.HashSet;
import java.util.Set;

import com.agnitas.emm.wsmanager.common.WebserviceUser;

/**
 * Implementation of the {@link WebserviceUser} interface.
 */
public class WebserviceUserImpl extends WebserviceUserListItemImpl implements WebserviceUser {
	
	/** Contact information. */
	private String contact;
	private String contactEmail;
	
	private Set<String> grantedPermissions;
	private Set<Integer> grantedPermissionGroups;
	
	public WebserviceUserImpl() {
		setGrantedPermissions(null);
		setGrantedPermissionGroupIDs(null);
	}

	@Override
	public String getContact() {
		return this.contact;
	}

	@Override
	public void setContact(final String contact) {
		this.contact = contact;
	}
	
	@Override
	public String getContactEmail() {
		return this.contactEmail;
	}

	@Override
	public void setContactEmail(final String contactEmail) {
		this.contactEmail = contactEmail;
	}
	
	@Override
	public final void setGrantedPermissions(final Set<String> permissions) {
		this.grantedPermissions = permissions != null ? permissions : new HashSet<>();
	}
	
	@Override
	public final Set<String> getGrantedPermissions() {
		return this.grantedPermissions;
	}
	
	
	@Override
	public final void setGrantedPermissionGroupIDs(final Set<Integer> permissionGroups) {
		this.grantedPermissionGroups = permissionGroups != null ? permissionGroups : new HashSet<>();
	}
	
	@Override
	public final Set<Integer> getGrantedPermissionGroupIDs() {
		return this.grantedPermissionGroups;
	}

}
