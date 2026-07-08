/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.responseheaders.dao;

import com.agnitas.emm.responseheaders.common.HttpHeaderConfig;
import jakarta.servlet.FilterConfig;
import com.agnitas.dao.impl.BaseDaoImpl;

import java.util.List;

/**
 * Implementation of {@link HeaderConfigSource} reading the HTTP response headers from database.
 */
public final class DatabaseHeaderConfigSource extends BaseDaoImpl implements HeaderConfigSource {
	
	@Override
	public List<HttpHeaderConfig> loadHeaderConfiguration() {
		return select("SELECT * FROM http_response_headers_tbl", HttpHeaderConfigRowMapper.INSTANCE);
	}

	@Override
	public void init(FilterConfig filterConfig) {
		// Unused
	}

}
