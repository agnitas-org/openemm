/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.profilefields.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import com.agnitas.beans.ProfileField;
import com.agnitas.beans.impl.ProfileFieldImpl;
import com.agnitas.emm.core.profilefields.form.ProfileFieldForm;

@Component
public class ProfileFieldFormToComProfileFieldConverter implements Converter<ProfileFieldForm, ProfileField> {

    @Override
    public ProfileField convert(ProfileFieldForm form) {
        ProfileField field = new ProfileFieldImpl();

        field.setColumn(form.getFieldname());
        field.setDataType(form.getFieldType());
        field.setDataTypeLength(form.getFieldLength());
        field.setDescription(form.getDescription());
        field.setShortname(form.getShortname());
        field.setDefaultValue(form.getFieldDefault());
        field.setSort(form.getFieldSort());
        field.setModeEdit(form.isFieldVisible() ? ProfileField.MODE_EDIT_EDITABLE : ProfileField.MODE_EDIT_NOT_VISIBLE);
        field.setLine(form.getLine() ? 1 : 0);
        field.setInterest(form.isInterest());
        field.setHistorize(form.isIncludeInHistory());
        field.setNullable(form.isFieldNull());

        if (form.isUseAllowedValues()) {
            field.setAllowedValues(form.getAllowedValues());
        }

        return field;
    }
}
