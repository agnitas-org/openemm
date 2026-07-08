/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.recipient.dto;

import static com.agnitas.emm.core.recipient.utils.RecipientUtils.formatRecipientDateTimeValue;
import static com.agnitas.emm.core.recipient.utils.RecipientUtils.formatRecipientDateValue;
import static com.agnitas.emm.core.recipient.utils.RecipientUtils.formatRecipientDoubleValue;

import java.util.Date;
import java.util.Map;

import com.agnitas.beans.Admin;
import com.agnitas.beans.ProfileField;
import com.agnitas.emm.core.service.RecipientStandardField;
import com.agnitas.util.DbColumnType;
import com.agnitas.util.importvalues.Gender;
import com.agnitas.util.importvalues.MailType;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

public class RecipientDto {

    private int id;

    private Map<String, Object> parameters = new CaseInsensitiveMap<>();
    private Map<String, ProfileField> dbColumns = new CaseInsensitiveMap<>();

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    public Map<String, ProfileField> getDbColumns() {
        return dbColumns;
    }

    public void setDbColumns(Map<String, ProfileField> dbColumns) {
        this.dbColumns = dbColumns;
    }

    public Object getValue(String column) {
        return parameters.get(column);
    }

    public String getStringValue(String column) {
        Object value = parameters.get(column);
        if (value != null) {
            return value.toString();
        } else {
        	return "";
        }
    }

    public int getIntValue(String column) {
        return getIntValue(column, 0);
    }

    public int getIntValue(String column, int defaultValue) {
        Object value = parameters.get(column);
        if (value != null) {
            return ((Number) value).intValue();
        } else {
        	return defaultValue;
        }
    }

    public Date getDateValue(String column) {
        Object value = parameters.get(column);
        if (value != null) {
            return (Date) value;
        } else {
        	return null;
        }
    }

    public double getDoubleValue(String column) {
        Object value = parameters.get(column);
        if (value != null) {
            return ((Number) value).doubleValue();
        } else {
        	return 0.0d;
        }
    }

    public String getColumnFormattedValue(Admin admin, String columnName) {
        ProfileField profileField = getDbColumns().get(columnName);
        if (profileField == null) {
            return "";
        } else {
	        DbColumnType.SimpleDataType dataType = profileField.getSimpleDataType();
	        String formattedValue;
	        if (dataType == DbColumnType.SimpleDataType.Date) {
	            formattedValue = formatRecipientDateValue(admin, getDateValue(columnName));
	        } else if (dataType == DbColumnType.SimpleDataType.DateTime) {
	            formattedValue = formatRecipientDateTimeValue(admin, getDateValue(columnName));
	        } else if (dataType == DbColumnType.SimpleDataType.Float) {
	        	if (parameters.get(columnName) == null) {
	        		formattedValue = "";
	        	} else {
	        		formattedValue = formatRecipientDoubleValue(admin, getDoubleValue(columnName));
	        	}
	        } else if (dataType == DbColumnType.SimpleDataType.Numeric) {
	        	if (parameters.get(columnName) == null) {
	        		formattedValue = "";
	        	} else {
		            formattedValue = formatRecipientDoubleValue(admin, getIntValue(columnName));
	        	}
	        } else {
	            formattedValue = getStringValue(columnName);
	        }
	
	        return formattedValue;
        }
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

    public boolean isEncryptedSend() {
        return BooleanUtils.toBoolean(getIntValue(RecipientStandardField.EncryptedSending.getColumnName()));
    }

    public int getLatestDataSourceId() {
        return getIntValue(RecipientStandardField.LatestDatasourceID.getColumnName());
    }

    public int getDataSourceId() {
        return getIntValue(RecipientStandardField.DatasourceID.getColumnName());
    }

    public String getTimestamp(){
        return getStringValue(RecipientStandardField.ChangeDate.getColumnName());
    }

    public String getMention() {
        String shortname = getFirstname() + " " + getLastname();
        return (StringUtils.isBlank(shortname) ? getEmail() : shortname).trim();
    }
}
