/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.beans;

import java.util.Date;

import org.agnitas.emm.core.velocity.VelocityCheck;

public class ComRecommendation {
	private int recommendationId;
	private Date creationDate;
	private int companyId;
	private String keyname;
	private String shortname;
	private Date startDate;
	private Date endDate;
	
	public int getId() {
		return recommendationId;
	}
	
	public void setId(int recommendationId) {
		this.recommendationId = recommendationId;
	}
	
	public Date getCreationDate() {
		return creationDate;
	}
	
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}
	
	public int getCompanyId() {
		return companyId;
	}
	
	public void setCompanyId(@VelocityCheck int companyId) {
		this.companyId = companyId;
	}
	
	public String getKeyname() {
		return keyname;
	}
	
	public void setKeyname(String keyname) {
		this.keyname = keyname;
	}
	
	public String getShortname() {
		return shortname;
	}
	
	public void setShortname(String shortname) {
		this.shortname = shortname;
	}
	
	public Date getStartDate() {
		return startDate;
	}
	
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	
	public Date getEndDate() {
		return endDate;
	}
	
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
}
