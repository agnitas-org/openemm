/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.linkcheck.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

import org.agnitas.emm.core.velocity.VelocityCheck;

import com.agnitas.beans.ComTrackableLink;
import com.agnitas.beans.LinkProperty;
import com.agnitas.emm.grid.grid.beans.GridCustomPlaceholderType;
import com.agnitas.util.Caret;

public interface LinkService {
	String personalizeLink(ComTrackableLink link, String orgUID, int customerID, final String referenceTableRecordSelector, final boolean applyLinkExtensions, final String encodedStaticValueMap);

	void findAllLinks(String text, BiConsumer<Integer, Integer> consumer);

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

	Integer getLineNumberOfFirstRdirLink(final int companyID, String text);
	
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
		private final List<ComTrackableLink> trackableLinks;
		private final List<String> imageLinks;
		private final List<String> notTrackableLinks;
		private final List<ErroneousLink> erroneousLinks;
		private final List<ErroneousLink> localLinks;
		private final List<LinkWarning> linkWarnings;
		
		
		public LinkScanResult() {
			this(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
		}
		
		public LinkScanResult(List<ComTrackableLink> trackableLinks, List<String> imageLinks, List<String> notTrackableLinks, List<ErroneousLink> erroneousLinks, List<ErroneousLink> localLinks, final List<LinkWarning> linkWarnings) {
			this.trackableLinks = trackableLinks;
			this.imageLinks = imageLinks;
			this.notTrackableLinks = notTrackableLinks;
			this.erroneousLinks = erroneousLinks;
			this.localLinks = localLinks;
			this.linkWarnings = linkWarnings;
		}

		public List<ComTrackableLink> getTrackableLinks() {
			return trackableLinks;
		}
		
		public List<String> getImageLinks() {
			return imageLinks;
		}
		
		public List<String> getNotTrackableLinks() {
			return notTrackableLinks;
		}
		
		public List<ErroneousLink> getErroneousLinks() {
			return erroneousLinks;
		}
		
		public final List<ErroneousLink> getLocalLinks() {
			return this.localLinks;
		}

		public final List<LinkWarning> getLinkWarnings() {
			return linkWarnings;
		}
		
	}
	
	class ErroneousLink {
		String errorMessageKey;
		int position;
		String linkText;
		
		public ErroneousLink(String errorMessageKey, int position, String linkText) {
			this.errorMessageKey = Objects.requireNonNull(errorMessageKey, "Message key is null");
			this.position = position;
			this.linkText = Objects.requireNonNull(linkText, "Link is null");
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
	
	class LinkWarning {
		public enum WarningType {
			INSECURE,
			AGN_TAG_NON_TRACKABLE
		}
		
		private final WarningType warningType;
		private final String link;
		
		public LinkWarning(final WarningType warningType, final String link) {
			this.warningType = Objects.requireNonNull(warningType, "Warning type is null");
			this.link = Objects.requireNonNull(link, "Link is null");
		}

		public final WarningType getWarningType() {
			return warningType;
		}

		public final String getLink() {
			return link;
		}
		
	}
}
