/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.recipient.converter;

import org.agnitas.beans.BindingEntry;
import org.agnitas.dao.UserStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.emm.core.recipient.dto.RecipientBindingDto;

@Component
public class BindingEntryToRecipientBindingDtoConverter implements Converter<BindingEntry, RecipientBindingDto> {

    private static final Logger logger = LogManager.getLogger(BindingEntryToRecipientBindingDtoConverter.class);

    @Override
    public RecipientBindingDto convert(BindingEntry bindingEntry) {
        RecipientBindingDto dto = new RecipientBindingDto(MediaTypes.getMediaTypeForCode(bindingEntry.getMediaType()));
        dto.setMailinglistId(bindingEntry.getMailinglistID());

        UserStatus userStatus = null;
        try {
             userStatus = UserStatus.getUserStatusByID(bindingEntry.getUserStatus());
        } catch (Exception e) {
            logger.warn("Incorrect user status code: " + bindingEntry.getUserStatus());
        }
        dto.setStatus(userStatus);
        dto.setUserType(bindingEntry.getUserType());

        dto.setUserRemark(bindingEntry.getUserRemark());
        dto.setReferrer(bindingEntry.getReferrer());
        dto.setExitMailingId(bindingEntry.getExitMailingID());
        dto.setChangeDate(bindingEntry.getChangeDate());
        return dto;
    }
}
