/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.rdir.processing;

import java.util.Objects;

import com.agnitas.beans.TrackableLink;
import com.agnitas.emm.core.commons.uid.ExtensibleUID;

public final class SubstituteLinkResult {

	private final TrackableLink trackableLink;
	private final String fullUrl;
	private final ExtensibleUID uid;

	public SubstituteLinkResult(final TrackableLink trackableLink, final String fullUrl, final ExtensibleUID uid) {
		this.trackableLink = Objects.requireNonNull(trackableLink);
		this.fullUrl = Objects.requireNonNull(fullUrl);
		this.uid = Objects.requireNonNull(uid);
	}

	public TrackableLink getTrackableLink() {
		return trackableLink;
	}

	public String getFullUrl() {
		return fullUrl;
	}

	public ExtensibleUID getUid() {
		return uid;
	}
	
}
