/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.mcp.v1.mailinglist.mapper;

import com.agnitas.beans.Mailinglist;
import com.agnitas.beans.PaginatedList;
import com.agnitas.emm.core.commons.pagination.PaginatedFilter;
import com.agnitas.emm.core.mailinglist.MailinglistSortCriteria;
import com.agnitas.emm.core.mailinglist.dto.CreateMailinglistDTO;
import com.agnitas.emm.core.mailinglist.dto.MailinglistDto;
import com.agnitas.emm.core.mailinglist.dto.PatchMailinglistCommand;
import com.agnitas.emm.mcp.v1.common.dto.McpPageable;
import com.agnitas.emm.mcp.v1.common.dto.PageResponse;
import com.agnitas.emm.mcp.v1.common.mapper.McpMapper;
import com.agnitas.emm.mcp.v1.mailinglist.dto.CreateMailinglistRequest;
import com.agnitas.emm.mcp.v1.mailinglist.dto.MailinglistResponse;
import com.agnitas.emm.mcp.v1.mailinglist.dto.PatchMailinglistRequest;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface MailinglistMcpMapper extends McpMapper {

    @Mapping(source = "shortname", target = "name")
    MailinglistResponse toResponse(Mailinglist mailinglist);

    CreateMailinglistDTO toDto(CreateMailinglistRequest mailinglistRequest);

    @Mapping(source = "shortname", target = "name")
    MailinglistResponse toResponse(MailinglistDto mailinglistDto);

    @Mapping(target = "sort", expression = "java(toSortCriteria(pageable.sortBy()))")
    @Mapping(target = "ascending", expression = "java(pageable.ascending() == null ? true : pageable.ascending())")
    PaginatedFilter<MailinglistSortCriteria> toPaginationFilter(McpPageable pageable);

    PatchMailinglistCommand toCommand(PatchMailinglistRequest request, int mailinglistId, int companyId);

    default PageResponse<MailinglistResponse> toPageResponse(PaginatedList<Mailinglist> source) {
        return toPageResponse(source, this::toResponse);
    }

    default MailinglistSortCriteria toSortCriteria(String mcpKey) {
        if (StringUtils.isBlank(mcpKey)) {
            return MailinglistSortCriteria.ID;
        }

        return MailinglistSortCriteria.valueOf(mcpKey.toUpperCase());
    }

}
