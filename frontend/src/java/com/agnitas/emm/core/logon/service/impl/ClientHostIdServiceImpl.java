/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.logon.service.impl;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.util.WebUtils;

import com.agnitas.emm.core.logon.service.ClientHostIdService;

public final class ClientHostIdServiceImpl implements ClientHostIdService {

	/** The logger. */
	private static final transient Logger LOGGER = LogManager.getLogger(ClientHostIdServiceImpl.class);
	
	private ConfigService configService;

	@Override
	public final String createHostId() { // TODO: This method cannot be unit-tested. Move generator code behind interface
									// (see security code generator)
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("Creating new UUID for host identification");
		}

		final UUID uuid0 = UUID.randomUUID();
		final UUID uuid1 = UUID.randomUUID();

		return String.format("%s-%s", uuid0.toString(), uuid1.toString());
	}

	@Override
	public final Optional<String> getClientHostId(final HttpServletRequest request) {
        final Cookie cookie = WebUtils.getCookie(request, configService.getHostAuthenticationHostIdCookieName());

        if (cookie == null) {
            if (LOGGER.isInfoEnabled()) {
            	LOGGER.info("No host ID cookie found for host " + request.getRemoteHost() + " (" + request.getRemoteAddr() + ")");
            }

            return Optional.empty();
        }

        if (LOGGER.isInfoEnabled()) {
        	LOGGER.info("Found host ID '" + cookie.getValue() + "' found for host " + request.getRemoteHost() + " (" + request.getRemoteAddr() + ")");
        }

        return Optional.of(cookie.getValue());
	}

	@Override
	public final void createAndPublishHostAuthenticationCookie(final String hostId, final int companyId, final HttpServletResponse response) {
        final Cookie cookie = new Cookie(configService.getHostAuthenticationHostIdCookieName(), hostId);
        final boolean useSecureCookie = configService.getBooleanValue(ConfigValue.HostauthenticationCookiesHttpsOnly);

        cookie.setMaxAge(daysToSeconds(configService.getIntegerValue(ConfigValue.HostAuthenticationHostIdCookieExpireDays, companyId)));
        cookie.setSecure(useSecureCookie);
		cookie.setHttpOnly(true);
		cookie.setPath("/");

        response.addCookie(cookie);
	}

	private static final int daysToSeconds(final int days) {
		return days * 86400;
	}
	
	@Required
	public final void setConfigService(final ConfigService service) {
		this.configService = Objects.requireNonNull(service, "Config service is null");
	}
}
