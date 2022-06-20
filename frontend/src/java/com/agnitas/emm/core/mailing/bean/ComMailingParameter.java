/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailing.bean;

import java.util.Date;

import org.agnitas.emm.core.velocity.VelocityCheck;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class ComMailingParameter {

	private int mailingInfoID = 0;  //increasing number

	private int mailingID = 0;      //mailing on which the parameter is set, 0 for default values

	@JsonIgnore
	private int companyID = 0;      //need companyID for default values

	private String name;        	//name of the parameter

	private String value;        	//value of the parameter

	private String description; 	//description of the parameter

	@JsonIgnore
	private Date creationDate;

	private Date changeDate;

	@JsonIgnore
	private int creationAdminID; 	//who created the parameter

	@JsonIgnore
	private int changeAdminID; 		// who changed the values
	
	public ComMailingParameter() { }
	
	public ComMailingParameter(Integer mailingInfoID, Integer mailingID, @VelocityCheck Integer companyID, String name, String value, String description, Date creationDate, Date changeDate, Integer creationAdminID, Integer changeAdminID) {
		setMailingInfoID(mailingInfoID);
		setMailingID(mailingID);
		setCompanyID(companyID);
		setName(name);
		setValue(value);
		setDescription(description);
		setCreationDate(creationDate);
		setChangeDate(changeDate);
		setCreationAdminID(creationAdminID);
		setChangeAdminID(changeAdminID);
    }
	
	public int getMailingInfoID() {
		return mailingInfoID;
	}
	
	public void setMailingInfoID(int mailingInfoID) {
		this.mailingInfoID = mailingInfoID;
	}
	
	public int getMailingID() {
		return mailingID;
	}
	
	public void setMailingID(int mailingID) {
		this.mailingID = mailingID;
	}
	
	public int getCompanyID() {
		return companyID;
	}
	
	public void setCompanyID(@VelocityCheck int companyID) {
		this.companyID = companyID;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	public String getValue() {
		return value;
	}
	
	public void setValue(String value) {
		this.value = value;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public Date getCreationDate() {
		return creationDate;
	}
	
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}
	
	public Date getChangeDate() {
		return changeDate;
	}
	
	public void setChangeDate(Date changeDate) {
		this.changeDate = changeDate;
	}
	
	public int getCreationAdminID() {
		return creationAdminID;
	}
	
	public void setCreationAdminID(int creationAdminID) {
		this.creationAdminID = creationAdminID;
	}
	
	public int getChangeAdminID() {
		return changeAdminID;
	}
	
	public void setChangeAdminID(int changeAdminID) {
		this.changeAdminID = changeAdminID;
	}
}
