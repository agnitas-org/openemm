/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.userform.service.impl;

import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;

import org.agnitas.beans.BaseTrackableLink;
import org.agnitas.emm.core.commons.uid.ExtensibleUIDService;
import org.agnitas.emm.core.commons.uid.parser.exception.DeprecatedUIDVersionException;
import org.agnitas.emm.core.commons.uid.parser.exception.InvalidUIDException;
import org.agnitas.emm.core.commons.uid.parser.exception.UIDParseException;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.exceptions.FormNotFoundException;
import org.agnitas.util.AgnUtils;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionErrors;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.agnitas.beans.LinkProperty;
import com.agnitas.beans.LinkProperty.PropertyType;
import com.agnitas.dao.ComCompanyDao;
import com.agnitas.dao.ComRecipientDao;
import com.agnitas.dao.UserFormDao;
import com.agnitas.emm.core.action.service.EmmActionOperationErrors;
import com.agnitas.emm.core.commons.uid.ComExtensibleUID;
import com.agnitas.emm.core.mailing.cache.MailingContentTypeCache;
import com.agnitas.emm.core.mailtracking.service.TrackingVetoHelper;
import com.agnitas.emm.core.mailtracking.service.TrackingVetoHelper.TrackingLevel;
import com.agnitas.emm.core.mobile.bean.DeviceClass;
import com.agnitas.emm.core.mobile.service.ClientService;
import com.agnitas.emm.core.mobile.service.ComDeviceService;
import com.agnitas.emm.core.userform.exception.BlacklistedDeviceException;
import com.agnitas.emm.core.userform.service.UserFormExecutionResult;
import com.agnitas.emm.core.userform.service.UserFormExecutionService;
import com.agnitas.userform.bean.UserForm;
import com.agnitas.userform.trackablelinks.bean.ComTrackableUserFormLink;

public final class UserFormExecutionServiceImpl implements UserFormExecutionService, ApplicationContextAware {
	
	private static final transient Logger logger = Logger.getLogger(UserFormExecutionServiceImpl.class);

	private ConfigService configService;
	private ComDeviceService deviceService;
	private ClientService clientService;
	private UserFormDao userFormDao;
	private ExtensibleUIDService extensibleUIDService;
	private ComCompanyDao companyDao;
	private ComRecipientDao recipientDao;
	private MailingContentTypeCache mailingContentTypeCache;
	
	private ApplicationContext applicationContext;

	@Override
	public final UserFormExecutionResult executeForm(final int companyID, final String formName, final HttpServletRequest request, CaseInsensitiveMap<String, Object> params, final boolean useSession) throws Exception {
		populateRequestParametersAsVelocityParameters(request, params);

		int deviceID = deviceService.getDeviceId(request.getHeader("User-Agent"));
		DeviceClass deviceClass = deviceService.getDeviceClassForStatistics(deviceID);
		if (deviceID == ComDeviceService.DEVICE_BLACKLISTED_NO_SERVICE) {
			throw new BlacklistedDeviceException();
		}

		final int mobileID = deviceClass == DeviceClass.MOBILE ? deviceID : -1;
		if (mobileID > 0) {
			populateMobileDeviceParametersAsVelocityParameters(mobileID, params, request);
		}

		final int clientID = clientService.getClientId(request.getHeader("User-Agent"));
		final UserForm userForm = loadUserForm(formName, companyID);

		final ComExtensibleUID uid = processUID(request, params, useSession);
		
		logFormAccess(userForm, uid, request.getRemoteAddr(), deviceID, deviceClass, clientID);
		
		final EmmActionOperationErrors actionOperationErrors = populateEmmActionErrorsAsVelocityParameters(params);
		populateFormPropertiesAsVelocityParameters(userForm, params);

		return doExecuteForm(userForm, actionOperationErrors, params, request);
	}

	private void populateFormPropertiesAsVelocityParameters(UserForm userForm, CaseInsensitiveMap<String, Object> params) {
		params.put("formID", userForm.getId());
	}

	private EmmActionOperationErrors populateEmmActionErrorsAsVelocityParameters(CaseInsensitiveMap<String, Object> params) {
		final EmmActionOperationErrors actionOperationErrors = new EmmActionOperationErrors();
		params.put("actionErrors", actionOperationErrors);
		
		return actionOperationErrors;
	}

