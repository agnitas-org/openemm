/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.agnitas.emm.core.mobile.bean.DeviceClass;
import org.agnitas.beans.impl.ViciousFormDataException;
import org.agnitas.emm.core.commons.uid.ExtensibleUIDService;
import org.agnitas.emm.core.commons.uid.parser.exception.DeprecatedUIDVersionException;
import org.agnitas.emm.core.commons.uid.parser.exception.InvalidUIDException;
import org.agnitas.emm.core.commons.uid.parser.exception.UIDParseException;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.exceptions.FormNotFoundException;
import org.agnitas.util.AgnUtils;
import org.agnitas.web.StrutsActionBase;
import org.agnitas.web.UserFormExecuteForm;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.agnitas.beans.ComCompany;
import com.agnitas.beans.LinkProperty;
import com.agnitas.beans.LinkProperty.PropertyType;
import com.agnitas.dao.ComCompanyDao;
import com.agnitas.dao.ComRecipientDao;
import com.agnitas.dao.UserFormDao;
import com.agnitas.emm.core.action.service.EmmActionOperationErrors;
import com.agnitas.emm.core.commons.uid.ComExtensibleUID;
import com.agnitas.emm.core.mobile.service.ClientService;
import com.agnitas.emm.core.mobile.service.ComDeviceService;
import com.agnitas.userform.bean.UserForm;
import com.agnitas.userform.trackablelinks.bean.ComTrackableUserFormLink;

/**
 * Implementation of <strong>Action</strong> that processes a form.do request
 */
public final class ComUserFormExecuteAction extends StrutsActionBase {
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(ComUserFormExecuteAction.class);
	
	// ----------------------------------------------------------------------------------------------------------------
	// Dependency injection

	protected ConfigService configService;
	protected ComDeviceService comDeviceService;
	protected ClientService clientService;
	protected ComRecipientDao comRecipientDao;
	protected ComCompanyDao comCompanyDao;
	protected UserFormDao userFormDao;
	protected ExtensibleUIDService extensibleUIDService;

