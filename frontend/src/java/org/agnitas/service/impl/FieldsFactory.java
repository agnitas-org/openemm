/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.service.impl;

import org.agnitas.beans.ColumnMapping;
import org.agnitas.beans.ImportProfile;

public class FieldsFactory {
    public String getDBColumnNameByCsvFileName(String headerName, ImportProfile profile) {
        if (headerName == null) {
            return null;
        }

        for (ColumnMapping columnMapping : profile.getColumnMapping()) {
            if (headerName.equals(columnMapping.getFileColumn()) && !columnMapping.getDatabaseColumn().equals(ColumnMapping.DO_NOT_IMPORT)) {
                return columnMapping.getDatabaseColumn();
            }
        }

        return null;
    }
}
