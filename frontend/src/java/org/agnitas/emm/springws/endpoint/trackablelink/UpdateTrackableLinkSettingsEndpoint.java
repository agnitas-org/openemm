/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.springws.endpoint.trackablelink;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.agnitas.emm.springws.endpoint.Utils;
import org.agnitas.emm.springws.jaxb.ObjectFactory;
import org.agnitas.emm.springws.jaxb.UpdateTrackableLinkSettingsRequest;
import org.agnitas.emm.springws.jaxb.UpdateTrackableLinkSettingsRequest.LinkExtensions;
import org.agnitas.emm.springws.jaxb.UpdateTrackableLinkSettingsRequest.LinkExtensions.LinkExtension;
import org.agnitas.emm.springws.jaxb.UpdateTrackableLinkSettingsResponse;
import org.springframework.ws.server.endpoint.AbstractMarshallingPayloadEndpoint;

import com.agnitas.beans.LinkProperty;
import com.agnitas.beans.LinkProperty.PropertyType;
import com.agnitas.beans.TrackableLinkModel;
import com.agnitas.emm.core.trackablelinks.service.ComTrackableLinkService;

public class UpdateTrackableLinkSettingsEndpoint extends AbstractMarshallingPayloadEndpoint {

    @Resource
    private ComTrackableLinkService trackableLinkService;

    @Resource
    private ObjectFactory objectFactory;

    @Override
    protected Object invokeInternal(Object request) throws Exception {
        UpdateTrackableLinkSettingsRequest req = (UpdateTrackableLinkSettingsRequest) request;
        UpdateTrackableLinkSettingsResponse response = objectFactory.createUpdateTrackableLinkSettingsResponse();

        TrackableLinkModel trackableLinkModel = getTrackableLinkModel(req);
        trackableLinkService.updateTrackableLinkSettings(trackableLinkModel);

        return response;
    }

    private TrackableLinkModel getTrackableLinkModel(UpdateTrackableLinkSettingsRequest req) {
        TrackableLinkModel trackableLinkModel = new TrackableLinkModel();
        trackableLinkModel.setId(req.getUrlID());
        trackableLinkModel.setFullUrl(req.getUrl());
        trackableLinkModel.setActionID(req.getActionID());
        trackableLinkModel.setShortname(req.getShortname());
        trackableLinkModel.setDeepTracking(req.getDeepTracking());
        trackableLinkModel.setRelevance(req.getRelevance());
        trackableLinkModel.setAltText(req.getAltText());
        trackableLinkModel.setAdminLink(req.isIsAdminLink());
        trackableLinkModel.setUsage(req.getTracking());
        trackableLinkModel.setCompanyID(Utils.getUserCompany());

        List<LinkProperty> linkProperties = getLinkProperties(req);

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
