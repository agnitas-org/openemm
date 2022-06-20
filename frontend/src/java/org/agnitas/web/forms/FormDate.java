/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.web.forms;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Objects;

import org.agnitas.util.DateUtilities;

public class FormDate {
    public static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ofPattern(DateUtilities.YYYY_MM_DD);

    private DateTimeFormatter formatter;
    private String date;

    public FormDate(DateTimeFormatter formatter) {
        Objects.requireNonNull(formatter, "formatter == null");
        this.formatter = formatter;
    }

    public FormDate() {
        this.formatter = DEFAULT_FORMATTER;
    }

    public LocalDate get() {
        return DateUtilities.parseDate(date, formatter);
    }

    public LocalDate get(LocalDate defaultValue) {
        return DateUtilities.parseDate(date, formatter, defaultValue);
    }

    public LocalDate get(DateTimeFormatter formatterToUse) {
        return DateUtilities.parseDate(date, formatterToUse);
    }

    public LocalDate get(LocalDate defaultValue, DateTimeFormatter formatterToUse) {
        return DateUtilities.parseDate(date, formatterToUse, defaultValue);
    }

    public void set(LocalDate date) {
        set(date, formatter);
    }

    public void set(LocalDate date, DateTimeFormatter formatter) {
        if (date == null) {
            this.date = null;
        } else {
            this.date = formatter.format(date);
        }
    }

    public void set(Date date, ZoneId zoneId) {
        set(DateUtilities.toLocalDate(date, zoneId), formatter);
    }

    public void set(Date date, ZoneId zoneId, DateTimeFormatter formatter) {
        set(DateUtilities.toLocalDate(date, zoneId), formatter);
    }

    public void set(Date date, SimpleDateFormat dateFormat) {
        if (date == null) {
            this.date = null;
        } else {
            this.date = dateFormat.format(date);
        }
    }

    public Date get(ZoneId zoneId) {
        return DateUtilities.toDate(get(), zoneId);
    }

    public Date get(ZoneId zoneId, DateTimeFormatter formatterToUse) {
        return DateUtilities.toDate(get(formatterToUse), zoneId);
    }

    public Date get(SimpleDateFormat dateFormat) {
        return DateUtilities.parse(date, dateFormat);
    }

    public Date get(Date defaultValue, SimpleDateFormat dateFormat) {
        return DateUtilities.parse(date, dateFormat, defaultValue);
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setFormatter(DateTimeFormatter formatter) {
        this.formatter = formatter;
    }
}
