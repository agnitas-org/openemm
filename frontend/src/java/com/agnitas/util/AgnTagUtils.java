/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections4.ListUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

public class AgnTagUtils {
    private static final Pattern ENCODED_INNER_AGN_TAG_PATTERN = Pattern.compile("agn\\((\\S+?)\\)");
    private static final Pattern AGN_TAG_PATTERN = Pattern.compile("\\[agn[^]]+]");
    private static final Pattern ESCAPED_AGN_TAG_PATTERN = Pattern.compile("\\[agn.*?=&quot;.*?&quot;/?]");
    private static final String DEFAULT_DYN_NAME = "BLANK_DYN_NAME";

    public static String escapeAgnTags(String text) {
        return replaceAll(text, AGN_TAG_PATTERN, StringEscapeUtils::escapeHtml4);
    }

    public static boolean containsAnyAgnTag(String text) {
        if (StringUtils.isEmpty(text)) {
            return false;
        }

        return AGN_TAG_PATTERN.matcher(text).find();
    }

    public static boolean containsAnyEncodedInnerAgnTag(String text) {
		if (StringUtils.isEmpty(text)) {
			return false;
		}

		return ENCODED_INNER_AGN_TAG_PATTERN.matcher(text).find();
    }

    public static boolean isEncodedInnerAgnTag(String text) {
        if (StringUtils.isEmpty(text)) {
            return false;
        }

        return ENCODED_INNER_AGN_TAG_PATTERN.matcher(text).matches();
    }

    public static String decodeInnerAgnTag(String text) {
		// Remove agn() wrapping (if any) before decoding base64 string.
		return fromB64(StringUtils.removeEnd(StringUtils.removeStart(text, "agn("), ")"));
    }

	public static String decodeInnerAgnTags(String text) {
		return replaceAll(text, ENCODED_INNER_AGN_TAG_PATTERN, AgnTagUtils::decodeInnerAgnTag);
	}

	public static String encodeInnerAgnTag(String text) {
		return "agn(" + toB64(text) + ")";
	}

    public static String encodeInnerAgnTags(String text) {
		return replaceAll(text, AGN_TAG_PATTERN, AgnTagUtils::encodeInnerAgnTag);
    }

    public static List<String> getParametersForTag(final String tagName) {
        return ListUtils.union(getMandatoryParametersForTag(tagName), getOptionalParametersForTag(tagName));
    }

    public static List<String> getMandatoryParametersForTag(String tagName) {
        if (tagName != null) {
            switch (tagName) {
                case "agnDB":
                    return Collections.singletonList("column");

                case "agnTITLE":
                case "agnTITLEFULL":
                case "agnTITLEFIRST":
                    return Collections.singletonList("type");

                case "agnIMGLINK":
                case "agnFORM":
                case "agnDYN":
                case "agnVOUCHER":
                    return Collections.singletonList("name");

                default:
                    return Collections.emptyList();
            }
        } else {
            return Collections.emptyList();
        }
    }

    public static List<String> getOptionalParametersForTag(final String tagName) {
        final String safeTagName = StringUtils.defaultIfEmpty(tagName, "");

        if ("agnVOUCHER".equals(safeTagName)) {
            return Collections.singletonList("default");
        }
        return Collections.emptyList();
    }

    public static String unescapeAgnTags(String text) {
        if (StringUtils.isEmpty(text)) {
            return text;
        }

        StringBuilder sb = new StringBuilder();
        Matcher matcher = ESCAPED_AGN_TAG_PATTERN.matcher(text);

        while (matcher.find()) {
            matcher.appendReplacement(sb, StringEscapeUtils.unescapeHtml4(matcher.group()));
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    public static String toSafeDynName(String name) {
        return toSafeDynName(name, "", DEFAULT_DYN_NAME);
    }

    public static String toSafeDynName(String name, String squareBracketsPlaceholder) {
        return toSafeDynName(name, squareBracketsPlaceholder, DEFAULT_DYN_NAME);
    }

    public static String toSafeDynName(String name, String squareBracketsPlaceholder, String defaultName) {
        if (StringUtils.isBlank(name)) {
            return defaultName;
        }

        String placeHolder = squareBracketsPlaceholder
                .replace("[", "")
                .replace("]", "");

        name = name.replace("\"", "&quot;")
                .replace("'", "&apos;")
                .replace("[", placeHolder)
                .replace("]", placeHolder);

        return StringUtils.isBlank(name) ? defaultName : name;
    }

	private static String replaceAll(String text, Pattern pattern, Function<String, String> replace) {
		if (StringUtils.isEmpty(text)) {
			return text;
		}

		StringBuilder sb = new StringBuilder();
		Matcher matcher = pattern.matcher(text);

		while (matcher.find()) {
			matcher.appendReplacement(sb, replace.apply(matcher.group()));
		}
		matcher.appendTail(sb);

		return sb.toString();
	}

	private static String toB64(String text) {
		// Use getUrlEncoder() instead of getEncoder().
		// Default encoder (getEncoder()) uses characters that break CSS parser.
		return Base64.getUrlEncoder().withoutPadding().encodeToString(text.getBytes(StandardCharsets.UTF_8));
	}

	private static String fromB64(String text) {
		return new String(Base64.getUrlDecoder().decode(text));
	}
	
    public static String getAgnTagName(String componentLinkString) {
    	String name = StringUtils.substringBetween(componentLinkString, "name=\"", "\"/]");
    	if (!StringUtils.isBlank(name)) {
    	    return name;
        }
        name = StringUtils.substringBetween(componentLinkString, "name=\"", "\"]");
        if (!StringUtils.isBlank(name)) {
            return name;
        }
        name = StringUtils.substringBetween(componentLinkString, "name='", "'/]");
        if (!StringUtils.isBlank(name)) {
            return name;
        }
        return StringUtils.substringBetween(componentLinkString, "name='", "']");
    }
}
