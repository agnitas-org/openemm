/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.restful;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import com.agnitas.emm.core.useractivitylog.dao.RestfulUserActivityLogDao;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.HttpUtils.RequestMethod;
import org.apache.commons.lang3.StringUtils;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.Permission;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface RestfulServiceHandler {
	ResponseType getResponseType();
	void doService(HttpServletRequest request, HttpServletResponse response, Admin admin, byte[] requestData, File requestDataTempFile, BaseRequestResponse restfulResponse, ServletContext context, RequestMethod requestMethod, boolean extendedLogging) throws Exception;
	RestfulServiceHandler redirectServiceHandlerIfNeeded(ServletContext context, HttpServletRequest request, String restfulSubInterfaceName) throws Exception;
	static String[] getRestfulContext(HttpServletRequest request, String namespace, int minimumItems, int maximumItems) throws Exception {
		String requestUri = request.getRequestURI();
		if (StringUtils.isBlank(requestUri)) {
			throw new RestfulClientException("Invalid request");
		}
		if (requestUri.contains("?")) {
			requestUri = requestUri.substring(0, requestUri.indexOf("?"));
		}
		requestUri = requestUri.trim();
		if (requestUri.endsWith("/")) {
			throw new RestfulClientException("Invalid request");
		}
		String[] uriParts = requestUri.split("/");
		
		int namespaceIndex = -1;
		for (int i = uriParts.length - 1; i >= 0; i--) {
			if (namespace.equals(uriParts[i])) {
				namespaceIndex = i;
				break;
			}
		}
		
		if (namespaceIndex < 0) {
			throw new RestfulClientException("Invalid request");
		} else {
			String[] restfulContext = new String[uriParts.length - (namespaceIndex + 1)];
			for (int i = 0; i < restfulContext.length; i++) {
				restfulContext[i] = AgnUtils.decodeURL(uriParts[namespaceIndex + 1 + i]);
			}
			if (restfulContext.length < minimumItems || restfulContext.length > maximumItems) {
				throw new RestfulClientException("Invalid request");
			} else {
				return restfulContext;
			}
		}
	}
	
	static InputStream getRequestDataStream(byte[] requestData, File requestDataFile) throws FileNotFoundException {
		if (requestData != null) {
			return new ByteArrayInputStream(requestData);
		} else {
			return new FileInputStream(requestDataFile);
		}
	}
	
	static void checkPermission(Admin admin, Permission permission) throws RestfulClientException {
		if (!admin.permissionAllowed(Permission.WEBSERVICE_USER_CREATE)) {
			throw new RestfulClientException("Authorization failed: Access denied '" + Permission.WEBSERVICE_USER_CREATE.toString() + "'");
		}
	}

	default void writeActivityLog(RestfulUserActivityLogDao activityLogDao, String description, HttpServletRequest request, Admin admin) {
		String host = request.getHeader("Host");
		if (StringUtils.isBlank(host)) {
			host = AgnUtils.getHostName();
		}
		activityLogDao.writeUserActivityLog(request.getRequestURI(), description, request.getMethod(), host, admin);
	}
}
