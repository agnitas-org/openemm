/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailing.cache;

import java.util.Objects;

import org.agnitas.emm.core.commons.daocache.AbstractCompanyBasedDaoCache;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import com.agnitas.util.TimeoutLRUMap;
import com.agnitas.beans.MailingContentType;
import com.agnitas.dao.MailingDao;

public final class MailingContentTypeCacheImpl extends AbstractCompanyBasedDaoCache<MailingContentType> implements MailingContentTypeCache {
	private MailingDao mailingDao;
	private ConfigService configService;

	public final void setMailingDao(final MailingDao dao) {
		this.mailingDao = Objects.requireNonNull(dao, "Mailing DAO cannot be null");
	}

	public final void setConfigService(final ConfigService configService) {
		this.configService = Objects.requireNonNull(configService, "ConfigService cannot be null");
	}

	@Override
	protected final MailingContentType getItemFromDao(final int mailingId, final int companyId) {
		if (!isCacheInitialized()) {
			setCache(new TimeoutLRUMap<>(
				configService.getIntegerValue(ConfigValue.CompanyMaxCache),
				configService.getIntegerValue(ConfigValue.CompanyMaxCacheTimeMillis)));
		}
		return mailingDao.getMailingContentType(companyId, mailingId);
	}
}
