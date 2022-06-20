/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.salutation.service.impl;

import org.agnitas.beans.SalutationEntry;
import org.agnitas.beans.Title;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.dao.ComTitleDao;
import com.agnitas.emm.core.salutation.service.SalutationService;

public class SalutationServiceImpl implements SalutationService {

    private ComTitleDao titleDao;

    @Required
    public void setTitleDao(ComTitleDao titleDao) {
        this.titleDao = titleDao;
    }

    @Override
    public PaginatedListImpl<SalutationEntry> paginatedList(int companyID, String sort, String order, int page, int rowsCount) {
        return titleDao.getSalutationList(companyID, sort, order, page, rowsCount);
    }

    @Override
    public Title get(int salutationId, int companyID) {
        return titleDao.getTitle(salutationId, companyID);
    }

    @Override
    public void save(Title title) throws Exception {
        titleDao.save(title);
    }

    @Override
    public boolean delete(int salutationId, int companyId) {
        return titleDao.delete(salutationId, companyId);
    }
}
