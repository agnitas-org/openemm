/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.restful;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.agnitas.util.DateUtilities;
import org.agnitas.util.XmlUtilities;
import org.apache.log4j.Logger;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class XmlRequestResponse extends BaseRequestResponse {
	private static final transient Logger logger = Logger.getLogger(XmlRequestResponse.class);
	
	private static final String RESPONSE_ENCODING = "UTF-8";

	/**
	 * XML data constants
	 */
	private static final String XMLSTRING_RESPONSE = "Response";
	private static final String XMLSTRING_STATE = "State";
	protected static final String XMLSTRING_EXCEPTION = "Exception";
	protected static final String XMLSTRING_ERROR_CODE = "ErrorCode";
	protected static final String XMLSTRING_EXCEPTION_MSG = "ExceptionMessage";
	protected static final String XMLSTRING_EXCEPTION_DETAIL = "ExceptionDetail";
	protected static final String XMLSTRING_ERROR_TIME = "ErrorTime";

	@Override
	public String getString() throws Exception {
		try {
			DocumentBuilderFactory docBuilderFactory = XmlUtilities.newXxeProtectedDocumentBuilderFactory();
			if (docBuilderFactory == null) {
				throw new Exception("Cannot create request response data: DocumentBuilderFactory.newInstance was null");
			}

			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			if (docBuilder == null) {
				throw new Exception("Cannot create request response data: factory.newDocumentBuilder was null");
			}
			
			Document xmlDocument = docBuilder.newDocument();
			xmlDocument.setXmlStandalone(true);
			
			Element rootElement = XmlUtilities.createRootTagNode(xmlDocument, XMLSTRING_RESPONSE);
			XmlUtilities.appendTextValueNode(rootElement, XMLSTRING_STATE, Integer.toString(responseState.getStateCode()));
			
			if (error != null) {
				Node exceptionNode = XmlUtilities.appendNode(rootElement, XMLSTRING_EXCEPTION);
				XmlUtilities.appendTextValueNode(exceptionNode, XMLSTRING_ERROR_CODE, errorCode.getCode());
				XmlUtilities.appendTextValueNode(exceptionNode, XMLSTRING_EXCEPTION_MSG, error.getMessage());
				if (error.getCause() != null && error.getCause() != error) {
					XmlUtilities.appendTextValueNode(exceptionNode, XMLSTRING_EXCEPTION_DETAIL, error.getCause().getClass().getSimpleName() + ": " + error.getCause().getMessage());
				}
				XmlUtilities.appendTextValueNode(exceptionNode, XMLSTRING_ERROR_TIME, new SimpleDateFormat(DateUtilities.ISO_8601_DATETIME_FORMAT).format(new Date()));
			}
			
			String responseXmlString = XmlUtilities.convertXML2String(xmlDocument, RESPONSE_ENCODING);
			
			logger.debug(responseXmlString);
			return responseXmlString;
		} catch (FactoryConfigurationError e) {
			logger.error(e.getMessage(), e);
			throw new Exception("Cannot create request response data: FactoryConfigurationError-Exception", e);
		} catch (ParserConfigurationException e) {
			logger.error(e.getMessage(), e);
			throw new Exception("Cannot create request response data: ParserConfigurationException", e);
		} catch (DOMException e) {
			logger.error(e.getMessage(), e);
			throw new Exception("Cannot create request response data: DOMException", e);
		}
	}

	@Override
	public String getMimeType() throws Exception {
		return "application/xml";
	}
}
