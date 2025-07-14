/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.usergroup.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import com.agnitas.emm.core.usergroup.dto.UserGroupDto;
import com.agnitas.emm.core.usergroup.form.UserGroupForm;

@Component
public class UserGroupFormToUserGroupDtoConverter implements Converter<UserGroupForm, UserGroupDto> {
    @Override
    public UserGroupDto convert(UserGroupForm form) {
        UserGroupDto userGroupDto = new UserGroupDto();

        userGroupDto.setUserGroupId(form.getId());
        userGroupDto.setCompanyId(form.getCompanyId());
        userGroupDto.setShortname(form.getShortname());
        userGroupDto.setDescription(form.getDescription());
        userGroupDto.setParentGroupIDs(form.getParentGroupIDs());
        userGroupDto.setGrantedPermissions(form.getGrantedPermissions());

        return userGroupDto;
    }
}
