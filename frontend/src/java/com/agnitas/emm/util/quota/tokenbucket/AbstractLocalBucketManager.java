/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.util.quota.tokenbucket;

import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.apache.log4j.Logger;

import com.agnitas.emm.wsmanager.common.WebserviceUserException;
import com.agnitas.emm.wsmanager.service.WebserviceUserServiceException;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.ConfigurationBuilder;
import io.github.bucket4j.Refill;
import io.github.bucket4j.TokensInheritanceStrategy;
import io.github.bucket4j.local.LocalBucketBuilder;

/**
 * Abstract implementation of {@link BucketManager} that does not 
 * share token buckets across different machines.
 */
public abstract class AbstractLocalBucketManager implements BucketManager {
	
	/** The logger. */
	private static final transient Logger LOGGER = Logger.getLogger(AbstractLocalBucketManager.class);
	
	/** Map containing one bucket for each user name. */
	private final Map<String, Bucket> bucketMap;
	
	/** Map containing the bandwidth specification for each user name. */
	private final Map<String, String> bandwidthMap;

	/**
	 * Creates a new instance.
	 */
	public AbstractLocalBucketManager() {
		this.bucketMap = new HashMap<>();
		this.bandwidthMap = new HashMap<>();
	}

	@Override
	public final Bucket getOrCreateBucket(final String username, final int companyId) throws BucketManagerException {
		Bucket bucket = this.bucketMap.get(username);
		
		try {
			if(bucket == null) {
				bucket = createNewBucket(username, companyId);
				this.bucketMap.put(username, bucket);
			}
			
			updateBucketSettings(bucket, username, companyId);
			
			return bucket;
		} catch(final WebserviceUserException | WebserviceUserServiceException e) {
			throw BucketManagerException.cannotCreateBucket(username, companyId, e);
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
	private final void updateBucketSettings(final Bucket bucket, final String username, final int companyId) throws WebserviceUserException, WebserviceUserServiceException, BucketManagerException {
		final List<Bandwidth> bandwidthListOrNull = readBandwidthOrNull(username, companyId);
		
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
	private final Bucket createNewBucket(final String username, final int companyId) throws WebserviceUserException, WebserviceUserServiceException, BucketManagerException {
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug(String.format("Create new token bucket for webservice user '%s'", username));
		}
		
		final List<Bandwidth> bandwidthList = readBandwidthOrNull(username, companyId);
		
		if(bandwidthList == null) {
			throw BucketManagerException.noApiCallLimit(username, companyId);
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
	private final List<Bandwidth> readBandwidthOrNull(final String username, final int companyId) throws WebserviceUserException, WebserviceUserServiceException, BucketManagerException {
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug(String.format("Reading bandwidths for webservice user '%s' of company %d", username, companyId));
		}
		
		final String bandwidthSpec = readBandwidthSpec(username, companyId);
		
		if(bandwidthSpec != null) {
			final String cachedBandwidthSpec = this.bandwidthMap.get(username);
			
			if(!Objects.equals(bandwidthSpec, cachedBandwidthSpec)) {
				final List<Bandwidth> list = toBandwidthList(bandwidthSpec);
				
				this.bandwidthMap.put(username, bandwidthSpec);
				
				return list;
			} else {
				return null;
			}
		} else {
			throw BucketManagerException.noApiCallLimit(username, companyId);
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
	private final String readBandwidthSpec(final String username, final int companyId) throws WebserviceUserException, WebserviceUserServiceException {
		final Optional<String> settings = findBandwidthSettingsForUser(username, companyId);
		
		return settings.isPresent() 
				? settings.get()
				: findDefaultBandwidthSettings(companyId);
	}
	
	protected abstract Optional<String> findBandwidthSettingsForUser(final String username, final int companyId);
	protected abstract String findDefaultBandwidthSettings(final int companyId);
	
}
