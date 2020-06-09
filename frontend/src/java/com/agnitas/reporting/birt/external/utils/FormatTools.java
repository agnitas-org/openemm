/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.external.utils;

import java.math.BigDecimal;

public class FormatTools {

	/**
	 * rounds a double from it's 1/10000 place
	 * @param gros_percent
	 * @return
	 */
	public static double roundDecimal(double value, int size) {
		BigDecimal decimal = new BigDecimal( value );
		decimal = decimal.setScale(size, BigDecimal.ROUND_HALF_UP);
		return decimal.doubleValue();
	}
	
}
