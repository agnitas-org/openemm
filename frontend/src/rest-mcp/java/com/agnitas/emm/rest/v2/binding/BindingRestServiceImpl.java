/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2.binding;

import java.util.List;

import com.agnitas.beans.Admin;
import com.agnitas.beans.BindingEntry;
import com.agnitas.beans.PaginatedList;
import com.agnitas.emm.core.binding.BindingIdentifier;
import com.agnitas.emm.core.binding.BulkDeleteBindingCommand;
import com.agnitas.emm.core.binding.BulkUpsertBindingCommand;
import com.agnitas.emm.core.binding.CreateBindingCommand;
import com.agnitas.emm.core.binding.DeleteBindingCommand;
import com.agnitas.emm.core.binding.GetBindingQuery;
import com.agnitas.emm.core.binding.ListBindingsQuery;
import com.agnitas.emm.core.binding.PatchBindingCommand;
import com.agnitas.emm.core.binding.UpsertBindingResult;
import com.agnitas.emm.core.binding.service.BindingCreateResult;
import com.agnitas.emm.core.binding.service.BindingService;
import com.agnitas.emm.core.commons.validation.DeleteResult;
import com.agnitas.emm.rest.v2.infrastructure.bulk.BulkDeleteResult;
import com.agnitas.emm.rest.v2.infrastructure.bulk.BulkOperationResponse;
import com.agnitas.emm.rest.v2.infrastructure.search.PageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
class BindingRestServiceImpl implements BindingRestService {

    private final BindingRestMapper bindingRestMapper;
    private final BindingService bindingService;

    BindingRestServiceImpl(
            BindingRestMapper bindingRestMapper,
            BindingService bindingService
    ) {
        this.bindingRestMapper = bindingRestMapper;
        this.bindingService = bindingService;
    }

    @Override
    public PageResponse<BindingResponse> find(ListBindingsRequest request, Pageable pageable, int companyId) {
        ListBindingsQuery query = bindingRestMapper.toQuery(request, pageable);
        PaginatedList<BindingEntry> bindings = bindingService.find(query, companyId);
        return bindingRestMapper.toPageResponse(bindings, bindingRestMapper::toResponse);
    }

    @Override
    public BindingResponse find(BindingIdentifier request, int companyId) {
        GetBindingQuery query = bindingRestMapper.toQuery(request);
        BindingEntry binding = bindingService.get(query, companyId);
        return bindingRestMapper.toResponse(binding);
    }

    @Override
    public BindingResponse create(CreateBindingRequest request, int companyId) {
        CreateBindingCommand command = bindingRestMapper.toCommand(request);
        BindingCreateResult result = bindingService.create(command, companyId);
        return bindingRestMapper.toResponse(result.binding(), result.actionExecutionInfo());
    }

    @Override
    public void delete(BindingIdentifier identifier, int companyId) {
        DeleteBindingCommand command = bindingRestMapper.toDeleteCommand(identifier);
        bindingService.deleteBinding(command, companyId);
    }

    @Override
    public BulkOperationResponse<BulkDeleteResult> delete(BulkDeleteBindingRequest request, int companyId) {
        BulkDeleteBindingCommand command = bindingRestMapper.toCommand(request, companyId);
        List<DeleteResult> results = bindingService.bulkDelete(command);
        return BulkOperationResponse.fromDeleteResult(results);
    }

    @Override
    public BindingResponse update(BindingIdentifier identifier, PatchBindingRequest request, int companyId) {
        PatchBindingCommand command = bindingRestMapper.toCommand(identifier, request);
        bindingService.updateBinding(command, companyId);
        return find(identifier, companyId);
    }

    @Override
    public BulkUpsertBindingResponse upsert(BulkUpsertBindingRequest request, Admin admin) {
        BulkUpsertBindingCommand command = bindingRestMapper.toCommand(
                request,
                "%s by Admin via Restful API".formatted(request.status())
        );
        List<UpsertBindingResult> result = bindingService.upsert(command, admin.getCompanyID());
        return BulkUpsertBindingResponse.from(result);
    }
}
