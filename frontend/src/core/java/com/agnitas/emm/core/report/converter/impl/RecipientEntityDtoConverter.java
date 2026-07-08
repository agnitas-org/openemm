/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.report.converter.impl;

import java.util.HashMap;
import java.util.Locale;

import com.agnitas.emm.core.mailing.service.MailingModel;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.agnitas.emm.core.report.bean.RecipientEntity;
import com.agnitas.emm.core.report.converter.GenericLocalizableConverter;
import com.agnitas.emm.core.report.dto.RecipientEntityDto;
import com.agnitas.emm.core.report.dto.impl.RecipientEntityDtoImpl;
import com.agnitas.emm.core.report.enums.fields.Salutations;
import com.agnitas.messages.I18nString;

@Component
public class RecipientEntityDtoConverter implements GenericLocalizableConverter<RecipientEntity, RecipientEntityDto> {

    private static final String DEFAULT_VALUE = "Not Set";

    @Override
    public RecipientEntityDto convert(RecipientEntity entity, Locale locale) {
        RecipientEntityDtoImpl recipientEntityDto = new RecipientEntityDtoImpl();

        String salutationKey = Salutations.getTranslationKeyByCode(entity.getSalutation());
        recipientEntityDto.setSalutation(I18nString.getLocaleStringOrDefault(salutationKey, locale, DEFAULT_VALUE));
        recipientEntityDto.setTitle(StringUtils.defaultIfEmpty(entity.getTitle(), DEFAULT_VALUE));
        recipientEntityDto.setFirstName(StringUtils.defaultIfEmpty(entity.getFirstName(), DEFAULT_VALUE));
        recipientEntityDto.setLastName(StringUtils.defaultIfEmpty(entity.getLastName(), DEFAULT_VALUE));
        recipientEntityDto.setEmail(StringUtils.defaultIfEmpty(entity.getEmail(), DEFAULT_VALUE));
        recipientEntityDto.setTrackingVeto(entity.isTrackingVeto());
        recipientEntityDto.setMailFormat(MailingModel.Format.getByCode(entity.getMailFormat()).getReadableName());
        recipientEntityDto.setOtherRecipientData(new HashMap<>(entity.getOtherRecipientData()));

        return recipientEntityDto;
    }
}
