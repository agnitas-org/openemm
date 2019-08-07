/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.commons.password;

/**
 * Utility class dealing with passwords.
 */
public class PasswordUtil {

	/** Minimum length of password. */
	public static final int MINIMUM_PASSWORD_LENGTH = 8;
	
	/**
	 * Checks basic properties of given password:
	 * <ul>
	 *   <li>Password must contain at least 1 lowercase letter</li>
	 *   <li>Password must contain at least 1 uppercase letter</li>
	 *   <li>Password must contain at least 1 digit</li>
	 *   <li>Password must contain at least 1 punctuation character</li>
	 *   <li>Password must be at least {@value PasswordUtil#MINIMUM_PASSWORD_LENGTH} characters long</li>
	 * </ul>
	 *   
	 * @param password password to check
	 * 
	 * @throws PasswordConstraintException when password violates one of the checked rules
	 */
	public static void checkPasswordConstraints(String password) throws PasswordConstraintException {
		if (!password.matches(".*\\p{Lower}.*")) {
			throw new PasswordContainsNoLowerCaseLettersException();
		} else if (!password.matches(".*\\p{Upper}.*")) {
			throw new PasswordContainsNoUpperCaseLettersException();
		} else if (!password.matches(".*\\p{Digit}.*")) {
			throw new PasswordContainsNoDigitsException();
		} else if (!password.matches(".*[^\\p{Alnum}].*")) {	// Every character, that is not alpha or digit is treated as special character
			throw new PasswordContainsNoSpecialCharactersException();
		} else if (password.length() < MINIMUM_PASSWORD_LENGTH) {
			throw new PasswordTooShortException();
		}
	}
}
