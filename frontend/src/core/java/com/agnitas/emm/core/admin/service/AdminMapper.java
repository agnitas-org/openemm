/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.admin.service;

import java.util.List;

import com.agnitas.beans.AdminGroup;
import com.agnitas.beans.Company;
import com.agnitas.beans.impl.AdminImpl;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface AdminMapper {

    @Mapping(target = "altgIds", ignore = true)
    @Mapping(target = "accessLimitingTargetGroupID", ignore = true)
    @Mapping(target = "defaultImportProfileID", ignore = true)
    @Mapping(target = "companyPermissions", ignore = true)
    @Mapping(target = "layoutType", ignore = true)
    @Mapping(target = "passwordReminderState", ignore = true)
    @Mapping(target = "layoutBaseID", ignore = true)
    @Mapping(target = "layoutID", ignore = true)
    @Mapping(target = "restful", ignore = true)
    @Mapping(target = "adminID", ignore = true)
    @Mapping(target = "supervisor", ignore = true)
    @Mapping(target = "lastNewsDate", ignore = true)
    @Mapping(target = "lastLoginDate", ignore = true)
    @Mapping(target = "lastPasswordChange", ignore = true)
    @Mapping(target = "lastMessageDate", ignore = true)
    @Mapping(target = "securePasswordHash", ignore = true)
    @Mapping(target = "passwordForStorage", source = "command.password")
    @Mapping(target = "adminPermissions", expression = "java(Set.of())")
    @Mapping(target = "adminCountry", expression = "java(mapCountry(command))")
    @Mapping(target = "adminLang", expression = "java(mapLanguage(command))")
    @Mapping(target = "adminLangVariant", expression = "java(mapCountry(command))")
    @Mapping(target = "shortname", source = "command.firstName")
    @Mapping(target = "fullname", source = "command.lastName")
    @Mapping(target = "companyName", source = "command.companyName")
    @Mapping(target = "company", source = "company")
    @Mapping(target = "gender", source = "command.gender.storageValue")
    @Mapping(target = "adminTimezone", source = "command.timezone")
    @Mapping(target = "adminPhone", source = "command.phone")
    @Mapping(target = "employeeID", source = "command.employeeId")
    @Mapping(target = "statEmail", source = "command.statisticsEmail")
    AdminImpl toUiUser(
            CreateUiUserCommand command,
            Company company,
            List<AdminGroup> groups
    );

    default String mapLanguage(CreateUiUserCommand command) {
        return command.language().substring(0, command.language().indexOf('_'));
    }

    default String mapCountry(CreateUiUserCommand command) {
        if (command.language() == null || !command.language().contains("_")) {
            return null;
        }
        return command.language().substring(command.language().indexOf('_') + 1);
    }
}
