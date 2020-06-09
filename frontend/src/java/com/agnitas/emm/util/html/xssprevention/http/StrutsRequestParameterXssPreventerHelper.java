/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.util.html.xssprevention.http;

import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import javax.servlet.http.HttpServletRequest;

import com.agnitas.emm.util.html.xssprevention.HtmlCheckError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMessages;

public class StrutsRequestParameterXssPreventerHelper extends RequestParameterXssPreventerHelper {

	private final HtmlCheckErrorToActionErrorMapper errorMapper;
	
	public StrutsRequestParameterXssPreventerHelper() {
		this(x -> false, new DefaultHtmlCheckErrorToActionErrorsMapper());
	}
	
	public StrutsRequestParameterXssPreventerHelper(final Function<String, Boolean> isParameterExcludedFromCheck, final HtmlCheckErrorToActionErrorMapper errorMapper) {
		super(isParameterExcludedFromCheck);
		this.errorMapper = Objects.requireNonNull(errorMapper, "Error mapper cannot be null");
	}

	public final ActionErrors validateRequestParametersAndMapToActionMessages(final HttpServletRequest request, final Function<String, Boolean> isParameterExcludedFromCheck) {
		return validateRequestParametersAndMapToActionMessages(request, isParameterExcludedFromCheck, null);
	}
	
	public final ActionErrors validateRequestParametersAndMapToActionMessages(final HttpServletRequest request, final Function<String, Boolean> isParameterExcludedFromCheck, final ActionErrors messagesOrNull) {
		final Set<HtmlCheckError> errors = this.validateRequestParameters(request);
		
		if(!errors.isEmpty()) {
			final ActionErrors messages = messagesOrNull != null ? messagesOrNull : new ActionErrors();

			for(final HtmlCheckError error : errors) {
				messages.add(ActionMessages.GLOBAL_MESSAGE, this.errorMapper.mapToActionError(error));
			}
			
			return messages;
		} else {
			return messagesOrNull;
		}
	}

}
