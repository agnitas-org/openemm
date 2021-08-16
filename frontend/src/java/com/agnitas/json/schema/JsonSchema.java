/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.json.schema;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import com.agnitas.json.Json5Reader;
import com.agnitas.json.JsonNode;
import com.agnitas.json.JsonObject;
import com.agnitas.json.JsonReader;
import com.agnitas.json.schema.validator.AdditionalPropertiesValidator;
import com.agnitas.json.schema.validator.AllOfValidator;
import com.agnitas.json.schema.validator.AnyOfValidator;
import com.agnitas.json.schema.validator.BaseJsonSchemaValidator;
import com.agnitas.json.schema.validator.DependenciesValidator;
import com.agnitas.json.schema.validator.EnumValidator;
import com.agnitas.json.schema.validator.FormatValidator;
import com.agnitas.json.schema.validator.ItemsValidator;
import com.agnitas.json.schema.validator.MaxItemsValidator;
import com.agnitas.json.schema.validator.MaxLengthValidator;
import com.agnitas.json.schema.validator.MaxPropertiesValidator;
import com.agnitas.json.schema.validator.MaximumValidator;
import com.agnitas.json.schema.validator.MinItemsValidator;
import com.agnitas.json.schema.validator.MinLengthValidator;
import com.agnitas.json.schema.validator.MinPropertiesValidator;
import com.agnitas.json.schema.validator.MinimumValidator;
import com.agnitas.json.schema.validator.MultipleOfValidator;
import com.agnitas.json.schema.validator.NotValidator;
import com.agnitas.json.schema.validator.OneOfValidator;
import com.agnitas.json.schema.validator.PatternPropertiesValidator;
import com.agnitas.json.schema.validator.PatternValidator;
import com.agnitas.json.schema.validator.PropertiesValidator;
import com.agnitas.json.schema.validator.ReferenceValidator;
import com.agnitas.json.schema.validator.RequiredValidator;
import com.agnitas.json.schema.validator.TypeValidator;
import com.agnitas.json.schema.validator.UniqueItemsValidator;

/**
 * JSON Schema Validator vor Draft Version v4<br />
 * https://json-schema.org/draft-04/schema#<br />
 * <br />
 * For Validation of JSON schemas you may use the included file resource "JsonSchemaDescriptionDraftV4.json":<br />
 * JsonSchema.class.getClassLoader().getResourceAsStream("json/JsonSchemaDescriptionDraftV4.json")<br />
 * <br />
 * For the JSON schema standard definition see:<br />
 * https://json-schema.org<br />
 * <br />
 * For examples and help an JSON schema creation see:<br />
 * https://spacetelescope.github.io<br />
 */
public class JsonSchema {
	/**
	 * Url describing the JSON schema standard and version a JSON schema was written for in compliance<br />
	 * Example: "https://json-schema.org/schema#"
	 */
	private String schemaVersionUrl;

	private String id;
	private String title;
	private String description;
	private JsonObject jsonSchemaDefinition;
	private JsonSchemaDependencyResolver jsonSchemaDependencyResolver;

	/**
	 * Draft V4 mode is NOT default mode<br />
	 * <br />
	 * The default mode uses a slightly more strict JSON schema definition.<br />
	 * This is useful in detection of schema definition errors.<br />
	 * Nontheless you can switch to the Draf V4 standard behaviour<br />
	 */
	public JsonSchema(final InputStream jsonSchemaInputStream) throws JsonSchemaDefinitionError  {
		this(jsonSchemaInputStream, StandardCharsets.UTF_8, false);
	}

	/**
	 * Draft V4 mode is NOT default mode<br />
	 * <br />
	 * The default mode uses a slightly more strict JSON schema definition.<br />
	 * This is useful in detection of schema definition errors.<br />
	 * Nontheless you can switch to the Draf V4 standard behaviour<br />
	 */
	public JsonSchema(final InputStream jsonSchemaInputStream, final Charset encoding) throws JsonSchemaDefinitionError  {
		this(jsonSchemaInputStream, encoding, false);
	}

	/**
	 * Draft V4 mode is NOT default mode<br />
	 * <br />
	 * The default mode uses a slightly more strict JSON schema definition.<br />
	 * This is useful in detection of schema definition errors.<br />
	 * Nontheless you can switch to the Draf V4 standard behaviour<br />
	 */
	public JsonSchema(final InputStream jsonSchemaInputStream, final boolean useDraftV4Mode) throws JsonSchemaDefinitionError  {
		this(jsonSchemaInputStream, StandardCharsets.UTF_8, useDraftV4Mode);
	}

