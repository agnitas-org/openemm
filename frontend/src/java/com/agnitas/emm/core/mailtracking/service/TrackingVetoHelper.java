/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailtracking.service;

import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;

import com.agnitas.beans.MailingContentType;
import com.agnitas.emm.core.commons.uid.ComExtensibleUID;
import com.agnitas.emm.core.mailing.cache.MailingContentTypeCache;

/**
 * Utility class for Tracking Veto feature.
 */
public final class TrackingVetoHelper {
	
	/** 
	 * Level of tracking.
	 */
	public enum TrackingLevel {
		/** Personal tracking .*/
		PERSONAL,
		
		/** Anonymous tracking. */
		ANONYMOUS
	}

	/**
	 * Computes the tracking level for a customer depending on UID and configuration.
	 * 
	 * @param uid UID
	 * @param configService configuration service
	 * @param mailingContentTypeCache cache holding the type of mailing content
	 * 
	 * @return tracking level
	 */
	public static final TrackingLevel computeTrackingLevel(final ComExtensibleUID uid, final boolean doNotTrackRecipient, final ConfigService configService, final MailingContentTypeCache mailingContentTypeCache) {
		// If personalized tracking for transaction mailings is enabled, check type of mailing content
		if (configService.getBooleanValue(ConfigValue.TrackingVetoAllowTransactionTracking, uid.getCompanyID())) {
			final MailingContentType type = mailingContentTypeCache.getItem(uid.getMailingID(), uid.getCompanyID());
			
			// For transaction mailing allow personalized tracking
			if (type == MailingContentType.transaction) {
				return TrackingLevel.PERSONAL;
			}
		}
		
		// Check if personal tracking is disabled for the whole company. 
		if (configService.getBooleanValue(ConfigValue.AnonymizeAllRecipients, uid.getCompanyID())) {
			return TrackingLevel.ANONYMOUS;
		}

		// At least, check Tracking Veto settings of the recipient
		return doNotTrackRecipient ? TrackingLevel.ANONYMOUS : TrackingLevel.PERSONAL;
	}

}
