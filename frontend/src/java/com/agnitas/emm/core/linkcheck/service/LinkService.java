/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.linkcheck.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

import com.agnitas.beans.LinkProperty;
import com.agnitas.beans.TrackableLink;
import com.agnitas.emm.core.trackablelinks.exceptions.DependentTrackableLinkException;
import com.agnitas.emm.grid.grid.beans.GridCustomPlaceholderType;

public interface LinkService {

	String personalizeLink(TrackableLink link, String orgUID, int customerID, final String referenceTableRecordSelector, final boolean applyLinkExtensions, final String encodedStaticValueMap);

	void findAllLinks(String text, BiConsumer<Integer, Integer> consumer);

	/**
	 * Scan a text for http and https links.
	 * DOCTYPE-Links will be ignored.
	 * Link strings may include Agnitas specific HashTags (##tagname: parameter='list'##).
	 * 
	 * AgnTag [agnPROFILE] will be resolved.
	 * AgnTag [agnUNSUBSCRIBE] will be resolved.
	 * AgnTag [agnFORM] will be resolved.
	 */
	LinkScanResult scanForLinks(String text, int mailingID, int mailinglistID, int companyID);

	/**
	 * Scan a text for http and https links.
	 * DOCTYPE-Links will be ignored.
	 * Link strings may include Agnitas specific HashTags (##tagname: parameter='list'##).
	 * 
	 * AgnTag [agnPROFILE] ARE NOT resolved.
	 * AgnTag [agnUNSUBSCRIBE] ARE NOT resolved.
	 * AgnTag [agnFORM] ARE NOT resolved.
	 */
	LinkScanResult scanForLinks(String text, int companyID);

	String createDeepTrackingUID(int companyID, int mailingID, int linkID, int customerID);

	Integer getLineNumberOfFirstRdirLink(final String rdirDomain, String text);
	
	/**
	 * Return number of line with invalid link
	 * @return if invalid line exists - return number of line, otherwise return -1
	 */
	int getLineNumberOfFirstInvalidLink(String text);

	String validateLink(int companyId, String link, GridCustomPlaceholderType type);

	List<LinkProperty> getDefaultExtensions(int companyId);

	String replaceLinks(String content, Map<String, String> replacementsMap);

	String addNumbersToLinks(Map<String, Integer> linksCounters, List<String> newLinks, String originContent);

    void assertChangedOrDeletedLinksNotDepended(
            Collection<TrackableLink> oldLinks,
            Collection<TrackableLink> newLinks) throws DependentTrackableLinkException;

	class LinkScanResult {
		private final List<TrackableLink> trackableLinks;
		private final List<String> imageLinks;
		private final List<String> notTrackableLinks;
		private final List<ErroneousLink> erroneousLinks;
		private final List<ErroneousLink> localLinks;
		private final List<LinkWarning> linkWarnings;

		public LinkScanResult() {
			this(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
		}
		
		public LinkScanResult(List<TrackableLink> trackableLinks, List<String> imageLinks, List<String> notTrackableLinks, List<ErroneousLink> erroneousLinks, List<ErroneousLink> localLinks, final List<LinkWarning> linkWarnings) {
			this.trackableLinks = trackableLinks;
			this.imageLinks = imageLinks;
			this.notTrackableLinks = notTrackableLinks;
			this.erroneousLinks = erroneousLinks;
			this.localLinks = localLinks;
			this.linkWarnings = linkWarnings;
		}

		public List<TrackableLink> getTrackableLinks() {
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

	String getRdirDomain(int companyID);
}
