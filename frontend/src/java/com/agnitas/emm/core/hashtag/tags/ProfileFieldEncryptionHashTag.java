/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.hashtag.tags;

import java.util.Objects;

import com.agnitas.emm.core.commons.encrypt.ProfileFieldEncryptor;
import com.agnitas.emm.core.hashtag.AbstractColonHashTag;
import com.agnitas.emm.core.hashtag.HashTagContext;

/**
 * Implementation of "encrypt:profilefield" hash tag.
 * 
 * <b>Note: This implementation is limited to profile fields. Encryption of
 * reference tables is currently not supported!</b>
 */
public class ProfileFieldEncryptionHashTag extends AbstractColonHashTag {
	
	private ProfileFieldHashTagSupport support;

	/** Encryptor for profile field content. */
	private ProfileFieldEncryptor encryptor;
	
	@Override
	public boolean isSupportedTag(String tagName, boolean hasColon) {
		// Handling of tag is independent of presence/absence of colon and/or appendix.
		return "encrypt".equalsIgnoreCase(tagName);
	}

	@Override
	public String handleInternal(HashTagContext context, String tagName, String appendix) {
		try {
			final String replacement = this.support.evaluateExpression(context, appendix);
			
			return encryptor.encryptToBase64(replacement, context.getCompanyID(), context.getCustomerId());
		} catch(Exception e) {
			return "";
		}
	}

	/**
	 * Set encryptor for profile field content.
	 * 
	 * @param encryptor encryptor for profile field content.
	 */
	public final void setProfileFieldEncryptor(final ProfileFieldEncryptor encryptor) {
		this.encryptor = encryptor;
	}

	public final void setProfileFieldHashTagSupport(final ProfileFieldHashTagSupport support) {
		this.support = Objects.requireNonNull(support, "Profile field Hashtag support is null");
	}
}
