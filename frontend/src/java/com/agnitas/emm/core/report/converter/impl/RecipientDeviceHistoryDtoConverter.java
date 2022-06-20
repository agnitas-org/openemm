/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.report.converter.impl;

import java.util.Locale;
import java.util.Objects;

import org.springframework.stereotype.Component;

import com.agnitas.beans.ComRecipientReaction;
import com.agnitas.emm.core.report.converter.GenericLocalizableConverter;
import com.agnitas.emm.core.report.dto.RecipientDeviceHistoryDto;
import com.agnitas.emm.core.report.dto.impl.RecipientDeviceHistoryDtoImpl;
import com.agnitas.messages.I18nString;

@Component
public class RecipientDeviceHistoryDtoConverter implements GenericLocalizableConverter<ComRecipientReaction, RecipientDeviceHistoryDto> {

    @Override
    public RecipientDeviceHistoryDto convert(ComRecipientReaction entity, Locale locale) {
        RecipientDeviceHistoryDto deviceHistoryDto = new RecipientDeviceHistoryDtoImpl();

        String actionDescription = I18nString.getLocaleString(entity.getReactionType().getMessageKey(), locale);
        if (Objects.nonNull(entity.getClickedUrl())){
            actionDescription += " (" + entity.getClickedUrl().toExternalForm() + ")";
        }

        deviceHistoryDto.setDate(entity.getTimestamp());
        deviceHistoryDto.setMailingDescription(entity.getMailingName());
        deviceHistoryDto.setActionDescription(actionDescription);
        deviceHistoryDto.setDeviceType(entity.getDeviceClass());
        deviceHistoryDto.setDeviceName(entity.getDeviceName());

        return deviceHistoryDto;
    }
}
