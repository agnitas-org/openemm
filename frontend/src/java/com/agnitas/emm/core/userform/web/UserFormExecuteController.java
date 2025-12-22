/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.userform.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.agnitas.beans.Company;
import com.agnitas.beans.FormComponent.FormComponentType;
import com.agnitas.beans.impl.CompanyStatus;
import com.agnitas.beans.impl.ViciousFormDataException;
import com.agnitas.dao.CompanyDao;
import com.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.core.commons.util.ConfigValue;
import com.agnitas.emm.core.commons.web.ParameterOverwritingHttpServletRequestWrapper;
import com.agnitas.emm.core.company.service.CompanyTokenService;
import com.agnitas.emm.core.components.service.ComponentService;
import com.agnitas.emm.core.mobile.bean.DeviceClass;
import com.agnitas.emm.core.mobile.service.DeviceService;
import com.agnitas.emm.core.userform.exception.BlacklistedDeviceException;
import com.agnitas.emm.core.userform.service.UserFormExecutionResult;
import com.agnitas.emm.core.userform.service.UserFormExecutionService;
import com.agnitas.emm.core.userform.util.WebFormUtils;
import com.agnitas.exception.FormNotFoundException;
import com.agnitas.messages.I18nString;
import com.agnitas.service.AgnTagService;
import com.agnitas.userform.bean.UserForm;
import com.agnitas.util.AgnUtils;
import com.agnitas.util.ImageUtils;
import com.agnitas.web.UserFormSupportForm;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

public class UserFormExecuteController {

	private static final Logger logger = LogManager.getLogger(UserFormExecuteController.class);

	/** Name of request parameter containing company ID. */
	public static final String COMPANY_ID_PARAMETER_NAME = "agnCI";
	
	/** Name of request parameter containing company token. */
	public static final String COMPANY_TOKEN_PARAMETER_NAME = "agnCTOKEN";
	
	public static final String AGN_IMAGE_TAG = "agnIMAGE";
	
	/** Name of request parameter containing form name. */
	public static final String FORM_NAME_PARAMETER_NAME = "agnFN";
	
	public static final String USE_SESSION_PARAMETER_NAME = "agnUseSession";

	protected ConfigService configService;
	protected UserFormExecutionService userFormExecutionService;
	protected CompanyDao companyDao;
	protected AgnTagService agnTagService;
	protected DeviceService deviceService;
	protected ComponentService componentService;
	
	/** Service dealing with company tokens. */
	protected final CompanyTokenService companyTokenService;

	public UserFormExecuteController(final ConfigService configService, final UserFormExecutionService userFormExecutionService, final CompanyDao companyDao, final CompanyTokenService companyTokenService, final AgnTagService agnTagService, final DeviceService deviceService, final ComponentService componentService) {
		this.configService = configService;
		this.userFormExecutionService = userFormExecutionService;
		this.companyDao = companyDao;
		this.companyTokenService = Objects.requireNonNull(companyTokenService, "CompanyTokenService is null");
		this.agnTagService = agnTagService;
		this.deviceService = deviceService;
		this.componentService = componentService;
	}

