/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.commons.uid.builder.impl;

import java.io.IOException;
import java.util.Objects;

import com.agnitas.emm.core.commons.encoder.ByteArrayEncoder;
import com.agnitas.emm.core.commons.encoder.Sha512Encoder;
import com.agnitas.emm.core.commons.encoder.UIDBase64;
import com.agnitas.emm.core.commons.uid.ExtensibleUID;
import com.agnitas.emm.core.commons.uid.ExtensibleUidVersion;
import com.agnitas.emm.core.commons.uid.beans.CompanyUidData;
import com.agnitas.emm.core.commons.uid.builder.ExtensibleUIDStringBuilder;
import com.agnitas.emm.core.commons.uid.builder.impl.exception.RequiredInformationMissingException;
import com.agnitas.emm.core.commons.uid.builder.impl.exception.UIDStringBuilderException;
import com.agnitas.emm.core.commons.uid.daocache.impl.CompanyUidDataDaoCache;
import com.agnitas.util.ByteBuilder;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;

/**
 * V5 agnUID builder
 * 
 * The payload is encoded in a single part of the agnUID coded as a map and encoded using msgpack
 * the map itself uses strings as key (and to ensure compatibility with other implementations
 * these should consist only of printable ascii character).
 * 
 * Keys starting with an underscore ("_") are reserved for internal use of essentials values
 * of the agnUID. At the moment these keys are used:
 * _c: the companyID
 * _l: the licenceID
 * _m: the mailingID
 * _o: the bitfield coded option
 * _r: the customerID (receiver)
 * _s: the sendDate in seconds since the unix epoch
 * _u: the urlID
 * 
 * If a value is NOT present, it must be replaced with a valid default value, for the currently
 * reserved keys the default value is always 0 (numeric null).
 */
public class V5ExtensibleUIDStringBuilderImpl implements ExtensibleUIDStringBuilder {

	public static final char SEPARATOR = '.';
	
	// ---------------------------------------------------- Dependency Injection
	
	private CompanyUidDataDaoCache companyUidDataCache;
	
	public final void setCompanyUidDataDaoCache(final CompanyUidDataDaoCache cache) { // TODO Replace by constructor injection
		this.companyUidDataCache = Objects.requireNonNull(cache, "Cache canno tbe null");
	}
	
	// ---------------------------------------------------- Business Logic
	
	private final ByteArrayEncoder hashEncoder;
	private final UIDBase64 base64Encoder;
	
	public V5ExtensibleUIDStringBuilderImpl() {
		this.hashEncoder = createSignatureHashEncoder();
		this.base64Encoder = new UIDBase64();
	}
	
	public ByteArrayEncoder createSignatureHashEncoder() {
		return new Sha512Encoder();
	}
	
	@Override
	public String buildUIDString(final ExtensibleUID uid) throws UIDStringBuilderException, RequiredInformationMissingException {
		if(uid.getCompanyID() == 0) {
			throw new RequiredInformationMissingException("company ID");
		}

		final CompanyUidData companyUidData = this.companyUidDataCache.getItem(uid.getCompanyID());
		
		if (companyUidData.getSecretKey() != null && !companyUidData.getSecretKey().equals("")) {
			try {
				byte[] packed = createPacked (uid);
			
				return makeBaseUID(uid, packed) + SEPARATOR + getSignature(uid, packed, companyUidData.getSecretKey());
			} catch (IOException e) {
				throw new UIDStringBuilderException ("failed to pack UID: " + e.toString ());
			}
		} else {
			throw new RequiredInformationMissingException("secret key");
		}
	}

	@Override
	public ExtensibleUidVersion getVersionOfBuiltUIDs() {
		return ExtensibleUidVersion.V5_AGNOSTIC;
	}
	
	private byte[] createPacked(final ExtensibleUID uid) throws IOException {
		try (MessageBufferPacker mp = MessagePack.newDefaultBufferPacker ()) {		
			int	licenceID = uid.getLicenseID ();
			int	companyID = uid.getCompanyID ();
			int	mailingID = uid.getMailingID ();
			int	customerID = uid.getCustomerID ();
			int	urlID = uid.getUrlID ();
			int	position = uid.getPosition ();
			long	bitfield = uid.getBitField ();
			long	senddate = uid.getSendDate ();
			char	statusField = uid.getStatusField ();
		
			mp.packMapHeader (
				(licenceID > 0 ? 1 : 0) +
				(companyID > 0 ? 1 : 0) +
				(mailingID > 0 ? 1 : 0) +
				(customerID > 0 ? 1 : 0) +
				(urlID > 0 ? 1 : 0) +
				(urlID > 0 && position > 1 ? 1 : 0) +
				(bitfield != 0 ? 1 : 0) +
				(senddate > 0 ? 1 : 0) +
				(statusField != '\0' ? 1 : 0)
			);
			if (companyID > 0) {
				mp.packString ("_c").packInt (companyID);
			}
			if (licenceID > 0) {
				mp.packString ("_l").packInt (licenceID);
			}
			if (mailingID > 0) {
				mp.packString ("_m").packInt (mailingID);
			}
			if (bitfield != 0) {
				mp.packString ("_o").packLong (bitfield);
			}
			if (customerID > 0) {
				mp.packString ("_r").packInt (customerID);
			}
			if (senddate > 0) {
				mp.packString ("_s").packLong (senddate);
			}
			if (statusField != '\0') {
				mp.packString ("_f").packString (Character.toString (statusField));
			}
			if (urlID > 0) {
				mp.packString ("_u").packInt (urlID);
				if (position > 1) {
					mp.packString ("_x").packInt (position);
				}
			}
			return mp.toByteArray ();
		}
	}
	
	private String makeBaseUID(final ExtensibleUID uid, final byte[] packed) {
		final StringBuffer buffer = new StringBuffer();
		
		// Add prefix (and separator) if prefix is set
		if (uid.getPrefix() != null && !uid.getPrefix().equals("")) {
			buffer.append(uid.getPrefix());
			buffer.append(SEPARATOR);
		}
		
		buffer.append(base64Encoder.encodeLong(getVersionOfBuiltUIDs().getVersionCode()));
		buffer.append(SEPARATOR);
		buffer.append(base64Encoder.encodeBytes (packed));
		
		return buffer.toString();
	}
	
	private String getSignature(final ExtensibleUID uid, final byte[] packed, final String secretKey) {
		ByteBuilder	bb = new ByteBuilder ();

		if (uid.getPrefix() != null && !uid.getPrefix().equals("")) {
			bb.append (uid.getPrefix());
			bb.append (SEPARATOR);
		}
		bb.append (getVersionOfBuiltUIDs ().getVersionCode ());
		bb.append (SEPARATOR);
		bb.append (packed);
		bb.append (SEPARATOR);
		bb.append (secretKey);
		
		/*
		 *  Synchronizing on hash-encoder is a must-have. MessageDigest is not thread-safe!
		 * 
		 *  We are loosing a bit of throughput here, but it's only around 2%
		 */
		synchronized(this.hashEncoder) {
			return base64Encoder.encodeBytes(hashEncoder.encode(bb.value ()));
		}
	}
	
}
