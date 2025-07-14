/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.commons.password;

/**
 * Enumeration of policy violation indicators.
 */
public enum PolicyViolation {
	
	/** Password is too short. */
	TOO_SHORT,
	
	/** Password does not contain a digit. */
	NO_DIGITS,
	
	/** Password does not contain a lowercase letter. */
	NO_LOWER_CASE,
	
	/** Password does not contain an uppercase letter. */
	NO_UPPER_CASE,
	
	/** Password does not contain a special character. */
	NO_SPECIAL,
	
	/** New password matches current password. */
	MATCHES_CURRENT_PASSWORD
	
}
