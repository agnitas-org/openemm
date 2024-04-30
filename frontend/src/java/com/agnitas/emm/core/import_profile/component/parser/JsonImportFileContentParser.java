/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.import_profile.component.parser;

import com.agnitas.emm.core.import_profile.component.RecipientImportFileInputStreamProvider;
import com.agnitas.json.Json5Reader;
import com.agnitas.json.JsonObject;
import com.agnitas.json.JsonReader;
import com.agnitas.messages.Message;
import com.agnitas.service.ServiceResult;
import org.agnitas.beans.ImportProfile;
import org.agnitas.dao.ImportRecipientsDao;
import org.agnitas.service.impl.CSVColumnState;
import org.agnitas.util.DbColumnType;
import org.agnitas.util.importvalues.Charset;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Component
public class JsonImportFileContentParser extends RecipientImportFileContentParser {

    private final RecipientImportFileInputStreamProvider inputStreamProvider;
    private final ImportRecipientsDao importRecipientsDao;

    public JsonImportFileContentParser(RecipientImportFileInputStreamProvider inputStreamProvider, ImportRecipientsDao importRecipientsDao) {
        this.inputStreamProvider = inputStreamProvider;
        this.importRecipientsDao = importRecipientsDao;
    }

    private static class InvalidFileStructureException extends RuntimeException {
        private static final long serialVersionUID = -5513342148190618919L;

		public InvalidFileStructureException(String message) {
            super(message);
        }
    }

    @Override
    public ServiceResult<List<List<String>>> parse(File importFile, ImportProfile profile) {
        List<List<String>> previewParsedContent = new LinkedList<>();
        int lineNumber = 0;

        try (Json5Reader jsonReader = newReader(importFile, profile)) {
            jsonReader.readNextToken();

            while (jsonReader.getCurrentToken() != null && jsonReader.getCurrentToken() != JsonReader.JsonToken.JsonArray_Open) {
                jsonReader.readNextToken();
            }

            if (jsonReader.getCurrentToken() != JsonReader.JsonToken.JsonArray_Open) {
                throw new InvalidFileStructureException("Json data does not contain expected JsonArray");
            }

            List<CSVColumnState> columnsList = new ArrayList<>();

            while (jsonReader.readNextJsonNode() && lineNumber <= 20) {
                lineNumber++;
                Object currentObject = jsonReader.getCurrentObject();
                if (!(currentObject instanceof JsonObject)) {
                    throw new InvalidFileStructureException("Json data does not contain expected JsonArray of JsonObjects");
                }
                JsonObject currentJsonObject = (JsonObject) currentObject;

                // Check if this jsonObject contains a new property
                for (String propertyKey : currentJsonObject.keySet()) {
                    if (StringUtils.isBlank(propertyKey)) {
                        throw new InvalidFileStructureException("Invalid empty jsonfile property key");
                    }

                    CSVColumnState propertyKeyAlreadyListed = null;
                    for (CSVColumnState column : columnsList) {
                        if (column.getColName().equals(propertyKey)) {
                            propertyKeyAlreadyListed = column;
                            break;
                        }
                    }
                    if (propertyKeyAlreadyListed == null) {
                        if (profile.isAutoMapping()) {
                            CaseInsensitiveMap<String, DbColumnType> customerDbFields = importRecipientsDao.getCustomerDbFields(profile.getCompanyId());
                            if (customerDbFields.containsKey(propertyKey)) {
                                columnsList.add(new CSVColumnState(propertyKey, true, -1));
                            } else {
                                columnsList.add(new CSVColumnState(propertyKey, false, -1));
                            }
                        } else {
                            if (getDBColumnNameByCsvFileName(propertyKey, profile) != null) {
                                columnsList.add(new CSVColumnState(propertyKey, true, -1));
                            } else {
                                columnsList.add(new CSVColumnState(propertyKey, false, -1));
                            }
                        }

                        // Fill all other items, which do not contain a value for this new property, with an empty value for it
                        for (List<String> previewParsedContentItem : previewParsedContent) {
                            previewParsedContentItem.add("");
                        }
                    }
                }

                // Collect the property values of this jsonObject
                final List<String> contentListItem = new LinkedList<>();
                for (CSVColumnState column : columnsList) {
                    if (column.getImportedColumn()) {
                        if (currentJsonObject.containsPropertyKey(column.getColName())) {
                            contentListItem.add(currentJsonObject.get(column.getColName()).toString());
                        } else {
                            contentListItem.add("");
                        }
                    }
                }
                previewParsedContent.add(contentListItem);
            }

            // Add headers
            final LinkedList<String> headersList = new LinkedList<>();
            for (CSVColumnState column : columnsList) {
                headersList.add(column.getColName());
            }

            previewParsedContent.add(0, headersList);
            return ServiceResult.success(previewParsedContent);
        } catch (Exception e) {
            return ServiceResult.error(Message.of("import.csv_errors_linestructure", lineNumber));
        }
    }

    private Json5Reader newReader(File importFile, ImportProfile profile) throws Exception {
        InputStream inputStream = inputStreamProvider.provide(importFile, profile);
        String charsetName = Charset.getCharsetById(profile.getCharset()).getCharsetName();

        return new Json5Reader(inputStream, charsetName);
    }
}
