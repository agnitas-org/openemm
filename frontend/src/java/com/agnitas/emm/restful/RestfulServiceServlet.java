/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.restful;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.DateUtilities;
import org.agnitas.util.HttpUtils;
import org.agnitas.util.HttpUtils.RequestMethod;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadBase.SizeLimitExceededException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.logon.service.ComLogonService;
import com.agnitas.emm.core.logon.web.LogonFailedException;
import com.agnitas.emm.util.quota.api.QuotaLimitExceededException;
import com.agnitas.emm.util.quota.api.QuotaService;
import com.agnitas.emm.util.quota.api.QuotaServiceException;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
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
public class RestfulServiceServlet extends HttpServlet {
	private static final int REQUEST_DATA_KEEP_IN_MEMORY_LIMIT = 8192;
	
	/** Serial version UID. */
	private static final long serialVersionUID = -126080706211654654L;

	/** The logge.r */
	private static final transient Logger logger = LogManager.getLogger(RestfulServiceServlet.class);

	private static final String REQUEST_ATTRIBUTE_REQUEST_WAS_MULTIPART = "wasMultiPart";
	private static final String REQUEST_ATTRIBUTE_REQUEST_DATA_IN_MEMORY = "requestData";
	private static final String REQUEST_ATTRIBUTE_REQUEST_DATA_TEMP_FILE = "requestDataTempFile";
	private static final String REQUEST_ATTRIBUTE_PARAMETER_MAP = "parameterMap";
	
	protected static final String TEMP_FILE_DIRECTORY = AgnUtils.getTempDir() + File.separator + "ServletFileRequest";
	
	protected ConfigService configService;

	/**
	 * Do not use directly. Use getComLogonService() instead
	 */
	private ComLogonService logonService;

	/**
	 * Do not use directly. Use getQuotaService() instead
	 */
	private QuotaService quotaService;

