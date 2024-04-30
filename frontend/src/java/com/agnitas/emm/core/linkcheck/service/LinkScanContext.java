/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.linkcheck.service;

import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;

import com.agnitas.beans.ComTrackableLink;
import com.agnitas.emm.core.linkcheck.service.LinkService.ErroneousLink;
import com.agnitas.emm.core.linkcheck.service.LinkService.LinkWarning;

final class LinkScanContext {
	private final String fullText;
	private final int start;
	private final int end;
	
	private final List<ComTrackableLink> foundTrackableLinks;
	private final List<String> foundImages;
	private final List<String> foundNotTrackableLinks;
	private final List<ErroneousLink> foundErroneousLinks;
	private final List<ErroneousLink> localLinks;
	private final List<LinkWarning> linkWarnings;
	
	private String fullTextWithTagsReplaced;
	private String linkUrl;
	private String linkUrlWithTagsReplaced;
	
	private boolean protocolSchemaPresent;
	private String protocolSchema;
	
	public LinkScanContext(final String fullText, final String fullTextWithReplacedTags, final int start, final int end, final List<ComTrackableLink> foundTrackableLinks, final List<String> foundImages, final List<String> foundNotTrackableLinks, final List<ErroneousLink> foundErroneousLinks, final List<ErroneousLink> localLinks, final List<LinkWarning> linkWarnings) {
		assert fullTextWithReplacedTags == null || fullText.length() == fullTextWithReplacedTags.length();
		
		this.fullText = Objects.requireNonNull(fullText);
		this.fullTextWithTagsReplaced = fullTextWithReplacedTags;
		this.start = start;
		this.end = end;
		this.foundTrackableLinks = Objects.requireNonNull(foundTrackableLinks);
		this.foundImages = Objects.requireNonNull(foundImages);
		this.foundNotTrackableLinks = Objects.requireNonNull(foundNotTrackableLinks);
		this.foundErroneousLinks = Objects.requireNonNull(foundErroneousLinks);
		this.localLinks = Objects.requireNonNull(localLinks);
		this.linkWarnings = Objects.requireNonNull(linkWarnings);
		this.protocolSchemaPresent = false;
	}

	public final String getFullText() {
		return this.fullText;
	}

	public final int getStart() {
		return this.start;
	}
	
	public final List<ComTrackableLink> getFoundTrackableLinks() {
		return foundTrackableLinks;
	}

	public final List<String> getFoundImages() {
		return foundImages;
	}

	public final List<String> getFoundNotTrackableLinks() {
		return foundNotTrackableLinks;
	}

	public final List<ErroneousLink> getFoundErroneousLinks() {
		return foundErroneousLinks;
	}

	public final List<ErroneousLink> getLocalLinks() {
		return localLinks;
	}

	public final List<LinkWarning> getLinkWarnings() {
		return linkWarnings;
	}

	public final String getTextWithAgnTagsReplaced() {
		if(this.fullTextWithTagsReplaced == null) {
			this.fullTextWithTagsReplaced = LinkServiceImpl.getTextWithReplacedAgnTags(this.fullText, "x");
			
			assert fullTextWithTagsReplaced == null || fullText.length() == fullTextWithTagsReplaced.length();
		}
		
		return this.fullTextWithTagsReplaced;
	}
	
	public final String getLinkUrl() {
		if(linkUrl == null) {
			this.linkUrl = fullText.substring(start, end).trim();
		}
		
		return linkUrl;
	}
	
	public final String getLinkUrlWithAgnTagsReplaced() {
		if(linkUrlWithTagsReplaced == null) {
			this.linkUrlWithTagsReplaced = getTextWithAgnTagsReplaced().substring(start, end).trim();
		}
		
		return linkUrlWithTagsReplaced;
	}
	
	public final String getProtocolSchema() {
		if(!this.protocolSchemaPresent) {
			final Matcher schemaMatcher = LinkServiceImpl.PROTOCOL_SCHEMA_PATTERN.matcher(getLinkUrl());
			this.protocolSchema = schemaMatcher.matches() ? schemaMatcher.group(1) : null;
			this.protocolSchemaPresent = true;
		}
		
		return this.protocolSchema;
	}
}
