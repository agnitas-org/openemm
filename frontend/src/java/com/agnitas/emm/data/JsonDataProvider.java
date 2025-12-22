/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.agnitas.json.Json5Reader;
import com.agnitas.json.JsonNode;
import com.agnitas.json.JsonObject;
import com.agnitas.json.JsonReader.JsonToken;
import com.agnitas.json.JsonUtilities;
import com.agnitas.json.schema.JsonSchema;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

public class JsonDataProvider extends DataProvider {

	private static final Charset ENCODING = StandardCharsets.UTF_8;

	private Json5Reader jsonReader = null;
	private List<String> dataPropertyNames = null;
	private Integer itemsAmount = null;

	private final String dataPath;
	private final String schemaFilePath;

	public JsonDataProvider(File importFile, char[] zipPassword, String dataPath, String schemaFilePath) {
		super(importFile, zipPassword);
		this.dataPath = dataPath;
		this.schemaFilePath = schemaFilePath;
	}

	@Override
	public String getConfigurationLogString() {
		return getConfigurationLogString()
			+ "Format: JSON" + "\n"
			+ "Encoding: " + ENCODING + "\n";
	}

	@Override
	public List<String> getAvailableDataPropertyNames() throws Exception {
		if (dataPropertyNames == null) {
			openReader();

			int itemCount = 0;
			dataPropertyNames = new ArrayList<>();

			Map<String, Object> nextItem;
			while ((nextItem = getNextItemData()) != null) {
				for (final Entry<String, Object> itemProperty : nextItem.entrySet()) {
					final String propertyName = itemProperty.getKey();
					if (!dataPropertyNames.contains(propertyName)) {
						dataPropertyNames.add(propertyName);
					}
				}

				itemCount++;
			}

			close();

			itemsAmount = itemCount;
		}

		return dataPropertyNames;
	}

	@Override
	public long getItemsAmountToImport() throws Exception {
		if (itemsAmount == null) {
			getAvailableDataPropertyNames();
		}

		return itemsAmount;
	}

	@Override
	public Map<String, Object> getNextItemData() throws Exception {
		if (jsonReader == null) {
			openReader();
		}

		if (!jsonReader.readNextJsonNode()) {
			return null;
		} else {
			JsonObject nextJsonObject;
			final Object nextObject = jsonReader.getCurrentObject();
			if (nextObject instanceof JsonObject) {
				nextJsonObject = (JsonObject) nextObject;
			} else if (nextObject instanceof JsonNode) {
				if (((JsonNode) nextObject).isJsonObject()) {
					nextJsonObject = (JsonObject) ((JsonNode) nextObject).getValue();
				} else {
					throw new Exception("Invalid json data of type: " + ((JsonNode) nextObject).getJsonDataType().getName());
				}
			} else {
				throw new Exception("Invalid json data of type: " + nextObject.getClass().getSimpleName());
			}
			
			if (nextJsonObject != null) {
				final Map<String, Object> returnMap = new HashMap<>();
				for (final String key : nextJsonObject.keySet()) {
					returnMap.put(key, nextJsonObject.get(key));
				}
				return returnMap;
			} else {
				return null;
			}
		}
	}

	@Override
	public void close() {
		IOUtils.closeQuietly(jsonReader);
		jsonReader = null;
		super.close();
	}

	private void openReader() throws Exception {
		if (jsonReader != null) {
			throw new IllegalStateException("Reader was already opened before");
		}

		try {
			if (StringUtils.isNotEmpty(schemaFilePath)) {
				if (!new File(schemaFilePath).exists()) {
					throw new IllegalArgumentException("JSON-Schema file does not exist: " + schemaFilePath);
				} else if (new File(schemaFilePath).isDirectory()) {
					throw new IllegalArgumentException("JSON-Schema path is a directory: " + schemaFilePath);
				} else if (new File(schemaFilePath).length() == 0) {
					throw new IllegalArgumentException("JSON-Schema file is empty: " + schemaFilePath);
				}

				try (InputStream validationStream = getInputStream();
						InputStream schemaStream = new FileInputStream(new File(schemaFilePath))) {
					final JsonSchema schema = new JsonSchema(schemaStream);
					schema.validate(validationStream);
				} catch (final Exception e) {
					throw new Exception("JSON data does not comply to JSON schema '" + schemaFilePath + "': " + e.getMessage());
				}
			}

			jsonReader = new Json5Reader(getInputStream(), ENCODING);
			if (StringUtils.isNotEmpty(dataPath)) {
				// Read JSON path
				JsonUtilities.readUpToJsonPath(jsonReader, dataPath);
				jsonReader.readNextToken();
				if (jsonReader.getCurrentToken() != JsonToken.JsonArray_Open) {
					throw new Exception("Invalid non-array json data for import at: " + dataPath);
				}
			} else {
				jsonReader.readNextToken();
				if (jsonReader.getCurrentToken() != JsonToken.JsonArray_Open) {
					throw new Exception("Invalid non-array json data for import");
				}
			}
		} catch (final Exception e) {
			IOUtils.closeQuietly(jsonReader);
			throw e;
		}
	}
}
