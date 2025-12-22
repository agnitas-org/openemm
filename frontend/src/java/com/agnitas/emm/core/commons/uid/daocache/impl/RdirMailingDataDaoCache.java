/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.commons.uid.daocache.impl;

import com.agnitas.beans.RdirMailingData;
import com.agnitas.dao.MailingDao;
import com.agnitas.emm.core.commons.daocache.AbstractDaoCache;
import com.agnitas.util.TimeoutLRUMap;
import com.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.core.commons.util.ConfigValue;

public class RdirMailingDataDaoCache extends AbstractDaoCache<RdirMailingData> {

	private MailingDao mailingDao;

	private ConfigService configService;

	public void setMailingDao( MailingDao mailingDao) {
		this.mailingDao = mailingDao;
	}

	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}
	
	@Override
	protected RdirMailingData getItemFromDao(int mailingId) {
		if (!isCacheInitialized()) {
			setCache(new TimeoutLRUMap<>(
				configService.getIntegerValue(ConfigValue.RdirMailingIdsMaxCache),
				configService.getIntegerValue(ConfigValue.RdirMailingIdsMaxCacheTimeMillis)));
		}
		return this.mailingDao.getRdirMailingData(mailingId);
	}
}
