/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/


package com.agnitas.beans;

import org.apache.commons.lang3.BooleanUtils;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BooleanWebStorageEntry implements WebStorageEntry {

    @JsonProperty("value")
    private Boolean value;

    public Boolean getValue() {
        return value;
    }

    public void setValue(Boolean value) {
        this.value = value;
    }

    public boolean isTrue() {
        return BooleanUtils.isTrue(getValue());
    }

    public boolean isNotTrue() {
        return BooleanUtils.isNotTrue(getValue());
    }

    public boolean isFalse() {
        return BooleanUtils.isFalse(getValue());
    }

    public boolean isNotFalse() {
        return BooleanUtils.isNotFalse(getValue());
    }

    @Override
    public WebStorageEntry clone() {
        final BooleanWebStorageEntry clone = new BooleanWebStorageEntry();
        clone.setValue(getValue());
        return clone;
    }
}
