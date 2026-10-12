/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.mcp.v1.trigger.service;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.agnitas.beans.Admin;
import com.agnitas.beans.PaginatedList;
import com.agnitas.emm.core.action.ActionNotFoundException;
import com.agnitas.emm.core.action.bean.EmmAction;
import com.agnitas.emm.core.action.dto.CreateTriggerCommand;
import com.agnitas.emm.core.action.dto.PatchTriggerCommand;
import com.agnitas.emm.core.action.service.EmmActionOperationErrors;
import com.agnitas.emm.core.action.service.EmmActionService;
import com.agnitas.emm.core.velocity.Constants;
import com.agnitas.emm.mcp.v1.common.dto.McpPageable;
import com.agnitas.emm.mcp.v1.common.dto.PageResponse;
import com.agnitas.emm.mcp.v1.trigger.dto.CreateTriggerRequest;
import com.agnitas.emm.mcp.v1.trigger.dto.TriggerLightResponse;
import com.agnitas.emm.mcp.v1.trigger.dto.TriggerResponse;
import com.agnitas.emm.mcp.v1.trigger.dto.PatchTriggerRequest;
import com.agnitas.emm.mcp.v1.trigger.exception.TriggerExecutionException;
import com.agnitas.emm.mcp.v1.trigger.mapper.TriggerMcpMapper;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.springframework.stereotype.Service;

@Service
public class TriggerMcpServiceImpl implements TriggerMcpService {

    private final EmmActionService triggerService;
    private final TriggerMcpMapper mapper;

    public TriggerMcpServiceImpl(EmmActionService triggerService, TriggerMcpMapper mapper) {
        this.triggerService = triggerService;
        this.mapper = mapper;
    }

    @Override
    public PageResponse<TriggerLightResponse> list(McpPageable pageable, int companyId) {
        PaginatedList<EmmAction> list = triggerService.paginatedList(companyId, pageable.page(), pageable.size());
        return mapper.toPageResponse(list);
    }

    @Override
    public TriggerResponse getById(int id, Admin admin) {
        return mapper.toResponse(triggerService.getTrigger(id, admin));
    }

    @Override
    public TriggerResponse create(CreateTriggerRequest request, Admin admin) {
        CreateTriggerCommand command = mapper.toCommand(request);
        EmmAction trigger = triggerService.create(command, admin);
        return getById(trigger.getId(), admin);
    }

    @Override
    public TriggerResponse patch(int id, PatchTriggerRequest request, Admin admin) {
        PatchTriggerCommand command = mapper.toCommand(id, request);
        triggerService.patch(command, admin);
        return getById(id, admin);
    }

    @Override
    public void execute(int id, int companyId, Map<String, Object> fields) {
        if (!triggerService.actionExists(id, companyId)) {
            throw new ActionNotFoundException(id);
        }

        Map<String, Object> safeFields = fields != null ? fields : Map.of();
        CaseInsensitiveMap<String, Object> params = new CaseInsensitiveMap<>(safeFields);
        params.put("requestparameters", new CaseInsensitiveMap<>(safeFields));

        EmmActionOperationErrors errors = new EmmActionOperationErrors();
        params.put(Constants.ACTION_OPERATION_ERRORS_CONTEXT_NAME, errors);

        if (!triggerService.executeActions(id, companyId, params, errors)) {
            String message = errors.isEmpty()
                    ? "Can't execute trigger"
                    : errors.getErrorCodes().stream()
                            .map(EmmActionOperationErrors.ErrorCode::getDescription)
                            .collect(Collectors.joining("; "));
            throw new TriggerExecutionException(message);
        }
    }

    @Override
    public void delete(int id, int companyId) {
        triggerService.bulkDelete(Set.of(id), companyId);
    }

}
