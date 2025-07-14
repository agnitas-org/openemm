/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.recipient.dto;

import java.util.List;

import com.agnitas.util.DbColumnType;

import com.agnitas.beans.ProfileFieldMode;

/**
 * @deprecated Use RecipientFieldDescription instead
 */
@Deprecated
public class RecipientColumnDefinition {
    private String columnName;
    private String shortname;
    private String description;
    private DbColumnType.SimpleDataType dataType;
    private ProfileFieldMode editMode;
    private boolean nullable;
    private long maxSize;
    private boolean lineAfter;
    private String defaultValue;
    private List<String> fixedValues;
    private boolean isMainColumn;

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setShortname(String shortname) {
        this.shortname = shortname;
    }

    public String getShortname() {
        return shortname;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDataType(DbColumnType.SimpleDataType dataType) {
        this.dataType = dataType;
    }

    public DbColumnType.SimpleDataType getDataType() {
        return dataType;
    }

    public void setEditMode(ProfileFieldMode editMode) {
        this.editMode = editMode;
    }

    public ProfileFieldMode getEditMode() {
        return editMode;
    }

    public boolean isReadable() {
        return editMode == ProfileFieldMode.Editable || editMode == ProfileFieldMode.ReadOnly;
    }

    public boolean isWritable() {
        return editMode == ProfileFieldMode.Editable;
    }

    public void setNullable(boolean nullable) {
        this.nullable = nullable;
    }

    public boolean isNullable() {
        return nullable;
    }
    
    public void setMaxSize(long maxSize) {
        this.maxSize = maxSize;
    }

    public long getMaxSize() {
        return maxSize;
    }

    public boolean isLineAfter() {
        return lineAfter;
    }

    public void setLineAfter(boolean lineAfter) {
        this.lineAfter = lineAfter;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setFixedValues(List<String> fixedValues) {
        this.fixedValues = fixedValues;
    }

    public List<String> getFixedValues() {
        return fixedValues;
    }

    public boolean isMainColumn() {
        return isMainColumn;
    }

    public void setMainColumn(boolean mainColumn) {
        isMainColumn = mainColumn;
    }
}
