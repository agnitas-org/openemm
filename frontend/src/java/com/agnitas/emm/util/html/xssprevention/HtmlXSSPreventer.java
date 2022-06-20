/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.util.html.xssprevention;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Stream;

import au.id.jericho.lib.html.Attribute;
import au.id.jericho.lib.html.Attributes;
import au.id.jericho.lib.html.EndTag;
import au.id.jericho.lib.html.HTMLElements;
import au.id.jericho.lib.html.Source;
import au.id.jericho.lib.html.StartTag;
import au.id.jericho.lib.html.Tag;

/**
 * Code to check a String for forbidden tags and attributes and unclosed / unopened tags.
 *
 */
public final class HtmlXSSPreventer {
	
	/*
	 * IMPORTANT: Keep this class stateless!!!
	 */
	
    private static final String[] ALLOWED_HTML_TAGS = { "u", "i", "b", "sup", "sub", "strong", "em" };
    
    private static final String[] UNSUPPORTED_TAGS = { "applet", "object", "embed", "iframe" };

    @SuppressWarnings("unchecked")
	private static final Set<String> END_TAG_REQUIRED_ELEMENTS = HTMLElements.getEndTagRequiredElementNames();

    public static final void checkUnsupportedTags(final String string) throws XSSHtmlException {
		final Set<HtmlCheckError> errors = new HashSet<>();
    	
		final Source source = new Source(string);

		@SuppressWarnings("unchecked")
		final List<Tag> tags = source.findAllTags();
		
		for(final Tag tag : tags) {
			if(tag instanceof StartTag || tag instanceof EndTag) {
				// isWhitelisted() is misused here to check, if tag is listed in given array.
				if(isWhitelisted(tag, UNSUPPORTED_TAGS)) {
					errors.add(new ForbiddenTagError(tag.getName()));
				}
			}
		}
		
		// Found at least one error? Throw an exception
		if(!errors.isEmpty()) {
			throw new XSSHtmlException(errors);
		}

    }
    
    
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

		@SuppressWarnings("unchecked")
		final List<Tag> tags = source.findAllTags();

		// Perform checks on each tag
		for(final Tag tag : tags) {
			if(tag instanceof StartTag) {
				checkStartTag(tag, whitelist, openedTags, errors);
			} else if(tag instanceof EndTag) {
				checkEndTag(tag, openedTags, errors);
			}
		}
		
		checkUnclosedTags(openedTags, errors);
		
		// Found at least one error? Throw an exception
		if(!errors.isEmpty()) {
			throw new XSSHtmlException(errors);
		}
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
		
		if(attributes.size() > 0) {
			((Stream<?>)attributes.stream())
					.map(attr -> ((Attribute)attr).getName())
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
