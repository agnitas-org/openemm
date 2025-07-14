/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.calendar.form;

import static com.agnitas.util.DateUtilities.parseDate;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import com.agnitas.util.DateUtilities;

public class DashboardCalendarForm {

    private int dayMailingsLimit;
    private String start;
    private String end;
    private boolean loadComments;
    private Boolean showUnsentList;
    private Boolean showUnsentPlanned;

    public int getDayMailingsLimit() {
        return dayMailingsLimit;
    }

    public void setDayMailingsLimit(int dayMailingsLimit) {
        this.dayMailingsLimit = dayMailingsLimit;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public boolean isLoadComments() {
        return loadComments;
    }

    public void setLoadComments(boolean loadComments) {
        this.loadComments = loadComments;
    }

    public Boolean getShowUnsentList() {
        return showUnsentList;
    }

    public void setShowUnsentList(Boolean showUnsentList) {
        this.showUnsentList = showUnsentList;
    }

    public Boolean getShowUnsentPlanned() {
        return showUnsentPlanned;
    }

    public void setShowUnsentPlanned(Boolean showUnsentPlanned) {
        this.showUnsentPlanned = showUnsentPlanned;
    }

    public Date getStartDate(ZoneId zoneId, DateTimeFormatter formatter) {
        return DateUtilities.toDate(parseDate(start, formatter).atStartOfDay(), zoneId);
    }

    public Date getEndDate(ZoneId zoneId, DateTimeFormatter formatter) {
        return DateUtilities.toDate(parseDate(end, formatter).plusDays(1).atStartOfDay(), zoneId);
    }
}
