/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.schedule.entity;

import com.agnitas.beans.IntEnum;
import com.agnitas.emm.core.auto_import.bean.TimeScheduledEntry;

public class IntervalScheduledEntry extends TimeScheduledEntry {

    private PeriodType type;

    private MonthlyType monthDay;

    public PeriodType getType() {
        return type;
    }

    public void setType(PeriodType type) {
        this.type = type;
    }

    public MonthlyType getMonthDay() {
        return monthDay;
    }

    public void setMonthDay(MonthlyType monthDay) {
        this.monthDay = monthDay;
    }

    public boolean isWeeklyActive() {
        return type == PeriodType.TYPE_WEEKLY;
    }

    public boolean isMonthlyActive() {
        return type == PeriodType.TYPE_MONTHLY;
    }

    public enum PeriodType implements IntEnum {
        TYPE_WEEKLY(1),
        TYPE_MONTHLY(2);

        private final int id;

        PeriodType(int key) {
            this.id = key;
        }

        @Override
        public int getId() {
            return id;
        }
    }

    public enum MonthlyType implements IntEnum {
        TYPE_MONTHLY_FIRST(1),
        TYPE_MONTHLY_15TH(15),
        TYPE_MONTHLY_LAST(99);

        private final int id;

        MonthlyType(int key) {
            this.id = key;
        }

        @Override
        public int getId() {
            return id;
        }
    }
}
