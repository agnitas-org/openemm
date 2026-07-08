/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.hashtag.tags;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

import org.apache.commons.codec.binary.Hex;
import com.agnitas.emm.core.commons.encoder.MD5Encoder;
import com.agnitas.emm.core.hashtag.AbstractColonHashTag;
import com.agnitas.emm.core.hashtag.HashTagContext;
import com.agnitas.emm.core.hashtag.exception.HashTagException;

/**
 * Md5ProfileFieldHashTag may still use MD5-Encoder.
 */
public final class Md5ProfileFieldHashTag extends AbstractColonHashTag {
	
	/** Encoder for MD5 hashes. */
	private static final transient MD5Encoder MD5ENCODER = new MD5Encoder();

	private ProfileFieldHashTagSupport support;

	@Override
	public final boolean isSupportedTag(final String tagName, final boolean hasColon) {
		return "md5".equalsIgnoreCase(tagName);
	}

	@Override
	public final String handleInternal(final HashTagContext context, final String tagName, final String appendix) throws HashTagException {
		final String fieldValue = support.evaluateExpression(context, appendix);
		
		final byte[] md5Hash = MD5ENCODER.encode(fieldValue.getBytes(StandardCharsets.UTF_8));
	
		final char[] hex = Hex.encodeHex(md5Hash);
		return new String(hex);
	}

	public final void setProfileFieldHashTagSupport(final ProfileFieldHashTagSupport support) {
		this.support = Objects.requireNonNull(support, "Profile field Hashtag support is null");
	}

}
