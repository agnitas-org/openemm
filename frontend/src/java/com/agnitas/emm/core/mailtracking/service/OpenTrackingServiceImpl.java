/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailtracking.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.agnitas.emm.core.commons.uid.ExtensibleUID;
import com.agnitas.emm.core.mailing.cache.MailingContentTypeCache;
import com.agnitas.emm.core.mailtracking.service.TrackingVetoHelper.TrackingLevel;
import com.agnitas.emm.core.mailtracking.service.event.OnMailOpenedHandler;
import com.agnitas.emm.core.mobile.bean.DeviceClass;
import com.agnitas.dao.OnepixelDao;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Implementation of {@link OpenTrackingService} interface.
 */
public final class OpenTrackingServiceImpl implements OpenTrackingService {

	private static final Logger logger = LogManager.getLogger(OpenTrackingServiceImpl.class);
	
	/** DAO for handling onepixel events. */
	private OnepixelDao onepixelDao;
	
	/** Service for accessing configuration data. */
	private ConfigService configService;
	
	/** Cache for content types of mailings. */
	private MailingContentTypeCache mailingContentTypeCache;

	private final List<OnMailOpenedHandler> mailingOpenedHandlerList;

	public OpenTrackingServiceImpl() {
		this.mailingOpenedHandlerList = new ArrayList<>();
	}
	
	@Override
	public void trackOpening(final ExtensibleUID uid, final boolean doNotTrackRecipient, final String remoteAddr, final DeviceClass deviceClass, final int deviceID, final int clientID) {
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
	
	private void notifyOnMailingOpenedHandlers(final int companyID, final int mailingID, final int customerID) {
		assert this.mailingOpenedHandlerList != null; // Ensured by constructor and "final" modifier
		
		for(final OnMailOpenedHandler handler : this.mailingOpenedHandlerList) {
			try {
				handler.handleMailOpened(companyID, mailingID, customerID);
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
	public void setOnepixelDao(final OnepixelDao dao) {
		this.onepixelDao = Objects.requireNonNull(dao, "Onepixel DAO cannot be null");
	}
	
	/**
	 * Sets the service accessing configuration data.
	 * 
	 * @param service service accessing configuration data
	 */
	public void setConfigService(final ConfigService service) {
		this.configService = Objects.requireNonNull(service, "Config service cannot be null");
	}
	
	/**
	 * Set cache for mailing content types.
	 * 
	 * @param cache cache for mailing content types
	 */
	public void setMailingContentTypeCache(final MailingContentTypeCache cache) {
		this.mailingContentTypeCache = Objects.requireNonNull(cache, "Content type cache cannot be null");
	}
	
	/**
	 * Set list of handlers for "mailingOpened" event.
	 * 
	 * @param handlers list of event handlers
	 */
	// Not @Required. List is initialized as empty list by default and this list can be left empty.
	public void setOnMailingOpenedHandlers(final List<OnMailOpenedHandler> handlers) {
		this.mailingOpenedHandlerList.clear();
		
		if(handlers != null) {
			this.mailingOpenedHandlerList.addAll(handlers);
		}
	}
}
