/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.beans.impl;

import org.agnitas.emm.core.velocity.VelocityCheck;

import com.agnitas.beans.ComContentSource;

public class ComContentSourceImpl implements ComContentSource {

	protected int id = 0;
	protected int companyID = -1;
	protected String description = "";
	protected String shortname = "";
	protected String url = "";

	// CONSTRUCTOR:
	public ComContentSourceImpl() {
	}

	// * * * * *
	// SETTER:
	// * * * * *
	@Override
	public void setId(int id) {
		this.id = id;
	}

	@Override
	public void setCompanyID(@VelocityCheck int company) {
		this.companyID = company;
	}

	@Override
	public void setDescription(String desc) {
		this.description = desc;
	}

	@Override
	public void setShortname(String shortname) {
		this.shortname = shortname;
	}

	@Override
	public void setUrl(String url) {
		this.url = url;

		if (this.url != null) {
			this.url = this.url.trim();
		}
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
	public String getDescription() {
		return this.description;
	}

	@Override
	public String getShortname() {
		return this.shortname;
	}

	@Override
	public String getUrl() {
		return this.url;
	}
}
