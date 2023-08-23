/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.import_profile.component.reader;

import com.agnitas.beans.Admin;
import com.agnitas.dao.ComRecipientDao;
import com.agnitas.json.Json5Reader;
import com.agnitas.json.JsonObject;
import com.agnitas.json.JsonReader;
import com.agnitas.service.ServiceResult;
import org.agnitas.beans.ColumnMapping;
import org.agnitas.beans.ImportProfile;
import org.agnitas.util.CsvColInfo;
import org.agnitas.util.importvalues.Charset;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ColumnMappingsJsonReader extends ColumnMappingsReader {

    private final ComRecipientDao recipientDao;

    public ColumnMappingsJsonReader(ComRecipientDao recipientDao) {
        this.recipientDao = recipientDao;
    }

    @Override
	public ServiceResult<List<ColumnMapping>> read(InputStream fileStream, ImportProfile profile, Admin admin) throws Exception {
        try (Json5Reader jsonReader = new Json5Reader(fileStream, Charset.getCharsetById(profile.getCharset()).getCharsetName())) {
            jsonReader.readNextToken();

            while (jsonReader.getCurrentToken() != null && jsonReader.getCurrentToken() != JsonReader.JsonToken.JsonArray_Open) {
                jsonReader.readNextToken();
            }

            if (jsonReader.getCurrentToken() != JsonReader.JsonToken.JsonArray_Open) {
                throw new Exception("Json data does not contain expected JsonArray");
            }

            Map<String, CsvColInfo> dbColumns = recipientDao.readDBColumns(admin.getCompanyID(), admin.getAdminID(), profile.getKeyColumns());
            List<ColumnMapping> newMappings = new ArrayList<>();

            while (jsonReader.readNextJsonNode()) {
                Object currentObject = jsonReader.getCurrentObject();
                if (!(currentObject instanceof JsonObject)) {
                    throw new Exception("Json data does not contain expected JsonArray of JsonObjects");
                }

                JsonObject currentJsonObject = (JsonObject) currentObject;

                List<ColumnMapping> foundMappings = currentJsonObject.keySet().stream()
                        .distinct()
                        .filter(key -> !isMappingWithFileColumnExists(key, newMappings))
                        .map(key -> createNewColumnMapping(key, profile.getId(), dbColumns))
                        .collect(Collectors.toList());

                newMappings.addAll(foundMappings);
            }

            return ServiceResult.success(newMappings);
        }
    }

    private boolean isMappingWithFileColumnExists(String fileColumn, List<ColumnMapping> mappings) {
        return mappings.stream()
                .anyMatch(cm -> cm.getFileColumn() != null && cm.getFileColumn().equals(fileColumn));
    }
}
