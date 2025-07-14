/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.json;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.regex.Pattern;

import com.agnitas.util.AgnUtils;

/**
 * Differences to standard JSON:<br />
 * Objects:<br />
 * 	Unquoted Object keys<br />
 * 	Single-quoted Object keys<br />
 *  Objects properties list may have trailing comma<br />
 * Arrays:<br />
 * 	Arrays item list may have trailing comma<br />
 * Strings:<br />
 * 	Single-quoted Strings<br />
 * 	Strings can be split across multiple lines (escape newlines with backslashes)<br />
 * Numbers:<br />
 * 	Hexadecimal numbers as unquoted values<br />
 * 	Numbers may begin or end with a (leading or trailing) decimal point<br />
 * 	Numbers include Infinity, -Infinity, NaN, and -NaN<br />
 * 	Numbers may begin with an explicit plus sign<br />
 * Comments:<br />
 * 	Inline comment (single-line up to the line end)<br />
 * 	Block comment (multi-line)<br />
 */
public class Json5Reader extends JsonReader {
	public Json5Reader(InputStream inputStream) throws Exception {
		this(inputStream, (String) null);
	}
	
	public Json5Reader(InputStream inputStream, String encoding) throws Exception {
		super(inputStream, encoding);
	}
	
	public Json5Reader(InputStream inputStream, Charset encodingCharset) throws Exception {
		super(inputStream, encodingCharset);
	}

	@Override
	protected JsonToken readNextTokenInternal() throws Exception {
		currentObject = null;
		Character currentChar = readNextNonWhitespace();
		if (currentChar == null) {
			if (openJsonItems.size() > 0) {
				throw new Exception("Premature end of data");
			} else {
				return null;
			}
		}
		
		JsonToken jsonToken;
		switch (currentChar) {
			case '{': // Open JsonObject
				if (openJsonItems.size() > 0 && openJsonItems.peek() == JsonToken.JsonObject_PropertyKey) {
					openJsonItems.pop();
				}
				openJsonItems.push(JsonToken.JsonObject_Open);
				jsonToken = JsonToken.JsonObject_Open;
				break;
			case '}': // Close JsonObject
				if (openJsonItems.pop() != JsonToken.JsonObject_Open) {
					throw new Exception("Invalid json data '" + currentChar + "' in line " + getReadLines() + " at overall index " + getReadCharacters());
				} else {
					jsonToken = JsonToken.JsonObject_Close;
				}
				break;
			case '[': // Open JsonArray
				if (openJsonItems.size() > 0 && openJsonItems.peek() == JsonToken.JsonObject_PropertyKey) {
					openJsonItems.pop();
				}
				openJsonItems.push(JsonToken.JsonArray_Open);
				jsonToken = JsonToken.JsonArray_Open;
				break;
			case ']': // Close JsonArray
				if (openJsonItems.pop() != JsonToken.JsonArray_Open) {
					throw new Exception("Invalid json data '" + currentChar + "' in line " + getReadLines() + " at overall index " + getReadCharacters());
				} else {
					jsonToken = JsonToken.JsonArray_Close;
				}
				break;
			case ',': // Separator of JsonObject properties or JsonArray items
				jsonToken = readNextTokenInternal();
				break;
			case '/': // Start comment
				currentChar = readNextCharacter();
				if (currentChar == null) {
					throw new Exception("Invalid json data '" + currentChar + "' in line " + getReadLines() + " at overall index " + getReadCharacters());
				} else if (currentChar == '/') {
					// Line comment
					readUpToNext(false, null, '\n');
				} else if (currentChar == '*') {
					// Block comment
					while ((currentChar = readNextCharacter()) != '/') {
						readUpToNext(true, null, '*');
					}
				} else {
					throw new Exception("Invalid json data '" + currentChar + "' in line " + getReadLines() + " at overall index " + getReadCharacters());
				}
				jsonToken = readNextTokenInternal();
				break;
			case '"':
			case '\'': // Start JsonObject propertykey or propertyvalue or JsonArray item
				if (openJsonItems.size() == 0) {
					currentObject = readQuotedText(currentChar, '\\');
					jsonToken = JsonToken.JsonSimpleValue;
				} else if (openJsonItems.peek() == JsonToken.JsonArray_Open) {
					currentObject = readQuotedText(currentChar, '\\');
					jsonToken = JsonToken.JsonSimpleValue;
				} else if (openJsonItems.peek() == JsonToken.JsonObject_Open) {
					currentObject = readQuotedText(currentChar, '\\');
					currentChar = readNextNonWhitespace();
					if (currentChar != ':') {
						throw new Exception("Invalid json data '" + currentChar + "' in line " + getReadLines() + " at overall index " + getReadCharacters());
					}
					openJsonItems.push(JsonToken.JsonObject_PropertyKey);
					jsonToken = JsonToken.JsonObject_PropertyKey;
				} else if (openJsonItems.peek() == JsonToken.JsonObject_PropertyKey) {
					currentObject = readQuotedText(currentChar, '\\');
					openJsonItems.pop();
					currentChar = readNextNonWhitespace();
					if (currentChar == null) {
						throw new Exception("Invalid json data '" + currentChar + "' in line " + getReadLines() + " at overall index " + getReadCharacters());
					} else if (currentChar == '}') {
						reuseCurrentChar();
					} else if (currentChar != ',') {
						throw new Exception("Invalid json data '" + currentChar + "' in line " + getReadLines() + " at overall index " + getReadCharacters());
					}
					jsonToken = JsonToken.JsonSimpleValue;
				} else {
					throw new Exception("Invalid json data '" + currentChar + "' in line " + getReadLines() + " at overall index " + getReadCharacters());
				}
				break;
			default: // Start JsonObject propertykey or propertyvalue or JsonArray item
				if (openJsonItems.size() == 0) {
					currentObject = readSimpleJsonValue(readUpToNext(false, null).trim());
					jsonToken = JsonToken.JsonSimpleValue;
				} else if (openJsonItems.peek() == JsonToken.JsonObject_Open) {
					currentObject = readJsonIdentifier(readUpToNext(false, null, ':').trim());
					readNextCharacter();
					openJsonItems.push(JsonToken.JsonObject_PropertyKey);
					jsonToken = JsonToken.JsonObject_PropertyKey;
				} else if (openJsonItems.peek() == JsonToken.JsonObject_PropertyKey) {
					openJsonItems.pop();
					currentObject = readSimpleJsonValue(readUpToNext(false, null, ',', '}').trim());
					Character nextCharAfterSimpleValue = readNextNonWhitespace();
					if (nextCharAfterSimpleValue == null) {
						throw new Exception("Invalid json data '" + currentChar + "' in line " + getReadLines() + " at overall index " + getReadCharacters());
					} else if (nextCharAfterSimpleValue == '}') {
						reuseCurrentChar();
					}
					jsonToken = JsonToken.JsonSimpleValue;
				} else if (openJsonItems.peek() == JsonToken.JsonArray_Open) {
					currentObject = readSimpleJsonValue(readUpToNext(false, null, ',', ']').trim());
					char nextCharAfterSimpleValue = readNextNonWhitespace();
					if (nextCharAfterSimpleValue == ']') {
						reuseCurrentChar();
					}
					jsonToken = JsonToken.JsonSimpleValue;
				} else {
					throw new Exception("Invalid json data '" + currentChar + "' in line " + getReadLines() + " at overall index " + getReadCharacters());
				}
				break;
		}
		
		return jsonToken;
	}

