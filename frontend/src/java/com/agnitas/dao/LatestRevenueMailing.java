/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao;

import java.util.Date;

public class LatestRevenueMailing {
	private int mailingID;
	private Date sendDate;
	private float revenue;
	
	public LatestRevenueMailing(int mailingID, Date sendDate, float revenue) {
		this.mailingID = mailingID;
		this.sendDate = sendDate;
		this.revenue = revenue;
	}

	public int getMailingID() {
		return mailingID;
	}
	
	public Date getSendDate() {
		return sendDate;
	}
	
	public float getRevenue() {
		return revenue;
	}
}
