/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.objectusage.web;

import java.util.Locale;

import com.agnitas.emm.core.objectusage.common.ObjectUsage;
import com.agnitas.emm.core.objectusage.common.ObjectUsages;
import com.agnitas.emm.core.objectusage.common.ObjectUsageType;
import com.agnitas.web.mvc.Popups;

/**
 * <p>
 * Utility class to convert {@link ObjectUsages} to {@link Popups}.
 * </p>
 * 
 * <p>
 * This class requires message keys with these placeholder:
 * <ul>
 *   <li>{0} placeholder for HTML list of using objects</li>
 *   <li>{1} placeholder for number of using objects not shown in the list (like "and {1} more objects")</li>
 * </ul>
 * </p>     
 */
public final class ObjectUsagesToPopups {

	/** Maximum number of using objects shown in error message. */
	private static final int MAX_OBJECTS_PER_MESSAGE = 5;

	private ObjectUsagesToPopups() {
    }

	/**
	 * Converts {@link ObjectUsages} to {@link Popups}. All using objects (up to limit defined in {@link #MAX_OBJECTS_PER_MESSAGE}),
	 * are shown in a single message.
	 * 
	 * @param errorMessageKey message key for error message (without placeholder for number of usages not shown)
	 * @param errorMessageWithMoreKey message key for error message (with placeholder for number of usages not shown) 
	 * @param usages object usages
	 * @param errors list of messages to add new message
	 * @param locale locale of current user
	 */
	public static void objectUsagesToPopups(final String errorMessageKey, final String errorMessageWithMoreKey, final ObjectUsages usages, final Popups errors, final Locale locale) {
		if (usages.isEmpty()) {
		    return;
		}
        if (usages.size() <= MAX_OBJECTS_PER_MESSAGE) {
            errors.alert(errorMessageKey, itemsToHtmlList(usages, locale));
        } else {
            errors.alert(errorMessageWithMoreKey, itemsToHtmlList(usages, locale), usages.size() - MAX_OBJECTS_PER_MESSAGE);
        }
    }
	
	/**
	 * Converts object usages to a HTML list.
	 * 
	 * @param usages object usages
	 * @param locale locale of current admin
	 * 
	 * @return HTML code for list
	 */
	private static String itemsToHtmlList(final ObjectUsages usages, final Locale locale) {
		final StringBuffer buffer = new StringBuffer("<ul>");
		
		int count = 0;
		
		for(final ObjectUsageType userType : ObjectUsageType.values()) {
			for(final ObjectUsage usage : usages.getUsagesByUserType(userType)) {
				count++;
				
				buffer.append("<li>");
				buffer.append(HyperlinkHelper.toHyperlink(usage, locale));
				buffer.append("</li>");

				if(count >= MAX_OBJECTS_PER_MESSAGE) {
					break;
				}
			}			
			
			if(count >= MAX_OBJECTS_PER_MESSAGE) {
				break;
			}
		}
		
		buffer.append("</ul>");
		
		return buffer.toString();
	}
	
}
