/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.sessionhijacking.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.agnitas.emm.core.sessionhijacking.beans.IpSettings;

final class IpSettingsRowMapper implements RowMapper<IpSettings> {

	@Override
	public final IpSettings mapRow(final ResultSet rs, final int row) throws SQLException {
		final String ip = rs.getString("ip");
		final int group = rs.getInt("ip_group");
		final boolean groupIsNull = rs.wasNull();

		return new IpSettings(ip, groupIsNull ? null : group);
	}

}
