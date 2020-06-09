/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.wsmanager.dao.impl;

import java.util.List;

import org.agnitas.dao.impl.BaseDaoImpl;
import org.apache.log4j.Logger;

import com.agnitas.emm.wsmanager.bean.WebservicePermission;
import com.agnitas.emm.wsmanager.dao.WebservicePermissionDao;

public final class WebservicePermissionDaoImpl extends BaseDaoImpl implements WebservicePermissionDao {

	/** The logger .*/
	private static final transient Logger LOGGER = Logger.getLogger(WebservicePermissionDaoImpl.class);
	
	@Override
	public final List<WebservicePermission> listPermissions() {
		final String sql = "SELECT * FROM webservice_permissions_tbl";
		
		return select(LOGGER, sql, new WebservicePermissionRowMapper());		
	}

}
