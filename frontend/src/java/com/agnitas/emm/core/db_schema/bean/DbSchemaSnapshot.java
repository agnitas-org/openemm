/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.db_schema.bean;

import java.util.List;
import java.util.Objects;

public class DbSchemaSnapshot {

    private String versionNumber;
    private List<DbTableInfo> tables;

    @SuppressWarnings("unused") // used by ObjectMapper
    public DbSchemaSnapshot() {
    }

    public DbSchemaSnapshot(String versionNumber, List<DbTableInfo> tables) {
        this.versionNumber = versionNumber;
        this.tables = tables;
    }

    public String getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(String versionNumber) {
        this.versionNumber = versionNumber;
    }

    public List<DbTableInfo> getTables() {
        return tables;
    }

    public void setTables(List<DbTableInfo> tables) {
        this.tables = tables;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DbSchemaSnapshot snapshot = (DbSchemaSnapshot) o;
        return Objects.equals(versionNumber, snapshot.versionNumber) && Objects.equals(tables, snapshot.tables);
    }

    @Override
    public int hashCode() {
        return Objects.hash(versionNumber, tables);
    }
}
