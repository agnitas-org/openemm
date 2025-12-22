/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.json;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map.Entry;
import java.util.Stack;

import com.agnitas.util.AgnUtils;
import com.agnitas.util.DateUtilities;

public class JsonWriter implements Closeable {

	/** Output stream. */
	private OutputStream outputStream;

	/** Output encoding. */
	private final Charset encoding;

	/** Output writer. */
	private BufferedWriter outputWriter = null;
	
	private final Stack<JsonStackItem> openJsonStackItems = new Stack<>();
	
	private String linebreak = "\n";
	private String indention = "\t";
	private String separator = " ";

	private enum JsonStackItem {
		Array_Empty,
		Array,
		Object_Empty,
		Object,
		Object_Value
	}
	
	public JsonWriter(OutputStream outputStream) {
		this(outputStream, StandardCharsets.UTF_8);
	}
	
	public JsonWriter(OutputStream outputStream, Charset charset) {
		this.outputStream = outputStream;
		this.encoding = charset;
	}
	
	public void setIndentation(String indentation) {
		this.indention = indentation;
	}
	
	public void setIndentation(char indentationCharacter) {
		this.indention = Character.toString(indentationCharacter);
	}
	
	public String getLinebreak() {
		return linebreak;
	}

	public void setLinebreak(String linebreak) {
		this.linebreak = linebreak;
	}

	public String getSeparator() {
		return separator;
	}

	public void setSeparator(String separator) {
		this.separator = separator;
	}

	public void setUglify(boolean value) {
		if (value) {
			linebreak = "";
			indention = "";
			separator = "";
		} else {
			linebreak = "\n";
			indention = "\t";
			separator = " ";
		}
	}
	
	public void openJsonObject() {
		if (outputWriter == null) {
			write("{", true);
			openJsonStackItems.push(JsonStackItem.Object_Empty);
		} else {
			JsonStackItem latestOpenJsonItem = openJsonStackItems.pop();
			if (latestOpenJsonItem != JsonStackItem.Array_Empty && latestOpenJsonItem != JsonStackItem.Array && latestOpenJsonItem != JsonStackItem.Object_Value) {
				openJsonStackItems.push(latestOpenJsonItem);
				throw new IllegalStateException("Not matching open Json item for opening object: " + latestOpenJsonItem);
			} else {
				if (latestOpenJsonItem == JsonStackItem.Array) {
					write("," + linebreak, false);
				} else if (latestOpenJsonItem == JsonStackItem.Array_Empty) {
					write(linebreak, false);
				} else if (latestOpenJsonItem == JsonStackItem.Object_Value) {
					openJsonStackItems.push(JsonStackItem.Object_Value);
					write(linebreak, false);
				}
				
				if (latestOpenJsonItem != JsonStackItem.Object_Value) {
					openJsonStackItems.push(JsonStackItem.Array);
				}
				
				write("{", true);
				openJsonStackItems.push(JsonStackItem.Object_Empty);
			}
		}
	}
	
	public void openJsonObjectProperty(String propertyName) {
		JsonStackItem latestOpenJsonItem = openJsonStackItems.pop();
		if (latestOpenJsonItem != JsonStackItem.Object_Empty && latestOpenJsonItem != JsonStackItem.Object) {
			openJsonStackItems.push(latestOpenJsonItem);
			throw new IllegalStateException("Not matching open Json item for opening object property: " + latestOpenJsonItem);
		} else {
			if (latestOpenJsonItem == JsonStackItem.Object) {
				write("," + linebreak, false);
			} else {
				write(linebreak, false);
			}
			openJsonStackItems.push(JsonStackItem.Object);
			write("\"" + formatStringOutput(propertyName) + "\":", true);
			openJsonStackItems.push(JsonStackItem.Object_Value);
		}
	}
	
