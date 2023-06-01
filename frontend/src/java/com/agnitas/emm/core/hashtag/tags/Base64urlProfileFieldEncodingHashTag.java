/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.hashtag.tags;

import com.agnitas.emm.core.commons.encoder.Base64urlEncoder;
import com.agnitas.emm.core.commons.encoder.ByteArrayToStringEncoder;

public class Base64urlProfileFieldEncodingHashTag extends AbstractProfileFieldEncodingHashTag {

	private static final Base64urlEncoder ENCODER = new Base64urlEncoder();
	
	@Override
	public final boolean isSupportedTag(final String tagName, final boolean hasColon) {
		return isRegularBase64urltag(tagName) || isStrippedBase64urltag(tagName);
	}
	
	private final boolean isRegularBase64urltag(final String tagName) {
		return "base64url".equalsIgnoreCase(tagName);
	}
	
	private final boolean isStrippedBase64urltag(final String tagName) {
		return "base64url-stripped".equalsIgnoreCase(tagName);
	}
	
	@Override
	protected String postProcessResult(final String string, final String tagName) {
		return isStrippedBase64urltag(tagName)
				? stripRight(string)
				: string;
	}

	@Override
	public ByteArrayToStringEncoder getByteArrayToStringEncoder() {
		return Base64urlProfileFieldEncodingHashTag.ENCODER;
	}
	
	private static final String stripRight(String string) {
		while(string.endsWith("=")) {
			string = string.substring(0, string.length () - 1);
		}
		
		return string;
	}

}
