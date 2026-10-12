/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.userform.service;

import com.agnitas.emm.core.userform.dto.ResultSettings;
import com.agnitas.emm.core.userform.dto.UserFormDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface UserFormMapper {

    @Mapping(target = "active", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "changeDate", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "successSettings", expression = "java(withActionIds(command.successSettings(), "
            + "command.startTriggerId(), command.endTriggerId()))")
    UserFormDto toDto(CreateFormCommand command);

    default ResultSettings withActionIds(ResultSettings settings, int startTriggerId, int endTriggerId) {
        settings.setStartActionId(startTriggerId);
        settings.setFinalActionId(endTriggerId);
        return settings;
    }
}
