/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.recipient.dto;

import java.util.Map;

import org.agnitas.util.importvalues.Gender;
import org.agnitas.util.importvalues.MailType;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.agnitas.emm.core.service.RecipientStandardField;

public class SaveRecipientDto {

    private int id;
    private Map<String, String> fieldsToSave = new CaseInsensitiveMap<>();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Map<String, String> getFieldsToSave() {
        return fieldsToSave;
    }

    public void setFieldsToSave(Map<String, String> fieldsToSave) {
        this.fieldsToSave = fieldsToSave;
    }

    public int getIntValue(String column) {
        return getIntValue(column, 0);
    }

    public int getIntValue(String column, int defaultValue) {
        Object value = fieldsToSave.get(column);
        if (value != null) {
            return NumberUtils.toInt(value.toString(), defaultValue);
        }

        return defaultValue;
    }

    public String getStringValue(String column) {
        Object value = fieldsToSave.get(column);
        if (value != null) {
            return value.toString();
        }

        return "";
    }

    public String getEmail() {
        return getStringValue(RecipientStandardField.Email.getColumnName());
    }

    public int getGender() {
        return getIntValue(RecipientStandardField.Gender.getColumnName(), Gender.UNKNOWN.getStorageValue());
    }

    public String getTitle() {
        return getStringValue(RecipientStandardField.Title.getColumnName());
    }

    public String getFirstname() {
        return getStringValue(RecipientStandardField.Firstname.getColumnName());
    }

    public String getLastname() {
        return getStringValue(RecipientStandardField.Lastname.getColumnName());
    }

    public int getMailtype() {
        return getIntValue(RecipientStandardField.Mailtype.getColumnName(), MailType.HTML.getIntValue());
    }

    public boolean isTrackingVeto() {
        return BooleanUtils.toBoolean(getIntValue(RecipientStandardField.DoNotTrack.getColumnName()));
    }

    public int getLatestDataSourceId() {
        return getIntValue(RecipientStandardField.LatestDatasourceID.getColumnName());
    }

    public int getDataSourceId() {
        return getIntValue(RecipientStandardField.DatasourceID.getColumnName());
    }
}
