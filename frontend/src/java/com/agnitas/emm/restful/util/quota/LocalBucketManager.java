/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.restful.util.quota;

import java.util.Objects;
import java.util.Optional;

import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.emm.restful.util.quota.dao.RestfulQuotaDao;
import com.agnitas.emm.util.quota.tokenbucket.AbstractLocalBucketManager;

public final class LocalBucketManager extends AbstractLocalBucketManager {

	private ConfigService configService;
	private RestfulQuotaDao quotaDao;
	
	@Override
	protected final Optional<String> findBandwidthSettingsForUser(final String username, final int companyId) {
		return this.quotaDao.readQuotaSpecs(username, companyId);
	}

	@Override
	protected final String findDefaultBandwidthSettings(final int companyId) {
		return configService.getValue(ConfigValue.DefaultRestfulApiCallLimits, companyId);
	}
	
	@Required
	public final void setConfigService(final ConfigService configService) {
		this.configService = Objects.requireNonNull(configService, "ConfigService is null");
	}
	
	@Required
	public final void setRestfulQuotaDao(final RestfulQuotaDao dao) {
		this.quotaDao = Objects.requireNonNull(dao, "RestfulQuotaDao is null");
	}

}