	public void addSimpleJsonObjectPropertyValue(Object propertyValue) {
		JsonStackItem latestOpenJsonItem = openJsonStackItems.pop();
		if (latestOpenJsonItem != JsonStackItem.Object_Value) {
			openJsonStackItems.push(latestOpenJsonItem);
			throw new IllegalStateException("Not matching open Json item for adding object property value: " + latestOpenJsonItem);
		} else {
			if (propertyValue == null) {
				write(separator + "null", false);
			} else if (propertyValue instanceof Boolean) {
				write(separator + propertyValue, false);
			} else if (propertyValue instanceof Date dateVal) {
				write(separator + "\"" + new SimpleDateFormat(DateUtilities.ISO_8601_DATETIME_FORMAT).format(dateVal) + "\"", false);
			} else if (propertyValue instanceof Number) {
				write(separator + propertyValue, false);
			} else {
				write(separator + "\"" + formatStringOutput(propertyValue.toString()) + "\"", false);
			}
		}
	}
	
	public void closeJsonObject() {
		JsonStackItem latestOpenJsonItem = openJsonStackItems.pop();
		if (latestOpenJsonItem != JsonStackItem.Object_Empty && latestOpenJsonItem != JsonStackItem.Object) {
			openJsonStackItems.push(latestOpenJsonItem);
			throw new IllegalStateException("Not matching open Json item for closing object: " + latestOpenJsonItem);
		} else if (latestOpenJsonItem == JsonStackItem.Object_Empty) {
			write("}", false);
		} else {
			write(linebreak, false);
			write("}", true);
		}
		
		if (!openJsonStackItems.isEmpty() && openJsonStackItems.peek() == JsonStackItem.Object_Value) {
			openJsonStackItems.pop();
		}
	}
	
	public void openJsonArray() {
		if (outputWriter == null) {
			write("[", true);
			openJsonStackItems.push(JsonStackItem.Array_Empty);
		} else {
			JsonStackItem latestOpenJsonItem = openJsonStackItems.pop();
			if (latestOpenJsonItem != JsonStackItem.Array_Empty && latestOpenJsonItem != JsonStackItem.Array && latestOpenJsonItem != JsonStackItem.Object_Value) {
				openJsonStackItems.push(latestOpenJsonItem);
				throw new IllegalStateException("Not matching open Json item for opening array: " + latestOpenJsonItem);
			} else {
				if (latestOpenJsonItem == JsonStackItem.Array) {
					write("," + linebreak, false);
				} else if (latestOpenJsonItem == JsonStackItem.Array_Empty) {
					write(linebreak, false);
				} else if (latestOpenJsonItem == JsonStackItem.Object_Value) {
					openJsonStackItems.push(JsonStackItem.Object_Value);
					write(linebreak, false);
				}
				
				if (latestOpenJsonItem != JsonStackItem.Object_Value) {
					openJsonStackItems.push(JsonStackItem.Array);
				}
				
				write("[", true);
				openJsonStackItems.push(JsonStackItem.Array_Empty);
			}
		}
	}
	
	public void addSimpleJsonArrayValue(Object arrayValue) {
		JsonStackItem latestOpenJsonItem = openJsonStackItems.pop();
		if (latestOpenJsonItem != JsonStackItem.Array_Empty && latestOpenJsonItem != JsonStackItem.Array) {
			openJsonStackItems.push(latestOpenJsonItem);
			throw new IllegalStateException("Not matching open Json item for adding array value: " + latestOpenJsonItem);
		} else {
			if (latestOpenJsonItem == JsonStackItem.Array) {
				write("," + linebreak, false);
			} else {
				write(linebreak, false);
			}

			openJsonStackItems.push(JsonStackItem.Array);
			
			if (arrayValue == null) {
				write("null", true);
			} else if (arrayValue instanceof Boolean boolVal) {
				write(Boolean.toString(boolVal), true);
			} else if (arrayValue instanceof Date dateVal) {
				write("\"" + new SimpleDateFormat(DateUtilities.ISO_8601_DATETIME_FORMAT).format(dateVal) + "\"", true);
			} else if (arrayValue instanceof Number) {
				write(arrayValue.toString(), true);
			} else {
				write("\"" + formatStringOutput(arrayValue.toString()) + "\"", true);
			}
		}
	}
	
