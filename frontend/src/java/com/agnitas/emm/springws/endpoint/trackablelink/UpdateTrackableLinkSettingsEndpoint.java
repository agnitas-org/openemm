/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.springws.endpoint.trackablelink;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.agnitas.emm.springws.endpoint.BaseEndpoint;
import com.agnitas.emm.springws.endpoint.MailingEditableCheck;
import com.agnitas.emm.springws.endpoint.Namespaces;
import com.agnitas.emm.springws.jaxb.UpdateTrackableLinkSettingsRequest;
import com.agnitas.emm.springws.jaxb.UpdateTrackableLinkSettingsRequest.LinkExtensions;
import com.agnitas.emm.springws.jaxb.UpdateTrackableLinkSettingsRequest.LinkExtensions.LinkExtension;
import com.agnitas.emm.springws.jaxb.UpdateTrackableLinkSettingsResponse;
import com.agnitas.emm.springws.util.SecurityContextAccess;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import com.agnitas.beans.LinkProperty;
import com.agnitas.beans.LinkProperty.PropertyType;
import com.agnitas.beans.TrackableLinkModel;
import com.agnitas.emm.core.trackablelinks.service.TrackableLinkService;

@Endpoint
public class UpdateTrackableLinkSettingsEndpoint extends BaseEndpoint {

    private final TrackableLinkService trackableLinkService;
    private final MailingEditableCheck mailingEditableCheck;
    private final SecurityContextAccess securityContextAccess;

    public UpdateTrackableLinkSettingsEndpoint(TrackableLinkService trackableLinkService, final MailingEditableCheck mailingEditableCheck, final SecurityContextAccess securityContextAccess) {
        this.trackableLinkService = Objects.requireNonNull(trackableLinkService, "trackableLinkService");
        this.mailingEditableCheck = Objects.requireNonNull(mailingEditableCheck, "mailingEditableCheck");
        this.securityContextAccess = Objects.requireNonNull(securityContextAccess, "securityContextAccess");
    }

    @PayloadRoot(namespace = Namespaces.AGNITAS_ORG, localPart = "UpdateTrackableLinkSettingsRequest")
    public @ResponsePayload UpdateTrackableLinkSettingsResponse updateTrackableLinkSettingsResponse(@RequestPayload UpdateTrackableLinkSettingsRequest request) throws Exception {
    	this.mailingEditableCheck.requireMailingForTrackableLinkEditable(request.getUrlID(), this.securityContextAccess.getWebserviceUserCompanyId());

    	final TrackableLinkModel trackableLinkModel = getTrackableLinkModel(request);
        trackableLinkService.updateTrackableLinkSettings(trackableLinkModel);

        final UpdateTrackableLinkSettingsResponse response = new UpdateTrackableLinkSettingsResponse();
        return response;
    }

    private TrackableLinkModel getTrackableLinkModel(UpdateTrackableLinkSettingsRequest req) {
        final TrackableLinkModel trackableLinkModel = new TrackableLinkModel();
        trackableLinkModel.setId(req.getUrlID());
        trackableLinkModel.setFullUrl(req.getUrl());
        trackableLinkModel.setActionID(req.getActionID());
        trackableLinkModel.setShortname(req.getShortname());
        trackableLinkModel.setDeepTracking(req.getDeepTracking());
        trackableLinkModel.setAltText(req.getAltText());
        trackableLinkModel.setAdminLink(req.isIsAdminLink());
        trackableLinkModel.setUsage(req.getTracking());
        trackableLinkModel.setCompanyID(this.securityContextAccess.getWebserviceUserCompanyId());

        final List<LinkProperty> linkProperties = getLinkProperties(req);
        trackableLinkModel.setLinkProperties(linkProperties);

        return trackableLinkModel;
    }

    private List<LinkProperty> getLinkProperties(UpdateTrackableLinkSettingsRequest req) {
        List<LinkProperty> linkProperties =null;

        LinkExtensions linkExtensions = req.getLinkExtensions();
        if(linkExtensions != null) {
            linkProperties = new ArrayList<>();
            for (LinkExtension linkExtension : linkExtensions.getLinkExtension()) {
                PropertyType type = PropertyType.LinkExtension;
                String name = linkExtension.getName();
                String value = linkExtension.getValue();

                LinkProperty property = new LinkProperty(type, name, value);
                linkProperties.add(property);
            }
        }
        return linkProperties;
    }
}
