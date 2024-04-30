/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

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
import org.agnitas.emm.core.commons.uid.builder.impl.exception.UIDStringBuilderException;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.util.TimeoutLRUMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.beans.Company;
import com.agnitas.beans.ComRdirMailingData;
import com.agnitas.dao.ComMailingDao;
import com.agnitas.emm.core.commons.encoder.ByteArrayEncoder;
import com.agnitas.emm.core.commons.encoder.EncodingException;
import com.agnitas.emm.core.commons.encoder.Sha512Encoder;
import com.agnitas.emm.core.commons.encoder.UIDBase64;
import com.agnitas.emm.core.commons.uid.ComExtensibleUID;
import com.agnitas.emm.core.commons.uid.ExtensibleUidVersion;
import com.agnitas.emm.core.commons.uid.UIDFactory;
import com.agnitas.emm.core.commons.uid.daocache.impl.ComRdirMailingDataDaoCache;

public class V3ComExtensibleUIDStringBuilderImpl implements ExtensibleUIDStringBuilder {

	/** The logger. */
	private static final transient Logger logger = LogManager.getLogger(V3ComExtensibleUIDStringBuilderImpl.class);
	
	public static final char SEPARATOR = '.';
	
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
	
	public V3ComExtensibleUIDStringBuilderImpl() {
		this.hashEncoder = createSignatureHashEncoder();
		this.base64Encoder = new UIDBase64();
	}
	
	public ByteArrayEncoder createSignatureHashEncoder() {
		return new Sha512Encoder();
	}
	
	@Override
	public String buildUIDString(final ComExtensibleUID extensibleUID) throws UIDStringBuilderException, RequiredInformationMissingException {
		// Do workaround for missing mailing ID
		final ComExtensibleUID uid = workaroundMissingMailingId(extensibleUID);

		final ComRdirMailingData mailingData = this.mailingDataDaoCache.getItem(uid.getMailingID());
		final Company company = this.companyDaoCache.getItem(mailingData != null ? mailingData.getCompanyID() : 0);
		
		if (company.getSecretKey() != null && !company.getSecretKey().equals("")) {
			return makeBaseUID(uid) + SEPARATOR + getSignature(uid, company.getSecretKey());
		} else
			throw new RequiredInformationMissingException("secret key");
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
		return ExtensibleUidVersion.UID_WITH_BITFIELD_USING_SHA512;
	}
	
	private String makeBaseUID(final ComExtensibleUID uid) {
		final StringBuffer buffer = new StringBuffer();
		
		// Add prefix (and separator) if prefix is set
		if (uid.getPrefix() != null && !uid.getPrefix().equals("")) {
			buffer.append(uid.getPrefix());
			buffer.append(SEPARATOR);
		}
		
		buffer.append(base64Encoder.encodeLong(getVersionOfBuiltUIDs().getVersionCode()));
		buffer.append(SEPARATOR);
		buffer.append(base64Encoder.encodeLong(uid.getLicenseID()));
		buffer.append(SEPARATOR);
		buffer.append(base64Encoder.encodeLong(uid.getMailingID()));
		buffer.append(SEPARATOR);
		buffer.append(base64Encoder.encodeLong(uid.getCustomerID()));
		buffer.append(SEPARATOR);
		buffer.append(base64Encoder.encodeLong(uid.getUrlID()));
		buffer.append(SEPARATOR);
		buffer.append(base64Encoder.encodeLong(uid.getBitField()));
		
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
		signature.append(uid.getBitField());
		signature.append(SEPARATOR);
		signature.append(secretKey);
		
		/*
		 *  Synchronizing on hash-encoder is a must-have. MessageDigest is not thread-safe!
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
