/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.service.impl;


import java.util.List;

import org.agnitas.beans.AdminEntry;
import org.agnitas.beans.AdminGroup;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.dao.ComAdminGroupDao;
import com.agnitas.service.ComCSVService;

public class ComCSVServiceImpl implements ComCSVService {
    public static final String DEFAULT_SEPARATOR = ",";
    public static final String DEFAULT_DELIMITER = "\"";
    /** DAO for accessing admin group data. */
	protected ComAdminGroupDao adminGroupDao;
	
    @Required
	public void setAdminGroupDao(ComAdminGroupDao adminGroupDao) {
		this.adminGroupDao = adminGroupDao;
	}

    @Override
	public String getUserCSV(List<AdminEntry> users){
        StringBuilder sb = new StringBuilder();
        writeUserCSVHeaders(sb);
        for (AdminEntry user : users) {
        	List<AdminGroup> adminGroups = adminGroupDao.getAdminGroupByAdminID(user.getId());
        	for (AdminGroup adminGroup : adminGroups) {
        		writeUserCSVLine(user, adminGroup.getShortname(), sb);
        	}
        }
        return sb.toString();
    }

    private void writeUserCSVLine(AdminEntry user, String adminGroup, StringBuilder sb){
        writeCSVValue(user.getUsername(), DEFAULT_DELIMITER, DEFAULT_SEPARATOR, sb);
        writeCSVValue(user.getFirstname(), DEFAULT_DELIMITER, DEFAULT_SEPARATOR, sb);
        writeCSVValue(user.getFullname(), DEFAULT_DELIMITER, DEFAULT_SEPARATOR, sb);
        writeCSVValue(user.getEmail(), DEFAULT_DELIMITER, DEFAULT_SEPARATOR, sb);
        writeCSVValue(adminGroup, DEFAULT_DELIMITER, "", sb);
        writeCSVLineBreak(sb);
    }

    private void writeUserCSVHeaders(StringBuilder sb){
        writeCSVValue("Username", DEFAULT_DELIMITER, DEFAULT_SEPARATOR, sb);
        writeCSVValue("Firstname", DEFAULT_DELIMITER, DEFAULT_SEPARATOR, sb);
        writeCSVValue("Lastname", DEFAULT_DELIMITER, DEFAULT_SEPARATOR, sb);
        writeCSVValue("Email", DEFAULT_DELIMITER, DEFAULT_SEPARATOR, sb);
        writeCSVValue("Usergroup", DEFAULT_DELIMITER, "", sb);
        writeCSVLineBreak(sb);
    }

    private void writeCSVValue(Object value, String delimiter, String separator, StringBuilder sb){
        sb.append(delimiter);
        if(value != null){
            sb.append(value.toString());
        }
        sb.append(delimiter);
        sb.append(separator);
    }

    private void writeCSVLineBreak(StringBuilder sb){
        sb.append("\n");
    }
}
