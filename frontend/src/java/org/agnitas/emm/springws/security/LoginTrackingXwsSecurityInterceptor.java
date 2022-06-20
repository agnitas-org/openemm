/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.springws.security;

import java.util.Objects;
import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;

import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.core.logintracking.service.LoginTrackService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.security.WsSecurityValidationException;
import org.springframework.ws.soap.security.xwss.XwsSecurityInterceptor;
import org.springframework.ws.soap.security.xwss.XwsSecurityValidationException;
import org.springframework.ws.transport.context.TransportContext;
import org.springframework.ws.transport.context.TransportContextHolder;
import org.springframework.ws.transport.http.HttpServletConnection;

import com.agnitas.emm.springws.WebserviceUserDetails;
import com.agnitas.emm.wsmanager.service.WebserviceUserService;

public class LoginTrackingXwsSecurityInterceptor extends XwsSecurityInterceptor {

	private static final transient Logger LOGGER = LogManager.getLogger(LoginTrackingXwsSecurityInterceptor.class);
	
	private LoginTrackService loginTrackService;
	private final WebserviceUserService webserviceUserService;
	private ConfigService configService;
	
	public LoginTrackingXwsSecurityInterceptor(final WebserviceUserService webserviceUserService) {
		this.webserviceUserService = Objects.requireNonNull(webserviceUserService, "WebserviceUserService is null");
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
			final String usernameOrNull = user.isPresent() 
					? user.get().getUsername()
					: null;

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
				
			throw e;
		}
	
		// Cannot be done before super.validateMessage() because the security context will be setup in this method
		final Optional<WebserviceUserDetails> user = webserviceUserDetailsForRequest();
		final String ip = ipForRequest();
		final String usernameOrNull = user.isPresent() 
				? user.get().getUsername()
				: null;
		final int companyID = user.isPresent() ? user.get().getCompanyID() : 0;
		
		if(this.loginTrackService.isIpAddressLocked(ip, companyID)) {
			if(LOGGER.isInfoEnabled()) {
				LOGGER.info(String.format("Login for user '%s' blocked by IP lock", usernameOrNull));
			}

			this.loginTrackService.trackLoginSuccessfulButBlocked(ip, usernameOrNull);
			
			throw new XwsSecurityValidationException("IP blocked");
		} else {
			if(LOGGER.isInfoEnabled()) {
				LOGGER.info(String.format("Login for user '%s' successful", usernameOrNull));
			}
			
			this.loginTrackService.trackLoginSuccessful(ip, usernameOrNull);
			
			if(usernameOrNull != null) {
				this.webserviceUserService.updateLastLoginDate(usernameOrNull);
			}
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
		
		return authentication != null 
				? Optional.of((UserDetails) authentication.getPrincipal())
				: Optional.empty();
	}
	
	private final String ipForRequest() {
		final TransportContext context = TransportContextHolder.getTransportContext();
		HttpServletConnection connection = (HttpServletConnection) context.getConnection();
		HttpServletRequest request = connection.getHttpServletRequest();
		
		return request.getRemoteAddr();
	}
	
	@Required
	public final void setLoginTrackService(final LoginTrackService service) {
		this.loginTrackService = Objects.requireNonNull(service, "Login tracking service is null");
	}

	@Required
	public final void setConfigService(final ConfigService service) {
		this.configService = Objects.requireNonNull(service, "ConfigService is null");
	}
}
