/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.commons.password;

import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

/**
 * Implementation of {@link PasswordCheckHandler} adding Struts {@link ActionMessage}s to
 * {@link ActionMessages} collection.
 */
public class StrutsPasswordCheckHandler implements PasswordCheckHandler {
	
	/** {@link ActionMessages} for collecting password errors. */
	private final ActionMessages errors;
	
	/** Name of the property to which the error messages is added. */
	private final String propertyName;
	
	/**
	 * Creates a new {@link PasswordCheckHandler} to add Struts messages.
	 * 
	 * @param errors {@link ActionMessages} collection to add error messages to
	 * @param propertyName name of the property to which the messages are added
	 */
	public StrutsPasswordCheckHandler(ActionMessages errors, String propertyName) {
		this.errors = errors;
		this.propertyName = propertyName;
	}
	
	@Override
	public void handleNoLowerCaseLettersException() {
		errors.add(propertyName, new ActionMessage("error.password_no_lowercase_letters"));
	}
	
	@Override
	public void handleNoUpperCaseLettersException() {
		errors.add(propertyName, new ActionMessage("error.password_no_uppercase_letters"));
	}

	@Override
	public void handleNoDigitsException() {
		errors.add(propertyName, new ActionMessage("error.password_no_digits"));
	}

	@Override
	public void handleNoPunctuationException() {
		errors.add(propertyName, new ActionMessage("error.password_no_special_chars"));
	}

	@Override
	public void handlePasswordTooShort() {
		errors.add(propertyName, new ActionMessage("error.password.tooShort"));
	}

	@Override
	public void handleMatchesCurrentPassword() {
		errors.add(propertyName, new ActionMessage("error.password_must_differ"));
	}

	@Override
	public void handleGenericError() {
		errors.add(propertyName, new ActionMessage("error.password.general"));
	}

}
