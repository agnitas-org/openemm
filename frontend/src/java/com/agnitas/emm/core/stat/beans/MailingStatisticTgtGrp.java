/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.stat.beans;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MailingStatisticTgtGrp {
	
	private int id;
	private int jobId;
	private int mailingId;
	private int targetGroupId;
	private Date creationDate;
	
	private Map<Integer, StatisticValue> statValues = new HashMap<>();
	private double revenue;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getJobId() {
		return jobId;
	}
	public void setJobId(int jobId) {
		this.jobId = jobId;
	}
	public int getMailingId() {
		return mailingId;
	}
	public void setMailingId(int mailingId) {
		this.mailingId = mailingId;
	}
	public int getTargetGroupId() {
		return targetGroupId;
	}
	public void setTargetGroupId(int targetGroupId) {
		this.targetGroupId = targetGroupId;
	}
	public Date getCreationDate() {
		return creationDate;
	}
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}
	
	public Map<Integer, StatisticValue> getStatValues() {
		return statValues;
	}
	public double getRevenue() {
		return revenue;
	}
	public void setRevenue(double d) {
		this.revenue = d;
	}
}
