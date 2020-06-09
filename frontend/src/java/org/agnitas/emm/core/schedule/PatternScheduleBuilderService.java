/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.schedule;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.agnitas.emm.core.autoimport.web.beans.ScheduledInterval;
import com.agnitas.emm.core.autoimport.web.beans.ScheduledTime;
import com.agnitas.emm.core.autoimport.web.beans.TimeScheduledEntry;

public class PatternScheduleBuilderService implements ScheduleBuilderService<String, List<TimeScheduledEntry>> {

    public static final int ONE_HOUR_IN_SECONDS = 3600;
    public static final int EVERY_DAY = 0;
    private static final String COLON = ":";

    private static final String SEMICOLON = ";";

    private Map<Integer, String> weekDays;
    private Map<String, Integer> reverseWeekDays;
    private DateTimeFormatter fromFormat;
    private DateTimeFormatter toFormat;

    public PatternScheduleBuilderService(Map<Integer, String> weekDays, String fromFormat, String toFormat) {
        this.weekDays = weekDays;
        this.fromFormat = DateTimeFormatter.ofPattern(fromFormat);
        this.toFormat = DateTimeFormatter.ofPattern(toFormat);
        this.reverseWeekDays = reverseWeekDays(weekDays);
    }

    @Override
    public String buildSchedule(List<TimeScheduledEntry> entries) {
        return entries.stream().map(this::processScheduledEntry)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.joining(SEMICOLON));
    }

    @Override
    public List<TimeScheduledEntry> parse(String parameter) {
        if (parameter == null) {
            return Collections.emptyList();
        }

        Map<String, TimeScheduledEntry> entries = new HashMap<>();

        for (String value : parameter.split(SEMICOLON)) {
            String day, scheduledTime;

            String[] dayTime = value.split(COLON);
            if (dayTime.length == 2) {
                day = dayTime[0];
                scheduledTime = formatTime(dayTime[1], toFormat, fromFormat);
            } else {
                day = StringUtils.EMPTY;
                scheduledTime = formatTime(dayTime[0], toFormat, fromFormat);
            }

            TimeScheduledEntry entry = entries.get(day);
            if (entry == null) {
                entry = toTimeScheduledEntry(reverseWeekDays.get(day), scheduledTime);
                entries.put(day, entry);
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

    @Override
    public boolean isInvalid(List<TimeScheduledEntry> parameter) {
        Map<Integer, List<LocalTime>> scheduledTimeByDays = extractTimesByDay(parameter);
        List<LocalTime> everyDay = scheduledTimeByDays.get(EVERY_DAY);

        if (Objects.isNull(everyDay)) {
            everyDay = new ArrayList<>();
        }

        for (List<LocalTime> scheduledTimes : scheduledTimeByDays.values()) {
            if (isInvalidDayTimes(everyDay, scheduledTimes)) {
                return true;
            }
        }
        return false;
    }

    private Map<Integer, List<LocalTime>> extractTimesByDay(List<TimeScheduledEntry> parameter) {
        Map<Integer, List<LocalTime>> times = extractActiveTimes(parameter);
        extractTimesFromActiveInterval(parameter)
                .forEach((day, intervals) ->
                        times.merge(day, intervals, this::mergeDuplicateDays));

        return times;
    }

    private Map<Integer, List<LocalTime>> extractActiveTimes(List<TimeScheduledEntry> parameter) {
        return parameter.stream()
                .collect(Collectors.toMap(
                        TimeScheduledEntry::getWeekDay,
                        entry -> convertScheduledToLocalTime(entry.getScheduledTime()),
                        this::mergeDuplicateDays));
    }

    private List<LocalTime> convertScheduledToLocalTime(List<ScheduledTime> scheduled) {
        return scheduled.stream()
                .filter(ScheduledTime::isActive)
                .map(ScheduledTime::getTime)
                .map(LocalTime::parse)
                .collect(Collectors.toList());
    }

    private <T> List<T> mergeDuplicateDays(List<T> firstTimes, List<T> secondTimes) {
        firstTimes.addAll(secondTimes);
        return firstTimes;
    }

    private Map<Integer, List<LocalTime>> extractTimesFromActiveInterval(List<TimeScheduledEntry> parameter) {
        return parameter.stream()
                .collect(Collectors.toMap(
                        TimeScheduledEntry::getWeekDay,
                        entry -> convertIntervalToLocalTime(entry.getScheduledInterval()),
                        this::mergeDuplicateDays));
    }

    private List<LocalTime> convertIntervalToLocalTime(ScheduledInterval scheduledInterval) {
        List<LocalTime> times = new ArrayList<>();
        if (scheduledInterval.isActive()) {
            int interval = scheduledInterval.getInterval();
            for (int hours = 0; hours < 24; hours += interval) {
                times.add(LocalTime.of(hours, 0));
            }
        }
        return times;
    }

    private boolean isInvalidDayTimes(List<LocalTime> baseSet, List<LocalTime> scheduledTimes) {
        Set<LocalTime> times = new HashSet<>(baseSet);
        times.addAll(scheduledTimes);
        return times.stream().anyMatch(time -> isInvalidTime(time, times));
    }

    private boolean isInvalidTime(LocalTime currentTime, Set<LocalTime> times) {
        return times.stream()
                .filter(time -> !currentTime.equals(time))
                .anyMatch(time -> Math.abs(ChronoUnit.SECONDS.between(currentTime, time)) < ONE_HOUR_IN_SECONDS);
    }

    private TimeScheduledEntry toTimeScheduledEntry(int weekDay, String time) {
        TimeScheduledEntry entry = new TimeScheduledEntry();

        ScheduledTime scheduledTime = new ScheduledTime();
        scheduledTime.setActive(true);
        scheduledTime.setTime(time);

        entry.setWeekDay(weekDay);
        entry.setScheduledTime(new ArrayList<>(Collections.singletonList(scheduledTime)));

        return entry;
    }

    private String processScheduledEntry(TimeScheduledEntry entry) {
        String weekDay = weekDays.get(entry.getWeekDay());
        String weekDayToAppend = StringUtils.isBlank(weekDay) ? StringUtils.EMPTY : weekDay + COLON;

        if (entry.getScheduledInterval().isActive()) {
            return processScheduledInterval(entry.getScheduledInterval(), weekDayToAppend);
        } else {
            return processScheduledTime(entry.getScheduledTime(), weekDayToAppend);
        }
    }

    private String processScheduledInterval(ScheduledInterval interval, String weekDay) {
        List<String> patterns = new ArrayList<>();

        int integerInterval = interval.getInterval();
        for (int hours = 0; hours < 24; hours = hours + integerInterval) {
            patterns.add(weekDay + toFormat.format(LocalTime.of(hours, 0)));
        }

        return StringUtils.join(patterns, SEMICOLON);
    }

    private String processScheduledTime(List<ScheduledTime> scheduledTimes, String weekDay) {
        List<String> patterns = new ArrayList<>();
        for (ScheduledTime time : scheduledTimes) {
            if (time.isActive() && StringUtils.isNotBlank(time.getTime())) {
                patterns.add(weekDay + formatTime(time.getTime(), fromFormat, toFormat));
            }
        }
        return StringUtils.join(patterns, SEMICOLON);
    }

    private String formatTime(String time, DateTimeFormatter fromFormatToUse, DateTimeFormatter toFormatToUse) {
        LocalTime localTime = LocalTime.parse(time, fromFormatToUse);
        return toFormatToUse.format(localTime);
    }

    private Map<String, Integer> reverseWeekDays(Map<Integer, String> weekDaysToReverse) {
        return weekDaysToReverse.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
    }
}
