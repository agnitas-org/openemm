/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.db_schema.service;

import java.io.File;

import com.agnitas.emm.core.db_schema.bean.DbSchemaCheckResult;
import com.agnitas.emm.core.db_schema.bean.DbSchemaSnapshot;
import org.springframework.web.multipart.MultipartFile;

public interface DbSchemaSnapshotService {

    String generateFileName();

    File create();

    boolean exists();

    DbSchemaCheckResult check();

    DbSchemaSnapshot read(MultipartFile file);

    DbSchemaSnapshot read(File file);

    boolean exists(String versionNumber);

    void save(DbSchemaSnapshot snapshot);

    String getVendorName();

    File createDiffFile();

}
