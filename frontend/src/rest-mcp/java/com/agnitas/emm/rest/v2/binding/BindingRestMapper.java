/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2.binding;

import com.agnitas.beans.BindingEntry;
import com.agnitas.beans.BindingEntry.UserType;
import com.agnitas.emm.common.UserStatus;
import com.agnitas.emm.core.binding.BindingIdentifier;
import com.agnitas.emm.core.binding.BulkDeleteBindingCommand;
import com.agnitas.emm.core.binding.BulkUpsertBindingCommand;
import com.agnitas.emm.core.binding.CreateBindingCommand;
import com.agnitas.emm.core.binding.DeleteBindingCommand;
import com.agnitas.emm.core.binding.GetBindingQuery;
import com.agnitas.emm.core.binding.ListBindingsQuery;
import com.agnitas.emm.core.binding.PatchBindingCommand;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.emm.rest.v2.infrastructure.mapper.RestMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.springframework.data.domain.Pageable;

@Mapper(
        componentModel = "spring",
        imports = {MediaTypes.class, UserStatus.class, UserType.class},
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
interface BindingRestMapper extends RestMapper {

    GetBindingQuery toQuery(BindingIdentifier request);

    BulkDeleteBindingCommand toCommand(BulkDeleteBindingRequest request, int companyId);

    @Mapping(source = "request.userStatus", target = "userStatus")
    @Mapping(source = "request.userType", target = "userType")
    @Mapping(source = "request.userRemark", target = "userRemark")
    @Mapping(source = "request.referrer", target = "referrer")
    @Mapping(source = "request.entryMailingId", target = "entryMailingId")
    @Mapping(source = "request.exitMailingId", target = "exitMailingId")
    @Mapping(source = "identifier", target = "getQuery")
    PatchBindingCommand toCommand(BindingIdentifier identifier, PatchBindingRequest request);

    @Mapping(source = "pageable.pageNumber", target = "page")
    @Mapping(source = "pageable.pageSize", target = "size")
    ListBindingsQuery toQuery(ListBindingsRequest request, Pageable pageable);

    CreateBindingCommand toCommand(CreateBindingRequest request);

    DeleteBindingCommand toDeleteCommand(BindingIdentifier identifier);

    @Mapping(source = "binding.mailinglistID", target = "mailinglistId")
    @Mapping(source = "binding.customerID", target = "recipientId")
    @Mapping(source = "binding.userType", target = "userType")
    @Mapping(target = "mediatype", expression = "java(MediaTypes.getMediaTypeForCode(binding.getMediaType()).name())")
    @Mapping(target = "userStatus", expression = "java(UserStatus.getByCode(binding.getUserStatus()).name())")
    @Mapping(source = "binding.userRemark", target = "userRemark")
    @Mapping(source = "binding.referrer", target = "referrer")
    @Mapping(source = "binding.entryMailingID", target = "entryMailingId")
    @Mapping(source = "binding.exitMailingID", target = "exitMailingId")
    @Mapping(source = "binding.creationDate", target = "creationDate")
    @Mapping(source = "binding.changeDate", target = "changeDate")
    @Mapping(source = "actionExecutionInfo", target = "actionExecutionInfo")
    BindingResponse toResponse(BindingEntry binding, String actionExecutionInfo);

    default BindingResponse toResponse(BindingEntry binding) {
        return toResponse(binding, null);
    }

    BulkUpsertBindingCommand toCommand(BulkUpsertBindingRequest request, String remark);
}
