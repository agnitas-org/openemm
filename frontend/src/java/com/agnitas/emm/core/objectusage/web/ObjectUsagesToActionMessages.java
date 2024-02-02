/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.objectusage.web;

import java.util.Locale;

import com.agnitas.messages.Message;

import com.agnitas.emm.core.objectusage.common.ObjectUsage;
import com.agnitas.emm.core.objectusage.common.ObjectUsages;
import com.agnitas.emm.core.objectusage.common.ObjectUserType;

// TODO: remove
public final class ObjectUsagesToActionMessages {
	
	/** Maximum number of using objects shown in error message. */
	public static final int MAX_OBJECTS_PER_MESSAGE = 5;

    /**
  	 * Converts {@link ObjectUsages} to {@link com.agnitas.web.mvc.Popups}. All using objects (up to limit defined in {@link #MAX_OBJECTS_PER_MESSAGE}),
  	 * are shown in a single message.
  	 * 
  	 * @param errorMessageKey message key for error message (without placeholder for number of usages not shown)
  	 * @param errorMessageWithMoreKey message key for error message (with placeholder for number of usages not shown) 
  	 * @param usages object usages
  	 * @param locale locale of current user
  	 */
  	public static Message objectUsagesToMessage(String errorMessageKey, String errorMessageWithMoreKey, ObjectUsages usages, Locale locale) {
        if (usages.isEmpty()) {
            return null;
        }
        if (usages.size() <= MAX_OBJECTS_PER_MESSAGE) {
            return Message.of(errorMessageKey, itemsToHtmlList(usages, locale));
        }
        return Message.of(errorMessageWithMoreKey, itemsToHtmlList(usages, locale), usages.size() - MAX_OBJECTS_PER_MESSAGE);
    }

	/**
	 * Converts object usages to a HTML list.
	 * 
	 * @param usages object usages
	 * @param locale locale of current admin
	 * 
	 * @return HTML code for list
	 */
	private static final String itemsToHtmlList(final ObjectUsages usages, final Locale locale) {
		final StringBuffer buffer = new StringBuffer("<ul>");
		
		int count = 0;
		
		for(final ObjectUserType userType : ObjectUserType.values()) {
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
