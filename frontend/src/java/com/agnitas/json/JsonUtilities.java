/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.json;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.agnitas.util.DateUtilities;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import net.sf.json.JsonConfig;
import net.sf.json.processors.DefaultValueProcessorMatcher;

public class JsonUtilities {
	public static JsonObject convertXmlDocument(Document xmlDocument, boolean throwExceptionOnError) throws Exception {
		try {
			JsonObject jsonObject = new JsonObject();
			jsonObject.add(xmlDocument.getChildNodes().item(0).getNodeName(), convertXmlNode(xmlDocument.getChildNodes().item(0)));
			return jsonObject;
		} catch (Exception e) {
			if (throwExceptionOnError) {
				throw new Exception("Invalid data", e);
			} else {
				return null;
			}
		}
	}

	public static JsonObject convertXmlNode(Node xmlNode) {
		JsonObject jsonObject = new JsonObject();
		if (xmlNode.getAttributes() != null && xmlNode.getAttributes().getLength() > 0) {
			for (int attributeIndex = 0; attributeIndex < xmlNode.getAttributes().getLength(); attributeIndex++) {
				Node attributeNode = xmlNode.getAttributes().item(attributeIndex);
				jsonObject.add(attributeNode.getNodeName(), attributeNode.getNodeValue());
			}
		}
		if (xmlNode.getChildNodes() != null && xmlNode.getChildNodes().getLength() > 0) {
			for (int i = 0; i < xmlNode.getChildNodes().getLength(); i++) {
				Node childNode = xmlNode.getChildNodes().item(i);
				if (childNode.getNodeType() == Node.TEXT_NODE) {
					if (StringUtils.isNotBlank(childNode.getNodeValue())) {
						jsonObject.add("text", childNode.getNodeValue());
					}
				} else if (childNode.getNodeType() == Node.COMMENT_NODE) {
					// do nothing
				} else if (childNode.getChildNodes().getLength() == 1 && childNode.getChildNodes().item(0).getNodeType() == Node.TEXT_NODE) {
					// only one textnode under this node
					jsonObject.add(childNode.getNodeName(), childNode.getChildNodes().item(0).getNodeValue());
				} else {
					Node xmlSubNode = childNode;
					JsonObject nodeJsonObject = convertXmlNode(xmlSubNode);
					if (nodeJsonObject != null) {
						jsonObject.add(xmlSubNode.getNodeName(), nodeJsonObject);
					}
				}
			}
		}
		return jsonObject;
	}

	public static Document convertToXmlDocument(JsonNode jsonNode, boolean useAttributes) throws Exception {
		try {
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			Document xmlDocument = documentBuilder.newDocument();
			xmlDocument.setXmlStandalone(true);
			List<Node> mainNodes;
			if (jsonNode.isJsonObject()) {
				mainNodes = convertToXmlNodes((JsonObject) jsonNode.getValue(), xmlDocument, useAttributes);
				if (mainNodes == null || mainNodes.size() < 1) {
					throw new Exception("No data found");
				} else if (mainNodes.size() == 1) {
					xmlDocument.appendChild(mainNodes.get(0));
				} else {
					Node rootNode = xmlDocument.createElement("root");
					for (Node subNode : mainNodes) {
						if (subNode instanceof Attr) {
							rootNode.getAttributes().setNamedItem(subNode);
						} else {
							rootNode.appendChild(subNode);
						}
					}
					xmlDocument.appendChild(rootNode);
				}
			} else if (jsonNode.isJsonArray()) {
				mainNodes = convertToXmlNodes((JsonArray) jsonNode.getValue(), "root", xmlDocument, useAttributes);
				if (mainNodes == null || mainNodes.size() < 1) {
					throw new Exception("No data found");
				} else if (mainNodes.size() == 1) {
					xmlDocument.appendChild(mainNodes.get(0));
				} else {
					Node rootNode = xmlDocument.createElement("root");
					for (Node subNode : mainNodes) {
						if (subNode instanceof Attr) {
							rootNode.getAttributes().setNamedItem(subNode);
						} else {
							rootNode.appendChild(subNode);
						}
					}
					xmlDocument.appendChild(rootNode);
				}
			} else if (jsonNode.isNull()) {
				Node rootNode = xmlDocument.createElement("root");
				rootNode.setTextContent("null");
				xmlDocument.appendChild(rootNode);
			} else {
				Node rootNode = xmlDocument.createElement("root");
				rootNode.setTextContent(jsonNode.getValue().toString());
				xmlDocument.appendChild(rootNode);
			}
			
			return xmlDocument;
		} catch (Exception e) {
			throw new Exception("Invalid data", e);
		}
	}

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
	 * 
	 * @param jsonReader
	 * @param jsonPath
	 * @throws Exception
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

	public static JsonNode validateJson(byte[] jsonData, String encoding) throws Exception {
		try (JsonReader jsonReader = new JsonReader(new ByteArrayInputStream(jsonData), encoding)) {
			return jsonReader.read();
		}
	}

	/**
	 * The shortcut for {@link #useNulls(JsonConfig)}.
	 *
	 * @return a {@link JsonConfig} instance.
	 */
	public static JsonConfig useNulls() {
		return useNulls(new JsonConfig());
	}

	/**
	 * Change the default value processing so {@code null} values are represented "as is" (not replaced with stubs like
	 * empty list or zeros).
	 *
	 * @param config a {@link JsonConfig} instance to change.
	 * @return a {@link JsonConfig} instance passes as an argument.
	 */
	public static JsonConfig useNulls(JsonConfig config) {
		config.registerDefaultValueProcessor(Object.class, type -> null);
		config.setDefaultValueProcessorMatcher(new DefaultValueProcessorMatcher() {
			@SuppressWarnings("rawtypes")
			@Override
			public Object getMatch(Class type, Set set) {
				for (Object o : set) {
					Class<?> candidate = (Class<?>) o;
					if (candidate.isAssignableFrom(type)) {
						return candidate;
					}
				}
				return null;
			}
		});
		return config;
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
}
