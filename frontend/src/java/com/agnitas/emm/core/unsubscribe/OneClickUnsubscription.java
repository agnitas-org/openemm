/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.unsubscribe;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.MimeType;

import com.agnitas.emm.core.commons.uid.ComExtensibleUID;

final class OneClickUnsubscription {
	
	private static final transient Logger LOGGER = LogManager.getLogger(OneClickUnsubscription.class);

	public static final String UNSUBSCRIBE_PARAMETER_NAME = "List-Unsubscribe";
	public static final String UNSUBSCRIBE_PARAMTER_VALUE = "One-Click";
	
	public static final MimeType MIMETYPE_MULTIPART_FORMDATA = MimeType.valueOf("multipart/form-data");
	public static final MimeType MIMETYPE_APPLICATION_X_WWW_FORM_URLENCODED = MimeType.valueOf("application/x-www-form-urlencoded");
	
	private final Unsubscription unsubscription;
	
	public OneClickUnsubscription(final Unsubscription unsubscription) {
		this.unsubscription = Objects.requireNonNull(unsubscription, "Unsubscription is null");
	}

	public final boolean isOneClickUnsubscritionRequest(final HttpServletRequest request) {
		final MimeType mimeTypeWithParameters = extractContentType(request);
		
		/*
		 * Remove mimetype parameter because
		 * 
		 * "foo/bar" != "foo/bar;x=y" in Java
		 */
		final MimeType mimeTypeWithoutParameters = new MimeType(mimeTypeWithParameters.getType(), mimeTypeWithParameters.getSubtype());
		
		if(MIMETYPE_MULTIPART_FORMDATA.equals(mimeTypeWithoutParameters)) {
			return isCorrectMultipartFormDataContent(request);
		} else if(MIMETYPE_APPLICATION_X_WWW_FORM_URLENCODED.equals(mimeTypeWithoutParameters)) {
			return isCorrectUrlEncodedFormData(request);
		} else {
			return false;
		}
	}

	public final void performOneClickUnsubscription(final ComExtensibleUID uid, final String remark) {
		this.unsubscription.performUnsubscription(uid, remark);
	}
	
	private static final boolean isCorrectMultipartFormDataContent(final HttpServletRequest request) {
		try {
			final Part part = request.getPart(UNSUBSCRIBE_PARAMETER_NAME);
			
			if(part == null) {
				if(LOGGER.isInfoEnabled()) {
					LOGGER.info(String.format("Multipart message does not contain parameter '%s'", UNSUBSCRIBE_PARAMETER_NAME));
				}
				
				return false;
			}
			
			return isCorrectParameterValue(part);
		} catch(final Exception e) {
			return false;
		}
	}
	
	private static final boolean isCorrectParameterValue(final Part part) throws IOException {
		try(final InputStreamReader reader = new InputStreamReader(part.getInputStream())) {
			try(final BufferedReader in = new BufferedReader(reader)) {
				// Check if "List-Unsubscribe" contains "One-Click"
				if(!UNSUBSCRIBE_PARAMTER_VALUE.equals(in.readLine())) {
					if(LOGGER.isInfoEnabled()) {
						LOGGER.info("Content of multipart parameters does not denote One-Click unsubscription");
					}
					
					return false;
				}
				
				// Check if "List-Unsubscribe" does not contain more lines
				if(in.readLine() != null) {
					if(LOGGER.isInfoEnabled()) {
						LOGGER.info("Content of multipart parameters does not denote One-Click unsubscription");
					}
					
					return false;
				}
				
				return true;
			}
		}
		
	}
	
	private static final boolean isCorrectUrlEncodedFormData(final HttpServletRequest request) {
		final String value = request.getParameter(UNSUBSCRIBE_PARAMETER_NAME);
		
		return UNSUBSCRIBE_PARAMTER_VALUE.equals(value);
	}

    private static final MimeType extractContentType(final HttpServletRequest request) {
    	final String contentType = request.getContentType();
    	
    	return contentType != null ? MimeType.valueOf(contentType) : null;
    }

}
