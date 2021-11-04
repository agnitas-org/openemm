/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.springws.throttling.migration;

import java.util.Objects;

import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.emm.springws.throttling.impl.SimpleSlidingAverageThrottlingServiceImpl;
import com.agnitas.emm.util.quota.api.QuotaLimitExceededException;
import com.agnitas.emm.util.quota.api.QuotaService;
import com.agnitas.emm.util.quota.api.QuotaServiceException;

/**
 * Switch between old {@link SimpleSlidingAverageThrottlingServiceImpl} and
 * new implementation of {@link QuotaService}.
 */
@Deprecated // Removed without replacement after rollout EMM-8124
public final class ThrottlingServiceImplementationSwitch implements QuotaService {

	private QuotaService legacyService;
	private QuotaService newService;
	private ConfigService configService;
	
	@Required
	public final void setConfigService(final ConfigService service) {
		this.configService = Objects.requireNonNull(service, "ConfigService is null");
	}
	
	@Required
	public final void setLegacyThrottlingService(final QuotaService service) {
		this.legacyService = Objects.requireNonNull(service, "Legacy ThrottlingService is null");
	}
	
	@Required
	public final void setNewThrottlingService(final QuotaService service) {
		this.newService = Objects.requireNonNull(service, "New ThrottlingService is null");
	}
	
	@Override
	public final void checkAndTrack(final String username, final int companyID, final String endpointName) throws QuotaServiceException, QuotaLimitExceededException {
		if(configService.getBooleanValue(ConfigValue.Development.UseNewWebserviceRateLimiting, companyID)) {
			newService.checkAndTrack(username, companyID, endpointName);
		} else {
			legacyService.checkAndTrack(username, companyID, endpointName);
		}
	}

}
