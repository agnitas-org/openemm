/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.userform.service.impl;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

import org.agnitas.beans.BaseTrackableLink;
import org.agnitas.beans.Recipient;
import org.agnitas.beans.factory.RecipientFactory;
import org.agnitas.emm.core.commons.uid.ExtensibleUIDService;
import org.agnitas.emm.core.commons.uid.parser.exception.DeprecatedUIDVersionException;
import org.agnitas.emm.core.commons.uid.parser.exception.InvalidUIDException;
import org.agnitas.emm.core.commons.uid.parser.exception.UIDParseException;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.recipient.service.RecipientService;
import org.agnitas.exceptions.FormNotFoundException;
import org.agnitas.util.AgnUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionErrors;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.agnitas.beans.LinkProperty;
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
import com.agnitas.emm.core.trackablelinks.dao.FormTrackableLinkDao;
import com.agnitas.emm.core.userform.exception.BlacklistedDeviceException;
import com.agnitas.emm.core.userform.service.UserFormExecutionResult;
import com.agnitas.emm.core.userform.service.UserFormExecutionService;
import com.agnitas.userform.bean.UserForm;
import com.agnitas.userform.trackablelinks.bean.ComTrackableUserFormLink;
import com.agnitas.util.LinkUtils;

import jakarta.servlet.http.HttpServletRequest;

public final class UserFormExecutionServiceImpl implements UserFormExecutionService, ApplicationContextAware {
	
	private static final transient Logger logger = LogManager.getLogger(UserFormExecutionServiceImpl.class);

	private ConfigService configService;
	private ComDeviceService deviceService;
	private ClientService clientService;
	private UserFormDao userFormDao;
	private ExtensibleUIDService extensibleUIDService;
	private ComCompanyDao companyDao;
	private ComRecipientDao recipientDao;
	private MailingContentTypeCache mailingContentTypeCache;
	private FormTrackableLinkDao trackableLinkDao;
	
	private RecipientFactory recipientFactory;
	private RecipientService recipientService;

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

		final ComExtensibleUID uid = processUID(companyID, request, params, useSession);
		
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
	private final ComExtensibleUID processUID(final int companyIdRequestParam, HttpServletRequest req, Map<String, Object> params, boolean useSession) {
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
				if(logger.isInfoEnabled()) {
					logger.info(String.format("Deprecated UID version of UID: %s", uidString), e);
				}
			} catch (UIDParseException e) {
				if(logger.isInfoEnabled()) {
					logger.info(String.format("Error parsing UID: %s", uidString), e);
				}
			} catch (InvalidUIDException e) {
				if(logger.isInfoEnabled()) {
					logger.info(String.format("Invalid UID: %s", uidString), e);
				}
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

	private void logFormAccess(final UserForm userForm, final ComExtensibleUID uid, String remoteAddress, final int deviceID, final DeviceClass deviceClass, final int clientID) throws Exception {
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
			final ComTrackableUserFormLink formStatisticsDummyLink = trackableLinkDao.getDummyUserFormTrackableLinkForStatisticCount(userForm.getCompanyID(), userForm.getId());
			if (formStatisticsDummyLink != null) {
				/*
				 * Set default tracking level:
				 *   - When no recipient can be identified (customer ID <= 0):
				 *   	We track all data (here: IP address only, because recipient ID is 0)
				 *   - When a recipient can be identified (customer ID > 0):
				 *   	The default behavior is anonymous tracking (customer ID and IP address are not tracked). This will be overwritten by
				 *      recipient setting ("sys_tracking_veto" = 1), when the customer ID is known.
				 */
				TrackingLevel trackingLevel = uid != null && uid.getCustomerID() > 0 ? TrackingLevel.ANONYMOUS : TrackingLevel.PERSONAL;
				
				if (uid != null) {
					if(uid.getCustomerID() != 0) {
						final Recipient recipient = recipientFactory.newRecipient(uid.getCompanyID());

						recipient.setCustomerID(uid.getCustomerID());
						recipient.setCustParameters(recipientService.getCustomerDataFromDb(uid.getCompanyID(), uid.getCustomerID(), recipient.getDateFormat()));

						trackingLevel = TrackingVetoHelper.computeTrackingLevel(uid, recipient.isDoNotTrackMe(), configService, mailingContentTypeCache);
					}
				}
				
				trackableLinkDao.logUserFormCallInDB(
						userForm.getCompanyID(),
						userForm.getId(),
						formStatisticsDummyLink.getId(),
						mailingIdInt,
						(trackingLevel == TrackingLevel.ANONYMOUS) ? Integer.valueOf(0) : customerIdInt,
						(trackingLevel == TrackingLevel.ANONYMOUS) ? null : remoteAddress,
						deviceClass,
						deviceID,
						clientID);
			}
		}
	}
	
	private UserForm loadUserForm(final String formName, final int companyID) throws Exception {
		final UserForm userForm = userFormDao.getUserFormByName(formName, companyID);

		// Show "form not found" page if form is actually not found or if it's inactive.
		if (userForm == null || !userForm.isActive()) {
			throw new FormNotFoundException(companyID, formName);
		}
		
		return userForm;
	}
	