	@RequestMapping({"/form.action", "/form.do"})
	public String executeForm(HttpServletRequest originalRequest, @RequestParam(value = "file", required = false) MultipartFile file, HttpServletResponse response) throws IOException {
		final ParameterOverwritingHttpServletRequestWrapper request = new ParameterOverwritingHttpServletRequestWrapper(originalRequest);
		
		// Validate the request parameters specified by the user
		final CaseInsensitiveMap<String, Object> params = new CaseInsensitiveMap<>();
		if (file != null) {
			params.put("file_name", file.getOriginalFilename());
			params.put("file_type", file.getContentType());
			params.put("file_size", file.getSize());
			params.put("file", file);
		}

		Company company;
		try {
			final String companyToken = request.getParameter(COMPANY_TOKEN_PARAMETER_NAME);
			if (companyToken != null) {
				company = companyTokenService.findCompanyByToken(companyToken).orElse(null);
				if (company == null) {
					logger.info("Company token '{}' not found", companyToken);
				}
			} else {
				int companyID = Integer.parseInt(request.getParameter(COMPANY_ID_PARAMETER_NAME));
				company = companyDao.getCompany(companyID);
				if (company == null) {
					logger.info("Company id '{}' not found", companyID);
				}
			}
		} catch (Exception e) {
			logger.warn("Error viewing user form", e);
			return null;
		}
		
		if (company == null) {
			// We could not determine company ID from token, so we cannot determine if support button should be shown.
			request.setAttribute("SHOW_SUPPORT_BUTTON", false);
			return "form_not_found";
		}
		
		try {
			response.setBufferSize(65535);
			// Spring Filter in web.xml only sets request encoding charset
			response.setCharacterEncoding(request.getCharacterEncoding());

			final boolean useSession = AgnUtils.interpretAsBoolean(request.getParameter(USE_SESSION_PARAMETER_NAME));
			final String formName = request.getParameter(FORM_NAME_PARAMETER_NAME);
			
			request.setParameter(COMPANY_ID_PARAMETER_NAME, Integer.toString(company.getId()));

			final UserFormExecutionResult result = userFormExecutionService.executeForm(company.getId(), formName, request, params, useSession);
			String responseContent = result.responseContent;
			
	    	final int deviceID = deviceService.getDeviceId(request.getHeader("User-Agent"));
	    	if (deviceID == DeviceService.DEVICE_BLACKLISTED_NO_SERVICE) {
				throw new BlacklistedDeviceException();
			}
	    	DeviceClass deviceClass = deviceService.getDeviceClassForStatistics(deviceID);
	    	boolean isMobile = deviceClass == DeviceClass.MOBILE;
			
			// Replace agnTags as far as possible
			responseContent = resolveAgnTags(company.getId(), result.userFormID, company.getRdirDomain(), responseContent, isMobile);

			Boolean redirectParam = (Boolean) params.get(UserForm.TEMP_REDIRECT_PARAM);
			if (redirectParam != null && redirectParam) {
				response.sendRedirect(responseContent);
			} else {
				if (params.get("responseRedirect") != null) {
					response.sendRedirect((String) params.get("responseRedirect"));
				} else {
					response.setContentType(result.responseMimeType);

					try (PrintWriter out = response.getWriter()) {
						out.print(responseContent);
						out.flush();
					}
				}
			}
			response.flushBuffer();
			return null;
		} catch (BlacklistedDeviceException e) {
			response.setContentType("text/plain");
			response.getOutputStream().write("No service".getBytes(StandardCharsets.UTF_8));
			return null;
		} catch (FormNotFoundException formNotFoundEx) {
            HashMap<String, List<String>> reqParams = new HashMap<>();
            Enumeration<String> parameterEnumeration = request.getParameterNames();
            while (parameterEnumeration.hasMoreElements()) {
                String paramName = parameterEnumeration.nextElement();
                List<String> paramValues = Arrays.asList(request.getParameterValues(paramName));
                reqParams.put(paramName, paramValues);
            }
            UserFormSupportForm supportForm = new UserFormSupportForm();
            supportForm.setUrl(request.getRequestURL() + "?" + request.getQueryString());
            supportForm.setParams(reqParams);
            request.setAttribute("userFormSupportForm", supportForm);

			// Check, that "agnFN" and "agnCI" parameters are both present
			if (request.getParameter(FORM_NAME_PARAMETER_NAME) != null && !request.getParameter(FORM_NAME_PARAMETER_NAME).equals("") && request.getParameter(COMPANY_ID_PARAMETER_NAME) != null
					&& !request.getParameter(COMPANY_ID_PARAMETER_NAME).equals("")) {
				request.setAttribute("SHOW_SUPPORT_BUTTON", CompanyStatus.ACTIVE == company.getStatus());
				return "form_not_found";
			} else {
				return null;
			}
		} catch (ViciousFormDataException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return null;
		} catch (Exception e) {
			logger.error("execute()", e);
			return I18nString.getLocaleString("error.exception", configService.getValue(ConfigValue.SupportEmergencyUrl));
		}
	}
	
	private String resolveAgnTags(int companyID, int formID, String redirectDomain, String text, boolean isMobileView) throws Exception {
		if (text != null && text.contains("[agn")) {
			// Resolve agnIMAGE tags and escape the rest of agn-tags.
	        return agnTagService.resolveTags(text, false, (tag) -> {
	            if (AGN_IMAGE_TAG.equals(tag.getTagName())) {
	                Map<String, String> options = tag.getTagParameters();
	                String filename;
	                if (MapUtils.isNotEmpty(options) && (filename = options.get("name")) != null) {
	                    String source = options.get("source");
	                    if (source == null || "userform".equalsIgnoreCase(source)) {
	                        if (isMobileView) {
	                        	String mobileUserFormImageName = ImageUtils.MOBILE_IMAGE_PREFIX + filename;
	                            // try to find mobile image if exists
	                        	if (componentService.getFormComponent(formID, companyID, mobileUserFormImageName, FormComponentType.IMAGE) != null) {
	                        		return WebFormUtils.getImageSrcPattern(redirectDomain, configService.getIntegerValue(ConfigValue.System_Licence), companyID, formID, false).replace("{name}", mobileUserFormImageName);
	                        	}
	                        }
	
	                        if (componentService.getFormComponent(formID, companyID, filename, FormComponentType.IMAGE) != null) {
	                        	return WebFormUtils.getImageSrcPattern(redirectDomain, configService.getIntegerValue(ConfigValue.System_Licence), companyID, formID, false).replace("{name}", filename);
	                        }
	                    }
	                    
	                    String url = resolveByAdditionalSources(companyID, redirectDomain, isMobileView, filename, source);
	                    if (url != null) {
	                    	return url;
	                    }
	                }
	            }

	            // Simply escape rest of the agn-tags.
	            return StringEscapeUtils.escapeHtml4(tag.getFullText());
	        });
		} else {
			return text;
		}
	}

	protected String resolveByAdditionalSources(int companyID, String redirectDomain, boolean isMobileView, String filename, String source) {
		return null;
	}
}
