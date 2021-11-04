/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.springws.throttling.tokenbucket.service;

import java.util.Objects;
import java.util.Optional;

import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.emm.util.quota.tokenbucket.AbstractLocalBucketManager;
import com.agnitas.emm.wsmanager.bean.WebserviceUserSettings;
import com.agnitas.emm.wsmanager.common.WebserviceUserException;
import com.agnitas.emm.wsmanager.service.WebserviceUserService;
import com.agnitas.emm.wsmanager.service.WebserviceUserServiceException;

/**
 * Implementation of {@link AbstractLocalBucketManager} that does not 
 * share token buckets across different machines.
 */
public final class LocalBucketManager extends AbstractLocalBucketManager {
	
	/** The logger. */
	private static final transient Logger LOGGER = Logger.getLogger(LocalBucketManager.class);
	
	/** Service for handling webservice users. */
	private WebserviceUserService userService;
	
	/** Configuration service. */
	private ConfigService configService;
	
	/**
	 * Set service handling webservice users.
	 * 
	 * @param service service handling webservice users
	 */
	@Required
	public final void setWebserviceUserService(final WebserviceUserService service) {
		this.userService = Objects.requireNonNull(service, "Webservice user service is null");
	}
	
	/**
	 * Set configuration service.
	 * 
	 * @param service configuration service
	 */
	@Required
	public final void setConfigService(final ConfigService service) {
		this.configService = Objects.requireNonNull(service, "ConfigService is null");
	}

	@Override
	protected final Optional<String> findBandwidthSettingsForUser(final String username, final int companyId) {
		try {
			final WebserviceUserSettings settings = this.userService.findSettingsForWebserviceUser(username);
			
			return settings.getApiCallLimitsSpec();
		} catch(final WebserviceUserException | WebserviceUserServiceException e) {
			if(LOGGER.isInfoEnabled()) {
				LOGGER.info(String.format("Could not read API call limits for webservice user '%s'", username), e);
			}
			
			return Optional.empty();
		}
	}

	@Override
	protected String findDefaultBandwidthSettings(final int companyId) {
		return configService.getValue(ConfigValue.Webservices.DefaultApiCallLimits, companyId);
	}

}
