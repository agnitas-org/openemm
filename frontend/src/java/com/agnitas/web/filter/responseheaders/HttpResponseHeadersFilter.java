/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web.filter.responseheaders;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.util.ServerCommand.Server;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

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

	/** Name of parameter to configure class name of configuration source. */
	public static final String SOURCE_CLASSNAME_PARAMETER_NAME = "source.classname";
	
	/** Name of parameter to configure cache period (in ms). */
	public static final String CACHE_PERIOD_MILLIS_PARAMETER_NAME = "cache.period-millis";
	
	/** Configuration service. */
	private ConfigService configService;
	
	/** Cache for header configuration. */
	private HeaderConfigCache headerConfigCache;

	@Override
	public final void init(final FilterConfig filterConfig) throws ServletException {
		Filter.super.init(filterConfig);
		
		try {
			final WebApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(filterConfig.getServletContext()); 
			this.configService = context.getBean("ConfigService", ConfigService.class);
		} catch(final Throwable t) {
			throw new ServletException("Could not retrieve config service", t);
		}
		
		try {
			this.headerConfigCache = createHeaderConfigCache(filterConfig);
		} catch(final Exception e) {
			throw new ServletException("Error setting up header configuration cache", e);
		}
	}
	
	/**
	 * Creates the configuration cache.
	 * 
	 * @param filterConfig filter configuration
	 * 
	 * @return new cache instance
	 * 
	 * @throws Exception on errors creating cache instance
	 */
	private final HeaderConfigCache createHeaderConfigCache(final FilterConfig filterConfig) throws Exception {
		final HeaderConfigSource headerConfigSource = createHeaderConfigSource(filterConfig);
		
		final int cachePeriodMillis = Integer.parseInt(filterConfig.getInitParameter(CACHE_PERIOD_MILLIS_PARAMETER_NAME));
		
		return new HeaderConfigCache(headerConfigSource, cachePeriodMillis);
	}
	
	/**
	 * Creates a new configuration source.
	 * 
	 * @param filterConfig filter configuration
	 * 
	 * @return new source instance
	 * 
	 * @throws Exception on errors creating source instance
	 */
	private static final HeaderConfigSource createHeaderConfigSource(final FilterConfig filterConfig) throws Exception {
		final HeaderConfigSource source = (HeaderConfigSource) Class.forName(filterConfig.getInitParameter(SOURCE_CLASSNAME_PARAMETER_NAME)).getConstructor().newInstance();
		source.init(filterConfig);
		
		return source;
	}

	@Override
	public final void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
		if(response instanceof HttpServletResponse) {
			final HttpServletResponseWrapper responseWrapper = new HttpServletResponseWrapper((HttpServletResponse) response);

			addResponseHeaders(responseWrapper);
			
			chain.doFilter(request, responseWrapper);
		} else {
			chain.doFilter(request, response);
		}
	}
	
	/**
	 * Adds configured response headers.
	 * 
	 * @param response HTTP response
	 */
	private final void addResponseHeaders(final HttpServletResponse response) {
		final Server applicationType = this.configService.getApplicationType();
		
		final List<HttpHeaderConfig> headerConfigurationList = this.headerConfigCache.listHeaderConfigs();
		
		/*
		 * Determine headers to add.
		 * 
		 * Determination of headers to add and adding headers are implemented as separate steps, because headers can
		 * occur more than one in the configuration source. When doing in one step, this breaks the overwrite mechanism.
		 */
		final List<HttpHeaderConfig> headersToAdd = headerConfigurationList
				.stream()
				.filter(headerConfig -> headerConfig.isApplicableForApplicationType(applicationType))
				.filter(headerConfig -> headerConfig.isOverwrite() || !response.containsHeader(headerConfig.getHeaderName()))
				.collect(Collectors.toList());

		// Add selected headers to response or replaces existing header
		for(final HttpHeaderConfig headerConfig : headersToAdd) {
			response.setHeader(headerConfig.getHeaderName(), headerConfig.getHeaderValue());
		}
	}

}
