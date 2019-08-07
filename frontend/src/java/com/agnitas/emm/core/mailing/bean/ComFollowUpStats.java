/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailing.bean;

import java.sql.Timestamp;

import org.agnitas.emm.core.velocity.VelocityCheck;

public class ComFollowUpStats {
	private int resultID;	// 
	private int companyID;	
	private int basemailID;	
	private int followupID;
	private Timestamp creationDate;	// when was this entry created
	private int duration;		// how long was this statement running
	private String sessionID;	// which session wrote this entry
	private String statement;	// sql statement for calculating the stats
	private int resultValue;	// recipients which would get a followup.
	
	public int getResultID() {
		return resultID;
	}
	public void setResultID(int resultID) {
		this.resultID = resultID;
	}
	public int getCompanyID() {
		return companyID;
	}
	public void setCompanyID(@VelocityCheck int companyID) {
		this.companyID = companyID;
	}
	public Timestamp getCreationDate() {
		return creationDate;
	}
	public void setCreationDate(Timestamp creationDate) {
		this.creationDate = creationDate;
	}
	public int getDuration() {
		return duration;
	}
	public void setDuration(int duration) {
		this.duration = duration;
	}
	public String getSessionID() {
		return sessionID;
	}
	public void setSessionID(String sessionID) {
		this.sessionID = sessionID;
	}
	public String getStatement() {
		return statement;
	}
	public void setStatement(String statement) {
		this.statement = statement;
	}
	public int getResultValue() {
		return resultValue;
	}
	public void setResultValue(int resultValue) {
		this.resultValue = resultValue;
	}
	public int getBasemailID() {
		return basemailID;
	}
	public void setBasemailID(int basemailID) {
		this.basemailID = basemailID;
	}
	public int getFollowupID() {
		return followupID;
	}
	public void setFollowupID(int followupID) {
		this.followupID = followupID;
	}	
	
	
}
