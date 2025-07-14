/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.hashtag;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.agnitas.emm.core.hashtag.exception.HashTagException;

/**
 * Abstract implementation of hash tags supporting colon. This class contains all the boilerplate code
 * needed to handle colons.
 */
public abstract class AbstractColonHashTag implements HashTag {
	
	private static final transient Logger LOGGER = LogManager.getLogger(AbstractColonHashTag.class);

	@Override
	public boolean canHandle(final HashTagContext context, final String tagString) {
		final int colonIndex = tagString.indexOf(':');
		
		final String tagName = (colonIndex == -1) ? tagString : tagString.substring(0, colonIndex);

		return isSupportedTag(tagName, colonIndex != -1);
	}

	@Override
	public final String handle(final HashTagContext context, final String tagString) throws HashTagException {
		final int colonIndex = tagString.indexOf(':');
		
		final String tagName = (colonIndex == -1) ? tagString : tagString.substring(0, colonIndex).trim();
		final String appendix = (colonIndex == -1) ? null : tagString.substring(colonIndex + 1).trim();

		final String unencoded = handleInternal(context, tagName, appendix);
		
		return encodeResult(unencoded != null ? unencoded : "");
	}

	/**
	 * Performs the final encoding. In common, URL-encoding the result of the hash tag is done here.
	 * 
	 * @param unencodedResult unencoded result
	 * 
	 * @return encoded result
	 */
	public String encodeResult(final String unencodedResult) {
       	try {
       		return URLEncoder.encode(unencodedResult, "UTF8");
    	} catch(UnsupportedEncodingException e) {
    		LOGGER.error("Error while URL-encoding", e);
    		return "";
    	}
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
