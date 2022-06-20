/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.recipient.dto;

public class RecipientFrequencyCounterDto {
    private int dayFrequencyCounter;
    private int weekFrequencyCounter;
    private int monthFrequencyCounter;

    public int getWeekFrequencyCounter() {
        return weekFrequencyCounter;
    }

    public int getMonthFrequencyCounter() {
        return monthFrequencyCounter;
    }

    public int getDayFrequencyCounter() {
        return dayFrequencyCounter;
    }

    public void setWeekFrequencyCounter(int weekFrequencyCounter) {
        this.weekFrequencyCounter = weekFrequencyCounter;
    }

    public void setMonthFrequencyCounter(int monthFrequencyCounter) {
        this.monthFrequencyCounter = monthFrequencyCounter;
    }

    public void setDayFrequencyCounter(int dayFrequencyCounter) {
        this.dayFrequencyCounter = dayFrequencyCounter;
    }
}
