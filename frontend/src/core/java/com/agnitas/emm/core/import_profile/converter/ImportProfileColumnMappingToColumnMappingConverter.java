/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.import_profile.converter;

import com.agnitas.emm.core.import_profile.bean.ImportProfileColumnMapping;
import com.agnitas.beans.ColumnMapping;
import com.agnitas.beans.impl.ColumnMappingImpl;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class ImportProfileColumnMappingToColumnMappingConverter implements Converter<ImportProfileColumnMapping, ColumnMapping> {

    @Override
    public ColumnMapping convert(ImportProfileColumnMapping source) {
        ColumnMapping columnMapping = new ColumnMappingImpl();

        columnMapping.setId(source.getId());
        columnMapping.setFileColumn(source.getFileColumn());
        columnMapping.setDatabaseColumn(source.getDatabaseColumn());
        columnMapping.setMandatory(source.isMandatory());
        columnMapping.setDefaultValue(source.getDefaultValue());

        return columnMapping;
    }

}
