/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.usergroup.converter;

import java.util.HashSet;
import java.util.Set;

import com.agnitas.beans.AdminGroup;
import com.agnitas.beans.impl.AdminGroupImpl;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import com.agnitas.dao.AdminGroupDao;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.usergroup.dto.UserGroupDto;

@Component
public class UserGroupDtoToAdminGroupConverter implements Converter<UserGroupDto, AdminGroup> {
	private AdminGroupDao adminGroupDao;
	
	public UserGroupDtoToAdminGroupConverter(AdminGroupDao adminGroupDao) {
		this.adminGroupDao = adminGroupDao;
	}
	
    @Override
    public AdminGroup convert(UserGroupDto source) {
        AdminGroup adminGroup = new AdminGroupImpl();
        adminGroup.setGroupID(source.getUserGroupId());
        adminGroup.setCompanyID(source.getCompanyId());
        adminGroup.setShortname(source.getShortname());
        adminGroup.setDescription(source.getDescription());
        adminGroup.setGroupPermissions(Permission.fromTokens(source.getGrantedPermissions()));
        
        if (source.getParentGroupIDs() != null) {
            Set<AdminGroup> adminGroups = new HashSet<>();
            for (String adminGroupId : source.getParentGroupIDs()) {
            	adminGroups.add(adminGroupDao.getAdminGroup(Integer.parseInt(adminGroupId), source.getCompanyId()));
            }
            adminGroup.setParentGroups(adminGroups);
        }
        
        return adminGroup;
    }
}
