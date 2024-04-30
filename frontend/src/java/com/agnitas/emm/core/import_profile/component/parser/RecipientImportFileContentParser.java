/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.import_profile.component.parser;

import com.agnitas.service.ServiceResult;
import org.agnitas.beans.ColumnMapping;
import org.agnitas.beans.ImportProfile;

import java.io.File;
import java.util.List;

public abstract class RecipientImportFileContentParser {

    public abstract ServiceResult<List<List<String>>> parse(File importFile, ImportProfile profile);

    protected String getDBColumnNameByCsvFileName(String headerName, ImportProfile profile) {
        if (headerName == null) {
            return null;
        }

        for (ColumnMapping mapping : profile.getColumnMapping()) {
            if (headerName.equals(mapping.getFileColumn()) && !mapping.getDatabaseColumn().equals(ColumnMapping.DO_NOT_IMPORT)) {
                return mapping.getDatabaseColumn();
            }
        }

        return null;
    }
}
