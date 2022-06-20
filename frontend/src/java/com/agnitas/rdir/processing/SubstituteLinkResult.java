/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.rdir.processing;

import java.util.Objects;

import com.agnitas.beans.ComTrackableLink;
import com.agnitas.emm.core.commons.uid.ComExtensibleUID;

public final class SubstituteLinkResult {

	private final ComTrackableLink trackableLink;
	private final String fullUrl;
	private final ComExtensibleUID uid;

	public SubstituteLinkResult(final ComTrackableLink trackableLink, final String fullUrl, final ComExtensibleUID uid) {
		this.trackableLink = Objects.requireNonNull(trackableLink);
		this.fullUrl = Objects.requireNonNull(fullUrl);
		this.uid = Objects.requireNonNull(uid);
	}

	public ComTrackableLink getTrackableLink() {
		return trackableLink;
	}

	public String getFullUrl() {
		return fullUrl;
	}

	public ComExtensibleUID getUid() {
		return uid;
	}
	
}
