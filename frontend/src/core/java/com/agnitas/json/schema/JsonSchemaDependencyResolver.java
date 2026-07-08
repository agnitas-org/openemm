/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.json.schema;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.agnitas.json.Json5Reader;
import com.agnitas.json.JsonArray;
import com.agnitas.json.JsonNode;
import com.agnitas.json.JsonObject;
import com.agnitas.json.JsonPath;

public class JsonSchemaDependencyResolver {
	private JsonObject schemaDocumentNode = null;
	private final Map<String, JsonObject> additionalSchemaDocumentNodes = new HashMap<>();

	/**
	 * Draft V4 mode is NOT default mode<br />
	 * <br />
	 * The default mode uses a slightly more strict JSON schema definition.<br />
	 * This is useful in detection of schema definition errors.<br />
	 * Nontheless you can switch to the Draf V4 standard behaviour<br />
	 */
	private boolean useDraftV4Mode = false;

	private boolean downloadReferencedSchemas = false;

	private String latestJsonPath = null;
	private Set<String> latestDependencies;

	public JsonSchemaDependencyResolver(final JsonObject schemaDocumentNode) throws JsonSchemaDefinitionError {
		if (schemaDocumentNode == null) {
			throw new JsonSchemaDefinitionError("Invalid data type 'null' for JsonSchemaDependencyResolver", "$");
		}
		this.schemaDocumentNode = schemaDocumentNode;
	}

	public Object getDependencyByReference(final String reference, final String jsonSchemaPath) throws Exception {
		if (reference != null) {
			if (!reference.contains("#")) {
				// Dereference simple reference without '#'
				if (schemaDocumentNode.get("definitions") != null && schemaDocumentNode.get("definitions") instanceof JsonObject && ((JsonObject) schemaDocumentNode.get("definitions")).containsPropertyKey(reference)) {
					return ((JsonObject) schemaDocumentNode.get("definitions")).get(reference);
				} else {
					for (final JsonObject indirectJsonDefinitions : additionalSchemaDocumentNodes.values()) {
						if (indirectJsonDefinitions.get("definitions") != null && indirectJsonDefinitions.get("definitions") instanceof JsonObject && ((JsonObject) indirectJsonDefinitions.get("definitions")).containsPropertyKey(reference)) {
							return ((JsonObject) indirectJsonDefinitions.get("definitions")).get(reference);
						}
					}
					throw new Exception("Invalid JSON schema reference key '" + reference + "' or reference key not found. Use simple reference keys or this pattern for reference keys: '<referenced packagename or empty>#/definitions/<your reference key>'");
				}
			} else if (reference.startsWith("#")) {
				// Dereference local document reference
				final JsonPath jsonPath = new JsonPath(reference);
				JsonObject referencedObject = schemaDocumentNode;
				for (final Object referencePathPartObject : jsonPath.getPathParts()) {
					if (!(referencePathPartObject instanceof String)) {
						throw new JsonSchemaDefinitionError("Invalid JSON reference path contains array index'" + reference + "'", jsonSchemaPath);
					}
					final String referencePathPart = (String) referencePathPartObject;
					if (!referencedObject.containsPropertyKey(referencePathPart)) {
						throw new JsonSchemaDefinitionError("JsonSchema does not contain the reference path '" + reference + "'", jsonSchemaPath);
					} else if (referencedObject.get(referencePathPart) == null) {
						throw new JsonSchemaDefinitionError("Invalid data type 'null' for reference path '" + reference + "'", jsonSchemaPath);
					} else if (!(referencedObject.get(referencePathPart) instanceof JsonObject)) {
						throw new JsonSchemaDefinitionError("Invalid data type '" + schemaDocumentNode.get("definitions").getClass().getSimpleName() + "' for reference path '" + reference + "'", jsonSchemaPath);
					} else {
						referencedObject = (JsonObject) referencedObject.get(referencePathPart);
					}
				}
				return referencedObject;
			} else {
				// Dereference other document reference
				final String packageName = reference.substring(0, reference.lastIndexOf("#"));

				if (!additionalSchemaDocumentNodes.containsKey(packageName) && packageName != null && packageName.toLowerCase().startsWith("http") && downloadReferencedSchemas) {
					final URLConnection urlConnection = new URL(packageName).openConnection();
					final int statusCode = ((HttpURLConnection) urlConnection).getResponseCode();
					if (statusCode != HttpURLConnection.HTTP_OK) {
						throw new Exception("Cannot get content from '" + packageName + "'. Http-Code was " + statusCode);
					}
					try (InputStream jsonSchemaInputStream = urlConnection.getInputStream()) {
						addJsonSchemaDefinition(packageName, jsonSchemaInputStream);
					}
				}

				if (!additionalSchemaDocumentNodes.containsKey(packageName)) {
					throw new Exception("Unknown JSON schema reference package name '" + packageName + "'");
				} else if (additionalSchemaDocumentNodes.get(packageName) == null) {
					throw new Exception("Invalid empty JSON schema reference for package name '" + packageName + "'");
				} else {
					JsonObject referencedObject = additionalSchemaDocumentNodes.get(packageName);
					final JsonPath jsonPath = new JsonPath(reference.substring(reference.lastIndexOf("#")));
					for (final Object referencePathPartObject : jsonPath.getPathParts()) {
						if (!(referencePathPartObject instanceof String)) {
							throw new JsonSchemaDefinitionError("Invalid JSON reference path contains array index'" + reference + "'", jsonSchemaPath);
						}
						final String referencePathPart = (String) referencePathPartObject;
						if (!referencedObject.containsPropertyKey(referencePathPart)) {
							throw new JsonSchemaDefinitionError("Referenced JsonSchema does not contain the reference path '" + reference + "'", jsonSchemaPath);
						} else if (referencedObject.get(referencePathPart) == null) {
							throw new JsonSchemaDefinitionError("Invalid data type 'null' for reference path '" + reference + "'", jsonSchemaPath);
						} else if (!(referencedObject.get(referencePathPart) instanceof JsonObject)) {
							throw new JsonSchemaDefinitionError("Invalid data type '" + schemaDocumentNode.get("definitions").getClass().getSimpleName() + "' for reference path '" + reference + "'", jsonSchemaPath);
						} else {
							referencedObject = (JsonObject) referencedObject.get(referencePathPart);
						}
					}
					return referencedObject;
				}
			}
		} else {
			throw new Exception("Invalid JSON schema reference key 'null'");
		}
	}

