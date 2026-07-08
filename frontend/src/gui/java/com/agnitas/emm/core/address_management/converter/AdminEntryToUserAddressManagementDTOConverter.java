/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.address_management.converter;

import com.agnitas.emm.core.address_management.dto.UserAddressManagementDTO;
import com.agnitas.beans.AdminEntry;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.Optional;

@Component
public class AdminEntryToUserAddressManagementDTOConverter implements Converter<AdminEntry, UserAddressManagementDTO> {

    @Override
    public UserAddressManagementDTO convert(AdminEntry source) {
        return new UserAddressManagementDTO(
                source.getId(),
                source.getCompanyID(),
                source.getUsername(),
                source.getFirstname(),
                source.getFullname(),
                Optional.ofNullable(source.getLoginDate()).map(Timestamp::getTime).orElse(0L)
        );
    }

}
