/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.messages;

import java.util.Locale;

import org.apache.commons.lang.ArrayUtils;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;

public class DBMessageSource implements MessageSource {
    @Override
    public String getMessage(String code, Object[] args, String defaultMessage, Locale locale) {
        if (I18nString.hasMessageForKey(code)) {
            return I18nString.getLocaleString(code, locale, args);
        } else {
            return defaultMessage;
        }
    }

    @Override
    public String getMessage(String code, Object[] args, Locale locale) throws NoSuchMessageException {
        if (I18nString.hasMessageForKey(code)) {
            return I18nString.getLocaleString(code, locale, args);
        } else {
            throw new NoSuchMessageException(code, locale);
        }
    }

    @Override
    public String getMessage(MessageSourceResolvable resolvable, Locale locale) throws NoSuchMessageException {
        String[] codes = resolvable.getCodes();

        if (codes != null) {
            for (String code : codes) {
                if (I18nString.hasMessageForKey(code)) {
                    return I18nString.getLocaleString(code, locale, resolvable.getArguments());
                }
            }
        }

        String defaultMessage = resolvable.getDefaultMessage();

        if (defaultMessage == null) {
            throw new NoSuchMessageException(ArrayUtils.isEmpty(codes) ? null : codes[codes.length - 1], locale);
        }

        return defaultMessage;
    }
}
