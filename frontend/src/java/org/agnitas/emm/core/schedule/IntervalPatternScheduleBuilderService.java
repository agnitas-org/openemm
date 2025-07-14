/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.schedule;

import static org.agnitas.emm.core.schedule.bean.IntervalScheduledEntry.PeriodType;
import static org.agnitas.emm.core.schedule.bean.IntervalScheduledEntry.MonthlyType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.agnitas.emm.core.schedule.bean.IntervalScheduledEntry;
import org.apache.commons.lang3.StringUtils;

import com.agnitas.beans.IntEnum;
import com.agnitas.emm.core.autoimport.web.beans.ScheduledInterval;
import com.agnitas.emm.core.autoimport.web.beans.ScheduledTime;
import com.agnitas.emm.core.autoimport.web.beans.TimeScheduledEntry;

public class IntervalPatternScheduleBuilderService extends AbstractPatternScheduleBuilderService<IntervalScheduledEntry> {

    private PatternScheduleBuilderService patternScheduleBuilderService;

    public IntervalPatternScheduleBuilderService(PatternScheduleConfig config, PatternScheduleBuilderService patternScheduleBuilderService) {
        super(config);
        this.patternScheduleBuilderService = patternScheduleBuilderService;
    }

    @Override
    public String buildSchedule(List<IntervalScheduledEntry> entries) {
        return entries.stream().map(this::processScheduledEntry)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.joining());
    }

    @Override
    public String processScheduledEntry(IntervalScheduledEntry entry) {
        if (entry.isWeeklyActive()) {
            return patternScheduleBuilderService.processScheduledEntry(entry);
        }

        if (entry.isMonthlyActive()) {
            String monthDay = config.getMonthlyPatterns().get(entry.getMonthDay().getId());
            String monthDayToAppend = config.getMonthlyPatterns().get(entry.getMonthDay().getId()) + COLON;
            if (monthDay != null) {
                return processScheduledTime(entry.getScheduledTime(), monthDayToAppend);
            }
        }

        return "";
    }

    public boolean isInvalid(IntervalScheduledEntry parameter) {
        if (parameter.isWeeklyActive()) {
            return false;
        }

        return parameter.isMonthlyActive()
                && (parameter.getMonthDay() == null
                || config.getMonthlyPatterns().get(parameter.getMonthDay().getId()) == null);
    }

    @Override
    public boolean isInvalid(List<IntervalScheduledEntry> parameter) {

        for (IntervalScheduledEntry scheduledEntry : parameter) {
            if (isInvalid(scheduledEntry)) {
                return true;
            }
        }

        return patternScheduleBuilderService.isInvalid(
                parameter.stream().map(TimeScheduledEntry.class::cast)
                        .collect(Collectors.toList()));
    }

    @Override
    public List<IntervalScheduledEntry> parse(String parameter) {
        if (parameter == null) {
            return Collections.emptyList();
        }

        Map<String, IntervalScheduledEntry> entries = new HashMap<>();

        for (String value : parameter.split(SEMICOLON)) {
            String pattern = "";
            PeriodType type = PeriodType.TYPE_WEEKLY;
            MonthlyType monthDay = null;
            String scheduledTime;


            String[] interval = value.split(COLON);
            if (interval.length == 2) {
                for (Map.Entry<Integer, String> monthEntry : config.getMonthlyPatterns().entrySet()) {
                    if (monthEntry.getValue().equals(interval[0])) {
                        pattern = interval[0];
                        type = PeriodType.TYPE_MONTHLY;
                        monthDay = IntEnum.fromId(MonthlyType.class, monthEntry.getKey());
                        break;
                    }
                }

                if (type != PeriodType.TYPE_MONTHLY) {
                    pattern = interval[0];
                }
                scheduledTime = formatTime(interval[1], config.getToFormat(), config.getFromFormat());
            } else {
                pattern = StringUtils.EMPTY;
                scheduledTime = formatTime(interval[0], config.getToFormat(), config.getFromFormat());
            }


            IntervalScheduledEntry entry = entries.get(pattern);
            if (entry == null) {
                entry = toIntervalScheduledEntry(pattern, type, monthDay, scheduledTime);
                entries.put(pattern, entry);
            } else {
                List<ScheduledTime> times = entry.getScheduledTime();
                ScheduledTime time = new ScheduledTime();
                time.setTime(scheduledTime);
                time.setActive(true);
                times.add(time);
            }

            entry.setScheduledInterval(new ScheduledInterval(1));
        }

        return new ArrayList<>(entries.values());
    }

    private IntervalScheduledEntry toIntervalScheduledEntry(String pattern, PeriodType type, MonthlyType monthDay, String time) {
        IntervalScheduledEntry entry = new IntervalScheduledEntry();

        entry.setType(type);
        entry.setMonthDay(monthDay);

        if (type == PeriodType.TYPE_WEEKLY) {
            entry.setWeekDay(config.getReverseWeekDays().get(pattern));
        }

        ScheduledTime scheduledTime = new ScheduledTime();
        scheduledTime.setActive(true);
        scheduledTime.setTime(time);

        entry.setScheduledTime(new ArrayList<>(Collections.singletonList(scheduledTime)));

        return entry;
    }
}
