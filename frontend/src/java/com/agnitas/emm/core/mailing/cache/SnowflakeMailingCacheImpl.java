/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailing.cache;

import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.core.mailing.beans.LightweightMailing;
import com.agnitas.util.TimeoutLRUMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.agnitas.dao.MailingDao;

/**
 * Cache for light-weight mailing objects with DAO access.
 */
public class SnowflakeMailingCacheImpl implements SnowflakeMailingCache {
	/** The logger. */
	private static final transient Logger logger = LogManager.getLogger(SnowflakeMailingCacheImpl.class);

	private ConfigService configService;
	
	/** DAO for accessing mailing data. */
	private MailingDao mailingDao;

	/** Internal cache structure. */
	private TimeoutLRUMap<String, LightweightMailing> cache;

	@Override
	public LightweightMailing getSnowflakeMailing(int companyId, int mailingId) throws SnowflakeMailingCacheException {
		LightweightMailing mailing = getCache().get(companyId + "_" + mailingId);

		if (mailing == null) {
			mailing = mailingDao.getLightweightMailing(companyId, mailingId);

			if (mailing != null && mailing.getMailingID() != 0) {
				getCache().put(companyId + "_" + mailingId, mailing);
			} else {
				mailing = null;
			}
		}

		if (mailing == null) {
			logger.error("Mailing ID " + mailingId + " not found");

			throw new SnowflakeMailingCacheException("Mailing ID " + mailingId + " not found");
		}

		return mailing;
	}
	
	private TimeoutLRUMap<String, LightweightMailing> getCache() {
		if (cache == null) {
			cache = new TimeoutLRUMap<>(configService.getIntegerValue(ConfigValue.MailgunMaxCache), configService.getLongValue(ConfigValue.MailgunMaxCacheTimeMillis));
		}
		
		return cache;
	}

	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}

	/**
	 * Setter for MailingDao.
	 * 
	 * @param mailingDao
	 *            DAO
	 */
	public void setMailingDao(MailingDao mailingDao) {
		this.mailingDao = mailingDao;
	}
}