	/**
	 * Draft V4 mode is NOT default mode<br />
	 * <br />
	 * The default mode uses a slightly more strict JSON schema definition.<br />
	 * This is useful in detection of schema definition errors.<br />
	 * Nontheless you can switch to the Draf V4 standard behaviour<br />
	 */
	public JsonSchema(final InputStream jsonSchemaInputStream, final Charset encoding, final boolean useDraftV4Mode) throws JsonSchemaDefinitionError  {
		JsonNode jsonNode;
		try (JsonReader jsonReader = new Json5Reader(jsonSchemaInputStream, encoding)) {
			jsonNode = jsonReader.read();
		} catch (final Exception e) {
			throw new JsonSchemaDefinitionError("Cannot read JSON-Schema: " + e.getMessage(), null);
		}

		if (jsonNode == null) {
			throw new JsonSchemaDefinitionError("Contains null data", null);
		} else if (jsonNode.isJsonObject()) {
			readSchemaData((JsonObject)jsonNode.getValue());
			jsonSchemaDependencyResolver.setUseDraftV4Mode(useDraftV4Mode);
		} else {
			throw new JsonSchemaDefinitionError("Does not contain JsonObject", "$");
		}
	}

	/**
	 * Draft V4 mode is NOT default mode<br />
	 * <br />
	 * The default mode uses a slightly more strict JSON schema definition.<br />
	 * This is useful in detection of schema definition errors.<br />
	 * Nontheless you can switch to the Draf V4 standard behaviour<br />
	 */
	public JsonSchema(final JsonObject jsonSchemaDefinition) throws JsonSchemaDefinitionError  {
		this(jsonSchemaDefinition, false);
	}

	/**
	 * Draft V4 mode is NOT default mode<br />
	 * <br />
	 * The default mode uses a slightly more strict JSON schema definition.<br />
	 * This is useful in detection of schema definition errors.<br />
	 * Nontheless you can switch to the Draf V4 standard behaviour<br />
	 */
	public JsonSchema(final JsonObject jsonSchemaDefinition, final boolean useDraftV4Mode) throws JsonSchemaDefinitionError  {
		readSchemaData(jsonSchemaDefinition);
		jsonSchemaDependencyResolver.setUseDraftV4Mode(useDraftV4Mode);
	}

	private void readSchemaData(final JsonObject jsonSchemaDefinitionObject) throws JsonSchemaDefinitionError {
		if (jsonSchemaDefinitionObject == null) {
			throw new JsonSchemaDefinitionError("Contains null data", null);
		} else {
			jsonSchemaDefinition = jsonSchemaDefinitionObject;
		}

		if (jsonSchemaDefinitionObject.containsPropertyKey("id")) {
			if (jsonSchemaDefinitionObject.get("id") == null) {
				throw new JsonSchemaDefinitionError("Invalid data type 'null' for key 'id'", "$");
			} else if (!(jsonSchemaDefinitionObject.get("id") instanceof String)) {
				throw new JsonSchemaDefinitionError("Invalid data type '" + jsonSchemaDefinitionObject.get("id").getClass().getSimpleName() + "' for key 'id'", "$");
			} else {
				id = (String) jsonSchemaDefinitionObject.get("id");
			}
		}

		if (jsonSchemaDefinitionObject.containsPropertyKey("$schema")) {
			if (jsonSchemaDefinitionObject.get("$schema") == null) {
				throw new JsonSchemaDefinitionError("Invalid data type 'null' for key '$schema'", "$");
			} else if (!(jsonSchemaDefinitionObject.get("$schema") instanceof String)) {
				throw new JsonSchemaDefinitionError("Invalid data type '" + jsonSchemaDefinitionObject.get("$schema").getClass().getSimpleName() + "' for key '$schema'", "$");
			} else {
				schemaVersionUrl = (String) jsonSchemaDefinitionObject.get("$schema");
			}
		}

		if (jsonSchemaDefinitionObject.containsPropertyKey("title")) {
			if (jsonSchemaDefinitionObject.get("title") == null) {
				throw new JsonSchemaDefinitionError("Invalid data type 'null' for key 'title'", "$");
			} else if (!(jsonSchemaDefinitionObject.get("title") instanceof String)) {
				throw new JsonSchemaDefinitionError("Invalid data type '" + jsonSchemaDefinitionObject.get("title").getClass().getSimpleName() + "' for key 'title'", "$");
			} else {
				title = (String) jsonSchemaDefinitionObject.get("title");
			}
		}

		if (jsonSchemaDefinitionObject.containsPropertyKey("description")) {
			if (jsonSchemaDefinitionObject.get("description") == null) {
				throw new JsonSchemaDefinitionError("Invalid data type 'null' for key 'description'", "$");
			} else if (!(jsonSchemaDefinitionObject.get("description") instanceof String)) {
				throw new JsonSchemaDefinitionError("Invalid data type '" + jsonSchemaDefinitionObject.get("title").getClass().getSimpleName() + "' for key 'description'", "$");
			} else {
				description = (String) jsonSchemaDefinitionObject.get("description");
			}
		}

		jsonSchemaDependencyResolver = new JsonSchemaDependencyResolver(jsonSchemaDefinitionObject);
	}

