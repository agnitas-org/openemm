/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.action.service;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Class to collect errors occurred during processing action operations.
 * <p>
 * Each error code is listed once, even if added more than once.
 */
public final class EmmActionOperationErrors {

	/**
	 * Classifies WHY an {@link ErrorCode} occurred
	 */
	public enum Fault {

		/** The request itself (its supplied data) is at fault; the caller can fix it by correcting the request. */
		CALLER,

		/** The trigger's own stored configuration is broken/stale */
		CONFIGURATION,

		/** A persistent account/plan-level limit was reached. */
		QUOTA,

		/** Access to a resource was denied. */
		PERMISSION,

		/** The resource used to exist / be usable but is no longer available (e.g. a time-limited access
		 *  window has passed). */
		EXPIRED,

		/** Unexpected/internal failure */
		INTERNAL

	}

	public enum ErrorCode {

		GENERAL_ERROR("General error occurred while executing the trigger", Fault.INTERNAL),
		MISSING_KEY_COLUMN("Key column name is missing", Fault.CALLER),
		MISSING_KEY_VALUE("Key value is missing", Fault.CALLER),
		EMAIL_ADDRESS_INVALID("Email address is malformed", Fault.CALLER),
		EMAIL_ADDRESS_BLACKLISTED("Email address is blacklisted", Fault.CALLER),
		MOBILEPHONE_NUMBER_NOT_WHITELISTED("Mobile phone number is not whitelisted", Fault.CALLER),
		MALFORMED_MOBILEPHONE_NUMBER("Mobile phone number is malformed", Fault.CALLER),
		NO_RECIPIENT_PROFILE_FIELDS_READ("No recipient profile fields could be read", Fault.INTERNAL),
		RECEIVER_ADDRESS_BLACKLISTED("Receiver address is blacklisted", Fault.CALLER),
		SENDER_ADDRESS_BLACKLISTED("Sender address is blacklisted", Fault.CALLER),
		REPLY_ADDRESS_BLACKLISTED("Reply address is blacklisted", Fault.CALLER),
		NO_SENDER_ADDRESS("No sender address configured", Fault.INTERNAL),
		SUBSCRIBER_LIMIT_EXCEEDED("Subscriber limit exceeded", Fault.QUOTA),
		ACCESS_TO_COMPANY_DENIED("Access to company denied", Fault.PERMISSION),
		MISSING_UID("UID is missing", Fault.CALLER),
		INVALID_CUSTOMER_ID("Customer ID is invalid", Fault.CALLER),
		UNKNOWN_RECIPIENT("No recipient found for given customer ID", Fault.CALLER),
		RECIPIENT_NOT_ACTIVE("Recipient is not active", Fault.CALLER),
		MISSING_CUSTOMER_ID("Customer ID is missing", Fault.CALLER),
		UNKNOWN_COMPANY_ID("Unknown company ID", Fault.INTERNAL),
		INVALID_MAILINGLIST_ID("Mailinglist ID is invalid", Fault.CALLER),
		MISSING_MAILING_ID("Mailing ID is missing", Fault.CALLER),
		MAILING_NOT_FOUND("Mailing not found", Fault.CONFIGURATION),
		INVALID_MAILING_ID("Mailing ID is invalid", Fault.CALLER),
		BINDING_NOT_FOUND("No binding found for the given recipient", Fault.CALLER),
		UNKNOWN_MAILING_ID("No mailing found for given mailing ID", Fault.CALLER),
		INVALID_TRACKING_POINT_TYPE("Tracking point type is invalid", Fault.CALLER),
		ARCHIVE_EXPIRED("Archive mailing access has expired", Fault.EXPIRED);

		private final String description;
		private final Fault fault;

		ErrorCode(String description, Fault fault) {
			this.description = description;
			this.fault = fault;
		}

		public String getDescription() {
			return description;
		}

		public Fault getFault() {
			return fault;
		}
	}
	
	/** Set of codes representing errors occurred during processing. */
	private final Set<ErrorCode> errorCodes;

	public EmmActionOperationErrors() {
		errorCodes = new HashSet<>();
	}

	public boolean isEmpty() {
		return errorCodes.isEmpty();
	}

	public Set<ErrorCode> getErrorCodes() {
		return Set.copyOf(errorCodes);
	}

	/**
	 * Adds an error code.
	 * 
	 * @param errorCode error code to add
	 * 
	 * @throws NullPointerException if errorCode is <code>null</code>
	 */
	public void addErrorCode(ErrorCode errorCode) {
		errorCodes.add(Objects.requireNonNull(errorCode, "Error code is null"));
	}

	@Override
	public String toString() {
		return errorCodes.toString();
	}
}
