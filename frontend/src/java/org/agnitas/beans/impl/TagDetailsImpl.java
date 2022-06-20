/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.beans.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.agnitas.beans.TagDetails;
import org.agnitas.util.AttributeParser;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TagDetailsImpl implements TagDetails {
	private static final transient Logger logger = LogManager.getLogger(TagDetailsImpl.class);

	private static Pattern PATTERN_TAGNAME_AND_PARAMETERS = Pattern.compile("\\[\\s*/?([^\\]/\\s]++)\\s*(.*?)\\s*/?\\s*\\]");
	private static int PATTERN_GROUP_TAGNAME = 1;
	private static int PATTERN_GROUP_PARAMETERS = 2;

	protected int startPos;
	protected int endPos;
	protected String fullText;

	/** Holds value of property tagName. */
	protected String tagName;

	/** Holds value of property tagParameters. */
	protected Map<String, String> tagParameters;

	/** Creates new TagDetails */
	public TagDetailsImpl() {
	}

	@Override
	public int getStartPos() {
		return startPos;
	}

	@Override
	public int getEndPos() {
		return endPos;
	}

	@Override
	public String getFullText() {
		return fullText;
	}

	@Override
	public String getName() {
		return tagParameters.get("name");
	}

	/**
	 * Getter for property tagName.
	 * 
	 * @return Value of property tagName.
	 * 
	 */
	@Override
	public String getTagName() {
		return this.tagName;
	}

	/**
	 * Setter for property tagName.
	 * 
	 * @param tagName
	 *            New value of property tagName.
	 * 
	 */
	@Override
	public void setTagName(String tagName) {
		this.tagName = tagName;
	}

	@Override
	public String findTagName() {
		tagName = null;

		Matcher matcher = PATTERN_TAGNAME_AND_PARAMETERS.matcher(fullText);
		if (matcher.matches()) {
			tagName = matcher.group(PATTERN_GROUP_TAGNAME);
		} else {
			logger.error("Unable to parse a tag: " + fullText);
		}

		return tagName;
	}

	@Override
	public boolean findTagParameters() {
		Matcher matcher = PATTERN_TAGNAME_AND_PARAMETERS.matcher(fullText);
		if (matcher.matches()) {
			if (StringUtils.isEmpty(tagName)) {
				tagName = matcher.group(PATTERN_GROUP_TAGNAME);
			}
			String attributes = matcher.group(PATTERN_GROUP_PARAMETERS);
			if (attributes != null) {
				try {
					tagParameters = AttributeParser.parse(attributes);
					return true;
				} catch (Exception e) {
					logger.error("Error in tag parameters: " + e.getMessage(), e);
					return false;
				}
			}
		} else {
			logger.error("Unable to parse a tag: " + fullText);
		}
		tagParameters = null;
		return false;
	}

	/**
	 * Getter for property tagParameters.
	 * 
	 * @return Value of property tagParameters.
	 * 
	 */
	@Override
	public Map<String, String> getTagParameters() {
		return this.tagParameters;
	}

	/**
	 * Setter for property tagParameters.
	 * 
	 * @param tagParameters
	 *            New value of property tagParameters.
	 * 
	 */
	@Override
	public void setTagParameters(Map<String, String> tagParameters) {
		// nothing to do
	}

	/**
	 * Setter for property endPos.
	 * 
	 * @param endPos
	 *            New value of property endPos.
	 */
	@Override
	public void setEndPos(int endPos) {
		this.endPos = endPos;
	}

	/**
	 * Setter for property fullText.
	 * 
	 * @param fullText
	 *            New value of property fullText.
	 */
	@Override
	public void setFullText(java.lang.String fullText) {
		this.fullText = fullText;
	}

	/**
	 * Setter for property name.
	 * 
	 * @param name
	 *            New value of property name.
	 */
	@Override
	public void setName(String name) {
		if (tagParameters == null) {
			tagParameters = new HashMap<>(); 
		}
		tagParameters.put("name", name);
	}

	/**
	 * Setter for property startPos.
	 * 
	 * @param startPos
	 *            New value of property startPos.
	 */
	@Override
	public void setStartPos(int startPos) {
		this.startPos = startPos;
	}
}