	/**
	 * Sets configService for testing
	 */
	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}
	
	/**
	 * Sets logon service for testing
	 */
	public void setLogonService(ComLogonService logonService) {
		this.logonService = logonService;
	}

	/**
	 * Sets quotaService for testing
	 */
	public void setQuotaService(QuotaService quotaService) {
		this.quotaService = quotaService;
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		processRequest(request, response, RequestMethod.GET);
	}
	
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		processRequest(request, response, RequestMethod.POST);
	}
	
	@Override
	public void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		processRequest(request, response, RequestMethod.DELETE);
	}
	
	@Override
	public void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		processRequest(request, response, RequestMethod.PUT);
	}

	private void processRequest(HttpServletRequest request, HttpServletResponse response, RequestMethod requestMethod) throws ServletException {
		try {
			Admin admin = null;
			
			// Default response type is json
			BaseRequestResponse restfulResponse = new JsonRequestResponse();
			
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
					if (request.getAttribute(REQUEST_ATTRIBUTE_PARAMETER_MAP) != null) {
						throw new IllegalStateException("Error parsing request. Request was already parsed");
					}
					request.setAttribute(REQUEST_ATTRIBUTE_PARAMETER_MAP, new HashMap<>());
					if (ServletFileUpload.isMultipartContent(request)) {
						parseMultipartRequest(request);
						admin = authenticateUser(request, response, restfulResponse);
						if (admin == null) {
							return;
						}
					} else {
						admin = authenticateUser(request, response, restfulResponse);
						if (admin == null) {
							return;
						}
						parseSimpleRequest(request, admin.getCompanyID());
					}

					RestfulServiceHandler restfulServiceHandler;
					try {
						restfulServiceHandler = WebApplicationContextUtils.getWebApplicationContext(getServletContext()).getBean("RestfulServiceHandler_" + restfulInterface1, RestfulServiceHandler.class);
					} catch (Exception e) {
						throw new RestfulClientException("No such service: " + restfulInterface1, e);
					}

					RestfulServiceHandler serviceHandler;
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
				if (request.getSession(false) != null) {
					request.getSession(false).invalidate();
				}
			}
		} catch (Throwable e) {
			logger.error("", e);
			
			try (PrintWriter responseWriter = response.getWriter()) {
				if (e instanceof SizeLimitExceededException) {
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					responseWriter.write("Error: filesize too large");
				} else {
					response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
					responseWriter.write("Error: Internal error");
				}
			} catch (Exception ex) {
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				throw new ServletException("ErrorException: " + e.getMessage() + "\n caused other Exception: " + ex.getMessage());
			}
		}
	}

	private Admin authenticateUser(HttpServletRequest request, HttpServletResponse response, BaseRequestResponse restfulResponse) throws Exception {
		Admin admin;
		String authorizationToken = HttpUtils.getAuthorizationToken(request);
		if (authorizationToken != null) {
			String username = "<undefined>";
			try {
				DecodedJWT jwtToken;
				try {
					jwtToken = JWT.decode(authorizationToken);
					username = jwtToken.getClaim("username").asString();
				} catch (Exception e) {
					throw new RestfulAuthentificationException("Authentification by JWT authorization token failed", e);
				}
				
				// HMAC512
				if (!"HS512".equals(jwtToken.getAlgorithm())) {
					throw new RestfulAuthentificationException("Authentification by JWT authorization token failed, because of an unsupported signature algorithm");
				}

				try {
					try {
						admin = getComLogonService().getAdminByUsername(username);
					} catch (Exception e) {
						throw new RestfulAuthentificationException("Authentification by JWT authorization token failed", e);
					}
					if (admin == null) {
						throw new RestfulAuthentificationException("Authentification by JWT authorization token failed");
					}
					
					String restfulJwtSharedSecret = getConfigService().getValue(ConfigValue.RestfulJwtSecret, admin.getCompanyID());
					if (StringUtils.isBlank(restfulJwtSharedSecret)) {
						throw new RestfulAuthentificationException("Authentification by JWT authorization token is not supported");
					}
					
					// Throws JWTVerificationException on verification failure
					Algorithm.HMAC512(restfulJwtSharedSecret).verify(jwtToken);
					
					if (jwtToken.getExpiresAt() == null || jwtToken.getExpiresAt().before(new Date())) {
						throw new RestfulAuthentificationException("JWT authorization token has expired");
					}
					
					AgnUtils.setAdmin(request, admin);
				} catch (JWTVerificationException e) {
					throw new RestfulAuthentificationException("Authentification by JWT authorization token failed", e);
				}
			} catch (RestfulAuthentificationException e) {
				// do not show full stacktrace, because of possible log flooding
				logger.error("JWT Authentication error (User: " + username + "): " + e.getMessage());
				restfulResponse.setAuthentificationError(new Exception("JWT Authentication failed", e), ErrorCode.USER_AUTHENTICATION_ERROR);
				writeResponse(response, restfulResponse);
				return null;
			} catch (Exception e) {
				logger.error("JWT Authentication error (User: " + username + "): " + e.getMessage(), e);
				restfulResponse.setError(new Exception("JWT Authentication failed", e), ErrorCode.USER_AUTHENTICATION_ERROR);
				writeResponse(response, restfulResponse);
				return null;
			}
		} else {
			String basicAuthorizationUsername = HttpUtils.getBasicAuthenticationUsername(request);
			String basicAuthorizationPassword = HttpUtils.getBasicAuthenticationPassword(request);
			String username = StringUtils.isNotBlank(basicAuthorizationUsername) ? basicAuthorizationUsername : request.getParameter("username");
			String password = StringUtils.isNotBlank(basicAuthorizationPassword) ? basicAuthorizationPassword : request.getParameter("password");
			
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
					throw new RestfulAuthentificationException("Authentication failed", e);
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
				return null;
			} catch (Exception e) {
				logger.error("Authentication error (User: " + username + "): " + e.getMessage(), e);
				restfulResponse.setError(new Exception("Authentication failed", e), ErrorCode.USER_AUTHENTICATION_ERROR);
				writeResponse(response, restfulResponse);
				return null;
			}
		}
			
		try {
			// Check authorization
			if (!admin.isRestful()) {
				throw new RestfulClientException("User not authorized for Restful webservices");
			} else {
				return admin;
			}
		} catch (RestfulClientException e) {
			restfulResponse.setClientError(new Exception("Authorization failed: " + e.getMessage(), e), ErrorCode.USER_AUTHORIZATION_ERROR);
			writeResponse(response, restfulResponse);
			return null;
		} catch (Exception e) {
			restfulResponse.setError(new Exception("Authorization failed: " + e.getMessage(), e), ErrorCode.USER_AUTHORIZATION_ERROR);
			writeResponse(response, restfulResponse);
			return null;
		}
	}

	public final Map<String, String> getRequestParameterMap(HttpServletRequest request) {
		@SuppressWarnings("unchecked")
		Map<String, String> parameterMap = (Map<String, String>) request.getAttribute(REQUEST_ATTRIBUTE_PARAMETER_MAP);
		return parameterMap;
	}

	protected void writeResponseData(HttpServletResponse response, byte[] responseData) throws Exception {
		try (OutputStream outputStream = response.getOutputStream()) {
			response.setContentType("application/octet-stream");
			outputStream.write(responseData);
		} catch (IOException e) {
			throw new Exception("Error while sending response", e);
		}
	}
	
	private void writeResponse(HttpServletResponse response, BaseRequestResponse restfulResponse) throws Exception {
		if (restfulResponse.responseState == State.AUTHENTIFICATION_ERROR) {
			response.addHeader("WWW-Authenticate", "Basic realm=\"EMM Restful Services\", charset=\"UTF-8\"");
			writeResponse(response, HttpServletResponse.SC_UNAUTHORIZED, restfulResponse.getMimeType(), restfulResponse.getString());
		} else if (restfulResponse.responseState == State.NO_DATA_FOUND_ERROR) {
			writeResponse(response, HttpServletResponse.SC_NOT_FOUND, restfulResponse.getMimeType(), restfulResponse.getString());
		} else if (restfulResponse.responseState == State.CLIENT_ERROR) {
			if (restfulResponse.errorCode == ErrorCode.MAX_LOAD_EXCEED_ERROR) {
				writeResponse(response, 429, restfulResponse.getMimeType(), restfulResponse.getString());
			} else {
				writeResponse(response, HttpServletResponse.SC_BAD_REQUEST, restfulResponse.getMimeType(), restfulResponse.getString());
			}
		} else if (restfulResponse.responseState == State.ERROR) {
			writeResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, restfulResponse.getMimeType(), restfulResponse.getString());
		} else {
			writeResponse(response, HttpServletResponse.SC_OK, restfulResponse.getMimeType(), restfulResponse.getString());
		}
	}

	protected void writeResponse(HttpServletResponse response, int httpStatusCode, String responseMimeType, String responseText) throws Exception {
		response.setStatus(httpStatusCode);
		response.setContentType(responseMimeType);
		response.setCharacterEncoding("UTF-8");
		if (responseText != null) {
			try (OutputStream outputStream = response.getOutputStream()) {
				outputStream.write(responseText.getBytes("UTF-8"));
			} catch (IOException e) {
				throw new Exception("Error while sending response", e);
			}
		}
	}

	/**
	 * Multipart Servlet Requests from html file upload forms do not contain a username as request parameter oder header.
	 * The parameters are included within the request data, which has to be parsed for reading them.
	 * So the EMM client specific request data size limit cannot be checked while parsing the data, but the global default limit is used.
	 * 
	 * @param request
	 * @throws Exception
	 */
	private void parseMultipartRequest(HttpServletRequest request) throws Exception {
		// Parse data with Apache commons-fileupload
		ServletFileUpload uploadHelper = new ServletFileUpload(new DiskFileItemFactory());
		uploadHelper.setSizeMax(getConfigService().getIntegerValue(ConfigValue.MaxRestfulRequestDataSize));

		List<FileItem> uploadParts = uploadHelper.parseRequest(request);
		
		@SuppressWarnings("unchecked")
		Map<String, String> parameterMap = (Map<String, String>) request.getAttribute(REQUEST_ATTRIBUTE_PARAMETER_MAP);
		for (FileItem part : uploadParts) {
			if (part.isFormField()) {
				try {
					parameterMap.put(part.getFieldName(), part.getString("UTF-8"));
				} catch (UnsupportedEncodingException e) {
					throw new Exception("Error in MultiPart-Decoding", e);
				}
			} else {
				if (getRequestData(request) != null || getRequestDataTempFile(request) != null) {
					throw new IllegalStateException("Error parsing request. Multiple data entries found");
				} else {
					try {
						// Do not trust in request header "Content-Length", because it might be invalid or even missing
						try (InputStream inputStream = part.getInputStream()) {
							boolean createRequestDataTempFile = false;
							
							byte[] data = null;
							try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
								long count = 0;
								int bytesRead;
								byte[] buffer = new byte[1024];
								while ((bytesRead = inputStream.read(buffer)) != -1) {
									outputStream.write(buffer, 0, bytesRead);
									count += bytesRead;
									if (count > REQUEST_DATA_KEEP_IN_MEMORY_LIMIT) {
										createRequestDataTempFile = true;
										break;
									}
								}
								data = outputStream.toByteArray();
							}
							
							if (createRequestDataTempFile) {
								// requestData is too big to be kept in memory, so write already read data in temp file and upcoming data too
								File tempFile = File.createTempFile("Request_", ".multipart.requestData", AgnUtils.createDirectory(TEMP_FILE_DIRECTORY));
								try (FileOutputStream outputStream = new FileOutputStream(tempFile)) {
									outputStream.write(data);
									IOUtils.copy(inputStream, outputStream);
								}
								request.setAttribute(REQUEST_ATTRIBUTE_REQUEST_DATA_TEMP_FILE, tempFile.getAbsolutePath());
							} else {
								request.setAttribute(REQUEST_ATTRIBUTE_REQUEST_DATA_IN_MEMORY, data);
							}
							request.setAttribute(REQUEST_ATTRIBUTE_REQUEST_WAS_MULTIPART, true);
						}
					} catch (IOException e) {
						throw new Exception("Error while reading request data", e);
					}
				}
			}
		}
	}

	private void parseSimpleRequest(HttpServletRequest request, int companyID) throws Exception {
		try {
			// Do not trust in request header "Content-Length", because it might be invalid or even missing
			try (InputStream inputStream = request.getInputStream()) {
				boolean createRequestDataTempFile = false;
				
				byte[] data = null;
				try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
					long count = 0;
					int bytesRead;
					byte[] buffer = new byte[1024];
					while ((bytesRead = inputStream.read(buffer)) != -1) {
						outputStream.write(buffer, 0, bytesRead);
						count += bytesRead;
						if (count > REQUEST_DATA_KEEP_IN_MEMORY_LIMIT) {
							createRequestDataTempFile = true;
							break;
						}
					}
					data = outputStream.toByteArray();
				}
				
				if (createRequestDataTempFile) {
					// requestData is too big to be kept in memory, so write already read data in temp file and upcoming data too
					long maxRestfulRequestDataSize = getConfigService().getLongValue(ConfigValue.MaxRestfulRequestDataSize, companyID);
					long count = 0;
					File tempFile = File.createTempFile("Request_", ".requestData", AgnUtils.createDirectory(TEMP_FILE_DIRECTORY));
					try (FileOutputStream outputStream = new FileOutputStream(tempFile)) {
						outputStream.write(data);
						count += data.length;
						
						int bytesRead;
						byte[] buffer = new byte[8192];
						while ((bytesRead = inputStream.read(buffer)) != -1) {
							outputStream.write(buffer, 0, bytesRead);
							count += bytesRead;
							if (count > maxRestfulRequestDataSize) {
								throw new RestfulClientException("Maximum request data size exceeded. Maximum size : " + maxRestfulRequestDataSize + " bytes");
							}
						}
					}
					request.setAttribute(REQUEST_ATTRIBUTE_REQUEST_DATA_TEMP_FILE, tempFile.getAbsolutePath());
				} else {
					request.setAttribute(REQUEST_ATTRIBUTE_REQUEST_DATA_IN_MEMORY, data);
				}
			}
		} catch (IOException e) {
			throw new Exception("Error while reading request data", e);
		}
		
		Set<String> parameterNames = request.getParameterMap().keySet();
		
		if (!parameterNames.isEmpty()) {
			for (String parameterName : parameterNames) {
				// URLDecoding happens automatically
				@SuppressWarnings("unchecked")
				Map<String, String> parameterMap = (Map<String, String>) request.getAttribute(REQUEST_ATTRIBUTE_PARAMETER_MAP);
				parameterMap.put(parameterName, request.getParameter(parameterName));
			}
		} else {
			try {
				String tempFilePath = getRequestDataTempFile(request);
				if (tempFilePath != null) {
					File tempFile = new File(tempFilePath);
					String requestDataString = FileUtils.readFileToString(tempFile, "UTF-8");
					String characterString = "\\w\\d:#@%/;$()~_\\+-\\.\\\\";
					String valuePairString = "[" + characterString + "]+=[" + characterString + "]*";
					Pattern pattern = Pattern.compile("^(" + valuePairString + ")(\\&(" + valuePairString + "))*$");
					Matcher matcher = pattern.matcher(requestDataString);
	
					if (matcher.find()) {
						String[] valuePairs = requestDataString.split("&");

						@SuppressWarnings("unchecked")
						Map<String, String> parameterMap = (Map<String, String>) request.getAttribute(REQUEST_ATTRIBUTE_PARAMETER_MAP);
						
						for (String valuePair : valuePairs) {
							String[] valuePairParts = valuePair.split("=");
							if (valuePairParts != null && valuePairParts.length == 2) {
								String parameterName = AgnUtils.decodeURL(valuePairParts[0]);
								String parameterValue = AgnUtils.decodeURL(valuePairParts[1]);
								parameterMap.put(parameterName, parameterValue);
							}
						}
					}
				}
			} catch (SizeLimitExceededException e) {
				throw e;
			} catch (IOException e) {
				throw new Exception("Error while reading request data: " + e.getMessage(), e);
			}
		}
	}

	protected boolean wasMultiPartRequest(HttpServletRequest request) {
		Boolean attribute = (Boolean) request.getAttribute(REQUEST_ATTRIBUTE_REQUEST_WAS_MULTIPART);
		return attribute != null && attribute;
	}

	protected byte[] getRequestData(HttpServletRequest request) {
		return (byte[]) request.getAttribute(REQUEST_ATTRIBUTE_REQUEST_DATA_IN_MEMORY);
	}

	protected String getRequestDataTempFile(HttpServletRequest request) {
		return (String) request.getAttribute(REQUEST_ATTRIBUTE_REQUEST_DATA_TEMP_FILE);
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
		if (quotaService == null) {
			ApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
			quotaService = applicationContext.getBean("RestfulQuotaService", QuotaService.class);
		}
		
		return this.quotaService;
	}
}
