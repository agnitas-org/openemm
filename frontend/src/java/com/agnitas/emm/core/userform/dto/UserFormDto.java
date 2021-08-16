/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.userform.dto;

import java.util.Date;

public class UserFormDto {
	private int id;
	private String name;
	private String description;
	private Date creationDate;
	private Date changeDate;
	private boolean active;
	
	private ResultSettings successSettings = new ResultSettings(true);
	private ResultSettings errorSettings = new ResultSettings(true);

	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
	
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}
	
	public Date getCreationDate() {
		return creationDate;
	}
	
	public void setChangeDate(Date changeDate) {
		this.changeDate = changeDate;
	}
	
	public Date getChangeDate() {
		return changeDate;
	}
	
	public void setActive(boolean active) {
		this.active = active;
	}
	
	public boolean isActive() {
		return active;
	}
	
	public ResultSettings getSuccessSettings() {
		return successSettings;
	}
	
	public void setSuccessSettings(ResultSettings successSettings) {
		this.successSettings = successSettings;
	}
	
	public ResultSettings getErrorSettings() {
		return errorSettings;
	}
	
	public void setErrorSettings(ResultSettings errorSettings) {
		this.errorSettings = errorSettings;
	}
}
