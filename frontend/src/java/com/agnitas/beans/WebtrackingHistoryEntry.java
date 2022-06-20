/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.beans;

import java.util.Date;

public class WebtrackingHistoryEntry implements Comparable<WebtrackingHistoryEntry> {
    private Date date;
    private String mailingName;
    private int mailingID;
    private String name;
    private Object value;
	private String ipAddress;
    
	public Date getDate() {
		return date;
	}
	
	public void setDate(Date date) {
		this.date = date;
	}

	public String getMailingName() {
		return mailingName;
	}
	
	public void setMailingName(String mailingName) {
		this.mailingName = mailingName;
	}
	
	public int getMailingID() {
		return mailingID;
	}

	public void setMailingID(int mailingID) {
		this.mailingID = mailingID;
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public Object getValue() {
		return value;
	}
	
	public void setValue(Object value) {
		this.value = value;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public String getMailingNameAndID() {
		return mailingName + " (" + mailingID + ")";
	}

    @Override
    public int compareTo(WebtrackingHistoryEntry other) {
        if (other != null) {
        	return date.compareTo(other.getDate());
        }
        return 0;
    }
	
	public boolean isLinkValue() {
		return "Link".equals(value);
	}
}
