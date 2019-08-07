/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.external.dataset;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.agnitas.util.DateUtilities;

import com.agnitas.reporting.birt.external.beans.ClickStatRow;

public class TimeBasedClickStatRow extends ClickStatRow {
	private String targetgroup;
	private int targetgroupID;
	private Date clickTime;
	
	public String getTargetgroup() {
		return targetgroup;
	}
	
	public void setTargetgroup(String targetgroup) {
		this.targetgroup = targetgroup;
	}
	
	public int getTargetgroupID() {
		return targetgroupID;
	}
	
	public void setTargetgroupID(int targetgroupID) {
		this.targetgroupID = targetgroupID;
	}
	
	public Date getClickTime() {
		return clickTime;
	}
	
	public void setClickTime(Date clickTime) {
		this.clickTime = clickTime;
	}
    
    @Override
    public String toString() {
    	return 
			"ClicksGross: " + getClicks_gross()
			+ " ClicksNet: " + getClicks_net()
			+ " TotalClicksGross: " + getTotal_clicks_gros()
	    	+ " TotalClicksNet: " + getTotal_clicks_net()
	    	+ " DeviceClass: " + getDeviceClass()
	    	+ " Time: " + new SimpleDateFormat(DateUtilities.DD_MM_YYYY_HH_MM_SS).format(clickTime);
    }
}
