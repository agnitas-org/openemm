/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.commons.uid.builder.impl;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

import com.agnitas.beans.RdirMailingData;
import com.agnitas.dao.MailingDao;
import com.agnitas.emm.core.commons.encoder.ByteArrayEncoder;
import com.agnitas.emm.core.commons.encoder.Sha512Encoder;
import com.agnitas.emm.core.commons.encoder.UIDBase64;
import com.agnitas.emm.core.commons.uid.ExtensibleUID;
import com.agnitas.emm.core.commons.uid.ExtensibleUidVersion;
import com.agnitas.emm.core.commons.uid.UIDFactory;
import com.agnitas.emm.core.commons.uid.beans.CompanyUidData;
import com.agnitas.emm.core.commons.uid.builder.ExtensibleUIDStringBuilder;
import com.agnitas.emm.core.commons.uid.builder.impl.exception.RequiredInformationMissingException;
import com.agnitas.emm.core.commons.uid.daocache.impl.CompanyUidDataDaoCache;
import com.agnitas.emm.core.commons.uid.daocache.impl.RdirMailingDataDaoCache;
import com.agnitas.util.TimeoutLRUMap;
import com.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.core.commons.util.ConfigValue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class V3ExtensibleUIDStringBuilderImpl implements ExtensibleUIDStringBuilder {

	private static final Logger logger = LogManager.getLogger(V3ExtensibleUIDStringBuilderImpl.class);
	
	public static final char SEPARATOR = '.';
	
	// ---------------------------------------------------- Dependency Injection
	
	private RdirMailingDataDaoCache mailingDataDaoCache;
	private CompanyUidDataDaoCache companyUidDataCache;
	
	private MailingDao mailingDao;
	
	private ConfigService configService;
	
	public final void setRdirMailingDataDaoCache(final RdirMailingDataDaoCache cache) { // TODO Replace by constructor injection
		this.mailingDataDaoCache = Objects.requireNonNull(cache, "Cache cannot be null");
	}
	
	public final void setCompanyUidDataDaoCache(final CompanyUidDataDaoCache cache) { // TODO Replace by constructor injection
		this.companyUidDataCache = Objects.requireNonNull(cache, "Cache canno tbe null");
	}

	public final void setMailingDao(final MailingDao dao) { // TODO Replace by constructor injection
		this.mailingDao = Objects.requireNonNull(dao, "Mailing DAO cannot be null");
	}

	public final void setConfigService(final ConfigService service) { // TODO Replace by constructor injection
		this.configService = Objects.requireNonNull(service, "Config service cannot be null");
	}
	
	// ---------------------------------------------------- Business Logic
	
	/** Cache used by the workaround. */
	private TimeoutLRUMap<Integer, Integer> workaroundCompanyMailingCache;
	
	private final ByteArrayEncoder hashEncoder;
	private final UIDBase64 base64Encoder;
	
	public V3ExtensibleUIDStringBuilderImpl() {
		this.hashEncoder = createSignatureHashEncoder();
		this.base64Encoder = new UIDBase64();
	}
	
	public ByteArrayEncoder createSignatureHashEncoder() {
		return new Sha512Encoder();
	}
	
	@Override
	public String buildUIDString(final ExtensibleUID extensibleUID) throws RequiredInformationMissingException {
		// Do workaround for missing mailing ID
		final ExtensibleUID uid = workaroundMissingMailingId(extensibleUID);

		final RdirMailingData mailingData = this.mailingDataDaoCache.getItem(uid.getMailingID());
		final CompanyUidData companyUidData = this.companyUidDataCache.getItem(mailingData != null ? mailingData.getCompanyID() : 0);
		
		if (companyUidData.getSecretKey() != null && !companyUidData.getSecretKey().equals("")) {
			return makeBaseUID(uid) + SEPARATOR + getSignature(uid, companyUidData.getSecretKey());
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
	private ExtensibleUID workaroundMissingMailingId(final ExtensibleUID uid) {
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
		
		final ExtensibleUID newUid = UIDFactory.copyWithNewMailingID(uid, mailingId);
		
		return newUid;
	}

	@Override
	public ExtensibleUidVersion getVersionOfBuiltUIDs() {
		return ExtensibleUidVersion.UID_WITH_BITFIELD_USING_SHA512;
	}
	
	private String makeBaseUID(final ExtensibleUID uid) {
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
	
	private String getSignature(final ExtensibleUID uid, final String secretKey) {
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
			return base64Encoder.encodeBytes(hashEncoder.encode(signature.toString().getBytes(StandardCharsets.UTF_8)));
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
