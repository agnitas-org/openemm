/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.webhooks.common;

/**
 * Collection of constants regarding webhook messages.
 */
public interface WebhookMessageConstants {

	/** Name of JSON element to store a mailing ID. */
	public static final String MAILING_ID_JSON_ELEMENT_NAME = "mailing_id";

	/** Name of JSON element to store a recipient ID. */
	public static final String RECIPIENT_ID_JSON_ELEMENT_NAME = "recipient_id";

	/** Name of JSON element to store profile fields data. */
	public static final String RECIPIENT_DATA_JSON_ELEMENT_NAME = "recipient_data";

	/** Name of JSON element to store a link ID. */
	public static final String LINK_ID_JSON_ELEMENT_NAME = "link_id";
	
	/** Name of JSON element to store a mailinglist ID. */
	public static final String MAILINGLIST_ID_JSON_ELEMENT_NAME = "mailinglist_id";

	/** Value for recipient ID, if recipient is not tracked. */
	public static final String UNTRACKED_RECIPIENT_ID_VALUE = "not_tracked";

	public static final String MEDIATYPE_JSON_ELEMENT_NAME = "mediatype";

	public static final String USER_STATUS_JSON_ELEMENT_NAME = "status";

	public static final String OPTOUT_BY_JSON_ELEMENT_NAME = "opt_out_by";

	public static final String OPTOUT_BY_ADMIN_ELEMENT_VALUE = "admin";

	public static final String OPTOUT_BY_RECIPIENT_ELEMENT_VALUE = "recipient";

}
