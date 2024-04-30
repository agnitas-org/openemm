/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.import_profile.component.parser;

import com.agnitas.dao.ComRecipientDao;
import com.agnitas.emm.core.import_profile.component.RecipientImportFileInputStreamProvider;
import com.agnitas.messages.Message;
import com.agnitas.service.ServiceResult;
import org.agnitas.beans.ColumnMapping;
import org.agnitas.beans.ImportProfile;
import org.agnitas.dao.ImportRecipientsDao;
import org.agnitas.service.impl.CSVColumnState;
import org.agnitas.util.CsvColInfo;
import org.agnitas.util.CsvDataException;
import org.agnitas.util.CsvDataInvalidItemCountException;
import org.agnitas.util.CsvDataInvalidTextAfterQuoteException;
import org.agnitas.util.CsvReader;
import org.agnitas.util.DbColumnType;
import org.agnitas.util.importvalues.Charset;
import org.agnitas.util.importvalues.Separator;
import org.agnitas.util.importvalues.TextRecognitionChar;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Component
public class CsvImportFileContentParser extends RecipientImportFileContentParser {

    private final RecipientImportFileInputStreamProvider inputStreamProvider;
    private final ImportRecipientsDao importRecipientsDao;
    private final ComRecipientDao recipientDao;

    public CsvImportFileContentParser(RecipientImportFileInputStreamProvider inputStreamProvider, ImportRecipientsDao importRecipientsDao, ComRecipientDao recipientDao) {
        this.inputStreamProvider = inputStreamProvider;
        this.importRecipientsDao = importRecipientsDao;
        this.recipientDao = recipientDao;
    }

    @Override
    public ServiceResult<List<List<String>>> parse(File importFile, ImportProfile profile) {
        List<List<String>> previewParsedContent = new LinkedList<>();
        int lineNumber = 0;
        CSVColumnState[] columns = null;

        try (CsvReader csvReader = newReader(importFile, profile)) {
            while (lineNumber <= 20) {
                List<String> csvLineData = csvReader.readNextCsvLine();

                if (csvLineData == null) {
                    break;
                }

                lineNumber++;
                // If we haven't been sent the header data yet then we store
                // them (but don't process them)
                if (columns == null) {
                    if (!profile.isNoHeaders()) {
                        columns = new CSVColumnState[csvLineData.size()];
                        if (profile.isAutoMapping()) {
                            CaseInsensitiveMap<String, DbColumnType> customerDbFields = importRecipientsDao.getCustomerDbFields(profile.getCompanyId());
                            for (int i = 0; i < csvLineData.size(); i++) {
                                String headerName = csvLineData.get(i);
                                if (StringUtils.isBlank(headerName)) {
                                    return ServiceResult.error(Message.of("Invalid empty csvfile header for import automapping"));
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
                            for (int i = 0; i < csvLineData.size(); i++) {
                                String headerName = csvLineData.get(i);
                                final String columnNameByCvsFileName = getDBColumnNameByCsvFileName(headerName, profile);
                                if (columnNameByCvsFileName != null) {
                                    columns[i] = new CSVColumnState();
                                    columns[i].setColName(columnNameByCvsFileName);
                                    columns[i].setImportedColumn(true);
                                } else {
                                    columns[i] = new CSVColumnState();
                                    columns[i].setColName(headerName);
                                    columns[i].setImportedColumn(false);
                                }
                            }
                        }
                    } else {
                        int csvColumnsExpected = 0;
                        for (ColumnMapping columnMapping : profile.getColumnMapping()) {
                            if (StringUtils.isNotBlank(columnMapping.getFileColumn())) {
                                if (!columnMapping.getFileColumn().startsWith("column_")) {
                                    return ServiceResult.error(Message.of("error.import.mapping.column.invalid", columnMapping.getFileColumn()));
                                } else {
                                    int columnId;
                                    try {
                                        columnId = Integer.parseInt(columnMapping.getFileColumn().substring(7));
                                    } catch (Exception e) {
                                        return ServiceResult.error(Message.of("error.import.mapping.column.invalid", columnMapping.getFileColumn()));
                                    }
                                    csvColumnsExpected = Math.max(csvColumnsExpected, columnId);
                                }
                            }
                        }

                        if (csvLineData.size() != csvColumnsExpected) {
                            throw new CsvDataException("Number of import file columns does not fit mapped columns", csvReader.getReadCsvLines());
                        }
                        columns = new CSVColumnState[Math.min(csvLineData.size(), profile.getColumnMapping().size())];

                        for (int i = 0; i < columns.length; i++) {
                            ColumnMapping columnMapping = profile.getColumnMapping().get(i);
                            columns[i] = new CSVColumnState();
                            columns[i].setColName(columnMapping.getFileColumn());
                            if (columnMapping.getDatabaseColumn() != null && !columnMapping.getDatabaseColumn().equals(ColumnMapping.DO_NOT_IMPORT)) {
                                columns[i].setImportedColumn(true);
                            } else {
                                columns[i].setImportedColumn(false);
                            }
                        }

                        // Add dummy column names to preview data
                        final LinkedList<String> columnsList = new LinkedList<>();
                        for (int idx = 0; (idx < columns.length) && (idx < csvLineData.size()); idx++) {
                            if (!columns[idx].getImportedColumn()) {
                                continue;
                            }
                            columnsList.add(columns[idx].getColName());
                        }
                        previewParsedContent.add(columnsList);
                    }
                    initColumnsNullableCheck(columns, profile);
                }

                final LinkedList<String> linelinkedList = new LinkedList<>();
                for (int idx = 0; (idx < columns.length) && (idx < csvLineData.size()); idx++) {
                    if (!columns[idx].getImportedColumn()) {
                        continue;
                    }
                    String value = csvLineData.get(idx);

                    linelinkedList.add(value);
                }
                previewParsedContent.add(linelinkedList);
            }

            return ServiceResult.success(previewParsedContent);
        } catch (CsvDataInvalidItemCountException e) {
            return ServiceResult.error(Message.of("error.import.data.itemcount", e.getExpected(), e.getActual(), e.getErrorLineNumber()));
        } catch (CsvDataInvalidTextAfterQuoteException e) {
            return ServiceResult.error(Message.of("error.import.data.invalidTextAfterQuote", e.getErrorLineNumber()));
        } catch (CsvDataException e) {
            return ServiceResult.error(Message.of("import.csv_errors_linestructure", e.getMessage() + " in line " + e.getErrorLineNumber()));
        } catch (Exception e) {
            return ServiceResult.error(Message.of("import.csv_errors_linestructure", lineNumber));
        }
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

    private CsvReader newReader(File importFile, ImportProfile profile) throws Exception {
        InputStream inputStream = inputStreamProvider.provide(importFile, profile);
        char separator = Separator.getSeparatorById(profile.getSeparator())
                .getValueChar();

        Character stringQuote = TextRecognitionChar.getTextRecognitionCharById(profile.getTextRecognitionChar())
                .getValueCharacter();

        String charsetName = Charset.getCharsetById(profile.getCharset())
                .getCharsetName();

        CsvReader csvReader = new CsvReader(inputStream, charsetName, separator, stringQuote);
        csvReader.setAlwaysTrim(true);

        return csvReader;
    }
}
