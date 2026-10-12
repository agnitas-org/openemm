/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.mcp.v1.binding.service;

import java.util.List;

import com.agnitas.beans.Admin;
import com.agnitas.beans.BindingEntry;
import com.agnitas.beans.PaginatedList;
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
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.emm.mcp.v1.binding.dto.BindingDeleteResult;
import com.agnitas.emm.mcp.v1.binding.dto.BindingResponse;
import com.agnitas.emm.mcp.v1.binding.dto.BulkDeleteBindingsRequest;
import com.agnitas.emm.mcp.v1.binding.dto.BulkDeleteBindingsResponse;
import com.agnitas.emm.mcp.v1.binding.dto.BulkUpsertBindingsRequest;
import com.agnitas.emm.mcp.v1.binding.dto.BulkUpsertBindingsResponse;
import com.agnitas.emm.mcp.v1.binding.dto.CreateBindingRequest;
import com.agnitas.emm.mcp.v1.binding.dto.ListBindingsFilter;
import com.agnitas.emm.mcp.v1.binding.dto.PatchBindingRequest;
import com.agnitas.emm.mcp.v1.binding.mapper.BindingMcpMapper;
import com.agnitas.emm.mcp.v1.common.dto.McpPageable;
import com.agnitas.emm.mcp.v1.common.dto.PageResponse;
import org.springframework.stereotype.Service;

@Service
public class BindingMcpServiceImpl implements BindingMcpService {

    private final BindingService bindingService;
    private final BindingMcpMapper mapper;

    public BindingMcpServiceImpl(BindingService bindingService, BindingMcpMapper mapper) {
        this.bindingService = bindingService;
        this.mapper = mapper;
    }

    @Override
    public PageResponse<BindingResponse> list(ListBindingsFilter filter, McpPageable pageable, int companyId) {
        ListBindingsQuery query = mapper.toQuery(filter, pageable);
        PaginatedList<BindingEntry> bindings = bindingService.find(query, companyId);
        return mapper.toPageResponse(bindings);
    }

    @Override
    public BindingResponse getById(int recipientId, int mailinglistId, MediaTypes mediatype, int companyId) {
        GetBindingQuery query = new GetBindingQuery(recipientId, mailinglistId, mediatype);
        BindingEntry binding = bindingService.get(query, companyId);
        return mapper.toResponse(binding);
    }

    @Override
    public BindingResponse create(CreateBindingRequest request, int companyId) {
        CreateBindingCommand command = mapper.toCommand(request);
        BindingCreateResult result = bindingService.create(command, companyId);
        return mapper.toResponse(result.binding(), result.actionExecutionInfo());
    }

    @Override
    public BindingResponse patch(int recipientId, int mailinglistId, MediaTypes mediatype, PatchBindingRequest request, int companyId) {
        GetBindingQuery query = new GetBindingQuery(recipientId, mailinglistId, mediatype);
        PatchBindingCommand command = mapper.toCommand(query, request);
        bindingService.updateBinding(command, companyId);
        return getById(recipientId, mailinglistId, mediatype, companyId);
    }

    @Override
    public void delete(int recipientId, int mailinglistId, MediaTypes mediatype, int companyId) {
        DeleteBindingCommand command = new DeleteBindingCommand(recipientId, mailinglistId, mediatype);
        bindingService.deleteBinding(command, companyId);
    }

    @Override
    public BulkDeleteBindingsResponse deleteBulk(BulkDeleteBindingsRequest request, int companyId) {
        List<DeleteResult> results = bindingService.bulkDelete(mapper.toCommand(request, companyId));

        List<BindingDeleteResult> mappedResults = results.stream()
                .map(result -> new BindingDeleteResult(result.id(), result.status().name()))
                .toList();

        long succeeded = results.stream()
                .filter(DeleteResult::isDeleted)
                .count();

        return new BulkDeleteBindingsResponse(results.size(), succeeded, results.size() - succeeded, mappedResults);
    }

    @Override
    public BulkUpsertBindingsResponse upsertBulk(BulkUpsertBindingsRequest request, Admin admin) {
        BulkUpsertBindingCommand command = mapper.toCommand(request, "Set by %s via MCP".formatted(admin.getUsername()));
        List<UpsertBindingResult> results = bindingService.upsert(command, admin.getCompanyID());
        return BulkUpsertBindingsResponse.from(results);
    }

}
