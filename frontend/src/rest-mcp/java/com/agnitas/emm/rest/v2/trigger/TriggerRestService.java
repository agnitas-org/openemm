/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2.trigger;

import java.util.Map;
import java.util.Set;

import com.agnitas.beans.Admin;
import com.agnitas.beans.PaginatedList;
import com.agnitas.emm.core.action.ActionNotFoundException;
import com.agnitas.emm.core.action.bean.EmmAction;
import com.agnitas.emm.core.action.dto.CreateTriggerCommand;
import com.agnitas.emm.core.action.dto.PatchTriggerCommand;
import com.agnitas.emm.core.action.service.EmmActionOperationErrors;
import com.agnitas.emm.core.action.service.EmmActionService;
import com.agnitas.emm.core.velocity.Constants;
import com.agnitas.emm.rest.v2.infrastructure.search.PageResponse;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
class TriggerRestService {

    private final EmmActionService triggerService;
    private final TriggerRestMapper mapper;

    TriggerRestService(EmmActionService triggerService, TriggerRestMapper mapper) {
        this.triggerService = triggerService;
        this.mapper = mapper;
    }

    public PageResponse<TriggerLightResponse> find(Pageable pageable, int companyId) {
        PaginatedList<EmmAction> list = triggerService.paginatedList(
                companyId,
                pageable.getPageNumber(),
                pageable.getPageSize()
        );
        return mapper.toPageResponse(list, mapper::toLightResponse);
    }

    public TriggerResponse find(int triggerId, Admin admin) {
        EmmAction trigger = triggerService.getTrigger(triggerId, admin);
        return mapper.toResponse(trigger);
    }

    public TriggerResponse create(CreateTriggerRequest request, Admin admin) {
        CreateTriggerCommand command = mapper.toCommand(request);
        EmmAction trigger = triggerService.create(command, admin);
        return find(trigger.getId(), admin);
    }

    public TriggerResponse patch(int id, PatchTriggerRequest request, Admin admin) {
        PatchTriggerCommand command = mapper.toCommand(request, id);
        triggerService.patch(command, admin);
        return find(id, admin);
    }

    public void execute(int id, int companyId, Map<String, Object> body) {
        if (!triggerService.actionExists(id, companyId)) {
            throw new ActionNotFoundException(id);
        }
        CaseInsensitiveMap<String, Object> params = new CaseInsensitiveMap<>(body);
        params.put("requestparameters", new CaseInsensitiveMap<>(body));
        EmmActionOperationErrors errors = new EmmActionOperationErrors();
        params.put(Constants.ACTION_OPERATION_ERRORS_CONTEXT_NAME, errors);

        if (!triggerService.executeActions(id, companyId, params, errors)) {
            throw TriggerExecutionErrorMapper.toException(errors.getErrorCodes());
        }
    }

    public void delete(int id, int companyId) {
        triggerService.bulkDelete(Set.of(id), companyId);
    }
}
