/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.commons.uid.builder.impl;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

import org.agnitas.emm.core.commons.daocache.CompanyDaoCache;
import org.agnitas.emm.core.commons.uid.builder.ExtensibleUIDStringBuilder;
import org.agnitas.emm.core.commons.uid.builder.impl.exception.RequiredInformationMissingException;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.util.TimeoutLRUMap;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.beans.ComCompany;
import com.agnitas.beans.ComRdirMailingData;
import com.agnitas.dao.ComMailingDao;
import com.agnitas.emm.core.commons.encoder.ByteArrayEncoder;
import com.agnitas.emm.core.commons.encoder.EncodingException;
import com.agnitas.emm.core.commons.encoder.MD5Encoder;
import com.agnitas.emm.core.commons.encoder.UIDBase64;
import com.agnitas.emm.core.commons.uid.ComExtensibleUID;
import com.agnitas.emm.core.commons.uid.ExtensibleUidVersion;
import com.agnitas.emm.core.commons.uid.UIDFactory;
import com.agnitas.emm.core.commons.uid.daocache.impl.ComRdirMailingDataDaoCache;

/**
 * This is legacy code and must still use MD5-Encoder
 */
@SuppressWarnings("deprecation")
public class V2ComExtensibleUIDStringBuilderImpl implements ExtensibleUIDStringBuilder {

	private static final Logger logger = Logger.getLogger(V2ComExtensibleUIDStringBuilderImpl.class);
	private static final char SEPARATOR = '.';
	
	// ---------------------------------------------------- Dependency Injection
	
	private ComRdirMailingDataDaoCache mailingDataDaoCache;
	private CompanyDaoCache companyDaoCache;
	
	private ComMailingDao mailingDao;
	
	private ConfigService configService;
	
	@Required
	public final void setRdirMailingDataDaoCache(final ComRdirMailingDataDaoCache cache) {
		this.mailingDataDaoCache = Objects.requireNonNull(cache, "Cache cannot be null");
	}
	
	@Required
	public final void setCompanyDaoCache(final CompanyDaoCache cache) {
		this.companyDaoCache = Objects.requireNonNull(cache, "Cache cannot be null");
	}

	@Required
	public final void setMailingDao(final ComMailingDao dao) {
		this.mailingDao = Objects.requireNonNull(dao, "Mailing DAO cannot be null");
	}

	@Required
	public final void setConfigService(final ConfigService service) {
		this.configService = Objects.requireNonNull(service, "Config service cannot be null");
	}
	
	// ---------------------------------------------------- Business Logic
	
	/** Cache used by the workaround. */
	private TimeoutLRUMap<Integer, Integer> workaroundCompanyMailingCache;
	
	private final ByteArrayEncoder hashEncoder;
	private final UIDBase64 base64Encoder;
	
	public V2ComExtensibleUIDStringBuilderImpl() {
		this.hashEncoder = createSignatureHashEncoder();
		this.base64Encoder = new UIDBase64();
	}
	
	public ByteArrayEncoder createSignatureHashEncoder() {
		return new MD5Encoder();
	}
	
	@Override
	public String buildUIDString(final ComExtensibleUID extensibleUID) throws RequiredInformationMissingException {
		// Do workaround for missing mailing ID
		final ComExtensibleUID uid = workaroundMissingMailingId(extensibleUID);

		final ComRdirMailingData mailingData = this.mailingDataDaoCache.getItem(uid.getMailingID());
		final ComCompany company = this.companyDaoCache.getItem(mailingData != null ? mailingData.getCompanyID() : 0);
		
		if (company.getSecretKey() != null && !company.getSecretKey().equals("")) {
			if (mailingData == null) {
				throw new RuntimeException("mailingData was null");
			}
			final long timestamp = mailingData.getCreationDate().getTime();
		
			return makeBaseUID(uid, timestamp) + SEPARATOR + getSignature(uid, company.getSecretKey());
		} else {
			throw new RequiredInformationMissingException("secret key");
		}
	}
	
