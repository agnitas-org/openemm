/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.userform.trackablelinks.web;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Objects;

import org.agnitas.beans.BaseTrackableLink;
import org.agnitas.beans.Recipient;
import org.agnitas.beans.factory.RecipientFactory;
import org.agnitas.emm.core.commons.uid.ExtensibleUIDConstants;
import org.agnitas.emm.core.commons.uid.ExtensibleUIDService;
import org.agnitas.emm.core.commons.uid.parser.exception.DeprecatedUIDVersionException;
import org.agnitas.emm.core.commons.uid.parser.exception.InvalidUIDException;
import org.agnitas.emm.core.commons.uid.parser.exception.UIDParseException;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.recipient.service.RecipientService;
import org.agnitas.util.AgnUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.agnitas.beans.LinkProperty;
import com.agnitas.dao.ComRecipientDao;
import com.agnitas.emm.core.commons.uid.ComExtensibleUID;
import com.agnitas.emm.core.mailing.cache.MailingContentTypeCache;
import com.agnitas.emm.core.mailtracking.service.TrackingVetoHelper;
import com.agnitas.emm.core.mailtracking.service.TrackingVetoHelper.TrackingLevel;
import com.agnitas.emm.core.mobile.bean.DeviceClass;
import com.agnitas.emm.core.mobile.service.ClientService;
import com.agnitas.emm.core.mobile.service.ComDeviceService;
import com.agnitas.emm.core.trackablelinks.dao.FormTrackableLinkDao;
import com.agnitas.userform.trackablelinks.bean.ComTrackableUserFormLink;
import com.agnitas.util.LinkUtils;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * redirect servlet for links within a user form
 */
public class ComRdirUserForm extends HttpServlet {

	private static final Logger logger = LogManager.getLogger(ComRdirUserForm.class);
	private static final long serialVersionUID = -83951043191964625L;

	// ----------------------------------------------------------------------------------------------------------------
	// Dependency Injection

	private ConfigService configService;
	private FormTrackableLinkDao formTrackableLinkDao;
	private ExtensibleUIDService extensibleUIDService;
	private ComRecipientDao comRecipientDao;
	private ComDeviceService comDeviceService;
	private ClientService clientService;
	private MailingContentTypeCache mailingContentTypeCache;
	
	private RecipientFactory recipientFactory;
	private RecipientService recipientService;