	public void addSimpleValue(Object value) {
		if (!openJsonStackItems.isEmpty()) {
			throw new IllegalStateException("Not matching empty Json output for adding simple value");
		} else {
			if (value == null) {
				write("null", true);
			} else if (value instanceof Boolean boolVal) {
				write(Boolean.toString(boolVal), true);
			} else if (value instanceof Date dateVal) {
				write("\"" + new SimpleDateFormat(DateUtilities.ISO_8601_DATETIME_FORMAT).format(dateVal) + "\"", true);
			} else if (value instanceof Number) {
				write((value).toString(), true);
			} else {
				write("\"" + formatStringOutput(value.toString()) + "\"", true);
			}
		}
	}
	
	public void closeJsonArray() {
		JsonStackItem latestOpenJsonItem = openJsonStackItems.pop();
		if (latestOpenJsonItem != JsonStackItem.Array_Empty && latestOpenJsonItem != JsonStackItem.Array) {
			openJsonStackItems.push(latestOpenJsonItem);
			throw new IllegalStateException("Not matching open Json item for closing array: " + latestOpenJsonItem);
		} else if (latestOpenJsonItem == JsonStackItem.Array_Empty) {
			write("]", false);
		} else {
			write(linebreak, false);
			write("]", true);
		}
		
		if (!openJsonStackItems.isEmpty() && openJsonStackItems.peek() == JsonStackItem.Object_Value) {
			openJsonStackItems.pop();
		}
	}
	
	public void add(JsonObject jsonObject) {
		if (jsonObject == null) {
			throw new IllegalArgumentException("Invalid null value added via 'add'. If done by intention use 'addSimpleJsonArrayValue' or 'addSimpleJsonObjectPropertyValue'");
		} else {
			openJsonObject();
			for (Entry<String, Object> property : jsonObject) {
				openJsonObjectProperty(property.getKey());
				Object propertyValue = property.getValue();
				if (propertyValue instanceof JsonObject jsonObj) {
					add(jsonObj);
				} else if (propertyValue instanceof JsonArray jsonArr) {
					add(jsonArr);
				} else {
					addSimpleJsonObjectPropertyValue(propertyValue);
				}
			}
			closeJsonObject();
		}
	}
	
	public void add(JsonArray jsonArray) {
		if (jsonArray == null) {
			throw new IllegalStateException("Invalid null value added via 'add'. If done by intention use 'addSimpleJsonArrayValue' or 'addSimpleJsonObjectPropertyValue'");
		} else {
			openJsonArray();
			for (Object arrayValue : jsonArray) {
				if (arrayValue instanceof JsonObject jsonObj) {
					add(jsonObj);
				} else if (arrayValue instanceof JsonArray jsonArr) {
					add(jsonArr);
				} else {
					addSimpleJsonArrayValue(arrayValue);
				}
			}
			closeJsonArray();
		}
	}
	
	/**
	 * Flush buffered data.
	 *
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void flush() throws IOException {
		if (outputWriter != null) {
			outputWriter.flush();
		}
	}

	/**
	 * Close this writer and its underlying stream.
	 */
	@Override
	public void close() throws IOException {
		if (outputWriter != null) {
			outputWriter.write(linebreak);
		}
		
		closeQuietly(outputWriter);
		outputWriter = null;
		closeQuietly(outputStream);
		outputStream = null;
		
		if (!openJsonStackItems.isEmpty()) {
			String jsonItemsStackString = "";
			while (!openJsonStackItems.isEmpty()) {
				jsonItemsStackString += "/" + openJsonStackItems.pop().toString();
			}
			throw new IOException("There are still Json items open: " + jsonItemsStackString);
		}
	}
	
