/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.wsmanager.dao.impl;

import com.agnitas.emm.wsmanager.bean.WebserviceUserSettings;
import com.agnitas.emm.wsmanager.dao.WebserviceUserSettingsDao;
import com.agnitas.dao.impl.BaseDaoImpl;

import java.util.List;
import java.util.Optional;

public final class WebserviceUserSettingsDaoImpl extends BaseDaoImpl implements WebserviceUserSettingsDao {
	
	@Override
	public final Optional<WebserviceUserSettings> findSettingsForWebserviceUser(final String username) {
		final String sql = "SELECT * FROM webservice_user_tbl WHERE username=?";
		
		final List<WebserviceUserSettings> list = select(sql, new WebserviceUserSettingsRowMapper(), username);

		return list.isEmpty()
				? Optional.empty()
				: Optional.of(list.get(0));
	}

}
