/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.db_schema.bean;

import java.util.List;
import java.util.Map;

public class DbSchemaCheckResult {

    private final List<String> obsoleteTables;
    private final List<String> missingTables;
    private final Map<String, List<String>> missingColumns;
    private final Map<String, List<String>> columnsWithMismatchedTypes;
    private final Map<String, List<String>> columnsWithMismatchedLength;

    public DbSchemaCheckResult(List<String> obsoleteTables, List<String> missingTables, Map<String, List<String>> missingColumns,
                               Map<String, List<String>> columnsWithMismatchedTypes, Map<String, List<String>> columnsWithMismatchedLength) {
        this.obsoleteTables = obsoleteTables;
        this.missingTables = missingTables;
        this.missingColumns = missingColumns;
        this.columnsWithMismatchedTypes = columnsWithMismatchedTypes;
        this.columnsWithMismatchedLength = columnsWithMismatchedLength;
    }

    public boolean isSuccessful() {
        return obsoleteTables.isEmpty() && missingTables.isEmpty() && missingColumns.isEmpty()
                && columnsWithMismatchedTypes.isEmpty() && columnsWithMismatchedLength.isEmpty();
    }

    public List<String> getObsoleteTables() {
        return obsoleteTables;
    }

    public List<String> getMissingTables() {
        return missingTables;
    }

    public Map<String, List<String>> getMissingColumns() {
        return missingColumns;
    }

    public Map<String, List<String>> getColumnsWithMismatchedTypes() {
        return columnsWithMismatchedTypes;
    }

    public Map<String, List<String>> getColumnsWithMismatchedLength() {
        return columnsWithMismatchedLength;
    }
}
