/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.service.impl;

import java.util.List;

import org.agnitas.beans.ExportPredef;
import org.agnitas.dao.ExportPredefDao;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.beans.ComAdmin;
import com.agnitas.service.ExportPredefService;

public class ExportPredefServiceImpl implements ExportPredefService {
    private ExportPredefDao exportPredefDao;

    @Override
    public ExportPredef get(int id, @VelocityCheck int companyId) {
        return exportPredefDao.get(id, companyId);
    }

    @Override
    public ExportPredef create(@VelocityCheck int companyId) {
        return exportPredefDao.create(companyId);
    }

    @Override
    public int save(ExportPredef src) {
        return exportPredefDao.save(src);
    }

    @Override
    public List<ExportPredef> getExportProfiles(ComAdmin admin) {
    	return exportPredefDao.getAllByCompany(admin.getCompanyID());
    }

    @Override
    public List<Integer> getExportProfileIds(ComAdmin admin) {
    	return exportPredefDao.getAllIdsByCompany(admin.getCompanyID());
    }

    public final ExportPredefDao getExportPredefDao() {
    	return this.exportPredefDao;
    }

    @Required
    public void setExportPredefDao(ExportPredefDao exportPredefDao) {
        this.exportPredefDao = exportPredefDao;
    }
}