	/**
	 * Workaround for design flaw in the UID.
	 * 
	 * When no mailing ID is present, it is not possible to infer a company ID.
	 * 
	 * In the case of missing mailing ID (mailing ID is 0 in the UID), this methods creates a copy of the UID,
	 * reads an arbitrary mailing ID for the company ID from database (or cache) and writes it into the UID.
	 * 
	 * If the UID has a mailing ID set, then this methods does nothing but returning the given UID itself.
	 * 
	 * @param uid UID to fix (when required)
	 * 
	 * @return new UID instance with mailing ID (if given UID had mailing ID 0) or the same UID as given, if mailing ID was set.
	 */
	private ComExtensibleUID workaroundMissingMailingId(final ComExtensibleUID uid) {
		if (uid.getMailingID() > 0) {
			if (logger.isInfoEnabled()) {
				logger.info("UID is sane. Nothing to do.");
			}
			
			return uid;
		}
		
		if (logger.isInfoEnabled()) {
			logger.info("Mailing ID is missing in UID. Required to set arbitrary mailing ID");
		}
		
		int mailingId = 0;
		if (getWorkaroundCompanyMailingCache().get(uid.getCompanyID()) != null) {
			if (logger.isInfoEnabled()) {
				logger.info("Found cached mailing ID for company ID " + uid.getCompanyID());
			}
			
			mailingId = getWorkaroundCompanyMailingCache().get(uid.getCompanyID());
		} else {
			if (logger.isInfoEnabled()) {
				logger.info("No mailing ID was previously cached for company ID " + uid.getCompanyID() + " - reading from DB");
			}

			mailingId = mailingDao.getAnyMailingIdForCompany(uid.getCompanyID());
			
			getWorkaroundCompanyMailingCache().put(uid.getCompanyID(), mailingId);
		}

		if (logger.isDebugEnabled()) {
			logger.info("Setting mailing ID " + mailingId);
		}
		
		final ComExtensibleUID newUid = UIDFactory.copyWithNewMailingID(uid, mailingId);
		
		return newUid;
	}

	@Override
	public ExtensibleUidVersion getVersionOfBuiltUIDs() {
		/*
		 * For compatibility reasons to the old implementation of the XUID,
		 * this string builder also returns version LEGACY_UID (version code 0), not  XUID (version code 1).
		 * This is required, because the XUID is enabled for a specific company
		 * by setting its secret key.
		 */
		return ExtensibleUidVersion.LEGACY_UID;
	}
	
	private String makeBaseUID(final ComExtensibleUID uid, final long timestamp) {
		final StringBuffer buffer = new StringBuffer();
		
		// Add prefix (and separator) if prefix is set
		if (uid.getPrefix() != null && !uid.getPrefix().equals("")) {
			buffer.append(uid.getPrefix());
			buffer.append(SEPARATOR);
		}
		
		final long timestamp1 = timestamp % 65536L;
		final long timestamp2 = (timestamp * 37L) % 65536L;
		
		buffer.append(base64Encoder.encodeLong(getVersionOfBuiltUIDs().getVersionCode()));
		buffer.append(SEPARATOR);
		buffer.append(base64Encoder.encodeLong(uid.getLicenseID()));
		buffer.append(SEPARATOR);
		buffer.append(base64Encoder.encodeLong(uid.getMailingID()));
		buffer.append(SEPARATOR);
		buffer.append(base64Encoder.encodeLong(uid.getCustomerID() ^ timestamp1));
		buffer.append(SEPARATOR);
		buffer.append(base64Encoder.encodeLong(uid.getUrlID() ^ timestamp2 ^ uid.getCompanyID()));
		
		return buffer.toString();
	}
	
	private String getSignature(final ComExtensibleUID uid, final String secretKey) {
		final StringBuffer signature = new StringBuffer();
		
		if (uid.getPrefix() != null && !uid.getPrefix().equals("")) {
			signature.append(uid.getPrefix());
			signature.append('.');
		}
		
		signature.append(getVersionOfBuiltUIDs().getVersionCode());
		signature.append(SEPARATOR);
		signature.append(uid.getLicenseID());
		signature.append(SEPARATOR);
		signature.append(uid.getMailingID());
		signature.append(SEPARATOR);
		signature.append(uid.getCustomerID());
		signature.append(SEPARATOR);
		signature.append(uid.getUrlID());
		signature.append(SEPARATOR);
		signature.append(secretKey);
		
		/*
		 *  Synchronizing on md5Encoder is a must-have. MessageDigest is not thread-safe!
		 * 
		 *  We are loosing a bit of throughput here, but it's only around 2%
		 */
		synchronized(this.hashEncoder) {
			try {
				return base64Encoder.encodeBytes(hashEncoder.encode(signature.toString().getBytes(StandardCharsets.UTF_8)));
			} catch(final EncodingException e) {
				logger.error("Error while hashing UID signature", e);
				
				return null;
			}
		}
	}
	
	private TimeoutLRUMap<Integer, Integer> getWorkaroundCompanyMailingCache() {
		if (workaroundCompanyMailingCache == null) {
			// Create the cache structure for the workaround
			workaroundCompanyMailingCache = new TimeoutLRUMap<>(
				configService.getIntegerValue(ConfigValue.CompanyMaxCache),
				configService.getIntegerValue(ConfigValue.CompanyMaxCacheTimeMillis));
		}
		return workaroundCompanyMailingCache;
	}
}
