/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.export.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.agnitas.beans.Admin;
import com.agnitas.beans.ProfileField;
import com.agnitas.beans.ProfileFieldMode;
import com.agnitas.emm.core.service.RecipientFieldDescription;
import com.agnitas.emm.core.service.RecipientFieldService;
import com.agnitas.service.ColumnInfoService;
import com.agnitas.beans.ExportColumnMapping;
import com.agnitas.beans.ExportPredef;

public final class ExportUtils {
    public static List<ExportColumnMapping> getCustomColumnMappingsFromExport(ExportPredef export, int companyId, Admin admin, ColumnInfoService columnInfoService) {
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

    public static List<ExportColumnMapping> getCustomColumnMappingsFromExport(ExportPredef export, int companyId, Admin admin, RecipientFieldService recipientFieldService) {
    	List<RecipientFieldDescription> profileFields = new ArrayList<>();
        for (RecipientFieldDescription field : recipientFieldService.getRecipientFields(companyId)) {
			ProfileFieldMode permission;
    		if (admin != null) {
    			permission = field.getAdminPermission(admin.getAdminID());
    		} else {
    			permission = field.getDefaultPermission();
    		}
    		if (permission == ProfileFieldMode.Editable || permission == ProfileFieldMode.ReadOnly) {
    			profileFields.add(field);
			}
        }
    	
        Set<String> profileFieldNames = profileFields
			.stream()
			.map(profileField -> profileField.getColumnName().toLowerCase())
			.collect(Collectors.toSet());
        profileFieldNames.add("mailing_bounce");
        return export.getExportColumnMappings().stream()
            .filter(mapping -> !profileFieldNames.contains(mapping.getDbColumn().toLowerCase()))
            .collect(Collectors.toList());
    }

    public static List<ExportColumnMapping> getProfileFieldColumnsFromExport(ExportPredef export, int companyId, Admin admin, RecipientFieldService recipientFieldService) {
        Set<String> customColumns = getCustomColumnMappingsFromExport(export, companyId, admin, recipientFieldService)
                .stream().map(ExportColumnMapping::getDbColumn).collect(Collectors.toSet());
        return export.getExportColumnMappings().stream()
                .filter(column -> !customColumns.contains(column.getDbColumn())).collect(Collectors.toList());
    }
}
