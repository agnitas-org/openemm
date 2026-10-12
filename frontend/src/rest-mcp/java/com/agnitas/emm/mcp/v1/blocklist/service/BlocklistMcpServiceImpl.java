/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.mcp.v1.blocklist.service;

import static java.util.Collections.emptySet;

import java.util.Set;

import com.agnitas.beans.Admin;
import com.agnitas.beans.PaginatedList;
import com.agnitas.emm.core.blacklist.exception.BlocklistEntryNotFoundException;
import com.agnitas.emm.core.blacklist.service.BlacklistService;
import com.agnitas.emm.core.blacklist.service.impl.AddBlacklistEntryCommand;
import com.agnitas.emm.core.globalblacklist.beans.BlacklistDto;
import com.agnitas.emm.core.globalblacklist.forms.BlacklistOverviewFilter;
import com.agnitas.emm.mcp.v1.blocklist.dto.BlocklistEntryResponse;
import com.agnitas.emm.mcp.v1.blocklist.dto.CreateBlocklistEntryRequest;
import com.agnitas.emm.mcp.v1.blocklist.dto.PatchBlocklistEntryRequest;
import com.agnitas.emm.mcp.v1.blocklist.mapper.BlocklistMcpMapper;
import com.agnitas.emm.mcp.v1.common.dto.McpPageable;
import com.agnitas.emm.mcp.v1.common.dto.PageResponse;
import org.springframework.stereotype.Service;

@Service
public class BlocklistMcpServiceImpl implements BlocklistMcpService {

    private final BlacklistService blacklistService;
    private final BlocklistMcpMapper mapper;

    public BlocklistMcpServiceImpl(BlacklistService blacklistService, BlocklistMcpMapper mapper) {
        this.blacklistService = blacklistService;
        this.mapper = mapper;
    }

    @Override
    public PageResponse<BlocklistEntryResponse> list(McpPageable pageable, int companyId) {
        BlacklistOverviewFilter filter = mapper.toFilter(pageable);
        PaginatedList<BlacklistDto> list = blacklistService.getAll(filter, companyId);
        return mapper.toPageResponse(list);
    }

    @Override
    public BlocklistEntryResponse getByEmail(String email, int companyId) {
        BlacklistDto dto = blacklistService.getByEmail(email, companyId);
        return mapper.toResponse(dto);
    }

    @Override
    public BlocklistEntryResponse create(CreateBlocklistEntryRequest request, Admin admin) {
        AddBlacklistEntryCommand command = mapper.toCommand(request, admin.getAdminID(), admin.getCompanyID());
        BlacklistDto entry = blacklistService.add(command);
        return mapper.toResponse(entry);
    }

    @Override
    public BlocklistEntryResponse patch(String email, PatchBlocklistEntryRequest request, int companyId) {
        blacklistService.update(email, companyId, request.reason());
        return getByEmail(email, companyId);
    }

    @Override
    public void delete(String email, int companyId) {
        if (!blacklistService.delete(Set.of(email), emptySet(), companyId)) {
            throw new BlocklistEntryNotFoundException(email, companyId);
        }
    }

}
