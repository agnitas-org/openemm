/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.springws.throttling.impl;

import java.io.Serializable;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.emm.util.quota.api.QuotaLimitExceededException;
import com.agnitas.emm.util.quota.api.QuotaService;
import com.agnitas.emm.util.quota.api.QuotaServiceException;
import com.agnitas.emm.wsmanager.bean.WebserviceUserSettings;
import com.agnitas.emm.wsmanager.service.WebserviceUserService;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

/**
 * Simple implementatinon of {@link QuotaService}.
 */
@Deprecated // Replaced by Bucket4jThrottlingService (remove after rollout EMM-8146)
public class SimpleSlidingAverageThrottlingServiceImpl implements QuotaService {
	private static final transient Logger logger = Logger.getLogger(SimpleSlidingAverageThrottlingServiceImpl.class);
	
	private static final int CACHE_SIZE = 10000;
	private static final int CACHE_EXPIRED_SECONDS = 300;
//	private static final double DEFAULT_RATE_LIMIT = 3.;
	private static final long TRACKING_PERIOD_SECONDS = 60;
	private static final long THRESHOLD_TIME_SECONDS = 1;
	
	private WebserviceUserService userService;
	
	private Cache cache;
	
	@Required
	public void setWebserviceUserService(WebserviceUserService service) {
		this.userService = Objects.requireNonNull(service, "Webservice user service is null");
	}
	
	private SimpleSlidingAverageThrottlingServiceImpl() throws CacheException {
		CacheManager cacheManager = CacheManager.create();
		cache = cacheManager.getCache("Meters");
		if (cache == null) {
			cache = new Cache("Meters", CACHE_SIZE, false, false, 0, CACHE_EXPIRED_SECONDS);
			cacheManager.addCache(cache);
		}
	}
	
	@Override
	public synchronized void checkAndTrack(String username, int companyID, String apiServiceName) throws QuotaLimitExceededException, QuotaServiceException {
		try {
			final boolean result = checkAndTrack(username);
			
			if(!result) {
				throw new QuotaLimitExceededException(username, companyID, apiServiceName);
			}
		} catch (final IllegalStateException | ExecutionException | CacheException e) {
			throw new QuotaServiceException(e);
		}
	}
	
	private final boolean checkAndTrack(String user) throws ExecutionException, IllegalStateException, CacheException {
		LimitMeter limitMeter = getMeter(user);
		if (limitMeter.limit != null && limitMeter.limit > 0 && !limitMeter.checkAndTrack()) {
			//TODO: log limit exceeded event for statistic
			logger.error("WS-request rejected: rate limit exceeded. User:[" + user + "]");
			return false;
		}
		
		return true;
	}

	private LimitMeter getMeter(String user) throws IllegalStateException, CacheException {
		Element element = cache.get(user);
		if (element == null) {
			LimitMeter limitMeter = createMeter(user);
			cache.put(new Element(user, limitMeter));
			return limitMeter;
		}
		return (LimitMeter) element.getValue();
	}

	private LimitMeter createMeter(String name) {
		Double limit = getRequestRateLimit(name);
		SlidingAverageRateMeter meter = (limit == null) ? null
				: new SlidingAverageRateMeter(TRACKING_PERIOD_SECONDS * 1000, THRESHOLD_TIME_SECONDS * 1000);
		LimitMeter limitMeter = new LimitMeter(meter, limit);
		logger.info("Created SlidingAverageRateMeter for: " + name);
		return limitMeter;
	}

	private Double getRequestRateLimit(final String username) {
		try {
			final WebserviceUserSettings settings = this.userService.findSettingsForWebserviceUser(username);
			
			return settings.getRequestRateLimit().isPresent()
					? (double)settings.getRequestRateLimit().getAsInt()
					: null;
		} catch(final Exception e) {
			logger.error(String.format("Error reading settings for webservice user '%s'", username), e);
			
			return null;
		}
	}
	
	private static class LimitMeter implements Serializable {
		private static final long serialVersionUID = 3586882607020373003L;
		
		private SlidingAverageRateMeter meter;
		private Double limit;
		
		public LimitMeter(SlidingAverageRateMeter meter, Double limit) {
			this.meter = meter;
			this.limit = limit;
		}
		
		public boolean checkAndTrack() {
			double rate = meter.getRate();
			if (rate > limit) {
				return false;
			}
			meter.tick();
			return true;
		}
	}
}
