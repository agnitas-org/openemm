/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.import_profile.component.reader;

import com.agnitas.beans.Admin;
import com.agnitas.service.ServiceResult;
import org.agnitas.beans.ColumnMapping;
import org.agnitas.beans.ImportProfile;
import org.agnitas.beans.impl.ColumnMappingImpl;
import org.agnitas.util.CsvColInfo;
import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public abstract class ColumnMappingsReader {

    public abstract ServiceResult<List<ColumnMapping>> read(InputStream fileStream, ImportProfile profile, Admin admin) throws Exception;

    protected ColumnMapping createNewColumnMapping(String fileColumn, int profileId, Map<String, CsvColInfo> dbColumns) {
        ColumnMapping mapping = new ColumnMappingImpl();

        mapping.setProfileId(profileId);
        mapping.setFileColumn(fileColumn);

        mapping.setDatabaseColumn(findDependentDbColumn(fileColumn, dbColumns));

        if (StringUtils.isEmpty(mapping.getDatabaseColumn())) {
            mapping.setDatabaseColumn(ColumnMapping.DO_NOT_IMPORT);
        }

        return mapping;
    }

    private String findDependentDbColumn(String fileColumn, Map<String, CsvColInfo> dbColumns) {
        String columnValue = removeNameSeparators(fileColumn);

        return dbColumns.keySet().stream()
                .map(this::removeNameSeparators)
                .filter(columnValue::equalsIgnoreCase)
                .findAny()
                .orElse(null);
    }

    private String removeNameSeparators(String columnName) {
        return columnName.replace("-", "").replace("_", "");
    }
}
