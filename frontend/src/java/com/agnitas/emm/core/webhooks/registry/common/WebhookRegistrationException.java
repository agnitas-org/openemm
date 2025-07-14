/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.webhooks.registry.common;

import java.util.Objects;

import com.agnitas.emm.core.webhooks.common.WebhookEventType;

/**
 * Base class for any errors regarding the webhook registry.
 */
public class WebhookRegistrationException extends Exception {

	/** Serial version UID. */
	private static final long serialVersionUID = 2762594123130935744L;

	/** Company ID of operation. */
	private final int companyID;
	
	/** Event type of operation. */
	private final WebhookEventType eventType;
	
	/**
	 * Creates a new exception.
	 * 
	 * @param companyId company ID of operation
	 * @param eventType event type of operation
	 * 
	 * @throws NullPointerException if <code>eventType</code> is <code>null</code>
	 */
	public WebhookRegistrationException(final int companyId, final WebhookEventType eventType) {
		super();
		
		this.companyID = companyId;
		this.eventType = Objects.requireNonNull(eventType, "eventType is null");
	}

	/**
	 * Creates a new exception with error message and cause.
	 * 
	 * @param message error message
	 * @param cause cause
	 * @param companyId company ID of operation
	 * @param eventType event type of operation
	 * 
	 * @throws NullPointerException if <code>eventType</code> is <code>null</code>
	 */
	public WebhookRegistrationException(final int companyId, final WebhookEventType eventType, final String message, final Throwable cause) {
		super(message, cause);
		
		this.companyID = companyId;
		this.eventType = Objects.requireNonNull(eventType, "eventType is null");
	}

	/**
	 * Creates a new exception with error message.
	 * 
	 * @param message error message
	 * @param companyId company ID of operation
	 * @param eventType event type of operation
	 * 
	 * @throws NullPointerException if <code>eventType</code> is <code>null</code>
	 */
	public WebhookRegistrationException(final int companyId, final WebhookEventType eventType, final String message) {
		super(message);
		
		this.companyID = companyId;
		this.eventType = Objects.requireNonNull(eventType, "eventType is null");
	}

	/**
	 * Creates a new exception with cause.
	 * 
	 * @param cause cause
	 * @param companyId company ID of operation
	 * @param eventType event type of operation
	 * 
	 * @throws NullPointerException if <code>eventType</code> is <code>null</code>
	 */
	public WebhookRegistrationException(final int companyId, final WebhookEventType eventType, final Throwable cause) {
		super(cause);
		
		this.companyID = companyId;
		this.eventType = Objects.requireNonNull(eventType, "eventType is null");
	}

	/**
	 * Returns the company ID of the operation.
	 * 
	 * @return company ID
	 */
	public final int getCompanyID() {
		return companyID;
	}

	/**
	 * Returns the event type of the operation.
	 * 
	 * @return event type
	 */
	public final WebhookEventType getEventType() {
		return eventType;
	}

}