	@Required
	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}
	
	@Required
	public void setDeviceService(ComDeviceService comDeviceService) {
		this.comDeviceService = comDeviceService;
	}
	
	@Required
	public void setClientService(ClientService clientService) {
		this.clientService = clientService;
	}

	@Required
	public void setRecipientDao(ComRecipientDao comRecipientDao) {
		this.comRecipientDao = comRecipientDao;
	}

	@Required
	public void setCompanyDao(ComCompanyDao comCompanyDao) {
		this.comCompanyDao = comCompanyDao;
	}

	@Required
	public void setUserFormDao(UserFormDao userFormDao) {
		this.userFormDao = userFormDao;
	}
	
	@Required
	public void setExtensibleUIDService(ExtensibleUIDService uidService) {
		this.extensibleUIDService = uidService;
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
		ActionMessages errors = new ActionMessages();
		UserFormExecuteForm aForm = (UserFormExecuteForm) form;
		ActionForward destination = null;
		CaseInsensitiveMap<String, Object> params = new CaseInsensitiveMap<>();

		try {
			res.setBufferSize(65535);
			// Spring Filter in web.xml only sets request encoding charset
			res.setCharacterEncoding(req.getCharacterEncoding());

			int agnUseSession = aForm.getAgnUseSession();
			ComExtensibleUID uid = processUID(req, params, agnUseSession);

			params.put("requestParameters", AgnUtils.getReqParameters(req));
			params.put("_request", req);
			if ((req.getParameter("requestURL") != null) && (req.getParameter("queryString") != null)) {
				params.put("formURL", req.getRequestURL() + "?" + req.getQueryString());
			}
			
			// adding mobile informations
			int deviceID = comDeviceService.getDeviceId(req.getHeader("User-Agent"));
			int clientID = clientService.getClientId(req.getHeader("User-Agent"));
			DeviceClass deviceClass = comDeviceService.getDeviceClassForStatistics(deviceID);

			if (deviceID == ComDeviceService.DEVICE_BLACKLISTED_NO_SERVICE) {
				res.setContentType("text/plain");
				res.getOutputStream().write("No service".getBytes(StandardCharsets.UTF_8));
			}

			int mobileID = deviceClass == DeviceClass.MOBILE ? deviceID : -1;
			if (mobileID > 0) {
				params.put("mobileDevice", String.valueOf(mobileID));
				// just to be sure
				req.setAttribute("mobileDevice", String.valueOf(mobileID));
			}

			Integer mailingIdInt = null;
			Integer customerIdInt = null;

			if (uid != null) {
				if (uid.getMailingID() > 0) {
					mailingIdInt = uid.getMailingID();
				}
				if (uid.getCustomerID() > 0) {
					customerIdInt = uid.getCustomerID();
				}
			}

			String formName = aForm.getAgnFN();
			int companyID = aForm.getAgnCI();
			UserForm userForm = userFormDao.getUserFormByName(formName, companyID);

			// Show "form not found" page if form is actually not found or if it's inactive.
			if (userForm == null || !userForm.isActive()) {
				throw new FormNotFoundException(companyID, formName);
			}

			// only count statistics if form exists
			if (deviceID != ComDeviceService.DEVICE_BLACKLISTED_NO_COUNT) {
				ComTrackableUserFormLink formStatisticsDummyLink = userFormDao.getDummyUserFormTrackableLinkForStatisticCount(companyID, userForm.getId());
				if (formStatisticsDummyLink != null) {
					userFormDao.logUserFormCallInDB(userForm.getCompanyID(), userForm.getId(), formStatisticsDummyLink.getId(), mailingIdInt, customerIdInt, req.getRemoteAddr(), deviceClass, deviceID, clientID);
				}
			}
			
			final EmmActionOperationErrors actionOperationErrors = new EmmActionOperationErrors();
			params.put("actionErrors", actionOperationErrors);
			params.put("formID", userForm.getId());
			String responseContent = userForm.evaluateForm(WebApplicationContextUtils.getWebApplicationContext(req.getServletContext()), params, actionOperationErrors);
			String responseMimeType = userForm.getSuccessMimetype();

			String uidString = (String) params.get("agnUID");
			responseContent = addRedirectLinks(responseContent, uidString, userForm);
			
			if (params.get("_error") == null) {
				evaluateFormEndAction(req, userForm, params, actionOperationErrors);
				responseContent = handleEndActionErrors(userForm, params, responseContent);
			} else {
				responseMimeType = userForm.getErrorMimetype();
			}
			
			sendFormResult(res, params, responseContent, responseMimeType);
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

	/**
	 * Execute the end action of the requested form. Reads the form defined by aForm.getAgnFN() and aForm.getAgnCI() from the database and executes it's end action.
	 * 
	 * @param params a map containing the form values.
	 * @return true==success false==error
	 * @throws Exception
	 */
	protected boolean evaluateFormEndAction(HttpServletRequest request, UserForm userForm, Map<String, Object> params, final EmmActionOperationErrors errors) throws Exception {
		if (userForm == null || userForm.getEndActionID() == 0) {
			return false;
		}

		return userForm.evaluateEndAction(WebApplicationContextUtils.getWebApplicationContext(request.getServletContext()), params, errors);
	}

	/**
	 * information from a given url. Parses an url and returns the retrieved values in a hash.
	 * 
	 * @param req ServletRequest, used to get the Session.
	 * @param params HashMap to store the retrieved values in.
	 * @param useSession also store the result in the session if this is not 0.
	 */
	@SuppressWarnings("unchecked")
	public ComExtensibleUID processUID(HttpServletRequest req, Map<String, Object> params, int useSession) {
		int	companyIdRequestParam = NumberUtils.toInt(req.getParameter("agnCI"));
		String uidString = getUidStringFromRequest(req);
		ComExtensibleUID uidObject = decodeUidString(uidString);

		if (Objects.nonNull(uidObject)) {
			if (companyIdRequestParam == uidObject.getCompanyID()) {
				params.put("customerID", uidObject.getCustomerID());
				params.put("mailingID", uidObject.getMailingID());
				params.put("urlID", uidObject.getUrlID());
				params.put("agnUID", uidString);
				params.put("companyID", uidObject.getCompanyID());
				params.put("locale", req.getLocale());

				if (useSession != 0) {
					CaseInsensitiveMap<String, Object> tmpPars = new CaseInsensitiveMap<>();
					tmpPars.putAll(params);
					req.getSession().setAttribute("agnFormParams", tmpPars);
					params.put("sessionID", req.getSession().getId());
				}
			}
		} else {
			if (useSession != 0) {
				if (req.getSession().getAttribute("agnFormParams") != null) {
					params.putAll((Map<String, Object>) req.getSession().getAttribute("agnFormParams"));
				}
			}
		}

		return uidObject;
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
	 * Creates the redirect link to the formula content
	 * 
	 * @param content formula content
	 * @return
	 * @throws Exception
	 */
	protected String addRedirectLinks(String content, String uidString, UserForm userForm) throws Exception {
		// create the redirect link for each trackable link or use extensions on direct link
		Map<String, ComTrackableUserFormLink> trackableLinks = userFormDao.getUserFormTrackableLinks(userForm.getId(), userForm.getCompanyID());
		for (ComTrackableUserFormLink link : trackableLinks.values()) {
			String replaceFromString = "href=\"" + link.getFullUrl() + "\"";

			if (link.getUsage() != ComTrackableUserFormLink.TRACKABLE_NO) {
				// Create rdir link (without link extensions, extensions are inserted in rdir call)
				String rdirLinkString = "href=\"" + comCompanyDao.getCompany(userForm.getCompanyID()).getRdirDomain() + "/rdirFormUrl?lid=" + link.getId();
				if (uidString != null) {
					rdirLinkString += "&uid=" + uidString;
				}
				rdirLinkString += "\"";
				content = content.replace(replaceFromString, rdirLinkString);
			} else if (link.getProperties() != null && link.getProperties().size() > 0) {
				// Use link extensions for direct link
				content = content.replace(replaceFromString, "href=\"" + createDirectLinkWithOptionalExtensions(uidString, link) + "\"");
			}
		}

		return content;
	}
	
	private String createDirectLinkWithOptionalExtensions(String uidString, ComTrackableUserFormLink comTrackableUserFormLink) throws UIDParseException, InvalidUIDException, DeprecatedUIDVersionException, UnsupportedEncodingException {
		String linkString = comTrackableUserFormLink.getFullUrl();
		CaseInsensitiveMap<String, Object> cachedRecipientData = null;
		for (LinkProperty linkProperty : comTrackableUserFormLink.getProperties()) {
			if (linkProperty.getPropertyType() == PropertyType.LinkExtension) {
				String propertyValue = linkProperty.getPropertyValue();
				if (propertyValue != null && propertyValue.contains("##")) {
					if (cachedRecipientData == null && StringUtils.isNotBlank(uidString)) {
						final ComExtensibleUID uid = decodeUidString(uidString);
						cachedRecipientData = comRecipientDao.getCustomerDataFromDb(uid.getCompanyID(), uid.getCustomerID());
						cachedRecipientData.put("mailing_id", uid.getMailingID());
					}
					// Replace customer and form placeholders
					@SuppressWarnings("unchecked")
					String replacedPropertyValue = AgnUtils.replaceHashTags(propertyValue, cachedRecipientData);
					propertyValue = replacedPropertyValue;
				}
				// Extend link properly (watch out for html-anchors etc.)
				linkString = AgnUtils.addUrlParameter(linkString, linkProperty.getPropertyName(), propertyValue == null ? "" : propertyValue, "UTF-8");
			}
		}
		return linkString;
	}

	/**
	 * For user form that requires error handling adds the error messages (including velocity errors) to response context.
	 *
	 * @param params
	 * @param responseContent html content to be sent in response (could be changed inside the method).
	 * @return responseContent
	 * @throws Exception
	 */
	protected String handleEndActionErrors(UserForm userForm, CaseInsensitiveMap<String, Object> params, String responseContent) throws Exception {
		if (userForm != null && userForm.isSuccessUseUrl()) {
			// no error handling, return original content
			return responseContent;
		}
		
		if (params.get("velocity_error") != null) {
			responseContent += "<br/><br/>";
			responseContent += params.get("velocity_error");
		}
		
		if (params.get("errors") != null) {
			responseContent += "<br/>";
			ActionErrors velocityErrors = ((ActionErrors) params.get("errors"));
			@SuppressWarnings("rawtypes")
			Iterator it = velocityErrors.get();
			while (it.hasNext()) {
				responseContent += "<br/>" + it.next();
			}
		}
		
		return responseContent;
	}

	/**
	 * Sends responce with execution form result.
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
				res.setCharacterEncoding("UTF-8");
				res.setContentType(responseMimeType);

				try (PrintWriter out = res.getWriter()) {
					out.print(responseContent);
					out.flush();
				}
			}
		}
		res.flushBuffer();
	}

	/**
	 * Use a uidString to get a UID object. Retrieves a UID according to a given tag.
	 * 
	 * @param uidString a string defining the uid.
	 * @return the resulting UID or NULL in case of some problem during parsing.
	 */
	private ComExtensibleUID decodeUidString(String uidString) {
		if (StringUtils.isNotBlank(uidString)) {
			try {
				return extensibleUIDService.parse(uidString);
			} catch (DeprecatedUIDVersionException e) {
				logger.warn(String.format("Deprecated UID version of UID: %s", uidString), e);
			} catch (UIDParseException e) {
				logger.warn(String.format("Error parsing UID: %s", uidString), e);
			} catch (InvalidUIDException e) {
				logger.warn(String.format("Invalid UID: %s", uidString), e);
			}
		}
		return null;
	}

	private String getUidStringFromRequest(HttpServletRequest req) {
		String uidString = req.getParameter("agnUID");
		if (StringUtils.isBlank(uidString)) {
			uidString = req.getParameter("uid");
		}
		return uidString;
	}
}
