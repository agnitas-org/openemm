/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.target.mapper;

import com.agnitas.beans.Target;
import com.agnitas.beans.impl.TargetImpl;
import com.agnitas.emm.core.target.dto.CreateTargetCommand;
import com.agnitas.emm.core.target.dto.TargetExportDto;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface TargetMapper {

    @Mapping(source = "companyID", target = "companyId")
    @Mapping(source = "targetName", target = "name")
    @Mapping(source = "targetDescription", target = "description")
    @Mapping(source = "adminTestDelivery", target = "isAdminTestDelivery")
    @Mapping(source = "EQL", target = "eql")
    TargetExportDto toExportDto(Target target);


    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    @Mapping(source = "companyId", target = "companyID")
    @Mapping(source = "name", target = "targetName")
    @Mapping(source = "description", target = "targetDescription")
    @Mapping(source = "isAdminTestDelivery", target = "adminTestDelivery")
    @Mapping(source = "eql", target = "EQL")
    TargetImpl fromExportDto(TargetExportDto targetExportDto);

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    @Mapping(source = "companyId", target = "companyID")
    @Mapping(source = "name", target = "targetName")
    @Mapping(source = "description", target = "targetDescription")
    @Mapping(source = "eql", target = "EQL")
    TargetImpl toTarget(CreateTargetCommand command);
}
