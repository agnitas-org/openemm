/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2.blocklist;

import com.agnitas.emm.core.blacklist.service.impl.AddBlacklistEntryCommand;
import com.agnitas.emm.core.globalblacklist.beans.BlacklistDto;
import com.agnitas.emm.core.globalblacklist.forms.BlacklistOverviewFilter;
import com.agnitas.emm.rest.v2.infrastructure.mapper.RestMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.springframework.data.domain.Pageable;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
interface BlocklistRestMapper extends RestMapper {

    BlocklistEntryResponse toResponse(BlacklistDto blacklistDto);

    @Mapping(target = "reason", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "dir", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "sort", constant = "email")
    @Mapping(source = "pageNumber", target = "page")
    @Mapping(source = "pageSize", target = "numberOfRows")
    BlacklistOverviewFilter toFilter(Pageable pageable);

    AddBlacklistEntryCommand toCommand(CreateBlocklistEntryRequest request, int adminId, int companyId);
}