	@Required
	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}

	private ConfigService getConfigService() {
		if (configService == null) {
			ApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
			configService = (ConfigService) applicationContext.getBean("ConfigService");
		}
		return configService;
	}

	public void setFormTrackableLinkDao(FormTrackableLinkDao formTrackableLinkDao) {
		this.formTrackableLinkDao = formTrackableLinkDao;
	}

	private FormTrackableLinkDao getUserFormDao() {
		if (formTrackableLinkDao == null) {
			ApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
			formTrackableLinkDao = applicationContext.getBean("FormTrackableLinkDao", FormTrackableLinkDao.class);
		}
		return formTrackableLinkDao;
	}
	
	public void setExtensibleUIDService(ExtensibleUIDService extensibleUIDService) {
		this.extensibleUIDService = extensibleUIDService;
	}
	
	private ExtensibleUIDService getExtensibleUIDService() {
		if (extensibleUIDService == null) {
			ApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
			extensibleUIDService = (ExtensibleUIDService) applicationContext.getBean(ExtensibleUIDConstants.SERVICE_BEAN_NAME);
		}
		return extensibleUIDService;
	}

	public void setComRecipientDao(ComRecipientDao comRecipientDao) {
		this.comRecipientDao = comRecipientDao;
	}

	private ComRecipientDao getRecipientDao() {
		if (comRecipientDao == null) {
			ApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
			comRecipientDao = (ComRecipientDao) applicationContext.getBean("RecipientDao");
		}
		return comRecipientDao;
	}

	public void setComDeviceService(ComDeviceService comDeviceService) {
		this.comDeviceService = comDeviceService;
	}

	private ComDeviceService getDeviceService() {
		if (comDeviceService == null) {
			ApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
			comDeviceService = applicationContext.getBean(ComDeviceService.class);
		}
		return comDeviceService;
	}

	public void setClientService(ClientService clientService) {
		this.clientService = clientService;
	}

	private ClientService getClientService() {
		if (clientService == null) {
			ApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
			clientService = applicationContext.getBean(ClientService.class);
		}
		return clientService;
	}
	
	@Required
	public void setMailingContentTypeCache(MailingContentTypeCache mailingContentTypeCache) {
		this.mailingContentTypeCache = mailingContentTypeCache;
	}

	private MailingContentTypeCache getMailingContentTypeCache() {
		if (mailingContentTypeCache == null) {
			ApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
			mailingContentTypeCache = (MailingContentTypeCache) applicationContext.getBean("MailingContentTypeCache");
		}
		return mailingContentTypeCache;
	}
	
	private RecipientFactory getRecipientFactory() {
		if(this.recipientFactory == null) {
			this.recipientFactory = WebApplicationContextUtils.getWebApplicationContext(getServletContext()).getBean("RecipientFactory", RecipientFactory.class);
		}
		
		return this.recipientFactory;
	}
	
	private RecipientService getRecipientService() {
		if(this.recipientService == null) {
			this.recipientService = WebApplicationContextUtils.getWebApplicationContext(getServletContext()).getBean("recipientService", RecipientService.class);
		}
		
		return this.recipientService;
	}

	// ----------------------------------------------------------------------------------------------------------------
	// Business Logic
	
	/**
	 * Service-Method, gets called every time a User calls the servlet
	 */
	@Override
	public void service(HttpServletRequest request, HttpServletResponse res) throws IOException, ServletException {
		logger.debug("Starting ComRdirUserForm");

		// url_id in rdir_url_userform_tbl
		String paramLinkID = request.getParameter("lid");
		if (paramLinkID == null) {
			logger.error("ComRdirUserForm: link id is null");
			return;
		}
		
		String userAgentParam = StringUtils.trimToEmpty(request.getHeader("User-Agent"));
		int deviceID = getDeviceService().getDeviceId(userAgentParam);
		int clientID = getClientService().getClientId(userAgentParam);

		if (deviceID == ComDeviceService.DEVICE_BLACKLISTED_NO_SERVICE) {
			res.setContentType("text/plain");
			res.getOutputStream().write("No service".getBytes("UTF-8"));
		}
		
		DeviceClass deviceClass = getDeviceService().getDeviceClassForStatistics(deviceID);
		
		ComExtensibleUID uid = null;

		/*
		 *  In all cases I found in code, the parameter is named "agnUID".
		 *  Why the parameter here is named "uid" I don't know.
		 *  I also don't know, if there is a link using "uid" instead of "agnUID", 
		 *  so I will support both variants, preferring "agnUID" over "uid". 
		 */
		final String paramUid = request.getParameter("agnUID") != null 
				? request.getParameter("agnUID")
				: request.getParameter("uid");

		if (StringUtils.isNotBlank(paramUid)) {
			try {
				uid = getExtensibleUIDService().parse(paramUid);
			} catch (DeprecatedUIDVersionException e) {
				if(logger.isInfoEnabled()) {
					logger.info("deprecated UID version: " + paramUid);
				}
			} catch (UIDParseException e) {
				if(logger.isInfoEnabled()) {
					logger.info("error parsing UID: " + paramUid, e);
				}
			} catch (InvalidUIDException e) {
				if(logger.isInfoEnabled()) {
					logger.info("Invalid UID: " + paramUid, e);
				}
			}
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
		

		ComTrackableUserFormLink comTrackableUserFormLink;
		try {
			comTrackableUserFormLink = getUserFormDao().getUserFormTrackableLink(Integer.parseInt(paramLinkID));
		} catch (Exception e) {
			throw new ServletException(e);
		}

		if (deviceID != ComDeviceService.DEVICE_BLACKLISTED_NO_COUNT) {
			/*
			 * Set default tracking level:
			 *   - When no recipient can be identified (customer ID <= 0):
			 *   	We track all data (here: IP address only, because recipient ID is 0)
			 *   - When a recipient can be identified (customer ID > 0):
			 *   	The default behavior is anonymous tracking (customer ID and IP address are not tracked). This will be overwritten by
			 *      recipient setting ("sys_tracking_veto" = 1), when the customer ID is known.
			 */
			TrackingLevel trackingLevel = uid != null && uid.getCustomerID() > 0 ? TrackingLevel.ANONYMOUS : TrackingLevel.PERSONAL;
			
			// The available mailing_id and customer_id should be logged in any case of a trackable link
			if (comTrackableUserFormLink.getUsage() == BaseTrackableLink.TRACKABLE_YES
					|| comTrackableUserFormLink.getUsage() == BaseTrackableLink.TRACKABLE_YES_WITH_MAILING_INFO
					|| comTrackableUserFormLink.getUsage() == BaseTrackableLink.TRACKABLE_YES_WITH_MAILING_AND_USER_INFO) {
				
				if(uid != null && uid.getCustomerID() > 0) {
					final Recipient recipient = getRecipientFactory().newRecipient(uid.getCompanyID());

					recipient.setCustomerID(uid.getCustomerID());
					recipient.setCustParameters(getRecipientService().getCustomerDataFromDb(uid.getCompanyID(), uid.getCustomerID(), recipient.getDateFormat()));

					trackingLevel = TrackingVetoHelper.computeTrackingLevel(uid, recipient.isDoNotTrackMe(), getConfigService(), getMailingContentTypeCache());
				}

				getUserFormDao().logUserFormTrackableLinkClickInDB(
						comTrackableUserFormLink,
						(trackingLevel == TrackingLevel.ANONYMOUS) ? Integer.valueOf(0) : customerIdInt,
						mailingIdInt,
						(trackingLevel == TrackingLevel.ANONYMOUS) ? null : request.getRemoteAddr(),
						deviceClass,
						deviceID,
						clientID);
			}
		}

		// Create redirect Url with extensions
		if (comTrackableUserFormLink.getProperties() != null && comTrackableUserFormLink.getProperties().size() > 0) {
			res.sendRedirect(createDirectLinkWithOptionalExtensions(uid, comTrackableUserFormLink));
		} else {
			res.sendRedirect(comTrackableUserFormLink.getFullUrl());
		}

		logger.debug("Finished ComRdirUserForm");
	}

	private String createDirectLinkWithOptionalExtensions(final ComExtensibleUID uid, final ComTrackableUserFormLink comTrackableUserFormLink) throws UnsupportedEncodingException {
		String linkString = comTrackableUserFormLink.getFullUrl();
		Map<String, Object> cachedRecipientData = null;
		for (LinkProperty linkProperty : comTrackableUserFormLink.getProperties()) {
			if (LinkUtils.isExtension(linkProperty)) {
				String propertyValue = linkProperty.getPropertyValue();
				if (propertyValue != null && propertyValue.contains("##")) {
					if (cachedRecipientData == null && uid != null) {
						try {
							cachedRecipientData = getRecipientDao().getCustomerDataFromDb(uid.getCompanyID(), uid.getCustomerID());
							cachedRecipientData.put("mailing_id", uid.getMailingID());
						} catch (Throwable e) {
							logger.error("Error occured: " + e.getMessage(), e);
						}
					}
					
					// Replace customer and form placeholders
                    propertyValue = AgnUtils.replaceHashTags(propertyValue, cachedRecipientData);
				}
				// Extend link properly (watch out for html-anchors etc.)
				linkString = AgnUtils.addUrlParameter(linkString, linkProperty.getPropertyName(), propertyValue == null ? "" : propertyValue, "UTF-8");
			}
		}
		return linkString;
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
