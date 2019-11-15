/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.agnitas.beans.impl.ViciousFormDataException;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.exceptions.FormNotFoundException;
import org.agnitas.web.StrutsActionBase;
import org.agnitas.web.UserFormExecuteForm;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.beans.ComCompany;
import com.agnitas.dao.ComCompanyDao;
import com.agnitas.emm.core.userform.exception.BlacklistedDeviceException;
import com.agnitas.emm.core.userform.service.UserFormExecutionResult;
import com.agnitas.emm.core.userform.service.UserFormExecutionService;
import com.agnitas.userform.bean.UserForm;

/**
 * Implementation of <strong>Action</strong> that processes a form.do request
 */
public final class ComUserFormExecuteAction extends StrutsActionBase {
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(ComUserFormExecuteAction.class);
	
	// ----------------------------------------------------------------------------------------------------------------
	// Dependency injection

	protected ConfigService configService;
	protected ComCompanyDao comCompanyDao;
	
	private UserFormExecutionService userFormExecutionService;

	@Required
	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}

	@Required
	public void setCompanyDao(ComCompanyDao comCompanyDao) {
		this.comCompanyDao = comCompanyDao;
	}
	
	@Required
	public final void setUserFormExecutionService(final UserFormExecutionService service) {
		this.userFormExecutionService = service;
	}
	
	// ----------------------------------------------------------------------------------------------------------------
	// Business logic

	/**
	 * Process the specified HTTP request, and create the corresponding HTTP response (or forward to another web component that will create it). Return an <code>ActionForward</code> instance
	 * describing where and how control should be forwarded, or <code>null</code> if the response has already been completed. <br>
	 * Loads the requested user form into response context; executes requested form evaluates user form end action, sends the html response. <br>
	 * If used Oracle database, loads character encoding into response. <br>
	 * If requested user form not found, sends error message into response. <br>
	 * <br>
	 * 
	 * @param form ActionForm object
	 * @param req request
	 * @param res response
	 * @param mapping The ActionMapping used to select this instance
	 * @exception IOException if an input/output error occurs
	 * @exception ServletException if a servlet exception occurs
	 * @return null
	 */
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
		// Validate the request parameters specified by the user
		final ActionMessages errors = new ActionMessages();
		final UserFormExecuteForm aForm = (UserFormExecuteForm) form;
		ActionForward destination = null;
		final CaseInsensitiveMap<String, Object> params = new CaseInsensitiveMap<>();

		try {
			res.setBufferSize(65535);
			// Spring Filter in web.xml only sets request encoding charset
			res.setCharacterEncoding(req.getCharacterEncoding());

			final boolean useSession = aForm.getAgnUseSession() != 0;
			final String formName = aForm.getAgnFN();
			final int companyID = aForm.getAgnCI();

			final UserFormExecutionResult result = userFormExecutionService.executeForm(companyID, formName, req, params, useSession);
			
			sendFormResult(res, params, result.responseContent, result.responseMimeType);
		} catch(BlacklistedDeviceException e) {
			res.setContentType("text/plain");
			res.getOutputStream().write("No service".getBytes(StandardCharsets.UTF_8));
		} catch (FormNotFoundException formNotFoundEx) {
			destination = handleFormNotFound(mapping, req, res, params);
		} catch (ViciousFormDataException e) {
			res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			destination = null;
		} catch (Exception e) {
			logger.error("execute()", e);
			errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.exception", configService.getValue(ConfigValue.SupportEmergencyUrl)));
		}

		// Report any errors we have discovered back to the original form
		if (!errors.isEmpty()) {
			saveErrors(req, errors);
			return new ActionForward(mapping.getInput());
		} else {
			return destination;
		}
	}

	protected ActionForward handleFormNotFound(ActionMapping mapping, HttpServletRequest request, HttpServletResponse res, Map<String, Object> param) throws IOException {
		final ComSupportForm supportForm = new ComSupportForm();

		final Enumeration<String> parameterEnumeration = request.getParameterNames();
		while (parameterEnumeration.hasMoreElements()) {
			final String paramName =parameterEnumeration.nextElement();
			final String[] paramValues = request.getParameterValues(paramName);

			for (String paramValue : paramValues) {
				supportForm.addParameter(paramName, paramValue);
			}
		}
		supportForm.setUrl(request.getRequestURL() + "?" + request.getQueryString());

		request.setAttribute("supportForm", supportForm);

		// Check, that "agnFN" and "agnCI" parameters are both present
		if (request.getParameter("agnFN") != null && !request.getParameter("agnFN").equals("") && request.getParameter("agnCI") != null
				&& !request.getParameter("agnCI").equals("")) {
			try {
				final int companyID = Integer.parseInt(request.getParameter("agnCI"));
				final ComCompany company = this.comCompanyDao.getCompany(companyID);
				
				request.setAttribute("SHOW_SUPPORT_BUTTON", company != null && company.getStatus().equals("active"));
				
				return mapping.findForward("form_not_found");
			} catch(final Exception e) {
				logger.warn("Error viewing form-not-found message", e);
				
				return null;
			}
		} else {
			return null;
		}
	}

	/**
	 * Sends response with execution form result.
	 * 
	 * @param res response
	 * @param params
	 * @param responseContent html content to be sent in response
	 * @throws IOException
	 */
	protected void sendFormResult(HttpServletResponse res, Map<String, Object> params, String responseContent, String responseMimeType) throws IOException {
		Boolean redirectParam = (Boolean) params.get(UserForm.TEMP_REDIRECT_PARAM);
		if (redirectParam != null && redirectParam) {
			res.sendRedirect(responseContent);
		} else {
			if (params.get("responseRedirect") != null) {
				res.sendRedirect((String) params.get("responseRedirect"));
			} else {
				res.setContentType(responseMimeType);

				try (PrintWriter out = res.getWriter()) {
					out.print(responseContent);
					out.flush();
				}
			}
		}
		res.flushBuffer();
	}

}
