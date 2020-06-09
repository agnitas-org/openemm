/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.profilefields.converter;

import org.agnitas.beans.ProfileField;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import com.agnitas.beans.ComProfileField;
import com.agnitas.emm.core.profilefields.form.ProfileFieldForm;

@Component
public class ComProfileFieldToProfileFieldFormConverter implements Converter<ComProfileField, ProfileFieldForm> {

    @Override
    public ProfileFieldForm convert(ComProfileField field) {
        ProfileFieldForm form = new ProfileFieldForm();

        form.setFieldname(field.getColumn());
        form.setShortname(field.getShortname());
        form.setDescription(field.getDescription());
        form.setFieldType(field.getDataType());
        form.setFieldLength(field.getDataTypeLength());
        form.setFieldDefault(field.getDefaultValue());
        form.setFieldNull(field.getNullable());
        form.setFieldSort(field.getSort());
        form.setLine(field.getLine() > 0);
        form.setInterest(field.isInterest());
        form.setFieldVisible(field.getModeEdit() != ProfileField.MODE_EDIT_NOT_VISIBLE);
        form.setIncludeInHistory(field.getHistorize());
        form.setAllowedValues(field.getAllowedValues());
        form.setUseAllowedValues(form.getAllowedValues() != null);

        return form;
    }
}
