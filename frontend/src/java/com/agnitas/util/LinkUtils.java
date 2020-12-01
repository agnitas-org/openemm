/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.agnitas.util.AgnUtils;
import org.agnitas.util.Tuple;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.log4j.Logger;

import com.agnitas.beans.LinkProperty;

import bsh.StringUtil;

public class LinkUtils {
	private static final Logger logger = Logger.getLogger(LinkUtils.class);

	public static final int KEEP_UNCHANGED = -1;

	/**
	 *  link should not be tracked
	 */
    public static final int TRACKABLE_NO = 0;

    /**
	 * link should be tracked
	 */
    public static final int TRACKABLE_YES = 1;

	/**
	 * link should be tracked with mailing information(when applicable)
	 */
    public static final int TRACKABLE_YES_WITH_MAILING_INFO = 2;

	/**
	 * link should be tracked with mailing information(when applicable) and
	 * user(when applicable)
	 */
    public static final int TRACKABLE_YES_WITH_MAILING_AND_USER_INFO = 3;

	public static boolean isExtension(LinkProperty.PropertyType type) {
		return LinkProperty.PropertyType.LinkExtension == type;
	}

	public static boolean isExtension(LinkProperty linkProperty) {
		return linkProperty != null && isExtension(linkProperty.getPropertyType());
	}

	public static boolean isNotExtension(LinkProperty.PropertyType type) {
		return !isExtension(type);
	}

	public static boolean isNotExtension(LinkProperty linkProperty) {
		return !isExtension(linkProperty);
	}

	public static int countLinkExtensions(List<LinkProperty> linkProperties) {
		if(linkProperties == null || linkProperties.isEmpty()) {
    		return 0;
		}

		return (int) linkProperties.stream()
				.map(LinkProperty::getPropertyType)
				.filter(LinkUtils::isExtension)
				.count();
	}

	public static List<LinkProperty> collectCommonExtensions(Map<Integer, List<LinkProperty>> trackableLinks) {
		return trackableLinks.values().stream()
				.filter(Objects::nonNull)
				.map(ArrayList::new)
				.reduce((p1, p2) -> { p1.retainAll(p2); return p1; })
				.orElse(new ArrayList<>());
	}

	public static List<LinkProperty> parseLinkExtension(String linkExtension) {
		if (StringUtils.isNotEmpty(linkExtension)) {
        	try {
				linkExtension = StringUtils.removeStart(StringUtils.trim(linkExtension), "?");
				List<LinkProperty> extensions = new ArrayList<>();
				for (String query : linkExtension.split("&")) {
					String[] arr = StringUtil.split(query, "=");
					if (ArrayUtils.isNotEmpty(arr)) {
						Tuple<String, String> pair = arr.length > 1 ? new Tuple<>(arr[0], arr[1]) : new Tuple<>(arr[0], "");

						LinkProperty linkProperty = new LinkProperty(LinkProperty.PropertyType.LinkExtension,
								URLDecoder.decode(pair.getFirst(), "UTF-8"),
								URLDecoder.decode(pair.getSecond(), "UTF-8"));
						extensions.add(linkProperty);
					}
				}
				return extensions;
			} catch (Exception e) {
        		logger.error("Could not obtain default extensions: " + e.getMessage(), e);
			}
        }
		return Collections.emptyList();
	}

	/**
     * This method extends the full url of this link with its link extensions for display purposes.
     * User or mailing data is not used, so hash-tags will be left empty.
     * For usage of user and mailing data in correct replacements of hash-tags use,
     * use the methods of corresponding actions like "ComMailingContentAction"
     *
     * Caution:
     * This is used by JSP-Files
     *
     * @return full url with extensions
     */
	public static String getFullUrlWithExtensions(String linkString, List<LinkProperty> properties) {
		String directLink = linkString;
		try {
			for (LinkProperty linkProperty : properties) {
				if (isExtension(linkProperty)) {
					String propertyValue = replaceHashTagsByEmptyString(linkProperty.getPropertyValue());

					// Extend link properly (watch out for html-anchors etc.)
					directLink = AgnUtils.addUrlParameter(directLink, linkProperty.getPropertyName(), StringUtils.defaultString(propertyValue), "UTF-8");
				}
			}

			return StringEscapeUtils.escapeHtml4(directLink);
		} catch (UnsupportedEncodingException e) {
			logger.warn("Creating directory link with optional extension without user data failed, cause: " + e.getMessage());
		}
		return directLink;
	}

	private static String replaceHashTagsByEmptyString(String hashTagString) {
		if (StringUtils.isBlank(hashTagString)) {
			return hashTagString;
		} else {
			String returnString = hashTagString;
			Pattern pattern = Pattern.compile("##([^#]+)##");
			Matcher matcher = pattern.matcher(hashTagString);
			int currentPosition = 0;

			while (matcher.find(currentPosition)) {
				int matcherStart = matcher.start();
				returnString = matcher.replaceAll("");
				matcher = pattern.matcher(returnString);
				currentPosition += matcherStart;
			}
			return returnString;
		}
	}
}
