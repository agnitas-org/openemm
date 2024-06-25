/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.commons.uid.builder.impl;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

import org.agnitas.emm.core.commons.uid.builder.ExtensibleUIDStringBuilder;
import org.agnitas.emm.core.commons.uid.builder.impl.exception.RequiredInformationMissingException;
import org.agnitas.emm.core.commons.uid.builder.impl.exception.UIDStringBuilderException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.emm.core.commons.encoder.ByteArrayEncoder;
import com.agnitas.emm.core.commons.encoder.EncodingException;
import com.agnitas.emm.core.commons.encoder.Sha512Encoder;
import com.agnitas.emm.core.commons.encoder.UIDBase64;
import com.agnitas.emm.core.commons.uid.ComExtensibleUID;
import com.agnitas.emm.core.commons.uid.ExtensibleUidVersion;
import com.agnitas.emm.core.commons.uid.beans.CompanyUidData;
import com.agnitas.emm.core.commons.uid.daocache.impl.CompanyUidDataDaoCache;

public class V4ExtensibleUIDStringBuilderImpl implements ExtensibleUIDStringBuilder {

	/** The logger. */
	private static final transient Logger logger = LogManager.getLogger(V4ExtensibleUIDStringBuilderImpl.class);
	
	public static final char SEPARATOR = '.';
	
	// ---------------------------------------------------- Dependency Injection
	
	private CompanyUidDataDaoCache companyUidDataCache;
	
	@Required
	public final void setCompanyUidDataDaoCache(final CompanyUidDataDaoCache cache) { // TODO Replace by constructor injection
		this.companyUidDataCache = Objects.requireNonNull(cache, "Cache canno tbe null");
	}
	
	// ---------------------------------------------------- Business Logic
	
	private final ByteArrayEncoder hashEncoder;
	private final UIDBase64 base64Encoder;
	
	public V4ExtensibleUIDStringBuilderImpl() {
		this.hashEncoder = createSignatureHashEncoder();
		this.base64Encoder = new UIDBase64();
	}
	
	public ByteArrayEncoder createSignatureHashEncoder() {
		return new Sha512Encoder();
	}
	
	@Override
	public String buildUIDString(final ComExtensibleUID uid) throws UIDStringBuilderException, RequiredInformationMissingException {
		if(uid.getCompanyID() == 0) {
			throw new RequiredInformationMissingException("company ID");
		}

		final CompanyUidData companyUidData = this.companyUidDataCache.getItem(uid.getCompanyID());
		
		if (companyUidData.getSecretKey() != null && !companyUidData.getSecretKey().equals("")) {
			return makeBaseUID(uid) + SEPARATOR + getSignature(uid, companyUidData.getSecretKey());
		} else {
			throw new RequiredInformationMissingException("secret key");
		}
	}

	@Override
	public ExtensibleUidVersion getVersionOfBuiltUIDs() {
		return ExtensibleUidVersion.V4_WITH_COMPANY_ID;
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
		buffer.append(base64Encoder.encodeLong(uid.getCompanyID()));
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
		signature.append(uid.getCompanyID());
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
	
}
