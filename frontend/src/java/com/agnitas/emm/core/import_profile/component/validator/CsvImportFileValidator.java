/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.import_profile.component.validator;

import com.agnitas.emm.core.import_profile.component.RecipientImportFileInputStreamProvider;
import com.agnitas.messages.Message;
import com.agnitas.service.SimpleServiceResult;
import org.agnitas.beans.ColumnMapping;
import org.agnitas.beans.ImportProfile;
import org.agnitas.util.CaseInsensitiveSet;
import org.agnitas.util.CsvReader;
import org.agnitas.util.importvalues.Charset;
import org.agnitas.util.importvalues.Separator;
import org.agnitas.util.importvalues.TextRecognitionChar;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
public class CsvImportFileValidator extends RecipientImportFileValidator {

    private final RecipientImportFileInputStreamProvider inputStreamProvider;

    public CsvImportFileValidator(RecipientImportFileInputStreamProvider inputStreamProvider) {
        this.inputStreamProvider = inputStreamProvider;
    }

    @Override
    public SimpleServiceResult executeValidation(ImportProfile profile, File importFile) throws Exception {
        try (CsvReader csvReader = newReader(importFile, profile)) {
            List<String> fileHeaders;
            if (!profile.isNoHeaders()) {
                fileHeaders = csvReader.readNextCsvLine();

                SimpleServiceResult validationResult = validateReadColumns(fileHeaders);
                if (!validationResult.isSuccess()) {
                    return validationResult;
                }
            } else {
                List<String> firstDataLine = csvReader.readNextCsvLine();
                fileHeaders = new ArrayList<>();
                for (ColumnMapping mapping : profile.getColumnMapping()) {
                    if (!mapping.getDatabaseColumn().equals(ColumnMapping.DO_NOT_IMPORT)) {
                        SimpleServiceResult validationResult = validateMappingColumn(mapping, firstDataLine);
                        if (!validationResult.isSuccess()) {
                            return validationResult;
                        }

                        fileHeaders.add(mapping.getFileColumn());
                    }
                }
            }

            Optional<String> missingColumn = findMissingRequiredFileColumn(fileHeaders, profile.getColumnMapping());
            if (missingColumn.isPresent()) {
                return SimpleServiceResult.simpleError(Message.of("error.import.no_keycolumn_mapping_found_in_file", missingColumn.get()));
            }

            return SimpleServiceResult.simpleSuccess();
        }
    }

    private SimpleServiceResult validateMappingColumn(ColumnMapping mapping, List<String> fileColumns) {
        if (!mapping.getFileColumn().startsWith("column_")) {
            return SimpleServiceResult.simpleError(Message.of("error.import.mapping.column.invalid", mapping.getFileColumn()));
        }

        int columnId;
        try {
            columnId = Integer.parseInt(mapping.getFileColumn().substring(7));
        } catch (Exception e) {
            return SimpleServiceResult.simpleError(Message.of("error.import.mapping.column.invalid", mapping.getFileColumn()));
        }

        if (columnId > fileColumns.size()) {
            return SimpleServiceResult.simpleError(Message.of("error.import.mapping.column.invalid", mapping.getFileColumn()));
        }

        return SimpleServiceResult.simpleSuccess();
    }

    private SimpleServiceResult validateReadColumns(List<String> columns) {
        Set<String> processedFileHeaders = new CaseInsensitiveSet();
        for (String csvColumns : columns) {
            if (StringUtils.isBlank(csvColumns)) {
                return SimpleServiceResult.simpleError(Message.of("error.import.column.name.empty"));
            }

            if (processedFileHeaders.contains(csvColumns)) {
                return SimpleServiceResult.simpleError(Message.of("error.import.column.csv.duplicate"));
            }
            processedFileHeaders.add(csvColumns);
        }

        return SimpleServiceResult.simpleSuccess();
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
