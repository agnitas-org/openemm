/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailingcontent.dto;

import java.util.Objects;

public final class ContentBlockAndMailingMetaData {

	private final int mailingId;
	private final String mailingName;
	private final int contentBlockId;
	private final String contentBlockName;
	
	public ContentBlockAndMailingMetaData(final int mailingId, final String mailingName, final int contentBlockId, final String contentBlockName) {
		this.mailingId = mailingId;
		this.mailingName = Objects.requireNonNull(mailingName);
		this.contentBlockId = contentBlockId;
		this.contentBlockName = Objects.requireNonNull(contentBlockName);
	}

	public final int getMailingId() {
		return mailingId;
	}

	public final String getMailingName() {
		return mailingName;
	}

	public final int getContentBlockId() {
		return contentBlockId;
	}

	public final String getContentBlockName() {
		return contentBlockName;
	}
	
	
}
