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

import org.agnitas.util.DbUtilities;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.agnitas.emm.springws.WebservicePasswordEncryptor;
import com.agnitas.startuplistener.api.JobContext;
import com.agnitas.startuplistener.api.StartupJob;
import com.agnitas.startuplistener.api.StartupJobException;

public final class PasswordConversionStartupJob implements StartupJob {
	
	private static final transient Logger LOGGER = Logger.getLogger(PasswordConversionStartupJob.class);
	
	final static class UsernamePasswordPair {
		public final String username;
		public final String password;
		
		public UsernamePasswordPair(final String username, final String password) {
			this.username = username;
			this.password = password;
		}
	}
	
	final static class _RowMapper implements RowMapper<UsernamePasswordPair> {

		@Override
		public final UsernamePasswordPair mapRow(final ResultSet rs, final int row) throws SQLException {
			return new UsernamePasswordPair(
					rs.getString("username"),
					rs.getString("password_encrypted"));
		}
		
	}
	
	@Override
	public final void runStartupJob(final JobContext context) throws StartupJobException {
		if(!backupTableExists(context.getDataSource())) {
			createBackupTable(context.getDataSource());
		}
		
		final WebservicePasswordEncryptor enc = context.getApplicationContext().getBean("WebservicePasswordEncryptor", WebservicePasswordEncryptor.class);
		
		upgradePasswordEncryption(context.getDataSource(), enc);
	}
	
	private final boolean backupTableExists(final DataSource dataSource) {
		return DbUtilities.checkIfTableExists(dataSource, "webservice_user_backup_tbl");
	}

	private final void createBackupTable(final DataSource dataSource) {
		final JdbcTemplate template = new JdbcTemplate(dataSource);
		
		template.execute("CREATE TABLE webservice_user_backup_tbl AS SELECT username, password_encrypted FROM webservice_user_tbl");
	}
	
	private final void upgradePasswordEncryption(final DataSource dataSource, final WebservicePasswordEncryptor enc) {
		final JdbcTemplate template = new JdbcTemplate(dataSource);
		final List<UsernamePasswordPair> list = listUsernamesAndPasswords(template);
		
		for(final UsernamePasswordPair pair : list) {
			try {
				upgradePasswordEncryption(enc, template, pair);
			} catch(final Exception e) {
				LOGGER.error(String.format("Error upgrading password encryption for webservice user '%s'", pair.username), e);
			}
		}
	}
	
	private final List<UsernamePasswordPair> listUsernamesAndPasswords(final JdbcTemplate template) {
		final String sql = "SELECT username, password_encrypted FROM webservice_user_tbl";
		
		return template.query(sql, new _RowMapper());
	}
	
	private final void upgradePasswordEncryption(final WebservicePasswordEncryptor enc, final JdbcTemplate template, final UsernamePasswordPair pair) throws Exception {
		final String decryptedPassword = enc.decrypt(pair.username, pair.password);
		final String reencryptedPassword = enc.encrypt(pair.username, decryptedPassword);
		
		final String sql = "UPDATE webservice_user_tbl SET password_encrypted=? WHERE username=?";
		template.update(sql, reencryptedPassword, pair.username);
	}
}
