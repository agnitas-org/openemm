/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.startuplistener.startupjobs;

import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import org.agnitas.util.DbUtilities;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;

import com.agnitas.startuplistener.api.JobContext;
import com.agnitas.startuplistener.api.StartupJob;
import com.agnitas.startuplistener.api.StartupJobException;

public final class LastLoginMigrationStartupJob implements StartupJob {

	/** The logger. */
	private static final transient Logger LOGGER = Logger.getLogger(LastLoginMigrationStartupJob.class);
	
	@Override
	public final void runStartupJob(final JobContext context) throws StartupJobException {
		// Check if a company ID is defined
		if (context.getCompanyId() <= 0) {
			final String msg = String.format("Startup job %d requires a company ID", context.getJobId());
			LOGGER.error(msg);
			throw new StartupJobException(msg);
		}

		if(requiredColumnsExist(context.getDataSource())) {
			final List<String> adminsWithoutLastLoginDate = listAdmins(context.getCompanyId(), context.getDataSource());
			
			if(adminsWithoutLastLoginDate != null) {
				for(final String username : adminsWithoutLastLoginDate) {
					updateLastLoginDate(context.getCompanyId(), username, context.getDataSource());
				}
			}
		}
	}
	
	private final void updateLastLoginDate(final int companyId, final String username, final DataSource dataSource) {
		final JdbcTemplate template = new JdbcTemplate(dataSource);
		
		final String select = "SELECT max(creation_date) AS last_login FROM login_track_tbl WHERE username=? AND login_status=10";
		final Date lastLoginDate = template.queryForObject(select, Date.class, username);
		
		if(lastLoginDate != null) {
			final String update = "UPDATE admin_tbl SET last_login_date=? WHERE username=? AND company_id=?";
			
			template.update(update, lastLoginDate, username, companyId);
		}
	}
	
	private final List<String> listAdmins(final int companyID, final DataSource dataSource) {
		// Select usernames from a given company ID without last login date set
		final String sql = "SELECT username FROM admin_tbl WHERE company_id=? AND last_login_date IS NULL";
		
		final JdbcTemplate template = new JdbcTemplate(dataSource);
		return template.queryForList(sql, String.class, companyID);
	}
	
	private final boolean requiredColumnsExist(final DataSource dataSource) throws StartupJobException {
		try {
			return DbUtilities.checkTableAndColumnsExist(dataSource, "admin_tbl", "last_login_date");
		} catch (final Exception e) {
			throw new StartupJobException(e);
		}
	}

}
