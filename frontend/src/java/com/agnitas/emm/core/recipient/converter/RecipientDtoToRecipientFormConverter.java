/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.recipient.converter;

import static org.agnitas.emm.core.recipient.RecipientUtils.COLUMN_FREQUENCY_COUNTER_WEEK;
import static org.agnitas.emm.core.recipient.RecipientUtils.COLUMN_FREQUENCY_COUNT_DAY;
import static org.agnitas.emm.core.recipient.RecipientUtils.COLUMN_FREQUENCY_COUNT_MONTH;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletRequest;

import org.agnitas.emm.core.recipient.RecipientUtils;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.importvalues.Gender;
import org.agnitas.util.importvalues.MailType;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import com.agnitas.beans.ComAdmin;
import com.agnitas.beans.ProfileField;
import com.agnitas.emm.core.recipient.dto.FrequencyCounter;
import com.agnitas.emm.core.recipient.dto.RecipientDto;
import com.agnitas.emm.core.recipient.forms.RecipientForm;

@Component
public class RecipientDtoToRecipientFormConverter implements Converter<RecipientDto, RecipientForm> {

    private static final Logger logger = Logger.getLogger(RecipientDtoToRecipientFormConverter.class);

    @Lazy
    @Autowired
    private HttpServletRequest request;

    @Override
    public RecipientForm convert(RecipientDto recipientDto) {
        RecipientForm form = new RecipientForm();

        form.setId(recipientDto.getId());
        try {
            form.setGender(Gender.getGenderById(recipientDto.getGender()));
        } catch (Exception e) {
            logger.warn("Unknown gender type", e);
        }
        form.setTitle(recipientDto.getTitle());
        form.setFirstname(recipientDto.getFirstname());
        form.setLastname(recipientDto.getLastname());
        form.setEmail(recipientDto.getEmail());
        try {
            form.setMailtype(MailType.getFromInt(recipientDto.getMailtype()));
        } catch (Exception e) {
            logger.warn("Unknown mail type", e);
        }
        form.setTrackingVeto(recipientDto.isTrackingVeto());
        form.setLatestDataSourceId(recipientDto.getLatestDataSourceId());
        form.setDataSourceId(recipientDto.getDataSourceId());

        FrequencyCounter frequencyCounter = new FrequencyCounter();
        frequencyCounter.setDays(recipientDto.getIntValue(COLUMN_FREQUENCY_COUNT_DAY));
        frequencyCounter.setWeeks(recipientDto.getIntValue(COLUMN_FREQUENCY_COUNTER_WEEK));
        frequencyCounter.setMonths(recipientDto.getIntValue(COLUMN_FREQUENCY_COUNT_MONTH));
        form.setCounter(frequencyCounter);

        Set<ProfileField> recipientFields = recipientDto.getDbColumns().values().stream()
                    .sorted(Comparator.comparing(ProfileField::getSort)).collect(Collectors.toCollection(LinkedHashSet::new));
        form.setAdditionalColumns(collectAdditionalFields(recipientDto, recipientFields));

        return form;
    }

    private Map<String, String> collectAdditionalFields(RecipientDto dto, Set<ProfileField> recipientFields) {
        ComAdmin admin = AgnUtils.getAdmin(request);
        Map<String, String> columnData = new CaseInsensitiveMap<>();
        try {
            for (ProfileField field : recipientFields) {
                String columnName = field.getColumn();
                // ignore fields with supplemental suffix
                if (!RecipientUtils.hasSupplementalSuffix(columnName)) {
                    columnData.put(columnName, dto.getColumnFormattedValue(admin, columnName));
                }
            }
        } catch (Exception e) {
            logger.error("Could not collect additional fields value: ", e);
        }

        return columnData;
    }
}
