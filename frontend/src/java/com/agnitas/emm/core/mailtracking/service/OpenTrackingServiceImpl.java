/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailtracking.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.agnitas.beans.Recipient;
import org.agnitas.beans.factory.RecipientFactory;
import org.agnitas.dao.OnepixelDao;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.recipient.service.RecipientService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.emm.core.commons.uid.ComExtensibleUID;
import com.agnitas.emm.core.commons.uid.UIDFactory;
import com.agnitas.emm.core.mailing.cache.MailingContentTypeCache;
import com.agnitas.emm.core.mailtracking.service.TrackingVetoHelper.TrackingLevel;
import com.agnitas.emm.core.mailtracking.service.event.OnMailingOpenedHandler;
import com.agnitas.emm.core.mobile.bean.DeviceClass;

/**
 * Implementation of {@link OpenTrackingService} interface.
 */
public final class OpenTrackingServiceImpl implements OpenTrackingService {

	/** The logger. */
	private static final transient Logger logger = LogManager.getLogger(OpenTrackingServiceImpl.class);
	
	/** DAO for handling onepixel events. */
	private OnepixelDao onepixelDao;
	
	/** Service for accessing configuration data. */
	private ConfigService configService;
	
	/** Factory for creating recipients. */
	private RecipientFactory recipientFactory;
	
	/** Cache for content types of mailings. */
	private MailingContentTypeCache mailingContentTypeCache;

	private RecipientService recipientService;
	
	private final List<OnMailingOpenedHandler> mailingOpenedHandlerList;

	public OpenTrackingServiceImpl() {
		this.mailingOpenedHandlerList = new ArrayList<>();
	}
	
	@Override
	public final void trackOpening(final int companyID, final int customerID, final int mailingID, final String remoteAddr, final DeviceClass deviceClass, final int deviceID, final int clientID) {
		final Recipient recipient = recipientFactory.newRecipient(companyID);

		recipient.setCustomerID(customerID);
		recipient.setCustParameters(recipientService.getCustomerDataFromDb(companyID, customerID, recipient.getDateFormat()));
		
		final int licenseID = configService.getLicenseID();
		
		final ComExtensibleUID uid = UIDFactory.from(licenseID, companyID, customerID, mailingID);

		trackOpening(uid, recipient.isDoNotTrackMe(), remoteAddr, deviceClass, deviceID, clientID);
	}
	
	@Override
	public final void trackOpening(final ComExtensibleUID uid, final boolean doNotTrackRecipient, final String remoteAddr, final DeviceClass deviceClass, final int deviceID, final int clientID) {
		if(uid == null) {
			logger.warn("No UID", new Exception("No UID"));
		} else {
			final TrackingLevel trackingLevel = TrackingVetoHelper.computeTrackingLevel(uid, doNotTrackRecipient, configService, mailingContentTypeCache);
			
			if(trackingLevel == TrackingLevel.ANONYMOUS) {
				if(logger.isInfoEnabled()) {
					logger.info(String.format("Customer %d disagreed opening tracking", uid.getCustomerID()));
				}
			}
			
			final int trackedRecipientID = (trackingLevel == TrackingLevel.ANONYMOUS) ? 0 : uid.getCustomerID();
			
			final boolean result = onepixelDao.writePixel(uid.getCompanyID(), trackedRecipientID, uid.getMailingID(), ((trackingLevel == TrackingLevel.ANONYMOUS) ? null : remoteAddr), deviceClass, deviceID, clientID);
			
			if(!result) {
				logger.warn(String.format("Could not track opening of mailing %d for customer %d (company ID %d)", uid.getMailingID(), uid.getCustomerID(), uid.getCompanyID()));
			}
			
			notifyOnMailingOpenedHandlers(uid.getCompanyID(), uid.getMailingID(), trackedRecipientID);
		}
	}
	
	private final void notifyOnMailingOpenedHandlers(final int companyID, final int mailingID, final int customerID) {
		assert this.mailingOpenedHandlerList != null; // Ensured by constructor and "final" modifier
		
		for(final OnMailingOpenedHandler handler : this.mailingOpenedHandlerList) {
			try {
				handler.handleMailingOpened(companyID, mailingID, customerID);
			} catch(final Exception e) {
				logger.warn(
						String.format(
								"OnMailingOpenedHandler of type '%s' threw an exception (company ID %d, mailing ID %d, customer ID %d)",
								handler.getClass().getCanonicalName(),
								companyID,
								mailingID,
								customerID), 
						e);
			}
		}
	}

	/**
	 * Set DAO for onepixel tracking.
	 * 
	 * @param dao DAO for onepixel tracking
	 */
	@Required
	public final void setOnepixelDao(final OnepixelDao dao) {
		this.onepixelDao = Objects.requireNonNull(dao, "Onepixel DAO cannot be null");
	}
	
	/**
	 * Sets the service accessing configuration data.
	 * 
	 * @param service service accessing configuration data
	 */
	@Required
	public final void setConfigService(final ConfigService service) {
		this.configService = Objects.requireNonNull(service, "Config service cannot be null");
	}
	
	/**
	 * Sets the factory creating new recipients.
	 * 
	 * @param factory factory creating new recipients
	 */
	@Required
	public final void setRecipientFactory(final RecipientFactory factory) {
		this.recipientFactory = Objects.requireNonNull(factory, "Recipient factory cannot be null");
	}
	
	/**
	 * Set cache for mailing content types.
	 * 
	 * @param cache cache for mailing content types
	 */
	@Required
	public final void setMailingContentTypeCache(final MailingContentTypeCache cache) {
		this.mailingContentTypeCache = Objects.requireNonNull(cache, "Content type cache cannot be null");
	}
	
	/**
	 * Set list of handlers for "mailingOpened" event.
	 * 
	 * @param handlers list of event handlers
	 */
	// Not @Required. List is initialized as empty list by default and this list can be left empty.
	public final void setOnMailingOpenedHandlers(final List<OnMailingOpenedHandler> handlers) {
		this.mailingOpenedHandlerList.clear();
		
		if(handlers != null) {
			this.mailingOpenedHandlerList.addAll(handlers);
		}
	}

	@Required
	public void setRecipientService(RecipientService recipientService) {
		this.recipientService = recipientService;
	}
}