	/**
	 * Download of any additional data is prevented by default.<br />
	 * Especially because there is no check for internet connection in forehand.<br />
	 */
	public void setDownloadReferencedSchemas(final boolean downloadReferencedSchemas) {
		jsonSchemaDependencyResolver.setDownloadReferencedSchemas(downloadReferencedSchemas);
	}

	/**
	 * Add some other JSON schema for usage of its reference definitions
	 *
	 * @param definitionPackageName
	 * @param jsonSchemaInputStream
	 * @throws Exception
	 */
	public void addJsonSchemaDefinition(final String definitionPackageName, final InputStream jsonSchemaInputStream) throws Exception {
		jsonSchemaDependencyResolver.addJsonSchemaDefinition(definitionPackageName, jsonSchemaInputStream);
	}

	public String getId() {
		return id;
	}

	public String getSchemaVersionUrl() {
		return schemaVersionUrl;
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

	public JsonNode validate(final InputStream jsonDataInputStream, final Charset encoding) throws JsonSchemaDefinitionError, JsonSchemaDataValidationError {
		JsonNode jsonDataNode;
		try (JsonReader jsonReader = new Json5Reader(jsonDataInputStream, encoding)) {
			jsonDataNode = jsonReader.read();
		} catch (final Exception e) {
			throw new JsonSchemaDataValidationError("Cannot read JSON data: " + e.getMessage(), "");
		}

		final List<BaseJsonSchemaValidator> validators = createValidators(jsonSchemaDefinition, jsonSchemaDependencyResolver, "$", jsonDataNode, "$");
		for (final BaseJsonSchemaValidator validator : validators) {
			validator.validate();
		}
		return jsonDataNode;
	}

	public JsonNode validate(final InputStream jsonDataInputStream) throws JsonSchemaDefinitionError, JsonSchemaDataValidationError {
		JsonNode jsonDataNode;
		try (JsonReader jsonReader = new Json5Reader(jsonDataInputStream)) {
			jsonDataNode = jsonReader.read();
		} catch (final Exception e) {
			throw new JsonSchemaDataValidationError("Cannot read JSON data: " + e.getMessage(), "");
		}

		final List<BaseJsonSchemaValidator> validators = createValidators(jsonSchemaDefinition, jsonSchemaDependencyResolver, "$", jsonDataNode, "$");
		for (final BaseJsonSchemaValidator validator : validators) {
			validator.validate();
		}
		return jsonDataNode;
	}

	public void validate(final Object jsonData) throws Exception {
		final JsonNode jsonDataNode = new JsonNode(jsonData);

		final List<BaseJsonSchemaValidator> validators = createValidators(jsonSchemaDefinition, jsonSchemaDependencyResolver, "$", jsonDataNode, "$");
		for (final BaseJsonSchemaValidator validator : validators) {
			validator.validate();
		}
	}

	public static List<BaseJsonSchemaValidator> createValidators(final JsonObject jsonSchemaDefinitionObject, final JsonSchemaDependencyResolver jsonSchemaDependencyResolver, final String currentJsonSchemaPath, final JsonNode jsonNode, final String currentJsonPath) throws JsonSchemaDefinitionError {
		final List<BaseJsonSchemaValidator> validators = new ArrayList<>();
		for (final Entry<String, Object> entry : jsonSchemaDefinitionObject.entrySet()) {
			switch (entry.getKey()) {
				case "type":
					validators.add(new TypeValidator(jsonSchemaDependencyResolver, currentJsonSchemaPath + "." + entry.getKey(), entry.getValue(), jsonNode, currentJsonPath));
					break;
				case "properties":
					validators.add(new PropertiesValidator(jsonSchemaDependencyResolver, currentJsonSchemaPath + "." + entry.getKey(), entry.getValue(), jsonNode, currentJsonPath));
					break;
				case "patternProperties":
					validators.add(new PatternPropertiesValidator(jsonSchemaDependencyResolver, currentJsonSchemaPath + "." + entry.getKey(), entry.getValue(), jsonNode, currentJsonPath));
					break;
				case "additionalProperties":
					validators.add(new AdditionalPropertiesValidator(jsonSchemaDefinitionObject, jsonSchemaDependencyResolver, currentJsonSchemaPath + "." + entry.getKey(), entry.getValue(), jsonNode, currentJsonPath));
					break;
				case "required":
					validators.add(new RequiredValidator(jsonSchemaDependencyResolver, currentJsonSchemaPath + "." + entry.getKey(), entry.getValue(), jsonNode, currentJsonPath));
					break;
				case "minProperties":
					validators.add(new MinPropertiesValidator(jsonSchemaDependencyResolver, currentJsonSchemaPath + "." + entry.getKey(), entry.getValue(), jsonNode, currentJsonPath));
					break;
				case "maxProperties":
					validators.add(new MaxPropertiesValidator(jsonSchemaDependencyResolver, currentJsonSchemaPath + "." + entry.getKey(), entry.getValue(), jsonNode, currentJsonPath));
					break;
				case "items":
					validators.add(new ItemsValidator(jsonSchemaDefinitionObject, jsonSchemaDependencyResolver, currentJsonSchemaPath + "." + entry.getKey(), entry.getValue(), jsonNode, currentJsonPath));
					break;
				case "minItems":
					validators.add(new MinItemsValidator(jsonSchemaDependencyResolver, currentJsonSchemaPath + "." + entry.getKey(), entry.getValue(), jsonNode, currentJsonPath));
					break;
				case "maxItems":
					validators.add(new MaxItemsValidator(jsonSchemaDependencyResolver, currentJsonSchemaPath + "." + entry.getKey(), entry.getValue(), jsonNode, currentJsonPath));
					break;
				case "minLength":
					validators.add(new MinLengthValidator(jsonSchemaDependencyResolver, currentJsonSchemaPath + "." + entry.getKey(), entry.getValue(), jsonNode, currentJsonPath));
					break;
				case "maxLength":
					validators.add(new MaxLengthValidator(jsonSchemaDependencyResolver, currentJsonSchemaPath + "." + entry.getKey(), entry.getValue(), jsonNode, currentJsonPath));
					break;
				case "uniqueItems":
					validators.add(new UniqueItemsValidator(jsonSchemaDependencyResolver, currentJsonSchemaPath + "." + entry.getKey(), entry.getValue(), jsonNode, currentJsonPath));
					break;
				case "minimum":
					validators.add(new MinimumValidator(jsonSchemaDefinitionObject, jsonSchemaDependencyResolver, currentJsonSchemaPath + "." + entry.getKey(), entry.getValue(), jsonNode, currentJsonPath));
					break;
				case "maximum":
					validators.add(new MaximumValidator(jsonSchemaDefinitionObject, jsonSchemaDependencyResolver, currentJsonSchemaPath + "." + entry.getKey(), entry.getValue(), jsonNode, currentJsonPath));
					break;
				case "multipleOf":
					validators.add(new MultipleOfValidator(jsonSchemaDependencyResolver, currentJsonSchemaPath + "." + entry.getKey(), entry.getValue(), jsonNode, currentJsonPath));
					break;
				case "anyOf":
					validators.add(new AnyOfValidator(jsonSchemaDependencyResolver, currentJsonSchemaPath + "." + entry.getKey(), entry.getValue(), jsonNode, currentJsonPath));
					break;
				case "allOf":
					validators.add(new AllOfValidator(jsonSchemaDependencyResolver, currentJsonSchemaPath + "." + entry.getKey(), entry.getValue(), jsonNode, currentJsonPath));
					break;
				case "oneOf":
					validators.add(new OneOfValidator(jsonSchemaDependencyResolver, currentJsonSchemaPath + "." + entry.getKey(), entry.getValue(), jsonNode, currentJsonPath));
					break;
				case "not":
					validators.add(new NotValidator(jsonSchemaDependencyResolver, currentJsonSchemaPath + "." + entry.getKey(), entry.getValue(), jsonNode, currentJsonPath));
					break;
				case "enum":
					validators.add(new EnumValidator(jsonSchemaDependencyResolver, currentJsonSchemaPath + "." + entry.getKey(), entry.getValue(), jsonNode, currentJsonPath));
					break;
				case "format":
					validators.add(new FormatValidator(jsonSchemaDependencyResolver, currentJsonSchemaPath + "." + entry.getKey(), entry.getValue(), jsonNode, currentJsonPath));
					break;
				case "pattern":
					validators.add(new PatternValidator(jsonSchemaDependencyResolver, currentJsonSchemaPath + "." + entry.getKey(), entry.getValue(), jsonNode, currentJsonPath));
					break;
				case "dependencies":
					validators.add(new DependenciesValidator(jsonSchemaDependencyResolver, currentJsonSchemaPath + "." + entry.getKey(), entry.getValue(), jsonNode, currentJsonPath));
					break;

				case "exclusiveMinimum":
					// Do nothing, because this is validated by MinimumValidator, too
					if (!jsonSchemaDefinitionObject.containsPropertyKey("minimum")) {
						throw new JsonSchemaDefinitionError("Missing 'minimum' rule for 'exclusiveMinimum'", currentJsonSchemaPath);
					}
					break;
				case "exclusiveMaximum":
					// Do nothing, because this is validated by MaximumValidator, too
					if (!jsonSchemaDefinitionObject.containsPropertyKey("maximum")) {
						throw new JsonSchemaDefinitionError("Missing 'maximum' rule for 'exclusiveMaximum'", currentJsonSchemaPath);
					}
					break;
				case "additionalItems":
					// Do nothing, because this is validated by ItemsValidator, too
					if (!jsonSchemaDefinitionObject.containsPropertyKey("items") && !jsonSchemaDependencyResolver.isUseDraftV4Mode()) {
						throw new JsonSchemaDefinitionError("Missing 'items' rule for 'additionalItems'", currentJsonSchemaPath);
					}
					break;

				case "$ref":
					validators.add(new ReferenceValidator(jsonSchemaDependencyResolver, currentJsonSchemaPath + "." + entry.getKey(), entry.getValue(), jsonNode, currentJsonPath));
					break;

				case "id":
					// id should be a descriptive url
					if (!"$".equals(currentJsonSchemaPath) && !currentJsonSchemaPath.endsWith("#")) {
						throw new JsonSchemaDefinitionError("JSON schema 'id' must be defined on top level of JSON schema", currentJsonSchemaPath);
					}
					break;
				case "$schema":
					// $schema should be a descriptive url
					if (!"$".equals(currentJsonSchemaPath) && !currentJsonSchemaPath.endsWith("#")) {
						throw new JsonSchemaDefinitionError("JSON schema '$schema' must be defined on top level of JSON schema", currentJsonSchemaPath);
					}
					break;
				case "definitions":
					// Defined JSON schema definitions
					if (!"$".equals(currentJsonSchemaPath) && !currentJsonSchemaPath.endsWith("#")) {
						throw new JsonSchemaDefinitionError("JSON schema 'definitions' must be defined on top level of JSON schema", currentJsonSchemaPath);
					}
					break;

				case "title":
					// Descriptive title
					if (!(entry.getValue() instanceof String)) {
						throw new JsonSchemaDefinitionError("Invalid data type '" + entry.getValue().getClass().getSimpleName() + "' for key 'description'", currentJsonSchemaPath);
					}
					break;
				case "description":
					// Descriptive comments
					if (!(entry.getValue() instanceof String)) {
						throw new JsonSchemaDefinitionError("Invalid data type '" + entry.getValue().getClass().getSimpleName() + "' for key 'description'", currentJsonSchemaPath);
					}
					break;
				case "default":
					// Default value for processing the given JSON data, which is irrelevant for validation
					break;
				default:
					if (!jsonSchemaDependencyResolver.isUseDraftV4Mode()) {
						throw new JsonSchemaDefinitionError("Unexpected data key '" + entry.getKey() + "'", currentJsonSchemaPath);
					}
			}
		}
		return validators;
	}
}
