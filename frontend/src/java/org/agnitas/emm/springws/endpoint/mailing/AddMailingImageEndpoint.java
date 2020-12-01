/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.springws.endpoint.mailing;

import org.agnitas.beans.Mailing;
import org.agnitas.beans.MailingComponent;
import org.agnitas.beans.TrackableLink;
import org.agnitas.beans.impl.MailingComponentImpl;
import org.agnitas.dao.MailingDao;
import org.agnitas.dao.TrackableLinkDao;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.core.component.service.ComponentMaximumSizeExceededException;
import org.agnitas.emm.core.mailing.service.MailingNotExistException;
import org.agnitas.emm.springws.endpoint.BaseEndpoint;
import org.agnitas.emm.springws.endpoint.Utils;
import org.agnitas.emm.springws.jaxb.AddMailingImageRequest;
import org.agnitas.emm.springws.jaxb.AddMailingImageResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.util.Base64Utils;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import com.agnitas.beans.ComTrackableLink;
import com.agnitas.beans.impl.ComTrackableLinkImpl;
import com.agnitas.emm.core.components.service.ComComponentService;
import com.agnitas.service.MimeTypeService;

@Endpoint
public class AddMailingImageEndpoint extends BaseEndpoint {
    private ComComponentService componentService;

    private TrackableLinkDao trackableLinkDao;

    private MailingDao mailingDao;

    private  MimeTypeService mimeTypeService;

    private ConfigService configService;

    public AddMailingImageEndpoint(@Qualifier("componentService") ComComponentService componentService, TrackableLinkDao trackableLinkDao, MailingDao mailingDao, MimeTypeService mimeTypeService, ConfigService configService) {
        this.componentService = componentService;
        this.trackableLinkDao = trackableLinkDao;
        this.mailingDao = mailingDao;
        this.mimeTypeService = mimeTypeService;
        this.configService = configService;
    }

    @PayloadRoot(namespace = Utils.NAMESPACE_ORG, localPart = "AddMailingImageRequest")
    public @ResponsePayload AddMailingImageResponse addMailingImage(@RequestPayload AddMailingImageRequest request) throws Exception {
        AddMailingImageResponse res = new AddMailingImageResponse();

        validateParameters(request);

        MailingComponent component = new MailingComponentImpl();
        component.setMailingID(request.getMailingID());
        component.setCompanyID(Utils.getUserCompany());
        component.setType(MailingComponent.TYPE_HOSTED_IMAGE);
        component.setDescription(request.getDescription());
        
        byte[] fileData = Base64Utils.decodeFromString(request.getContent());
		if (fileData.length > configService.getIntegerValue(ConfigValue.MaximumUploadImageSize)) {
			throw new ComponentMaximumSizeExceededException();
		}

        component.setComponentName(request.getFileName());
        component.setBinaryBlock(fileData, mimeTypeService.getMimetypeForFile(request.getFileName()));

        int urlId = saveTrackableLink(request);
        component.setUrlID(urlId);

        int imageComponentId = componentService.addMailingComponent(component);

        res.setID(imageComponentId);
        return res;
    }

    private int saveTrackableLink(AddMailingImageRequest req) {
        String imageUrl = req.getURL();
        int urlId = 0;
        if (StringUtils.isNotBlank(imageUrl)) {
            ComTrackableLink trackableLink = new ComTrackableLinkImpl();
            trackableLink.setCompanyID(Utils.getUserCompany());
            trackableLink.setMailingID(req.getMailingID());
            trackableLink.setFullUrl(imageUrl);
            trackableLink.setUsage(TrackableLink.TRACKABLE_TEXT_HTML);
            trackableLink.setActionID(0);
            urlId = trackableLinkDao.saveTrackableLink(trackableLink);
        }
        return urlId;
    }

    private void validateParameters(AddMailingImageRequest req) {
        int companyId = Utils.getUserCompany();

        if(!isValidMailingId(req.getMailingID(), companyId)) {
            throw new MailingNotExistException();
        }

        if (!isValidMandatoryFields(req.getContent(), req.getFileName())) {
            throw new IllegalArgumentException();
        }
    }

    private boolean isValidMailingId(int mailingID, int companyID) {
        Mailing mailing = mailingDao.getMailing(mailingID, companyID);
        return mailing != null && mailing.getId() != 0;
    }

    private boolean isValidMandatoryFields(String content, String filename) {
        return StringUtils.isNotBlank(content) && StringUtils.isNotBlank(filename);
    }
}
