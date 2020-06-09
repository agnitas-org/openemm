/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.startuplistener.startupjobs;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.core.logintracking.bean.LoginTrackSettings;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.agnitas.startuplistener.api.JobContext;
import com.agnitas.startuplistener.api.StartupJob;
import com.agnitas.startuplistener.api.StartupJobException;

public final class LoginTrackSettingsMigrationStartupJob implements StartupJob {
	
	private static final int NEW_MAX_FAILS_DEFAULT = 10;
	private static final int NEW_BLOCK_TIME = 60;

	/** The logger. */
	private static final transient Logger LOGGER = Logger.getLogger(LoginTrackSettingsMigrationStartupJob.class);
	
	private static final class _RowMapper implements RowMapper<LoginTrackSettings> {

		@Override
		public final LoginTrackSettings mapRow(final ResultSet resultSet, final int row) throws SQLException {
			final int maxFails = resultSet.getInt("max_login_fails");
			final int blockTime = resultSet.getInt("login_block_time");

			return new LoginTrackSettings(blockTime, maxFails, blockTime);
		}
		
	}
	
	@Override
	public final void runStartupJob(final JobContext context) throws StartupJobException {
		// Check if a company ID is defined
		if(context.getCompanyId() <= 0) {
			final String msg = String.format("Startup job %d requires a company ID", context.getJobId());
			LOGGER.error(msg);
			throw new StartupJobException(msg);
		}
		
		// Load settings from company_tbl
		final LoginTrackSettings settings = loadSettingsFromCompanyTable(context.getDataSource(), context.getCompanyId());

		// Check that configures company ID exists
		if(settings == null) {
			final String msg = String.format("Startup job %d is configured for company ID %d, which is unknown", context.getJobId(), context.getCompanyId());
			LOGGER.error(msg);
			throw new StartupJobException(msg);
		}
		
		if(settings.getMaxFailedLogins() != NEW_MAX_FAILS_DEFAULT || settings.getLockTimeSeconds() != NEW_BLOCK_TIME) {
			// At least one setting differes from new default -> keep both values (write entry, even value is equals to new default value)
			context.getConfigService().writeValue(
					ConfigValue.LoginTracking.WebuiMaxFailedAttempts, 
					context.getCompanyId(),
					Integer.toString(settings.getMaxFailedLogins()));
			
			context.getConfigService().writeValue(
					ConfigValue.LoginTracking.WebuiIpBlockTimeSeconds, 
					context.getCompanyId(),
					Integer.toString(settings.getLockTimeSeconds()));
		} else {
			// Both values are set to new default values (remove entry, if value is equals to new default value)
			context.getConfigService().writeOrDeleteIfDefaultValue(
					ConfigValue.LoginTracking.WebuiMaxFailedAttempts, 
					context.getCompanyId(),
					Integer.toString(settings.getMaxFailedLogins()));
			
			context.getConfigService().writeOrDeleteIfDefaultValue(
					ConfigValue.LoginTracking.WebuiIpBlockTimeSeconds, 
					context.getCompanyId(),
					Integer.toString(settings.getLockTimeSeconds()));
		}
	}
	
	private final LoginTrackSettings loadSettingsFromCompanyTable(final DataSource dataSource, final int companyID) {
		final String sql = "SELECT max_login_fails, login_block_time FROM company_tbl WHERE company_id=?";
		
		final List<LoginTrackSettings> result = new JdbcTemplate(dataSource).query(sql, new _RowMapper(), companyID);
		
		if(result.isEmpty()) {
			return null;
		} else {
			return result.get(0);
		}
	}

}
