/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.admin.web;

import java.util.Locale;

import com.agnitas.emm.core.admin.form.AdminListForm;
import com.agnitas.emm.core.admin.service.UserListOpts;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface UserUiMapper {

    @Mapping(source = "form.filterUsername", target = "username")
    @Mapping(source = "form.searchCompany", target = "searchCompanyName")
    @Mapping(target = "sortDirectionAsc", expression = "java(\"ASC\".equalsIgnoreCase(form.getOrder()))")
    @Mapping(source = "form.page", target = "pageNumber")
    @Mapping(source = "form.filterCreationDate", target = "creationDate")
    @Mapping(source = "form.filterLastLoginDate", target = "lastLoginDate")
    @Mapping(source = "form.numberOfRows", target = "pageSize")
    UserListOpts toListOpts(AdminListForm form, int companyId, Locale locale);
}
