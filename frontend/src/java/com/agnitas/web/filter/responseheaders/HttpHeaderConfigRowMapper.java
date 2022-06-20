/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web.filter.responseheaders;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.jdbc.core.RowMapper;

/**
 * {@link RowMapper} for {@link HttpHeaderConfig}.
 */
final class HttpHeaderConfigRowMapper implements RowMapper<HttpHeaderConfig> {
	
	/** Singleton instance. */
	public static final HttpHeaderConfigRowMapper INSTANCE = new HttpHeaderConfigRowMapper();

	@Override
	public HttpHeaderConfig mapRow(final ResultSet resultSet, final int row) throws SQLException {
		final String headerName = resultSet.getString("header_name");
		final String headerValue = resultSet.getString("header_value");
		final boolean overwrite = resultSet.getBoolean("overwrite");
		final String applicationTypesList = resultSet.getString("app_types");
				
		final List<String> applicationTypes = applicationTypesList != null
				? Arrays.asList(applicationTypesList.split(",|;"))
				: Collections.emptyList();
		
		return new HttpHeaderConfig(headerName, headerValue, overwrite, applicationTypes);
	}

}
