/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2.forms;

import java.util.List;

import com.agnitas.beans.Admin;
import com.agnitas.beans.PaginatedList;
import com.agnitas.emm.core.commons.pagination.PaginatedFilter;
import com.agnitas.emm.core.userform.dto.UserFormDto;
import com.agnitas.emm.core.userform.service.CreateFormCommand;
import com.agnitas.emm.core.userform.service.PatchFormCommand;
import com.agnitas.emm.core.userform.service.UserformService;
import com.agnitas.emm.rest.v2.infrastructure.search.PageResponse;
import com.agnitas.exception.FormNotFoundException;
import com.agnitas.userform.bean.UserForm;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
class FormRestService {

    private final FormRestMapper mapper;
    private final UserformService formService;

    public FormRestService(FormRestMapper mapper, UserformService formService) {
        this.mapper = mapper;
        this.formService = formService;
    }

    PageResponse<FormLightResponse> list(Pageable pageable, int companyId) {
        PaginatedFilter<String> filter = mapper.toPaginationFilter(pageable);
        PaginatedList<UserForm> list = formService.getPaginatedList(filter, companyId);
        return mapper.toPageResponse(list, mapper::toLightResponse);
    }

    FormResponse find(int formId, int companyId) {
        UserForm dto = formService.getForm(formId, companyId);
        return mapper.toResponse(dto);
    }

    FormResponse create(CreateFormRequest request, Admin admin) {
        CreateFormCommand command = mapper.toCommand(request);
        int createdId = formService.create(command, admin);
        return find(createdId, admin.getCompanyID());
    }

    FormResponse patch(int formId, PatchFormRequest request, Admin admin) {
        PatchFormCommand command = mapper.toCommand(request);
        formService.patchForm(formId, command, admin);
        return find(formId, admin.getCompanyID());
    }

    void delete(int formId, int companyId) {
        List<UserFormDto> deleted = formService.bulkDeleteUserForm(List.of(formId), companyId);
        if (deleted.isEmpty()) {
            throw new FormNotFoundException(formId);
        }
    }

    public FormResponse activate(int id, Admin admin) {
        formService.updateActiveness(id, admin.getCompanyID(), true);
        return find(id, admin.getCompanyID());
    }

    public FormResponse deactivate(int id, Admin admin) {
        formService.updateActiveness(id, admin.getCompanyID(), false);
        return find(id, admin.getCompanyID());
    }
}
