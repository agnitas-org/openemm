/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.beans.impl;

import java.util.Date;

import org.agnitas.beans.DatasourceDescription;
import org.agnitas.emm.core.velocity.VelocityCheck;

public class DatasourceDescriptionImpl implements DatasourceDescription {
	protected int id = -1;
	protected int companyID = -1;
	protected int sourcegroupID;
	protected String description;
	protected String description2;
	protected Date creationDate = null;
	protected String url;
	
	// * * * * *
	// SETTER:
	// * * * * *
	@Override
	public void setId(int id) {
		this.id = id;
	}

	@Override
    public void setCompanyID( @VelocityCheck int company) {
        this.companyID=company;
    }

	@Override
	public void setSourcegroupID(int sourcegroup) {
		this.sourcegroupID = sourcegroup;
	}

	@Override
	public void setDescription(String desc) {
		this.description = desc;
	}
	
	@Override
    public void setUrl(String url) {
        this.url = url;
    }

	@Override
	public void setDescription2(String desc) {
		this.description2 = desc;
	}

	@Override
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	// * * * * *
	// GETTER:
	// * * * * *
	@Override
	public int getId() {
		return id;
	}

	@Override
	public int getCompanyID() {
		return companyID;
	}

	@Override
	public int getSourcegroupID() {
		return sourcegroupID;
	}

	@Override
	public String getDescription() {
		return this.description;
	}
	
   @Override
    public String getUrl() {
        return this.url;
    }

	@Override
	public String getDescription2() {
		return this.description2;
	}

	@Override
	public Date getCreationDate() {
		return this.creationDate;
	}
}
