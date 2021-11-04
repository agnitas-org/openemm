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
import org.apache.log4j.Logger;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * This filter's purpose is to fix RCE vulnerability that the BIRT servlets have.
 * See https://bugs.eclipse.org/bugs/show_bug.cgi?id=538142 for more detail.
 */
public class BirtNoDocumentParameterInterceptor implements Filter {
    /** Logger. */
    @SuppressWarnings("unused")
	private static final transient Logger logger = Logger.getLogger(BirtNoDocumentParameterInterceptor.class);

	private FilterConfig filterConfig;
	protected ConfigService configService;

    @Override
    public void init(FilterConfig filterConfigParameter) throws ServletException {
    	this.filterConfig = filterConfigParameter;
    }

    @Override
    public void destroy() {
        // Do nothing.
    }

	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (request.getParameter("__document") == null) {
            chain.doFilter(request, response);
        } else {
            error(request, response);
        }
    }

    private void error(ServletRequest request, ServletResponse response) throws ServletException, IOException {
        request.setAttribute("error", new Exception("Using __document parameter is denied"));
		RequestDispatcher dispatcher = request.getRequestDispatcher(getConfigService().getValue(ConfigValue.BirtErrorPage));
        dispatcher.forward(request, response);
    }

    private ConfigService getConfigService() {
		if (configService == null) {
			configService = WebApplicationContextUtils.getWebApplicationContext(filterConfig.getServletContext()).getBean("ConfigService", ConfigService.class);
		}
		return configService;
	}
}
