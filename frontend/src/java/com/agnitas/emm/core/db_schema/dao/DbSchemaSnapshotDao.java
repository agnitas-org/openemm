/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.db_schema.dao;

import java.util.List;

import com.agnitas.emm.core.db_schema.bean.DbColumnInfo;
import com.agnitas.util.Tuple;

public interface DbSchemaSnapshotDao {

    List<String> getTableNames();

    List<DbColumnInfo> getTableColumns(String tableName);

    String getVendorName();

    boolean exists();

    boolean exists(String versionNumber);

    void save(String versionNumber, String schemaJson);

    Tuple<String, String> read();

}
