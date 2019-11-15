/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.action.service;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Class to collect errors occured during processing action operations.
 * 
 * Each error code is listed once, even if added more than once.
 */
public final class EmmActionOperationErrors {
	
	/**
	 * Enumeration of error codes.
	 *
	 */
	public enum ErrorCode {
		/** Indicator for general errors. */
		GENERAL_ERROR,
		
		/** Indicator for missing key column name. */
		MISSING_KEY_COLUMN,
		
		/** Missing key value. */
		MISSING_KEY_VALUE,
		
		/** Email address malformed. */
		EMAIL_ADDRESS_INVALID,
		
		/** Email address not allowed (blacklisted, ...) */
		EMAIL_ADDRESS_NOT_ALLOWED, 
		
		/** Malformed mobile phone number. */
		MALFORMED_MOBILEPHONE_NUMBER,
		
		/** Mobile phone number not allowed (not whitelisted, ...) */
		MOBILEPHONE_NUMBER_NOT_ALLOWED,
		
		/** A hugh number of requests are detected, request got blocked. */
		FLOODING,
		
		MISSING_CUSTOMER_ID,
		MISSING_MAILING_ID
	}
	
	/** Set of codes representing errors occurred during processing. */
	private final Set<ErrorCode> errorCodes;
	
	/**
	 * Creates a new empty set of error codes.
	 */
	public EmmActionOperationErrors() {
		this.errorCodes = new HashSet<>();
	}
	
	/**
	 * Returns <code>true</code> if no error code was added to the instance.
	 * 
	 * @return <code>true</code> if no error code was added
	 */
	public final boolean isEmpty() {
		return this.errorCodes.isEmpty();
	}
	
	/**
	 * Checks, if this instance contains the given error token. 
	 * 
	 * The token must match one of the constants defined in {@link ErrorCode}. If not, the token
	 * is considered as be not contained in this instance.
	 * 
	 * @param token error token
	 * 
	 * @return <code>true</code> if error code was added previously
	 * 
	 * @see ErrorCode
	 */
	public final boolean containsToken(final String token) {
		final ErrorCode errorCode = ErrorCode.valueOf(token);

		return errorCode != null
				? this.errorCodes.contains(errorCode)
				: false;		// By definition, undefined error codes can never occur in set
	}
	
	/**
	 * Adds an error code.
	 * 
	 * @param errorCode error code to add
	 * 
	 * @throws NullPointerException if errorCode is <code>null</code>
	 */
	public final void addErrorCode(final ErrorCode errorCode) {
		this.errorCodes.add(Objects.requireNonNull(errorCode, "Error code is null"));
	}
	
	@Override
	public String toString() {
		return this.errorCodes.toString();
	}
	
}
