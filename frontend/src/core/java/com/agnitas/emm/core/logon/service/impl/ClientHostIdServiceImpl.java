/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.logon.service.impl;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import com.agnitas.emm.core.logon.service.ClientHostIdService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.core.commons.util.ConfigValue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.util.WebUtils;

public final class ClientHostIdServiceImpl implements ClientHostIdService {

	private static final Logger LOGGER = LogManager.getLogger(ClientHostIdServiceImpl.class);
	
	private ConfigService configService;

	@Override
	public String createHostId() { // TODO: This method cannot be unit-tested. Move generator code behind interface
									// (see security code generator)
		LOGGER.info("Creating new UUID for host identification");

		final UUID uuid0 = UUID.randomUUID();
		final UUID uuid1 = UUID.randomUUID();

		return String.format("%s-%s", uuid0, uuid1);
	}

	@Override
	public Optional<String> getClientHostId(HttpServletRequest request) {
        final Cookie cookie = WebUtils.getCookie(request, configService.getHostAuthenticationHostIdCookieName());

        if (cookie == null) {
			LOGGER.info("No host ID cookie found for host {} ({})", request.getRemoteHost(), request.getRemoteAddr());
            return Optional.empty();
        }

		LOGGER.info("Found host ID '{}' found for host {} ({})", cookie.getValue(), request.getRemoteHost(), request.getRemoteAddr());
        return Optional.of(cookie.getValue());
	}

	@Override
	public void createAndPublishHostAuthenticationCookie(String hostId, int companyId, HttpServletResponse response) {
        final Cookie cookie = new Cookie(configService.getHostAuthenticationHostIdCookieName(), hostId);
        final boolean useSecureCookie = configService.getBooleanValue(ConfigValue.HostauthenticationCookiesHttpsOnly);

        cookie.setMaxAge(daysToSeconds(configService.getIntegerValue(ConfigValue.HostAuthenticationHostIdCookieExpireDays, companyId)));
        cookie.setSecure(useSecureCookie);
		cookie.setHttpOnly(true);
		cookie.setPath("/");

        response.addCookie(cookie);
	}

	private static int daysToSeconds(int days) {
		return days * 86400;
	}
	
	public void setConfigService(ConfigService service) {
		this.configService = Objects.requireNonNull(service, "Config service is null");
	}
}
