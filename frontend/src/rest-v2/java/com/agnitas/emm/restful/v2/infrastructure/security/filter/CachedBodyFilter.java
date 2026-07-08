/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.restful.v2.infrastructure.security.filter;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Ensures that every request reaching the {@code DispatcherServlet} already has a cached body.
 * Wraps every {@link HttpServletRequest} into a {@link CachedBodyHttpServletRequest},
 * allowing the request body to be safely read multiple times later in the filter chain
 * or by Spring MVC components (e.g., XSS detection).
 * <p>
 * Normally, the Servlet input stream can be consumed only once.
 * This filter caches the body bytes during the first read and exposes them.
 */
public class CachedBodyFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        HttpServletRequest toUse = (request instanceof CachedBodyHttpServletRequest)
            ? request
            : new CachedBodyHttpServletRequest(request);
        filterChain.doFilter(toUse, response);
    }
}
