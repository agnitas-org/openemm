/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.export.util;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.agnitas.beans.ExportColumnMapping;
import org.agnitas.beans.ExportPredef;

import com.agnitas.beans.Admin;
import com.agnitas.beans.ProfileField;
import com.agnitas.service.ColumnInfoService;

public final class ExportUtils {
    public static List<ExportColumnMapping> getCustomColumnMappingsFromExport(ExportPredef export, int companyId, Admin admin, ColumnInfoService columnInfoService) throws Exception {
    	List<ProfileField> profileFields;
    	if (admin != null) {
    		profileFields = columnInfoService.getComColumnInfos(companyId, admin.getAdminID());
    	} else {
    		profileFields = columnInfoService.getComColumnInfos(companyId);
    	}
    	
        Set<String> profileFieldNames = profileFields
			.stream()
			.map(profileField -> profileField.getColumn().toLowerCase())
			.collect(Collectors.toSet());
        profileFieldNames.add("mailing_bounce");
        return export.getExportColumnMappings().stream()
                .filter(mapping -> !profileFieldNames.contains(mapping.getDbColumn().toLowerCase()))
                .collect(Collectors.toList());
    }

    public static List<ExportColumnMapping> getProfileFieldColumnsFromExport(ExportPredef export, int companyId, Admin admin, ColumnInfoService columnInfoService) throws Exception {
        Set<String> customColumns = getCustomColumnMappingsFromExport(export, companyId, admin, columnInfoService)
                .stream().map(ExportColumnMapping::getDbColumn).collect(Collectors.toSet());
        return export.getExportColumnMappings().stream()
                .filter(column -> !customColumns.contains(column.getDbColumn())).collect(Collectors.toList());
    }
}
