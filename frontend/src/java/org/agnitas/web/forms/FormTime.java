/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.web.forms;

import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Objects;

import org.agnitas.util.DateUtilities;

public class FormTime {
    public static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ofPattern(DateUtilities.HH_MM);

    private DateTimeFormatter formatter;
    private String time;

    public FormTime(DateTimeFormatter formatter) {
        Objects.requireNonNull(formatter, "formatter == null");
        this.formatter = formatter;
    }

    public FormTime() {
        this.formatter = DEFAULT_FORMATTER;
    }

    public LocalTime get() {
        return DateUtilities.parseTime(time, formatter);
    }

    public LocalTime get(LocalTime defaultValue) {
        return DateUtilities.parseTime(time, formatter, defaultValue);
    }

    public LocalTime get(DateTimeFormatter formatterParam) {
        return DateUtilities.parseTime(time, formatterParam);
    }

    public LocalTime get(LocalTime defaultValue, DateTimeFormatter formatterParam) {
        return DateUtilities.parseTime(time, formatterParam, defaultValue);
    }

    public FormTime set(LocalTime time) {
        set(time, formatter);
        return this;
    }

    public void set(LocalTime time, DateTimeFormatter formatter) {
        if (time == null) {
            this.time = null;
        } else {
            this.time = formatter.format(time);
        }
    }

    public void set(Date date, ZoneId zoneId) {
        set(DateUtilities.toLocalTime(date, zoneId), formatter);
    }

    public void set(Date date, ZoneId zoneId, DateTimeFormatter formatter) {
        set(DateUtilities.toLocalTime(date, zoneId), formatter);
    }

    public void set(Date date, SimpleDateFormat dateFormat) {
        if (date == null) {
            this.time = null;
        } else {
            this.time = dateFormat.format(date);
        }
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
