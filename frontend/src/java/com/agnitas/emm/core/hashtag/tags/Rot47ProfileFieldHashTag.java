/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.hashtag.tags;

import java.util.Objects;

import com.agnitas.util.DateUtilities;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.agnitas.emm.core.commons.encoder.Rot47Encoder;
import com.agnitas.emm.core.hashtag.AbstractColonHashTag;
import com.agnitas.emm.core.hashtag.HashTagContext;
import com.agnitas.emm.core.hashtag.exception.HashTagException;

/**
 * Hash tag performing ROT47 obfuscation of profile field value.
 */
public final class Rot47ProfileFieldHashTag extends AbstractColonHashTag {
	
	private static final transient Logger logger = LogManager.getLogger(Rot47ProfileFieldHashTag.class);

	private ProfileFieldHashTagSupport support;
	
	/** Encoder (or better obfuscator) for ROT47. */
	private static final transient Rot47Encoder rot47Encoder = new Rot47Encoder();
	
	@Override
	public final boolean isSupportedTag(final String tagName, final boolean hasColon) {
		// Handling of tag is independent of presence/absence of colon and/or appendix.
		return "rot47".equalsIgnoreCase(tagName);
	}

	@Override
	public final String handleInternal(final HashTagContext context, final String tagName, final String appendix) {
		try {
			final String fieldValue = this.support.evaluateExpression(context, appendix, DateUtilities.YYYY_MM_DD_HH_MM_SS);
	
			return rot47Encoder.encode(fieldValue);
		} catch(final HashTagException e) {
			logger.error("Error handling hashtag", e);
			
			return "";
		}
	}

	public final void setProfileFieldHashTagSupport(final ProfileFieldHashTagSupport support) {
		this.support = Objects.requireNonNull(support, "Profile field Hashtag support is null");
	}

}
