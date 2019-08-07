/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.external.web.filter;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.sql.DataSource;

import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.apache.log4j.Logger;

import com.agnitas.reporting.birt.util.RSACryptUtil;
import com.agnitas.reporting.birt.util.UIDUtils;

public class BirtInterceptingFilter implements Filter {
	/** Logger. */
	private static final transient Logger logger = Logger.getLogger(BirtInterceptingFilter.class);

	private DataSource dataSource;
	private String privateKeyString;
	private String errorPage;

	@Override
	public void destroy() {
		// Nothing to do here...
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
		String encodedUID = request.getParameter("uid");
		String companyIDStr = request.getParameter("companyID");

		if (encodedUID == null) {
			forwardToErrorPage(request, response);
			return;
		}

		if ("".equals(encodedUID.trim())) {
			forwardToErrorPage(request, response);
			return;
		}

		if (companyIDStr == null) {
			forwardToErrorPage(request, response);
			return;
		}

		if ("".equals(companyIDStr.trim())) {
			forwardToErrorPage(request, response);
			return;
		}

		if (!companyIDStr.matches("[0-9]*")) {
			forwardToErrorPage(request, response);
			return;
		}

		if (!validateUID(encodedUID, Integer.parseInt(companyIDStr))) {
			forwardToErrorPage(request, response);
			return;
		}

		filterChain.doFilter(request, response);
	}

	private void forwardToErrorPage(ServletRequest request, ServletResponse response) throws ServletException, IOException {
		request.setAttribute("error", new Exception("Not authenticated"));
		RequestDispatcher dispatcher = request.getRequestDispatcher(errorPage);
		dispatcher.forward(request, response);
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// error page for redirects
		errorPage = filterConfig.getInitParameter("errorpage");
		if (errorPage == null) {
			errorPage = "/error.do";
		}
		
		ConfigService configService = ConfigService.getInstance();
		
		try {
			if (dataSource == null) {
				String datasourceName = "jdbc/" + configService.getValue(ConfigValue.EmmDbJndiName);
				Context context = (Context) new InitialContext().lookup("java:comp/env");
				dataSource = (DataSource) context.lookup(datasourceName);
				if (dataSource == null) {
					throw new Exception("'" + datasourceName + "' is an unknown DataSource");
				}
			}
		} catch (Exception e) {
			throw new ServletException("Cannot create datasource: " + e.getMessage(), e);
		}
		
		try {
			privateKeyString = RSACryptUtil.getPrivateKey(configService.getValue(ConfigValue.BirtPrivateKeyFile));
		} catch (Exception e) {
			logger.error("BirtPrivateKeyFile: " + configService.getValue(ConfigValue.BirtPrivateKeyFile));
			throw new ServletException("Cannot read BirtPrivateKeyFile: " + e.getMessage(), e);
		}
	}

	private boolean validateUID(String encodedUID, @VelocityCheck int companyID) {
		String uid = "";

		try {
			uid = RSACryptUtil.decrypt(encodedUID, privateKeyString);
		} catch (Exception e) {
			logger.error("Could not decrpyt uid :" + encodedUID, e);
			return false;
		}

		String[] tokens = UIDUtils.extractSecurityTokensFromUID(uid);

		String userIDStr = tokens[0];
		long userID = Integer.parseInt(userIDStr);

		String usercompanyIDStr = tokens[1];
		long usercompanyId = Integer.parseInt(usercompanyIDStr);

		String providedSecurePasswordHash = tokens[2];

		if (companyID != usercompanyId) {
			return false;
		}

		try (Connection connection = dataSource.getConnection()) {
			try (PreparedStatement statement = connection.prepareStatement("SELECT secure_password_hash FROM admin_tbl WHERE admin_id = ? AND company_id = ?")) {
				statement.setLong(1, userID);
				statement.setLong(2, companyID);
				
				try (ResultSet resultSet = statement.executeQuery()) {
					if (resultSet.next()) {
						String securePasswordHash = resultSet.getString("secure_password_hash") != null ? resultSet.getString("secure_password_hash") : "";
						
						if(securePasswordHash.length() > 32) {
							// According to UIDUtils.createUID(), password hash is cropped to 32 bytes
							securePasswordHash = securePasswordHash.substring(0, 32);
						}
						
						return providedSecurePasswordHash.equals(securePasswordHash);
					} else {
						if (logger.isInfoEnabled()) {
							logger.info("Didn't get authentication data for given username: " + userID + " (company ID: " + companyID + ")");
						}

						return false;
					}
				}
			}
		} catch (Exception e) {
			logger.error("Couldn't get password for adminID:" + userID, e);
			return false;
		}
	}
}
