/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.responseheaders.web;

import java.io.IOException;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Filter to add response headers.
 * 
 * This filter supports these init parameters:
 * <ul>
 *   <li><b>source.classname</b> class name of configuration source</li>
 *   <li><b>cache.period-millis</b> cache period in milliseconds. After this period, the cached data is considered as outdated
 *     will be refreshed by reading from configuration source. (0: cache never outdates, -1 cache always outdates)</li>
 * </ul>
 * 
 * Further init parameters depends on the configured configuration source.
 */
public final class HttpResponseHeadersFilter implements Filter {
	
	public static final String HTTP_HEADER_APPLIER_BEAN_NAME = "headerapplier.bean-name";
	
	private HttpResponseHeaderApplier headerApplier;

	@Override
	public final void init(final FilterConfig filterConfig) throws ServletException {
		Filter.super.init(filterConfig);
		
		try {
			final WebApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(filterConfig.getServletContext());
			this.headerApplier = context.getBean(filterConfig.getInitParameter(HTTP_HEADER_APPLIER_BEAN_NAME), HttpResponseHeaderApplier.class);
		} catch(final Throwable t) {
			throw new ServletException("Could not retrieve required Spring beans", t);
		}
	}

	@Override
	public final void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
		if(response instanceof HttpServletResponse) {
			this.headerApplier.applyHeadersToFilterResponse((HttpServletRequest) request, (HttpServletResponse) response);
			
			chain.doFilter(request, response);
		} else {
			chain.doFilter(request, response);
		}
	}

}
