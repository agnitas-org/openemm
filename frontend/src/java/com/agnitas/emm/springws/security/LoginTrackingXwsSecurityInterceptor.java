/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.springws.security;

import com.agnitas.emm.springws.WebserviceUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.core.logintracking.service.LoginTrackService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.security.WsSecurityValidationException;
import org.springframework.ws.soap.security.wss4j2.Wss4jSecurityInterceptor;
import org.springframework.ws.soap.security.wss4j2.Wss4jSecurityValidationException;
import org.springframework.ws.transport.context.TransportContext;
import org.springframework.ws.transport.context.TransportContextHolder;
import org.springframework.ws.transport.http.HttpServletConnection;

import java.util.Objects;
import java.util.Optional;

public class LoginTrackingXwsSecurityInterceptor extends Wss4jSecurityInterceptor {

	private static final Logger LOGGER = LogManager.getLogger(LoginTrackingXwsSecurityInterceptor.class);
	
	private LoginTrackService loginTrackService;
	private ConfigService configService;
	
	public LoginTrackingXwsSecurityInterceptor() {
		// Empty
	}
	
	@Override
	protected void validateMessage(final SoapMessage message, final MessageContext context) throws WsSecurityValidationException {
		final boolean loginTrackingEnabled = this.configService.getBooleanValue(ConfigValue.LoginTracking.LoginTrackingWebserviceEnabled);		

		if(loginTrackingEnabled) {
			validateMessageWithLoginTracking(message, context);
		} else {
			super.validateMessage(message, context);
		}
		
	}
	
	private void validateMessageWithLoginTracking(final SoapMessage message, final MessageContext context) throws WsSecurityValidationException {
		try {
			super.validateMessage(message, context);
		} catch(final WsSecurityValidationException e) {
			// Cannot be done before super.validateMessage() because the security context will be setup in this method
			final Optional<WebserviceUserDetails> user = webserviceUserDetailsForRequest();
			final String ip = ipForRequest();
			final String usernameOrNull = user.map(User::getUsername).orElse(null);

			if(usernameOrNull != null) {
				if(LOGGER.isInfoEnabled()) {
					LOGGER.info(String.format("Login for user '%s' failed", usernameOrNull), e);
				}
			} else {
				if(LOGGER.isInfoEnabled()) {
					LOGGER.info("Login for unknown user failed", e);
				}
			}

			this.loginTrackService.trackLoginFailed(ip, usernameOrNull);
				
			
			logger.fatal("STACK TRACE", e);
			throw e;
		}
	
		// Cannot be done before super.validateMessage() because the security context will be setup in this method
		final Optional<WebserviceUserDetails> user = webserviceUserDetailsForRequest();
		final String ip = ipForRequest();
		final String usernameOrNull = user.map(User::getUsername).orElse(null);
		final int companyID = user.map(WebserviceUserDetails::getCompanyID).orElse(0);
		
		if(this.loginTrackService.isIpAddressLocked(ip, companyID)) {
			if(LOGGER.isInfoEnabled()) {
				LOGGER.info(String.format("Login for user '%s' blocked by IP lock", usernameOrNull));
			}

			this.loginTrackService.trackLoginSuccessfulButBlocked(ip, usernameOrNull);

			throw new Wss4jSecurityValidationException("IP blocked");
		} else {
			if(LOGGER.isInfoEnabled()) {
				LOGGER.info(String.format("Login for user '%s' successful", usernameOrNull));
			}
			
			this.loginTrackService.trackLoginSuccessful(ip, usernameOrNull);
		}
	}
	
	private final Optional<WebserviceUserDetails> webserviceUserDetailsForRequest() {
		final Optional<UserDetails> details = userDetailsForRequest();
		
		return details.isPresent() && (details.get() instanceof WebserviceUserDetails)
				? Optional.of((WebserviceUserDetails) details.get())
				: Optional.empty();
	}
	
	private final Optional<UserDetails> userDetailsForRequest() {
		final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null) {
			return Optional.empty();
		}

		return Optional.ofNullable(((UserDetails) authentication.getDetails()));
	}
	
	private final String ipForRequest() {
		final TransportContext context = TransportContextHolder.getTransportContext();
		HttpServletConnection connection = (HttpServletConnection) context.getConnection();
		HttpServletRequest request = connection.getHttpServletRequest();
		
		return request.getRemoteAddr();
	}
	
	public final void setLoginTrackService(final LoginTrackService service) {
		this.loginTrackService = Objects.requireNonNull(service, "Login tracking service is null");
	}

	public final void setConfigService(final ConfigService service) {
		this.configService = Objects.requireNonNull(service, "ConfigService is null");
	}
}
