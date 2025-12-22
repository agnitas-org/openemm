/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.preview;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.agnitas.backend.AgnTag;
import com.agnitas.beans.TagDefinition;
import com.agnitas.dao.TagDao;
import com.agnitas.preview.AgnTagError.AgnTagErrorKey;
import com.agnitas.util.AgnUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * The Class TagSyntaxChecker.
 */
public class TagSyntaxChecker {
	/**
	 * Any agnTag may have a closing slash, this is not defined by now. In a
	 * future implementation any standalone agnTags should have a closing slash
	 * analog to XML-Tags and only opening agnDYN-Tags should look like an
	 * opening XML-Tag without any slashes.
	 */
	private static boolean ALLOW_LEGACY_MISSING_CLOSING_SLASHES = true;

	private final TagDao tagDao;

	public TagSyntaxChecker(TagDao tagDao) {
		this.tagDao = tagDao;
	}

	/**
	 * Check a component text for agnTag syntax validity
	 *
	 * @param companyID   the company id
	 * @param contentText the content text
	 * @return true, if successful
	 */
	public List<AgnTagError> check(int companyID, String contentText) {
		List<AgnTagError> returnList = new ArrayList<>();
		try {
			check(companyID, contentText, returnList);
		} catch (Exception e) {
			returnList.add(new AgnTagError(AgnTagErrorKey.exceptionWhileChecking, e.getMessage()));
		}
		return returnList;
	}

