/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2.user;

import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;

import com.agnitas.beans.Admin;
import com.agnitas.beans.AdminEntry;
import com.agnitas.beans.AdminGroup;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.admin.service.CreateUiUserCommand;
import com.agnitas.emm.core.admin.service.PatchUiUserCommand;
import com.agnitas.emm.core.admin.service.UserListOpts;
import com.agnitas.emm.core.usergroup.dto.UserGroupDto;
import com.agnitas.emm.rest.v2.infrastructure.mapper.RestMapper;
import com.agnitas.messages.I18nString;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.springframework.data.domain.Pageable;

@Mapper(
        componentModel = "spring",
        imports = I18nString.class,
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
interface UiUserRestMapper extends RestMapper {

    @Mapping(target = "permissions", source = "admin.adminPermissions")
    @Mapping(target = "statisticsEmail", source = "admin.statEmail")
    @Mapping(target = "phone", source = "admin.adminPhone")
    @Mapping(target = "firstname", source = "admin.firstName")
    @Mapping(target = "lastname", source = "admin.fullname")
    @Mapping(target = "company", source = "admin.companyName")
    @Mapping(target = "clientId", source = "admin.companyID")
    @Mapping(target = "id", source = "admin.adminID")
    @Mapping(target = "timezone", source = "admin.adminTimezone")
    @Mapping(target = "language", expression = "java(toLanguage(admin))")
    @Mapping(target = "accessLimitingTargetGroupIds", expression = "java(mapAltgIds(admin))")
    UiUserResponse toResponse(Admin admin, Collection<Integer> disabledMailingListIds);

    @Mapping(target = "id", source = "groupID")
    @Mapping(target = "name", source = "shortname")
    UserGroupResponse toResponse(AdminGroup userGroup);

    @Mapping(target = "name", source = "shortname")
    @Mapping(target = "id", source = "userGroupId")
    UserGroupResponse toResponse(UserGroupDto userGroupDto);

    default String toLanguage(Admin admin) {
        return admin.getAdminLang()
               + (StringUtils.isNotEmpty(admin.getAdminCountry())
                ? "_" + admin.getAdminCountry()
                : "");
    }

    default Set<Integer> mapAltgIds(Admin admin) {
        if (CollectionUtils.isNotEmpty(admin.getAltgIds())) {
            return admin.getAltgIds();
        }
        if (admin.getAccessLimitingTargetGroupID() > 0) {
            return Set.of(admin.getAccessLimitingTargetGroupID());
        }
        return Collections.emptySet();
    }

    @Mapping(target = "code", source = "tokenString")
    @Mapping(target = "name", expression = "java(mapPermissionName(permission))")
    UserPermissionResponse toResponse(Permission permission);

    default String mapPermissionName(Permission permission) {
        return I18nString.getLocaleString("UserRight." + permission.getTokenString());
    }

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    @Mapping(source = "pageable.sort", target = "sortDirectionAsc")
    UserListOpts toListOpts(Pageable pageable, int companyId, Locale locale);

    @Mapping(target = "clientId", source = "companyID")
    @Mapping(target = "lastname", source = "fullname")
    UiUserLightResponse toLightResponse(AdminEntry adminEntry);

    @Mapping(target = "companyName", source = "company")
    CreateUiUserCommand toCommand(CreateUiUserRequest request);

    @Mapping(target = "companyName", source = "company")
    PatchUiUserCommand toCommand(PatchUiUserRequest request);
}
