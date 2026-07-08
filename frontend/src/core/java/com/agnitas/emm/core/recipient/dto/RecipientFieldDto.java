/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.recipient.dto;

import com.agnitas.util.AgnUtils;
import com.agnitas.util.DbColumnType;

/**
 * @deprecated Use RecipientFieldDescription instead
 */
@Deprecated
public class RecipientFieldDto {
    private String shortname;
    private DbColumnType.SimpleDataType type;
    private String newValue;
    private boolean clear;
    
    public RecipientFieldDto() {
    }
    
    public RecipientFieldDto(String shortname, DbColumnType.SimpleDataType type, String newValue, boolean clear) {
        this.shortname = shortname;
        this.type = type;
        this.newValue = newValue;
        this.clear = clear;
    }
    
    public String getShortname() {
        return shortname;
    }

    public void setShortname(String shortname) {
        this.shortname = shortname;
    }

    public DbColumnType.SimpleDataType getType() {
        return type;
    }

    public void setType(DbColumnType.SimpleDataType type) {
        this.type = type;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public boolean isClear() {
        return clear;
    }
    
    public String isClearAsString() {
        return clear ? "on" : "";
    }

    public void setClear(boolean clear) {
        this.clear = clear;
    }
    
    public void setClear(String clearValue) {
        this.clear = AgnUtils.interpretAsBoolean(clearValue);
    }
}
