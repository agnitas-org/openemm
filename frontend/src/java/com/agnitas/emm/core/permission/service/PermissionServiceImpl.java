/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.permission.service;

import java.util.LinkedHashMap;
import java.util.List;

import com.agnitas.dao.PermissionDao;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.PermissionInfo;

public class PermissionServiceImpl implements PermissionService {

	protected PermissionDao permissionDao;

	public void setPermissionDao(PermissionDao permissionDao) {
		this.permissionDao = permissionDao;
	}
	
	@Override
	public List<Permission> getAllPermissions() {
		return permissionDao.getAllPermissions();
	}
	
	@Override
	public List<String> getAllCategoriesOrdered() {
		return permissionDao.getAllCategoriesOrdered();
	}

	@Override
	public LinkedHashMap<String, PermissionInfo> getPermissionInfos() {
		return permissionDao.getPermissionInfos();
	}
}