	private Object readSimpleJsonValue(String valueString) throws Exception {
		if (valueString.equalsIgnoreCase("null")) {
			return null;
		} else if (valueString.equalsIgnoreCase("true")) {
			return true;
		} else if (valueString.equalsIgnoreCase("false")) {
			return false;
		} else if (valueString.equalsIgnoreCase("Infinity")) {
			return Double.POSITIVE_INFINITY;
		} else if (valueString.equalsIgnoreCase("-Infinity")) {
			return Double.NEGATIVE_INFINITY;
		} else if (valueString.equalsIgnoreCase("NaN")) {
			return Double.NaN;
		} else if (valueString.equalsIgnoreCase("-NaN")) {
			return Double.NaN;
		} else if (isHexNumber(valueString)) {
			return parseHexNumber(valueString);
		} else if (AgnUtils.isDouble(valueString)) {
			return AgnUtils.parseNumber(valueString);
		} else {
			throw new Exception("Invalid json data '" + shortenStringToMaxLengthCutLeft(valueString, 20) + "' in line " + getReadLines() + " at overall index " + getReadCharacters());
		}
	}

	/**
	 * A valid identifier must
	 * 	start with a letter (A-Za-z), underscore (_) or dollar sign ($)
	 * 	subsequent characters can also be digits (0-9)
	 * 
	 * @param identifierString
	 * @return
	 * @throws Exception
	 */
	private String readJsonIdentifier(String identifierString) throws Exception {
		if (Pattern.matches("[A-Za-z_$]+[A-Za-z_$0-9]*", identifierString)) {
			return identifierString;
		} else {
			throw new Exception("Invalid json identifier '" + shortenStringToMaxLengthCutLeft(identifierString, 20) + "' in line " + getReadLines() + " at overall index " + getReadCharacters());
		}
	}
	
	/**
	 * This method should only be used to read small Json items
	 * 
	 * @param data
	 * @return
	 * @throws IOException
	 * @throws UnsupportedEncodingException
	 */
	public static JsonNode readJsonItemString(String data) throws Exception {
		try (ByteArrayInputStream inputStream = new ByteArrayInputStream(data.getBytes("UTF-8"))) {
			try (Json5Reader jsonReader = new Json5Reader(inputStream)) {
				return jsonReader.read();
			}
		}
	}

	private static String shortenStringToMaxLengthCutLeft(String value, int maxLength) {
		if (value != null && value.length() > maxLength) {
			return "... " + value.substring((value.length() - maxLength) + 4);
		} else {
			return value;
		}
	}

	private static boolean isHexNumber(String numberString) {
		return Pattern.matches("0(x|X)[0-9A-Fa-f]+", numberString);
	}
	
	private static Number parseHexNumber(String hexNumberString) throws NumberFormatException {
		if (!isHexNumber(hexNumberString)) {
			throw new NumberFormatException("Not a hex number: '" + hexNumberString + "'");
		} else {
			if (hexNumberString.length() < 12) {
				return Integer.parseInt(hexNumberString.substring(2), 16);
			} else {
				BigInteger value = new BigInteger(hexNumberString.substring(2), 16);
				boolean isInteger = BigInteger.valueOf(Integer.MIN_VALUE).compareTo(value) < 0 && value.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) < 0;
				if (isInteger) {
					return Integer.parseInt(hexNumberString.substring(2), 16);
				} else {
					boolean isLong = BigInteger.valueOf(Long.MIN_VALUE).compareTo(value) < 0 && value.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) < 0;
					if (isLong) {
						return Long.parseLong(hexNumberString.substring(2), 16);
					} else {
						return value;
					}
				}
			}
		}
	}
}
