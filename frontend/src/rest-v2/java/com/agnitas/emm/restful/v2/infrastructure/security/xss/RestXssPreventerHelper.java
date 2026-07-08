/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.restful.v2.infrastructure.security.xss;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import com.agnitas.beans.Admin;
import com.agnitas.emm.restful.v2.infrastructure.security.filter.CachedBodyHttpServletRequest;
import com.agnitas.emm.util.html.xssprevention.HtmlCheckError;
import com.agnitas.emm.util.html.xssprevention.XSSHtmlException;
import com.agnitas.emm.util.html.xssprevention.http.RequestParameterXssPreventerHelper;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;

@Component
public class RestXssPreventerHelper extends RequestParameterXssPreventerHelper {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
		.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);

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
		checkJson(errors, OBJECT_MAPPER.readTree(
			cached.getCachedBodyStr()),
			"",
			field -> isExcluded(field, XssCheckLocation.BODY_FIELD, method, admin)
		);
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
				errors.addAll(getHtmlCheckErrors(node.asText()));
			}
		} else if (node.isObject()) {
			Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
			while (fields.hasNext()) {
				Map.Entry<String, JsonNode> field = fields.next();
				String childPath = (path == null || path.isEmpty()) ? field.getKey() : path + "." + field.getKey();
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
