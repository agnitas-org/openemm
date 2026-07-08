/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.serverprio.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.springframework.jdbc.core.RowMapper;

import com.agnitas.emm.core.serverprio.bean.ServerPrio;

final class ServerPrioRowMapper implements RowMapper<ServerPrio> {

	@Override
	public final ServerPrio mapRow(final ResultSet rs, final int row) throws SQLException {
		final int companyID = rs.getInt("company_id");
		final int mailingID = rs.getInt("mailing_id");
		final int prio = rs.getInt("priority");
		final Integer prioOrNull = rs.wasNull() ? null : prio;
		
		final Date startDateOrNull = rs.getTimestamp("start_date");
		final Date endDateOrNull = rs.getTimestamp("end_date");
		
		return new ServerPrio(companyID, mailingID, prioOrNull, startDateOrNull, endDateOrNull);
	}

}
