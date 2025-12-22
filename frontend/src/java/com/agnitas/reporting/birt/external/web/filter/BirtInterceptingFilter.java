/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.external.web.filter;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import com.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.core.commons.util.ConfigValue;
import com.agnitas.reporting.birt.external.exception.BirtSecurityTokenCreationException;
import com.agnitas.reporting.birt.util.RSACryptUtil;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BirtInterceptingFilter implements Filter {

	private static final Logger logger = LogManager.getLogger(BirtInterceptingFilter.class);
	
	protected ConfigService configService;

	@Override
	public void init(FilterConfig filterConfig) {
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

	private boolean verifySecurityToken(String securityTokenStringEncoded, int companyID) {
		final String securityTokenString = URLDecoder.decode(securityTokenStringEncoded, StandardCharsets.US_ASCII);

		String privateKeyString = getConfigService().getValue(ConfigValue.BirtPrivateKey, companyID);
		if (StringUtils.isBlank(privateKeyString) ) {
			logger.warn("Birt private key is missing");
			return false;
		}

		String securityTokenContent;
		try {
			securityTokenContent = RSACryptUtil.decrypt(securityTokenString, privateKeyString);
		} catch (Exception e) {
			logger.warn("Could not decrpyt securityToken(sec): %s".formatted(securityTokenString), e);
			return false;
		}
		if (StringUtils.isBlank(securityTokenContent) ) {
			logger.warn("Empty securityToken: {}", securityTokenString);
			return false;
		}
		int companyIdFromSecurityToken;
		try {
			companyIdFromSecurityToken = Integer.parseInt(securityTokenContent);
		} catch (Exception e) {
			logger.warn("Could not read securityToken(sec):%s".formatted(securityTokenContent), e);
			return false;
		}
		if (companyIdFromSecurityToken != companyID) {
			logger.warn("Invalid companyID in securityToken(sec). Expected: {} Actually: {}", companyID, companyIdFromSecurityToken);
			return false;
		} else {
			return true;
		}
	}
	
	public static String createSecurityToken(ConfigService configService, int companyID) {
		String publicKeyString = configService.getValue(ConfigValue.BirtPublicKey, companyID);
        if (StringUtils.isBlank(publicKeyString)) {
            throw new BirtSecurityTokenCreationException("Parameter 'birt.publickey' is missing");
        }

        try {
            return RSACryptUtil.encryptUrlSafe(Integer.toString(companyID), publicKeyString);
        } catch (Exception e) {
            throw new BirtSecurityTokenCreationException("Failed creating security token", e);
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
