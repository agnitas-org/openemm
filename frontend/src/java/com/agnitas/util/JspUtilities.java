/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.util;

import java.util.Collection;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;

import com.agnitas.beans.ComAdmin;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.DbColumnType;
import org.apache.commons.lang.StringUtils;

public class JspUtilities {
    public static final String JS_TABLE_COLUMN_TYPE_COMMON = "";
    public static final String JS_TABLE_COLUMN_TYPE_NUMBER = "numberColumn";
    public static final String JS_TABLE_COLUMN_TYPE_DATE = "dateColumn";

    public static String getTimeZoneId(HttpServletRequest request) {
        ComAdmin admin = AgnUtils.getAdmin(request);

        if (admin == null) {
            return null;
        }

        return admin.getAdminTimezone();
    }

    public static String asJsTableColumnType(DbColumnType type) {
        if (type == null) {
            return null;
        }

        switch (type.getSimpleDataType()) {
            case Date:
                return JS_TABLE_COLUMN_TYPE_DATE;

            case Numeric:
                return JS_TABLE_COLUMN_TYPE_NUMBER;

            case Blob:  // Fall-through
            case Characters:  // Fall-through
            default:
                // No custom handling.
                return JS_TABLE_COLUMN_TYPE_COMMON;
        }
    }

    public static String asText(Object value) {
        if (value == null) {
            return StringUtils.EMPTY;
        }

        return StringUtils.defaultString(value.toString());
    }

    public static boolean contains(Object container, Object object) throws JspException {
        if (container == null) {
            return false;
        }
        if (container instanceof Collection) {
            return ((Collection<?>) container).contains(object);
        }

        if (container instanceof String) {
            if (object == null) {
                return false;
            }

            return ((String) container).contains(object.toString());
        }

        throw new JspException("emm:contains() accepts either Collection or String");
    }
}
