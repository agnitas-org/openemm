/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.codegen;

/**
 * Enumeration of data types, a code fragment can evaluate to.
 * This enumeration is also be used for data types of profile fields (where {@link #BOOLEAN} may not
 * occur).
 */
public enum DataType {

	/** Indicator for alpha-numerical values. */
	TEXT,
	
	/** Indicator for numerical values. */
	NUMERIC,
	
	/** Indicator for date values. */
	DATE,
	
	/** Indicator for boolean values. Internal use only. */
	BOOLEAN
}
