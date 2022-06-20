/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.commons.filter;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

public class OriginUriFilter implements Filter {
    public static final String ORIGIN_URI_KEY = "com.agnitas.emm.ORIGIN_URI";
    public static final String ORIGIN_QUERY_STRING_KEY = "com.agnitas.emm.ORIGIN_QUERY_STRING";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Nothing to do on initialization
    }

    @Override
    public void destroy() {
        // Nothing to do on shutdown
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (request instanceof HttpServletRequest) {
            request.setAttribute(ORIGIN_URI_KEY, ((HttpServletRequest) request).getRequestURI());
            request.setAttribute(ORIGIN_QUERY_STRING_KEY, ((HttpServletRequest) request).getQueryString());
        }
        chain.doFilter(request, response);
    }
}
