/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.external.beans;

import com.agnitas.emm.core.mobile.bean.DeviceClass;

public class ClickStatRow {

	private int clicksGross;
	private int clicksNet;
	private float clicksGrossPercent;
	private float clicksNetPercent;
	private int clicksAnonymous;
	private int columnIndex;
	private int rowIndex;
	private boolean adminLink;
	private boolean deleted;
	private DeviceClass deviceClass;

	public int getClicksGross() {
		return clicksGross;
	}

	public void setClicksGross(int clicksGross) {
		this.clicksGross = clicksGross;
	}

	public int getClicksNet() {
		return clicksNet;
	}

	public void setClicksNet(int clicksNet) {
		this.clicksNet = clicksNet;
	}

	public float getClicksGrossPercent() {
		return clicksGrossPercent;
	}

	public void setClicksGrossPercent(float clicksGrossPercent) {
		this.clicksGrossPercent = clicksGrossPercent;
	}

	public float getClicksNetPercent() {
		return clicksNetPercent;
	}

	public void setClicksNetPercent(float clicksNetPercent) {
		this.clicksNetPercent = clicksNetPercent;
	}

	public int getClicksAnonymous() {
		return clicksAnonymous;
	}

	public void setClicksAnonymous(int clicksAnonymous) {
		this.clicksAnonymous = clicksAnonymous;
	}

	public int getColumnIndex() {
		return columnIndex;
	}

	public void setColumnIndex(int columnIndex) {
		this.columnIndex = columnIndex;
	}

	public int getRowIndex() {
		return rowIndex;
	}

	public void setRowIndex(int rowIndex) {
		this.rowIndex = rowIndex;
	}

	public boolean isAdminLink() {
		return adminLink;
	}

	public void setAdminLink(boolean adminLink) {
		this.adminLink = adminLink;
	}

    public boolean isMobile() {
        return deviceClass == DeviceClass.MOBILE;
    }

    public void setMobile(boolean mobile) {
    	deviceClass = mobile ? DeviceClass.MOBILE : DeviceClass.DESKTOP;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
    
    public int getDeviceClassID() {
        return deviceClass == null ? 0 : deviceClass.getId();
    }
    
    public DeviceClass getDeviceClass() {
        return deviceClass;
    }

    public void setDeviceClassID(int deviceClassID) {
        this.deviceClass = DeviceClass.fromId(deviceClassID);
    }

    public void setDeviceClass(DeviceClass deviceClass) {
        this.deviceClass = deviceClass;
    }
    
    @Override
    public String toString() {
    	return
			"ClicksGross: " + clicksGross
			+ " ClicksNet: " + clicksNet
	    	+ " ClicksAnonymous: " + clicksAnonymous
	    	+ " DeviceClass: " + deviceClass;
    }
	
	public void addClicksGross(int clicksGrossValue) {
		this.clicksGross += clicksGrossValue;
	}
	
	public void addClicksNet(int clicksNetValue) {
		this.clicksNet += clicksNetValue;
	}
	
	public void addClicksAnonymous(int clicksAnonymous) {
		this.clicksAnonymous += clicksAnonymous;
	}
}
