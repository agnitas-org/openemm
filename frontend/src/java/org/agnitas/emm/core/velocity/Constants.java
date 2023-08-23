/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/


package org.agnitas.emm.core.velocity;

/**
 * Set of constants defined for the EMM Velocity environment.
 */
public final class Constants {

	/** Name of context attribute holding the execution errors. */
	public static final transient String ACTION_OPERATION_ERRORS_CONTEXT_NAME = "actionErrors";
	
	/**
	 * Private ctor to avoid instantiation of this class.
	 */
	private Constants() {
		// Avoid instantiation
	}
}
