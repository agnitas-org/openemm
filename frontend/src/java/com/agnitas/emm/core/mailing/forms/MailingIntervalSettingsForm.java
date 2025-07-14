/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailing.forms;

import com.agnitas.emm.core.mailing.dao.MailingParameterDao;
import com.agnitas.util.AgnUtils;

public class MailingIntervalSettingsForm {
    /**
     * This array is index with the int values of Calendar days - 1 (Calendar.Sunday=1, Calendar.Saturday=7)
     */
    private boolean[] intervalDays = new boolean[7];
    private int dayOfMonth;
    private int numberOfMonth;
    private Integer weekdayOrdinal;
    private String intervalTime;
    private MailingParameterDao.IntervalType intervalType = MailingParameterDao.IntervalType.None;

    public int getDayOfMonth() {
        return dayOfMonth;
    }

    public void setDayOfMonth(int dayOfMonth) {
        this.dayOfMonth = dayOfMonth;
    }

    public Integer getWeekdayOrdinal() {
        return weekdayOrdinal;
    }

    public void setWeekdayOrdinal(Integer weekdayOrdinal) {
        this.weekdayOrdinal = weekdayOrdinal;
    }

    public int getNumberOfMonth() {
        return numberOfMonth;
    }

    public void setNumberOfMonth(int numberOfMonth) {
        this.numberOfMonth = numberOfMonth;
    }

    public boolean[] getIntervalDays() {
        return intervalDays;
    }

    public void setIntervalDays(boolean[] intervalDays) {
        this.intervalDays = intervalDays;
    }

    public boolean hasSelectedDay() {
        for (boolean intervalDay : intervalDays) {
            if (intervalDay) {
                return true;
            }
        }

        return false;
    }

    public MailingParameterDao.IntervalType getIntervalType() {
        return intervalType;
    }

    public void setIntervalType(MailingParameterDao.IntervalType intervalType) {
        this.intervalType = intervalType;
    }

    public String getIntervalTime() {
        if (intervalTime != null && !AgnUtils.check24HourTime(intervalTime)) {
            intervalTime = null;
        }
        return intervalTime == null ? "00:00" : intervalTime;
    }

    public void setIntervalTime(String intervalTime) {
        this.intervalTime = intervalTime;
    }
}