	/**
	 * information from a given url. Parses an url and returns the retrieved values in a hash.
	 * 
	 * @param req ServletRequest, used to get the Session.
	 * @param params HashMap to store the retrieved values in.
	 * @param useSession also store the result in the session if this is not 0.
	 */
	@SuppressWarnings("unchecked")
	public ComExtensibleUID processUID(HttpServletRequest req, Map<String, Object> params, boolean useSession) {
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

				if (useSession) {
					CaseInsensitiveMap<String, Object> tmpPars = new CaseInsensitiveMap<>();
					tmpPars.putAll(params);
					req.getSession().setAttribute("agnFormParams", tmpPars);
					params.put("sessionID", req.getSession().getId());
				}
			}
		} else {
			if (useSession) {
				if (req.getSession().getAttribute("agnFormParams") != null) {
					params.putAll((Map<String, Object>) req.getSession().getAttribute("agnFormParams"));
				}
			}
		}

		return uidObject;
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

	private final void logFormAccess(final UserForm userForm, final ComExtensibleUID uid, String remoteAddress, final int deviceID, final DeviceClass deviceClass, final int clientID) throws Exception {
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

		// only count statistics if form exists
		if (deviceID != ComDeviceService.DEVICE_BLACKLISTED_NO_COUNT) {
			final ComTrackableUserFormLink formStatisticsDummyLink = userFormDao.getDummyUserFormTrackableLinkForStatisticCount(userForm.getCompanyID(), userForm.getId());
			if (formStatisticsDummyLink != null) {
				if (uid != null) {
					final TrackingLevel trackingLevel = TrackingVetoHelper.computeTrackingLevel(uid, configService, mailingContentTypeCache);
					if (trackingLevel == TrackingLevel.ANONYMOUS) {
						customerIdInt = 0;
						remoteAddress = null;
					}
				}
				userFormDao.logUserFormCallInDB(userForm.getCompanyID(), userForm.getId(), formStatisticsDummyLink.getId(), mailingIdInt, customerIdInt, remoteAddress, deviceClass, deviceID, clientID);
			}
		}
	}
	
	private final UserForm loadUserForm(final String formName, final int companyID) throws Exception {
		final UserForm userForm = userFormDao.getUserFormByName(formName, companyID);

		// Show "form not found" page if form is actually not found or if it's inactive.
		if (userForm == null || !userForm.isActive()) {
			throw new FormNotFoundException(companyID, formName);
		}
		
		return userForm;
	}
	
	private final void populateRequestParametersAsVelocityParameters(final HttpServletRequest request, final CaseInsensitiveMap<String, Object> params) {
		params.put("requestParameters", AgnUtils.getReqParameters(request));
		params.put("_request", request);

		if ((request.getParameter("requestURL") != null) && (request.getParameter("queryString") != null)) {
			params.put("formURL", request.getRequestURL() + "?" + request.getQueryString());
		}
	}
	
	private final void populateMobileDeviceParametersAsVelocityParameters(final int mobileID, final CaseInsensitiveMap<String, Object> params, final HttpServletRequest request) {
		params.put("mobileDevice", String.valueOf(mobileID));
		// just to be sure
		request.setAttribute("mobileDevice", String.valueOf(mobileID));
	}
	
	private final UserFormExecutionResult doExecuteForm(final UserForm userForm, final EmmActionOperationErrors actionOperationErrors, final CaseInsensitiveMap<String, Object> params, final HttpServletRequest request) throws Exception {
		String responseContent = userForm.evaluateForm(applicationContext, params, actionOperationErrors);
		String responseMimeType = determineSuccessResponseMimeType(userForm, params);

		final String uidString = (String) params.get("agnUID");
		responseContent = addRedirectLinks(responseContent, uidString, userForm);
		
		if (params.get("_error") == null) {
			evaluateFormEndAction(request, userForm, params, actionOperationErrors);
			responseContent = handleEndActionErrors(userForm, params, responseContent);
		} else {
			responseMimeType = userForm.getErrorMimetype();
		}

		return new UserFormExecutionResult(responseContent, responseMimeType);
	}
	
	private final String determineSuccessResponseMimeType(final UserForm userForm, final CaseInsensitiveMap<String, Object> params) {
		final String formMimeType = (String)params.get(FORM_MIMETYPE_PARAM_NAME);
	
		
		return StringUtils.isNotBlank(formMimeType)
				? formMimeType
				: userForm.getSuccessMimetype();
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

			if (link.getUsage() != BaseTrackableLink.TRACKABLE_NO) {
				// Create rdir link (without link extensions, extensions are inserted in rdir call)
				String rdirLinkString = "href=\"" + companyDao.getCompany(userForm.getCompanyID()).getRdirDomain() + "/rdirFormUrl?lid=" + link.getId();
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
						cachedRecipientData = recipientDao.getCustomerDataFromDb(uid.getCompanyID(), uid.getCustomerID());
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

		return userForm.evaluateEndAction(applicationContext, params, errors);
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

	@Override
	public void setApplicationContext(ApplicationContext ctxt) throws BeansException {
		this.applicationContext = ctxt;
	}

	@Required
	public final void setDeviceService(final ComDeviceService service) {
		this.deviceService = service;
	}

	@Required
	public final void setClientService(final ClientService service) {
		this.clientService = service;
	}

	@Required
	public final void setUserFormDao(final UserFormDao dao) {
		this.userFormDao = dao;
	}

	@Required
	public final void setExtensibleUIDService(final ExtensibleUIDService service) {
		this.extensibleUIDService = service;
	}

	@Required
	public final void setCompanyDao(final ComCompanyDao dao) {
		this.companyDao = dao;
	}

	@Required
	public final void setRecipientDao(final ComRecipientDao dao) {
		this.recipientDao = dao;
	}

	@Required
	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}

	@Required
	public void setMailingContentTypeCache(MailingContentTypeCache mailingContentTypeCache) {
		this.mailingContentTypeCache = mailingContentTypeCache;
	}
}
