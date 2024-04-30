/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

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
public class UserGroupDtoToUserGroupFormConverter implements Converter<UserGroupDto, UserGroupForm> {
    @Override
    public UserGroupForm convert(UserGroupDto userGroup) {
        UserGroupForm userGroupForm = new UserGroupForm();

        userGroupForm.setId(userGroup.getUserGroupId());
        userGroupForm.setCompanyId(userGroup.getCompanyId());
        userGroupForm.setShortname(userGroup.getShortname());
        userGroupForm.setDescription(userGroup.getDescription());
        userGroupForm.setParentGroupIDs(userGroup.getParentGroupIDs());
        userGroupForm.setGrantedPermissions(userGroup.getGrantedPermissions());

        return userGroupForm;
    }
}
