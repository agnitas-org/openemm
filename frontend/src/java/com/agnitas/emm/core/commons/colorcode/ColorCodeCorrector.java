/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.commons.colorcode;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Element;

/**
 * Expands 3-digit color codes by expanding to 6-digit codes.
 */
public class ColorCodeCorrector {

	/** Pattern finding 3-digit color codes. */
	private static final Pattern pattern = Pattern.compile("^(.*?\\#)(\\p{XDigit}{3})([^\\p{XDigit}].*)?$");
	
	/**
	 * Expands all 3-digit color codes in the text.
	 * 
	 * @param text source text
	 * 
	 * @return resulting text with expanded color codes
	 */
	private static String expandColorCode(String text) {
		String remnant = text;
		StringBuffer buffer = new StringBuffer();
		Matcher matcher = pattern.matcher(remnant);
		while(matcher.matches()) {
			buffer.append(matcher.group(1));

			String colorCode = matcher.group(2);
			for(int i = 0; i < colorCode.length(); i++) {
				buffer.append(colorCode.charAt(i)).append(colorCode.charAt(i));
			}
			
			remnant = matcher.group(3);
			
			if(remnant == null) {
				remnant = "";
				break;
			}
				
			
			matcher = pattern.matcher(remnant);
		}
		
		buffer.append(remnant);
		
		return buffer.toString();
	}
	
	/**
	 * Expand color codes in attribute value.
	 * 
	 * @param attribute attribute
	 */
	private static void expandColorCodeInAttribute(Attribute attribute) {
		attribute.setValue(expandColorCode(attribute.getValue()));
	}
	
	/**
	 * Expands color codes in body of <i>style</i> tags.
	 * 
	 * @param styleTag tag
	 */
	private static void expandColorCodeInStyleTag(Element styleTag) {
		assert(isStyleTag(styleTag));
		
		styleTag.text(expandColorCode(styleTag.data()));
	}
	
	/**
	 * Checks, if given tag is a <i>style</i> tag.
	 * 
	 * @param tag tag to check
	 * 
	 * @return true, if tag is a <i>style</i> tag.
	 */
	private static boolean isStyleTag(Element tag) {
		return "style".equalsIgnoreCase(tag.nodeName());
	}
	
	/**
	 * Checks, if given attribute is a <i>style</i> attribute.
	 * 
	 * @param attribute attribute to check
	 * 
	 * @return true, if attribute is a <i>style</i> attribute
	 */
	private static boolean isStyleAttribute(Attribute attribute) {
		return "style".equalsIgnoreCase(attribute.getKey());
	}
	
	/**
	 * Traverses the element tree expanding all color codes.
	 * 
	 * @param element root element of (sub-)tree to traverse
	 */
	private static void traverse(Element element) {
		if(isStyleTag(element)) {
			expandColorCodeInStyleTag(element);
		}
		
		Attributes attributes = element.attributes();
		
		attributes.asList().stream().filter(a -> isStyleAttribute(a)).forEach(a -> expandColorCodeInAttribute(a));

		element.children().forEach(child -> traverse(child));
	}
	
	/**
	 * Expands color codes in given HTML code.
	 * 
	 * @param html HTML code
	 * 
	 * @return HTML code with expanded color codes
	 */
	public static String expandColorCodes(String html) {
		org.jsoup.nodes.Document document = Jsoup.parse(html);

		document.children().forEach(node -> traverse(node));
		
		return document.toString();
	}
	

}
