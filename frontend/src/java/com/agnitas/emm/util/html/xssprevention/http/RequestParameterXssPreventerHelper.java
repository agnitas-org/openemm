/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.util.html.xssprevention.http;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import javax.servlet.http.HttpServletRequest;

import com.agnitas.emm.util.html.xssprevention.HtmlCheckError;
import com.agnitas.emm.util.html.xssprevention.HtmlXSSPreventer;
import com.agnitas.emm.util.html.xssprevention.XSSHtmlException;

public class RequestParameterXssPreventerHelper {

	private final Function<String, Boolean> isParameterExcludedFromCheck;
	
	public RequestParameterXssPreventerHelper() {
		this(x -> false);
	}
	
	public RequestParameterXssPreventerHelper(final Function<String, Boolean> isParameterExcludedFromCheck) {
		this.isParameterExcludedFromCheck = Objects.requireNonNull(isParameterExcludedFromCheck, "Parameter function cannot be null");
	}
	
	public final Set<HtmlCheckError> validateRequestParameters(final HttpServletRequest request) {
		final Set<HtmlCheckError> htmlErrors = new HashSet<>();
		
		final Enumeration<String> parameterNames = request.getParameterNames();

		while (parameterNames.hasMoreElements()) {
			final String paramName = parameterNames.nextElement();

			if (!isParameterExcludedFromCheck.apply(paramName)) {
				getHtmlCheckErrors(paramName, request.getParameterValues(paramName), htmlErrors);
			}
		}

		return htmlErrors;
	}
	
	private final void getHtmlCheckErrors(final String paramName, final String[] textArray, final Set<HtmlCheckError> errors) {
		for(final String text : textArray) { 
			getHtmlCheckErrors(paramName, text, errors);
		}
	}
	
	private final void getHtmlCheckErrors(final String paramName, final String text, final Set<HtmlCheckError> errors) {
		try {
			HtmlXSSPreventer.checkString(text);
		} catch(final XSSHtmlException e) {
			errors.addAll(e.getErrors());
		}
	}

}
