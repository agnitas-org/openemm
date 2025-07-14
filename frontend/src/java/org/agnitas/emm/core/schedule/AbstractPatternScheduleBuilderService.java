/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.schedule;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.agnitas.emm.core.autoimport.web.beans.ScheduledTime;
import com.agnitas.emm.core.autoimport.web.beans.TimeScheduledEntry;

public abstract class AbstractPatternScheduleBuilderService<T extends TimeScheduledEntry> implements ScheduleBuilderService<String, List<T>>{

    public static final int ONE_HOUR_IN_SECONDS = 3600;
    public static final int EVERY_DAY = 0;
    protected static final String COLON = ":";

    protected static final String SEMICOLON = ";";

    protected final PatternScheduleConfig config;

    public AbstractPatternScheduleBuilderService(PatternScheduleConfig config) {
        this.config = config;

    }

    protected String processScheduledTime(List<ScheduledTime> scheduledTimes, String pattern) {
        List<String> patterns = new ArrayList<>();
        for (ScheduledTime time : scheduledTimes) {
            if (time.isActive() && StringUtils.isNotBlank(time.getTime())) {
                patterns.add(pattern + formatTime(time.getTime(), config.getFromFormat(), config.getToFormat()));
            }
        }
        return StringUtils.join(patterns, SEMICOLON);
    }

     protected String formatTime(String time, DateTimeFormatter fromFormatToUse, DateTimeFormatter toFormatToUse) {
        LocalTime localTime = LocalTime.parse(time, fromFormatToUse);
        return toFormatToUse.format(localTime);
    }

    public abstract String processScheduledEntry(T entry);
}
