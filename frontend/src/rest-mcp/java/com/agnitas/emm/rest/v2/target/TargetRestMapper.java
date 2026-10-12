/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2.target;

import com.agnitas.beans.Target;
import com.agnitas.beans.TargetLight;
import com.agnitas.emm.core.target.dto.CreateTargetCommand;
import com.agnitas.emm.core.target.dto.PatchTargetCommand;
import com.agnitas.emm.rest.v2.infrastructure.mapper.RestMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
interface TargetRestMapper extends RestMapper {

    @Mapping(target = "sql", source = "EQL")
    @Mapping(target = "name", source = "targetName")
    @Mapping(target = "eql", source = "EQL")
    @Mapping(target = "description", source = "targetDescription")
    TargetResponse toResponse(Target target);

    @Mapping(source = "targetName", target = "name")
    @Mapping(source = "targetDescription", target = "description")
    TargetLightResponse toLightResponse(TargetLight target);

    CreateTargetCommand toCommand(CreateTargetRequest request, int companyId);

    PatchTargetCommand toCommand(PatchTargetRequest request, int id, int companyId);
}
