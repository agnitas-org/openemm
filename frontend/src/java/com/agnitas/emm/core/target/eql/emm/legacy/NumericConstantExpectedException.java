/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.emm.legacy;

/**
 * Exception indicating a expected (but missing) numeric constant.
 */
public class NumericConstantExpectedException extends EqlToTargetRepresentationConversionException {

	/** Serial version UID. */
	private static final long serialVersionUID = 8554656939753463166L;

	/**
	 * Instantiates a new numeric constant expected exception.
	 */
	public NumericConstantExpectedException() {
		super("Numeric constant expected");
	}
}
