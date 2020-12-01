/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.restful;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.HttpUtils.RequestMethod;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadBase.SizeLimitExceededException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.web.context.support.WebApplicationContextUtils;

public abstract class BaseRequestServlet extends HttpServlet {
	private static final long serialVersionUID = 6817178588854693746L;

	private static final transient Logger logger = Logger.getLogger(BaseRequestServlet.class);
	
	private static final String REQUEST_ATTRIBUTE_REQUEST_DATA_TEMP_FILE = "requestDataTempFile";
	private static final String REQUEST_ATTRIBUTE_PARAMETER_MAP = "parameterMap";
	
	protected static final String TEMP_FILE_DIRECTORY = AgnUtils.getTempDir() + File.separator + "ServletFileRequest";
	
	protected ConfigService configService;

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
			parseRequest(request);
			
			doService(request, response, requestMethod);
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

	public final String getRequestParameter(HttpServletRequest request, String parameterName) throws Exception {
		@SuppressWarnings("unchecked")
		Map<String, String> parameterMap = (Map<String, String>) request.getAttribute(REQUEST_ATTRIBUTE_PARAMETER_MAP);
		return parameterMap.get(parameterName);
	}

	public final String getRequestParameter(HttpServletRequest request, String parameterName, boolean ignoreCase) throws Exception {
		@SuppressWarnings("unchecked")
		Map<String, String> parameterMap = (Map<String, String>) request.getAttribute(REQUEST_ATTRIBUTE_PARAMETER_MAP);
		for (Entry<String, String> entry : parameterMap.entrySet()) {
			String key = entry.getKey();
			if (key.equals(parameterName) || (ignoreCase && key.equalsIgnoreCase(parameterName))) {
				return entry.getValue();
			}
		}
		return null;
	}

	public final Map<String, String> getRequestParameterMap(HttpServletRequest request) throws Exception {
		@SuppressWarnings("unchecked")
		Map<String, String> parameterMap = (Map<String, String>) request.getAttribute(REQUEST_ATTRIBUTE_PARAMETER_MAP);
		return parameterMap;
	}

	protected abstract void doService(HttpServletRequest servletRequest, HttpServletResponse servletResponse, RequestMethod requestMethod) throws Exception;

	protected void writeResponseData(HttpServletResponse response, byte[] responseData) throws Exception {
		try (OutputStream outputStream = response.getOutputStream()) {
			response.setContentType("application/octet-stream");
			outputStream.write(responseData);
		} catch (IOException e) {
			throw new Exception("Error while sending response", e);
		}
	}

	protected void writeResponse(HttpServletResponse response, int httpStatusCode, String responseMimeType, String responseText) throws Exception {
		response.setStatus(httpStatusCode);
		response.setContentType(responseMimeType);
		response.setCharacterEncoding("UTF-8");
		try (OutputStream outputStream = response.getOutputStream()) {
			outputStream.write(responseText.getBytes("UTF-8"));
		} catch (IOException e) {
			throw new Exception("Error while sending response", e);
		}
	}

	private void parseRequest(HttpServletRequest request) throws Exception {
		if (request.getAttribute(REQUEST_ATTRIBUTE_PARAMETER_MAP) != null) {
			throw new IllegalStateException("Error parsing request. Request was already parsed");
		}
		request.setAttribute(REQUEST_ATTRIBUTE_PARAMETER_MAP, new HashMap<>());
		if (ServletFileUpload.isMultipartContent(request)) {
			parseMultipartRequest(request);
		} else {
			parseSimpleRequest(request);
		}
	}
	
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
				if (getRequestDataTempFile(request) != null) {
					throw new IllegalStateException("Error parsing request. Multiple data entries found");
				}
				File tempFile = File.createTempFile("Request_", ".requestData", AgnUtils.createDirectory(TEMP_FILE_DIRECTORY));
				try (FileOutputStream outputStream = new FileOutputStream(tempFile)) {
					try (InputStream inputStream = part.getInputStream()) {
						IOUtils.copy(inputStream, outputStream);
					}
				}
				request.setAttribute(REQUEST_ATTRIBUTE_REQUEST_DATA_TEMP_FILE, tempFile.getAbsolutePath());
			}
		}
	}

	private void parseSimpleRequest(HttpServletRequest request) throws Exception {
		try {
			File tempFile = null;
			try (InputStream inputStream = request.getInputStream()) {
				if (inputStream != null) {
					tempFile = File.createTempFile("Request_", ".requestData", AgnUtils.createDirectory(TEMP_FILE_DIRECTORY));
					try (FileOutputStream outputStream = new FileOutputStream(tempFile)) {
						IOUtils.copy(inputStream, outputStream);
					}
					if (tempFile.length() == 0) {
						tempFile.delete();
						tempFile = null;
					}
				}
			}
			if (tempFile != null) {
				request.setAttribute(REQUEST_ATTRIBUTE_REQUEST_DATA_TEMP_FILE, tempFile.getAbsolutePath());
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
					if (!tempFile.exists()) {
						throw new Exception("Error while reading request data");
					} else if (tempFile.length() < 0 || tempFile.length() > getConfigService().getIntegerValue(ConfigValue.MaxRestfulRequestDataSize)) {
						throw new SizeLimitExceededException("Error while reading request data: Invalid request data size", tempFile.length(), getConfigService().getIntegerValue(ConfigValue.MaxRestfulRequestDataSize));
					} else {
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
				}
			} catch (SizeLimitExceededException e) {
				throw e;
			} catch (IOException e) {
				throw new Exception("Error while reading request data: " + e.getMessage(), e);
			}
		}
	}

	protected String getRequestDataTempFile(HttpServletRequest request) {
		return (String) request.getAttribute(REQUEST_ATTRIBUTE_REQUEST_DATA_TEMP_FILE);
	}
	
	protected static String getBasicAuthenticationUsername(HttpServletRequest request) {
		try {
			String basicAuthorizationHeader = request.getHeader("Authorization"); // like: "Basic bXl1c2VybmFtZTpteXBhc3N3b3Jk"
			if (StringUtils.isBlank(basicAuthorizationHeader) || !basicAuthorizationHeader.startsWith("Basic ")) {
				return null;
			} else {
				String decodedAuthorization = new String(AgnUtils.decodeBase64(basicAuthorizationHeader.substring(6)), "UTF-8"); // like: "myusername:mypassword"
				if (decodedAuthorization.contains(":")) {
					return decodedAuthorization.substring(0, decodedAuthorization.indexOf(":"));
				} else {
					return decodedAuthorization;
				}
			}
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}
	
	protected static String getBasicAuthenticationPassword(HttpServletRequest request) {
		try {
			String basicAuthorizationHeader = request.getHeader("Authorization"); // like: "Basic bXl1c2VybmFtZTpteXBhc3N3b3Jk"
			if (StringUtils.isBlank(basicAuthorizationHeader) || !basicAuthorizationHeader.startsWith("Basic ")) {
				return null;
			} else {
				String decodedAuthorization = new String(AgnUtils.decodeBase64(basicAuthorizationHeader.substring(6)), "UTF-8"); // like: "myusername:mypassword"
				if (decodedAuthorization.contains(":")) {
					return decodedAuthorization.substring(decodedAuthorization.indexOf(":") + 1);
				} else {
					return null;
				}
			}
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}

    private ConfigService getConfigService() {
		if (configService == null) {
			configService = (ConfigService) WebApplicationContextUtils.getWebApplicationContext(getServletContext()).getBean("ConfigService");
		}
		return configService;
	}
}
