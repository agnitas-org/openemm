/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.wsmanager.dao.impl;

import java.util.List;

import org.agnitas.dao.impl.BaseDaoImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.agnitas.emm.wsmanager.bean.WebservicePermissionGroup;
import com.agnitas.emm.wsmanager.dao.WebservicePermissionGroupDao;

public final class WebservicePermissionGroupDaoImpl extends BaseDaoImpl implements WebservicePermissionGroupDao {

	/** The logger. */
	private static final transient Logger LOGGER = LogManager.getLogger(WebservicePermissionGroupDaoImpl.class);
	
	@Override
	public final List<WebservicePermissionGroup> listAllPermissionGroups() {
		final String sql = "SELECT * FROM webservice_perm_group_tbl";
		
		return select(LOGGER, sql, new WebservicePermissionGroupRowMapper());
		
	}

}
