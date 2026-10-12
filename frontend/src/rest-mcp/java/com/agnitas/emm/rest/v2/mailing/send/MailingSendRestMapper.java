/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2.mailing.send;

import java.time.OffsetDateTime;
import java.util.Date;

import com.agnitas.beans.MailingSendOptions;
import com.agnitas.emm.rest.v2.infrastructure.mapper.RestMapper;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
interface MailingSendRestMapper extends RestMapper {

    @BeanMapping(
            unmappedTargetPolicy = ReportingPolicy.IGNORE,
            nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT
    )
    @Mapping(source = "cleanupAdminAndTestActions", target = "cleanupTestsBeforeDelivery")
    MailingSendOptions.Builder toSendOptions(SendTestMailingRequest request);

    @BeanMapping(
            unmappedTargetPolicy = ReportingPolicy.IGNORE,
            nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT
    )
    @Mapping(source = "cleanupAdminAndTestActions", target = "cleanupTestsBeforeDelivery")
    MailingSendOptions.Builder toSendOptions(SendAdminMailingRequest request);

    @BeanMapping(
            unmappedTargetPolicy = ReportingPolicy.IGNORE,
            nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT
    )
    @Mapping(source = "sendDate", target = "date")
    @Mapping(source = "cleanupAdminAndTestActions", target = "cleanupTestsBeforeDelivery")
    MailingSendOptions.Builder toSendOptions(StartSendMailingRequest request);

    default Date toDate(OffsetDateTime date) {
        return date == null ? new Date() : Date.from(date.toInstant());
    }
}
