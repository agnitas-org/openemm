/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.beans.impl;

import java.util.Date;

import org.agnitas.beans.Mailinglist;
import org.agnitas.emm.core.velocity.VelocityCheck;

public class MailinglistImpl implements Mailinglist {
	/**
	 * ID of the mailinglist.
	 */
	protected int id;
	
	/**
	 * Company ID of the account
	 */
	protected int companyID;
	
	/**
	 * shortname to be displayed in mailinglist list.
	 */
	protected String shortname;
	
	/**
	 * a short mailinglist description for the frontend
	 */
	protected String description = "";
	
	/**
	 * Last changedate of this entry
	 */
	protected Date changeDate;

	/**
	 * Creationdate of this entry
	 */
	protected Date creationDate;

	protected boolean isRemoved;

	@Override
	public int getId() {
		return id;
	}

	@Override
	public void setId(int listid) {
		this.id = listid;
	}

	@Override
	public int getCompanyID() {
		return companyID;
	}
	
	@Override
	public void setCompanyID(@VelocityCheck int cid) {
		companyID = cid;
	}

	@Override
	public String getShortname() {
		return shortname;
	}

	@Override
	public void setShortname(String shortname) {
		this.shortname = shortname;
	}

	@Override
	public String getDescription() {
		return description;
	}
	
	@Override
	public void setDescription(String description) {
		if (description != null) {
			this.description = description;
		} else {
			this.description = "";
		}
	}

	@Override
	public Date getChangeDate() {
		return changeDate;
	}
	
	@Override
	public void setChangeDate(Date changeDate) {
		this.changeDate = changeDate;
	}

	@Override
	public Date getCreationDate() {
		return creationDate;
	}

	@Override
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	@Override
	public boolean isRemoved() {
		return isRemoved;
	}

	@Override
	public void setRemoved(boolean removed) {
		isRemoved = removed;
	}
}
