/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web.filter.responseheaders;

import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import jakarta.servlet.FilterConfig;

/**
 * Implementation of {@link HeaderConfigSource} reading the HTTP response headers from database.
 */
public final class DatabaseHeaderConfigSource implements HeaderConfigSource {

	/** {@link DataSource}. */
	private DataSource dataSource;
	
	@Override
	public final void init(final FilterConfig filterConfig) throws Exception {
		final WebApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(filterConfig.getServletContext());
		this.dataSource = context.getBean(DataSource.class);
	}

	@Override
	public final List<HttpHeaderConfig> loadHeaderConfiguration() throws Exception {
		final JdbcTemplate template = new JdbcTemplate(this.dataSource);
		
		return template.query("SELECT * FROM http_response_headers_tbl", HttpHeaderConfigRowMapper.INSTANCE);
	}

}
