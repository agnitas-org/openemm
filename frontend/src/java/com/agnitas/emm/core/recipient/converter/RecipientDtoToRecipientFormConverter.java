/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.recipient.converter;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.agnitas.beans.Admin;
import com.agnitas.beans.ProfileField;
import com.agnitas.emm.core.recipient.dto.FrequencyCounter;
import com.agnitas.emm.core.recipient.dto.RecipientDto;
import com.agnitas.emm.core.recipient.forms.RecipientForm;
import com.agnitas.emm.core.service.RecipientFieldService.RecipientOptionalField;
import com.agnitas.util.importvalues.Gender;
import com.agnitas.util.importvalues.MailType;
import org.agnitas.emm.core.recipient.RecipientUtils;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RecipientDtoToRecipientFormConverter {
    private static final Logger logger = LogManager.getLogger(RecipientDtoToRecipientFormConverter.class);
    
    public static RecipientForm convert(RecipientDto recipientDto, Admin admin) {
        RecipientForm form = new RecipientForm();

        form.setId(recipientDto.getId());
        form.setGender(Gender.getGenderById(recipientDto.getGender()));

        form.setTitle(recipientDto.getTitle());
        form.setFirstname(recipientDto.getFirstname());
        form.setLastname(recipientDto.getLastname());
        form.setEmail(recipientDto.getEmail());
        form.setEncryptedSend(recipientDto.isEncryptedSend());
        form.setMailtype(MailType.getFromInt(recipientDto.getMailtype()));

        form.setTrackingVeto(recipientDto.isTrackingVeto());
        form.setLatestDataSourceId(recipientDto.getLatestDataSourceId());
        form.setDataSourceId(recipientDto.getDataSourceId());

        FrequencyCounter frequencyCounter = new FrequencyCounter();
        frequencyCounter.setDays(recipientDto.getIntValue(RecipientOptionalField.FrequencyCountDay.getColumnName()));
        frequencyCounter.setWeeks(recipientDto.getIntValue(RecipientOptionalField.FrequencyCounterWeek.getColumnName()));
        frequencyCounter.setMonths(recipientDto.getIntValue(RecipientOptionalField.FrequencycountMonth.getColumnName()));
        form.setCounter(frequencyCounter);

        Set<ProfileField> recipientFields = recipientDto.getDbColumns().values().stream()
                    .sorted(Comparator.comparing(ProfileField::getSort)).collect(Collectors.toCollection(LinkedHashSet::new));
        
        Map<String, String> columnData = new CaseInsensitiveMap<>();
        try {
            for (ProfileField field : recipientFields) {
                String columnName = field.getColumn();
                // ignore fields with supplemental suffix
                if (!RecipientUtils.hasSupplementalSuffix(columnName)) {
                    columnData.put(columnName, recipientDto.getColumnFormattedValue(admin, columnName));
                }
            }
        } catch (Exception e) {
            logger.error("Could not collect additional fields value: ", e);
        }
        
        form.setAdditionalColumns(columnData);

        return form;
    }
}
