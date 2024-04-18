/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.import_profile.component;

import static org.agnitas.util.DateUtilities.DD_MM_YYYY;
import static org.agnitas.util.DateUtilities.DD_MM_YYYY_HH_MM;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.agnitas.beans.ColumnMapping;
import org.agnitas.beans.ImportProfile;
import org.agnitas.dao.ImportRecipientsDao;
import org.agnitas.service.ImportProfileService;
import org.agnitas.util.DbColumnType;
import org.agnitas.util.DbUtilities;
import org.agnitas.util.importvalues.ImportMode;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.service.RecipientFieldDescription;
import com.agnitas.emm.core.service.RecipientFieldService;
import com.agnitas.emm.core.service.RecipientFieldService.RecipientStandardField;
import com.agnitas.web.mvc.Popups;

@Component
public class ImportProfileColumnMappingsValidator {

    private final ImportRecipientsDao importRecipientsDao;
    private final RecipientFieldService recipientFieldService;
    private final ImportProfileService importProfileService;

    @Autowired
    public ImportProfileColumnMappingsValidator(ImportRecipientsDao importRecipientsDao, RecipientFieldService recipientFieldService, ImportProfileService importProfileService) {
        this.importRecipientsDao = importRecipientsDao;
        this.recipientFieldService = recipientFieldService;
        this.importProfileService = importProfileService;
    }

    public boolean validate(List<ColumnMapping> mappings, ImportProfile profile, Admin admin, Popups popups) throws Exception {
        if (existsDuplicatedMappings(mappings, popups)) {
            return false;
        }

        if (existsNotAllowedDefaultValue(mappings, admin, popups)) {
            return false;
        }

        if (!existsRequiredMappings(mappings, profile, popups)) {
            return false;
        }

        if (existsForbiddenMappings(mappings, profile, admin, popups)) {
            return false;
        }

        int importMode = profile.getImportMode();
        if (importMode == ImportMode.ADD.getIntValue() || importMode == ImportMode.ADD_AND_UPDATE.getIntValue() || importMode == ImportMode.UPDATE.getIntValue()) {
            if (!existsKeyColumn(profile.getKeyColumns(), mappings, popups)) {
                return false;
            }
        }

        return true;
    }

    private boolean existsDuplicatedMappings(List<ColumnMapping> mappings, Popups popups) {
        Set<String> dbColumns = new HashSet<>();
        Set<String> fileColumns = new HashSet<>();

        for (ColumnMapping mapping : mappings) {
            if (!ColumnMapping.DO_NOT_IMPORT.equals(mapping.getDatabaseColumn())) {
                if (dbColumns.contains(mapping.getDatabaseColumn()) || fileColumns.contains(mapping.getDatabaseColumn())) {
                    popups.alert("error.import.column.dbduplicate");
                    return true;
                }
                dbColumns.add(mapping.getDatabaseColumn());
                fileColumns.add(mapping.getFileColumn());
            }
        }

        return false;
    }

    private boolean existsNotAllowedDefaultValue(List<ColumnMapping> mappings, Admin admin, Popups popups) throws Exception {
        for (ColumnMapping mapping : mappings) {
            String dbColumnName = mapping.getDatabaseColumn();
            if (isStringEscaped(mapping.getDefaultValue()) && !ColumnMapping.DO_NOT_IMPORT.equalsIgnoreCase(dbColumnName)) {
                String defaultValue = unescapeString(mapping.getDefaultValue());

                RecipientFieldDescription recipientFieldDescription = recipientFieldService.getRecipientField(admin.getCompanyID(), dbColumnName);
                String dateFormat = DbColumnType.SimpleDataType.DateTime.equals(recipientFieldDescription.getSimpleDataType()) ? DD_MM_YYYY_HH_MM : DD_MM_YYYY;
                if (!DbUtilities.checkAllowedDefaultValue(recipientFieldDescription.getDatabaseDataType(), defaultValue, new SimpleDateFormat(dateFormat))) {
                    popups.alert("error.import.invalidDataForField", dbColumnName);
                    return true;
                }
            }
        }

        return false;
    }

