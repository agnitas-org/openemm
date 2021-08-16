/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.springws.throttling.tokenbucket.service;

import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.emm.springws.WebserviceUserDetails;
import com.agnitas.emm.wsmanager.bean.WebserviceUserSettings;
import com.agnitas.emm.wsmanager.common.WebserviceUserException;
import com.agnitas.emm.wsmanager.service.WebserviceUserService;
import com.agnitas.emm.wsmanager.service.WebserviceUserServiceException;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.ConfigurationBuilder;
import io.github.bucket4j.Refill;
import io.github.bucket4j.TokensInheritanceStrategy;
import io.github.bucket4j.local.LocalBucketBuilder;

/**
 * Implementation of {@link BucketManager} that does not 
 * share token buckets across different machines.
 */
public final class LocalBucketManager implements BucketManager {
	
	/** The logger. */
	private static final transient Logger LOGGER = Logger.getLogger(LocalBucketManager.class);
	
	/** Map containing one bucket for each user name. */
	private final Map<String, Bucket> bucketMap;
	
	/** Map containing the bandwidth specification for each user name. */
	private final Map<String, String> bandwidthMap;
	
	/** Service for handling webservice users. */
	private WebserviceUserService userService;
	
	/** Configuration service. */
	private ConfigService configService;

	/**
	 * Creates a new instance.
	 */
	public LocalBucketManager() {
		this.bucketMap = new HashMap<>();
		this.bandwidthMap = new HashMap<>();
	}
	
	/**
	 * Set service handling webservice users.
	 * 
	 * @param service service handling webservice users
	 */
	@Required
	public final void setWebserviceUserService(final WebserviceUserService service) {
		this.userService = Objects.requireNonNull(service, "Webservice user service is null");
	}
	
	/**
	 * Set configuration service.
	 * 
	 * @param service configuration service
	 */
	@Required
	public final void setConfigService(final ConfigService service) {
		this.configService = Objects.requireNonNull(service, "ConfigService is null");
	}

	@Override
	public final Bucket getOrCreateBucket(final WebserviceUserDetails webserviceUser) throws BucketManagerException {
		Bucket bucket = this.bucketMap.get(webserviceUser.getUsername());
		
		try {
			if(bucket == null) {
				bucket = createNewBucket(webserviceUser);
				this.bucketMap.put(webserviceUser.getUsername(), bucket);
			}
			
			updateBucketSettings(bucket, webserviceUser);
			
			return bucket;
		} catch(final WebserviceUserException | WebserviceUserServiceException e) {
			throw BucketManagerException.cannotCreateBucket(webserviceUser, e);
		}
	}

	/**
	 * Updates bucket settings if configuration changes have been detected.
	 * 
	 * @param bucket token bucket
	 * @param webserviceUser webservice user
	 * 
	 * @throws WebserviceUserException on errors reading configuration of webservice user
	 * @throws WebserviceUserServiceException on errors reading configuration of webservice user
	 * @throws BucketManagerException on errors updating token bucket configuration
	 */
	private final void updateBucketSettings(final Bucket bucket, final WebserviceUserDetails webserviceUser) throws WebserviceUserException, WebserviceUserServiceException, BucketManagerException {
		final List<Bandwidth> bandwidthListOrNull = readBandwidthOrNull(webserviceUser);
		
		// Simply replacing the configuration resets the bucket and will break the desired bahviour.
		if(bandwidthListOrNull != null) {
			final ConfigurationBuilder builder = Bucket4j.configurationBuilder();
			bandwidthListOrNull.forEach(bw -> builder.addLimit(bw));
			
			bucket.replaceConfiguration(builder.build(), TokensInheritanceStrategy.AS_IS);
		}
	}
	
	/**
	 * Creates a new token bucket
	 * 
	 * @param webserviceUser webservice user
	 * 
	 * @return new token bucket for webservice user
	 * 
	 * @throws WebserviceUserException on errors reading configuration of webservice user
	 * @throws WebserviceUserServiceException on errors reading configuration of webservice user
	 * @throws BucketManagerException on errors creating new token bucket
	 */
	private final Bucket createNewBucket(final WebserviceUserDetails webserviceUser) throws WebserviceUserException, WebserviceUserServiceException, BucketManagerException {
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug(String.format("Create new token bucket for webservice user '%s'", webserviceUser.getUsername()));
		}
		
		final List<Bandwidth> bandwidthList = readBandwidthOrNull(webserviceUser);
		
