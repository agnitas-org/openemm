/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.commons.intelliad;

import com.agnitas.beans.Mediatype;
import com.agnitas.beans.MediatypeEmail;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.emm.core.mediatypes.dao.MediatypesDao;
import com.agnitas.util.TimeoutLRUMap;
import com.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.core.commons.util.ConfigValue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class IntelliAdMailingSettingsCacheImpl implements IntelliAdMailingSettingsCache {
	
	private static final Logger logger = LogManager.getLogger( IntelliAdMailingSettingsCacheImpl.class);
	
	private MediatypesDao mediatypesDao;
	private ConfigService configService;
	private TimeoutLRUMap<Integer, IntelliAdMailingSettings> cache;
	
	@Override
	public IntelliAdMailingSettings getIntelliAdSettings(int companyId, int mailingId) {
		IntelliAdMailingSettings data = getCache().get( mailingId);
		
		if( data == null) {
			data = createIntelliAdSettings( companyId, mailingId);
			
			getCache().put( mailingId, data);
		}

		return data;
	}
	
	private TimeoutLRUMap<Integer, IntelliAdMailingSettings> getCache() {
		if (cache == null) {
			cache = new TimeoutLRUMap<>(configService.getIntegerValue(ConfigValue.MailgunMaxCache), configService.getLongValue(ConfigValue.MailgunMaxCacheTimeMillis));
		}
		
		return cache;
	}

	private IntelliAdMailingSettings createIntelliAdSettings(int companyId, int mailingId) {
        Mediatype mediatype = mediatypesDao.loadMediatypes(mailingId, companyId)
				.get(MediaTypes.EMAIL.getMediaCode());

		if (mediatype instanceof MediatypeEmail mediatypeEmail) {
			logger.info("Found media type email for mailing {}", mailingId);
            return new IntelliAdMailingSettings(mediatypeEmail.isIntelliAdEnabled(), mediatypeEmail.getIntelliAdString());
		}

		logger.info("Found no media type email for mailing {}", mailingId);
		// Return settings saying IntelliAd is disabled, no tracking string
		return new IntelliAdMailingSettings( false, null);
	}
	
	// ----------------------------------------------------------------------------------------------- dependency injection
	
	public void setMediatypesDao(MediatypesDao mediatypesDao) {
		this.mediatypesDao = mediatypesDao;
	}
	
	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}
}
