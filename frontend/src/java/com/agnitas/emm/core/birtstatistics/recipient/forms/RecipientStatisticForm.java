/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtstatistics.recipient.forms;

import com.agnitas.emm.core.birtstatistics.DateMode;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.web.forms.BirtStatisticForm;
import org.agnitas.web.forms.FormDate;
import org.apache.commons.lang.StringUtils;

public class RecipientStatisticForm extends BirtStatisticForm {
    
    public static final String DEFAULT_REPORT_NAME = "recipient_progress.rptdesign";
    
    public RecipientStatisticForm() {
        setReportName(DEFAULT_REPORT_NAME);
    }
    
    /**
     * Selected date mode, values of {@link DateMode}
     */
    private DateMode dateSelectMode = DateMode.SELECT_MONTH;
    /**
     * Restrict report by mediatype values of {@link MediaTypes}
     */
    private int mediaType;
    /**
     * Selected year (one month period).
     */
    private int year;
    /**
     * Selected the month-of-year field from 0 to 11.
     */
    private int month = -1;
    /**
     * Selected beginning date of period (arbitrary selected period).
     */
    private FormDate startDate = new FormDate();
    /**
     * Selected end date of period (arbitrary selected period).
     */
    private FormDate endDate = new FormDate();

    public DateMode getDateMode() {
        return dateSelectMode;
    }

    public String getDateSelectMode() {
        return (null != dateSelectMode) ? dateSelectMode.toString() : null;
    }

    public FormDate getEndDate() {
        return endDate;
    }

    public int getMediaType() {
        return mediaType;
    }

    public int getMonth() {
        return month;
    }

    public int getMonthValue() {
        return month + 1;
    }

    public FormDate getStartDate() {
        return startDate;
    }

    public int getYear() {
        return year;
    }

    public void setDateMode(DateMode dateMode) {
        this.dateSelectMode = dateMode;
    }

    public void setDateSelectMode(String dateSelectMode) {
        if (StringUtils.isEmpty(dateSelectMode)) {
            this.dateSelectMode = DateMode.SELECT_MONTH;
        } else {
            this.dateSelectMode = DateMode.valueOf(dateSelectMode);
        }
    }

    public void setMediaType(int mediaType) {
        this.mediaType = mediaType;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public void setYear(int year) {
        this.year = year;
    }
}
