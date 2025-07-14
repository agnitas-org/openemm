/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.webhooks.messages.common;

import java.util.Optional;

/**
 * Status for webhook messages.
 */
public enum WebhookMessageStatus {
	/** Message is ready for sending. */
	IDLE(0),
	
	/** Message is currently in send process. */
	SENDING(1),
	
	/** Message has been sent successfully. */
	SENT(2),
	
	/** 
	 * Message has been cancelled.
	 * 
	 * Reasons can be
	 * <ul>
	 *   <li>The maximum number of retry counts is reached.</li>
	 *   <li>No webhook URL is configured. (Can occur when webhook URL is removed while message is queued.)</li>
	 * </ul> 
	 */
	CANCELLED(3);
	
	/** Numeric status code. */
	private final int code;
	
	/**
	 * Creates enum constant.
	 * 
 	 * @param code numeric status code
	 */
	private WebhookMessageStatus(final int code) {
		this.code = code;
	}
	
	/**
	 * Returns the status constant for given numeric status code.
	 * If status code is unknown, {@link Optional#empty()} is returned.
	 * 
	 * @param code numeric status code
	 * 
	 * @return enum constant or {@link Optional#empty()}
	 */
	public static final Optional<WebhookMessageStatus> fromStatusCode(final int code) {
		for(final WebhookMessageStatus status : values()) {
			if(status.code == code) {
				return Optional.of(status);
			}
		}
		
		return Optional.empty();
	}
	
	/**
	 * Returns the numeric status code.
	 * 
	 * @return numeric status code
	 */
	public final int getStatusCode() {
		return this.code;
	}
}
