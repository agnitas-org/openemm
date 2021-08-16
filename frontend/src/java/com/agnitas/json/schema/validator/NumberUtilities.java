/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.json.schema.validator;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.regex.Pattern;

public class NumberUtilities {
	public static DecimalFormat NUMBER_WITH_POINTS = new DecimalFormat("###,##0");
	public static DecimalFormat NUMBER_WITH_MIN_2_DIGITS = new DecimalFormat("00");
	public static DecimalFormat NUMBER_WITH_MIN_4_DIGITS = new DecimalFormat("0000");
	public static DecimalFormat NUMBER_WITH_MIN_6_DIGITS = new DecimalFormat("000000");
	public static DecimalFormat NUMBER_WITH_MIN_7_DIGITS = new DecimalFormat("0000000");

	/**
	 * Check for a single digit
	 *
	 * @param value
	 * @return
	 */
	public static boolean isDigit(final String digitString) {
		for (final char character : digitString.toCharArray()) {
			if (!Character.isDigit(character)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Check for a integer value without decimals
	 *
	 * @param value
	 * @return
	 */
	public static boolean isInteger(final String value) {
		try {
			Integer.parseInt(value);
			return true;
		} catch (@SuppressWarnings("unused") final NumberFormatException e) {
			return false;
		}
	}

	/**
	 * Check for a double value with optional decimals after a dot(.) and exponent
	 *
	 * @param value
	 * @return
	 */
	public static boolean isDouble(final String value) {
		try {
			Double.parseDouble(value);
			return true;
		} catch (@SuppressWarnings("unused") final NumberFormatException e) {
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
	 *
	 * @param numberString
	 * @return
	 * @throws NumberFormatException
	 */
	public static Number parseNumber(final String numberString) throws NumberFormatException {
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
				final boolean isFloat = numberOfDecimalPoints <= 7 && new BigDecimal(Float.MIN_VALUE).compareTo(value) == -1 && value.compareTo(new BigDecimal(Float.MAX_VALUE)) == -1;
				if (isFloat) {
					return Float.valueOf(numberString);
				} else {
					final boolean isDouble = new BigDecimal(Double.MIN_VALUE).compareTo(value) == -1 && value.compareTo(new BigDecimal(Double.MAX_VALUE)) == -1;
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
				final boolean isInteger = new BigDecimal(Integer.MIN_VALUE).compareTo(value) == -1 && value.compareTo(new BigDecimal(Integer.MAX_VALUE)) == -1;
				if (isInteger) {
					return Integer.valueOf(numberString);
				} else {
					final boolean isLong = new BigDecimal(Long.MIN_VALUE).compareTo(value) == -1 && value.compareTo(new BigDecimal(Long.MAX_VALUE)) == -1;
					if (isLong) {
						return Long.valueOf(numberString);
					} else {
						return value;
					}
				}
			}
		}
	}

	public static boolean isHexNumber(final String numberString) {
		return Pattern.matches("0(x|X)[0-9A-Fa-f]+", numberString);
	}

	public static Number parseHexNumber(final String hexNumberString) throws NumberFormatException {
		if (!isHexNumber(hexNumberString)) {
			throw new NumberFormatException("Not a hex number: '" + hexNumberString + "'");
		} else {
			if (hexNumberString.length() < 12) {
				return Integer.parseInt(hexNumberString.substring(2), 16);
			} else {
				final BigInteger value = new BigInteger(hexNumberString.substring(2), 16);
				final boolean isInteger = new BigInteger(Integer.toString(Integer.MIN_VALUE)).compareTo(value) == -1 && value.compareTo(new BigInteger(Integer.toString(Integer.MAX_VALUE))) == -1;
				if (isInteger) {
					return Integer.parseInt(hexNumberString.substring(2), 16);
				} else {
					final boolean isLong = new BigInteger(Long.toString(Long.MIN_VALUE)).compareTo(value) == -1 && value.compareTo(new BigInteger(Long.toString(Long.MAX_VALUE))) == -1;
					if (isLong) {
						return Long.parseLong(hexNumberString.substring(2), 16);
					} else {
						return value;
					}
				}
			}
		}
	}
}
