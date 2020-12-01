/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.permission.service;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.dao.PermissionDao;
import com.agnitas.emm.core.Permission;

public class PermissionServiceImpl implements PermissionService {
	@SuppressWarnings("unused")
	private static final transient Logger logger = Logger.getLogger(PermissionServiceImpl.class);

	protected PermissionDao permissionDao;

	@Required
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
}
