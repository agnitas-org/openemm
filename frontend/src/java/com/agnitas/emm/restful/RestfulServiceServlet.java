/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.restful;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.agnitas.util.AgnUtils;
import org.agnitas.util.HttpUtils;
import org.agnitas.util.HttpUtils.RequestMethod;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.agnitas.beans.ComAdmin;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.logon.service.ComLogonService;
import com.agnitas.emm.core.logon.web.LogonFailedException;

/**
 * Restful services are available at:
 * https://<system.url>/restful/*
 * 
 * Test call via wget with auth parameter:
 * > rm test.txt; wget -S -O - --content-on-error "http://localhost:8080/restful/reference/TestTbl?username=<UrlencodedUserName>&password=<UrlencodedPassword>" > test.txt; cat test.txt; echo
 * Test call via wget with BASIC AUTH:
 * > rm test.txt; wget -S -O - --content-on-error --auth-no-challenge --http-user="<UrlencodedUserName>" --http-password="<UrlencodedPassword>" "http://localhost:8080/restful/reference/TestTbl" > test.txt; cat test.txt; echo
 */
public class RestfulServiceServlet extends BaseRequestServlet {
	private static final long serialVersionUID = -126080706211654654L;

	private static final transient Logger logger = Logger.getLogger(RestfulServiceServlet.class);

	/**
	 * Do not use directly. Use getComLogonService() instead
	 */
	private ComLogonService logonService;
	
	@Required
	public void setLogonService(ComLogonService logonService) {
		this.logonService = logonService;
	}
	
