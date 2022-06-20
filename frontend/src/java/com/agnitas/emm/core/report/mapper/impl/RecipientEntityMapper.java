/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.report.mapper.impl;

import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Component;

import com.agnitas.emm.core.report.bean.RecipientEntity;
import com.agnitas.emm.core.report.bean.impl.RecipientEntityImpl;
import com.agnitas.emm.core.report.enums.fields.RecipientFields;
import com.agnitas.emm.core.report.mapper.Mapper;

@Component
public class RecipientEntityMapper implements Mapper<RecipientEntity> {

    @Override
    public RecipientEntity map(Map<String, Object> map) {
        RecipientEntity recipientEntity = new RecipientEntityImpl();
        recipientEntity.setId(NumberUtils.toInt((String) map.get(RecipientFields.COLUMN_CUSTOMER_ID.getCode())));
        recipientEntity.setSalutation(NumberUtils.toInt((String) map.get(RecipientFields.COLUMN_SALUTATION.getCode()), 2));
        recipientEntity.setTitle((String) map.get(RecipientFields.COLUMN_TITLE.getCode()));
        recipientEntity.setFirstName((String) map.get(RecipientFields.COLUMN_FIRST_NAME.getCode()));
        recipientEntity.setLastName((String) map.get(RecipientFields.COLUMN_LAST_NAME.getCode()));
        recipientEntity.setEmail((String) map.get(RecipientFields.COLUMN_EMAIL.getCode()));
        recipientEntity.setTrackingVeto(NumberUtils.toInt((String) map.get(RecipientFields.COLUMN_TRACKING_VETO.getCode())) > 0);
        recipientEntity.setMailFormat(NumberUtils.toInt((String) map.get(RecipientFields.COLUMN_MAIL_TYPE.getCode()), 1));

        Map<String, Object> otherFields = map.entrySet().stream()
                .filter(entry -> !RecipientFields.isContainsCode(entry.getKey()) && StringUtils.isNotBlank((String) entry.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        recipientEntity.setOtherRecipientData(otherFields);

        return recipientEntity;
    }
}
