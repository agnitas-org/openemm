/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.security.sessionbinding.web.filter;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

import com.agnitas.beans.Admin;
import com.agnitas.emm.security.sessionbinding.common.SessionBindingConstants;
import com.agnitas.emm.security.sessionbinding.common.SessionBindingData;
import com.agnitas.emm.security.sessionbinding.web.CookieUtils;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import com.agnitas.util.AgnUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class SessionBindingRequestFilter implements Filter {

    /** The logger. */
	private static final transient Logger LOGGER = LogManager.getLogger(SessionBindingRequestFilter.class);

    private final Set<InetAddress> excludedClientAddresses = new HashSet<>();

    private static final class AccessDeniedException extends Exception {
        // Empty
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);

        setupExcludedClients(filterConfig.getInitParameter("excluded-clients"));
    }

    @Override
    public final void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse, final FilterChain filterChain) throws IOException, ServletException {
        if((servletRequest instanceof HttpServletRequest httpServletRequest) && (servletResponse instanceof HttpServletResponse httpServletResponse)) {
            try {
                if(!isClientExcluded(httpServletRequest)) {
                    checkAndUpdateSessionBinding(httpServletRequest, httpServletResponse);
                } else {
                    if(LOGGER.isInfoEnabled()) {
                        LOGGER.info(String.format("Client '%s' excluded from session binding check", servletRequest.getRemoteAddr()));
                    }
                }

                filterChain.doFilter(httpServletRequest, httpServletResponse);
            } catch(final AccessDeniedException e) {
                sendForbidden(httpServletResponse);
            }
        } else {
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }

    private final boolean isClientExcluded(final HttpServletRequest request) {
        try {
            final InetAddress requestAddress = InetAddress.getByName(request.getRemoteAddr());

            return this.excludedClientAddresses.contains(requestAddress);
        } catch(final UnknownHostException e) {
            LOGGER.warn(String.format("Unable to resolve host '%s'. Assuming client is not excluded.", request.getRemoteAddr()));

            return false;
        }
    }

    private final void checkAndUpdateSessionBinding(final HttpServletRequest request, final HttpServletResponse response) throws AccessDeniedException {
        final HttpSession session = request.getSession(false);

        if(session == null) {
            if(LOGGER.isInfoEnabled()) {
                LOGGER.info("Request has no session.");
            }
        } else {
            final Admin admin = AgnUtils.getAdmin(session);

            if(admin == null) {
                if(LOGGER.isInfoEnabled()) {
                    LOGGER.info(String.format("No user logged in to session '%s'", session.getId()));
                }
            } else {
                final SessionBindingData sessionBindingData = (SessionBindingData) session.getAttribute(SessionBindingConstants.HTTP_SESSION_BINDING_DATA_ATTRIBUTE);

                if(sessionBindingData == null) {
                    LOGGER.warn(String.format("No session binding data for session '%s' (user: '%s')", session.getId(), admin.getUsername()));

                    throw new AccessDeniedException();
                } else {
                    final Cookie cookie = findCookie(request, CookieUtils.createCookieName(sessionBindingData));

                    if(cookie == null) {
                        LOGGER.warn(String.format("No session binding cookie for session '%s' (user: '%s')", session.getId(), admin.getUsername()));

                        throw new AccessDeniedException();
                    } else {
                        if (!sessionBindingData.getSecurityToken().equals(cookie.getValue())) {
                            LOGGER.warn(String.format("Mismatching session binding data for session '%s' (user: '%s')", session.getId(), admin.getUsername()));

                            throw new AccessDeniedException();
                        } else {
                            if (LOGGER.isInfoEnabled()) {
                                LOGGER.info(String.format("Renewing session binding cookie for session '%s' (user: '%s')", session.getId(), admin.getUsername()));
                            }

                            final Cookie newCookie = CookieUtils.createSessionBindingCookie(sessionBindingData, session.getMaxInactiveInterval() + SessionBindingConstants.SESSION_IDLE_GRACE_SECONDS);

                            response.addCookie(newCookie);
                        }
                    }
                }
            }
        }
    }


    private static final Cookie findCookie(final HttpServletRequest request, final String name) {
        final Cookie[] cookies = request.getCookies();

        if(cookies != null) {
            for (final Cookie cookie : cookies) {
                if (name.equals(cookie.getName())) {
                    return cookie;
                }
            }
        }

        return null; // No cookie of given name found
    }

    private final void sendForbidden(final HttpServletResponse response) throws IOException {
        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
    }

    private final void setupExcludedClients(final String excludedClientsList) {
        if(excludedClientsList != null) {
            final String[] excludedClientsArray = excludedClientsList.trim().split("\\s*[,;]\\s*");

            for (final String excludedClientString : excludedClientsArray) {
                try {
                    final InetAddress[] clientAddresses = InetAddress.getAllByName(excludedClientString);

                    for(final InetAddress clientAddress : clientAddresses) {
                        if (LOGGER.isInfoEnabled()) {
                            LOGGER.info(String.format("Excluding '%s' (%s) from session binding checks", excludedClientString, clientAddress.getHostAddress()));
                        }

                        this.excludedClientAddresses.add(clientAddress);
                    }
                } catch (final UnknownHostException e) {
                    if(LOGGER.isDebugEnabled()) {
                        LOGGER.debug(String.format("Could not resolve host '%s'. Host not excluded from session binding.", excludedClientString), e);
                    } else {
                        LOGGER.warn(String.format("Could not resolve host '%s'. Host not excluded from session binding.", excludedClientString));
                    }
                }
            }
        }
    }

}
