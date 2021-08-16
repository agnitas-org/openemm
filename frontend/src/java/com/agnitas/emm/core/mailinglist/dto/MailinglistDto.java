/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailinglist.dto;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.agnitas.emm.core.mediatypes.common.MediaTypes;

public class MailinglistDto {
	
	private int id;
	
	private String shortname;
	
	private String description;
	
	private Date changeDate;
	
	private Date creationDate;
	
	private int targetId;

	private boolean frequencyCounterEnabled;
	
	private Set<MediaTypes> mediatypes = new HashSet<>();
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public String getShortname() {
		return shortname;
	}
	
	public void setShortname(String shortname) {
		this.shortname = shortname;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public Date getChangeDate() {
		return changeDate;
	}
	
	public void setChangeDate(Date changeDate) {
		this.changeDate = changeDate;
	}
	
	public Date getCreationDate() {
		return creationDate;
	}
	
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}
	
	public int getTargetId() {
		return targetId;
	}
	
	public void setTargetId(int targetId) {
		this.targetId = targetId;
	}

	public boolean isFrequencyCounterEnabled() {
		return frequencyCounterEnabled;
	}

	public void setFrequencyCounterEnabled(boolean frequencyCounterEnabled) {
		this.frequencyCounterEnabled = frequencyCounterEnabled;
	}

	public Set<MediaTypes> getMediatypes() {
		return mediatypes;
	}

	public void setMediatypes(Set<MediaTypes> mediatypes) {
		this.mediatypes = mediatypes;
	}
}
