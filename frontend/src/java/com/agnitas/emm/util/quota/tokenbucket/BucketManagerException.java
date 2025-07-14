/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.util.quota.tokenbucket;

/**
 * Exception indicating an error in {@link BucketManager}.
 */
public class BucketManagerException extends Exception {

	/** Serial version UID. */
	private static final long serialVersionUID = 151400568099065089L;

	/**
	 * Creates new instance.
	 */
	private BucketManagerException() {
		// Empty
	}

	/**
	 * Creates new instance with error message.
	 * 
	 * @param message error message 
	 */
	private BucketManagerException(final String message) {
		super(message);
	}

	/**
	 * Creates new instance with cause.
	 * 
	 * @param cause  cause
	 */
	private BucketManagerException(final Throwable cause) {
		super(cause);
	}

	/**
	 * Creates new instance with error message and cause.
	 * 
	 * @param message error message 
	 * @param cause  cause
	 */
	private BucketManagerException(final String message, final Throwable cause) {
		super(message, cause);
	}

	/**
	 * Returns an instance to indicate an undefined API call limit.
	 * 
	 * @param username user name
	 * @param companyID company ID of user
	 * 
	 * @return new instance for this error
	 */
	public static final BucketManagerException noApiCallLimit(final String username, final int companyID) {
		return new BucketManagerException(String.format("No API call limits defined for user '%s' of company %d", username, companyID));
	}
	
	/**
	 * Returns an instance to indicate an invalid API call limit specification.
	 * 
	 * @param spec invalid API call limit specification
	 * 
	 * @return new instance for this error
	 */
	public static final BucketManagerException invalidApiCallLimitSpecification(final String spec) {
		return new BucketManagerException(String.format("Invalid API call limit specification: '%s'", spec));
	}
	
	/**
	 * Returns an instance to indicate an invalid API call limit specification.
	 * 
	 * @param spec invalid API call limit specification
	 * @param cause cause
	 * 
	 * @return new instance for this error
	 */
	public static final BucketManagerException invalidApiCallLimitSpecification(final String spec, final Throwable cause) {
		return new BucketManagerException(String.format("Invalid API call limit specification: '%s'", spec), cause);
	}

	/**
	 * Returns an instance to indicate that the manager was not able to create a new token bucket.
	 * 
	 * @param username user name
	 * @param companyID company ID of user
	 * @param cause cause
	 * 
	 * @return new instance for this error
	 */
	public static final BucketManagerException cannotCreateBucket(final String username, final int companyID, final Throwable cause) {
		return new BucketManagerException(String.format("Unable to create new token bucket for user '%s' of company %d", username, companyID), cause);
	}
}
