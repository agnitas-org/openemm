/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.mcp.v1.mailinglist.service;

import com.agnitas.beans.Admin;
import com.agnitas.beans.Mailinglist;
import com.agnitas.beans.PaginatedList;
import com.agnitas.emm.core.commons.pagination.PaginatedFilter;
import com.agnitas.emm.core.mailinglist.MailinglistSortCriteria;
import com.agnitas.emm.core.mailinglist.dto.CreateMailinglistDTO;
import com.agnitas.emm.core.mailinglist.dto.MailinglistDto;
import com.agnitas.emm.core.mailinglist.service.MailinglistService;
import com.agnitas.emm.mcp.v1.common.dto.McpPageable;
import com.agnitas.emm.mcp.v1.common.dto.PageResponse;
import com.agnitas.emm.core.mailinglist.dto.PatchMailinglistCommand;
import com.agnitas.emm.mcp.v1.mailinglist.dto.CreateMailinglistRequest;
import com.agnitas.emm.mcp.v1.mailinglist.dto.MailinglistResponse;
import com.agnitas.emm.mcp.v1.mailinglist.dto.PatchMailinglistRequest;
import com.agnitas.emm.mcp.v1.mailinglist.exception.MailinglistDeletionException;
import com.agnitas.emm.mcp.v1.mailinglist.mapper.MailinglistMcpMapper;
import com.agnitas.service.SimpleServiceResult;
import org.springframework.stereotype.Service;

@Service
public class MailinglistMcpServiceImpl implements MailinglistMcpService {

    private final MailinglistService mailinglistService;
    private final MailinglistMcpMapper mailinglistMcpMapper;

    public MailinglistMcpServiceImpl(
            MailinglistService mailinglistService,
            MailinglistMcpMapper mailinglistMcpMapper
    ) {
        this.mailinglistService = mailinglistService;
        this.mailinglistMcpMapper = mailinglistMcpMapper;
    }

    @Override
    public PageResponse<MailinglistResponse> list(McpPageable pageable, int companyId) {
        PaginatedFilter<MailinglistSortCriteria> pagination = mailinglistMcpMapper.toPaginationFilter(pageable);
        PaginatedList<Mailinglist> mailinglists = mailinglistService.findAll(pagination, companyId);
        return mailinglistMcpMapper.toPageResponse(mailinglists);
    }

    @Override
    public MailinglistResponse getById(int id, int companyId) {
        Mailinglist mailinglist = mailinglistService.getMailinglist(id, companyId);
        return mailinglistMcpMapper.toResponse(mailinglist);
    }

    @Override
    public MailinglistResponse create(CreateMailinglistRequest request, int companyId) {
        CreateMailinglistDTO createDto = mailinglistMcpMapper.toDto(request);
        MailinglistDto mailinglist = mailinglistService.create(createDto, companyId);

        return mailinglistMcpMapper.toResponse(mailinglist);
    }

    @Override
    public MailinglistResponse patch(int id, PatchMailinglistRequest request, int companyId) {
        PatchMailinglistCommand command = mailinglistMcpMapper.toCommand(request, id, companyId);
        Mailinglist mailinglist = mailinglistService.update(command);
        return mailinglistMcpMapper.toResponse(mailinglist);
    }

    @Override
    public void delete(int id, Admin admin) {
        SimpleServiceResult canBeDeleted = mailinglistService.checkCanBeDeleted(id, admin);
        if (!canBeDeleted.isSuccess()) {
            throw new MailinglistDeletionException(canBeDeleted.getErrorMessages());
        }

        mailinglistService.deleteMailinglist(id, admin.getCompanyID());
    }

}
