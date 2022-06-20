/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.external.beans;

public class LightMailing {
	private int mailingID;
	private String shortname;
    private String description;
	private String targetExpression;
	private int mailinglistId;
    private int archiveId;
    private int mailingType;

	public int getMailingID() {
		return mailingID;
	}
	
	public void setMailingID(int mailingID) {
		this.mailingID = mailingID;
	}
	
	public String getShortname() {
		return shortname;
	}
	
	public void setShortname(String shortname) {
		this.shortname = shortname;
	}
	
	public String getTargetExpression() {
		return targetExpression;
	}
	
	public void setTargetExpression(String targetExpression) {
		this.targetExpression = targetExpression;
	}
	
	public int getMailinglistId() {
		return mailinglistId;
	}
	
	public void setMailinglistId(int mailinglistId) {
		this.mailinglistId = mailinglistId;
	}
	
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public int getArchiveId() {
        return archiveId;
    }
    
    public void setArchiveId(int archiveId) {
        this.archiveId = archiveId;
    }

    public int getMailingType() {
        return mailingType;
    }

    public void setMailingType(int mailingType) {
        this.mailingType = mailingType;
    }
}
