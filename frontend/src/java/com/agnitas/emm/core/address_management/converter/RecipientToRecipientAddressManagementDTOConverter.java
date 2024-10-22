/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.address_management.converter;

import com.agnitas.emm.core.address_management.dto.RecipientAddressManagementDTO;
import org.agnitas.beans.Recipient;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class RecipientToRecipientAddressManagementDTOConverter implements Converter<Recipient, RecipientAddressManagementDTO> {

    @Override
    public RecipientAddressManagementDTO convert(Recipient source) {
        return new RecipientAddressManagementDTO(
                source.getCustomerID(),
                source.getCompanyID(),
                source.getGender(),
                source.getFirstname(),
                source.getLastname(),
                source.getTimestamp().getTime()
        );
    }

}
