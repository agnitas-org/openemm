/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/


package org.agnitas.beans;

import java.sql.Timestamp;

public interface AdminEntry {
	int getCompanyID();
	
	void setCompanyID(int companyID);
	
    String getUsername();

    String getFullname();

    String getFirstname();

    String getShortname();

    String getEmail();

    void setEmail(String email);

    int getId();
    
    Timestamp getChangeDate();

	void setChangeDate(Timestamp changeDate);

	Timestamp getCreationDate();

	void setCreationDate(Timestamp creationDate);
	
	Timestamp getLoginDate();

	void setLoginDate(Timestamp loginDate);
	
	boolean isPasswordExpired();

	void setPasswordExpired(boolean passwordExpired);
	
}
