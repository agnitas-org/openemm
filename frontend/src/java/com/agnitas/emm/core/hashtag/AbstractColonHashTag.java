/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.hashtag;

import com.agnitas.emm.core.hashtag.exception.HashTagException;

/**
 * Abstract implementation of hash tags supporting colon. This class contains all the boilerplate code
 * needed to handle colons.
 */
public abstract class AbstractColonHashTag implements HashTag {

	@Override
	public boolean canHandle(HashTagContext context, String tagString) {
		int colonIndex = tagString.indexOf(':');
		
		String tagName = (colonIndex == -1) ? tagString : tagString.substring(0, colonIndex);

		return isSupportedTag(tagName, colonIndex != -1);
	}

	@Override
	public String handle(HashTagContext context, String tagString) throws HashTagException {
		int colonIndex = tagString.indexOf(':');
		
		String tagName = (colonIndex == -1) ? tagString : tagString.substring(0, colonIndex).trim();
		String appendix = (colonIndex == -1) ? null : tagString.substring(colonIndex + 1).trim();

		return handleInternal(context, tagName, appendix);
	}

	/**
	 * Checks, if the implementation supports given tag name with or without colon in tag string.
	 *   
	 * @param tagName name of tag
	 * @param hasColon <code>true</code> if tag string contains colon
	 * 
	 * @return <code>true</code> if tag is supported
	 */
	public abstract boolean isSupportedTag(String tagName, boolean hasColon);
	
	/**
	 * Handles the tag. The appendix is the part after the colon. appendix can be <code>null</code> if
	 * tag has no colon. If appendix is not null but empty, a colon is found but is immediately followed
	 * by the tag termination symbols.
	 * 
	 * @param context hash tag context
	 * @param tagName name of tag
	 * @param appendix appendix after colon
	 * 
	 * @return Resulting text of tag handling
	 */
	public abstract String handleInternal(HashTagContext context, String tagName, String appendix) throws HashTagException; 
}
