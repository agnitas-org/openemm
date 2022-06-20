/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.commons.intelliad;

import java.util.Map;

import com.agnitas.beans.Mediatype;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.core.mediatypes.dao.MediatypesDao;
import org.agnitas.emm.core.mediatypes.dao.MediatypesDaoException;
import org.agnitas.util.TimeoutLRUMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.beans.MediatypeEmail;

public class IntelliAdMailingSettingsCacheImpl implements IntelliAdMailingSettingsCache {
	
	// ----------------------------------------------------------------------------------------------- business code
	
	/** The logger. */
	private static final transient Logger logger = LogManager.getLogger( IntelliAdMailingSettingsCacheImpl.class);
	
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

	private IntelliAdMailingSettings createIntelliAdSettings( int companyId, int mailingId) {
		try {
			Map<Integer, Mediatype> map = this.mediatypesDao.loadMediatypes( mailingId, companyId);
			Mediatype mediatype = map.get( 0);
			
			if( mediatype != null) {
				if( logger.isInfoEnabled()) {
					logger.info( "Found media type email for mailing " + mailingId);
				}

				boolean enabled = ((MediatypeEmail) mediatype).isIntelliAdEnabled();
				String trackingString = ((MediatypeEmail) mediatype).getIntelliAdString();
				
				return new IntelliAdMailingSettings( enabled, trackingString);
			} else {
				if( logger.isInfoEnabled()) {
					logger.info( "Found no media type email for mailing " + mailingId);
				}
				
				// Return settings saying IntelliAd is disabled, no tracking string
				return new IntelliAdMailingSettings( false, null);
			}
		} catch( MediatypesDaoException e) {
			logger.error( "Error accessing media types for mailing " + mailingId, e);
			
			return null;
		}
	}
	
	// ----------------------------------------------------------------------------------------------- dependency injection
	
	/**
	 * Setter for MediatypesDao.
	 * 
	 * @param mediatypesDao instance of MediatypesDao
	 */
	@Required
	public void setMediatypesDao(MediatypesDao mediatypesDao) {
		this.mediatypesDao = mediatypesDao;
	}
	
	@Required
	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}
}
