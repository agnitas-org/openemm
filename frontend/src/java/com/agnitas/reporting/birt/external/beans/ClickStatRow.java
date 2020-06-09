/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.external.beans;

import com.agnitas.emm.core.mobile.bean.DeviceClass;

public class ClickStatRow {
	private int clicks_gross;
	private int clicks_net;
	private float clicks_gross_percent;
	private float clicks_net_percent;
	private int total_clicks_gros;
	private int total_clicks_net;
	private int clicks_anonymous;
	private int column_index;
	private int row_index;
	private boolean admin_link;
	private boolean deleted;
	private DeviceClass deviceClass;

	public int getClicks_gross() {
		return clicks_gross;
	}

	public void setClicks_gross(int clicks_gross) {
		this.clicks_gross = clicks_gross;
	}

	public int getClicks_net() {
		return clicks_net;
	}

	public void setClicks_net(int clicks_net) {
		this.clicks_net = clicks_net;
	}

	public float getClicks_gross_percent() {
		return clicks_gross_percent;
	}

	public void setClicks_gross_percent(float clicks_gross_percent) {
		this.clicks_gross_percent = clicks_gross_percent;
	}

	public float getClicks_net_percent() {
		return clicks_net_percent;
	}

	public void setClicks_net_percent(float clicks_net_percent) {
		this.clicks_net_percent = clicks_net_percent;
	}

	public int getClicks_anonymous() {
		return clicks_anonymous;
	}

	public void setClicks_anonymous(int clicks_anonymous) {
		this.clicks_anonymous = clicks_anonymous;
	}

	public int getTotal_clicks_gros() {
		return total_clicks_gros;
	}

	public void setTotal_clicks_gros(int total_clicks_gros) {
		this.total_clicks_gros = total_clicks_gros;
	}

	public int getTotal_clicks_net() {
		return total_clicks_net;
	}

	public void setTotal_clicks_net(int total_clicks_net) {
		this.total_clicks_net = total_clicks_net;
	}

	public int getColumn_index() {
		return column_index;
	}

	public void setColumn_index(int column_index) {
		this.column_index = column_index;
	}

	public int getRow_index() {
		return row_index;
	}

	public void setRow_index(int row_index) {
		this.row_index = row_index;
	}

	public boolean isAdmin_link() {
		return admin_link;
	}

	public void setAdmin_link(boolean admin_link) {
		this.admin_link = admin_link;
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
			"ClicksGross: " + clicks_gross
			+ " ClicksNet: " + clicks_net
			+ " TotalClicksGross: " + total_clicks_gros
	    	+ " TotalClicksNet: " + total_clicks_net
	    	+ " ClicksAnonymous: " + clicks_anonymous
	    	+ " DeviceClass: " + deviceClass;
    }
	
	public void addClicks_gross(int clicksGrossValue) {
		this.clicks_gross += clicksGrossValue;
	}
	
	public void addClicks_net(int clicksNetValue) {
		this.clicks_net += clicksNetValue;
	}
	
	public void addClicks_anonymous(int clicksAnonymous) {
		this.clicks_anonymous += clicksAnonymous;
	}
}
