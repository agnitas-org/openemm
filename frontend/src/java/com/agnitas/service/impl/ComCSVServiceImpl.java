/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.service.impl;


import java.io.ByteArrayOutputStream;
import java.util.List;

import org.agnitas.beans.AdminEntry;
import org.agnitas.beans.AdminGroup;
import org.agnitas.util.CsvWriter;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.dao.ComAdminGroupDao;
import com.agnitas.service.ComCSVService;

public class ComCSVServiceImpl implements ComCSVService {
    /** DAO for accessing admin group data. */
	protected ComAdminGroupDao adminGroupDao;
	
    @Required
	public void setAdminGroupDao(ComAdminGroupDao adminGroupDao) {
		this.adminGroupDao = adminGroupDao;
	}

    @Override
	public byte[] getUserCSV(List<AdminEntry> users) throws Exception{
    	ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (CsvWriter csvWriter = new CsvWriter(outputStream)) {
        	csvWriter.writeValues("Username","Firstname", "Lastname", "Email", "Usergroup");
        	
	        for (AdminEntry user : users) {
	        	List<AdminGroup> adminGroups = adminGroupDao.getAdminGroupsByAdminID(user.getCompanyID(), user.getId());
	        	StringBuilder adminGroupsList = new StringBuilder();
	        	for (AdminGroup adminGroup : adminGroups) {
	        		if (adminGroupsList.length() > 0) {
	        			adminGroupsList.append(", ");
	        		}
	        		adminGroupsList.append(adminGroup.getShortname());
	        	}
	        	csvWriter.writeValues(user.getUsername(), user.getFirstname(), user.getFullname(), user.getEmail(), adminGroupsList.toString());
	        }
        }
        return outputStream.toByteArray();
    }
}
