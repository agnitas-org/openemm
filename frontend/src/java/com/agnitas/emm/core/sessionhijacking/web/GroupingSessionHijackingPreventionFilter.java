/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.sessionhijacking.web;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Objects;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.agnitas.emm.core.commons.util.ConfigService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.agnitas.emm.core.sessionhijacking.service.SessionHijackingPreventionService;

public final class GroupingSessionHijackingPreventionFilter implements Filter {
	
	/** The logger. */
	private static final transient Logger logger = LogManager.getLogger(GroupingSessionHijackingPreventionFilter.class);
	
	private SessionHijackingPreventionService sessionHijackingPreventionService;
	private ConfigService configService;

	@Override
	public final void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain filterChain) throws IOException, ServletException {
		assert this.sessionHijackingPreventionService != null;	// Cannot be null here, if life cycle of filter is fulfilled as specified

		final HttpSession httpSession = ((HttpServletRequest) request).getSession(false); // Use "false" here to prevent container from creating new session

		if(httpSession != null) {
			// Session exists, check IP address
			final InetAddress clientIpAddress = InetAddress.getByName(request.getRemoteAddr());
			final InetAddress sessionIpAddress = IpBinding.getBoundIpAddress(httpSession);

			if(sessionIpAddress == null) {
				// No IP address bound to session.
				if(logger.isInfoEnabled()) {
					logger.info("No IP address bound to session " + httpSession.getId() + " - binding IP address");
				}
				
				// Bind IP address
				IpBinding.bindIpAddress(httpSession, clientIpAddress);
			} else {
				// IP address bound to session. Check if IP address of client is allowed for the session
				if(!this.sessionHijackingPreventionService.isAddressAllowed(sessionIpAddress, clientIpAddress)) {
					if(this.configService.isSessionHijackingPreventionEnabled()) {
						// Hijacking prevention enabled? Kill the session.
						
						logger.warn(String.format("IP address of client is not allowed for current session - invalidating session %s (session: %s, client: %s, URL: %s, referer: %s)", 
								httpSession.getId(),
								sessionIpAddress,
								clientIpAddress,
								((HttpServletRequest)request).getRequestURL().toString(),
								((HttpServletRequest)request).getHeader("referer")
								));
	
						// Invalidate old session	
						httpSession.invalidate();
	
						// Create new session with current IP address of client
						final HttpSession newHttpSession = ((HttpServletRequest) request).getSession();
						newHttpSession.setAttribute(SessionHijackingPreventionConstants.FORCED_LOGOUT_MARKER_ATTRIBUTE_NAME, Boolean.TRUE);
						
						IpBinding.bindIpAddress(newHttpSession, clientIpAddress);
					} else {
						// Hijacking prevention disabled? Log warning, but keep session alive.
						
						logger.warn(String.format("IP address of client is not allowed for current session - session would be invalidated, if session hijacking prevention is enabled. %s (session: %s, client: %s, URL: %s, referer: %s)", 
								httpSession.getId(),
								sessionIpAddress,
								clientIpAddress,
								((HttpServletRequest)request).getRequestURL().toString(),
								((HttpServletRequest)request).getHeader("referer")
								));
					}
				} else {
					if (logger.isInfoEnabled()) {
						logger.info("IP addresses of client is valid for current session");
					}
				}
			}
		} else {
			if(logger.isInfoEnabled()) {
				logger.info("No session available, proceeding without further handling");
			}
		}
		
		// Propagate request to next filter in chain
		if(filterChain != null) {
			filterChain.doFilter(request, response);
		}
	}
	

	@Override
	public final void init(final FilterConfig filterConfig) throws ServletException {
		if (logger.isInfoEnabled()) {
			logger.info("Initializing " + this.getClass().getCanonicalName());
			logger.info("Session attribute for client IP address is " + IpBinding.IP_ATTRIBUTE);
		}

		try {
			final WebApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(filterConfig.getServletContext());
			
			this.sessionHijackingPreventionService = Objects.requireNonNull(
					context.getBean("SessionHijackingPreventionService", SessionHijackingPreventionService.class),
					"SessionHijackingPreventionService is set to null within init() method!"
					);
			
			this.configService = Objects.requireNonNull(
					context.getBean("ConfigService", ConfigService.class),
					"ConfigService is set to null within init() method!"
					);
		} catch (final Exception e) {
			final String msg = String.format("Cannot initialize filter of class '%s'", this.getClass().getCanonicalName());
			
			logger.error(msg, e);
			throw new ServletException(msg, e);
		}

	}

	@Override
	public final void destroy() {
		// No special shutdown procedure required
	}

}
