/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.recipient.web.listener;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.sql.DataSource;

import org.agnitas.dao.impl.mapper.IntegerRowMapper;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.agnitas.emm.springws.WebservicePasswordEncryptor;

/**
 * Listener that converts the encrypted passwords for webservices users from
 * password-based encryption with MD5 to password-based encryption with SHA512-based.
 * 
 * See EMM-5549
 */
public final class WebservicePasswordMD5toSHA512TranscryptingListener implements ServletContextListener {
	
	private static final transient Logger logger = Logger.getLogger(WebservicePasswordMD5toSHA512TranscryptingListener.class);

	private static final class _WSUser {
		public final String username;
		public final String pwdEncrypted;
		
		public _WSUser(final String username, final String pwdEncrypted) {
			this.username = username;
			this.pwdEncrypted = pwdEncrypted;
		}
	}
	
	private static final class _WSUserRowMapper implements RowMapper<_WSUser> {

		@Override
		public final _WSUser mapRow(final ResultSet rs, final int row) throws SQLException {
			final String username = rs.getString("username");
			final String pwdEncrypted = rs.getString("password_encrypted");
			
			return new _WSUser(username, pwdEncrypted);
		}
		
	}
	
	@Override
	public final void contextDestroyed(final ServletContextEvent servletContextEvent) {
		// Nothing to do on context shutdown
	}

	@Override
	public final void contextInitialized(final ServletContextEvent servletContextEvent) {
		final ServletContext servletContext = servletContextEvent.getServletContext();
		final WebApplicationContext webApplicationContext = WebApplicationContextUtils.getWebApplicationContext(servletContext);
		final DataSource dataSource = webApplicationContext.getBean("dataSource", DataSource.class);
		final WebservicePasswordEncryptor encryptor = webApplicationContext.getBean("WebservicePasswordEncryptor", WebservicePasswordEncryptor.class);

		convertUsersOfMarkedCompanies(dataSource, encryptor);
	}
	
	private final void convertUsersOfMarkedCompanies(final DataSource dataSource, final WebservicePasswordEncryptor encryptor) {
		if(logger.isInfoEnabled()) {
			logger.info("Processing webservice users");
		}
		
		final List<Integer> markedCompanies = listMarkedCompanies(dataSource);
		
		logger.warn(String.format("Found %d companies to process", markedCompanies.size()));
		
		for(final int companyID : markedCompanies) {
			try {
				convertUsers(companyID, dataSource, encryptor);
			} catch(final Exception e) {
				logger.error(String.format("Error processing company ID %d", companyID), e);
			}
		}
		
		if(logger.isInfoEnabled()) {
			logger.info("Webservices users processed");
		}
	}
	
	private final List<Integer> listMarkedCompanies(final DataSource dataSource) {
		final String sql = "SELECT DISTINCT company_id FROM company_info_tbl WHERE cname=? AND cvalue='true' ORDER BY company_id";
		final JdbcTemplate template = new JdbcTemplate(dataSource);
		
		return template.query(sql, new IntegerRowMapper(), ConfigValue.WebserviceConvertPasswordsMD5toSHA512OnStartup.toString()); 
	}
	
	private final void convertUsers(final int companyID, final DataSource dataSource, final WebservicePasswordEncryptor encryptor) {
		logger.warn(String.format("Processing webservice users of company %d", companyID));
		
		final List<_WSUser> list = listUsers(companyID, dataSource);
		
		boolean hadErrors = false;
			
		for(final _WSUser user : list) {
			try {
				convertUser(user, dataSource, encryptor);
			} catch(final Exception e) {
				logger.error(String.format("Error converting password encryption of webservice user '%s'", user.username), e);
			}
		}
		
		if(!hadErrors) {
			removeCompanyMark(companyID, dataSource);
		}
	}
	
	private final void removeCompanyMark(final int companyID, final DataSource dataSource) {
		final String sql = "UPDATE company_info_tbl SET cvalue = 'false', description = 'Transcrypted WS password', timestamp = CURRENT_TIMESTAMP WHERE cname = ? AND company_id = ?";
		
		new JdbcTemplate(dataSource).update(sql, ConfigValue.WebserviceConvertPasswordsMD5toSHA512OnStartup.toString(), companyID);
	}
	
	private final List<_WSUser> listUsers(final int companyID, final DataSource dataSource) {
		final String sql = "SELECT username, password_encrypted FROM webservice_user_tbl WHERE company_id=?";
		
		final JdbcTemplate template = new JdbcTemplate(dataSource);
		
		return template.query(sql, new _WSUserRowMapper(), companyID); 
	}
	
	private final void convertUser(final _WSUser user, final DataSource dataSource, final WebservicePasswordEncryptor encryptor) throws GeneralSecurityException, UnsupportedEncodingException {
		logger.warn(String.format("Processing WS user '%s'", user.username));
		final String sql = "UPDATE webservice_user_tbl SET password_encrypted=? WHERE username=?";
		final String pwd = encryptor.decrypt(user.username, user.pwdEncrypted);
		
		final String newEncryptedPwd = encryptor.encrypt(user.username, pwd);
		
		// Update only, if encrypted password changed.
		if(!newEncryptedPwd.equals(user.pwdEncrypted)) {
			new JdbcTemplate(dataSource).update(sql, newEncryptedPwd, user.username);
		}
	}

}
