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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * some helper methods for handling mail head, and error messages
 */
public class PreviewHelper {

	public static String getFrom(String head) {
		Pattern pattern = Pattern.compile("\\s*From\\s*:(.*)");
		Matcher matcher = pattern.matcher(head);
		if (matcher.find()) {
			return matcher.group(1).trim();
		} else {
			return null;
		}
	}

	public static String getSubject(String head) {
		Pattern pattern = Pattern.compile("\\s*Subject\\s*:(.*)");
		Matcher matcher = pattern.matcher(head);
		if (matcher.find()) {
			return matcher.group(1).trim();
		} else {
			return null;
		}
	}

	public static String getPreHeader(String head) {
		Pattern pattern = Pattern.compile("\\s*Pre-Header\\s*:(.*)");
		Matcher matcher = pattern.matcher(head);
		if (matcher.find()) {
			return matcher.group(1).trim();
		} else {
			return null;
		}
	}

	/**
	 * extract the different tags and corresponding tag-errors from
	 * the error report
	 *
	 * @param report -
	 *               each line has to use the following structure
	 *               [agnTag]:errormessage
	 * @return a map with the tag as key and the error as value
	 */
	public static Map<String, String> getTagsWithErrors(StringBuffer report) {
		Map<String, String> tagWithErrors = new HashMap<>();

		Pattern tagPattern = Pattern.compile("(\\[.*?]):(.*?)$", Pattern.MULTILINE);
		Matcher matcher = tagPattern.matcher(report.toString());
		while (matcher.find()) {
			tagWithErrors.put(matcher.group(1), matcher.group(2));
		}

		return tagWithErrors;
	}

	/**
	 * extract the errormessages which are not related with a tag
	 *
	 * @param report
	 * @return list of strings describing the error
	 */
	public static List<String> getErrorsWithoutATag(StringBuffer report) {
		List<String> errorList = new ArrayList<>();
		String reportString = report.toString();
		Pattern failedToParsePattern = Pattern.compile("\\s*Failed to parse\\s*:\\s*(.*?)\\s*#");
		Matcher matcher = failedToParsePattern.matcher(reportString);
		while (matcher.find()) {
			errorList.add(matcher.group(1));
		}

		return errorList;
	}

}
