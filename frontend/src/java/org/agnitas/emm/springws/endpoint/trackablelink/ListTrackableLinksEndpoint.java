/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.springws.endpoint.trackablelink;

import java.util.List;
import java.util.Objects;

import org.agnitas.emm.springws.endpoint.BaseEndpoint;
import org.agnitas.emm.springws.endpoint.Namespaces;
import org.agnitas.emm.springws.jaxb.ListTrackableLinksRequest;
import org.agnitas.emm.springws.jaxb.ListTrackableLinksResponse;
import org.agnitas.emm.springws.jaxb.ListTrackableLinksResponse.TrackableLink;
import org.agnitas.emm.springws.util.SecurityContextAccess;
import org.agnitas.util.AgnUtils;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import com.agnitas.beans.TrackableLinkListItem;
import com.agnitas.emm.core.trackablelinks.service.ComTrackableLinkService;

@Endpoint
public class ListTrackableLinksEndpoint extends BaseEndpoint {

    private final ComTrackableLinkService trackableLinkService;
    private final SecurityContextAccess securityContextAccess;

    public ListTrackableLinksEndpoint(ComTrackableLinkService trackableLinkService, final SecurityContextAccess securityContextAccess) {
        this.trackableLinkService = Objects.requireNonNull(trackableLinkService, "trackableLinkService");
        this.securityContextAccess = Objects.requireNonNull(securityContextAccess, "securityContextAccess");
    }

    @PayloadRoot(namespace = Namespaces.AGNITAS_ORG, localPart = "ListTrackableLinksRequest")
    public @ResponsePayload ListTrackableLinksResponse listTrackableLinks(@RequestPayload ListTrackableLinksRequest request) throws Exception {
    	final int companyId = this.securityContextAccess.getWebserviceUserCompanyId();
        final int mailingId = request.getMailingID();
        final List<TrackableLinkListItem> trackableLinksList = trackableLinkService.getTrackableLinkItems(mailingId, companyId);

        final ListTrackableLinksResponse response = new ListTrackableLinksResponse();
        setTrackableLinksToResponse(response, trackableLinksList);

        return response;
    }

    private void setTrackableLinksToResponse(ListTrackableLinksResponse response, List<TrackableLinkListItem> trackableLinksList) {
    	final List<TrackableLink> trackableLinksListResponse = response.getTrackableLink();

        for (TrackableLinkListItem trackableLinkItem : trackableLinksList) {
           final  TrackableLink trackableLink = new ListTrackableLinksResponse.TrackableLink();

            trackableLink.setUrlID(trackableLinkItem.getId());
            trackableLink.setUrl(AgnUtils.getStringIfStringIsNull(trackableLinkItem.getFullUrl()));
            trackableLink.setShortname(AgnUtils.getStringIfStringIsNull(trackableLinkItem.getShortname()));
            trackableLink.setAltText(AgnUtils.getStringIfStringIsNull(trackableLinkItem.getAltText()));
            trackableLink.setOriginalUrl(AgnUtils.getStringIfStringIsNull(trackableLinkItem.getOriginalUrl()));

            trackableLinksListResponse.add(trackableLink);
        }
    }
}
