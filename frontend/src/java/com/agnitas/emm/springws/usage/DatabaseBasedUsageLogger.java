/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.springws.usage;

import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.Objects;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.jdbc.core.JdbcTemplate;

public final class DatabaseBasedUsageLogger implements UsageLogger  {
	/** The logger. */
	@SuppressWarnings("unused")
	private static final transient Logger logger = Logger.getLogger(DatabaseBasedUsageLogger.class);
	
	private DataSource dataSource;

	@Override
	public final void logWebserviceUsage(final ZonedDateTime timestamp, final String endpoint, final int companyID, final String user) {
		final String sql = "INSERT INTO webservice_usage_log_tbl (timestamp, endpoint, company_id, username) VALUES (?, ?, ?, ?)";
		
		final Timestamp ts = Timestamp.from(timestamp.toInstant());
		
		final JdbcTemplate template = new JdbcTemplate(dataSource);
		template.update(sql, ts, endpoint, companyID, user);
	} 

	@Required
	public final void setDataSource(final DataSource dataSource) {
		this.dataSource = Objects.requireNonNull(dataSource, "Data source cannot be null");
	}
}
