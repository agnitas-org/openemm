/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.restful.v2.mailinglists.mapper;

import com.agnitas.beans.Mailinglist;
import com.agnitas.beans.PaginatedList;
import com.agnitas.emm.core.mailinglist.dto.MailinglistDto;
import com.agnitas.emm.restful.v2.mailinglists.dto.CreateMailinglistRequest;
import com.agnitas.emm.restful.v2.mailinglists.dto.MailinglistResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MailinglistRestMapper {

    @Mapping(source = "shortname", target = "name")
    MailinglistResponse toResponse(Mailinglist mailinglist);

    MailinglistDto toDto(Mailinglist mailinglist);

    PaginatedList<MailinglistResponse> toResponseList(PaginatedList<Mailinglist> mailinglist);

    @Mapping(source = "name", target = "shortname")
    MailinglistDto createRequestToDto(CreateMailinglistRequest mailinglistRequest);

    @Mapping(source = "shortname", target = "name")
    MailinglistResponse dtoToRestResponse(MailinglistDto mailinglistDto);
}
