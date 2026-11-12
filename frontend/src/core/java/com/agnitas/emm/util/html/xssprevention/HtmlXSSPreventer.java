/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.util.html.xssprevention;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Pattern;

import net.htmlparser.jericho.Attribute;
import net.htmlparser.jericho.Attributes;
import net.htmlparser.jericho.EndTag;
import net.htmlparser.jericho.HTMLElements;
import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.StartTag;
import net.htmlparser.jericho.Tag;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.owasp.html.HtmlChangeListener;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;

/**
 * Code to check a String for forbidden tags and attributes and unclosed / unopened tags.
 *
 */
public final class HtmlXSSPreventer {

	private HtmlXSSPreventer() {

	}
	
	/*
	 * IMPORTANT: Keep this class stateless!!!
	 */
    private static final String[] ALLOWED_HTML_TAGS = { "u", "i", "b", "sup", "sub", "strong", "em" };
	private static final Set<String> END_TAG_REQUIRED_ELEMENTS = HTMLElements.getEndTagRequiredElementNames();
	private static final Pattern TAG_START = Pattern.compile("<[a-zA-Z/!?]");
	private static final PolicyFactory DEFAULT_POLICY = buildPolicy(ALLOWED_HTML_TAGS);

    public static void checkString(final String string) throws XSSHtmlException {
		checkString(string, ALLOWED_HTML_TAGS);
	}
    
	public static void checkString(final String string, String[] whitelist) throws XSSHtmlException {
    	if (whitelist == null) {
    		whitelist = ALLOWED_HTML_TAGS;
		}
		final Source source = new Source(string);
		final Stack<String> openedTags = new Stack<>();
		final Set<HtmlCheckError> errors = new HashSet<>();
		final List<Tag> tags = source.getAllTags();

		// Perform checks on each tag
		for(final Tag tag : tags) {
			if(tag instanceof StartTag) {
				checkStartTag(tag, whitelist, openedTags, errors);
			} else if(tag instanceof EndTag) {
				checkEndTag(tag, openedTags, errors);
			}
		}
		
		checkUnclosedTags(openedTags, errors);

		// GWUA-7164
		// Jericho's Source may miss tags that browsers still execute. For example, HTML5
		// treats '/' after a tag name as whitespace, so <Body/XSS onload=...> is parsed
		// as <body onload=...>. A second allowlist-based parser catches such discrepancies.
//		errors.addAll(findSanitizerErrors(string, whitelist));

		// Found at least one error? Throw an exception
		if(!errors.isEmpty()) {
			throw new XSSHtmlException(errors);
		}
	}

	/**
	 * Runs the input through the OWASP sanitizer (allowing only the whitelisted tags, no attributes)
	 * and reports whatever it discards.
	 */
	public static Set<HtmlCheckError> findSanitizerErrors(String input, String[] whitelist) {
		if (!TAG_START.matcher(StringUtils.defaultString(input)).find()) {
			return Collections.emptySet();
		}
		Set<HtmlCheckError> errors = new HashSet<>();

		String sanitized = policyFor(whitelist).sanitize(input, new HtmlChangeListener<>() {
            @Override
            public void discardedTag(Set<HtmlCheckError> ctx, String elementName) {
                ctx.add(new ForbiddenTagError(elementName));
            }
            @Override
            public void discardedAttributes(Set<HtmlCheckError> ctx, String tagName, String... attributeNames) {
                Arrays.stream(attributeNames)
                        .forEach(attributeName -> ctx.add(new ForbiddenTagAttributeError(tagName, attributeName)));
            }
        }, errors);

		// Regex saw a tag start, sanitizer reported nothing and kept no real tag
		if (errors.isEmpty() && !TAG_START.matcher(sanitized).find()) {
			errors.add(new ForbiddenTagError(sanitized));
		}
		return errors;
	}

	private static PolicyFactory policyFor(String[] whitelist) {
		return whitelist == ALLOWED_HTML_TAGS ? DEFAULT_POLICY : buildPolicy(whitelist);
	}

	private static PolicyFactory buildPolicy(String[] whitelist) {
		return new HtmlPolicyBuilder()
				.allowElements(Arrays.stream(whitelist).map(String::toLowerCase).toArray(String[]::new))
				.toFactory();
	}

	// -------------------------------------------------------------------------------------------------------- Start tag related tests
	private static void checkStartTag(final Tag tag, String[] whitelist, final Stack<String> openedTags, final Set<HtmlCheckError> errors) {
		openedTags.push(tag.getName());
		
		checkWhitelistedTag(tag, whitelist, errors);
		checkTagAttributes(tag, errors);
	}
	
	private static void checkWhitelistedTag(final Tag tag, String[] whitelist, final Set<HtmlCheckError> errors) {
		if(!isWhitelisted(tag, whitelist)) {
			errors.add(new ForbiddenTagError(tag.getName()));
		}
	}
	
	private static void checkTagAttributes(final Tag tag, final Set<HtmlCheckError> errors) {
		final Attributes attributes = tag.parseAttributes();
		
		if (CollectionUtils.isNotEmpty(attributes)) {
			attributes.stream()
					.map(Attribute::getName)
					.forEach(name -> errors.add(new ForbiddenTagAttributeError(tag.getName(), name)));
		}
	}
	
	// -------------------------------------------------------------------------------------------------------- End tag related tests
	private static void checkEndTag(final Tag tag, final Stack<String> openedTags, final Set<HtmlCheckError> errors) {
		checkOpenedTag(tag, openedTags, errors);
	}
	
	private static void checkOpenedTag(final Tag tag, final Stack<String> openedTags, final Set<HtmlCheckError> errors) {
		if(openedTags.isEmpty()) {
			errors.add(new UnopenedTagError(tag.getName()));
		} else {
			final String lastOpenedTag = openedTags.peek();
			
			if(!lastOpenedTag.equals(tag.getName()) ) {
				errors.add( new UnopenedTagError(tag.getName()));
			} else {
				openedTags.pop();
			}
		}
	}
	
	// -------------------------------------------------------------------------------------------------------- Checks related to start and end tags
	private static void checkUnclosedTags(final Stack<String> openedTags, final Set<HtmlCheckError> errors) {
		if (!openedTags.isEmpty()) {
			for (final String tagName : openedTags) {
				if (END_TAG_REQUIRED_ELEMENTS.contains(tagName.toLowerCase())) {
					errors.add(new UnclosedTagError(tagName));
				}
			}
		}
	}
	
	// -------------------------------------------------------------------------------------------------------- Utility methods
	private static boolean isWhitelisted(final Tag tag, String[] whitelist) {
		final String tagName = tag.getName();

		for(final String allowedTag : whitelist) {
			if(allowedTag.equalsIgnoreCase(tagName)) {
				return true;
			}
		}
		
		return false;
	}
}
