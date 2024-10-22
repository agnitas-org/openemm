/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.salutation.service.impl;

import com.agnitas.dao.ComTitleDao;
import com.agnitas.emm.common.service.BulkActionValidationService;
import com.agnitas.emm.core.salutation.form.SalutationOverviewFilter;
import com.agnitas.emm.core.salutation.service.SalutationService;
import com.agnitas.messages.Message;
import com.agnitas.service.ServiceResult;
import org.agnitas.beans.SalutationEntry;
import org.agnitas.beans.Title;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.emm.core.useractivitylog.UserAction;
import org.agnitas.util.Const;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SalutationServiceImpl implements SalutationService {

    private final ComTitleDao titleDao;
    private final BulkActionValidationService<Integer, Title> bulkActionValidationService;

    @Autowired
    public SalutationServiceImpl(ComTitleDao titleDao, BulkActionValidationService<Integer, Title> bulkActionValidationService) {
        this.titleDao = titleDao;
        this.bulkActionValidationService = bulkActionValidationService;
    }

    @Override
    public PaginatedListImpl<SalutationEntry> paginatedList(int companyID, String sort, String order, int page, int rowsCount) {
        return titleDao.getSalutationList(companyID, sort, order, page, rowsCount);
    }

    @Override
    public PaginatedListImpl<Title> overview(SalutationOverviewFilter filter) {
        return titleDao.overview(filter);
    }

    @Override
    public List<Title> getAll(int companyId, boolean includeGenders) {
        return titleDao.getTitles(companyId, includeGenders);
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

    @Override
    public ServiceResult<List<Title>> getAllowedForDeletion(Set<Integer> ids, int companyId) {
        return bulkActionValidationService.checkAllowedForDeletion(ids, id -> getSalutationForDeletion(id, companyId));
    }

    @Override
    public ServiceResult<UserAction> bulkDelete(Set<Integer> ids, int companyId) {
        List<Integer> deletedIds = ids.stream()
                .map(id -> getSalutationForDeletion(id, companyId))
                .filter(ServiceResult::isSuccess)
                .map(r -> r.getResult().getId())
                .filter(id -> delete(id, companyId))
                .collect(Collectors.toList());

        return ServiceResult.success(
                new UserAction(
                        "delete salutations",
                        String.format("Salutation IDs %s", StringUtils.join(deletedIds, ", "))
                ),
                Message.of(Const.Mvc.SELECTION_DELETED_MSG)
        );
    }

    private ServiceResult<Title> getSalutationForDeletion(int id, int companyId) {
        Title title = get(id, companyId);
        if (title == null) {
            return ServiceResult.errorKeys("error.general.missing");
        }

        if (title.getCompanyID() != companyId) {
            return ServiceResult.errorKeys("error.salutation.change.permission");
        }

        return ServiceResult.success(title);
    }
}
