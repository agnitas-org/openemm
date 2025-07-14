/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.imports.service.impl;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.agnitas.dao.RecipientDao;
import com.agnitas.emm.core.import_profile.bean.ImportDataType;
import com.agnitas.emm.core.imports.service.RecipientImportService;
import com.agnitas.emm.data.CsvDataProvider;
import com.agnitas.emm.data.DataProvider;
import com.agnitas.emm.data.ExcelDataProvider;
import com.agnitas.emm.data.JsonDataProvider;
import com.agnitas.emm.data.OdsDataProvider;
import com.agnitas.messages.Message;
import com.agnitas.service.ServiceResult;
import com.agnitas.beans.ColumnMapping;
import com.agnitas.beans.ImportProfile;
import com.agnitas.dao.ImportRecipientsDao;
import com.agnitas.service.impl.CSVColumnState;
import com.agnitas.util.CsvColInfo;
import com.agnitas.util.CsvDataBreakInsideCellException;
import com.agnitas.util.CsvDataException;
import com.agnitas.util.DbColumnType;
import com.agnitas.util.importvalues.Charset;
import com.agnitas.util.importvalues.DateFormat;
import com.agnitas.util.importvalues.Separator;
import com.agnitas.util.importvalues.TextRecognitionChar;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class RecipientImportServiceImpl implements RecipientImportService {

    private final ImportRecipientsDao importRecipientsDao;
    private final RecipientDao recipientDao;

    public RecipientImportServiceImpl(ImportRecipientsDao importRecipientsDao, RecipientDao recipientDao) {
        this.importRecipientsDao = importRecipientsDao;
        this.recipientDao = recipientDao;
    }

    @Override
    public ServiceResult<List<List<String>>> parseFileContent(File importFile, ImportProfile profile) throws Exception {
        DataProvider dataProvider = getDataProvider(profile, importFile);
        List<String> dataPropertyNames = dataProvider.getAvailableDataPropertyNames();
        List<List<String>> previewParsedContent = new LinkedList<>();

        CSVColumnState[] columns;
        if (!profile.isNoHeaders()) {
            columns = new CSVColumnState[dataPropertyNames.size()];
            if (profile.isAutoMapping()) {
                CaseInsensitiveMap<String, DbColumnType> customerDbFields = importRecipientsDao.getCustomerDbFields(profile.getCompanyId());
                for (int i = 0; i < dataPropertyNames.size(); i++) {
                    String headerName = dataPropertyNames.get(i);
                    if (StringUtils.isBlank(headerName)) {
                        return ServiceResult.error(Message.of("Invalid empty import data file header for import automapping"));
                    } else if (customerDbFields.containsKey(headerName)) {
                        columns[i] = new CSVColumnState();
                        columns[i].setColName(headerName.toLowerCase());
                        columns[i].setImportedColumn(true);
                    } else {
                        columns[i] = new CSVColumnState();
                        columns[i].setColName(headerName);
                        columns[i].setImportedColumn(false);
                    }
                }
            } else {
                for (int i = 0; i < dataPropertyNames.size(); i++) {
                    String propertyName = dataPropertyNames.get(i);
                    final String columnNameByCvsFileName = getDBColumnNameByImportFilePropertyName(propertyName, profile);
                    if (columnNameByCvsFileName != null) {
                        columns[i] = new CSVColumnState();
                        columns[i].setColName(columnNameByCvsFileName);
                        columns[i].setImportedColumn(true);
                    } else {
                        columns[i] = new CSVColumnState();
                        columns[i].setColName(propertyName);
                        columns[i].setImportedColumn(false);
                    }
                }
            }
        } else {
            int propertyNamesExpected = 0;
            for (ColumnMapping columnMapping : profile.getColumnMapping()) {
                if (StringUtils.isNotBlank(columnMapping.getFileColumn())) {
                    if (!columnMapping.getFileColumn().startsWith("column_")) {
                        return ServiceResult.error(Message.of("error.import.mapping.column.invalid", columnMapping.getFileColumn()));
                    } else {
                        int columnId;
                        try {
                            columnId = Integer.parseInt(columnMapping.getFileColumn().substring(7));
                        } catch (@SuppressWarnings("unused") Exception e) {
                            return ServiceResult.error(Message.of("error.import.mapping.column.invalid", columnMapping.getFileColumn()));
                        }
                        propertyNamesExpected = Math.max(propertyNamesExpected, columnId);
                    }
                }
            }

            if (dataPropertyNames.size() != propertyNamesExpected) {
                throw new CsvDataException("Number of import file columns does not fit mapped columns", propertyNamesExpected);
            }
            columns = new CSVColumnState[Math.min(dataPropertyNames.size(), profile.getColumnMapping().size())];

            for (int i = 0; i < columns.length; i++) {
                ColumnMapping columnMapping = profile.getColumnMapping().get(i);
                columns[i] = new CSVColumnState();
                columns[i].setColName(columnMapping.getFileColumn());
                columns[i].setImportedColumn(columnMapping.getDatabaseColumn() != null && !columnMapping.getDatabaseColumn().equals(ColumnMapping.DO_NOT_IMPORT));
            }

            // Add dummy column names to preview data
            final LinkedList<String> columnsList = new LinkedList<>();
            for (int idx = 0; (idx < columns.length) && (idx < dataPropertyNames.size()); idx++) {
                if (!columns[idx].getImportedColumn()) {
                    continue;
                }
                columnsList.add(columns[idx].getColName());
            }
            previewParsedContent.add(columnsList);
        }
        initColumnsNullableCheck(columns, profile);

        for (int dataItemIndex = 0; dataItemIndex < 20; dataItemIndex++) {
            Map<String, Object> dataItem = null;
            try {
                dataItem = dataProvider.getNextItemData();
            } catch (CsvDataBreakInsideCellException e) {
                return ServiceResult.error(Message.of("error.import.file.linebreak", e.getCellIndex(), e.getErrorLineNumber()));
            }
            if (dataItem == null) {
                break;
            } else {
                final List<String> dataItemValuesForImport = new ArrayList<>();
                for (int idx = 0; (idx < columns.length) && (idx < dataPropertyNames.size()); idx++) {
                    if (columns[idx].getImportedColumn()) {
                        String propertyName = null;
                        for (ColumnMapping columnMapping : profile.getColumnMapping()) {
                            if (columns[idx].getColName().equals(columnMapping.getDatabaseColumn())) {
                                propertyName = columnMapping.getFileColumn();
                                break;
                            }
                        }
                        Object dataValueObject = dataItem.get(propertyName);
                        String value;
                        if (dataValueObject == null) {
                            value = "";
                        } else if (dataValueObject instanceof Date dateValueObject) {
                            SimpleDateFormat format = new SimpleDateFormat(DateFormat.getDateFormatById(profile.getDateFormat()).getValue());
                            value = format.format(dateValueObject);
                        } else {
                            value = dataValueObject.toString();
                        }

                        dataItemValuesForImport.add(value);
                    }
                }
                previewParsedContent.add(dataItemValuesForImport);
            }
        }

        // Add headers
        final LinkedList<String> headersList = new LinkedList<>();
        for (CSVColumnState column : columns) {
            if (column.getImportedColumn()) {
                headersList.add(column.getColName());
            }
        }
        previewParsedContent.add(0, headersList);

        return ServiceResult.success(previewParsedContent);
    }

    @Override
    public DataProvider getDataProvider(ImportProfile importProfile, File importFile) throws Exception {
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
                throw new IllegalStateException("Invalid import datatype: %s".formatted(importProfile.getDatatype()));
        }
    }

    private String getDBColumnNameByImportFilePropertyName(String headerName, ImportProfile profile) {
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

    private void initColumnsNullableCheck(CSVColumnState[] cols, ImportProfile profile) {
        Map<String, CsvColInfo> columnsInfo = recipientDao.readDBColumns(profile.getCompanyId(), profile.getAdminId(), profile.getKeyColumns());
        for (CSVColumnState columnState : cols) {
            CsvColInfo columnInfo = columnsInfo.get(columnState.getColName());
            if (columnInfo != null) {
                columnState.setNullable(columnInfo.isNullable());
            }
        }
    }
}
