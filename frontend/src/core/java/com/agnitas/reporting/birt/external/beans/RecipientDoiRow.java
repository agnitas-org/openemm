/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.external.beans;

public class RecipientDoiRow {

    private String type;
    private int percent;
    private int amount;

    private boolean isTotal;

    public RecipientDoiRow(String type, int percent, int amount) {
        this.type = type;
        this.percent = percent;
        this.amount = amount;
    }

    public RecipientDoiRow(String type, int percent, int amount, boolean isTotal) {
        this(type, percent, amount);
        this.isTotal = isTotal;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getPercent() {
        return percent;
    }

    public void setPercent(int percent) {
        this.percent = percent;
    }

    public int getAmount() {
        return amount;
    }

    public boolean isTotal() {
        return isTotal;
    }
}
