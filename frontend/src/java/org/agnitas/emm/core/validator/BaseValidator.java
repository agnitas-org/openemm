/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.validator;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.agnitas.util.AgnUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

public abstract class BaseValidator {

    private final ResourceBundle messagesBundle;

    protected BaseValidator(final String propertiesFile) {
        this.messagesBundle = ResourceBundle.getBundle(propertiesFile);
    }

    protected void assertPositive(final Number propertyValue, final String propertyNameCode) {
        if (propertyValue == null || propertyValue.doubleValue() <= 0.0) {
            throwException("err.isPositive", propertyNameCode);
        }
    }

    protected void assertIsPositiveOrZero(final Number propertyValue, final String propertyNameCode) {
        if (propertyValue == null || propertyValue.doubleValue() < 0.0) {
            throwException("err.isPositiveOrZero", propertyNameCode);
        }
    }
    
    protected void assertIsNotBlank(final String propertyValue, final String propertyNameCode) {
        if (StringUtils.isBlank(propertyValue)) {
            throwException("err.required", propertyNameCode);
        }
    }

    protected void assertMaxLength(final String propertyValue, final String propertyNameCode, final int maxAllowedLength) {
        if (StringUtils.length(propertyValue) > maxAllowedLength) {
            throwException("err.maxLength", propertyNameCode, maxAllowedLength);
        }
    }

    protected void assertIsNotEmpty(final byte[] propertyValue, final String propertyNameCode) {
        if (ArrayUtils.isEmpty(propertyValue)) {
            throwException("err.required", propertyNameCode);
        }
    }
    
    protected void assertIsEmail(String propertyValue, final String propertyNameCode) {
        if (!AgnUtils.isEmailValid(propertyValue)) {
            throwException("err.isEmail", propertyNameCode);
        }
    }

    private void throwException(final String rootMessageCode, final Object... argsMessagesCodes) {
        throw new IllegalArgumentException(getMessage(rootMessageCode, argsMessagesCodes));
    }

    private String getMessage(final String mainKey, final Object... argsMessagesCodes) {
        return MessageFormat.format(getMessage(mainKey), getArgs(argsMessagesCodes));
    }

    private Object[] getArgs(final Object... argsMessagesCodes) {
        final List<String> args = new ArrayList<>();
        for (Object arg : argsMessagesCodes){
            if (arg instanceof Integer) {
                args.add(arg.toString());
            } else {
                args.add(getMessage(arg.toString()));
            }
        }
        return args.toArray();
    }

    protected String getMessage(final String messageCode) {
        String message = null;

        try {
            message = messagesBundle.getString(messageCode);
        } catch (MissingResourceException e) {
            message = messageCode;
        }

        return (message == null) ? "" : message;
    }
}
