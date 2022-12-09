/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.restful;

import java.io.ByteArrayOutputStream;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.context.support.WebApplicationContextUtils;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public abstract class BaseRequestServlet extends HttpServlet {
	private static final int REQUEST_DATA_KEEP_IN_MEMORY_LIMIT = 8192;

	private static final long serialVersionUID = 6817178588854693746L;

	private static final transient Logger logger = LogManager.getLogger(BaseRequestServlet.class);

	private static final String REQUEST_ATTRIBUTE_REQUEST_WAS_MULTIPART = "wasMultiPart";
	private static final String REQUEST_ATTRIBUTE_REQUEST_DATA_IN_MEMORY = "requestData";
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
		if (responseText != null) {
			try (OutputStream outputStream = response.getOutputStream()) {
				outputStream.write(responseText.getBytes("UTF-8"));
			} catch (IOException e) {
				throw new Exception("Error while sending response", e);
			}
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

	private void parseSimpleRequest(HttpServletRequest request) throws Exception {
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
					File tempFile = File.createTempFile("Request_", ".requestData", AgnUtils.createDirectory(TEMP_FILE_DIRECTORY));
					try (FileOutputStream outputStream = new FileOutputStream(tempFile)) {
						outputStream.write(data);
						IOUtils.copy(inputStream, outputStream);
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

    private ConfigService getConfigService() {
		if (configService == null) {
			configService = (ConfigService) WebApplicationContextUtils.getWebApplicationContext(getServletContext()).getBean("ConfigService");
		}
		return configService;
	}
}
