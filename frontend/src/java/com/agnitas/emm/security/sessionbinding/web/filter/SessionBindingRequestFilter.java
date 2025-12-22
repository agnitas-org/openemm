/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.security.sessionbinding.web.filter;

import static com.agnitas.emm.security.sessionbinding.common.SessionBindingConstants.SKIP_COOKIE_CHECK_ON_FIRST_REQUEST;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

import com.agnitas.beans.Admin;
import com.agnitas.emm.security.sessionbinding.common.SessionBindingConstants;
import com.agnitas.emm.security.sessionbinding.common.SessionBindingData;
import com.agnitas.emm.security.sessionbinding.web.CookieUtils;
import com.agnitas.util.AgnUtils;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class SessionBindingRequestFilter implements Filter {

	private static final Logger LOGGER = LogManager.getLogger(SessionBindingRequestFilter.class);

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
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if((servletRequest instanceof HttpServletRequest httpServletRequest) && (servletResponse instanceof HttpServletResponse httpServletResponse)) {
            try {
                if(!isClientExcluded(httpServletRequest)) {
                    checkAndUpdateSessionBinding(httpServletRequest, httpServletResponse);
                } else {
                    LOGGER.info("Client '{}' excluded from session binding check", servletRequest.getRemoteAddr());
                }

                filterChain.doFilter(httpServletRequest, httpServletResponse);
            } catch (AccessDeniedException e) {
                sendForbidden(httpServletResponse);
            }
        } else {
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }

    private boolean isClientExcluded(HttpServletRequest request) {
        try {
            final InetAddress requestAddress = InetAddress.getByName(request.getRemoteAddr());

            if (requestAddress.isLoopbackAddress()) {
                return true;
            }

            return this.excludedClientAddresses.contains(requestAddress);
        } catch (UnknownHostException e) {
            LOGGER.warn(String.format("Unable to resolve host '%s'. Assuming client is not excluded.", request.getRemoteAddr()));
            return false;
        }
    }

    private void checkAndUpdateSessionBinding(HttpServletRequest request, HttpServletResponse response) throws AccessDeniedException {
        HttpSession session = request.getSession(false);

        if (session == null) {
            LOGGER.info("Request has no session.");
            return;
        }

        Admin admin = AgnUtils.getAdmin(session);

        if (admin == null) {
            LOGGER.info("No user logged in to session '{}'", session.getId());
            return;
        }

        SessionBindingData sessionBindingData = (SessionBindingData) session.getAttribute(SessionBindingConstants.HTTP_SESSION_BINDING_DATA_ATTRIBUTE);

        if (sessionBindingData == null) {
            LOGGER.warn("No session binding data for session '{}' (user: '{}')", session.getId(), admin.getUsername());
            throw new AccessDeniedException();
        }

        if (session.getAttribute(SKIP_COOKIE_CHECK_ON_FIRST_REQUEST) != null) {
            session.removeAttribute(SKIP_COOKIE_CHECK_ON_FIRST_REQUEST);
            updateCookie(response, session, admin, sessionBindingData);
            return;
        }

        Cookie cookie = findCookie(request, CookieUtils.createCookieName(sessionBindingData));

        if (cookie == null) {
            LOGGER.warn("No session binding cookie for session '{}' (user: '{}'). URL: {}", session.getId(), admin.getUsername(), request.getRequestURL());
            throw new AccessDeniedException();
        }

        if (!sessionBindingData.getSecurityToken().equals(cookie.getValue())) {
            LOGGER.warn("Mismatching session binding data for session '{}' (user: '{}')", session.getId(), admin.getUsername());
            throw new AccessDeniedException();
        }

        updateCookie(response, session, admin, sessionBindingData);
    }

    private void updateCookie(HttpServletResponse response, HttpSession session, Admin admin, SessionBindingData sessionBindingData) {
        LOGGER.info("Renewing session binding cookie for session '{}' (user: '{}')", session.getId(), admin.getUsername());
        response.addCookie(CookieUtils.createSessionBindingCookie(
                sessionBindingData,
                session.getMaxInactiveInterval() + SessionBindingConstants.SESSION_IDLE_GRACE_SECONDS
        ));
    }

    private static Cookie findCookie(HttpServletRequest request, String name) {
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

    private void sendForbidden(HttpServletResponse response) throws IOException {
        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
    }

    private void setupExcludedClients(String excludedClientsList) {
        if (excludedClientsList == null) {
            return;
        }

        for (String excludedClientString : excludedClientsList.trim().split("\\s*[,;]\\s*")) {
            try {
                final InetAddress[] clientAddresses = InetAddress.getAllByName(excludedClientString);

                for (InetAddress clientAddress : clientAddresses) {
                    if (LOGGER.isInfoEnabled()) {
                        LOGGER.info("Excluding '{}' ({}) from session binding checks", excludedClientString, clientAddress.getHostAddress());
                    }

                    this.excludedClientAddresses.add(clientAddress);
                }
            } catch (UnknownHostException e) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(String.format("Could not resolve host '%s'. Host not excluded from session binding.", excludedClientString), e);
                } else {
                    LOGGER.warn(String.format("Could not resolve host '%s'. Host not excluded from session binding.", excludedClientString));
                }
            }
        }
    }

}