	/**
	 * Check a component text for agnTag syntax validity
	 *
	 * @param companyID          the company id
	 * @param contentText        the content text
	 * @param agnTagSyntaxErrors the agn tag syntax errors
	 * @return true, if successful
	 * @throws Exception the exception
	 */
	public boolean check(int companyID, String contentText, List<AgnTagError> agnTagSyntaxErrors) throws Exception {
		try {
			String tagStartText = "[agn";
			String closingTagStartText = "[/agn";
			String tagEndText = "]";
			char tagNameSeparator = ' ';
			String trailingCloserSign = "/"; // used for [agnTag name="xxx"/]
			String closingTagSign = "/"; // only allowed for agnDYN-Tags:
			// [/agnDYN name="xxx"]
			int searchIndex = 0;
			boolean errorsFoundGlobal = false;
			boolean errorsFoundInCurrentTag;
			Stack<String> openAgnDynTags = new Stack<>();

			Map<String, TagDefinition> tagDefinitions = tagDao.getTagDefinitionsMap(companyID);

			while ((searchIndex = AgnUtils.searchNext(contentText, searchIndex, tagStartText, closingTagStartText)) >= 0) {
				errorsFoundInCurrentTag = false;

				// Search the border brackets of this tag
				int tagStartIndex = searchIndex;
				int tagEndIndex = contentText.indexOf(tagEndText, tagStartIndex + tagStartText.length());
				if (tagEndIndex < 0) {
					// No end bracket was found. Try to find the end of this
					// tags name for error text
					Matcher nextNonWordMatcher = Pattern.compile("[\\W]").matcher(contentText);
					boolean foundNonWord = nextNonWordMatcher.find(tagStartIndex + tagStartText.length());
					String maybeTagName;
					if (foundNonWord) {
						maybeTagName = contentText.substring(tagStartIndex, nextNonWordMatcher.start());
					} else {
						maybeTagName = contentText.substring(tagStartIndex);
					}
					agnTagSyntaxErrors.add(new AgnTagError(maybeTagName.substring(1), maybeTagName, AgnTagErrorKey.missingClosingBracket, contentText, tagStartIndex));
					errorsFoundInCurrentTag = true;

					searchIndex++;
				} else {
					String fullTagText = contentText.substring(tagStartIndex, tagEndIndex + 1);

					// Search for the end of the tag name
					int tagNameEndIndex = tagEndIndex;
					int tagNameSeparatorIndex = contentText.indexOf(tagNameSeparator, tagStartIndex + tagStartText.length());
					if (tagNameSeparatorIndex > 0 && tagNameSeparatorIndex < tagEndIndex) {
						tagNameEndIndex = tagNameSeparatorIndex;
					}

					String tagName = contentText.substring(tagStartIndex + 1, tagNameEndIndex);

					// tag is like [/agnTag name="xxx"]
					boolean isClosingTag = false;
					if (tagName.startsWith(closingTagSign)) {
						tagName = tagName.substring(closingTagSign.length());
						isClosingTag = true;
					}

					boolean hasTrailingCloserSign = false;
					Map<String, String> tagParameterMap = new HashMap<>();

					if (tagName.endsWith(trailingCloserSign)) {
						// tag is like [agnTag/]
						tagName = tagName.substring(0, tagName.length() - 1);
						hasTrailingCloserSign = true;
					}

					if (!errorsFoundInCurrentTag && tagNameEndIndex < tagEndIndex) {
						// Read tag parameters
						String tagParameterString = contentText.substring(tagNameEndIndex + 1, tagEndIndex);
						// tag is like [agnTag name="xxx"/]
						if (tagParameterString.endsWith(trailingCloserSign)) {
							tagParameterString = tagParameterString.substring(0, tagParameterString.length() - trailingCloserSign.length());
							hasTrailingCloserSign = true;
						}

						if (tagNameEndIndex + 1 < tagEndIndex) {
							// Read all parameters
							try {
								tagParameterMap = readTagParameterString(tagParameterString);
							} catch (AgnTagError ate) {
								if (ate.getErrorKey() == AgnTagErrorKey.invalidParameterSyntax) {
									ate.setAdditionalErrorData(new String[] {tagName});
								}
								ate.setTagName(tagName);
								ate.setFullAgnTagText(fullTagText);
								ate.setTextPosition(contentText, tagStartIndex);
								agnTagSyntaxErrors.add(ate);
								errorsFoundInCurrentTag = true;
							} catch (Exception e) {
								agnTagSyntaxErrors.add(new AgnTagError(tagName, fullTagText, AgnTagErrorKey.invalidParameterSyntax, contentText, tagStartIndex, e.getMessage()));
								errorsFoundInCurrentTag = true;
							}
						}
					}

					if (!errorsFoundInCurrentTag) {
						// Check tag name
						if (!tagDefinitions.containsKey(tagName)) {
							agnTagSyntaxErrors.add(new AgnTagError(tagName, fullTagText, AgnTagErrorKey.unknownAgnTag, contentText, tagStartIndex));
							errorsFoundInCurrentTag = true;
						}
					}

					if (!errorsFoundInCurrentTag) {
						// Check tag definitions
						if (isClosingTag && hasTrailingCloserSign) {
							// tag is like [/agnTag name="xxx"/]
							agnTagSyntaxErrors.add(new AgnTagError(tagName, fullTagText, AgnTagErrorKey.invalidAgnTagSlashes, contentText, tagStartIndex));
							errorsFoundInCurrentTag = true;
						} else if (!AgnTag.DYN.getName().equals(tagName) && isClosingTag) {
							// Only agnDYN-Tag may start with slash as a closing
							// tag
							agnTagSyntaxErrors.add(new AgnTagError(tagName, fullTagText, AgnTagErrorKey.invalidClosingAgnTag, contentText, tagStartIndex));
							errorsFoundInCurrentTag = true;
						} else if (!AgnTag.DYN.getName().equals(tagName) && !hasTrailingCloserSign && !ALLOW_LEGACY_MISSING_CLOSING_SLASHES) {
							// Any agnTag may have a closing slash, this is not
							// defined by now.
							// In a future implementation any standalone agnTags
							// should have a closing slash analog to XML-Tags
							// and only opening agnDYN-Tags should look like an
							// opening XML-Tag without any slashes.
							agnTagSyntaxErrors.add(new AgnTagError(tagName, fullTagText, AgnTagErrorKey.missingAgnTagClosingSlash, contentText, tagStartIndex));
							errorsFoundInCurrentTag = true;
						} else {
							for (String mandatoryParameterName : tagDefinitions.get(tagName).getMandatoryParameters()) {
								String mandatoryParameterValue = tagParameterMap.getOrDefault(mandatoryParameterName, "");
								if (StringUtils.isBlank(mandatoryParameterValue)) {
									agnTagSyntaxErrors.add(new AgnTagError(tagName, fullTagText, AgnTagErrorKey.missingParameter, contentText, tagStartIndex, mandatoryParameterName));
									errorsFoundInCurrentTag = true;
								}
							}
						}
					}

					if (AgnTag.DYN.getName().equals(tagName)) {
						// Check for order of agnDYN-Tags, which can be opened
						// and closed
						if (isClosingTag) {
							if (openAgnDynTags.size() == 0) {
								agnTagSyntaxErrors.add(new AgnTagError(tagName, fullTagText, AgnTagErrorKey.invalidClosingAgnDynTag_notOpened, contentText, tagStartIndex));
								errorsFoundInCurrentTag = true;
							} else if (openAgnDynTags.peek().equals(tagParameterMap.get("name"))) {
								openAgnDynTags.pop();
							} else {
								agnTagSyntaxErrors.add(new AgnTagError(tagName, fullTagText, AgnTagErrorKey.invalidClosingAgnDynTag_notMatchingLastOpenedName, contentText, tagStartIndex, openAgnDynTags.peek()));
								errorsFoundInCurrentTag = true;
							}
						} else if (!hasTrailingCloserSign) {
							// Opening agnDYN-Tag
							if (tagParameterMap.get("name") != null) {
								openAgnDynTags.push(tagParameterMap.get("name"));
							}
						}
					} else if (AgnTag.DVALUE.getName().equals(tagName)) {
						// Check for enclosing of agnDVALUE-Tags, which may only
						// be used within the matching agnDYN-Tags
						if (!openAgnDynTags.contains(tagParameterMap.get("name"))) {
							agnTagSyntaxErrors.add(new AgnTagError(tagName, fullTagText, AgnTagErrorKey.unwrappedAgnDvalueTag, contentText, tagStartIndex, tagParameterMap.get("name")));
							errorsFoundInCurrentTag = true;
						}
					}

					searchIndex = tagEndIndex;
				}

				errorsFoundGlobal = errorsFoundGlobal || errorsFoundInCurrentTag;
			}

			if (openAgnDynTags.size() > 0) {
				agnTagSyntaxErrors.add(new AgnTagError(AgnTag.DYN.getName(), "[agnDYN name=\"" + openAgnDynTags.peek() + "\"]", AgnTagErrorKey.missingClosingAgnDynTag, openAgnDynTags.peek()));
				errorsFoundGlobal = true;
			}

			return !errorsFoundGlobal;
		} catch (Exception e) {
			throw new Exception("Error in agn-Tags: " + e.getMessage(), e);
		}
	}

