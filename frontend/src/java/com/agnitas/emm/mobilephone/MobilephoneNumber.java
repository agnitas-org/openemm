/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.mobilephone;

import java.util.regex.Pattern;


/**
 * Value object containing a mobile phone number in normalized form.
 */
public final class MobilephoneNumber {

	/** Regular expression for normalized form. */
	private static final Pattern PATTERN = Pattern.compile("^\\+\\d+$");
	
	/** Phone number in valid format. */
	private final String phoneNumber;
	
	/**
	 * Creates a new instance for given phone number.
	 * 
	 * @param number phone number as {@link String}
	 * 
	 * @throws NumberFormatException in phone number is not in valid form
	 */
	public MobilephoneNumber(final String number) throws NumberFormatException {
		this.phoneNumber = normalizeAndCheck(number);
	}
	
	/**
	 * Normalizes the phone number by removing all whitespaces. The resulting {@link String}
	 * is checked against regular expression.
	 * 
	 * @param s phone number as {@link String}
	 * 
	 * @return normalized number in valid form
	 * 
	 * @throws NumberFormatException if given phone number is not in valid form
	 */
	private static final String normalizeAndCheck(final String s) throws NumberFormatException {
		final String withoutSpaces = s.replace(" ", "");
		
		if(PATTERN.matcher(withoutSpaces).matches()) {
			return withoutSpaces;
		} else {
			throw new NumberFormatException("Invalid mobile phone format");
		}
	}
	
	@Override
	public final int hashCode() {
		return this.phoneNumber.hashCode();
	}
	
	@Override
	public final String toString() {
		return this.phoneNumber;
	}
	
	@Override
	public final boolean equals(final Object obj) {
		if(obj != null) {
			return obj instanceof MobilephoneNumber
					? phoneNumber.equals(((MobilephoneNumber) obj).phoneNumber)
					: false;
		} else {
			return false;
		}
	}
}
