/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.codegen.util;

import com.agnitas.emm.core.target.eql.codegen.CodeFragment;
import com.agnitas.emm.core.target.eql.codegen.CodeLocation;
import com.agnitas.emm.core.target.eql.codegen.DataType;
import com.agnitas.emm.core.target.eql.codegen.InvalidTypeException;

public final class DataTypeUtils {
	/**
	 * Checks, if code fragment evaluates to one of the listed types. If not, an
	 * InvalidTypeException is thrown.
	 * @param fragment
	 *            code fragment to verify
	 * @param location
	 *            code location
	 * @param expectedTypes
	 *            list of expected types
	 * 
	 * @return given code fragment if evaluates to one of given types
	 * 
	 * @throws InvalidTypeException
	 *             if code fragment does not evaluate to one of the listed types
	 */
	public static final CodeFragment requireDataType(final CodeFragment fragment, final CodeLocation location, final DataType... expectedTypes) throws InvalidTypeException {
		final DataType actualType = fragment.evaluatesToType();

		for (final DataType expectedType : expectedTypes) {
			if (actualType == expectedType) {
				return fragment;
			}
		}

		throw new InvalidTypeException(location, actualType, expectedTypes);
	}

}
