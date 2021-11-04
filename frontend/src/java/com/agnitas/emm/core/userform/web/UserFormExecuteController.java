/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.userform.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.Objects;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.agnitas.beans.impl.CompanyStatus;
import org.agnitas.beans.impl.ViciousFormDataException;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.exceptions.FormNotFoundException;
import org.agnitas.util.AgnUtils;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.agnitas.beans.ComCompany;
import com.agnitas.dao.ComCompanyDao;
import com.agnitas.emm.core.commons.web.ParameterOverwritingHttpServletRequestWrapper;
import com.agnitas.emm.core.company.service.CompanyTokenService;
import com.agnitas.emm.core.company.service.UnknownCompanyTokenException;
import com.agnitas.emm.core.userform.exception.BlacklistedDeviceException;
import com.agnitas.emm.core.userform.service.UserFormExecutionResult;
import com.agnitas.emm.core.userform.service.UserFormExecutionService;
import com.agnitas.messages.I18nString;
import com.agnitas.userform.bean.UserForm;
import com.agnitas.web.ComSupportForm;

@Controller
@RequestMapping("/")
public class UserFormExecuteController {
	
	/** Name of request parameter containing company ID. */
	public static final String COMPANY_ID_PARAMETER_NAME = "agnCI";
	
	/** Name of request parameter containing company token. */
	public static final String COMPANY_TOKEN_PARAMETER_NAME = "agnCTOKEN";
	
	/** Name of request parameter containing form name. */
	public static final String FORM_NAME_PARAMETER_NAME = "agnFN";
	
	public static final String USE_SESSION_PARAMETER_NAME = "agnUseSession";

	/** The logger. */
	private static final Logger logger = Logger.getLogger(UserFormExecuteController.class);

	protected ConfigService configService;
	private UserFormExecutionService userFormExecutionService;
	protected ComCompanyDao companyDao;
	
	/** Service dealing with company tokens. */
	private final CompanyTokenService companyTokenService;

	public UserFormExecuteController(final ConfigService configService, final UserFormExecutionService userFormExecutionService, final ComCompanyDao companyDao, final CompanyTokenService companyTokenService) {
		this.configService = configService;
		this.userFormExecutionService = userFormExecutionService;
		this.companyDao = companyDao;
		this.companyTokenService = Objects.requireNonNull(companyTokenService, "CompanyTokenService is null");
	}

	/**
	 * This method is always allowed by exclude entry in "spring-mvc-servlet.xml"
	 * 
	 * @throws IOException
	 */
	@RequestMapping("/form.action")
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

		try {
			response.setBufferSize(65535);
			// Spring Filter in web.xml only sets request encoding charset
			response.setCharacterEncoding(request.getCharacterEncoding());

			final boolean useSession = AgnUtils.interpretAsBoolean(request.getParameter(USE_SESSION_PARAMETER_NAME));
			final String formName = request.getParameter(FORM_NAME_PARAMETER_NAME);
			final int companyID = determineCompanyId(request);
			
			request.setParameter(COMPANY_ID_PARAMETER_NAME, Integer.toString(companyID));

			final UserFormExecutionResult result = userFormExecutionService.executeForm(companyID, formName, request, params, useSession);
			String responseContent = result.responseContent;

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
		} catch(final UnknownCompanyTokenException e) {
			if(logger.isInfoEnabled()) {
				logger.info(String.format("Company token '%s' not found", e.getToken()));
			}
			
			// We could not determine company ID from token, so we cannot determine if support button should be shown.
			request.setAttribute("SHOW_SUPPORT_BUTTON", false);
			return "form_not_found";
		} catch (FormNotFoundException formNotFoundEx) {
			final ComSupportForm supportForm = new ComSupportForm();

			final Enumeration<String> parameterEnumeration = request.getParameterNames();
			while (parameterEnumeration.hasMoreElements()) {
				final String paramName = parameterEnumeration.nextElement();
				final String[] paramValues = request.getParameterValues(paramName);

				for (String paramValue : paramValues) {
					supportForm.addParameter(paramName, paramValue);
				}
			}
			supportForm.setUrl(request.getRequestURL() + "?" + request.getQueryString());

			request.setAttribute("supportForm", supportForm);

			// Check, that "agnFN" and "agnCI" parameters are both present
			if (request.getParameter(FORM_NAME_PARAMETER_NAME) != null && !request.getParameter(FORM_NAME_PARAMETER_NAME).equals("") && request.getParameter(COMPANY_ID_PARAMETER_NAME) != null
					&& !request.getParameter(COMPANY_ID_PARAMETER_NAME).equals("")) {
				try {
					final int companyID = determineCompanyId(request);
					final ComCompany company = companyDao.getCompany(companyID);

					request.setAttribute("SHOW_SUPPORT_BUTTON", company != null && CompanyStatus.ACTIVE == company.getStatus());

					return "form_not_found";
				} catch(final UnknownCompanyTokenException e) {
					if(logger.isInfoEnabled()) {
						logger.info(String.format("Company token '%s' not found", e.getToken()));
					}
					
					// We could not determine company ID from token, so we cannot determine if support button should be shown.
					request.setAttribute("SHOW_SUPPORT_BUTTON", false);
					return "form_not_found";
				} catch (final Exception e) {
					logger.warn("Error viewing form-not-found message", e);

					return null;
				}
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
	
	private final int determineCompanyId(final HttpServletRequest request) throws UnknownCompanyTokenException {
		final String companyToken = request.getParameter(COMPANY_TOKEN_PARAMETER_NAME);

		if(companyToken != null) {
			return this.companyTokenService.findCompanyByToken(companyToken).getId();
		} else {
			return Integer.parseInt(request.getParameter(COMPANY_ID_PARAMETER_NAME));
		}
	}
}
