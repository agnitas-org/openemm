/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.trackablelinks.exceptions;

import com.agnitas.emm.core.commons.exception.EntityNotFoundException;

public class TrackableLinkUnknownLinkIdException extends EntityNotFoundException {

	private static final long serialVersionUID = -3161503210060994618L;
	
	private final int linkId;

    public TrackableLinkUnknownLinkIdException(int linkId) {
        super("Link", linkId);
        this.linkId = linkId;
    }

    public TrackableLinkUnknownLinkIdException(int linkId, int mailingId) {
        super("Link", "mailingId=" + mailingId + ", linkId=" + linkId);
        this.linkId = linkId;
    }

    public int getLinkId() {
        return linkId;
    }

}
