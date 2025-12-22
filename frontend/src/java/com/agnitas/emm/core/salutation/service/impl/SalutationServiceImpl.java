/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.salutation.service.impl;

import static java.util.Collections.emptyMap;

import java.util.List;
import java.util.Set;

import com.agnitas.beans.Admin;
import com.agnitas.beans.Title;
import com.agnitas.beans.PaginatedList;
import com.agnitas.dao.TitleDao;
import com.agnitas.emm.common.service.BulkActionValidationService;
import com.agnitas.emm.core.recipient.dto.RecipientDto;
import com.agnitas.emm.core.recipient.service.RecipientService;
import com.agnitas.emm.core.salutation.form.SalutationOverviewFilter;
import com.agnitas.emm.core.salutation.service.SalutationService;
import com.agnitas.emm.core.useractivitylog.bean.UserAction;
import com.agnitas.messages.Message;
import com.agnitas.service.ServiceResult;
import com.agnitas.util.Const;
import com.agnitas.util.CustomTitle;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

public class SalutationServiceImpl implements SalutationService {

    private final TitleDao titleDao;
    private final BulkActionValidationService<Integer, Title> bulkActionValidationService;
    private final RecipientService recipientService;

    @Autowired
    public SalutationServiceImpl(TitleDao titleDao, BulkActionValidationService<Integer, Title> bulkActionValidationService, RecipientService recipientService) {
        this.titleDao = titleDao;
        this.bulkActionValidationService = bulkActionValidationService;
        this.recipientService = recipientService;
    }

    @Override
    public PaginatedList<Title> overview(SalutationOverviewFilter filter) {
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
    public void save(Title title) {
        titleDao.save(title);
    }

    private boolean delete(int salutationId, int companyId) {
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
                .toList();

        return ServiceResult.success(
                new UserAction(
                        "delete salutations",
                        String.format("Salutation IDs %s", StringUtils.join(deletedIds, ", "))
                ),
                Message.of(Const.Mvc.SELECTION_DELETED_MSG)
        );
    }

    @Override
    public String resolve(int salutationId, int recipientId, int type, Admin admin) {
        RecipientDto recipient = recipientService.getRecipientDto(admin, recipientId);
        Title salutation = get(salutationId, admin.getCompanyID());
        CustomTitle title = new CustomTitle(salutation.getTitleGender().get(recipient.getGender()));
        return title.makeTitle(type,
            recipient.getGender(), recipient.getTitle(), recipient.getFirstname(), recipient.getLastname(),
            emptyMap(), new StringBuffer());
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
