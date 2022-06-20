/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.external.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.agnitas.util.DateUtilities;

public final class ExpirationUtils {

    private ExpirationUtils() {}

    public static boolean isDateOlderThenDays(String statPeriodStartDateStr, int expirationPeriodDays) {
        if(statPeriodStartDateStr.length() > DateUtilities.YYYY_MM_DD.length()) {
            statPeriodStartDateStr = statPeriodStartDateStr.substring(0, DateUtilities.YYYY_MM_DD.length());
        }

        LocalDate statPeriodStartDate = LocalDate.parse(statPeriodStartDateStr, DateTimeFormatter.ofPattern(DateUtilities.YYYY_MM_DD));
        return statPeriodStartDate.isBefore(LocalDate.now().minusDays(expirationPeriodDays));
    }

}