	/**
	 * Scann for name attributes of occurences of a defined list of agnTag
	 * names.
	 */
	public static List<String> scanForAgnTags(String contentText, String... tagNames) {
		if (contentText == null) {
			return new ArrayList<>();
		}

		int searchIndex = 0;
		try {
			List<String> tagContents = new ArrayList<>();
			char tagNameSeparator = ' ';

			for (String tagName : tagNames) {
				// Only if the tagname is followed by tagNameSeparator, there
				// can be a name attribute, other occurences of this agnTag are
				// ignored
				String tagStartText = "[" + tagName + tagNameSeparator;
				String tagEndText = "]";
				while ((searchIndex = AgnUtils.searchNext(contentText, searchIndex, tagStartText)) >= 0) {
					// Search the border brackets of this tag
					int tagStartIndex = searchIndex;
					int tagEndIndex = contentText.indexOf(tagEndText, tagStartIndex + tagStartText.length());
					if (tagEndIndex < 0) {
						// No end bracket was found.
						throw new Exception("Error in scanForAgnTagNameValues for '" + tagName + "' at position: " + tagStartIndex + " : Missing closing brackets");
					} else {
						tagContents.add(contentText.substring(tagStartIndex, tagEndIndex + tagEndText.length()));

						searchIndex = tagEndIndex + tagEndText.length();
					}
				}
			}
			return tagContents;
		} catch (Exception e) {
			throw new RuntimeException("Error in scanForAgnTagNameValues at position " + searchIndex + ": " + e.getMessage(), e);
		}
	}

