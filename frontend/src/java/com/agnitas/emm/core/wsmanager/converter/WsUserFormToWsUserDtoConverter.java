/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.wsmanager.converter;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import com.agnitas.emm.core.wsmanager.dto.WebserviceUserDto;
import com.agnitas.emm.core.wsmanager.form.WebserviceUserForm;

@Component
public class WsUserFormToWsUserDtoConverter implements Converter<WebserviceUserForm, WebserviceUserDto> {

    @Override
    public WebserviceUserDto convert(WebserviceUserForm userForm) {
        WebserviceUserDto userDto = new WebserviceUserDto();
        userDto.setUserName(userForm.getUserName());
        userDto.setEmail(userForm.getEmail());
        userDto.setPassword(userForm.getPassword());
        userDto.setActive(userForm.isActive());
        userDto.setCompanyId(userForm.getCompanyId());
        
        final Set<String> permissions = userForm.getEndpointPermission().entrySet().stream()
        		.filter(entry -> "true".equals(entry.getValue()))
        		.map(entry -> entry.getKey())
        		.collect(Collectors.toSet());
        
        userDto.setGrantedPermissions(permissions);
        
        final Set<Integer> permissionGroups = userForm.getPermissionGroup().entrySet().stream()
        		.filter(entry -> "true".equals(entry.getValue()))
        		.map(entry -> entry.getKey())
        		.collect(Collectors.toSet());
        		
        userDto.setGrantedPermissionGroupIDs(permissionGroups);
        
        return userDto;
    }
}
