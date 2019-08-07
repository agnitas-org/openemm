/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailing.cache;


import org.agnitas.emm.core.mailing.beans.LightweightMailing;
import org.agnitas.util.TimeoutLRUMap;
import org.apache.log4j.Logger;

import com.agnitas.dao.ComMailingDao;

/**
 * Cache for light-weight mailing objects with DAO access.
 */
public class ComSnowflakeMailingCacheImpl implements SnowflakeMailingCache {
	
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger( ComSnowflakeMailingCacheImpl.class);
	
	/** DAO for accessing mailing data. */
	private ComMailingDao mailingDao;
	
	/** Internal cache structure. */
	private TimeoutLRUMap<Integer, LightweightMailing> cache;
	
	// -------------------------------------------------------------------- business code
	@Override
	public LightweightMailing getSnowflakeMailing(int mailingId) throws ComSnowflakeMailingCacheException {
		LightweightMailing mailing = this.cache.get( mailingId);
		
		if( mailing == null) {
			mailing = this.mailingDao.getLightweightMailing( mailingId);
			
			if( mailing != null && mailing.getMailingID() != 0) {
				cache.put( mailingId, mailing);
			} else
				mailing = null;
		}
		
		if( mailing == null) {
			logger.error( "Mailing ID " + mailingId + " not found");
			
			throw new ComSnowflakeMailingCacheException( "Mailing ID " + mailingId + " not found");
		}
		
		return mailing;
	}
	
	
	// -------------------------------------------------------------------- dependency injection
	
	/**
	 * Setter for ComMailingDao.
	 * 
	 * @param comMailingDao DAO
	 */
	public void setComMailingDao( ComMailingDao comMailingDao) {
		this.mailingDao = comMailingDao;
	}
	
	/**
	 * Setter for cache structure.
	 * 
	 * @param cache cache structure
	 */
	public void setCache( TimeoutLRUMap<Integer, LightweightMailing> cache) {
		this.cache = cache;
	}

}
