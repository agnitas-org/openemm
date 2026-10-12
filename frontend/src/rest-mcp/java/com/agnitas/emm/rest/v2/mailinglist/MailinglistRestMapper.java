/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2.mailinglist;

import com.agnitas.beans.Mailinglist;
import com.agnitas.emm.core.commons.pagination.PaginatedFilter;
import com.agnitas.emm.core.mailinglist.MailinglistSortCriteria;
import com.agnitas.emm.core.mailinglist.dto.CreateMailinglistDTO;
import com.agnitas.emm.core.mailinglist.dto.MailinglistDto;
import com.agnitas.emm.core.mailinglist.dto.PatchMailinglistCommand;
import com.agnitas.emm.rest.v2.infrastructure.mapper.RestMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.springframework.data.domain.Pageable;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
interface MailinglistRestMapper extends RestMapper {

    @Mapping(source = "shortname", target = "name")
    MailinglistResponse toResponse(Mailinglist mailinglist);

    PatchMailinglistCommand toPatch(PatchMailinglistRequest source, int mailinglistId, int companyId);

    CreateMailinglistDTO toDto(CreateMailinglistRequest mailinglistRequest);

    @Mapping(source = "shortname", target = "name")
    MailinglistResponse toResponse(MailinglistDto mailinglistDto);

    @Mapping(source = "pageNumber", target = "page")
    @Mapping(source = "pageSize", target = "size")
    @Mapping(target = "ascending", expression = "java(toDirection(pageable.getSort()))")
    @Mapping(target = "sort", expression = "java(toSortCriteria(toSortProperty(pageable.getSort())))")
    PaginatedFilter<MailinglistSortCriteria> toPaginationFilter(Pageable pageable);

    default MailinglistSortCriteria toSortCriteria(String restKey) {
        if (restKey == null) {
            return MailinglistSortCriteria.ID;
        }
        return MailinglistSortCriteria.valueOf(restKey.toUpperCase());
    }
}
