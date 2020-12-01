/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import com.agnitas.util.Caret;
import com.agnitas.beans.LinkProperty;
import org.agnitas.emm.core.velocity.VelocityCheck;

import com.agnitas.beans.ComTrackableLink;
import com.agnitas.emm.grid.grid.beans.GridCustomPlaceholderType;

public interface LinkService {
	String personalizeLink(ComTrackableLink link, String orgUID, int customerID, final String referenceTableRecordSelector, final boolean applyLinkExtensions, final String encodedStaticValueMap);

	void findAllLinks(final int companyID, String text, BiConsumer<Integer, Integer> consumer);

	String resolveAgnTags(String text, int mailingID, int mailinglistID, int companyID);

	/**
	 * Scan a text for http and https links.
	 * DOCTYPE-Links will be ignored.
	 * Link strings may include Agnitas specific HashTags (##tagname: parameter='list'##).
	 * 
	 * AgnTag [agnPROFILE] will be resolved.
	 * AgnTag [agnUNSUBSCRIBE] will be resolved.
	 * AgnTag [agnFORM] will be resolved.
	 * @throws Exception
	 */
	LinkScanResult scanForLinks(String text, int mailingID, int mailinglistID, int companyID) throws Exception;

	/**
	 * Scan a text for http and https links.
	 * DOCTYPE-Links will be ignored.
	 * Link strings may include Agnitas specific HashTags (##tagname: parameter='list'##).
	 * 
	 * AgnTag [agnPROFILE] ARE NOT resolved.
	 * AgnTag [agnUNSUBSCRIBE] ARE NOT resolved.
	 * AgnTag [agnFORM] ARE NOT resolved.
	 * @throws Exception 
	 */
	LinkScanResult scanForLinks(String text, int companyID) throws Exception;

	String encodeTagStringLinkTracking(int companyID, int mailingID, int linkID, int customerID);

	String createDeepTrackingUID(int companyID, int mailingID, int linkID, int customerID);

	Integer getLineNumberOfFirstRdirLink(String text);
	
	/**
	 * Return number of line with invalid link
	 * @param text
	 * @return if invalid line exists - return number of line, otherwise return -1
	 */
	int getLineNumberOfFirstInvalidLink(String text);

	Integer getLineNumberOfFirstInvalidSrcLink(String text);

	String validateLink(@VelocityCheck int companyId, String link, GridCustomPlaceholderType type);

	List<LinkProperty> getDefaultExtensions(@VelocityCheck int companyId);

	class ParseLinkException extends Exception implements ErrorLinkStorage {
		private static final long serialVersionUID = -4821051425601251856L;
		
		private final String errorLink;
		private String errorMessage;

		public ParseLinkException(Throwable cause) {
			super(cause);
			if(cause instanceof ErrorLinkStorage){
				errorLink = ((ErrorLinkStorage) cause).getErrorLink();
				errorMessage = ((ErrorLinkStorage) cause).getErrorMessage();
			} else {
				errorLink = null;
			}
		}

		public ParseLinkException(String message, Throwable cause) {
			super(message, cause);
			if(cause instanceof ErrorLinkStorage){
				errorLink = ((ErrorLinkStorage) cause).getErrorLink();
				errorMessage = ((ErrorLinkStorage) cause).getErrorMessage();
			} else {
				errorLink = null;
			}
		}

		@Override
		public String getErrorLink() {
			return errorLink;
		}

		@Override
		public String getErrorMessage() {
			return errorMessage;
		}

	}

	class ParseLinkRuntimeException extends RuntimeException implements ErrorLinkStorage {
		private static final long serialVersionUID = -6277656615171367404L;

		private final String errorLink;
		private final String errorMessage;
		private final Caret caret;

		public ParseLinkRuntimeException(String message, String errorLink, Caret caret) {
			super(message);
			this.errorLink = errorLink;
			errorMessage = message;
			this.caret = caret;
		}

		@Override
		public String getErrorLink() {
			return errorLink;
		}

		@Override
		public String getErrorMessage() {
			return errorMessage;
		}

		public Caret getCaret() {
			return caret;
		}
	}

	interface ErrorLinkStorage {
		String getErrorLink();
		String getErrorMessage();
	}
	
	class LinkScanResult {
		List<ComTrackableLink> trackableLinks;
		List<String> imageLinks;
		List<String> notTrackableLinks;
		List<ErrorneousLink> errorneousLinks;
		List<ErrorneousLink> localLinks;
		
		public LinkScanResult() {
			this(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
		}
		
		public LinkScanResult(List<ComTrackableLink> trackableLinks, List<String> imageLinks, List<String> notTrackableLinks, List<ErrorneousLink> errorneousLinks, List<ErrorneousLink> localLinks) {
			this.trackableLinks = trackableLinks;
			this.imageLinks = imageLinks;
			this.notTrackableLinks = notTrackableLinks;
			this.errorneousLinks = errorneousLinks;
			this.localLinks = localLinks;
		}

		public List<ComTrackableLink> getTrackableLinks() {
			return trackableLinks;
		}
		
		public void setTrackableLinks(List<ComTrackableLink> trackableLinks) {
			this.trackableLinks = trackableLinks;
		}
		
		public List<String> getImageLinks() {
			return imageLinks;
		}
		
		public void setImageLinks(List<String> imageLinks) {
			this.imageLinks = imageLinks;
		}
		
		public List<String> getNotTrackableLinks() {
			return notTrackableLinks;
		}
		
		public void setNotTrackableLinks(List<String> notTrackableLinks) {
			this.notTrackableLinks = notTrackableLinks;
		}
		
		public List<ErrorneousLink> getErrorneousLinks() {
			return errorneousLinks;
		}
		
		public void setErrorneousLinks(List<ErrorneousLink> errorneousLinks) {
			this.errorneousLinks = errorneousLinks;
		}
		
		public final List<ErrorneousLink> getLocalLinks() {
			return this.localLinks;
		}
		
		public void setLocalLinks(List<ErrorneousLink> localLinks) {
			this.localLinks = localLinks;
		}
	}
	
	class ErrorneousLink {
		String errorMessageKey;
		int position;
		String linkText;
		
		public ErrorneousLink(String errorMessageKey, int position, String linkText) {
			this.errorMessageKey = errorMessageKey;
			this.position = position;
			this.linkText = linkText;
		}

		public String getErrorMessageKey() {
			return errorMessageKey;
		}
		
		public int getPosition() {
			return position;
		}
		
		public String getLinkText() {
			return linkText;
		}
	}
}
