/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.beans.impl;

import org.agnitas.beans.Company;
import org.agnitas.emm.core.velocity.VelocityCheck;

public class CompanyImpl implements Company {
	private int companyID;
	private int creatorID;
	private String shortname;
	private String description;
	private String status;
	private int mailtracking;
	private Number minimumSupportedUIDVersion;
	private int maxRecipients = 10000;
	private String rdirDomain;
	private String mailloopDomain;
	private int useUTF;

	@Override
	public int getId() {
		return companyID;
	}
	
	@Override
	public void setId(@VelocityCheck int id) {
		companyID = id;
	}

	@Override
	public int getCreatorID() {
		return creatorID;
	}
	
	@Override
	public void setCreatorID(int creatorID) {
		this.creatorID = creatorID;
	}

	@Override
	public String getShortname() {
		return shortname;
	}
	
	@Override
	public void setShortname(String name) {
		shortname = name;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public void setDescription(String sql) {
		description = sql;
	}

	@Override
	public String getStatus() {
		return status;
	}

	@Override
	public void setStatus(String status) {
		this.status = status;
	}

	@Override
	public int getMailtracking() {
		return mailtracking;
	}

	@Override
	public void setMailtracking(int tracking) {
		this.mailtracking = tracking;
	}

	@Override
	public Number getMinimumSupportedUIDVersion() {
		return minimumSupportedUIDVersion;
	}

	@Override
	public void setMinimumSupportedUIDVersion(Number minimumSupportedUIDVersion) {
		this.minimumSupportedUIDVersion = minimumSupportedUIDVersion;
	}

	@Override
	public int getMaxRecipients() {
		return maxRecipients;
	}

	@Override
	public void setMaxRecipients(int maxRecipients) {
		this.maxRecipients = maxRecipients;
	}

	@Override
	public String getRdirDomain() {
		return rdirDomain;
	}

	@Override
	public void setRdirDomain(String rdirDomain) {
		this.rdirDomain = rdirDomain;
	}

	@Override
	public String getMailloopDomain() {
		return mailloopDomain;
	}

	@Override
	public void setMailloopDomain(String mailloopDomain) {
		this.mailloopDomain = mailloopDomain;
	}

	@Override
	public int getUseUTF() {
		return useUTF;
	}

	@Override
	public void setUseUTF(int useUTF) {
		this.useUTF = useUTF;
	}

	@Override
	public String toString() {
		return "\"" + shortname + "\" (ID: " + companyID + ")";
	}
}
