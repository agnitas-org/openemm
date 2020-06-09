/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.hashtag.tags;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.emm.core.commons.CharsetConstants;
import com.agnitas.emm.core.commons.encoder.ByteArrayEncoder;
import com.agnitas.emm.core.commons.encoder.ByteArrayToStringEncoder;
import com.agnitas.emm.core.commons.encoder.EncodingException;
import com.agnitas.emm.core.commons.encoder.MD5Encoder;
import com.agnitas.emm.core.commons.encoder.Sha1Encoder;
import com.agnitas.emm.core.commons.encoder.Sha256Encoder;
import com.agnitas.emm.core.commons.encoder.Sha512Encoder;
import com.agnitas.emm.core.hashtag.AbstractColonHashTag;
import com.agnitas.emm.core.hashtag.HashTagContext;
import com.agnitas.emm.core.hashtag.exception.HashTagException;

/**
 * MD5-Encoding of profile field values is legacy code and may still use MD5-Encoder
 */
@SuppressWarnings("deprecation")
public abstract class AbstractProfileFieldEncodingHashTag extends AbstractColonHashTag {
	
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(AbstractProfileFieldEncodingHashTag.class);

	private ProfileFieldHashTagSupport support;
	
	/** Map of all available encoders. Keys are the encoding names in lower case. */
	private final Map<String, ByteArrayEncoder> encoderMap;
	
	// ------------------------------------------------------------------------------------- Business Code

	public AbstractProfileFieldEncodingHashTag() {
		this.encoderMap = initializeEncoderMap();
	}
	
	private static final Map<String, ByteArrayEncoder> initializeEncoderMap() {
		 final Map<String, ByteArrayEncoder> map = new HashMap<>();
		 
		 map.put("md5", new MD5Encoder());
		 map.put("sha1", new Sha1Encoder());
		 map.put("sha256",  new Sha256Encoder());
		 map.put("sha512", new Sha512Encoder());
		 
		 return map;
	}

	@Override
	public final String handleInternal(final HashTagContext context, final String tagName, final String appendix) {
		final String[] parts = appendix.split(",");
		
		if(parts.length == 0) {
			return "";
		} else {
			final String profileFieldName = parts[0];
			final String[] encodingNames = Arrays.copyOfRange(parts, 1, parts.length);
			
			try {
				return postProcessResult(encodeProfileField(context, tagName, profileFieldName, encodingNames), tagName);
			} catch(final Exception e) {
				final String message = String.format("Error encoding profile field '%s' with hash tag '%s' (appendix is '%s')", parts[0], tagName, appendix);
				
				logger.warn(message, e);
				
				return "";
			}
		}
	}
	
	protected String postProcessResult(final String string, final String tagName) {
		return string;
	}
	
	private final String encodeProfileField(final HashTagContext context, final String tagName, final String profileFieldName, final String[] encodingNames) throws EncodingException, HashTagException {
		final String profileFieldValue = this.support.evaluateExpression(context, profileFieldName);
		
		byte[] data = profileFieldValue.getBytes(CharsetConstants.UTF_8);
		data = encodeData(data, encodingNames);
		
		return getByteArrayToStringEncoder().encodeToString(data, CharsetConstants.UTF_8);
	}
	
	private final byte[] encodeData(final byte[] data, final String[] encodingNames) throws EncodingException {
		byte[] encodingData = Arrays.copyOf(data, data.length);

		for(String encodingName : encodingNames) {
			final ByteArrayEncoder encoder = getByteArrayEncoder(encodingName);
			encodingData = encoder.encode(encodingData);
		}
		
		return encodingData;
	}
	
	private final ByteArrayEncoder getByteArrayEncoder(final String name) throws UnknownEncodingException {
		final ByteArrayEncoder encoder = this.encoderMap.get(name.toLowerCase());
		
		if(encoder == null) {
			throw new UnknownEncodingException(name);
		}
		
		return encoder;
	}
	
	public abstract ByteArrayToStringEncoder getByteArrayToStringEncoder();

	// ------------------------------------------------------------------------------------- Dependency Injection
	
	@Required
	public final void setProfileFieldHashTagSupport(final ProfileFieldHashTagSupport support) {
		this.support = Objects.requireNonNull(support, "Profile field Hashtag support is null");
	}

}
