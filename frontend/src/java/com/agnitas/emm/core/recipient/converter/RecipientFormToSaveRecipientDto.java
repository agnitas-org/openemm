/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.recipient.converter;

import static org.agnitas.emm.core.recipient.RecipientUtils.COLUMN_DATASOURCE_ID;
import static org.agnitas.emm.core.recipient.RecipientUtils.COLUMN_CUSTOMER_ID;
import static org.agnitas.emm.core.recipient.RecipientUtils.COLUMN_DO_NOT_TRACK;
import static org.agnitas.emm.core.recipient.RecipientUtils.COLUMN_EMAIL;
import static org.agnitas.emm.core.recipient.RecipientUtils.COLUMN_ENCRYPTED_SENDING;
import static org.agnitas.emm.core.recipient.RecipientUtils.COLUMN_FIRSTNAME;
import static org.agnitas.emm.core.recipient.RecipientUtils.COLUMN_GENDER;
import static org.agnitas.emm.core.recipient.RecipientUtils.COLUMN_LASTNAME;
import static org.agnitas.emm.core.recipient.RecipientUtils.COLUMN_LATEST_DATASOURCE_ID;
import static org.agnitas.emm.core.recipient.RecipientUtils.COLUMN_MAILTYPE;
import static org.agnitas.emm.core.recipient.RecipientUtils.COLUMN_TITLE;

import java.util.Map;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import com.agnitas.emm.core.recipient.dto.SaveRecipientDto;
import com.agnitas.emm.core.recipient.forms.RecipientForm;

@Component
public class RecipientFormToSaveRecipientDto implements Converter<RecipientForm, SaveRecipientDto>  {

    @Override
    public SaveRecipientDto convert(RecipientForm form) {

        SaveRecipientDto dto = new SaveRecipientDto();
        Map<String, String> data = dto.getFieldsToSave();
        dto.setId(form.getId());
        data.put(COLUMN_CUSTOMER_ID, String.valueOf(form.getId()));
        data.put(COLUMN_GENDER, String.valueOf(form.getGender().getStorageValue()));
        data.put(COLUMN_TITLE, form.getTitle());
        data.put(COLUMN_FIRSTNAME, form.getFirstname());
        data.put(COLUMN_LASTNAME, form.getLastname());
        data.put(COLUMN_EMAIL, form.getEmail());
        data.put(COLUMN_MAILTYPE, String.valueOf(form.getMailtype().getIntValue()));
        data.put(COLUMN_DO_NOT_TRACK, String.valueOf(BooleanUtils.toInteger(form.isTrackingVeto())));
        data.put(COLUMN_ENCRYPTED_SENDING, String.valueOf(BooleanUtils.toInteger(form.isEncryptedSend())));
        data.put(COLUMN_DATASOURCE_ID, String.valueOf(form.getDataSourceId()));
        data.put(COLUMN_LATEST_DATASOURCE_ID, String.valueOf(form.getLatestDataSourceId()));
        form.getAdditionalColumns().forEach(data::put);
        return dto;
    }
}