	public void addJsonSchemaDefinition(final String definitionPackageName, final InputStream jsonSchemaInputStream) throws Exception {
		if (StringUtils.isBlank(definitionPackageName)) {
			throw new Exception("Invalid empty JSON schema definition package name");
		} else if (additionalSchemaDocumentNodes.containsKey(definitionPackageName)) {
			throw new Exception("Additional JSON schema definition package '" + definitionPackageName + "' was already added before");
		} else {
			try (Json5Reader reader = new Json5Reader(jsonSchemaInputStream)) {
				final JsonNode jsonNode = reader.read();
				if (!jsonNode.isJsonObject()) {
					throw new Exception("Additional JSON schema definition package '" + definitionPackageName + "' does not contain JSON schema data of type 'object'");
				} else {
					final JsonObject jsonSchema = (JsonObject) jsonNode.getValue();
					redirectReferences(jsonSchema, "#", definitionPackageName + "#");
					additionalSchemaDocumentNodes.put(definitionPackageName, jsonSchema);
				}
			}
		}
	}

	private void redirectReferences(final JsonObject jsonObject, final String referenceDefinitionStart, final String referenceDefinitionReplacement) {
		for (final Entry<String, Object> entry : jsonObject.entrySet()) {
			if ("$ref".equals(entry.getKey()) && entry.getValue() != null && entry.getValue() instanceof String && ((String) entry.getValue()).startsWith(referenceDefinitionStart)) {
				jsonObject.add("$ref", referenceDefinitionReplacement + ((String) entry.getValue()).substring(referenceDefinitionStart.length()));
			} else if (entry.getValue() instanceof JsonObject) {
				redirectReferences((JsonObject) entry.getValue(), referenceDefinitionStart, referenceDefinitionReplacement);
			} else if (entry.getValue() instanceof JsonArray) {
				redirectReferences((JsonArray) entry.getValue(), referenceDefinitionStart, referenceDefinitionReplacement);
			}
		}
	}

	private void redirectReferences(final JsonArray jsonArray, final String referenceDefinitionStart, final String referenceDefinitionReplacement) {
		for (final Object item : jsonArray) {
			if (item instanceof JsonObject) {
				redirectReferences((JsonObject) item, referenceDefinitionStart, referenceDefinitionReplacement);
			} else if (item instanceof JsonArray) {
				redirectReferences((JsonArray) item, referenceDefinitionStart, referenceDefinitionReplacement);
			}
		}
	}

	public void checkCyclicDependency(final String jsonPath, final String validatorData, final String jsonSchemaPath) throws JsonSchemaDefinitionError {
		if (latestJsonPath == null || !latestJsonPath.equals(jsonPath)) {
			latestJsonPath = jsonPath;
			latestDependencies = new HashSet<>();
		}
		if (latestDependencies.contains(validatorData)) {
			throw new JsonSchemaDefinitionError("Cyclic dependency detected: '" + StringUtils.join(latestDependencies, "', ") + "'", jsonSchemaPath);
		} else{
			latestDependencies.add(validatorData);
		}
	}

	public void setUseDraftV4Mode(final boolean useDraftV4Mode) {
		this.useDraftV4Mode = useDraftV4Mode;
	}

	public boolean isUseDraftV4Mode() {
		return useDraftV4Mode;
	}

	public void setDownloadReferencedSchemas(final boolean downloadReferencedSchemas) {
		this.downloadReferencedSchemas = downloadReferencedSchemas;
	}
}
