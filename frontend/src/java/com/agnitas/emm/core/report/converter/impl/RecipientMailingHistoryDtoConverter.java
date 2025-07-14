/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.report.converter.impl;

import java.util.Locale;

import org.springframework.stereotype.Component;

import com.agnitas.beans.RecipientMailing;
import com.agnitas.emm.core.report.converter.GenericLocalizableConverter;
import com.agnitas.emm.core.report.dto.RecipientMailingHistoryDto;
import com.agnitas.emm.core.report.dto.impl.RecipientMailingHistoryDtoImpl;
import com.agnitas.messages.I18nString;

@Component
public class RecipientMailingHistoryDtoConverter implements GenericLocalizableConverter<RecipientMailing, RecipientMailingHistoryDto> {

    @Override
    public RecipientMailingHistoryDto convert(RecipientMailing entity, Locale locale) {
        String translationKey = entity.getMailingType().getMessagekey();
        String translatedMailingType = I18nString.getLocaleString(translationKey, locale);

        RecipientMailingHistoryDto recipientMailingHistoryDto = new RecipientMailingHistoryDtoImpl();
        recipientMailingHistoryDto.setDeliveryDate(entity.getDeliveryDate());
        recipientMailingHistoryDto.setMailingType(translatedMailingType);
        recipientMailingHistoryDto.setNumberOfClicks(entity.getNumberOfClicks());
        recipientMailingHistoryDto.setNumberOfOpenings(entity.getNumberOfOpenings());
        recipientMailingHistoryDto.setSendDate(entity.getSendDate());
        recipientMailingHistoryDto.setShortName(entity.getShortName());
        recipientMailingHistoryDto.setSubject(entity.getSubject());
        recipientMailingHistoryDto.setNumberOfDeliveries(entity.getSendCount());

        return recipientMailingHistoryDto;
    }
}
