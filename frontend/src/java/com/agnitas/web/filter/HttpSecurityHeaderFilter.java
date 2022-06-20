/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web.filter;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

/**
 * Filter implementing HTTP Security headers.
 *
 * Filter properties (for web.xml) are:
 * 
 * 
 * <table summary="Overview of filter properties">
 *   <tr>
 *     <th colspan="4">Common properties</th>
 *   </tr>
 *   <tr>
 *     <th>Property</th>
 *     <th>Description</th>
 *     <th>Values</th>
 *     <th>Default</th>
 *   </tr>
 *   <tr>
 *     <td>${header}.enable</td>
 *     <td>Enables security header</td>
 *     <td>true, false</td>
 *     <td>{@value #COMMON_ENABLE_DEFAULT}</td>
 *   </tr>
 *   <tr>
 *     <td>${header}.overwrite</td>
 *     <td>Overwrite existing header</td>
 *     <td>true, false</td>
 *     <td>{@value #COMMON_OVERWRITE_DEFAULT}</td>
 *   </tr>
 *   <tr>
 *     <th colspan="4">HTTP Strict Transport Security (HSTS)</th>
 *   </tr>
 *   <tr>
 *     <td>hsts.magAge</td>
 *     <td>Period of time (in seconds) of advising browser to use HTTPS connections</td>
 *     <td>positive integer</td>
 *     <td>{@value #HSTS_MAXAGE_DEFAULT} seconds</td>
 *   </tr>
 *   <tr>
 *     <td>hsts.includeSubdomains</td>
 *     <td>Flag controlling if the browser should use HTTPS for sub-domains, too.</td>
 *     <td>true, false</td>
 *     <td>{@value #HSTS_INCLUDE_SUBDOMAINS_DEFAULT}</td>
 *   </tr>
 * </table>
 * 
 */
@Deprecated // Use com.agnitas.web.filter.responseheaders.HttpResponseHeadersFilter instead
public final class HttpSecurityHeaderFilter implements Filter {

	public static final String HSTS_HEADER_NAME = "Strict-Transport-Security";

	public static final String HSTS_ENABLE_PARAMETER_NAME = "hsts.enable";
	public static final String HSTS_OVERWRITE_PARAMETER_NAME = "hsts.overwrite";
	public static final String HSTS_MAXAGE_PARAMETER_NAME = "hsts.maxAge";
	public static final String HSTS_INCLUDE_SUBDOMAINS_PARAMETER_NAME = "hsts.includeSubdomains";

	public static final String REFERRER_POLICY_HEADER_NAME = "Referrer-Policy";
	public static final String REFERRER_POLICY_ENABLE_PARAMETER_NAME = "referrer-policy.enable";
	public static final String REFERRER_POLICY_OVERWRITE_PARAMETER_NAME = "referrer-policy.overwrite";
	public static final String REFERRER_POLICY_POLICY_PARAMETER_NAME = "referrer-policy.policy";
	
	
	public static final boolean COMMON_ENABLE_DEFAULT = false;
	public static final boolean COMMON_OVERWRITE_DEFAULT = false;
	
	
	public static final int HSTS_MAXAGE_DEFAULT = 86400;			// Default: 1 day
	public static final boolean HSTS_INCLUDE_SUBDOMAINS_DEFAULT = true;
	
	
	private boolean hstsEnabled = COMMON_ENABLE_DEFAULT;
	private boolean hstsOverwrite = COMMON_OVERWRITE_DEFAULT;
	private int hstsMaxAge = HSTS_MAXAGE_DEFAULT;
	private boolean hstsIncludeSubdomains = HSTS_INCLUDE_SUBDOMAINS_DEFAULT;
	
	private boolean referrerPolicyEnabled = COMMON_ENABLE_DEFAULT;
	private boolean referrerPolicyOverwrite = COMMON_OVERWRITE_DEFAULT;
	private String referrerPolicyPolicy = null;

	@Override
	public final void destroy() {
		// Nothing to do here
	}

	@Override
	public final void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain filterChain) throws IOException, ServletException {
		final HttpServletResponseWrapper httpResponse = new HttpServletResponseWrapper((HttpServletResponse) response);

		insertHttpSecurityHeader(httpResponse);

		filterChain.doFilter(request, httpResponse);
	}
	
	private final void insertHttpSecurityHeader(final HttpServletResponse response) {
		if(hstsEnabled) {
			insertHstsHeader(response);
		}
		
		if(referrerPolicyEnabled) {
			insertReferrerPolicyHeader(response);
		}
	}

	private final void insertHstsHeader(final HttpServletResponse response) {
		if(hstsOverwrite || response.getHeader(HSTS_HEADER_NAME) == null) {
			response.addHeader(HSTS_HEADER_NAME, 
					String.format("max-age=%d%s", 
							hstsMaxAge,
							hstsIncludeSubdomains ? "; includeSubDomains" : ""
							));
		}
	}
	
	private final void insertReferrerPolicyHeader(final HttpServletResponse response) {
		if(referrerPolicyOverwrite || response.getHeader(REFERRER_POLICY_HEADER_NAME) == null) {
			if(referrerPolicyPolicy != null) {
				response.addHeader(REFERRER_POLICY_HEADER_NAME, referrerPolicyPolicy);
			}
		}
	}
	
	@Override
	public final void init(final FilterConfig config) throws ServletException {
		hstsEnabled = getBooleanInitParam(config, HSTS_ENABLE_PARAMETER_NAME, COMMON_ENABLE_DEFAULT);
		hstsOverwrite = getBooleanInitParam(config, HSTS_OVERWRITE_PARAMETER_NAME, COMMON_OVERWRITE_DEFAULT);
		hstsMaxAge = getIntInitParam(config, HSTS_MAXAGE_PARAMETER_NAME, 86400);
		hstsIncludeSubdomains = getBooleanInitParam(config, HSTS_INCLUDE_SUBDOMAINS_PARAMETER_NAME, HSTS_INCLUDE_SUBDOMAINS_DEFAULT);
		
		referrerPolicyEnabled = getBooleanInitParam(config, REFERRER_POLICY_ENABLE_PARAMETER_NAME, COMMON_ENABLE_DEFAULT);
		referrerPolicyOverwrite = getBooleanInitParam(config, REFERRER_POLICY_OVERWRITE_PARAMETER_NAME, COMMON_OVERWRITE_DEFAULT);
		referrerPolicyPolicy = getStringInitParam(config, REFERRER_POLICY_POLICY_PARAMETER_NAME, null);
	}
	
	private static final boolean getBooleanInitParam(final FilterConfig config, final String name, final boolean defaultValue) {
		final String value = config.getInitParameter(name);
		
		if(value == null) {
			return defaultValue;
		} else {
			try {
				return Boolean.parseBoolean(value);
			} catch(final Exception e) {
				return defaultValue;
			}
		}
	}
	
	private static final int getIntInitParam(final FilterConfig config, final String name, final int defaultValue) {
		final String value = config.getInitParameter(name);
		
		if(value == null) {
			return defaultValue;
		} else {
			try {
				return Integer.parseInt(value);
			} catch(final Exception e) {
				return defaultValue;
			}
		}
	}
	
	private static final String getStringInitParam(final FilterConfig config, final String name, final String defaultValue) {
		final String value = config.getInitParameter(name);
		
		return value != null
				? value
				: defaultValue;
	}

}
