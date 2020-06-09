/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.wsmanager.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.agnitas.emm.wsmanager.bean.WebserviceUserSettings;

final class WebserviceUserSettingsRowMapper implements RowMapper<WebserviceUserSettings> {

	@Override
	public final WebserviceUserSettings mapRow(final ResultSet rs, final int row) throws SQLException {
		final int defaultDataSourceID = rs.getInt("default_data_source_id");
		
		final Integer requestRateLimitOrNull = readNullableInt(rs, "req_rate_limit");
		final Integer bulkSizeLimitOrNull = readNullableInt(rs, "bulk_size_limit");
		final Integer maxResultListSizeOrNull = readNullableInt(rs, "max_result_list_size");
		
		return new WebserviceUserSettings(defaultDataSourceID, requestRateLimitOrNull, bulkSizeLimitOrNull, maxResultListSizeOrNull);
	}
	
	private static final Integer readNullableInt(final ResultSet rs, final String column) throws SQLException {
		final int value = rs.getInt(column);
		
		return rs.wasNull()
				? null
				: value;
	}

}
