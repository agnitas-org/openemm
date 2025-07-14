/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.responseheaders.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;

import org.springframework.jdbc.core.RowMapper;

import com.agnitas.emm.responseheaders.common.HttpHeaderConfig;
import com.agnitas.emm.responseheaders.common.UsedFor;

/**
 * {@link RowMapper} for {@link HttpHeaderConfig}.
 */
final class HttpHeaderConfigRowMapper implements RowMapper<HttpHeaderConfig> {
	
	/** Singleton instance. */
	public static final HttpHeaderConfigRowMapper INSTANCE = new HttpHeaderConfigRowMapper();

	@Override
	public HttpHeaderConfig mapRow(final ResultSet resultSet, final int row) throws SQLException {
		/*
		 * Columns not read:
		 * 
		 * - comment:		informational column, not used by code
		 */
		
		final String headerName = resultSet.getString("header_name");
		final String headerValue = resultSet.getString("header_value");
		final boolean overwrite = resultSet.getBoolean("overwrite");
		final String applicationTypesList = resultSet.getString("app_types");
				
		final List<String> applicationTypes = applicationTypesList != null
				? Arrays.asList(applicationTypesList.split(",|;"))
				: Collections.emptyList();
		
		final UsedFor usedFor = decodeUsedFor(resultSet);
		final int companyID = resultSet.getInt("company_ref");
		final Pattern remoteHostnamePattern = decodeRemoteHostnamePattern(resultSet);
		final Pattern queryPattern = decodeQueryPattern(resultSet);
		
		return new HttpHeaderConfig(headerName, headerValue, overwrite, applicationTypes, usedFor, companyID, remoteHostnamePattern, queryPattern);
	}
	
	private static final UsedFor decodeUsedFor(final ResultSet rs) throws SQLException {
		final String s = rs.getString("used_for");
		try {
			return UsedFor.fromId(s);
		} catch(final NoSuchElementException e) {
			throw new SQLException(String.format("Found invalid value for 'used_for': %s", s));
		}
	}

	private static final Pattern decodeRemoteHostnamePattern(final ResultSet rs) throws SQLException {
		final String pattern = rs.getString("remote_hostname_pattern");

		return rs.wasNull() ? null : Pattern.compile(pattern);
	}

	private static final Pattern decodeQueryPattern(final ResultSet rs) throws SQLException {
		final String pattern = rs.getString("query_pattern");

		return rs.wasNull() ? null : Pattern.compile(pattern);
	}

	private static final Pattern decodeResponseMimetypePattern(final ResultSet rs) throws SQLException {
		final String pattern = rs.getString("resp_mimetype_pattern");

		return rs.wasNull() ? null : Pattern.compile(pattern);
	}
}
