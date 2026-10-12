/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.binding.service;

import com.agnitas.beans.BindingEntry;
import com.agnitas.beans.impl.BindingEntryImpl;
import com.agnitas.dao.BindingEntryDao.BulkDeleteBindingsFilter;
import com.agnitas.dao.BindingEntryDao.ListBindingsFilter;
import com.agnitas.emm.common.UserStatus;
import com.agnitas.emm.core.binding.BulkDeleteBindingCommand;
import com.agnitas.emm.core.binding.BulkUpsertBindingCommand;
import com.agnitas.emm.core.binding.CreateBindingCommand;
import com.agnitas.emm.core.binding.GetBindingQuery;
import com.agnitas.emm.core.binding.ListBindingsQuery;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        imports = {MediaTypes.class, UserStatus.class},
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface BindingMapper {

    ListBindingsFilter toFilter(ListBindingsQuery query);

    @Mapping(target = "emails", ignore = true)
    @Mapping(target = "userStatuses", ignore = true)
    @Mapping(target = "size", ignore = true)
    @Mapping(target = "page", ignore = true)
    @Mapping(target = "date", ignore = true)
    ListBindingsFilter toFilter(BulkUpsertBindingCommand query);

    BulkDeleteBindingsFilter toFilter(BulkDeleteBindingCommand command);

    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "changeDate", ignore = true)
    @Mapping(target = "bindingEntryDao", ignore = true)
    @Mapping(source = "mailinglistId", target = "mailinglistID")
    @Mapping(source = "recipientId", target = "customerID")
    @Mapping(source = "mediatype", target = "mediaType")
    @Mapping(source = "userStatus", target = "userStatus")
    @Mapping(source = "userType", target = "userType")
    @Mapping(source = "entryMailingId", target = "entryMailingID")
    @Mapping(source = "exitMailingId", target = "exitMailingID")
    BindingEntryImpl toEntity(CreateBindingCommand query);

    GetBindingQuery toGetQuery(CreateBindingCommand query);

    default int toMediatypeCode(MediaTypes mediaType) {
        return mediaType.getMediaCode();
    }

    default int toUserStatusCode(UserStatus userStatus) {
        return userStatus.getStatusCode();
    }

    default String toUserTypeCode(BindingEntry.UserType userType) {
        return userType.getTypeCode();
    }
}
