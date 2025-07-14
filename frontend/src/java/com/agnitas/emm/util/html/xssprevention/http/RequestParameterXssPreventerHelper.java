/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.util.html.xssprevention.http;

import com.agnitas.emm.util.html.xssprevention.HtmlCheckError;
import com.agnitas.emm.util.html.xssprevention.HtmlXSSPreventer;
import com.agnitas.emm.util.html.xssprevention.XSSHtmlException;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartRequest;
import org.springframework.web.util.WebUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class RequestParameterXssPreventerHelper {

	private final Predicate<String> isParameterExcludedFromCheck;
	
	public RequestParameterXssPreventerHelper() {
		this(x -> false);
	}
	
	public RequestParameterXssPreventerHelper(final Predicate<String> isParameterExcludedFromCheck) {
		this.isParameterExcludedFromCheck = Objects.requireNonNull(isParameterExcludedFromCheck, "Parameter function cannot be null");
	}
	
	public final Set<HtmlCheckError> validateRequestParameters(final HttpServletRequest request) {
		final Set<HtmlCheckError> htmlErrors = new HashSet<>();
		
		final Enumeration<String> parameterNames = request.getParameterNames();

		while (parameterNames.hasMoreElements()) {
			final String paramName = parameterNames.nextElement();

			if (!isParameterExcludedFromCheck.test(paramName)) {
				htmlErrors.addAll(getHtmlCheckErrors(request.getParameterValues(paramName)));
			}
		}
        htmlErrors.addAll(collectMultipartFilesHtmlErrors(request));
        return htmlErrors;
	}
	
	private Collection<HtmlCheckError> getHtmlCheckErrors(final String[] textArray) {
	    return Arrays.stream(textArray).map(this::getHtmlCheckErrors)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
	}
	
	private Collection<HtmlCheckError> getHtmlCheckErrors(final String text) {
		try {
			HtmlXSSPreventer.checkString(StringUtils.defaultString(text));
			return Collections.emptyList();
		} catch (final XSSHtmlException e) {
			return e.getErrors();
		}
	}

    private Collection<HtmlCheckError> collectMultipartFilesHtmlErrors(HttpServletRequest request) {
		MultipartRequest multipartRequest = WebUtils.getNativeRequest(request, MultipartRequest.class);

		if (multipartRequest == null) {
			return Collections.emptyList();
		}

		return multipartRequest.getMultiFileMap()
				.toSingleValueMap()
				.values()
				.stream()
				.map(MultipartFile::getOriginalFilename)
				.map(this::getHtmlCheckErrors)
				.flatMap(Collection::stream)
				.collect(Collectors.toList());
    }
}
