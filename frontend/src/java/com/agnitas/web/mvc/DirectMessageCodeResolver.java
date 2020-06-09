/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web.mvc;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.validation.MessageCodesResolver;

public class DirectMessageCodeResolver implements MessageCodesResolver {
    @Override
    public String[] resolveMessageCodes(String errorCode, String objectName) {
        return wrap(errorCode);
    }

    @Override
    public String[] resolveMessageCodes(String errorCode, String objectName, String field, Class<?> fieldType) {
        return wrap(errorCode);
    }

    private String[] wrap(String code) {
        if (code == null) {
            return ArrayUtils.EMPTY_STRING_ARRAY;
        }

        return new String[]{code};
    }
}
