/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.mcp.v1.form.service;

import java.util.List;

import com.agnitas.beans.Admin;
import com.agnitas.beans.PaginatedList;
import com.agnitas.emm.core.commons.pagination.PaginatedFilter;
import com.agnitas.emm.core.userform.dto.UserFormDto;
import com.agnitas.emm.core.userform.service.CreateFormCommand;
import com.agnitas.emm.core.userform.service.PatchFormCommand;
import com.agnitas.emm.core.userform.service.UserformService;
import com.agnitas.emm.mcp.v1.common.dto.McpPageable;
import com.agnitas.emm.mcp.v1.common.dto.PageResponse;
import com.agnitas.emm.mcp.v1.form.dto.CreateFormRequest;
import com.agnitas.emm.mcp.v1.form.dto.FormLightResponse;
import com.agnitas.emm.mcp.v1.form.dto.FormResponse;
import com.agnitas.emm.mcp.v1.form.dto.PatchFormRequest;
import com.agnitas.emm.mcp.v1.form.mapper.FormMcpMapper;
import com.agnitas.exception.FormNotFoundException;
import com.agnitas.userform.bean.UserForm;
import org.springframework.stereotype.Service;

@Service
public class FormMcpServiceImpl implements FormMcpService {

    private final UserformService userformService;
    private final FormMcpMapper mapper;

    public FormMcpServiceImpl(UserformService userformService, FormMcpMapper mapper) {
        this.userformService = userformService;
        this.mapper = mapper;
    }

    @Override
    public PageResponse<FormLightResponse> list(McpPageable pageable, int companyId) {
        PaginatedFilter<String> filter = mapper.toPaginationFilter(pageable);
        PaginatedList<UserForm> list = userformService.getPaginatedList(filter, companyId);
        return mapper.toPageResponse(list);
    }

    @Override
    public FormResponse getById(int formId, int companyId) {
        UserForm form = userformService.getForm(formId, companyId);
        return mapper.toResponse(form);
    }

    @Override
    public FormResponse create(CreateFormRequest request, Admin admin) {
        CreateFormCommand command = mapper.toCommand(request);
        int createdId = userformService.create(command, admin);
        return getById(createdId, admin.getCompanyID());
    }

    @Override
    public FormResponse patch(int formId, PatchFormRequest request, Admin admin) {
        PatchFormCommand command = mapper.toCommand(request);
        userformService.patchForm(formId, command, admin);
        request.active().ifPresent(active -> userformService.updateActiveness(formId, admin.getCompanyID(), active));
        return getById(formId, admin.getCompanyID());
    }

    @Override
    public void delete(int formId, int companyId) {
        List<UserFormDto> deleted = userformService.bulkDeleteUserForm(List.of(formId), companyId);
        if (deleted.isEmpty()) {
            throw new FormNotFoundException(formId);
        }
    }

}
