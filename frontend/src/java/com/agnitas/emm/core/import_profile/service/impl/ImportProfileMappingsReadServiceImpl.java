/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.import_profile.service.impl;

import static java.text.MessageFormat.format;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.agnitas.beans.ColumnMapping;
import org.agnitas.beans.ImportProfile;
import org.agnitas.beans.impl.ColumnMappingImpl;
import org.agnitas.util.CaseInsensitiveSet;
import org.agnitas.util.CsvColInfo;
import org.agnitas.util.CsvDataInvalidItemCountException;
import org.agnitas.util.ImportUtils;
import org.agnitas.util.importvalues.Charset;
import org.agnitas.util.importvalues.Separator;
import org.agnitas.util.importvalues.TextRecognitionChar;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import com.agnitas.beans.Admin;
import com.agnitas.dao.ComRecipientDao;
import com.agnitas.emm.core.import_profile.bean.ImportDataType;
import com.agnitas.emm.core.import_profile.service.ImportProfileMappingsReadService;
import com.agnitas.emm.data.CsvDataProvider;
import com.agnitas.emm.data.DataProvider;
import com.agnitas.emm.data.ExcelDataProvider;
import com.agnitas.emm.data.JsonDataProvider;
import com.agnitas.emm.data.OdsDataProvider;
import com.agnitas.messages.Message;
import com.agnitas.service.ServiceResult;

@Service
public class ImportProfileMappingsReadServiceImpl implements ImportProfileMappingsReadService {

    private static final Logger LOGGER = LogManager.getLogger(ImportProfileMappingsReadServiceImpl.class);

    private final ComRecipientDao recipientDao;

    public ImportProfileMappingsReadServiceImpl(ComRecipientDao recipientDao) {
        this.recipientDao = recipientDao;
    }

    @Override
    public ServiceResult<List<ColumnMapping>> readMappingsFromFile(File file, ImportProfile profile, Admin admin) {
        try {
            if (!ImportUtils.checkIfImportFileHasData(file, profile.getZipPassword())) {
                return ServiceResult.error(Message.of("autoimport.error.emptyFile", file.getName()));
            }

            return performMappingsReading(file, profile, admin);
        } catch (CsvDataInvalidItemCountException e) {
            LOGGER.error(format("Error while mapping import columns: {0}", e.getMessage()), e);
            return ServiceResult.error(Message.of("error.import.data.itemcount", e.getExpected(), e.getActual(), e.getErrorLineNumber()));
        } catch (Exception e) {
            LOGGER.error(format("Error while mapping import columns: {0}", e.getMessage()), e);
            return ServiceResult.error(Message.of("error.import.exception", e.getMessage()));
        }
    }

    private ServiceResult<List<ColumnMapping>> performMappingsReading(File importFile, ImportProfile importProfile, Admin admin) throws Exception {
        DataProvider dataProvider = getDataProvider(importProfile, importFile);
        List<String> dataPropertyNames = dataProvider.getAvailableDataPropertyNames();
        List<Message> errorMessages = new ArrayList<>();

        if (importProfile.isNoHeaders()) {
            dataPropertyNames = IntStream.range(1, dataPropertyNames.size() + 1)
                    .mapToObj(index -> "column_" + index)
                    .collect(Collectors.toList());
        }

        if (dataPropertyNames.contains("")) {
            errorMessages.add(Message.of("error.import.column.name.empty"));
        }

        Set<String> processedColumns = new CaseInsensitiveSet();
		Set<String> duplicateColumns = new CaseInsensitiveSet();
        for (String dataPropertyName : dataPropertyNames) {
            if (StringUtils.isBlank(dataPropertyName)) {
            	errorMessages.add(Message.of("error.import.column.name.empty"));
            	break;
            } else if (processedColumns.contains(dataPropertyName)) {
            	duplicateColumns.add(dataPropertyName);
            } else {
            	processedColumns.add(dataPropertyName);
            }
        }
        
        if (duplicateColumns.size() > 0) {
        	errorMessages.add(Message.of("error.import.column.name.duplicate", StringUtils.join(duplicateColumns, ", ")));
        }

        Map<String, CsvColInfo> dbColumns = recipientDao.readDBColumns(admin.getCompanyID(), admin.getAdminID(), importProfile.getKeyColumns());

        List<ColumnMapping> foundMappings = dataPropertyNames.stream()
                .map(fh -> createNewColumnMapping(fh, importProfile.getId(), dbColumns))
                .collect(Collectors.toList());

        return new ServiceResult<>(
                foundMappings,
                errorMessages.isEmpty(),
                Collections.emptyList(),
                Collections.emptyList(),
                errorMessages
        );
    }

    private DataProvider getDataProvider(ImportProfile importProfile, File importFile) throws Exception {
        switch (ImportDataType.getImportDataTypeForName(importProfile.getDatatype())) {
            case CSV:
                Character valueCharacter = TextRecognitionChar.getTextRecognitionCharById(importProfile.getTextRecognitionChar()).getValueCharacter();
                return new CsvDataProvider(
                        importFile,
                        importProfile.getZipPassword() == null ? null : importProfile.getZipPassword().toCharArray(),
                        Charset.getCharsetById(importProfile.getCharset()).getCharsetName(),
                        Separator.getSeparatorById(importProfile.getSeparator()).getValueChar(),
                        valueCharacter,
                        valueCharacter == null ? '"' : valueCharacter,
                        false,
                        true,
                        importProfile.isNoHeaders(),
                        null);
            case Excel:
                return new ExcelDataProvider(
                        importFile,
                        importProfile.getZipPassword() == null ? null : importProfile.getZipPassword().toCharArray(),
                        true,
                        importProfile.isNoHeaders(),
                        null,
                        true,
                        null);
            case JSON:
                return new JsonDataProvider(
                        importFile,
                        importProfile.getZipPassword() == null ? null : importProfile.getZipPassword().toCharArray(),
                        null,
                        null);
            case ODS:
                return new OdsDataProvider(
                        importFile,
                        importProfile.getZipPassword() == null ? null : importProfile.getZipPassword().toCharArray(),
                        true,
                        importProfile.isNoHeaders(),
                        null,
                        true,
                        null);
            default:
                throw new RuntimeException("Invalid import datatype: " + importProfile.getDatatype());
        }
    }

    private ColumnMapping createNewColumnMapping(String fileColumn, int profileId, Map<String, CsvColInfo> dbColumns) {
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
