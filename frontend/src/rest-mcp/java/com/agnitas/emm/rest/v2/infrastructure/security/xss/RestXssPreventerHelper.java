/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2.infrastructure.security.xss;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import com.agnitas.beans.Admin;
import com.agnitas.emm.rest.v2.infrastructure.security.filter.CachedBodyHttpServletRequest;
import com.agnitas.emm.util.html.xssprevention.HtmlCheckError;
import com.agnitas.emm.util.html.xssprevention.XSSHtmlException;
import com.agnitas.emm.util.html.xssprevention.http.RequestParameterXssPreventerHelper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import tools.jackson.core.JacksonException;
import tools.jackson.core.json.JsonReadFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@Component
public class RestXssPreventerHelper extends RequestParameterXssPreventerHelper {

	private static final JsonMapper JSON_MAPPER = JsonMapper.builder()
			.enable(JsonReadFeature.ALLOW_UNQUOTED_PROPERTY_NAMES)
			.build();

	public RestXssPreventerHelper() {
		super(s -> false);
	}

	public void validate(
		HttpServletRequest request, HandlerMethod method, Admin admin
	) throws IOException, XSSHtmlException {
		Set<HtmlCheckError> errors = new HashSet<>();

		errors.addAll(validateRequestParams(request, method, admin));
		errors.addAll(validateHeaders(request, method, admin));
		errors.addAll(validateBody(request, method, admin));

		if (!errors.isEmpty()) {
			throw new XSSHtmlException(errors);
		}
	}

	public Set<HtmlCheckError> validateRequestParams(HttpServletRequest request, HandlerMethod method, Admin admin) {
		this.isParameterExcludedFromCheck = paramName -> isExcluded(paramName, XssCheckLocation.PARAM, method, admin);
		return super.validateRequestParameters(request);
	}

	public Set<HtmlCheckError> validateHeaders(HttpServletRequest request, HandlerMethod method, Admin admin) {
		Set<HtmlCheckError> errors = new HashSet<>();
		Enumeration<String> headerNames = request.getHeaderNames();
		while (headerNames != null && headerNames.hasMoreElements()) {
			String headerName = headerNames.nextElement();
			String value = request.getHeader(headerName);
			if (isNotBlank(value) && !isExcluded(headerName, XssCheckLocation.HEADER, method, admin)) {
				errors.addAll(getHtmlCheckErrors(value));
			}
		}
		return errors;
	}

	public Set<HtmlCheckError> validateBody(HttpServletRequest request, HandlerMethod method, Admin admin) throws IOException {
		CachedBodyHttpServletRequest cached = request instanceof CachedBodyHttpServletRequest cachedReq
			? cachedReq
			: new CachedBodyHttpServletRequest(request);

		Set<HtmlCheckError> errors = new HashSet<>();
		try {
			checkJson(errors, JSON_MAPPER.readTree(
				cached.getCachedBodyStr()),
				"",
				field -> isExcluded(field, XssCheckLocation.BODY_FIELD, method, admin)
			);
		} catch (JacksonException ex) {
			throw new HttpMessageNotReadableException(
					"Invalid JSON format or structure.",
					ex,
					new ServletServerHttpRequest(cached)
			);
		}
		return errors;
	}

	private void checkJson(
		Set<HtmlCheckError> errors,
		JsonNode node,
		String path,
		Predicate<String> isParameterExcludedFromCheck
	) {
		if (node.isValueNode()) {
			if (!isParameterExcludedFromCheck.test(path)) {
				errors.addAll(getHtmlCheckErrors(node.asString("")));
			}
		} else if (node.isObject()) {
			for (Map.Entry<String, JsonNode> field : node.properties()) {
				String childPath = (path == null || path.isEmpty())
						? field.getKey()
						: path + "." + field.getKey();
				checkJson(errors, field.getValue(), childPath, isParameterExcludedFromCheck);
			}
		} else if (node.isArray()) {
			int index = 0;
			for (JsonNode child : node) {
				String childPath = path + "[" + index + "]";
				checkJson(errors, child, childPath, isParameterExcludedFromCheck);
				index++;
			}
		}
	}

	private static boolean isExcluded(String name, XssCheckLocation location, HandlerMethod method, Admin admin) {
		XssExclude annotation = method.getMethodAnnotation(XssExclude.class);
		if (annotation == null) {
			return false;
		}
		if (annotation.checkMethod().isEmpty()) {
			throw new IllegalStateException("Check method(name, location, admin) is not specified.");
		}
		try {
			return (boolean) method
				.getBeanType()
				.getMethod(annotation.checkMethod(), String.class, XssCheckLocation.class, Admin.class)
				.invoke(method.getBean(), name, location, admin);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}
}
