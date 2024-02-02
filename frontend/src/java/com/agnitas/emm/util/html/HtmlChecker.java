/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.util.html;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import net.htmlparser.jericho.Attribute;
import net.htmlparser.jericho.Attributes;
import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.StartTag;
import net.htmlparser.jericho.Tag;

/**
 * Code to check a String for forbidden tags and attributes
 */
public final class HtmlChecker {
	/**
	 * These HTML tags are always allowed, but may not have any attributes
	 */
	private static final String[] ALWAYS_ALLOWED_HTML_TAGS = { "u", "i", "b", "sup", "sub", "strong", "em" };
	
	/**
	 * These HTML tags are always forbidden
	 */
	private static final String[] ALWAYS_FORBIDDEN_HTML_TAGS = { "applet", "object", "embed", "iframe", "script" };

	/**
	 * Check a String for forbidden tags and attributes
	 * 
	 * @param text
	 *  text to check for HTML tags
	 * 
	 * @param allowSafeHtmlTags
	 * 	Tags which are not included in ALWAYS_ALLOWED_HTML_TAGS or ALWAYS_FORBIDDEN_HTML_TAGS may be optionally allowed.
	 * 	Those safe tags must not have any attributes, whose name starts with "on...".
	 */
	public static void checkForUnallowedHtmlTags(final String text, boolean allowSafeHtmlTags) throws HtmlCheckerException {
		if (StringUtils.isNotBlank(text)) {
			final Source source = new Source(text);
			final Set<HtmlCheckerError> errors = new HashSet<>();
			
			for (final Tag tag : source.getAllTags()) {
				if (tag instanceof StartTag) {
					checkStartTag(allowSafeHtmlTags, tag, errors);
				}
			}
	
			if (!errors.isEmpty()) {
				throw new HtmlCheckerException(errors);
			}
		}
	}
	
	public static void checkForNoHtmlTags(final String text) throws HtmlCheckerException {
		if (StringUtils.isNotBlank(text)) {
			final Source source = new Source(text);
			final Set<HtmlCheckerError> errors = new HashSet<>();
			
			for (final Tag tag : source.getAllTags()) {
				if (tag instanceof StartTag) {
					errors.add(new HtmlCheckerForbiddenTagError(tag.getName()));
				}
			}
	
			if (!errors.isEmpty()) {
				throw new HtmlCheckerException(errors);
			}
		}
	}

	private static void checkStartTag(boolean allowSafeHtmlTags, final Tag tag, final Set<HtmlCheckerError> errors) {
		if (containsIgnoreCase(tag.getName(), ALWAYS_FORBIDDEN_HTML_TAGS)) {
			errors.add(new HtmlCheckerForbiddenTagError(tag.getName()));
		} else if (containsIgnoreCase(tag.getName(), ALWAYS_ALLOWED_HTML_TAGS)) {
			final Attributes attributes = tag.parseAttributes();
			if (!"!--".equals(tag.getName()) && CollectionUtils.isNotEmpty(attributes) && !allowSafeHtmlTags) {
				attributes.stream().map(Attribute::getName).forEach(name -> errors.add(new HtmlCheckerForbiddenTagAttributeError(tag.getName(), name)));
			}
		} else if (allowSafeHtmlTags) {
			final Attributes attributes = tag.parseAttributes();
			if (!"!--".equals(tag.getName()) && CollectionUtils.isNotEmpty(attributes)) {
				attributes.stream().map(Attribute::getName).filter(attributeName -> attributeName.toLowerCase().startsWith("on")).forEach(attributeName -> errors.add(new HtmlCheckerForbiddenTagAttributeError(tag.getName(), attributeName)));
			}
		} else {
			errors.add(new HtmlCheckerForbiddenTagError(tag.getName()));
		}
	}

	private static boolean containsIgnoreCase(final String needle, String[] haystack) {
		for (final String item : haystack) {
			if (needle == null) {
				if (item == null) {
					return true;
				}
			} else { 
				if (needle.equalsIgnoreCase(item)) {
					return true;
				}
			}
		}
		return false;
	}
}
