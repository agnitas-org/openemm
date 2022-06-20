/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web;

import com.agnitas.beans.Mailing;
import org.agnitas.beans.MailingComponent;
import org.agnitas.util.GuiConstants;
import org.apache.commons.lang.StringUtils;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

public class ComMailingContentChecker {
    public static void checkHtmlWarningConditions(String htmlContentString, ActionMessages messages) {
		if (StringUtils.containsIgnoreCase(htmlContentString, "background=\"#")) {
			// the attribute background causes ActionForms to load twice or multiple, because background.value should be an image and not a color-code
            messages.add(GuiConstants.ACTIONMESSAGE_CONTAINER_WARNING, new ActionMessage("warning.problematical_htmlcontent", "background=\"# ..."));
		}
    }
    
    public static void checkHtmlWarningConditions(Mailing aMailing, ActionMessages messages) {
    	for (MailingComponent component : aMailing.getComponents().values()) {
    		if (StringUtils.equalsIgnoreCase("text/html", component.getMimeType())) {
    			checkHtmlWarningConditions(component.getEmmBlock(), messages);
    		}
    	}
    }
}