	/**
	 * Scann for name attributes of occurences of a defined list of agnTag
	 * names.
	 */
	public static List<String> scanForAgnTagNameValues(String contentText, String... tagNames) throws Exception {
		if (contentText == null) {
			return new ArrayList<>();
		}

		int searchIndex = 0;
		try {
			List<String> nameValues = new ArrayList<>();
			char tagNameSeparator = ' ';
			String trailingCloserSign = "/";

			for (String tagName : tagNames) {
				// Only if the tagname is followed by tagNameSeparator, there
				// can be a name attribute, other occurences of this agnTag are
				// ignored
				String tagStartText = "[" + tagName + tagNameSeparator;
				String tagEndText = "]";
				while ((searchIndex = AgnUtils.searchNext(contentText, searchIndex, tagStartText)) >= 0) {
					// Search the border brackets of this tag
					int tagStartIndex = searchIndex;
					int tagEndIndex = contentText.indexOf(tagEndText, tagStartIndex + tagStartText.length());
					if (tagEndIndex < 0) {
						// No end bracket was found.
						throw new Exception("Error in scanForAgnTagNameValues for '" + tagName + "' at position: " + tagStartIndex + " : Missing closing brackets");
					} else {
						// Read tag parameters
						String tagParameterString = contentText.substring(tagStartIndex + tagStartText.length(), tagEndIndex);
						if (tagParameterString.endsWith(trailingCloserSign)) {
							tagParameterString = tagParameterString.substring(0, tagParameterString.length() - trailingCloserSign.length());
						}

						try {
							Map<String, String> tagParameterMap = readTagParameterString(tagParameterString);
							if (tagParameterMap.containsKey("name")) {
								nameValues.add(tagParameterMap.get("name"));
							}
						} catch (AgnTagError ate) {
							if (ate.getErrorKey() == AgnTagErrorKey.invalidParameterSyntax) {
								ate.setAdditionalErrorData(new String[] {tagName});
							}
							ate.setTagName(tagName);
							ate.setFullAgnTagText(contentText.substring(tagStartIndex, tagEndIndex + tagEndText.length()));
							ate.setTextPosition(contentText, tagStartIndex);
							throw new Exception("Error in scanForAgnTagNameValues for '" + tagName + "': " + ate.getMessage(), ate);
						} catch (Exception e) {
							AgnTagError ate = new AgnTagError(tagName, contentText.substring(tagStartIndex, tagEndIndex + tagEndText.length()),
									AgnTagErrorKey.invalidParameterSyntax, contentText, tagStartIndex, e.getMessage());
							throw new Exception("Error in scanForAgnTagNameValues for '" + tagName + "': " + ate.getMessage(), ate);
						}

						searchIndex = tagEndIndex + tagEndText.length();
					}
				}
			}
			return nameValues;
		} catch (Exception e) {
			throw new Exception("Error in scanForAgnTagNameValues at position " + searchIndex + ": " + e.getMessage(), e);
		}
	}

