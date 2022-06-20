/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.springws.endpoint.trackablelink;

import java.util.List;

import org.agnitas.emm.springws.endpoint.BaseEndpoint;
import org.agnitas.emm.springws.endpoint.Utils;
import org.agnitas.emm.springws.jaxb.GetTrackableLinkSettingsRequest;
import org.agnitas.emm.springws.jaxb.GetTrackableLinkSettingsResponse;
import org.agnitas.emm.springws.jaxb.GetTrackableLinkSettingsResponse.LinkExtensions.LinkExtension;
import org.agnitas.util.AgnUtils;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import com.agnitas.beans.ComTrackableLink;
import com.agnitas.beans.LinkProperty;
import com.agnitas.beans.TrackableLinkSettings;
import com.agnitas.emm.core.trackablelinks.service.ComTrackableLinkService;

@Endpoint
public class GetTrackableLinkSettingsEndpoint extends BaseEndpoint {

    private ComTrackableLinkService trackableLinkService;

    public GetTrackableLinkSettingsEndpoint(ComTrackableLinkService trackableLinkService) {
        this.trackableLinkService = trackableLinkService;
    }

    @PayloadRoot(namespace = Utils.NAMESPACE_ORG, localPart = "GetTrackableLinkSettingsRequest")
    public @ResponsePayload GetTrackableLinkSettingsResponse getTrackableLinkSettings(@RequestPayload GetTrackableLinkSettingsRequest request) throws Exception {
        GetTrackableLinkSettingsResponse response = new GetTrackableLinkSettingsResponse();

        int companyId = Utils.getUserCompany();
        int urlId = request.getUrlID();

        TrackableLinkSettings trackableLinkSettings = trackableLinkService.getTrackableLinkSettings(urlId, companyId);
        setTrackableLinkSettingsToResponse(response, trackableLinkSettings);

        return response;
    }

    private void setTrackableLinkSettingsToResponse(GetTrackableLinkSettingsResponse response, TrackableLinkSettings trackableLinkSettings) {
        ComTrackableLink trackableLink = trackableLinkSettings.getTrackableLink();
        response.setUrlID(trackableLink.getId());
        response.setUrl(AgnUtils.getStringIfStringIsNull(trackableLink.getFullUrl()));
        response.setActionID(trackableLink.getActionID());
        response.setShortname(AgnUtils.getStringIfStringIsNull(trackableLink.getShortname()));
        response.setDeepTracking(trackableLink.getDeepTracking());
        response.setAltText(AgnUtils.getStringIfStringIsNull(trackableLink.getAltText()));
        response.setOriginalUrl(AgnUtils.getStringIfStringIsNull(trackableLink.getOriginalUrl()));
        response.setIsAdminLink(trackableLink.isAdminLink());
        response.setTracking(trackableLink.getUsage());

        setLinkExtensionsToResponse(response, trackableLinkSettings);
    }

    private void setLinkExtensionsToResponse(GetTrackableLinkSettingsResponse response, TrackableLinkSettings trackableLinkSettings) {
        GetTrackableLinkSettingsResponse.LinkExtensions responseLinkExtensions = new GetTrackableLinkSettingsResponse.LinkExtensions();
        List<LinkExtension> responseLinkExtensionsList = responseLinkExtensions.getLinkExtension();

        for (LinkProperty linkProperty : trackableLinkSettings.getLinkProperties()) {
            LinkExtension responseLinkExtension = new GetTrackableLinkSettingsResponse.LinkExtensions.LinkExtension();

            responseLinkExtension.setName(AgnUtils.getStringIfStringIsNull(linkProperty.getPropertyName()));
            responseLinkExtension.setValue(AgnUtils.getStringIfStringIsNull(linkProperty.getPropertyValue()));

            responseLinkExtensionsList.add(responseLinkExtension);
        }

        response.setLinkExtensions(responseLinkExtensions);
    }
}
