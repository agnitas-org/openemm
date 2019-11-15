/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.external.beans;

import java.util.Date;

public class SendStatRow {
	
	private String category = "";
	private int categoryindex;	
	private String targetgroup;
	private int targetgroupindex;	
	private int count;
	private double rate;
    private Date dt;

	public SendStatRow() {
	}

	public SendStatRow(String category, int categoryindex, String targetgroup, int targetgroupindex, int count, double rate) {
		this.category = category;
		this.categoryindex = categoryindex;
		this.targetgroup = targetgroup;
		this.targetgroupindex = targetgroupindex;
		this.count = count;
		this.rate = rate;
	}

	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public int getCategoryindex() {
		return categoryindex;
	}
	public void setCategoryindex(int categoryindex) {
		this.categoryindex = categoryindex;
	}
	public String getTargetgroup() {
		return targetgroup;
	}
	public void setTargetgroup(String targetgroup) {
		this.targetgroup = targetgroup;
	}
	public int getTargetgroupindex() {
		return targetgroupindex;
	}
	public void setTargetgroupindex(int targetgroupindex) {
		this.targetgroupindex = targetgroupindex;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public double getRate() {
		return rate;
	}
	public void setRate(double rate) {
		this.rate = rate;
	}
    public Date getDt() {
        return dt;
    }
	public void setDt(Date dt) {
        this.dt = dt;
    }

	@Override
	public String toString() {
		
		return "SendStatRow : \n" + "Category: " + getCategory() + "\n" +
				"CategoryIndex: " + getCategoryindex() + "\n" +
				"Targetgroup: " + getTargetgroup() + "\n" +
				"TargetgroupIndex: " + getTargetgroupindex() + "\n" +
				"Value: " + getCount() + "\n" +
				"Rate: " + getRate() + "\n";
	}
	
}
