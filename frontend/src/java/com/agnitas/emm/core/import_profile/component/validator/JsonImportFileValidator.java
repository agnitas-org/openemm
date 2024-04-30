/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.import_profile.component.validator;

import com.agnitas.emm.core.import_profile.component.RecipientImportFileInputStreamProvider;
import com.agnitas.json.Json5Reader;
import com.agnitas.json.JsonObject;
import com.agnitas.json.JsonReader;
import com.agnitas.messages.Message;
import com.agnitas.service.SimpleServiceResult;
import org.agnitas.beans.ImportProfile;
import org.agnitas.util.CaseInsensitiveSet;
import org.agnitas.util.importvalues.Charset;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.InputStream;
import java.util.Optional;
import java.util.Set;

@Component
public class JsonImportFileValidator extends RecipientImportFileValidator {

    private final RecipientImportFileInputStreamProvider inputStreamProvider;

    public JsonImportFileValidator(RecipientImportFileInputStreamProvider inputStreamProvider) {
        this.inputStreamProvider = inputStreamProvider;
    }

    @Override
    public SimpleServiceResult executeValidation(ImportProfile profile, File importFile) throws Exception {
        try (Json5Reader jsonReader = newReader(importFile, profile)) {
            jsonReader.readNextToken();

            while (jsonReader.getCurrentToken() != null && jsonReader.getCurrentToken() != JsonReader.JsonToken.JsonArray_Open) {
                jsonReader.readNextToken();
            }

            if (jsonReader.getCurrentToken() != JsonReader.JsonToken.JsonArray_Open) {
                return SimpleServiceResult.simpleError(Message.of("import.error.noJsonArray"));
            }

            while (jsonReader.readNextJsonNode()) {
                Object currentObject = jsonReader.getCurrentObject();
                if (!(currentObject instanceof JsonObject)) {
                    throw new Exception("Json data does not contain expected JsonArray of JsonObjects");
                }

                Set<String> columns = ((JsonObject) currentObject).keySet();
                return validateColumns(columns, profile);
            }

            return SimpleServiceResult.simpleSuccess();
        }
    }

    private SimpleServiceResult validateColumns(Set<String> columns, ImportProfile profile) {
        Set<String> processedColumns = new CaseInsensitiveSet();

        for (String jsonPropertyKey : columns) {
            if (StringUtils.isBlank(jsonPropertyKey)) {
                return SimpleServiceResult.simpleError(Message.of("error.import.column.name.empty"));
            }

            if (processedColumns.contains(jsonPropertyKey)) {
                return SimpleServiceResult.simpleError(Message.of("error.import.column.csv.duplicate"));
            }
            processedColumns.add(jsonPropertyKey);
        }

        Optional<String> missingColumn = findMissingRequiredFileColumn(columns, profile.getColumnMapping());
        if (missingColumn.isPresent()) {
            return SimpleServiceResult.simpleError(Message.of("error.import.no_keycolumn_mapping_found_in_file", missingColumn.get()));
        }

        return SimpleServiceResult.simpleSuccess();
    }

    private Json5Reader newReader(File importFile, ImportProfile profile) throws Exception {
        InputStream inputStream = inputStreamProvider.provide(importFile, profile);
        String charsetName = Charset.getCharsetById(profile.getCharset()).getCharsetName();

        return new Json5Reader(inputStream, charsetName);
    }
}
