/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.json.schema.validator;

import java.math.BigDecimal;
import java.util.regex.Pattern;

public class NumberUtilities {

	/**
	 * Check for a double value with optional decimals after a dot(.) and exponent
	 */
	public static boolean isDouble(final String value) {
		try {
			Double.parseDouble(value);
			return true;
		} catch (final NumberFormatException e) {
			return false;
		}
	}

	/**
	 * Compare Number objects
	 *
	 * @param a
	 * @param b
	 * @return
	 * 	 1 if a > b
	 * 	 0 if a = b
	 * 	-1 if a < b
	 */
	public static int compare(final Number a, final Number b){
		return new BigDecimal(a.toString()).compareTo(new BigDecimal(b.toString()));
	}

	public static boolean isNumber(final String numberString) {
		return Pattern.matches("[+|-]?[0-9]*(\\.[0-9]*)?([e|E][+|-]?[0-9]*)?", numberString);
	}

	/**
	 * Parse a number of unknown type in english notation like "1,234,567.90E-12".
	 * Resulting type may be Integer, Long, Float, Double, BigDecimal. Byte and Short are returned as Integer.
	 * The resulting type is the smallest type able to contain the given number without loss of accuracy.
	 */
	public static Number parseNumber(String numberString) {
		if (!isNumber(numberString)) {
			throw new NumberFormatException("Not a number: '" + numberString + "'");
		} else if (numberString.contains("e") || numberString.contains("E")) {
			final String exponentString = numberString.substring(numberString.toLowerCase().indexOf("e") + 1);
			int exponent;
			if (exponentString.length() == 0) {
				exponent = 0;
			} else {
				exponent = Integer.parseInt(exponentString);
			}
			if (Float.MIN_EXPONENT < exponent && exponent < Float.MAX_EXPONENT) {
				return Float.valueOf(numberString);
			} else {
				return Double.valueOf(numberString);
			}
		} else if (numberString.contains(".")) {
			if (numberString.length() < 10) {
				return Float.valueOf(numberString);
			} else {
				final BigDecimal value = new BigDecimal(numberString);
				final int numberOfDecimalPoints = numberString.substring(numberString.indexOf(".") + 1).replace(",", "").length();
				final boolean isFloat = numberOfDecimalPoints <= 7 && BigDecimal.valueOf(Float.MIN_VALUE).compareTo(value) < 0 && value.compareTo(BigDecimal.valueOf(Float.MAX_VALUE)) < 0;
				if (isFloat) {
					return Float.valueOf(numberString);
				} else {
					final boolean isDouble = BigDecimal.valueOf(Double.MIN_VALUE).compareTo(value) < 0 && value.compareTo(BigDecimal.valueOf(Double.MAX_VALUE)) < 0;
					if (isDouble) {
						return Double.valueOf(numberString);
					} else {
						return value;
					}
				}
			}
		} else {
			if (numberString.length() < 10) {
				return Integer.valueOf(numberString);
			} else {
				final BigDecimal value = new BigDecimal(numberString);
				final boolean isInteger = BigDecimal.valueOf(Integer.MIN_VALUE).compareTo(value) < 0 && value.compareTo(BigDecimal.valueOf(Integer.MAX_VALUE)) < 0;
				if (isInteger) {
					return Integer.valueOf(numberString);
				} else {
					final boolean isLong = BigDecimal.valueOf(Long.MIN_VALUE).compareTo(value) < 0 && value.compareTo(BigDecimal.valueOf(Long.MAX_VALUE)) < 0;
					if (isLong) {
						return Long.valueOf(numberString);
					} else {
						return value;
					}
				}
			}
		}
	}

}
