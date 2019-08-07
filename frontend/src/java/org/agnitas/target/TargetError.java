/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.target;

/**
 * Error indicator for target rule.
 */
public class TargetError {
	
	/**
	 * List of defined error keys.
	 */
	public static enum ErrorKey {
		/** Key indicates rule that cannot be validated. Maybe validator is missing. */
		CANNOT_VALIDATE( "error.target.validate.cannot_validate"),
		
		/** Indicator for invalid mailing ID. */
		INVALID_MAILING( "error.target.validate.invalid_mailing"),

		/** Indicator for invalid mailing ID. */
		INVALID_AUTO_IMPORT("error.target.autoImport.reference"),
		
		/** Indicator for mailing ID that is not an internval mailing. */
		NOT_AN_INTERVAL_MAILING( "error.target.validate.not_an_interval_mailing"),
		
		/**
		 * Indicator, that link ID is invalid for given mailing.
		 */
		INVALID_LINK("error.target.validate.invalid_link");
		
		/** Error key. */
		final String key;
		
		/**
		 * Creates a new enum item with given error key.
		 * 
		 * @param key error key
		 */
		ErrorKey( String key) {
			this.key = key;
		}
		
		/**
		 * Returns the error key.
		 * 
		 * @return error key
		 */
		public String getKey() {
			return this.key;
		}
	}

	/** Key for error */
	private final ErrorKey errorKey;
	
	/**
	 * 
	 * @param errorKey
	 */
	public TargetError( ErrorKey errorKey) {
		this.errorKey = errorKey;
	}
	
	/**
	 * Returns the key of the error.
	 * 	
	 * @return key of error
	 */
	public String getErrorKey() {
		return this.errorKey.getKey();
	}
}