	/**
	 * Read tag parameter string.
	 *
	 * @param agnTagParameterString the agn tag parameter string
	 * @return the map
	 */
	public static Map<String, String> readTagParameterString(String agnTagParameterString) {
		Map<String, String> returnMap = new HashMap<>();

		if (StringUtils.isBlank(agnTagParameterString)) {
			return returnMap;
		}

		// Normalize Linebreaks
		agnTagParameterString = agnTagParameterString.replace("\r\n", "\n").replace("\r", "\n").trim();

		StringBuilder nextKey = new StringBuilder();
		StringBuilder nextValue = new StringBuilder();
		for (int index = 0; index < agnTagParameterString.length(); index++) {
			char nextChar = agnTagParameterString.charAt(index);

			if (returnMap.size() > 0 && nextChar != '\n' && nextChar != '\t' && nextChar != ' ') {
				// Missing keyValuePairSeparator
				throw new AgnTagError(AgnTagErrorKey.invalidParameterSyntax);
			}

			// skip leading whitespaces before key
			while (nextChar == '\n' || nextChar == '\t' || nextChar == ' ') {
				index++;
				if (index >= agnTagParameterString.length()) {
					break;
				}
				nextChar = agnTagParameterString.charAt(index);
			}

			if (index >= agnTagParameterString.length()) {
				break;
			}

			// read up to keyValueSeparator (keys may not be quoted)
			while (nextChar != '=') {
				nextKey.append(nextChar);
				index++;
				if (index >= agnTagParameterString.length()) {
					break;
				}
				nextChar = agnTagParameterString.charAt(index);
			}

			if (index >= agnTagParameterString.length()) {
				break;
			}

			nextKey = new StringBuilder(nextKey.toString().trim());

			// Skip keyValueSeparator
			index++;
			if (index >= agnTagParameterString.length()) {
				break;
			}
			nextChar = agnTagParameterString.charAt(index);

			// skip leading whitespaces before value
			while (nextChar == '\n' || nextChar == '\t' || nextChar == ' ') {
				index++;
				if (index >= agnTagParameterString.length()) {
					break;
				}
				nextChar = agnTagParameterString.charAt(index);
			}

			if (index >= agnTagParameterString.length()) {
				break;
			}

			// read quoted value
			if (nextChar == '"' || nextChar == '\'') {
				nextValue.append(nextChar);
				char quoteChar = nextChar;
				index++;
				if (index >= agnTagParameterString.length()) {
					break;
				}
				nextChar = agnTagParameterString.charAt(index);
				while (nextChar != quoteChar) {
					nextValue.append(nextChar);
					index++;
					if (index >= agnTagParameterString.length()) {
						break;
					}
					nextChar = agnTagParameterString.charAt(index);
				}
				if (index >= agnTagParameterString.length()) {
					throw new AgnTagError(AgnTagErrorKey.invalidQuotesInValue);
				}
				nextValue.append(nextChar);
				if (nextValue.charAt(nextValue.length() - 1) == quoteChar) {
					// remove enclosing quotes
					nextValue.deleteCharAt(nextValue.length() - 1);
					nextValue.deleteCharAt(0);
				}
			} else {
				while (nextChar != '\n' && nextChar != '\t' && nextChar != ' ') {
					if (nextChar == '\'' || nextChar == '"') {
						throw new AgnTagError(AgnTagErrorKey.invalidQuotesInValue);
					}
					nextValue.append(nextChar);
					index++;
					if (index >= agnTagParameterString.length()) {
						break;
					}
					nextChar = agnTagParameterString.charAt(index);
				}
				if (nextValue.toString().trim().startsWith("&")) {
					String nextValueString = StringEscapeUtils.unescapeHtml4(nextValue.toString().trim());
					if (nextValueString.startsWith("\"")) {
						nextValueString = StringUtils.strip(nextValueString, "\"");
					} else if (nextValueString.startsWith("'")) {
						nextValueString = StringUtils.strip(nextValueString, "'");
					}
					nextValue = new StringBuilder(nextValueString);
				} else {
					nextValue = new StringBuilder(nextValue.toString().trim());
				}
			}

			if (nextKey.indexOf("'") > -1 || nextKey.indexOf("\"") > -1) {
				throw new AgnTagError(AgnTagErrorKey.invalidQuotedKey);
			} else if (nextKey.indexOf(" ") > -1 || nextKey.indexOf("\n") > -1 || nextKey.indexOf("\t") > -1) {
				throw new AgnTagError(AgnTagErrorKey.invalidWhitespace);
			} else if (nextValue.length() == 0) {
				throw new AgnTagError(AgnTagErrorKey.invalidEmptyKey);
			} else {
				returnMap.put(nextKey.toString(), nextValue.toString());
				nextKey = new StringBuilder();
				nextValue = new StringBuilder();
			}
		}

		if (nextKey.length() > 0) {
			if (nextKey.indexOf("'") > -1 || nextKey.indexOf("\"") > -1) {
				throw new AgnTagError(AgnTagErrorKey.invalidQuotedKey);
			} else if (nextKey.indexOf(" ") > -1 || nextKey.indexOf("\n") > -1 || nextKey.indexOf("\t") > -1) {
				throw new AgnTagError(AgnTagErrorKey.invalidWhitespace);
			} else if (nextValue.length() == 0) {
				throw new AgnTagError(AgnTagErrorKey.invalidEmptyKey);
			} else {
				returnMap.put(nextKey.toString(), nextValue.toString());
			}
		}

		return returnMap;
	}

	public static Map<String, String> getParametersFromAgnTagString(String agnTagString) {
		if (StringUtils.isBlank(agnTagString)) {
			return new HashMap<>();
		} else {
			agnTagString = agnTagString.trim();
			String agnTagParameterString;
			if (agnTagString.startsWith("[")) {
				if (!agnTagString.endsWith("]")) {
					return new HashMap<>();
				} else {
					int parameterStart = agnTagString.indexOf(" ");
					if (parameterStart < 0) {
						return new HashMap<>();
					} else {
						agnTagParameterString = agnTagString.substring(parameterStart + 1, agnTagString.length() - 1);
						if (agnTagParameterString.endsWith("/")) {
							agnTagParameterString = agnTagParameterString.substring(0, agnTagParameterString.length() - 1).trim();
						}
						return readTagParameterString(agnTagParameterString);
					}
				}
			} else {
				return readTagParameterString(agnTagString);
			}
		}
	}
}
