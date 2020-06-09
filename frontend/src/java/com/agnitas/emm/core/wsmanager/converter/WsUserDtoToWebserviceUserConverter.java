/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.wsmanager.converter;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import com.agnitas.emm.core.wsmanager.dto.WebserviceUserDto;
import com.agnitas.emm.springws.WebservicePasswordEncryptor;
import com.agnitas.emm.wsmanager.common.WebserviceUserCredential;
import com.agnitas.emm.wsmanager.common.impl.WebserviceUserCredentialImpl;

@Component
public class WsUserDtoToWebserviceUserConverter implements Converter<WebserviceUserDto, WebserviceUserCredential> {

    private WebservicePasswordEncryptor passwordEncryptor;

    public WsUserDtoToWebserviceUserConverter(WebservicePasswordEncryptor passwordEncryptor) {
        this.passwordEncryptor = passwordEncryptor;
    }

    @Override
    public WebserviceUserCredential convert(WebserviceUserDto userDto) {
        try {
            WebserviceUserCredentialImpl user = new WebserviceUserCredentialImpl();

            String username = userDto.getUserName();
            user.setUsername(username);

            int companyId = userDto.getCompanyId();
            user.setCompanyID(companyId);

            if(StringUtils.isNotEmpty(userDto.getPassword())){
                String passwordHash = passwordEncryptor.encrypt(username, userDto.getPassword());
                user.setPasswordHash(passwordHash);
            }

            user.setContactEmail(userDto.getEmail());
            user.setContact(userDto.getContactInfo());

            user.setActive(userDto.isActive());
            user.setGrantedPermissions(userDto.getGrantedPermissions());
            user.setGrantedPermissionGroupIDs(userDto.getGrantedPermissionGroupIDs());
            
            return user;
        } catch (Exception e) {
            throw new RuntimeException("Cannot converted to webservice user: " + e.getMessage());
        }
    }
}
