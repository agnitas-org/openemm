/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.hashtag.tags;

import java.util.Objects;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.emm.core.hashtag.HashTag;
import com.agnitas.emm.core.hashtag.HashTagContext;
import com.agnitas.emm.core.hashtag.exception.HashTagException;

/**
 * Hash tag implementation for accessing profile fields and reference tables
 * with successive URL-encoded of content.
 */
public class ComProfileFieldHashTag implements HashTag {
	/** The logger. */
	@SuppressWarnings("unused")
	private static final transient Logger logger = Logger.getLogger(ComProfileFieldHashTag.class);
	
	private ProfileFieldHashTagSupport support;

	@Override
	public boolean canHandle(HashTagContext context, String tagString) {
		// Only handle tag strings, that do not contain a "command" part (like "MD5:xxx")
		return !tagString.contains(":");
	}

	@Override
	public String handle(final HashTagContext context, final String tagString) throws HashTagException {
		return support.evaluateExpression(context, tagString);
	}
	
	@Required
	public final void setProfileFieldHashTagSupport(final ProfileFieldHashTagSupport support) {
		this.support = Objects.requireNonNull(support, "Profile field Hashtag support is null");
	}
}
