/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.restful.util.quota;

import java.util.Objects;
import java.util.Optional;

import com.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.core.commons.util.ConfigValue;
import com.agnitas.emm.restful.util.quota.dao.RestfulQuotaDao;
import com.agnitas.emm.util.quota.tokenbucket.AbstractLocalBucketManager;

public final class LocalBucketManager extends AbstractLocalBucketManager {

	private ConfigService configService;
	private RestfulQuotaDao quotaDao;
	
	@Override
	protected Optional<String> findBandwidthSettingsForUser(String username, int companyId) {
		return this.quotaDao.readQuotaSpecs(username, companyId);
	}

	@Override
	protected String findDefaultBandwidthSettings(int companyId) {
		return configService.getValue(ConfigValue.DefaultRestfulApiCallLimits, companyId);
	}
	
	public void setConfigService(ConfigService configService) {
		this.configService = Objects.requireNonNull(configService, "ConfigService is null");
	}
	
	public void setRestfulQuotaDao(RestfulQuotaDao dao) {
		this.quotaDao = Objects.requireNonNull(dao, "RestfulQuotaDao is null");
	}

}
