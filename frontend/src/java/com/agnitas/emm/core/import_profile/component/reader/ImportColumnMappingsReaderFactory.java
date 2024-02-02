/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.import_profile.component.reader;

import com.agnitas.emm.core.import_profile.bean.ImportDataType;
import org.agnitas.beans.ImportProfile;
import org.springframework.stereotype.Component;

@Component
public class ImportColumnMappingsReaderFactory {

    private final ColumnMappingsJsonReader columnMappingsJsonReader;
    private final ColumnMappingsCsvReader columnMappingsCsvReader;

    public ImportColumnMappingsReaderFactory(ColumnMappingsJsonReader columnMappingsJsonReader, ColumnMappingsCsvReader columnMappingsCsvReader) {
        this.columnMappingsJsonReader = columnMappingsJsonReader;
        this.columnMappingsCsvReader = columnMappingsCsvReader;
    }

    public ColumnMappingsReader detectReader(ImportProfile profile) throws Exception {
        ImportDataType dataType = ImportDataType.getImportDataTypeForName(profile.getDatatype());

        if (ImportDataType.CSV.equals(dataType)) {
            return columnMappingsCsvReader;
        }

        if (ImportDataType.JSON.equals(dataType)) {
            return columnMappingsJsonReader;
        }

        throw new Exception("Invalid datatype: " + profile.getDatatype());
    }
}
