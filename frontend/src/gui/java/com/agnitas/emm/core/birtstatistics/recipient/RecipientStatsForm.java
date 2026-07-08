/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtstatistics.recipient;

import java.time.LocalDate;

import com.agnitas.emm.core.birtstatistics.DateMode;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;

public class RecipientStatsForm {

    private DateMode dateMode = DateMode.SELECT_MONTH;
    private MediaTypes mediaType;
    private int mailinglistId;
    private int targetId;
    private int year;
    private int month = -1;
    private String startDate;
    private String endDate;

    public DateMode getDateMode() {
        return dateMode;
    }

    public void setDateMode(DateMode dateMode) {
        this.dateMode = dateMode;
    }

    public MediaTypes getMediaType() {
        return mediaType;
    }

    public void setMediaType(MediaTypes mediaType) {
        this.mediaType = mediaType;
    }

    public int getMailinglistId() {
        return mailinglistId;
    }

    public void setMailinglistId(int mailinglistId) {
        this.mailinglistId = mailinglistId;
    }

    public int getTargetId() {
        return targetId;
    }

    public void setTargetId(int targetId) {
        this.targetId = targetId;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        if (month == -1) {
            return LocalDate.now().getMonthValue();
        }
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }
}