	@Override
	protected void doService(HttpServletRequest request, HttpServletResponse response, RequestMethod requestMethod) throws Exception {
		BaseRequestResponse restfulResponse = new JsonRequestResponse(); // Default response type is json
		RestfulServiceHandler serviceHandler;
		
		try {
			// Read restful interface to be called
			String restfulInterface1;
			String restfulInterface2;
			String[] uriParts = request.getRequestURI().split("/");
			int restfulIndex = -1;
			for (int i = uriParts.length - 1; i >= 0; i--) {
				if ("restful".equals(uriParts[i])) {
					restfulIndex = i;
					break;
				}
			}
			if (restfulIndex < 0) {
				throw new Exception("Invalid request");
			} else if (restfulIndex <= uriParts.length - 2) {
				restfulInterface1 = uriParts[restfulIndex + 1];
				if (restfulIndex <= uriParts.length - 3) {
					restfulInterface2 = uriParts[restfulIndex + 2];
				} else {
					restfulInterface2 = null;
				}
			} else {
				throw new RestfulClientException("Invalid request");
			}
			
			if ("openapi".equalsIgnoreCase(restfulInterface1)) {
				File openApiFile = new File(getServletContext().getRealPath("/assets/emm_restful_openapi.json"));
				try (InputStream inputStream = new FileInputStream(openApiFile)) {
					response.setStatus(HttpServletResponse.SC_OK);
					response.setContentType("application/json");
					response.setCharacterEncoding("UTF-8");
					IOUtils.copy(inputStream, response.getOutputStream());
				}
				return;
			} else {
				RestfulServiceHandler restfulServiceHandler;
				try {
					restfulServiceHandler = WebApplicationContextUtils.getWebApplicationContext(getServletContext()).getBean("RestfulServiceHandler_" + restfulInterface1, RestfulServiceHandler.class);
				} catch (Exception e) {
					throw new RestfulClientException("No such service: " + restfulInterface1, e);
				}
				
				if (restfulServiceHandler != null) {
					serviceHandler = restfulServiceHandler.redirectServiceHandlerIfNeeded(getServletContext(), request, restfulInterface2);
				} else {
					logger.error("Invalid restful interface: " + request.getRequestURI());
					throw new RestfulClientException("Invalid restful interface: " + request.getRequestURI());
				}
				
				 // Default response type is json, which the response object was already set before. SO change it here if the servicehandler needs a different response type
				if (serviceHandler.getResponseType() == ResponseType.XML) {
					restfulResponse = new XmlRequestResponse();
				}
				
				String basicAuthorizationUsername = HttpUtils.getBasicAuthenticationUsername(request);
				String basicAuthorizationPassword = HttpUtils.getBasicAuthenticationPassword(request);
				String username = StringUtils.isNotBlank(basicAuthorizationUsername) ? basicAuthorizationUsername : getRequestParameter(request, "username", true);
				String password = StringUtils.isNotBlank(basicAuthorizationPassword) ? basicAuthorizationPassword : getRequestParameter(request, "password", true);
				
				ComAdmin admin = null;
				
				try {
					if (StringUtils.isBlank(username)) {
						throw new RestfulAuthentificationException("Authentification failed: username is missing");
					}
	
					if (StringUtils.isBlank(password)) {
						throw new RestfulAuthentificationException("Authentification failed: password is missing");
					}
					
					// Check authentication
					try {
						admin = getComLogonService().getAdminByCredentials(username, password, request.getRemoteAddr());
					} catch (LogonFailedException e) {
						throw new RestfulAuthentificationException("Authentication failed");
					}
					if (admin == null) {
						throw new RestfulAuthentificationException("Authentication failed");
					} else {
						AgnUtils.setAdmin(request, admin);
					}
				} catch (RestfulAuthentificationException e) {
					// do not show full stacktrace, because of possible log flooding
					logger.error("Authentication error (User: " + username + "): " + e.getMessage());
					restfulResponse.setAuthentificationError(new Exception("Authentication failed", e), ErrorCode.USER_AUTHENTICATION_ERROR);
					writeResponse(response, restfulResponse);
					return;
				} catch (Exception e) {
					logger.error("Authentication error (User: " + username + "): " + e.getMessage(), e);
					restfulResponse.setError(new Exception("Authentication failed", e), ErrorCode.USER_AUTHENTICATION_ERROR);
					writeResponse(response, restfulResponse);
					return;
				}
				
				try {
					// Check authorization
					if (!admin.permissionAllowed(Permission.RESTFUL_ALLOWED)) {
						throw new RestfulClientException("User not authorized for: " + Permission.RESTFUL_ALLOWED.toString());
					}
				} catch (RestfulClientException e) {
					restfulResponse.setClientError(new Exception("Authorization failed: " + e.getMessage(), e), ErrorCode.USER_AUTHORIZATION_ERROR);
					writeResponse(response, restfulResponse);
					return;
				} catch (Exception e) {
					restfulResponse.setError(new Exception("Authorization failed: " + e.getMessage(), e), ErrorCode.USER_AUTHORIZATION_ERROR);
					writeResponse(response, restfulResponse);
					return;
				}
				
				serviceHandler.doService(request, response, admin, getRequestDataTempFile(request), restfulResponse, getServletContext(), requestMethod);
				writeResponse(response, restfulResponse);
			}
		} catch (RestfulNoDataFoundException e) {
			restfulResponse.setNoDataFoundError(e);
			writeResponse(response, restfulResponse);
		} catch (RestfulClientException e) {
			restfulResponse.setClientError(e);
			writeResponse(response, restfulResponse);
		} catch (Exception e) {
			restfulResponse.setError(e);
			writeResponse(response, restfulResponse);
		}
	}

	private void writeResponse(HttpServletResponse response, BaseRequestResponse restfulResponse) throws Exception {
		if (restfulResponse.responseState == State.AUTHENTIFICATION_ERROR) {
			response.addHeader("WWW-Authenticate", "Basic realm=\"EMM Restful Services\", charset=\"UTF-8\"");
			super.writeResponse(response, HttpServletResponse.SC_UNAUTHORIZED, restfulResponse.getMimeType(), restfulResponse.getString());
		} else if (restfulResponse.responseState == State.NO_DATA_FOUND_ERROR) {
			super.writeResponse(response, HttpServletResponse.SC_NOT_FOUND, restfulResponse.getMimeType(), restfulResponse.getString());
		} else if (restfulResponse.responseState == State.CLIENT_ERROR) {
			super.writeResponse(response, HttpServletResponse.SC_BAD_REQUEST, restfulResponse.getMimeType(), restfulResponse.getString());
		} else if (restfulResponse.responseState == State.ERROR) {
			super.writeResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, restfulResponse.getMimeType(), restfulResponse.getString());
		} else {
			super.writeResponse(response, HttpServletResponse.SC_OK, restfulResponse.getMimeType(), restfulResponse.getString());
		}
	}

	protected ComLogonService getComLogonService() {
		if (logonService == null) {
			ApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
			logonService = (ComLogonService) applicationContext.getBean("LogonService");
		}
		return logonService;
	}
}
