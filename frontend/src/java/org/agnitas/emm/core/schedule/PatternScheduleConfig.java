/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.schedule;

import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.stream.Collectors;

public class PatternScheduleConfig {

    private Map<Integer, String> weekDays;
    private Map<Integer, String> monthlyPatterns;
    private Map<String, Integer> reverseWeekDays;
    private DateTimeFormatter fromFormat;
    private DateTimeFormatter toFormat;

    public PatternScheduleConfig(Map<Integer, String> weekDays, Map<Integer, String> monthlyPatterns, String fromFormat, String toFormat) {
        this.weekDays = weekDays;
        this.monthlyPatterns = monthlyPatterns;
        this.fromFormat = DateTimeFormatter.ofPattern(fromFormat);
        this.toFormat = DateTimeFormatter.ofPattern(toFormat);
        this.reverseWeekDays = reverseWeekDays(weekDays);
    }

    private Map<String, Integer> reverseWeekDays(Map<Integer, String> weekDaysToReverse) {
        return weekDaysToReverse.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
    }

    public Map<Integer, String> getWeekDays() {
        return weekDays;
    }

    public Map<Integer, String> getMonthlyPatterns() {
        return monthlyPatterns;
    }

    public Map<String, Integer> getReverseWeekDays() {
        return reverseWeekDays;
    }

    public DateTimeFormatter getFromFormat() {
        return fromFormat;
    }

    public DateTimeFormatter getToFormat() {
        return toFormat;
    }
}
