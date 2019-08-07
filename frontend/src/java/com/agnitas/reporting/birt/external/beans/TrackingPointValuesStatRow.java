/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.external.beans;

import java.util.Date;

public class TrackingPointValuesStatRow extends CategoryTwoValues  {
	
	private String targetgroup;

	private String currency;
	
	private int format;

	private int targetgroup_index;
	
	private int targetgroup_id;
	
	private Date date;

    private String pageTag;
		
	public String getTargetgroup() {
		return targetgroup;
	}

	public void setTargetgroup(String targetgroup) {
		this.targetgroup = targetgroup;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public int getFormat() {
		return format;
	}

	public void setFormat(int format) {
		this.format = format;
	}

	public int getTargetgroup_index() {
		return targetgroup_index;
	}

	public void setTargetgroup_index(int targetgroup_index) {
		this.targetgroup_index = targetgroup_index;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public int getTargetgroup_id() {
		return targetgroup_id;
	}

	public void setTargetgroup_id(int targetgroup_id) {
		this.targetgroup_id = targetgroup_id;
	}

    public String getPageTag() {
        return pageTag;
    }

    public void setPageTag(String pageTag) {
        this.pageTag = pageTag;
    }
}
