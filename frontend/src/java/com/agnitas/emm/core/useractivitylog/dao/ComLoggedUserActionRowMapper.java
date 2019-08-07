/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.useractivitylog.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import org.agnitas.emm.core.useractivitylog.LoggedUserAction;
import org.springframework.jdbc.core.RowMapper;

/**
 * Implementatin of {@link RowMapper} for conversion of {@link ResultSet} to {@link LoggedUserAction}
 */
class ComLoggedUserActionRowMapper implements RowMapper<LoggedUserAction> {
	@Override
	public LoggedUserAction mapRow(ResultSet rs, int row) throws SQLException {
		Timestamp date = rs.getTimestamp( "logtime");
		String username = rs.getString( "username");
		String supervisorName = rs.getString( "supervisor_name");
		String action = rs.getString( "action");
		String description = rs.getString( "description");

		return new ComLoggedUserAction(action, description, username, supervisorName, new Date(date.getTime()));
	}
}
