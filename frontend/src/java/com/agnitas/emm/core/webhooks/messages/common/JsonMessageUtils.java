/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.webhooks.messages.common;

import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.emm.core.webhooks.common.WebhookMessageConstants;
import com.agnitas.emm.common.UserStatus;
import org.json.JSONObject;

/**
 * Convenience methods for building JSON documents for webhook messages.
 */
public final class JsonMessageUtils {

	/**
	 * Adds the element containing a mailing ID.
	 * 
	 * @param json JSON object
	 * @param id mailing ID
	 */
	public static final void addMailingId(final JSONObject json, final int id) {
		json.put(WebhookMessageConstants.MAILING_ID_JSON_ELEMENT_NAME, id);
	}

	/**
	 * Adds the element containing a recipient ID.
	 * If given ID is 0, then {@link WebhookMessageConstants#UNTRACKED_RECIPIENT_ID_VALUE} ({@value WebhookMessageConstants#UNTRACKED_RECIPIENT_ID_VALUE}) is set.
	 * 
	 * @param json JSON object
	 * @param profileFields profile field data
	 */
	public static void addRecipientIdAndData(final JSONObject json, final WebhookRecipientData profileFields) {
		if(profileFields.getRecipientId() > 0 && !profileFields.isTrackingVeto()) {
			json.put(WebhookMessageConstants.RECIPIENT_ID_JSON_ELEMENT_NAME, profileFields.getRecipientId());
			
			json.put(WebhookMessageConstants.RECIPIENT_DATA_JSON_ELEMENT_NAME, profileFields.getProfileFields());
		} else {
			json.put(WebhookMessageConstants.RECIPIENT_ID_JSON_ELEMENT_NAME, WebhookMessageConstants.UNTRACKED_RECIPIENT_ID_VALUE);
		}
	}

	/**
	 * Adds the element containing a link ID.
	 * 
	 * @param json JSON object
	 * @param id link ID
	 */
	public static void addLinkId(final JSONObject json, final int id) {
		json.put(WebhookMessageConstants.LINK_ID_JSON_ELEMENT_NAME, id);
	}

	public static void addMailinglistId(final JSONObject json, final int mailinglistID) {
		json.put(WebhookMessageConstants.MAILINGLIST_ID_JSON_ELEMENT_NAME, mailinglistID);
	}

	public static void addMediaType(final JSONObject json, final MediaTypes mediatypeOrNull) {
		json.put(
				WebhookMessageConstants.MEDIATYPE_JSON_ELEMENT_NAME, 
				mediatypeOrNull != null 
					? mediatypeOrNull.getWebhookIdentifier() 
					: null);
	}

	public static void addUserStatus(final JSONObject json, final UserStatus userStatus) {
		switch(userStatus) {
		case AdminOut:
			json.put(WebhookMessageConstants.USER_STATUS_JSON_ELEMENT_NAME, userStatus.getWebhookIdentifier());
			json.put(WebhookMessageConstants.OPTOUT_BY_JSON_ELEMENT_NAME, WebhookMessageConstants.OPTOUT_BY_ADMIN_ELEMENT_VALUE);
			break;
			
		case UserOut:
			json.put(WebhookMessageConstants.USER_STATUS_JSON_ELEMENT_NAME, userStatus.getWebhookIdentifier());
			json.put(WebhookMessageConstants.OPTOUT_BY_JSON_ELEMENT_NAME, WebhookMessageConstants.OPTOUT_BY_RECIPIENT_ELEMENT_VALUE);
			break;
			
		default:
			json.put(WebhookMessageConstants.USER_STATUS_JSON_ELEMENT_NAME, userStatus.getWebhookIdentifier());
			break;
		}
	}

}
