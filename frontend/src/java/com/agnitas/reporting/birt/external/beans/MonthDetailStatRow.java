/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.external.beans;

public class MonthDetailStatRow {
	private String date;
	private String shortName;
    private String description;
	private String kiloByte;
	private int mailingCount;
	private String mailtype;
    private int openings;
    private int clickRecipients;
    private int mailingId;

    public String getDate() {
		return date;
	}
    
	public void setDate(String date) {
		this.date = date;
	}
	
	public String getShortName() {
		return shortName;
	}
	
	public void setShortName(String shortName) {
		this.shortName = shortName;
	}
	
	public String getKiloByte() {
		return kiloByte;
	}
	
	public void setKiloByte(String i) {
		this.kiloByte = i;
	}
	
	public int getMailingCount() {
		return mailingCount;
	}
	
	public void setMailingCount(int mailingCount) {
		this.mailingCount = mailingCount;
	}
	
	public void setMailtype(String mailtype) {
		this.mailtype = mailtype;
	}
	
	public String getMailtype() {
		return mailtype;
	}

    public int getOpenings() {
        return openings;
    }

    public void setOpenings(int openings) {
        this.openings = openings;
    }

    public int getClickRecipients() {
        return clickRecipients;
    }

    public void setClickRecipients(int clickRecipients) {
        this.clickRecipients = clickRecipients;
    }

    public int getMailingId() {
        return mailingId;
    }

    public void setMailingId(int mailingId) {
        this.mailingId = mailingId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
