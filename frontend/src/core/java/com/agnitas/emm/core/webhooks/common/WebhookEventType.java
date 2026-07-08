/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.webhooks.common;

import java.util.Objects;
import java.util.Optional;

/**
 * Enumeration of all webhook events.
 */
public enum WebhookEventType {

	/** Mailing has been opened by recipient. */
	MAIL_OPENED(1, "mailing_opened", true),
	
	/** Link has been clicked. */
	LINK_CLICKED(2, "link_clicked", true),
	
	/** Mailing has been delivered to recipient successfully. */
	MAIL_DELIVERED(3, "mailing_delivered", true),
	
	/** Hard bounce. */
	HARD_BOUNCE(4, "hard_bounce", true),

	/** 
	 * Mailing completely delivered. 
	 * 
	 * Important: This event type is used by non-Java code. Do not remove!
	 */
	MAILING_DELIVERY_COMPLETE(5, "mailing_delivery_complete", false),
	
	/**
	 * Binding status of recipient has created, updated or deleted.
	 */
	BINDING_STATUS_CHANGED(6, "binding_changed", true),

	/** Test/Admin mailing has been delivered to recipient successfully. */
	TEST_MAIL_DELIVERED(7, "testmail_delivered", true),

	/** Value of the profile field changed. */
	PROFILE_FIELD_CHANGED(8, "profile_field_changed", true);

	/** Event code. */
	private final int eventCode;
	
	/** String representation of event. */
	private final String stringRepresentation;
	
	/** Flag indicating that event data includes recipient data. */
	private final boolean includesRecipientData;
	
	/**
	 * Creates a new enum constant.
	 * 
	 * @param eventCode event code
	 * @param stringRepresentation textual representation of this event type
	 * @param includesRecipientData <code>true</code> if event data includes recipient data
	 */
	WebhookEventType(int eventCode, String stringRepresentation, boolean includesRecipientData) {
		this.eventCode = eventCode;
		this.stringRepresentation = Objects.requireNonNull(stringRepresentation, "stringRepresentation is null");
		this.includesRecipientData = includesRecipientData;
	}
	
	/**
	 * Converts event code to enum constant.
	 * 
	 * @param eventCode event code
	 * 
	 * @return Webhook event or {@link Optional#empty()}
	 */
	public static final Optional<WebhookEventType> fromEventCode(final int eventCode) {
		for(final WebhookEventType type : values()) {
			if(type.eventCode == eventCode) {
				return Optional.of(type);
			}
		}
		
		return Optional.empty();
	}
	
	/**
	 * Returns event code for this webhook event.
	 * 
	 * @return event code for this webhook event
	 */
	public final int getEventCode() {
		return this.eventCode;
	}
	
	/**
	 * Returns the string representation of this webhook event.
	 * 
	 * @return string representation of this webhook event
	 */
	public final String getStringRepresentation() {
		return this.stringRepresentation;
	}
	
	/**
	 * Returns <code>true</code>, if data of this event includes
	 * recipient data.
	 * 
	 * @return <code>true</code>, if data of this event includes recipient data
	 */
	public final boolean isIncludesRecipientData() {
		return this.includesRecipientData;
	}
}
