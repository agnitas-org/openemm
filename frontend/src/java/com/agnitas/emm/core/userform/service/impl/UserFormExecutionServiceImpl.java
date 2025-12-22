/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.userform.service.impl;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.agnitas.beans.BaseTrackableLink;
import com.agnitas.beans.LinkProperty;
import com.agnitas.beans.Recipient;
import com.agnitas.beans.factory.RecipientFactory;
import com.agnitas.dao.CompanyDao;
import com.agnitas.dao.RecipientDao;
import com.agnitas.dao.UserFormDao;
import com.agnitas.emm.core.action.service.EmmActionOperationErrors;
import com.agnitas.emm.core.commons.uid.ExtensibleUID;
import com.agnitas.emm.core.commons.uid.ExtensibleUIDService;
import com.agnitas.emm.core.commons.uid.parser.exception.DeprecatedUIDVersionException;
import com.agnitas.emm.core.commons.uid.parser.exception.InvalidUIDException;
import com.agnitas.emm.core.commons.uid.parser.exception.UIDParseException;
import com.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.core.mailing.cache.MailingContentTypeCache;
import com.agnitas.emm.core.mailtracking.service.TrackingVetoHelper;
import com.agnitas.emm.core.mailtracking.service.TrackingVetoHelper.TrackingLevel;
import com.agnitas.emm.core.mobile.bean.DeviceClass;
import com.agnitas.emm.core.mobile.service.ClientService;
import com.agnitas.emm.core.mobile.service.DeviceService;
import com.agnitas.emm.core.recipient.service.RecipientService;
import com.agnitas.emm.core.trackablelinks.dao.FormTrackableLinkDao;
import com.agnitas.emm.core.userform.exception.BlacklistedDeviceException;
import com.agnitas.emm.core.userform.service.UserFormExecutionResult;
import com.agnitas.emm.core.userform.service.UserFormExecutionService;
import com.agnitas.emm.core.velocity.Constants;
import com.agnitas.exception.FormNotFoundException;
import com.agnitas.userform.bean.UserForm;
import com.agnitas.userform.trackablelinks.bean.TrackableUserFormLink;
import com.agnitas.util.AgnUtils;
import com.agnitas.util.LinkUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public final class UserFormExecutionServiceImpl implements UserFormExecutionService, ApplicationContextAware {
	
	private static final Logger logger = LogManager.getLogger(UserFormExecutionServiceImpl.class);

	private ConfigService configService;
	private DeviceService deviceService;
	private ClientService clientService;
	private UserFormDao userFormDao;
	private ExtensibleUIDService extensibleUIDService;
	private CompanyDao companyDao;
	private RecipientDao recipientDao;
	private MailingContentTypeCache mailingContentTypeCache;
	private FormTrackableLinkDao trackableLinkDao;
	
	private RecipientFactory recipientFactory;
	private RecipientService recipientService;

	private ApplicationContext applicationContext;

	@Override
	public UserFormExecutionResult executeForm(int companyID, String formName, HttpServletRequest request, CaseInsensitiveMap<String, Object> params, boolean useSession) throws BlacklistedDeviceException {
		populateRequestParametersAsVelocityParameters(request, params);

		int deviceID = deviceService.getDeviceId(request.getHeader("User-Agent"));
		DeviceClass deviceClass = deviceService.getDeviceClassForStatistics(deviceID);
		if (deviceID == DeviceService.DEVICE_BLACKLISTED_NO_SERVICE) {
			throw new BlacklistedDeviceException();
		}

		final int mobileID = deviceClass == DeviceClass.MOBILE ? deviceID : -1;
		if (mobileID > 0) {
			populateMobileDeviceParametersAsVelocityParameters(mobileID, params, request);
		}

		final int clientID = clientService.getClientId(request.getHeader("User-Agent"));
		final UserForm userForm = loadUserForm(formName, companyID);
		final EmmActionOperationErrors actionOperationErrors = populateEmmActionErrorsAsVelocityParameters(params);

		try {
			final ExtensibleUID uid = processUID(companyID, request, params, useSession);
			
			logFormAccess(userForm, uid, request.getRemoteAddr(), deviceID, deviceClass, clientID);
			
			populateFormPropertiesAsVelocityParameters(userForm, params);
	
			return doExecuteForm(userForm, actionOperationErrors, params, request);
		} catch(Exception e) {
			logger.error(String.format("Showing error form due to exception (company %d, form: %s)", companyID, formName), e);
			
			return doExecuteErrorForm(userForm, actionOperationErrors, params);
		}
	}

	private void populateFormPropertiesAsVelocityParameters(UserForm userForm, CaseInsensitiveMap<String, Object> params) {
		params.put("formID", userForm.getId());
	}

	private EmmActionOperationErrors populateEmmActionErrorsAsVelocityParameters(CaseInsensitiveMap<String, Object> params) {
		final EmmActionOperationErrors actionOperationErrors = new EmmActionOperationErrors();
		params.put(Constants.ACTION_OPERATION_ERRORS_CONTEXT_NAME, actionOperationErrors);
		
		return actionOperationErrors;
	}

	/**
	 * information from a given url. Parses an url and returns the retrieved values in a hash.
	 * 
	 * @param req ServletRequest, used to get the Session.
	 * @param params HashMap to store the retrieved values in.
	 * @param useSession also store the result in the session if this is not 0.
	 * @throws InvalidUIDException 
	 * @throws UIDParseException 
	 * @throws DeprecatedUIDVersionException 
	 */
	@SuppressWarnings("unchecked")
	private final ExtensibleUID processUID(final int companyIdRequestParam, HttpServletRequest req, Map<String, Object> params, boolean useSession) throws DeprecatedUIDVersionException, UIDParseException, InvalidUIDException {
		String uidString = getUidStringFromRequest(req);
		ExtensibleUID uidObject = decodeUidStringWithException(uidString);

		if (Objects.nonNull(uidObject)) {
			if (companyIdRequestParam == uidObject.getCompanyID()) {
				params.put("customerID", uidObject.getCustomerID());
				params.put("mailingID", uidObject.getMailingID());
				params.put("urlID", uidObject.getUrlID());
				params.put("agnUID", uidString);
				params.put("companyID", uidObject.getCompanyID());
				params.put("locale", req.getLocale());
				params.put("_uid", uidObject);

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
					params.putAll((Map<String, Object>) req.getSession().getAttribute("agnFormParams")); // suppress warning for this cast
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
	private ExtensibleUID decodeUidString(String uidString) {
		try {
			return decodeUidStringWithException(uidString);
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
		
		return null;
	}

	private ExtensibleUID decodeUidStringWithException(String uidString) throws DeprecatedUIDVersionException, UIDParseException, InvalidUIDException {
		return StringUtils.isNotBlank(uidString)
				? extensibleUIDService.parse(uidString)
				: null;
	}

	private String getUidStringFromRequest(HttpServletRequest req) {
		String uidString = req.getParameter("agnUID");
		if (StringUtils.isBlank(uidString)) {
			uidString = req.getParameter("uid");
		}
		return uidString;
	}

	private void logFormAccess(UserForm userForm, ExtensibleUID uid, String remoteAddress, int deviceID, DeviceClass deviceClass, int clientID) {
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
		if (deviceID != DeviceService.DEVICE_BLACKLISTED_NO_COUNT) {
			final TrackableUserFormLink formStatisticsDummyLink = trackableLinkDao.getDummyUserFormTrackableLinkForStatisticCount(userForm.getCompanyID(), userForm.getId());
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
	
	private UserForm loadUserForm(String formName, int companyID) {
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
	
	private UserFormExecutionResult doExecuteForm(UserForm userForm, EmmActionOperationErrors actionOperationErrors, CaseInsensitiveMap<String, Object> params, HttpServletRequest request) {
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
	
	private UserFormExecutionResult doExecuteErrorForm(UserForm userForm, EmmActionOperationErrors actionOperationErrors, CaseInsensitiveMap<String, Object> params) {
		String responseContent = userForm.evaluateErrorForm(applicationContext, params, actionOperationErrors);
		String responseMimeType = userForm.getErrorMimetype();

		final String uidString = (String) params.get("agnUID");
		responseContent = addRedirectLinks(responseContent, uidString, userForm);
		
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
	 */
	protected String addRedirectLinks(String content, String uidString, UserForm userForm) {
		// create the redirect link for each trackable link or use extensions on direct link
		Map<String, TrackableUserFormLink> trackableLinks = trackableLinkDao.getUserFormTrackableLinks(userForm.getId(), userForm.getCompanyID());
		for (TrackableUserFormLink link : trackableLinks.values()) {
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
	
	private String createDirectLinkWithOptionalExtensions(String uidString, TrackableUserFormLink trackableUserFormLink) {
		String linkString = trackableUserFormLink.getFullUrl();
		CaseInsensitiveMap<String, Object> cachedRecipientData = null;
		for (LinkProperty linkProperty : trackableUserFormLink.getProperties()) {
			if (LinkUtils.isExtension(linkProperty)) {
				String propertyValue = linkProperty.getPropertyValue();
				if (StringUtils.contains(propertyValue, "##")) {
					if (cachedRecipientData == null && StringUtils.isNotBlank(uidString)) {
						final ExtensibleUID uid = decodeUidString(uidString);
						cachedRecipientData = recipientDao.getCustomerDataFromDb(uid.getCompanyID(), uid.getCustomerID());
						cachedRecipientData.put("mailing_id", uid.getMailingID());
					}
					// Replace customer and form placeholders
                    propertyValue = AgnUtils.replaceHashTags(propertyValue, cachedRecipientData);
				}
				// Extend link properly (watch out for html-anchors etc.)
				linkString = AgnUtils.addUrlParameter(linkString, linkProperty.getPropertyName(), StringUtils.defaultString(propertyValue), StandardCharsets.UTF_8);
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

		return responseContent;
	}

	@Override
	public void setApplicationContext(ApplicationContext ctxt) {
		this.applicationContext = ctxt;
	}

	public final void setDeviceService(final DeviceService service) {
		this.deviceService = service;
	}

	public final void setClientService(final ClientService service) {
		this.clientService = service;
	}

	public final void setUserFormDao(final UserFormDao dao) {
		this.userFormDao = dao;
	}

	public final void setExtensibleUIDService(final ExtensibleUIDService service) {
		this.extensibleUIDService = service;
	}

	public final void setCompanyDao(final CompanyDao dao) {
		this.companyDao = dao;
	}

	public final void setRecipientDao(final RecipientDao dao) {
		this.recipientDao = dao;
	}

	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}

	public void setMailingContentTypeCache(MailingContentTypeCache mailingContentTypeCache) {
		this.mailingContentTypeCache = mailingContentTypeCache;
	}

	public void setTrackableLinkDao(FormTrackableLinkDao trackableLinkDao) {
		this.trackableLinkDao = trackableLinkDao;
	}
	
	public final void setRecipientFactory(final RecipientFactory factory) {
		this.recipientFactory = Objects.requireNonNull(factory, "recipientFactory is null");
	}
	
	public final void setRecipientService(final RecipientService service) {
		this.recipientService = Objects.requireNonNull(service, "recipientService is null");
	}
}
