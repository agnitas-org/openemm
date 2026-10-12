/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2.blocklist;

import static java.util.Collections.emptySet;

import java.util.Set;

import com.agnitas.beans.Admin;
import com.agnitas.beans.PaginatedList;
import com.agnitas.emm.core.blacklist.exception.BlocklistEntryNotFoundException;
import com.agnitas.emm.core.blacklist.service.BlacklistService;
import com.agnitas.emm.core.blacklist.service.impl.AddBlacklistEntryCommand;
import com.agnitas.emm.core.globalblacklist.beans.BlacklistDto;
import com.agnitas.emm.core.globalblacklist.forms.BlacklistOverviewFilter;
import com.agnitas.emm.rest.v2.infrastructure.search.PageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
class BlocklistRestService {

    private final BlacklistService blocklistService;
    private final BlocklistRestMapper mapper;

    BlocklistRestService(BlacklistService blocklistService, BlocklistRestMapper mapper) {
        this.blocklistService = blocklistService;
        this.mapper = mapper;
    }

    public PageResponse<BlocklistEntryResponse> find(Pageable pageable, int companyId) {
        BlacklistOverviewFilter filter = mapper.toFilter(pageable);
        PaginatedList<BlacklistDto> list = blocklistService.getAll(filter, companyId);
        return mapper.toPageResponse(list, mapper::toResponse);
    }

    public BlocklistEntryResponse find(String email, int companyId) {
        BlacklistDto dto = blocklistService.getByEmail(email, companyId);
        return mapper.toResponse(dto);
    }

    public void delete(String email, int companyId) {
        if (!blocklistService.delete(Set.of(email), emptySet(), companyId)) {
            throw new BlocklistEntryNotFoundException(email, companyId);
        }
    }

    public BlocklistEntryResponse addEntry(CreateBlocklistEntryRequest request, Admin admin) {
        AddBlacklistEntryCommand command = mapper.toCommand(request, admin.getAdminID(), admin.getCompanyID());
        BlacklistDto entry = blocklistService.add(command);
        return mapper.toResponse(entry);
    }

    public BlocklistEntryResponse updateEntry(String email, PatchBlocklistEntryRequest request, Admin admin) {
        blocklistService.update(email, admin.getCompanyID(), request.reason());
        return find(email, admin.getCompanyID());
    }
}
