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
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.DateUtilities;
import org.agnitas.util.HttpUtils;
import org.agnitas.util.HttpUtils.RequestMethod;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.logon.service.ComLogonService;
import com.agnitas.emm.core.logon.web.LogonFailedException;
import com.agnitas.emm.util.quota.api.QuotaLimitExceededException;
import com.agnitas.emm.util.quota.api.QuotaService;
import com.agnitas.emm.util.quota.api.QuotaServiceException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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
	
	/** Serial version UID. */
	private static final long serialVersionUID = -126080706211654654L;

	/** The logge.r */
	private static final transient Logger logger = LogManager.getLogger(RestfulServiceServlet.class);

	/**
	 * Do not use directly. Use getComLogonService() instead
	 */
	private ComLogonService logonService;
	
	private QuotaService quotaService;
	
	/**
	 * Sets logon service.
	 * 
	 * @param logonService logon service
	 */
	@Required
	public void setLogonService(ComLogonService logonService) {
		this.logonService = logonService;
	}
	
	@Override
	protected void doService(HttpServletRequest request, HttpServletResponse response, RequestMethod requestMethod) throws Exception {
		BaseRequestResponse restfulResponse = new JsonRequestResponse(); // Default response type is json
		RestfulServiceHandler serviceHandler;

		Admin admin = null;
		
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
				String basicAuthorizationUsername = HttpUtils.getBasicAuthenticationUsername(request);
				String basicAuthorizationPassword = HttpUtils.getBasicAuthenticationPassword(request);
				String username = StringUtils.isNotBlank(basicAuthorizationUsername) ? basicAuthorizationUsername : getRequestParameter(request, "username", true);
				String password = StringUtils.isNotBlank(basicAuthorizationPassword) ? basicAuthorizationPassword : getRequestParameter(request, "password", true);
				
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
					if (!admin.isRestful()) {
						throw new RestfulClientException("User not authorized for Restful webservices");
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

				if ("profilefield".equals(restfulInterface1) && !admin.permissionAllowed(Permission.PROFILEFIELD_MIGRATION)) {
					restfulInterface1 = "profilefield_old";
				}

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

				// Check quotas
				checkQuotas(admin, serviceHandler.getClass());
				
				boolean extendedLogging = configService.getBooleanValue(ConfigValue.ExtendedRestfulLogging, admin.getCompanyID());
				if (extendedLogging && getRequestData(request) != null) {
					// Store requestData in a temp file for logging purpose, even if it was small enough to be kept it in memory only
					File tempFile = File.createTempFile("Request_", wasMultiPartRequest(request) ? ".multipart.requestData": ".requestData", AgnUtils.createDirectory(TEMP_FILE_DIRECTORY));
					try (FileOutputStream outputStream = new FileOutputStream(tempFile)) {
						try (ByteArrayInputStream inputStream = new ByteArrayInputStream(getRequestData(request))) {
							IOUtils.copy(inputStream, outputStream);
						}
					}
				}
				serviceHandler.doService(request, response, admin, getRequestData(request), getRequestDataTempFile(request) == null ? null : new File(getRequestDataTempFile(request)), restfulResponse, getServletContext(), requestMethod, extendedLogging);
				writeResponse(response, restfulResponse);
			}
		} catch (RestfulNoDataFoundException e) {
			// do not write an errorlogfile
			restfulResponse.setNoDataFoundError(e);
			writeResponse(response, restfulResponse);
		} catch (RestfulClientException e) {
			writeErrorLogFile(request, requestMethod, admin, e);
			restfulResponse.setClientError(e);
			writeResponse(response, restfulResponse);
		} catch (QuotaLimitExceededException e) {
			// do not write an errorlogfile
			restfulResponse.setClientError(e, ErrorCode.MAX_LOAD_EXCEED_ERROR);
			writeResponse(response, restfulResponse);
		} catch (Exception e) {
			writeErrorLogFile(request, requestMethod, admin, e);
			restfulResponse.setError(e);
			writeResponse(response, restfulResponse);
			logger.error("Error in Restful handler", e);
		} finally {
			if (request.getSession() != null) {
				request.getSession().invalidate();
			}
		}
	}

	private void writeErrorLogFile(HttpServletRequest request, RequestMethod requestMethod, Admin admin, Exception errorToLog) {
		try {
			if (admin != null) {
				final File companyLogfileDirectory = new File(AgnUtils.getTempDir() + File.separator + "Restful" + File.separator + admin.getCompanyID() + File.separator + new SimpleDateFormat(DateUtilities.YYYYMMDD).format(new Date()));
				if (!companyLogfileDirectory.exists()) {
					companyLogfileDirectory.mkdirs();
				}
				final String requestUUID = AgnUtils.generateNewUUID().toString().replace("-", "").toUpperCase();
				final String dateString = new SimpleDateFormat(DateUtilities.YYYY_MM_DD_HH_MM_SS_FORFILENAMES).format(new Date());
				final String logOutputFilePrefix = "RestfulData_" + dateString + "_" + requestUUID;
				final File tempFile = new File(companyLogfileDirectory, logOutputFilePrefix + "_error.log");
				byte[] requestData = getRequestData(request);
				String requestDataFile = getRequestDataTempFile(request);
				if ((requestData != null && requestData.length > 0)) {
					try (FileOutputStream out = new FileOutputStream(tempFile)) {
						out.write(("Username: " + admin.getUsername() + "\n").getBytes("UTF-8"));
						out.write(("RequestURI: " + request.getRequestURI() + "\n").getBytes("UTF-8"));
						out.write(("RequestMethod: " + requestMethod.name() + "\n").getBytes("UTF-8"));
						out.write(("ErrorMessage: " + errorToLog.getMessage() + "\n").getBytes(StandardCharsets.UTF_8));
						out.write(("RequestData:\n").getBytes("UTF-8"));
						out.write(requestData);
					}
				} else if (requestDataFile != null) {
					try (FileOutputStream out = new FileOutputStream(tempFile);
							FileInputStream in = new FileInputStream(requestDataFile)) {
						out.write(("Username: " + admin.getUsername() + "\n").getBytes("UTF-8"));
						out.write(("RequestURI: " + request.getRequestURI() + "\n").getBytes("UTF-8"));
						out.write(("RequestMethod: " + requestMethod.name() + "\n").getBytes("UTF-8"));
						out.write(("ErrorMessage: " + errorToLog.getMessage() + "\n").getBytes(StandardCharsets.UTF_8));
						out.write(("RequestData:\n").getBytes("UTF-8"));
						IOUtils.copy(in, out);
					}
				} else {
					try (FileOutputStream out = new FileOutputStream(tempFile)) {
						out.write(("Username: " + admin.getUsername() + "\n").getBytes("UTF-8"));
						out.write(("RequestURI: " + request.getRequestURI() + "\n").getBytes("UTF-8"));
						out.write(("RequestMethod: " + requestMethod.name() + "\n").getBytes("UTF-8"));
						out.write(("ErrorMessage: " + errorToLog.getMessage() + "\n").getBytes(StandardCharsets.UTF_8));
						out.write(("RequestData: <empty>\n").getBytes("UTF-8"));
					}
				}
			}
		} catch (Exception e) {
			logger.error("Cannot write errorlogfile: " + e.getMessage(), e);
		}
	}
	
	private final void checkQuotas(final Admin admin, final Class<? extends RestfulServiceHandler> serviceHandlerClass) throws QuotaLimitExceededException, QuotaServiceException {
		if(getConfigService().getBooleanValue(ConfigValue.EnableRestfulQuotas, admin.getCompanyID())) {
			final String serviceHandlerName = classToServiceName(serviceHandlerClass);
			
			getQuotaService().checkAndTrack(admin.getUsername(), admin.getCompanyID(), serviceHandlerName);
		}
	}
	
	private static final String classToServiceName(final Class<? extends RestfulServiceHandler> clazz) {
		final String suffix = "RestfulServiceHandler";
		final String name = clazz.getSimpleName();
		
		return name.endsWith(suffix)
				? name.substring(0, name.length() - suffix.length())
				: name;
	}

	private void writeResponse(HttpServletResponse response, BaseRequestResponse restfulResponse) throws Exception {
		if (restfulResponse.responseState == State.AUTHENTIFICATION_ERROR) {
			response.addHeader("WWW-Authenticate", "Basic realm=\"EMM Restful Services\", charset=\"UTF-8\"");
			super.writeResponse(response, HttpServletResponse.SC_UNAUTHORIZED, restfulResponse.getMimeType(), restfulResponse.getString());
		} else if (restfulResponse.responseState == State.NO_DATA_FOUND_ERROR) {
			super.writeResponse(response, HttpServletResponse.SC_NOT_FOUND, restfulResponse.getMimeType(), restfulResponse.getString());
		} else if (restfulResponse.responseState == State.CLIENT_ERROR) {
			if (restfulResponse.errorCode == ErrorCode.MAX_LOAD_EXCEED_ERROR) {
				super.writeResponse(response, 429, restfulResponse.getMimeType(), restfulResponse.getString());
			} else {
				super.writeResponse(response, HttpServletResponse.SC_BAD_REQUEST, restfulResponse.getMimeType(), restfulResponse.getString());
			}
		} else if (restfulResponse.responseState == State.ERROR) {
			super.writeResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, restfulResponse.getMimeType(), restfulResponse.getString());
		} else {
			super.writeResponse(response, HttpServletResponse.SC_OK, restfulResponse.getMimeType(), restfulResponse.getString());
		}
	}

	protected ConfigService getConfigService() {
		if (configService == null) {
			ApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
			configService = applicationContext.getBean("ConfigService", ConfigService.class);
		}
		
		return configService;
	}
	
	protected ComLogonService getComLogonService() {
		if (logonService == null) {
			ApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
			logonService = applicationContext.getBean("LogonService", ComLogonService.class);
		}
		return logonService;
	}
	
	protected QuotaService getQuotaService() {
		if(this.quotaService == null) {
			this.quotaService = WebApplicationContextUtils.getWebApplicationContext(getServletContext()).getBean("RestfulQuotaService", QuotaService.class);
		}
		
		return this.quotaService;
	}
}
