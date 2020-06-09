/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.landingpage.dao;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import org.agnitas.dao.impl.BaseDaoImpl;
import org.agnitas.dao.impl.mapper.StringRowMapper;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.emm.landingpage.beans.RedirectSettings;

public final class LandingpageDaoImpl extends BaseDaoImpl implements com.agnitas.emm.landingpage.dao.LandingpageDao {
	
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(LandingpageDaoImpl.class);
	
	private ConfigService configService;

	@Required
	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}

	/**
	 * Finds the fitting landingpage entry for the requested domain.
	 * If there is no exact match for the domain, we take the best fitting domain, which the requested domain is a subdomain of.
	 * If there is no best fitting entry we take the RdirLandingpage entry from configService.
	 */
	@Override
	public String getLandingPage(String requestUrl) {
		if (requestUrl == null) {
			return configService.getValue(ConfigValue.RdirLandingpage, "www.agnitas.de");
		} else {
			requestUrl = requestUrl.toLowerCase();
			
			String domain;
			try {
				URI uri = new URI(requestUrl);
				domain = uri.getHost();
				if (domain == null) {
					throw new Exception("Empty request domain");
				} else if (domain.startsWith("www.")) {
					domain = domain.substring(4);
				}
			} catch (Exception e) {
				logger.error("Cannot find landingpage: " + e.getMessage(), e);
				return configService.getValue(ConfigValue.RdirLandingpage, "www.agnitas.de");
			}
				
			List<String> result;
			if (isOracleDB()) {
				result = select(logger, "SELECT landingpage FROM landingpage_tbl WHERE LOWER(domain) = ? OR ? LIKE '%.' || LOWER(domain) ORDER BY LENGTH(domain) DESC", new StringRowMapper(), domain, domain);
			} else {
				result = select(logger, "SELECT landingpage FROM landingpage_tbl WHERE LOWER(domain) = ? OR ? LIKE CONCAT('%.', LOWER(domain)) ORDER BY LENGTH(domain) DESC", new StringRowMapper(), domain, domain);
			}
			
			if (result.size() > 0) {
				return result.get(0);			
			} else {
				return configService.getValue(ConfigValue.RdirLandingpage, "www.agnitas.de");
			}
		}
	}

	@Override
	public final Optional<RedirectSettings> getLandingPageRedirectionForDomain(final String domain) {
		if(domain == null) {
			return Optional.empty();
		} else {
			final String sql = isOracleDB() 
					? "SELECT * FROM landingpage_tbl WHERE LOWER(domain) = ? OR ? LIKE '%.' || LOWER(domain) ORDER BY LENGTH(domain) DESC"
					: "SELECT * FROM landingpage_tbl WHERE LOWER(domain) = ? OR ? LIKE CONCAT('%.', LOWER(domain)) ORDER BY LENGTH(domain) DESC";
			
			final List<RedirectSettings> result = select(logger, sql, new RedirectSettingsRowMapper(), domain, domain);

			return !result.isEmpty()
					? Optional.of(result.get(0))
					: Optional.empty();
		}
	}
}
