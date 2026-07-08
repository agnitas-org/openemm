/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class XmlUtilities {

	private XmlUtilities() {

	}

	private static final Logger logger = LogManager.getLogger(XmlUtilities.class);

	/**
	 * @see <a href="https://www.owasp.org/index.php/XML_External_Entity_(XXE)_Prevention_Cheat_Sheet#JAXP_DocumentBuilderFactory.2C_SAXParserFactory_and_DOM4J">OWASP</a>
	 */
	public static final String EXTERNAL_GENERAL_ENTITIES_FEATURE = "http://xml.org/sax/features/external-general-entities";

	/**
	 * @see <a href="https://www.owasp.org/index.php/XML_External_Entity_(XXE)_Prevention_Cheat_Sheet#JAXP_DocumentBuilderFactory.2C_SAXParserFactory_and_DOM4J">OWASP</a>
	 */
	public static final String LOAD_EXTERNAL_DTD_FEATURE ="http://xml.org/sax/features/external-parameter-entities";

	/**
	 * @see <a href="https://www.owasp.org/index.php/XML_External_Entity_(XXE)_Prevention_Cheat_Sheet#JAXP_DocumentBuilderFactory.2C_SAXParserFactory_and_DOM4J">OWASP</a>
	 */
	public static final String EXTENRAL_PARAMETER_ENTITIES_FEATURE ="http://xml.org/sax/features/external-parameter-entities";

	/**
	 * Returns the content of a simple text tag If there are more than one text node or other nodetypes it will return null
	 */
	public static String getSimpleTextValueFromNode(Node node) {
		if (node != null && node.getChildNodes().getLength() == 1 && node.getChildNodes().item(0).getNodeType() == Node.TEXT_NODE) {
			return node.getChildNodes().item(0).getTextContent();
		} else {
			return null;
		}
	}

	public static String getAttributeValue(Node pNode, String pAttributeName) {
		String returnString = null;

		NamedNodeMap attributes = pNode.getAttributes();
		if (attributes != null) {
			for (int i = 0; i < attributes.getLength(); i++) {
				if (attributes.item(i).getNodeName().equalsIgnoreCase(pAttributeName)) {
					returnString = attributes.item(i).getNodeValue();
					break;
				}
			}
		}

		return returnString;
	}

	public static Map<String, String> getAttributes(Node node) {
		Map<String, String> returnMap = new HashMap<>();

		NamedNodeMap attributes = node.getAttributes();
		if (attributes != null) {
			for (int i = 0; i < attributes.getLength(); i++) {
				returnMap.put(attributes.item(i).getNodeName(), attributes.item(i).getNodeValue());
			}
		}

		return returnMap;
	}

	public static String getNodeValue(Node pNode) {
		if (pNode.getNodeValue() != null) {
			return pNode.getNodeValue();
		} else if (pNode.getFirstChild() != null) {
			return getNodeValue(pNode.getFirstChild());
		} else {
			return null;
		}
	}

	public static Document parseXMLDataAndXSDVerifyByDOM(byte[] pData, String byteEncoding, String xsdFileName) throws Exception {
		return parseXMLDataAndXSDVerifyByDOM(pData, byteEncoding, xsdFileName, false);
	}

	public static Document parseXMLDataAndXSDVerifyByDOM(byte[] pData, String byteEncoding, String xsdFileName, boolean ignoreAnyDtdDefinition) throws Exception {
		try {
			if (pData == null) {
				return null;
			}

			if (pData[pData.length - 1] == 0) {
				pData = new String(pData, StandardCharsets.UTF_8).trim().getBytes(StandardCharsets.UTF_8);
			}

			final DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
			if (docBuilderFactory == null) {
				throw new Exception("DocumentBuilderFactory error");
			}
			docBuilderFactory.setFeature(EXTERNAL_GENERAL_ENTITIES_FEATURE, false);
			docBuilderFactory.setFeature(LOAD_EXTERNAL_DTD_FEATURE, false);
			docBuilderFactory.setFeature(EXTENRAL_PARAMETER_ENTITIES_FEATURE, false);
			docBuilderFactory.setNamespaceAware(true);

			if (xsdFileName != null) {
                docBuilderFactory.setValidating(true);
				docBuilderFactory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaLanguage", "http://www.w3.org/2001/XMLSchema");
				docBuilderFactory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaSource", xsdFileName);
			}

			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();

			if (ignoreAnyDtdDefinition) {
				docBuilder.setEntityResolver((publicId, systemId) ->
						systemId.endsWith(".dtd") ? new InputSource(new StringReader("")) : null);
			}

			InputSource inputSource = new InputSource(new ByteArrayInputStream(pData));
			if (byteEncoding != null) {
				inputSource.setEncoding(byteEncoding);
			}
			ParseErrorHandler errorHandler = new ParseErrorHandler();
			docBuilder.setErrorHandler(errorHandler);

			Document xmlDocument = docBuilder.parse(inputSource);

			if (errorHandler.problemsOccurred()) {
				throw new Exception("ErrorConstGlobals.XML_SCHEMA_ERROR " + xsdFileName + " " + errorHandler.getMessage());
			} else {
				return xmlDocument;
			}
		} catch (ParserConfigurationException | SAXException e) {
			logger.error(e.getClass().getSimpleName(), e);
			throw new Exception("ErrorConstException.XML_PROCESSING " + e.getClass().getSimpleName() + " " + e.getMessage(), e);
		} catch (IOException e) {
            logger.error("{} während der XML-Verarbeitung", e.getClass().getSimpleName(), e);
			throw new Exception("ErrorConstException.XML_PROCESSING " + e.getClass().getSimpleName() + " " + e.getMessage(), e);
		}
	}

	private static class ParseErrorHandler implements ErrorHandler {
		ArrayList<SAXParseException> warnings = null;
		ArrayList<SAXParseException> errors = null;
		ArrayList<SAXParseException> fatalErrors = null;

		boolean problems = false;

		public boolean problemsOccurred() {
			return problems;
		}

		@Override
		public void warning(SAXParseException exception) {
			problems = true;
			if (warnings == null) {
				warnings = new ArrayList<>();
			}
			warnings.add(exception);
		}

		@Override
		public void error(SAXParseException exception) {
			problems = true;
			if (errors == null) {
				errors = new ArrayList<>();
			}
			errors.add(exception);
		}

		@Override
		public void fatalError(SAXParseException exception) {
			problems = true;
			if (fatalErrors == null) {
				fatalErrors = new ArrayList<>();
			}
			fatalErrors.add(exception);
		}

		public String getMessage() {
			if (fatalErrors != null && !fatalErrors.isEmpty()) {
				return fatalErrors.get(0).getMessage();
			} else if (errors != null && !errors.isEmpty()) {
				return errors.get(0).getMessage();
			} else if (warnings != null && !warnings.isEmpty()) {
				return warnings.get(0).getMessage();
			} else {
				return "No ParserErrors occured";
			}
		}
	}

	public static String convertXML2String(Document pDocument, String encoding) throws Exception {
		try (StringWriter writer = new StringWriter()) {
            Transformer transformer = newXxeProtectedTransformerFactory().newTransformer();

			if (encoding != null) {
				transformer.setOutputProperty(OutputKeys.ENCODING, encoding);
			} else {
				transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			}

			DOMSource domSource = new DOMSource(pDocument);
			StreamResult result = new StreamResult(writer);

			transformer.transform(domSource, result);

			return writer.toString();
		}
	}

	public static byte[] convertXML2ByteArray(Node pDocument, String encoding) throws Exception {
		try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Transformer transformer = newXxeProtectedTransformerFactory().newTransformer();

			if (encoding != null) {
				transformer.setOutputProperty(OutputKeys.ENCODING, encoding);
			} else {
				transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			}

			DOMSource domSource = new DOMSource(pDocument);
			StreamResult result = new StreamResult(outputStream);

			transformer.transform(domSource, result);

			return outputStream.toByteArray();
		}
	}

	public static Element createRootTagNode(Document baseDocument, String rootTagName) {
		Element newNode = baseDocument.createElement(rootTagName);
		baseDocument.appendChild(newNode);
		return newNode;
	}

	public static Element appendNode(Node baseNode, String tagName) {
		Element newNode = baseNode.getOwnerDocument().createElement(tagName);
		baseNode.appendChild(newNode);
		return newNode;
	}

	public static Element appendTextValueNode(Node baseNode, String tagName, int tagValue) {
		return appendTextValueNode(baseNode, tagName, Integer.toString(tagValue));
	}

	public static Element appendTextValueNode(Node baseNode, String tagName, String tagValue) {
		Element newNode = appendNode(baseNode, tagName);
		if (tagValue == null) {
			newNode.appendChild(baseNode.getOwnerDocument().createTextNode("<null>"));
		} else {
			newNode.appendChild(baseNode.getOwnerDocument().createTextNode(tagValue));
		}
		return newNode;
	}

	public static void appendAttribute(Element baseNode, String attributeName, boolean attributeValue) {
		appendAttribute(baseNode, attributeName, attributeValue ? "true" : "false");
	}

	public static void appendAttribute(Element baseNode, String attributeName, int attributeValue) {
		appendAttribute(baseNode, attributeName, Integer.toString(attributeValue));
	}

	public static void appendAttribute(Element baseNode, String attributeName, String attributeValue) {
		Attr typeAttribute = baseNode.getOwnerDocument().createAttribute(attributeName);
		if (attributeValue == null) {
			typeAttribute.setNodeValue("<null>");
		} else {
			typeAttribute.setNodeValue(attributeValue);
		}
		baseNode.setAttributeNode(typeAttribute);
	}

	public static void removeAttribute(Element baseNode, String attributeName) {
		baseNode.getAttributes().removeNamedItem(attributeName);
	}

	/**
	 * Returns all direct simple text value subnodes and their values for a dataNode
	 * 
	 * Example XML:
	 * 	<dataNode>
	 * 		<a>1</a>
	 *      <b>2</b>
	 *      <c>3</c>
	 * 	</dataNode>
	 * Returns: a=1, b=2, c=3
	 * 
	 */
	public static Map<String, String> getSimpleValuesOfNode(Node dataNode) {
		Map<String, String> returnMap = new HashMap<>();
		
		NodeList list = dataNode.getChildNodes();
		for (int i = 0; i < list.getLength(); i++) {
			Node node = list.item(i);
			if (node.getFirstChild() != null && node.getFirstChild().getNodeType() == Node.TEXT_NODE && node.getChildNodes().getLength() == 1) {
				// <nodename>Value of node</nodename>
				returnMap.put(node.getNodeName(), node.getFirstChild().getNodeValue());
			} else if (node.getFirstChild() != null && node.getFirstChild().getNodeType() == Node.CDATA_SECTION_NODE && node.getChildNodes().getLength() == 1) {
				// <nodename><![CDATA[Some Text including XMl <innerXml /> ]]></nodename>
				returnMap.put(node.getNodeName(), node.getFirstChild().getNodeValue());
			} else if (node.getNodeType() == Node.ELEMENT_NODE && node.getChildNodes().getLength() == 0) {
				// <nodename />
				returnMap.put(node.getNodeName(), "");
			}
		}
		
		return returnMap;
	}
	
	public static List<Node> getSubNodes(Node dataNode) {
		List<Node> returnList = new ArrayList<>();
		
		NodeList list = dataNode.getChildNodes();
		for (int i = 0; i < list.getLength(); i++) {
			Node node = list.item(i);
			returnList.add(node);
		}
		
		return returnList;
	}
	
	public static Node getSubNodeByName(Node dataNode, String subNodeName) {
		NodeList list = dataNode.getChildNodes();
		for (int i = 0; i < list.getLength(); i++) {
			Node node = list.item(i);
			if (node.getNodeName().equals(subNodeName)) {
				return node;
			}
		}
		
		return null;
	}

	/**
	 * Creates a new {@link DocumentBuilderFactory} which is hardened against XXE attacks.
	 * 
	 * @return {@link DocumentBuilderFactory}
	 */
	public static DocumentBuilderFactory newXxeProtectedDocumentBuilderFactory() {
		final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		
		dbf.setExpandEntityReferences(false);

		dbf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
		dbf.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");

		return dbf;
	}
	
	/**
	 * Creates a new {@link TransformerFactory} which is hardened against XXE attacks.
	 * 
	 * @return {@link TransformerFactory}
	 */
	public static TransformerFactory newXxeProtectedTransformerFactory() {
		final TransformerFactory tf = TransformerFactory.newInstance();

		tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
		tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
		
		return tf;
	}

}