	private void write(String text, boolean indent) {
		if (outputWriter == null) {
			if (outputStream == null) {
				throw new IllegalStateException("JsonWriter is already closed");
			}
			outputWriter = new BufferedWriter(new OutputStreamWriter(outputStream, encoding));
		}

		try {
			outputWriter.write((indent ? AgnUtils.repeatString(indention, openJsonStackItems.size()) : "") + text);
		} catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

	/**
	 * Check if String value is null or contains only whitespace characters.
	 *
	 * @param value
	 *            the value
	 * @return true, if is blank
	 */
	private static boolean isBlank(String value) {
		return value == null || value.trim().isEmpty();
	}

	/**
	 * Close a Closable item and ignore any Exception thrown by its close method.
	 *
	 * @param closeableItem
	 *            the closeable item
	 */
	private static void closeQuietly(Closeable closeableItem) {
		if (closeableItem != null) {
			try {
				closeableItem.close();
			} catch (IOException e) {
				// Do nothing
			}
		}
	}
	
	/**
	 * This method should only be used to write small Json items
	 */
	public static String getJsonItemString(JsonObject jsonObject) {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try (JsonWriter jsonWriter = new JsonWriter(outputStream)) {
			jsonWriter.add(jsonObject);
		} catch (IOException e) {
            throw new RuntimeException(e);
        }

        return outputStream.toString(StandardCharsets.UTF_8);
	}
	
	/**
	 * This method should only be used to write small Json items
	 */
	public static String getJsonItemString(JsonArray jsonArray) {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try (JsonWriter jsonWriter = new JsonWriter(outputStream)) {
			jsonWriter.add(jsonArray);
		} catch (IOException e) {
            throw new RuntimeException(e);
        }

        return outputStream.toString(StandardCharsets.UTF_8);
	}
	
	/**
	 * This method should only be used to write small Json items
	 */
	public static String getJsonItemString(JsonObject jsonObject, String linebreak, String indentation, String separator) {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try (JsonWriter jsonWriter = new JsonWriter(outputStream)) {
			jsonWriter.setLinebreak(linebreak);
			jsonWriter.setIndentation(indentation);
			jsonWriter.setSeparator(separator);
			jsonWriter.add(jsonObject);
		} catch (IOException e) {
            throw new RuntimeException(e);
        }

        return outputStream.toString(StandardCharsets.UTF_8);
	}
	
	/**
	 * This method should only be used to write small Json items
	 */
	public static String getJsonItemString(JsonArray jsonArray, String linebreak, String indentation, String separator) {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try (JsonWriter jsonWriter = new JsonWriter(outputStream)) {
			jsonWriter.setLinebreak(linebreak);
			jsonWriter.setIndentation(indentation);
			jsonWriter.setSeparator(separator);
			jsonWriter.add(jsonArray);
		} catch (IOException e) {
            throw new RuntimeException(e);
        }

        return outputStream.toString(StandardCharsets.UTF_8);
	}
	
	public static String getJsonItemString(JsonNode jsonNode) {
		return getJsonItemString(jsonNode, "\n", "\t", " ");
	}
	
	/**
	 * This method should only be used to write small Json items
	 */
	public static String getJsonItemString(JsonNode jsonNode, String linebreak, String indentation, String separator) {
		if (jsonNode.isJsonObject()) {
			return getJsonItemString((JsonObject) jsonNode.getValue(), linebreak, indentation, separator);
		} else if (jsonNode.isJsonArray()) {
			return getJsonItemString((JsonArray) jsonNode.getValue(), linebreak, indentation, separator);
		} else if (jsonNode.isNull()) {
			return "null";
		} else {
			return jsonNode.getValue().toString();
		}
	}
	
	public String formatStringOutput(String value) {
		return value
				.replace("\\", "\\\\")
				.replace("\"", "\\\"")
				.replace("\r\n", "\n")
				.replace("\r", "\n")
				.replace("\n", "\\n")
				.replace("\t", "\\t");
	}
}
