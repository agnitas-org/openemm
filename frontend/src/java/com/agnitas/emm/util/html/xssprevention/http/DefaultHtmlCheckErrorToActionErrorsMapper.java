/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.util.html.xssprevention.http;

import org.apache.struts.action.ActionMessage;

import com.agnitas.emm.util.html.xssprevention.AbstractTagError;
import com.agnitas.emm.util.html.xssprevention.ForbiddenTagAttributeError;
import com.agnitas.emm.util.html.xssprevention.ForbiddenTagError;
import com.agnitas.emm.util.html.xssprevention.HtmlCheckError;
import com.agnitas.emm.util.html.xssprevention.UnclosedTagError;
import com.agnitas.emm.util.html.xssprevention.UnopenedTagError;

public class DefaultHtmlCheckErrorToActionErrorsMapper implements HtmlCheckErrorToActionErrorMapper {

	@Override
	public ActionMessage mapToActionError(final HtmlCheckError error) {
		if(error instanceof AbstractTagError) {
			if(error instanceof ForbiddenTagError) {
				return new ActionMessage("GWUA.error.html.forbiddenTag", ((ForbiddenTagError) error).getTagName());
			} else if(error instanceof UnopenedTagError) {
				return new ActionMessage("error.html.missingStartTag", ((UnopenedTagError) error).getTagName());
			} else if(error instanceof UnclosedTagError) {
				return new ActionMessage("error.html.missingEndTag", ((UnclosedTagError) error).getTagName());
			} else if(error instanceof ForbiddenTagAttributeError) {
				return new ActionMessage("error.html.forbiddenAttribute", ((ForbiddenTagAttributeError) error).getTagName(), ((ForbiddenTagAttributeError) error).getAttributeName());
			} else {
				return unhandledTagError((AbstractTagError) error);
			}
		} else {
			return unhandledError(error);
		}
	}
	
	public ActionMessage unhandledTagError(final AbstractTagError error) {
		return new ActionMessage("error.html.genericTagError", ((ForbiddenTagAttributeError) error).getTagName(), ((ForbiddenTagAttributeError) error).getAttributeName());
	}
	
	public ActionMessage unhandledError(final HtmlCheckError error) {
		return new ActionMessage("error.html.genericError");
	}
}
