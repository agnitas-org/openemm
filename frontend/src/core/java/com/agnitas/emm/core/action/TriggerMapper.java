/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.action;

import com.agnitas.emm.core.action.bean.EmmActionImpl;
import com.agnitas.emm.core.action.bean.EmmActionType;
import com.agnitas.emm.core.action.dto.CreateTriggerCommand;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface TriggerMapper {

    @Mapping(target = "companyID", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "changeDate", ignore = true)
    @Mapping(target = "actionOperations", source = "operations")
    @Mapping(source = "isAdvertising", target = "advertising")
    @Mapping(source = "name", target = "shortname")
    EmmActionImpl toEmmAction(CreateTriggerCommand command);

    default int toIntType(EmmActionType type) {
        return type.getActionTypeCode();
    }
}
