/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.springws.endpoint.trackablelink;

import java.util.List;
import java.util.Objects;

import com.agnitas.emm.springws.endpoint.BaseEndpoint;
import com.agnitas.emm.springws.endpoint.Namespaces;
import com.agnitas.emm.springws.jaxb.GetTrackableLinkSettingsRequest;
import com.agnitas.emm.springws.jaxb.GetTrackableLinkSettingsResponse;
import com.agnitas.emm.springws.jaxb.GetTrackableLinkSettingsResponse.LinkExtensions.LinkExtension;
import com.agnitas.emm.springws.util.SecurityContextAccess;
import com.agnitas.util.AgnUtils;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import com.agnitas.beans.TrackableLink;
import com.agnitas.beans.LinkProperty;
import com.agnitas.beans.TrackableLinkSettings;
import com.agnitas.emm.core.trackablelinks.service.TrackableLinkService;

@Endpoint
public class GetTrackableLinkSettingsEndpoint extends BaseEndpoint {

    private final TrackableLinkService trackableLinkService;
    private final SecurityContextAccess securityContextAccess;

    public GetTrackableLinkSettingsEndpoint(TrackableLinkService trackableLinkService, final SecurityContextAccess securityContextAccess) {
        this.trackableLinkService = Objects.requireNonNull(trackableLinkService, "trackableLinkService");
        this.securityContextAccess = Objects.requireNonNull(securityContextAccess, "securityContextAccess");
    }

    @PayloadRoot(namespace = Namespaces.AGNITAS_ORG, localPart = "GetTrackableLinkSettingsRequest")
    public @ResponsePayload GetTrackableLinkSettingsResponse getTrackableLinkSettings(@RequestPayload GetTrackableLinkSettingsRequest request) {
        final int companyId = this.securityContextAccess.getWebserviceUserCompanyId();
        final int urlId = request.getUrlID();

        final GetTrackableLinkSettingsResponse response = new GetTrackableLinkSettingsResponse();
        final TrackableLinkSettings trackableLinkSettings = trackableLinkService.getTrackableLinkSettings(urlId, companyId);
        setTrackableLinkSettingsToResponse(response, trackableLinkSettings);

        return response;
    }

    private void setTrackableLinkSettingsToResponse(GetTrackableLinkSettingsResponse response, TrackableLinkSettings trackableLinkSettings) {
    	final TrackableLink trackableLink = trackableLinkSettings.getTrackableLink();
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
