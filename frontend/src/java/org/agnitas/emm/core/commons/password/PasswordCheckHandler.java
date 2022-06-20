/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.commons.password;

/**
 * Callback interface for reporting password errors.
 */
public interface PasswordCheckHandler {

	/**
	 * Callback method indicating that password does not contain lowercase letters.
	 */
	public void handleNoLowerCaseLettersException();

	/**
	 * Callback method indicating that password does not contain uppercase letters.
	 */
	public void handleNoUpperCaseLettersException();

	/**
	 * Callback method indicating that password does not contain digits.
	 */
	public void handleNoDigitsException();

	/**
	 * Callback method indicating that password does not contain punctuation.
	 */
	public void handleNoPunctuationException();

	/**
	 * Callback method indicating that password is too short.
	 */
	public void handlePasswordTooShort();

	/**
	 * Callback method indicating that password does not differ from current password.
	 */
	public void handleMatchesCurrentPassword();

	/**
	 * Callback method indicating a general or uncategorized error in password.
	 */
	public void handleGenericError();

}