    private boolean existsForbiddenMappings(List<ColumnMapping> mappings, ImportProfile profile, Admin admin, Popups popups) {
        for (String hiddenColumn : RecipientStandardField.getImportChangeNotAllowedColumns(admin.permissionAllowed(Permission.IMPORT_CUSTOMERID))) {
            for (ColumnMapping mapping : mappings) {
                if (mapping.getDatabaseColumn().equalsIgnoreCase(hiddenColumn)) {
                    if (RecipientStandardField.CustomerID.getColumnName().equalsIgnoreCase(hiddenColumn) && profile.getId() > 0) {
                        // if customer_id was configured in the mapping by some other user, it is allowed to keep it now
                        ColumnMapping existingCustomerIdMapping = importProfileService.findColumnMappingByDbColumn(RecipientStandardField.CustomerID.getColumnName(), profile.getColumnMapping());

                        if (existingCustomerIdMapping == null || !existingCustomerIdMapping.getFileColumn().equals(mapping.getFileColumn())) {
                            popups.alert("error.import.column.invalid", hiddenColumn);
                            return true;
                        }
                    } else {
                        popups.alert("error.import.column.invalid", hiddenColumn);
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private boolean existsKeyColumn(List<String> keyColumns, List<ColumnMapping> mappings, Popups popups) {
        for (String keyColumn : keyColumns) {
            boolean keyColumnExists = mappings.stream()
                    .anyMatch(m -> m.getDatabaseColumn().equalsIgnoreCase(keyColumn));

            if (!keyColumnExists) {
                popups.alert("error.import.keycolumn_missing", keyColumn);
                return false;
            }
        }

        return true;
    }

    private boolean existsRequiredMappings(List<ColumnMapping> mappings, ImportProfile profile, Popups popups) throws Exception {
        if (!isImportModeHasAdding(profile.getImportMode())) {
            return true;
        }

        String columnWithMissingMapping = findNotNullableColumnWithMissingMapping(profile, mappings);

        if (columnWithMissingMapping != null) {
            popups.alert("error.import.missingNotNullableColumnInMapping", columnWithMissingMapping);
            return false;
        }

        for (ColumnMapping mapping : mappings) {
            if (RecipientStandardField.Gender.getColumnName().equalsIgnoreCase(mapping.getDatabaseColumn())) {
                if (StringUtils.isBlank(mapping.getFileColumn()) && (StringUtils.isBlank(mapping.getDefaultValue()) || mapping.getDefaultValue().trim().equals("''") || mapping.getDefaultValue().trim().equalsIgnoreCase("null"))) {
                    popups.alert("error.import.missingNotNullableColumnInMapping", RecipientStandardField.Gender.getColumnName());
                    return false;
                }
            } else if (RecipientStandardField.Mailtype.getColumnName().equalsIgnoreCase(mapping.getDatabaseColumn())) {
                if (StringUtils.isBlank(mapping.getFileColumn()) && (StringUtils.isBlank(mapping.getDefaultValue()) || mapping.getDefaultValue().trim().equals("''") || mapping.getDefaultValue().trim().equalsIgnoreCase("null"))) {
                    popups.alert("error.import.missingNotNullableColumnInMapping", RecipientStandardField.Mailtype.getColumnName());
                    return false;
                }
            }
        }

        return true;
    }

    private String findNotNullableColumnWithMissingMapping(ImportProfile profile, List<ColumnMapping> mappings) throws Exception {
        List<String> notNullableCustomerColumns = getNotNullableCustomerColumns(profile.getCompanyId());

        for (String column : notNullableCustomerColumns) {
            ColumnMapping relatedMapping = importProfileService.findColumnMappingByDbColumn(column, mappings);

            if (relatedMapping == null || (relatedMapping.getFileColumn() == null && relatedMapping.getDefaultValue() == null)) {
                return column;
            }
        }

        return null;
    }

    private List<String> getNotNullableCustomerColumns(int companyId) throws Exception {
        List<String> columns = new ArrayList<>();
        CaseInsensitiveMap<String, DbColumnType> customerDbFields = importRecipientsDao.getCustomerDbFields(companyId);

        for (Map.Entry<String, DbColumnType> columnEntry : customerDbFields.entrySet()) {
            if (isNotNullableColumn(columnEntry, companyId)) {
                columns.add(columnEntry.getKey());
            }
        }

        return columns;
    }

    private boolean isNotNullableColumn(Map.Entry<String, DbColumnType> columnEntry, int companyId) throws Exception {
        return !columnEntry.getValue().isNullable()
                && DbUtilities.getColumnDefaultValue(importRecipientsDao.getDataSource(), "customer_" + companyId + "_tbl", columnEntry.getKey()) == null
                && !RecipientStandardField.CustomerID.getColumnName().equalsIgnoreCase(columnEntry.getKey())
                && !RecipientStandardField.Gender.getColumnName().equalsIgnoreCase(columnEntry.getKey())
                && !RecipientStandardField.Mailtype.getColumnName().equalsIgnoreCase(columnEntry.getKey())
                && !RecipientStandardField.Bounceload.getColumnName().equalsIgnoreCase(columnEntry.getKey());
    }

    private boolean isImportModeHasAdding(int importMode) {
        return importMode == ImportMode.ADD.getIntValue()
                || importMode == ImportMode.ADD_AND_UPDATE.getIntValue()
                || importMode == ImportMode.ADD_AND_UPDATE_FORCED.getIntValue();
    }

    private String unescapeString(String str) {
        return str.substring(1, str.length() - 1);
    }

    private boolean isStringEscaped(String str) {
        if (StringUtils.isEmpty(str)) {
            return false;
        }

        return str.startsWith("'") && str.endsWith("'");
    }
}
