/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2.trigger;

import java.sql.Timestamp;
import java.time.Instant;

import com.agnitas.emm.core.action.bean.EmmAction;
import com.agnitas.emm.core.action.bean.EmmActionType;
import com.agnitas.emm.core.action.dto.CreateTriggerCommand;
import com.agnitas.emm.core.action.dto.PatchTriggerCommand;
import com.agnitas.emm.rest.v2.infrastructure.mapper.RestMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
interface TriggerRestMapper extends RestMapper {

    @Mapping(target = "operations", source = "actionOperations")
    @Mapping(target = "isAdvertising", source = "advertising")
    @Mapping(target = "name", source = "shortname")
    TriggerResponse toResponse(EmmAction trigger);

    @Mapping(target = "isAdvertising", source = "advertising")
    @Mapping(target = "name", source = "shortname")
    TriggerLightResponse toLightResponse(EmmAction trigger);

    CreateTriggerCommand toCommand(CreateTriggerRequest request);

    PatchTriggerCommand toCommand(PatchTriggerRequest request, int id);

    default EmmActionType toEnumType(Integer type) {
        if (type == null) {
            return null;
        }
        return EmmActionType.getEmmActionTypeByID(type);
    }

    default Instant map(Timestamp value) {
        return value == null ? null : value.toInstant();
    }
}
