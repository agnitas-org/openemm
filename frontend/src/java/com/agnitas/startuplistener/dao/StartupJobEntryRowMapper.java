/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.startuplistener.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.agnitas.startuplistener.common.JobState;
import com.agnitas.startuplistener.common.StartupJobEntry;
import com.agnitas.util.Version;

final class StartupJobEntryRowMapper implements RowMapper<StartupJobEntry> {

	@Override
	public final StartupJobEntry mapRow(final ResultSet resultSet, final int row) throws SQLException {
		final int id = resultSet.getInt("id");
		final String versionString = resultSet.getString("version");
		final String classname = resultSet.getString("classname");
		final int companyId = resultSet.getInt("company_id");
		final boolean enabled = resultSet.getInt("enabled") == 1;
		final int stateCode = resultSet.getInt("state");
		
		final Version version = parseVersion(versionString);
		final JobState state = parseJobState(stateCode);
			
		return new StartupJobEntry(id, version, classname, companyId, enabled, state);
	}
	
	private static final Version parseVersion(final String s) throws SQLException {
		try {
			return new Version(s);
		} catch(final Exception e) {
			throw new SQLException(String.format("Unable to parse version string '%s'", s), e);
		}
	}

	private static final JobState parseJobState(final int stateCode) throws SQLException {
		try {
			return JobState.fromCode(stateCode);
		} catch(final Exception e) {
			throw new SQLException(String.format("Unable to parse job state '%s'", stateCode), e);
		}

	}
}
