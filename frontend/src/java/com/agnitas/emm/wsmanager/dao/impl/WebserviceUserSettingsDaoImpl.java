/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.wsmanager.dao.impl;

import java.util.List;
import java.util.Optional;

import org.agnitas.dao.impl.BaseDaoImpl;
import org.apache.log4j.Logger;

import com.agnitas.emm.wsmanager.bean.WebserviceUserSettings;
import com.agnitas.emm.wsmanager.dao.WebserviceUserSettingsDao;

public final class WebserviceUserSettingsDaoImpl extends BaseDaoImpl implements WebserviceUserSettingsDao {
	
	/** The logger. */
	private static final transient Logger LOGGER = Logger.getLogger(WebserviceUserSettingsDaoImpl.class);

	@Override
	public final Optional<WebserviceUserSettings> findSettingsForWebserviceUser(final String username) {
		final String sql = "SELECT * FROM webservice_user_tbl WHERE username=?";
		
		final List<WebserviceUserSettings> list = select(LOGGER, sql, new WebserviceUserSettingsRowMapper(), username);

		return list.isEmpty()
				? Optional.empty()
				: Optional.of(list.get(0));
	}

}
