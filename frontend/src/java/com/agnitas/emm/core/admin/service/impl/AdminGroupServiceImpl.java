/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.admin.service.impl;

import java.util.List;

import org.agnitas.beans.AdminGroup;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.dao.ComAdminGroupDao;
import com.agnitas.emm.core.admin.service.AdminGroupService;

public class AdminGroupServiceImpl implements AdminGroupService {

    private ComAdminGroupDao adminGroupDao;

    @Override
    public List<AdminGroup> getAdminGroupsByCompanyIdAndDefault(@VelocityCheck int companyId) {
        return adminGroupDao.getAdminGroupsByCompanyIdAndDefault(companyId);
    }

    @Required
    public void setAdminGroupDao(ComAdminGroupDao adminGroupDao) {
        this.adminGroupDao = adminGroupDao;
    }
}
