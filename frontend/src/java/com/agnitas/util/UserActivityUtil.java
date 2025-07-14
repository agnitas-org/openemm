/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.util;

import java.text.DateFormat;
import java.util.Date;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.useractivitylog.bean.UserAction;
import com.agnitas.service.UserActivityLogService;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;

public class UserActivityUtil {

    public static String addSetFieldLog(String fieldName, int value) {
        return addSetFieldLog(fieldName, String.valueOf(value));
    }

    public static String addSetFieldLog(String fieldName, boolean value) {
        return addSetFieldLog(fieldName, String.valueOf(value));
    }

    public static String addSetFieldLog(String fieldName, String value) {
        if (StringUtils.isBlank(value)) {
            return "";
        }

        return "set " + fieldName + " to " + value + ". ";
    }

    public static String addChangedFieldLog(String fieldName, boolean newValue, boolean oldValue) {
        return addChangedFieldLog(fieldName, String.valueOf(newValue), String.valueOf(oldValue));
    }

    public static String addChangedFieldLog(String fieldName, Integer newValue, Integer oldValue) {
        String newVal = newValue == null || newValue == 0 ? "" : String.valueOf(newValue);
        String oldVal = oldValue == null || oldValue == 0 ? "" : String.valueOf(oldValue);

        return addChangedFieldLog(fieldName, newVal, oldVal);
    }

    public static String addChangedFieldLog(String fieldName, Date newValue, Date oldValue, DateFormat format) {
        String newDate = newValue == null ? null : format.format(newValue);
        String oldDate = oldValue == null ? null : format.format(oldValue);

        return addChangedFieldLog(fieldName, newDate, oldDate);
    }

    public static String addChangedFieldLog(String fieldName, String newValue, String oldValue) {
        oldValue = StringUtils.trimToNull(oldValue);
        newValue = StringUtils.trimToNull(newValue);

        return buildFieldDescription(fieldName, newValue, oldValue);
    }

    private static String buildFieldDescription(String fieldName, String newValue, String oldValue) {
        StringBuilder fieldDescription = new StringBuilder();

        if (!StringUtils.equals(oldValue, newValue)) {
            fieldDescription.append(getRemovedFiledDescription(fieldName, newValue, oldValue));
            fieldDescription.append(getChangedFieldDescription(fieldName, newValue, oldValue));
        }
        return fieldDescription.toString();
    }

    private static String getRemovedFiledDescription(String fieldName, String newValue, String oldValue) {
        StringBuilder fieldDescription = new StringBuilder();

        if (StringUtils.isEmpty(newValue)) {
            fieldDescription.append(fieldName)
                    .append(" value ").append(oldValue)
                    .append(" was removed. ");
        }

        return fieldDescription.toString();
    }

    private static String getChangedFieldDescription(String fieldName, String newValue, String oldValue) {
        StringBuilder fieldDescription = new StringBuilder();

        if (StringUtils.isNotEmpty(newValue)) {
            if (StringUtils.isNotEmpty(oldValue)) {
                fieldDescription.append(fieldName).append(" changed from ")
                        .append(oldValue).append(" to ").append(newValue).append(". ");
            } else {
                fieldDescription.append(fieldName).append(" changed to ").append(newValue).append(". ");
            }
        }

        return fieldDescription.toString();
    }

    public static void log(UserActivityLogService logService, Admin admin, UserAction ua, Logger logger) {
        log(logService, admin, ua.getAction(), ua.getDescription(), logger);
   	}
   
   	public static void log(UserActivityLogService logService, Admin admin,
                           String action, String description, Logger logger) {
   		if (logService != null) {
            logService.writeUserActivityLog(admin, action, description, logger);
            return;
        }
        logger.error("Missing userActivityLogService in {}", logger.getClass());
        logger.info("Userlog: {}, {}, {}", admin.getUsername(), action,  description);
   	}
}
