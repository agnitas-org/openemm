/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.json;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.agnitas.util.DateUtilities;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class JsonUtilities {

    private static final Logger logger = LogManager.getLogger(JsonUtilities.class);
    
	public static List<Node> convertToXmlNodes(JsonObject jsonObject, Document xmlDocument, boolean useAttributes) {
		List<Node> list = new ArrayList<>();

		for (Entry<String, Object> entry : jsonObject.entrySet()) {
			String key = entry.getKey();
			Object subItem = entry.getValue();
			if (subItem instanceof JsonObject) {
				Node newNode = xmlDocument.createElement(key);
				list.add(newNode);
				for (Node subNode : convertToXmlNodes((JsonObject) subItem, xmlDocument, useAttributes)) {
					if (subNode instanceof Attr) {
						newNode.getAttributes().setNamedItem(subNode);
					} else {
						newNode.appendChild(subNode);
					}
				}
			} else if (subItem instanceof JsonArray) {
				for (Node subNode : convertToXmlNodes((JsonArray) subItem, key, xmlDocument, useAttributes)) {
					list.add(subNode);
				}
			} else if (useAttributes) {
				Attr newAttr = xmlDocument.createAttribute(key);
				newAttr.setNodeValue(subItem.toString());
				list.add(newAttr);
			} else {
				Node newNode = xmlDocument.createElement(key);
				list.add(newNode);
				newNode.setTextContent(subItem.toString());
			}
		}

		return list;
	}

	public static List<Node> convertToXmlNodes(JsonArray jsonArray, String nodeName, Document xmlDocument, boolean useAttributes) {
		List<Node> list = new ArrayList<>();

		if (jsonArray.size() > 0) {
			for (Object subItem : jsonArray) {
				if (subItem instanceof JsonObject) {
					Node newNode = xmlDocument.createElement(nodeName);
					list.add(newNode);
					for (Node subNode : convertToXmlNodes((JsonObject) subItem, xmlDocument, useAttributes)) {
						if (subNode instanceof Attr) {
							newNode.getAttributes().setNamedItem(subNode);
						} else {
							newNode.appendChild(subNode);
						}
					}
				} else if (subItem instanceof JsonArray) {
					Node newNode = xmlDocument.createElement(nodeName);
					list.add(newNode);
					for (Node subNode : convertToXmlNodes((JsonArray) subItem, nodeName, xmlDocument, useAttributes)) {
						newNode.appendChild(subNode);
					}
				} else {
					Node newNode = xmlDocument.createElement(nodeName);
					list.add(newNode);
					newNode.setTextContent(subItem.toString());
				}
			}
		} else {
			Node newNode = xmlDocument.createElement(nodeName);
			list.add(newNode);
		}

		return list;
	}
	
	/**
	 * JsonPath syntax:<br />
	 *	$ : root<br />
	 *	. or / : child separator<br />
	 *	[n] : array operator<br />
	 *<br />
	 * JsonPath example:<br />
	 * 	"$.list.customer[0].name"<br />
	 */
	public static void readUpToJsonPath(JsonReader jsonReader, String jsonPath) throws Exception {
		if (jsonPath.startsWith("/") || jsonPath.startsWith("$")) {
			jsonPath = jsonPath.substring(1);
		}
		if (jsonPath.endsWith("/")) {
			jsonPath = jsonPath.substring(0, jsonPath.length() - 1);
		}
		jsonPath = "$" + jsonPath.replace("/", ".");
		
		while (jsonReader.readNextToken() != null && !jsonReader.getCurrentJsonPath().equals(jsonPath)) {
			// nothing to do
		}

		if (!jsonReader.getCurrentJsonPath().equals(jsonPath)) {
			throw new Exception("Path '" + jsonPath + "' is not part of the JSON data");
		}
	}

	public static ObjectMapper getObjectMapper(TimeZone timezone) {
		return getObjectMapper(timezone, DateUtilities.ISO_8601_DATETIME_FORMAT);
	}

	public static ObjectMapper getObjectMapper(TimeZone timezone, String dateFormatPattern) {
		ObjectMapper mapper = new ObjectMapper();

		mapper.setTimeZone(timezone);
		mapper.setDateFormat(DateUtilities.getFormat(dateFormatPattern, timezone));
		mapper.registerModule(new LocalDateTimeModule());
		mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

		return mapper;
	}
    
    public static Map<String, Object> strToMap(String json) {
        try {
            return new ObjectMapper().readValue(json, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            logger.error("Cant convert json string to a map: {}", e.getMessage(), e);            
            return Collections.emptyMap();
        }
    }

    public static String mapToStr(Map<String, Object> json) {
        try {
            return new JsonMapper().writeValueAsString(json);
        } catch (JsonProcessingException e) {
            logger.error("Cant convert a map to json string: {}", e.getMessage(), e);
            return "";
        }
    }
}
