/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.db_schema.bean;

import java.util.Objects;

public class DbColumnInfo {

    private String name;
    private String dataType;
    private Long length;

    @SuppressWarnings("unused") // used by ObjectMapper
    public DbColumnInfo() {
    }

    public DbColumnInfo(String name, String dataType, Long length) {
        this.name = name;
        this.dataType = dataType;
        this.length = length;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getLength() {
        return length;
    }

    public void setLength(Long length) {
        this.length = length;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DbColumnInfo that = (DbColumnInfo) o;
        return Objects.equals(name, that.name) && Objects.equals(dataType, that.dataType) && Objects.equals(length, that.length);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, dataType, length);
    }
}
