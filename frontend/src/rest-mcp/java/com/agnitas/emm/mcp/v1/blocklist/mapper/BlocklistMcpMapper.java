/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.mcp.v1.blocklist.mapper;

import com.agnitas.beans.PaginatedList;
import com.agnitas.emm.core.blacklist.service.impl.AddBlacklistEntryCommand;
import com.agnitas.emm.core.globalblacklist.beans.BlacklistDto;
import com.agnitas.emm.core.globalblacklist.forms.BlacklistOverviewFilter;
import com.agnitas.emm.mcp.v1.blocklist.dto.BlocklistEntryResponse;
import com.agnitas.emm.mcp.v1.blocklist.dto.CreateBlocklistEntryRequest;
import com.agnitas.emm.mcp.v1.common.dto.McpPageable;
import com.agnitas.emm.mcp.v1.common.dto.PageResponse;
import com.agnitas.emm.mcp.v1.common.mapper.McpMapper;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface BlocklistMcpMapper extends McpMapper {

    BlocklistEntryResponse toResponse(BlacklistDto blacklistDto);

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    @Mapping(target = "sort", constant = "email")
    @Mapping(source = "size", target = "numberOfRows")
    BlacklistOverviewFilter toFilter(McpPageable pageable);

    AddBlacklistEntryCommand toCommand(CreateBlocklistEntryRequest request, int adminId, int companyId);

    default PageResponse<BlocklistEntryResponse> toPageResponse(PaginatedList<BlacklistDto> source) {
        return toPageResponse(source, this::toResponse);
    }

}
