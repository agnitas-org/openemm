/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core;

public enum PermissionType {
	/**
	 * Permission is visible and usable for everyone
	 */
	Standard,
	
	/**
	 * Permission is visible for everyone, company_permission_tbl entry is necessary for usage
	 */
	Premium,
	
	/**
	 * Permission is only visible for normal users, if it was granted<br />
	 * SaaS: emm-master and administrative users see this permission, even if it is not granted<br />
	 * Inhouse: emm-master and administrative users only see this permission, if it is granted by license and company_permission_tbl<br />
	 * company_permission_tbl entry is necessary for usage<br />
	 */
	System,
	
	/**
	 * For migration permissions or temporary permissions. 
	 * Not visible to users, but may be granted to any user.
	 * Entry in license is not needed.
	 * Entry in company_permission_tbl is not needed
	 */
	Migration
}
