/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.web.forms;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.function.BiFunction;

import org.agnitas.util.DateUtilities;

public class FormDateTime {

    private FormDate date;
    private FormTime time;

    public FormDateTime(DateTimeFormatter dateFormatter, DateTimeFormatter timeFormatter) {
        date = new FormDate(dateFormatter);
        time = new FormTime(timeFormatter);
    }

    public FormDateTime() {
        date = new FormDate();
        time = new FormTime();
    }

    public LocalDateTime get() {
        return DateUtilities.merge(date.get(), time.get());
    }

    public LocalDateTime get(BiFunction<FormDate, FormTime, LocalDateTime> resolve) {
        return resolve.apply(date, time);
    }

    public LocalDateTime get(DateTimeFormatter dateFormatter) {
        return DateUtilities.merge(date.get(dateFormatter), time.get());
    }

    public LocalDateTime get(LocalDate defaultDate, DateTimeFormatter dateFormatter) {
        return DateUtilities.merge(date.get(defaultDate, dateFormatter), time.get());
    }

    public void set(LocalDateTime value) {
        set(value, FormDate.DEFAULT_FORMATTER, FormTime.DEFAULT_FORMATTER);
    }

    public void set(LocalDateTime value, DateTimeFormatter dateFormatter) {
        set(value, dateFormatter, FormTime.DEFAULT_FORMATTER);
    }

    public void set(LocalDateTime value, DateTimeFormatter dateFormatter, DateTimeFormatter timeFormatter) {
        if (value == null) {
            date.setDate(null);
            time.setTime(null);
        } else {
            date.set(value.toLocalDate(), dateFormatter);
            time.set(value.toLocalTime(), timeFormatter);
        }
    }

    public Date get(ZoneId zoneId) {
        return DateUtilities.toDate(get(), zoneId);
    }

    public Date get(ZoneId zoneId, DateTimeFormatter dateFormatter) {
        return DateUtilities.toDate(get(dateFormatter), zoneId);
    }

    public Date get(SimpleDateFormat dateFormat) {
        return get(null, dateFormat);
    }

    public Date get(Date defaultValue, SimpleDateFormat dateFormat) {
        Date dateValue = date.get(dateFormat);

        if (dateValue == null) {
            return defaultValue;
        }

        LocalTime timeValue = time.get();

        if (timeValue == null) {
            return dateValue;
        }

        Calendar calendar = DateUtilities.calendar(dateValue, dateFormat.getTimeZone());
        calendar.set(Calendar.HOUR_OF_DAY, timeValue.getHour());
        calendar.set(Calendar.MINUTE, timeValue.getMinute());
        return calendar.getTime();
    }

    public void set(Date value, ZoneId zoneId) {
        set(DateUtilities.toLocalDateTime(value, zoneId));
    }

    public void set(Date value, ZoneId zoneId, DateTimeFormatter dateFormatter) {
        set(DateUtilities.toLocalDateTime(value, zoneId), dateFormatter);
    }

    public void set(Date value, SimpleDateFormat dateFormat) {
        if (value == null) {
            date.setDate(null);
            time.setTime(null);
        } else {
            TimeZone timezone = dateFormat.getTimeZone();
            date.set(value, dateFormat);
            time.set(value, timezone.toZoneId());
        }
    }

    public FormDate getFormDate() {
        return date;
    }

    public FormTime getFormTime() {
        return time;
    }

    public String getDate() {
        return date.getDate();
    }

    public void setDate(String value) {
        date.setDate(value);
    }

    public String getTime() {
        return time.getTime();
    }

    public void setTime(String value) {
        time.setTime(value);
    }
}
