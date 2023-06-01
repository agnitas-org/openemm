/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.admin.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.agnitas.beans.AdminGroup;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.beans.Admin;
import com.agnitas.dao.AdminGroupDao;
import com.agnitas.emm.core.admin.service.AdminGroupService;

public class AdminGroupServiceImpl implements AdminGroupService {

    private AdminGroupDao adminGroupDao;

    @Override
    public List<AdminGroup> getAdminGroupsByCompanyIdAndDefault(int companyId, Admin admin, Admin adminIdToEdit) {
        List<Integer> additionalAdminGroupIds = new ArrayList<>();
        if (admin != null) {
        	additionalAdminGroupIds.addAll(admin.getGroupIds());
        }
        if (adminIdToEdit != null) {
        	additionalAdminGroupIds.addAll(adminIdToEdit.getGroupIds());
        }
		return adminGroupDao.getAdminGroupsByCompanyIdAndDefault(companyId, additionalAdminGroupIds);
    }
    
    @Override
    public AdminGroup getAdminGroup(int adminGroupID, int companyToLimitPremiumPermissionsFor) {
    	return adminGroupDao.getAdminGroup(adminGroupID, companyToLimitPremiumPermissionsFor);
    }
    
    @Override
    public AdminGroup getAdminGroupByName(String adminGroupName, int companyToLimitPremiumPermissionsFor) {
    	return adminGroupDao.getAdminGroupByName(adminGroupName, companyToLimitPremiumPermissionsFor);
    }

    @Required
    public void setAdminGroupDao(AdminGroupDao adminGroupDao) {
        this.adminGroupDao = adminGroupDao;
    }

	@Override
	public boolean deleteAdminGroup(int companyID, int adminGroupIdToDelete) {
		AdminGroup adminGroupToDelete = adminGroupDao.getAdminGroup(adminGroupIdToDelete, companyID);
		if (adminGroupToDelete == null) {
			return false;
		} else {
			return adminGroupDao.delete(adminGroupToDelete.getCompanyID(), adminGroupToDelete.getGroupID()) == 1;
		}
	}

	@Override
	public int saveAdminGroup(AdminGroup adminGroup) throws Exception {
		return adminGroupDao.saveAdminGroup(adminGroup);
	}

	@Override
	public boolean adminGroupExists(int companyID, String adminGroupName) {
		return adminGroupDao.adminGroupExists(companyID, adminGroupName);
	}
}
