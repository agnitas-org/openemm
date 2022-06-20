/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.commons.password;

import com.agnitas.web.mvc.Popups;

/**
 * Implementation of {@link org.agnitas.emm.core.commons.password.PasswordCheckHandler} filling {@link com.agnitas.web.mvc.Popups} collection.
 */
public class SpringPasswordCheckHandler implements PasswordCheckHandler {
    /**
     * {@link com.agnitas.web.mvc.Popups} for collecting password errors.
     */
    private final Popups popups;

    /**
     * Name of the property to which the error messages is added.
     */
    private final String propertyName;

    /**
     * Creates a new {@link org.agnitas.emm.core.commons.password.PasswordCheckHandler} to add Struts messages.
     *
     * @param popups collection to add error messages to.
     * @param propertyName name of the property to which the messages are added.
     */
    public SpringPasswordCheckHandler(Popups popups, String propertyName) {
        this.popups = popups;
        this.propertyName = propertyName;
    }

    /**
     * Creates a new {@link org.agnitas.emm.core.commons.password.PasswordCheckHandler} to add Struts messages.
     *
     * @param popups collection to add error messages to.
     */
    public SpringPasswordCheckHandler(Popups popups) {
        this(popups, null);
    }

    @Override
    public void handleNoLowerCaseLettersException() {
        error("error.password_no_lowercase_letters");
    }

    @Override
    public void handleNoUpperCaseLettersException() {
        error("error.password_no_uppercase_letters");
    }

    @Override
    public void handleNoDigitsException() {
        error("error.password_no_digits");
    }

    @Override
    public void handleNoPunctuationException() {
        error("error.password_no_special_chars");
    }

    @Override
    public void handlePasswordTooShort() {
        error("error.password.tooShort");
    }

    @Override
    public void handleMatchesCurrentPassword() {
        error("error.password_must_differ");
    }

    @Override
    public void handleGenericError() {
        error("error.password.general");
    }

    private void error(String code) {
        if (propertyName == null) {
            popups.alert(code);
        } else {
            popups.field(propertyName, code);
        }
    }
}
