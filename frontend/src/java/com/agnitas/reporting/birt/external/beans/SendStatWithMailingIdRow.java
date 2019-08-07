/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.external.beans;

public class SendStatWithMailingIdRow extends SendStatRow {
    private int mailingId;

	public SendStatWithMailingIdRow() {
	}

	public SendStatWithMailingIdRow(String category, int categoryindex, String targetgroup, int targetgroupindex, int count,
									double rate, int mailingId) {
		super(category, categoryindex, targetgroup, targetgroupindex, count, rate);
		this.mailingId = mailingId;
	}

	public SendStatWithMailingIdRow(SendStatRow sendStatRow, int mailingId) {
		super(sendStatRow.getCategory(), sendStatRow.getCategoryindex(), sendStatRow.getTargetgroup(), sendStatRow.getTargetgroupindex(),
				sendStatRow.getCount(), sendStatRow.getRate());
		this.mailingId = mailingId;
    }

    public int getMailingId() {
        return mailingId;
    }

    public void setMailingId(int mailingId) {
        this.mailingId = mailingId;
    }
}
