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

	private static final long serialVersionUID = 151400568099065089L;

	private BucketManagerException(final String message) {
		super(message);
	}

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
	public static BucketManagerException noApiCallLimit(final String username, final int companyID) {
		return new BucketManagerException(String.format("No API call limits defined for user '%s' of company %d", username, companyID));
	}
	
	public static BucketManagerException invalidApiCallLimitSpecification(final String spec) {
		return new BucketManagerException(String.format("Invalid API call limit specification: '%s'", spec));
	}
	
	public static BucketManagerException invalidApiCallLimitSpecification(final String spec, final Throwable cause) {
		return new BucketManagerException(String.format("Invalid API call limit specification: '%s'", spec), cause);
	}
}
