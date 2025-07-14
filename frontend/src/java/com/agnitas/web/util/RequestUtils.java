/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web.util;

import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Logger;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Utility class dealing with {@link HttpServletRequest}s.
 */
public final class RequestUtils {
	
	private static final Pattern RANGE_VALUE_PATTERN = Pattern.compile("^([^=]*)=(\\d*)-(\\d*)$");

	/**
	 * Checks, if the request is a range request.
	 * 
	 * @param request {@link HttpServletRequest}
	 * 
	 * @return <code>true</code> if request contains Http-Header &quot;Range&quot;
	 */
	public static final boolean hasRangeHeader(final HttpServletRequest request) {
		return hasHeader(HttpHeaderName.RANGE, request);
	}
	
	/**
	 * Checks, if the request contains given HTTP header.
	 * 
	 * @param headerName HTTP header
	 * @param request {@link HttpServletRequest};
	 * 
	 * @return <code>true</code> if request contains given header
	 */
	public static final boolean hasHeader(final HttpHeaderName headerName, final HttpServletRequest request) {
		final String headerValue = request.getHeader(headerName.getHeaderName());
		
		return headerValue != null;
	}
	
	/**
	 * Returns the start value of the HTTP &quot;Range&quot; header.
	 * If header is missing or value does not match the expected pattern, -1 is returned.
	 * 
	 * @param request HTTP request
	 * 
	 * @return start value of Range header or -1
	 */
	public static final int getRangeRequestStart(final HttpServletRequest request) {
		try {
			final String headerValue = request.getHeader(HttpHeaderName.RANGE.getHeaderName());
			
			if(headerValue == null) {
				return -1;
			} else {
				final Matcher matcher = RANGE_VALUE_PATTERN.matcher(headerValue);
				
				if(!matcher.matches()) {
					return -1;
				} else {
					return Integer.parseInt(matcher.group(2));
				}
			}
		} catch(final Throwable t) {
			return -1;
		}
	}
	
	/**
	 * Simple dumping of request data to logger.
	 * 
	 * @param request {@link HttpServletRequest} to be dumped
	 * @param logger Log4j {@link Logger}
	 * @param level Log level
	 */
	public static final void dumpRequest(final HttpServletRequest request, final Logger logger) {
		logger.warn("    Query String: " + request.getQueryString());
		final Enumeration<String> names = request.getHeaderNames();
		while(names.hasMoreElements()) {
			final String name = names.nextElement();
			final Enumeration<String> values = request.getHeaders(name);
			
			while(values.hasMoreElements()) {
				logger.warn("    " + name + " : " + values.nextElement());
			}
		}

	}
}