		if(bandwidthList == null) {
			throw BucketManagerException.noApiCallLimit(webserviceUser);
		} 
		
		final LocalBucketBuilder builder = Bucket4j.builder();
		bandwidthList.forEach(bw -> builder.addLimit(bw));
		
		return builder.build();
	}
	
	/**
	 * Reads the bandwidths defined for given webservice user.
	 * If there is no custom settings the default settings is taken.
	 * If there is no default settings, <code>null</code> is returned.
	 * 
	 * @param webserviceUser webservice user
	 * 
	 * @return bandwidths or <code>null</code>
	 * 
	 * @throws WebserviceUserException on errors reading configuration of webservice user
	 * @throws WebserviceUserServiceException on errors reading configuration of webservice user
	 * @throws BucketManagerException on errors reading bandwidths
	 */
	private final List<Bandwidth> readBandwidthOrNull(final WebserviceUserDetails webserviceUser) throws WebserviceUserException, WebserviceUserServiceException, BucketManagerException {
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug(String.format("Reading bandwidths for webservice user '%s' of company %d", webserviceUser.getUsername(), webserviceUser.getCompanyID()));
		}
		
		final String bandwidthSpec = readBandwidthSpec(webserviceUser);
		
		if(bandwidthSpec != null) {
			final String cachedBandwidthSpec = this.bandwidthMap.get(webserviceUser.getUsername());
			
			if(!Objects.equals(bandwidthSpec, cachedBandwidthSpec)) {
				final List<Bandwidth> list = toBandwidthList(bandwidthSpec);
				
				this.bandwidthMap.put(webserviceUser.getUsername(), bandwidthSpec);
				
				return list;
			} else {
				return null;
			}
		} else {
			throw BucketManagerException.noApiCallLimit(webserviceUser);
		}
	}
	
	/**
	 * Converts the bandwidth specification to bandwidths.
	 * 
	 * @param bandwidthSpec bandwidth specification
	 * 
	 * @return list of bandwidths
	 * 
	 * @throws BucketManagerException on errors parsing specifications
	 */
	private static final List<Bandwidth> toBandwidthList(final String bandwidthSpec) throws BucketManagerException {
		assert bandwidthSpec != null; // Ensured by caller
		
		final String[] parts = bandwidthSpec.trim().split("\\s*;\\s*");
		
		final List<Bandwidth> list = new ArrayList<>();
		for(final String part : parts) {
			list.add(toBandwidth(part));
		}
		
		return list;
	}
	
	/**
	 * Converts the bandwidth specification to bandwidth.
	 * 
	 * @param spec single bandwidth specification
	 * 
	 * @return bandwidth
	 * 
	 * @throws BucketManagerException on errors parsing specification
	 */
	private static final Bandwidth toBandwidth(final String spec) throws BucketManagerException {
		assert spec != null; // Ensured by caller
		
		final int semicolonIndex = spec.indexOf('/');
		
		if(semicolonIndex == -1) {
			throw BucketManagerException.invalidApiCallLimitSpecification(spec);
		}
		
		try {
			final int tokens = Integer.parseInt(spec.substring(0, semicolonIndex));
			final Duration duration = Duration.parse(spec.substring(semicolonIndex + 1));
			
			return Bandwidth.classic(tokens, Refill.intervally(tokens, duration));
		} catch(final NumberFormatException | DateTimeParseException e) {
			throw BucketManagerException.invalidApiCallLimitSpecification(spec, e);
		}
	}
	
	/**
	 * Reads the bandwidths specification defined for given webservice user.
	 * If there is no custom settings the default settings is taken.
	 * If there is no default settings, <code>null</code> is returned.
	 * 
	 * @param webserviceUser webservice user
	 * 
	 * @return bandwidths specification or <code>null</code>
	 * 
	 * @throws WebserviceUserException on errors reading configuration of webservice user
	 * @throws WebserviceUserServiceException on errors reading configuration of webservice user
	 */
	private final String readBandwidthSpec(final WebserviceUserDetails webserviceUser) throws WebserviceUserException, WebserviceUserServiceException {
		final WebserviceUserSettings settings = this.userService.findSettingsForWebserviceUser(webserviceUser.getUsername());
		
		return settings.getApiCallLimitsSpec().isPresent() 
				? settings.getApiCallLimitsSpec().get()
				: configService.getValue(ConfigValue.Webservices.DefaultApiCallLimits, webserviceUser.getCompanyID());
	}

}
