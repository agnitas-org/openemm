/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.core.commons.util.ConfigValue;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * This servlet shows manual content
 */
public class ManualServlet extends HttpServlet {

	private static final Logger logger = LogManager.getLogger(ManualServlet.class);

	/** Serial version UID. */
	private static final long serialVersionUID = -595094416663851734L;

	public static final String MANUAL_CONTEXT = "manual";
	public static final String DEFAULT_LANGUAGE = "default";
	public static final String DEFAULT_STARTPAGE = "dashboard.htm";

	private static String MANUAL_INSTALLATION_PATH = null;

	protected ConfigService configService;

	// ----------------------------------------------------------------------------------------------------------------
	// Dependency Injection

	// ----------------------------------------------------------------------------------------------------------------
	// Business Logic

	@Override
	public void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
		try {
			if (!new File(getManualInstallationPath(request)).exists()) {
				String requestUrl = request.getRequestURL().toString();
				if (requestUrl.contains("/de/")) {
					response.sendRedirect(requestUrl.substring(0, requestUrl.indexOf(MANUAL_CONTEXT)) + "assets/manual/manual_404_de.html");
				} else {
					response.sendRedirect(requestUrl.substring(0, requestUrl.indexOf(MANUAL_CONTEXT)) + "assets/manual/manual_404_en.html");
				}
			} else {
				String requestUri = request.getRequestURI();
				if (!requestUri.contains(MANUAL_CONTEXT)) {
					if (requestUri.contains(MANUAL_CONTEXT)) {
						response.sendRedirect(requestUri.substring(0, requestUri.indexOf(MANUAL_CONTEXT)) + "logon.action");
					} else {
						response.sendRedirect("/logon.action");
					}
				} else {
					String manualRequestUriPart = StringUtils.strip(requestUri.substring(requestUri.indexOf(MANUAL_CONTEXT) + MANUAL_CONTEXT.length()), "/");
					if (manualRequestUriPart.contains(";")) {
						//remove sessionid if present
						manualRequestUriPart = manualRequestUriPart.substring(0, manualRequestUriPart.indexOf(";"));
					}
					String mimeType = null;
					if (manualRequestUriPart.endsWith(".html") || manualRequestUriPart.endsWith(".htm")) {
						mimeType = "text/html";
					} else if (manualRequestUriPart.endsWith(".jpeg") || manualRequestUriPart.endsWith(".jpg")) {
						mimeType = "image/jpg";
					} else if (manualRequestUriPart.endsWith(".png")) {
						mimeType = "image/png";
					} else if (manualRequestUriPart.endsWith(".gif")) {
						mimeType = "image/gif";
					} else if (manualRequestUriPart.endsWith(".pdf")) {
						mimeType = "application/pdf";
					} else if (manualRequestUriPart.endsWith(".js")) {
						mimeType = "text/javascript";
					} else if (manualRequestUriPart.endsWith(".css")) {
						mimeType = "text/css";
					} else if (manualRequestUriPart.endsWith(".svg")) {
						mimeType = "image/svg+xml";
					} else if (manualRequestUriPart.endsWith(".ttf")) {
						mimeType = "application/font-ttf";
					} else if (manualRequestUriPart.endsWith(".woff")) {
						mimeType = "application/font-woff";
					} else if (manualRequestUriPart.endsWith(".eot")) {
						mimeType = "application/vnd.ms-fontobject";
					} else if (manualRequestUriPart.endsWith(".zip")) {
						mimeType = "application/zip";
					}
	
					if (mimeType == null) {
						response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid datatype of requested manual document");
					} else {
						// check and set language
						String requestedLanguage = manualRequestUriPart.substring(0, manualRequestUriPart.indexOf("/"));
						manualRequestUriPart = StringUtils.strip(manualRequestUriPart.substring(requestedLanguage.length() + 1), "/");
	
						StringBuilder requestedResourcePath = new StringBuilder(getManualInstallationPath(request));
						requestedResourcePath.append("/");
						requestedResourcePath.append(requestedLanguage);
	
						if (!new File(requestedResourcePath.toString()).exists()) {
							requestedResourcePath = new StringBuilder(getManualInstallationPath(request));
							requestedResourcePath.append("/");
							requestedResourcePath.append(DEFAULT_LANGUAGE);
						}
	
						requestedResourcePath.append("/");
						requestedResourcePath.append(manualRequestUriPart);
						File requestedResourceFile = new File(requestedResourcePath.toString());
	
						if (!requestedResourceFile.getAbsolutePath().startsWith(getManualInstallationPath(request))) {
							// found illegal path outside of manual webapp resources (e.g. usage of "..")
							logger.error("Invalid manual path requested: " + requestedResourceFile.getAbsolutePath());
							response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid manual path");
						} else if (!requestedResourceFile.exists()) {
							logger.error("Requested manual resource not found: " + requestedResourceFile.getAbsolutePath());
							if ("text/html".equals(mimeType)) {
								// invalid manual page requested, so show the startpage
								response.setContentType(mimeType);
								String baseManualPath = requestedResourceFile.getAbsolutePath().substring(0, requestedResourceFile.getAbsolutePath().lastIndexOf("/"));
								try (FileInputStream resourceInputStream = new FileInputStream(new File(baseManualPath + "/" + DEFAULT_STARTPAGE))) {
									try (ServletOutputStream outputstream = response.getOutputStream()) {
										IOUtils.copy(resourceInputStream, outputstream);
										outputstream.flush();
									}
								}
							} else {
								// invalid manual resource (mostly image) requested
								response.sendError(HttpServletResponse.SC_NOT_FOUND, "Requested manual resource not found");
							}
						} else {
							response.setContentType(mimeType);
							try (FileInputStream resourceInputStream = new FileInputStream(requestedResourceFile)) {
								try (ServletOutputStream outputstream = response.getOutputStream()) {
									IOUtils.copy(resourceInputStream, outputstream);
									outputstream.flush();
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error("Error sending manual data: " + e.getMessage(), e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Requested manual document cannot be delivered");
		}
	}

	private String getManualInstallationPath(HttpServletRequest request) {
		if (MANUAL_INSTALLATION_PATH == null) {
			if (StringUtils.isNotBlank(getConfigService().getValue(ConfigValue.ManualInstallPath))) {
				MANUAL_INSTALLATION_PATH = getConfigService().getValue(ConfigValue.ManualInstallPath);
			} else {
				String applicationinstallationpath = request.getSession().getServletContext().getRealPath("/") + "../" + MANUAL_CONTEXT + "/";
				// follow relative path to absolute path
				MANUAL_INSTALLATION_PATH = new File(applicationinstallationpath).getAbsolutePath();
			}
			if (logger.isDebugEnabled()) {
				logger.debug("MANUAL_INSTALLATION_PATH = " + MANUAL_INSTALLATION_PATH);
			}
		}

		if (StringUtils.isBlank(MANUAL_INSTALLATION_PATH)) {
			logger.error("Missing manual_installation_path in emm configuration");
		}

		return MANUAL_INSTALLATION_PATH;
	}

    private ConfigService getConfigService() {
		if (configService == null) {
			configService = (ConfigService) WebApplicationContextUtils.getWebApplicationContext(getServletContext()).getBean("ConfigService");
		}
		return configService;
	}
}
