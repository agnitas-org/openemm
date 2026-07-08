/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.springws.throttling.tokenbucket.service;

import java.util.Objects;
import java.util.Optional;

import com.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.core.commons.util.ConfigValue;
import com.agnitas.emm.util.quota.tokenbucket.AbstractLocalBucketManager;
import com.agnitas.emm.wsmanager.bean.WebserviceUserSettings;
import com.agnitas.emm.wsmanager.service.WebserviceUserService;

/**
 * Implementation of {@link AbstractLocalBucketManager} that does not 
 * share token buckets across different machines.
 */
public final class LocalBucketManager extends AbstractLocalBucketManager {
	
	/** Service for handling webservice users. */
	private WebserviceUserService userService;
	
	/** Configuration service. */
	private ConfigService configService;
	
	/**
	 * Set service handling webservice users.
	 * 
	 * @param service service handling webservice users
	 */
	public void setWebserviceUserService(WebserviceUserService service) {
		this.userService = Objects.requireNonNull(service, "Webservice user service is null");
	}
	
	/**
	 * Set configuration service.
	 * 
	 * @param service configuration service
	 */
	public void setConfigService(ConfigService service) {
		this.configService = Objects.requireNonNull(service, "ConfigService is null");
	}

	@Override
	protected Optional<String> findBandwidthSettingsForUser(String username, int companyId) {
		return userService.findSettingsForWebserviceUser(username)
				.flatMap(WebserviceUserSettings::getApiCallLimitsSpec);
	}

	@Override
	protected String findDefaultBandwidthSettings(final int companyId) {
		return configService.getValue(ConfigValue.Webservices.DefaultApiCallLimits, companyId);
	}

}
