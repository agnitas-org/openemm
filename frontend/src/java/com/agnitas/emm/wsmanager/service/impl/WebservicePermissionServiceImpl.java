/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.wsmanager.service.impl;

import java.util.Objects;

import com.agnitas.emm.wsmanager.bean.WebservicePermissions;
import com.agnitas.emm.wsmanager.dao.WebservicePermissionDao;
import com.agnitas.emm.wsmanager.service.WebservicePermissionService;

public final class WebservicePermissionServiceImpl implements WebservicePermissionService {

	private WebservicePermissionDao permissionDao;
	
	@Override
	public final WebservicePermissions listAllPermissions() {
		return new WebservicePermissions(this.permissionDao.listPermissions());
	}
	
	public final void setWebservicePermissionDao(final WebservicePermissionDao dao) {
		this.permissionDao = Objects.requireNonNull(dao, "Webservice permission DAO is null");
	}

}
