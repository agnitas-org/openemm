/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.recipient.converter;

import java.util.Map;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import com.agnitas.emm.core.recipient.dto.SaveRecipientDto;
import com.agnitas.emm.core.recipient.forms.RecipientForm;
import com.agnitas.emm.core.service.RecipientStandardField;

@Component
public class RecipientFormToSaveRecipientDto implements Converter<RecipientForm, SaveRecipientDto>  {

    @Override
    public SaveRecipientDto convert(RecipientForm form) {

        SaveRecipientDto dto = new SaveRecipientDto();
        Map<String, String> data = dto.getFieldsToSave();
        dto.setId(form.getId());
        data.put(RecipientStandardField.CustomerID.getColumnName(), String.valueOf(form.getId()));
        data.put(RecipientStandardField.Gender.getColumnName(), String.valueOf(form.getGender().getStorageValue()));
        data.put(RecipientStandardField.Title.getColumnName(), form.getTitle());
        data.put(RecipientStandardField.Firstname.getColumnName(), form.getFirstname());
        data.put(RecipientStandardField.Lastname.getColumnName(), form.getLastname());
        data.put(RecipientStandardField.Email.getColumnName(), form.getEmail());
        data.put(RecipientStandardField.Mailtype.getColumnName(), String.valueOf(form.getMailtype().getIntValue()));
        data.put(RecipientStandardField.DoNotTrack.getColumnName(), String.valueOf(BooleanUtils.toInteger(form.isTrackingVeto())));
        data.put(RecipientStandardField.EncryptedSending.getColumnName(), String.valueOf(BooleanUtils.toInteger(form.isEncryptedSend())));
        data.put(RecipientStandardField.DatasourceID.getColumnName(), String.valueOf(form.getDataSourceId()));
        data.put(RecipientStandardField.LatestDatasourceID.getColumnName(), String.valueOf(form.getLatestDataSourceId()));
        form.getAdditionalColumns().forEach(data::put);
        return dto;
    }
}
