/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.permission.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.dao.PermissionDao;
import com.agnitas.emm.core.Permission;

public class PermissionServiceImpl implements PermissionService {
	@SuppressWarnings("unused")
	private static final transient Logger logger = Logger.getLogger(PermissionServiceImpl.class);

	private static Map<Permission, String> CATEGORY_BY_SYSTEM_PERMISSIONS_NEW = null;

	protected PermissionDao permissionDao;

	@Required
	public void setPermissionDao(PermissionDao permissionDao) {
		this.permissionDao = permissionDao;
	}

	@Override
	public List<Permission> getPermissionsByCategory(String category) {
		return permissionDao.getPermissionsByCategory(category);
	}
	
	@Override
	public List<Permission> getAllPermissions() {
		return permissionDao.getAllPermissions();
	}
	
	/**
	 * Feature toggle
	 */
	@Override
	public Map<Permission, String> getAllPermissionsAndCategories(int companyID) {
		if ("new".equalsIgnoreCase(ConfigService.getInstance().getValue(ConfigValue.PermissionSystem, companyID))) {
			return getAllPermissionsAndCategoriesNew();
		} else {
			return Permission.getAllPermissionsAndCategories();
		}
	}
	
	private Map<Permission, String> getAllPermissionsAndCategoriesNew() {
		if (CATEGORY_BY_SYSTEM_PERMISSIONS_NEW == null) {
			List<Permission> permissions = getAllPermissions();
			Map<Permission, String> categoryBySystemPermissions = new HashMap<>();
			
			for (Permission permission : permissions) {
				categoryBySystemPermissions.put(permission, permission.getCategory());
			}
	
			CATEGORY_BY_SYSTEM_PERMISSIONS_NEW = categoryBySystemPermissions;
		}
		return CATEGORY_BY_SYSTEM_PERMISSIONS_NEW;
	}
}
