/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.landingpage.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.Optional;

import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.emm.landingpage.beans.RedirectSettings;
import com.agnitas.emm.landingpage.dao.LandingpageDao;

public final class LandingpageServiceImpl implements LandingpageService {
	
	/** The logger. */
	private static final transient Logger LOGGER = Logger.getLogger(LandingpageServiceImpl.class);
	
	private LandingpageDao landingpageDao;
	private ConfigService configService;

	@Override
	public final RedirectSettings getLandingPageRedirection(final String url) {
		if (url == null) {
			return fallbackRedirect();
		} else {
			final String requestUrl = url.toLowerCase();

			try {
				final Optional<String> domainOpt = extractDomain(requestUrl);
				
				if(domainOpt.isPresent()) {
					final Optional<RedirectSettings> settingsOpt = this.landingpageDao.getLandingPageRedirectionForDomain(domainOpt.get());

					return settingsOpt.orElse(fallbackRedirect());
				} else {
					return fallbackRedirect();
				}
			} catch(final URISyntaxException e) {
				LOGGER.error(String.format("Malformed request URI: '%s'", requestUrl), e);
				
				return fallbackRedirect();
			}
		}
	}
	
	private final RedirectSettings fallbackRedirect() {
		final String redirectUrl = configService.getValue(ConfigValue.RdirLandingpage);
		
		// Do a HTML meta redirect
		return new RedirectSettings(redirectUrl, 0);
	}
	
	private static final Optional<String> extractDomain(final String url) throws URISyntaxException {
		final String requestUrl = url.toLowerCase();

		final URI uri = new URI(requestUrl);
		final String domain = uri.getHost();
		
		if (domain == null) {
			return Optional.empty();
		} else {
			return domain.startsWith("www.")
					? Optional.of(domain.substring(4))
					: Optional.of(domain);
		}
	}
	
	@Required
	public final void setLandingpageDao(final LandingpageDao dao) {
		this.landingpageDao = Objects.requireNonNull(dao, "LandingpageDAO is null");
	}

	@Required
	public final void setConfigService(final ConfigService configService) {
		this.configService = Objects.requireNonNull(configService, "Config service is null");
	}
	
}