	private void populateRequestParametersAsVelocityParameters(final HttpServletRequest request, final CaseInsensitiveMap<String, Object> params) {
		// "requestParameters" must be a casesensitive map, for some locations check the keys in entrySet() in a casesensitive way
		Map<String, Object> requestParameters = new HashMap<>();
		requestParameters.putAll(AgnUtils.getReqParameters(request));
		if (params.containsKey("file")) {
			requestParameters.put("file_name", params.get("file_name"));
			requestParameters.put("file_type", params.get("file_type"));
			requestParameters.put("file_size", params.get("file_size"));
			requestParameters.put("file", params.get("file"));
		}
		params.put("requestParameters", requestParameters);
		params.put("_request", request);

		if ((request.getParameter("requestURL") != null) && (request.getParameter("queryString") != null)) {
			params.put("formURL", request.getRequestURL() + "?" + request.getQueryString());
		}
	}
	
	private void populateMobileDeviceParametersAsVelocityParameters(final int mobileID, final CaseInsensitiveMap<String, Object> params, final HttpServletRequest request) {
		params.put("mobileDevice", String.valueOf(mobileID));
		// just to be sure
		request.setAttribute("mobileDevice", String.valueOf(mobileID));
	}
	
	private UserFormExecutionResult doExecuteForm(final UserForm userForm, final EmmActionOperationErrors actionOperationErrors, final CaseInsensitiveMap<String, Object> params, final HttpServletRequest request) throws Exception {
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

		return new UserFormExecutionResult(userForm.getId(), responseContent, responseMimeType);
	}
	
	private String determineSuccessResponseMimeType(final UserForm userForm, final CaseInsensitiveMap<String, Object> params) {
		final String formMimeType = (String)params.get(FORM_MIMETYPE_PARAM_NAME);
	
		
		return StringUtils.isNotBlank(formMimeType)
				? formMimeType
				: userForm.getSuccessMimetype();
	}

	/**
	 * Creates the redirect link to the form content
	 * 
	 * @param content form content
	 * @return
	 * @throws Exception
	 */
	protected String addRedirectLinks(String content, String uidString, UserForm userForm) throws Exception {
		// create the redirect link for each trackable link or use extensions on direct link
		Map<String, ComTrackableUserFormLink> trackableLinks = trackableLinkDao.getUserFormTrackableLinks(userForm.getId(), userForm.getCompanyID());
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
			} else if (CollectionUtils.size(link.getProperties()) > 0) {
				// Use link extensions for direct link
				content = content.replace(replaceFromString, "href=\"" + createDirectLinkWithOptionalExtensions(uidString, link) + "\"");
			}
		}

		return content;
	}
	
	private String createDirectLinkWithOptionalExtensions(String uidString, ComTrackableUserFormLink comTrackableUserFormLink) throws UnsupportedEncodingException {
		String linkString = comTrackableUserFormLink.getFullUrl();
		CaseInsensitiveMap<String, Object> cachedRecipientData = null;
		for (LinkProperty linkProperty : comTrackableUserFormLink.getProperties()) {
			if (LinkUtils.isExtension(linkProperty)) {
				String propertyValue = linkProperty.getPropertyValue();
				if (StringUtils.contains(propertyValue, "##")) {
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
				linkString = AgnUtils.addUrlParameter(linkString, linkProperty.getPropertyName(), StringUtils.defaultString(propertyValue), "UTF-8");
			}
		}
		return linkString;
	}

	/**
	 * Execute the end action of the requested form. Reads the form defined by aForm.getAgnFN() and aForm.getAgnCI() from the database and executes it's end action.
	 * 
	 * @param params a map containing the form values.
	 * @return true==success false==error
	 */
	protected boolean evaluateFormEndAction(HttpServletRequest request, UserForm userForm, Map<String, Object> params, final EmmActionOperationErrors errors) {
		if (userForm == null || userForm.getEndActionID() == 0) {
			return false;
		}

		return userForm.evaluateEndAction(applicationContext, params, errors);
	}

	/**
	 * For user form that requires error handling adds the error messages (including velocity errors) to response context.
	 *
	 * @param userForm
	 * @param params
	 * @param responseContent html content to be sent in response (could be changed inside the method).
	 * @return responseContent
	 */
	protected String handleEndActionErrors(UserForm userForm, CaseInsensitiveMap<String, Object> params, String responseContent) {
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

	@Required
	public void setTrackableLinkDao(FormTrackableLinkDao trackableLinkDao) {
		this.trackableLinkDao = trackableLinkDao;
	}
	
	@Required
	public final void setRecipientFactory(final RecipientFactory factory) {
		this.recipientFactory = Objects.requireNonNull(factory, "recipientFactory is null");
	}
	
	@Required
	public final void setRecipientService(final RecipientService service) {
		this.recipientService = Objects.requireNonNull(service, "recipientService is null");
	}
}
