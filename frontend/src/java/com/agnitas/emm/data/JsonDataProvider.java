/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.agnitas.util.DateUtilities;
import org.agnitas.util.DbColumnType;
import org.agnitas.util.DbColumnType.SimpleDataType;
import org.agnitas.util.Tuple;
import org.agnitas.util.ZipUtilities;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import com.agnitas.json.Json5Reader;
import com.agnitas.json.JsonNode;
import com.agnitas.json.JsonObject;
import com.agnitas.json.JsonReader.JsonToken;
import com.agnitas.json.JsonUtilities;
import com.agnitas.json.JsonWriter;
import com.agnitas.json.schema.JsonSchema;

public class JsonDataProvider extends DataProvider {
	private Json5Reader jsonReader = null;
	private List<String> dataPropertyNames = null;
	private final Map<String, DbColumnType> dataTypes = null;
	private Integer itemsAmount = null;
	private String dataPath = null;
	private String schemaFilePath = null;

	private final Charset encoding = StandardCharsets.UTF_8;

	public JsonDataProvider(final File importFile, final char[] zipPassword, final String dataPath, final String schemaFilePath) {
		super(importFile, zipPassword);
		this.dataPath = dataPath;
		this.schemaFilePath = schemaFilePath;
	}

	@Override
	public String getConfigurationLogString() {
		return getConfigurationLogString()
			+ "Format: JSON" + "\n"
			+ "Encoding: " + encoding + "\n";
	}

