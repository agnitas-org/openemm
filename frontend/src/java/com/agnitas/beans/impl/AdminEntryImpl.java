/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.beans.impl;

import java.sql.Timestamp;
import java.util.Locale;

import com.agnitas.beans.Admin;
import com.agnitas.beans.AdminEntry;
import org.apache.commons.lang3.StringUtils;

public class AdminEntryImpl implements AdminEntry {

	private final int id;
	private final String shortname;
	private final String username;
	private final String fullname;
	private final String language;
	private final String country;
	private int companyID;
	private String firstname;
	private String email;
	private Timestamp changeDate;
	private Timestamp creationDate;
	private Timestamp loginDate;
	private boolean passwordExpired;
	private String companyName;

    public AdminEntryImpl(Admin admin) {
        this(admin.getCompanyID(), admin.getAdminID(), admin.getUsername(), admin.getFullname(), admin.getFirstName(),
				admin.getShortname(), admin.getEmail(), admin.getCompanyName());
    }

    public AdminEntryImpl(int companyID, int id, String username, String fullname, String firstname, String shortname, String language, String country) {
		this(companyID, id, username, fullname, firstname, shortname, null, null, language, country);
    }

    public AdminEntryImpl(int companyID, int id, String userName, String fullName, String firstName, String shortName,
						  String email, String companyName, String language, String country) {
    	this.companyID = companyID;
        this.username = userName;
        this.fullname = fullName;
        this.firstname = firstName;
        this.shortname = shortName;
        this.id = id;
        this.email = email;
		this.companyName = companyName;
		this.language = language;
		this.country = country;
    }

    @Override
	public int getCompanyID() {
		return companyID;
	}

	@Override
	public void setCompanyID(int companyID) {
		this.companyID = companyID;
	}

	@Override
	public String getUsername() {
        return username;
    }

    @Override
	public String getFullname() {
        return fullname;
    }

    @Override
	public String getShortname() {
        return shortname;
    }

    @Override
	public int getId() {
        return id;
    }

	@Override
	public Timestamp getChangeDate() {
		return changeDate;
	}

	@Override
	public void setChangeDate(Timestamp changeDate) {
		this.changeDate = changeDate;
	}

	@Override
	public Timestamp getCreationDate() {
		return creationDate;
	}

	@Override
	public void setCreationDate(Timestamp creationDate) {
		this.creationDate = creationDate;
	}

    @Override
	public Timestamp getLoginDate() {
		return loginDate;
	}

	@Override
	public void setLoginDate(Timestamp loginDate) {
		this.loginDate = loginDate;
	}

	@Override
	public String getEmail() {
        return email;
    }

    @Override
	public void setEmail(String email) {
        this.email = email;
    }

    @Override
	public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }
	
	@Override
	public String toString()  {
		return username + " (" + id + ")";
	}

	@Override
	public boolean isPasswordExpired() {
		return passwordExpired;
	}

	@Override
	public void setPasswordExpired(boolean passwordExpired) {
		this.passwordExpired = passwordExpired;
	}

	@Override
	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	@Override
	public String getCompanyName() {
		return companyName;
	}

	@Override
	public Locale getLocale() {
		if (StringUtils.isBlank(language)) {
			return null;
		}

		return new Locale(language, country);
	}

}
