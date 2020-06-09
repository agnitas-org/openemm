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
import org.agnitas.emm.springws.jaxb.GetTrackableLinkSettingsRequest;
import org.agnitas.emm.springws.jaxb.GetTrackableLinkSettingsResponse;
import org.agnitas.emm.springws.jaxb.GetTrackableLinkSettingsResponse.LinkExtensions.LinkExtension;
import org.agnitas.emm.springws.jaxb.ObjectFactory;
import org.agnitas.util.AgnUtils;
import org.springframework.ws.server.endpoint.AbstractMarshallingPayloadEndpoint;

import com.agnitas.beans.ComTrackableLink;
import com.agnitas.beans.LinkProperty;
import com.agnitas.beans.TrackableLinkSettings;
import com.agnitas.emm.core.trackablelinks.service.ComTrackableLinkService;

public class GetTrackableLinkSettingsEndpoint extends AbstractMarshallingPayloadEndpoint {

    @Resource
    private ComTrackableLinkService trackableLinkService;

    @Resource
    private ObjectFactory objectFactory;

    @Override
    protected Object invokeInternal(Object req) throws Exception {
        GetTrackableLinkSettingsRequest request = (GetTrackableLinkSettingsRequest) req;
        GetTrackableLinkSettingsResponse response = objectFactory.createGetTrackableLinkSettingsResponse();

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
        response.setRelevance(trackableLink.getRelevance());
        response.setAltText(AgnUtils.getStringIfStringIsNull(trackableLink.getAltText()));
        response.setOriginalUrl(AgnUtils.getStringIfStringIsNull(trackableLink.getOriginalUrl()));
        response.setIsAdminLink(trackableLink.isAdminLink());
        response.setTracking(trackableLink.getUsage());

        setLinkExtensionsToResponse(response, trackableLinkSettings);
    }

    private void setLinkExtensionsToResponse(GetTrackableLinkSettingsResponse response, TrackableLinkSettings trackableLinkSettings) {
        GetTrackableLinkSettingsResponse.LinkExtensions responseLinkExtensions = objectFactory.createGetTrackableLinkSettingsResponseLinkExtensions();
        List<LinkExtension> responseLinkExtensionsList = responseLinkExtensions.getLinkExtension();

        for (LinkProperty linkProperty : trackableLinkSettings.getLinkProperties()) {
            LinkExtension responseLinkExtension = objectFactory.createGetTrackableLinkSettingsResponseLinkExtensionsLinkExtension();

            responseLinkExtension.setName(AgnUtils.getStringIfStringIsNull(linkProperty.getPropertyName()));
            responseLinkExtension.setValue(AgnUtils.getStringIfStringIsNull(linkProperty.getPropertyValue()));

            responseLinkExtensionsList.add(responseLinkExtension);
        }

        response.setLinkExtensions(responseLinkExtensions);
    }
}
