/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.external.web.filter;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.agnitas.reporting.birt.util.RSACryptUtil;

public class BirtInterceptingFilter implements Filter {
	/** Logger. */
	private static final transient Logger logger = Logger.getLogger(BirtInterceptingFilter.class);
	
	protected ConfigService configService;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// Nothing to do here...
	}
	
	@Override
	public void destroy() {
		// Nothing to do here...
	}

	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
		String securityToken = request.getParameter("sec");
		String companyIDStr = request.getParameter("companyID");

		if (StringUtils.isBlank(securityToken)) {
			forwardToErrorPage(request, response);
		} else if (StringUtils.isBlank(companyIDStr)) {
			forwardToErrorPage(request, response);
		} else if (!companyIDStr.matches("[0-9]*")) {
			forwardToErrorPage(request, response);
		} else if (!verifySecurityToken(securityToken, Integer.parseInt(companyIDStr))) {
			forwardToErrorPage(request, response);
		} else {
			filterChain.doFilter(request, response);
		}
	}

	private void forwardToErrorPage(ServletRequest request, ServletResponse response) throws ServletException, IOException {
		request.setAttribute("error", new Exception("Not authenticated"));
		RequestDispatcher dispatcher = request.getRequestDispatcher(getConfigService().getValue(ConfigValue.BirtErrorPage));
		dispatcher.forward(request, response);
	}

	private boolean verifySecurityToken(String securityTokenString, @VelocityCheck int companyID) {
		String privateKeyString = getConfigService().getValue(ConfigValue.BirtPrivateKey, companyID);
		if (StringUtils.isBlank(privateKeyString) ) {
			logger.warn("Birt private key is missing");
			return false;
		}
		String securityTokenContent;
		try {
			securityTokenContent = RSACryptUtil.decrypt(securityTokenString, privateKeyString);
		} catch (Exception e) {
			logger.warn("Could not decrpyt securityToken(sec):" + securityTokenString, e);
			return false;
		}
		if (StringUtils.isBlank(securityTokenContent) ) {
			logger.warn("Empty securityToken:" + securityTokenString);
			return false;
		}
		int companyIdFromSecurityToken;
		try {
			companyIdFromSecurityToken = Integer.parseInt(securityTokenContent);
		} catch (Exception e) {
			logger.warn("Could not read securityToken(sec):" + securityTokenContent, e);
			return false;
		}
		if (companyIdFromSecurityToken != companyID) {
			logger.warn("Invalid companyID in securityToken(sec). Expected: " + companyID + " Actually: " + companyIdFromSecurityToken);
			return false;
		} else {
			return true;
		}
	}
	
	public static String createSecurityToken(ConfigService configService, int companyID) throws Exception {
		String publicKeyString = configService.getValue(ConfigValue.BirtPublicKey, companyID);
        if (StringUtils.isBlank(publicKeyString)) {
            throw new Exception("Parameter 'birt.publickey' is missing");
        } else {
        	return RSACryptUtil.encrypt(Integer.toString(companyID), publicKeyString);
        }
	}

    private ConfigService getConfigService() {
		if (configService == null) {
			// Doesn't work here, because this filter runs in BIRT environment, which has no Spring context:
			// configService = WebApplicationContextUtils.getWebApplicationContext(filterConfig.getServletContext()).getBean("ConfigService", ConfigService.class);
			// So we use:
			configService = ConfigService.getInstance();
		}
		return configService;
	}
}
