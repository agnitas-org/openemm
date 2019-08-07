/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.springws.endpoint.trackablelink;

import java.util.List;

import javax.annotation.Resource;

import org.agnitas.emm.springws.endpoint.Utils;
import org.agnitas.emm.springws.jaxb.ListTrackableLinksRequest;
import org.agnitas.emm.springws.jaxb.ListTrackableLinksResponse;
import org.agnitas.emm.springws.jaxb.ListTrackableLinksResponse.TrackableLink;
import org.agnitas.emm.springws.jaxb.ObjectFactory;
import org.agnitas.util.AgnUtils;
import org.springframework.ws.server.endpoint.AbstractMarshallingPayloadEndpoint;

import com.agnitas.beans.TrackableLinkListItem;
import com.agnitas.emm.core.trackablelinks.service.ComTrackableLinkService;

@SuppressWarnings("deprecation")
public class ListTrackableLinksEndpoint extends AbstractMarshallingPayloadEndpoint {

    @Resource
    private ComTrackableLinkService trackableLinkService;
    @Resource
    private ObjectFactory objectFactory;

    @Override
    protected Object invokeInternal(Object req) throws Exception {
        ListTrackableLinksRequest request = (ListTrackableLinksRequest) req;
        ListTrackableLinksResponse response = objectFactory.createListTrackableLinksResponse();

        int companyId = Utils.getUserCompany();
        int mailingId = request.getMailingID();
        List<TrackableLinkListItem> trackableLinksList = trackableLinkService.getTrackableLinks(mailingId, companyId);

        setTrackableLinksToResponse(response, trackableLinksList);

        return response;
    }

    private void setTrackableLinksToResponse(ListTrackableLinksResponse response, List<TrackableLinkListItem> trackableLinksList) {
        List<TrackableLink> trackableLinksListResponse = response.getTrackableLink();

        for (TrackableLinkListItem trackableLinkItem : trackableLinksList) {
            TrackableLink trackableLink = objectFactory.createListTrackableLinksResponseTrackableLink();

            trackableLink.setUrlID(trackableLinkItem.getId());
            trackableLink.setUrl(AgnUtils.getStringIfStringIsNull(trackableLinkItem.getFullUrl()));
            trackableLink.setShortname(AgnUtils.getStringIfStringIsNull(trackableLinkItem.getShortname()));
            trackableLink.setAltText(AgnUtils.getStringIfStringIsNull(trackableLinkItem.getAltText()));
            trackableLink.setOriginalUrl(AgnUtils.getStringIfStringIsNull(trackableLinkItem.getOriginalUrl()));

            trackableLinksListResponse.add(trackableLink);
        }
    }
}
