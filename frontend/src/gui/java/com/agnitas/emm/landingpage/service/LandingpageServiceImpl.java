/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.landingpage.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

import com.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.core.commons.util.ConfigValue;
import com.agnitas.emm.landingpage.beans.RedirectSettings;
import com.agnitas.emm.landingpage.dao.LandingpageDao;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

@Service
public final class LandingpageServiceImpl implements LandingpageService {
	
	private static final Logger LOGGER = LogManager.getLogger(LandingpageServiceImpl.class);
	
	private final LandingpageDao landingpageDao;
	private final ConfigService configService;

    public LandingpageServiceImpl(LandingpageDao landingpageDao, ConfigService configService) {
        this.landingpageDao = landingpageDao;
        this.configService = configService;
    }

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
			} catch (URISyntaxException e) {
				LOGGER.error("Malformed request URI: '{}'", requestUrl, e);
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

}