	@Override
	public Map<String, DbColumnType> scanDataPropertyTypes(final Map<String, Tuple<String, String>> mapping) throws Exception {
		if (dataTypes == null) {
			openReader();

			int itemCount = 0;
			dataPropertyNames = new ArrayList<>();

			Map<String, Object> nextItem;
			while ((nextItem = getNextItemData()) != null) {
				for (final Entry<String, Object> itemProperty : nextItem.entrySet()) {
					final String propertyName = itemProperty.getKey();
					final Object propertyValue = itemProperty.getValue();

					String formatInfo = null;
					if (mapping != null) {
						for (final Tuple<String, String> mappingValue : mapping.values()) {
							if (mappingValue.getFirst().equals(propertyName)) {
								if (StringUtils.isNotBlank(mappingValue.getSecond())) {
									formatInfo = mappingValue.getSecond();
									break;
								}
							}
						}
					}

					final SimpleDataType currentType = dataTypes.get(propertyName) == null ? null : dataTypes.get(propertyName).getSimpleDataType();
					if (currentType != SimpleDataType.Blob) {
						if (propertyValue == null) {
							if (!dataTypes.containsKey(propertyName)) {
								dataTypes.put(propertyName, null);
							}
						} else if ("file".equalsIgnoreCase(formatInfo) || (propertyValue instanceof String && ((String) propertyValue).length() > 4000)) {
							dataTypes.put(propertyName, new DbColumnType("BLOB", -1, -1, -1, true));
						} else if (currentType != SimpleDataType.Characters && StringUtils.isNotBlank(formatInfo) && !".".equals(formatInfo) && !",".equals(formatInfo) && !"file".equalsIgnoreCase(formatInfo) && propertyValue instanceof String) {
							final String value = ((String) propertyValue).trim();
							try {
								DateUtilities.parseLocalDateTime(formatInfo, value);
								dataTypes.put(propertyName, new DbColumnType("TIMESTAMP", -1, -1, -1, true));
							} catch (@SuppressWarnings("unused") final Exception e) {
								try {
									DateUtilities.parseLocalDateTime(DateUtilities.ISO_8601_DATETIME_FORMAT, value);
									dataTypes.put(propertyName, new DbColumnType("TIMESTAMP", -1, -1, -1, true));
								} catch (@SuppressWarnings("unused") final Exception e2) {
									dataTypes.put(propertyName, new DbColumnType("VARCHAR", Math.max(dataTypes.get(propertyName) == null ? 0 : dataTypes.get(propertyName).getCharacterLength(), value.getBytes(StandardCharsets.UTF_8).length), -1, -1, true));
								}
							}
						} else if (currentType != SimpleDataType.Characters && StringUtils.isBlank(formatInfo) && propertyValue instanceof String) {
							final String value = ((String) propertyValue).trim();
							try {
								DateUtilities.parseLocalDateTime(DateUtilities.ISO_8601_DATETIME_FORMAT, value);
								dataTypes.put(propertyName, new DbColumnType("TIMESTAMP", -1, -1, -1, true));
							} catch (@SuppressWarnings("unused") final Exception e) {
								try {
									DateUtilities.parseDateTime(DateUtilities.ISO_8601_DATE_FORMAT, value);
									dataTypes.put(propertyName, new DbColumnType("DATE", -1, -1, -1, true));
								} catch (@SuppressWarnings("unused") final Exception e2) {
									dataTypes.put(propertyName, new DbColumnType("VARCHAR", Math.max(dataTypes.get(propertyName) == null ? 0 : dataTypes.get(propertyName).getCharacterLength(), value.getBytes(StandardCharsets.UTF_8).length), -1, -1, true));
								}
							}
						} else if (currentType != SimpleDataType.Characters && currentType != SimpleDataType.DateTime && currentType != SimpleDataType.Float && propertyValue instanceof Integer) {
							dataTypes.put(propertyName, new DbColumnType("INTEGER", -1, -1, -1, true));
						} else if (currentType != SimpleDataType.Characters && currentType != SimpleDataType.DateTime && (propertyValue instanceof Float || propertyValue instanceof Double)) {
							dataTypes.put(propertyName, new DbColumnType("DOUBLE", -1, -1, -1, true));
						} else {
							dataTypes.put(propertyName, new DbColumnType("VARCHAR", Math.max(dataTypes.get(propertyName) == null ? 0 : dataTypes.get(propertyName).getCharacterLength(), propertyValue.toString().getBytes(StandardCharsets.UTF_8).length), -1, -1, true));
						}
					}
				}

				itemCount++;
			}

			close();

			itemsAmount = itemCount;
			dataPropertyNames = new ArrayList<>(dataTypes.keySet());
		}

		return dataTypes;
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
			JsonObject nextJsonObject = null;
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

	@Override
	public File filterDataItems(final List<Integer> indexList, final String fileSuffix) throws Exception {
		@SuppressWarnings("resource")
		OutputStream outputStream = null;
		@SuppressWarnings("resource")
		JsonWriter jsonWriter = null;
		try {
			openReader();

			File filteredDataFile;
			if (StringUtils.endsWithIgnoreCase(getImportFilePath(), ".zip")) {
				filteredDataFile = new File(getImportFilePath() + "." + fileSuffix + ".json.zip");
				outputStream = ZipUtilities.openNewZipOutputStream(filteredDataFile);
				((ZipOutputStream) outputStream).putNextEntry(new ZipEntry(new File(getImportFilePath() + "." + fileSuffix + ".json").getName()));
			} else {
				filteredDataFile = new File(getImportFilePath() + "." + fileSuffix + ".json");
				outputStream = new FileOutputStream(filteredDataFile);
			}

			jsonWriter = new JsonWriter(outputStream, encoding.toString());
			jsonWriter.openJsonArray();

			Map<String, Object> item;
			int itemIndex = 0;
			while ((item = getNextItemData()) != null) {
				itemIndex++;
				if (indexList.contains(itemIndex)) {
					final JsonObject filteredObject = new JsonObject();
					for (final Entry<String, Object> entry : item.entrySet()) {
						filteredObject.add(entry.getKey(), entry.getValue());
					}

					jsonWriter.add(filteredObject);
				}
			}

			jsonWriter.closeJsonArray();

			return filteredDataFile;
		} finally {
			close();
			IOUtils.closeQuietly(jsonWriter);
			IOUtils.closeQuietly(outputStream);
		}
	}

	private void openReader() throws Exception {
		if (jsonReader != null) {
			throw new Exception("Reader was already opened before");
		}

		try {
			if (StringUtils.isNotEmpty(schemaFilePath)) {
				if (!new File(schemaFilePath).exists()) {
					throw new Exception("JSON-Schema file does not exist: " + schemaFilePath);
				} else if (new File(schemaFilePath).isDirectory()) {
					throw new Exception("JSON-Schema path is a directory: " + schemaFilePath);
				} else if (new File(schemaFilePath).length() == 0) {
					throw new Exception("JSON-Schema file is empty: " + schemaFilePath);
				}

				try (InputStream validationStream = getInputStream();
						InputStream schemaStream = new FileInputStream(new File(schemaFilePath))) {
					final JsonSchema schema = new JsonSchema(schemaStream);
					schema.validate(validationStream);
				} catch (final Exception e) {
					throw new Exception("JSON data does not comply to JSON schema '" + schemaFilePath + "': " + e.getMessage());
				}
			}

			jsonReader = new Json5Reader(getInputStream(), encoding);
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
