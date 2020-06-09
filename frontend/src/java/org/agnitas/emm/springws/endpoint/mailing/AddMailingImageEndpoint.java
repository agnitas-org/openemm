/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.springws.endpoint.mailing;

import javax.annotation.Resource;

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
import org.agnitas.emm.springws.endpoint.Utils;
import org.agnitas.emm.springws.jaxb.AddMailingImageRequest;
import org.agnitas.emm.springws.jaxb.AddMailingImageResponse;
import org.agnitas.emm.springws.jaxb.ObjectFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Base64Utils;
import org.springframework.ws.server.endpoint.AbstractMarshallingPayloadEndpoint;

import com.agnitas.beans.impl.ComTrackableLinkImpl;
import com.agnitas.emm.core.components.service.ComComponentService;
import com.agnitas.service.MimeTypeService;

public class AddMailingImageEndpoint extends AbstractMarshallingPayloadEndpoint {
    @Resource
    private ComComponentService componentService;

    @Resource
    private TrackableLinkDao trackableLinkDao;

    @Resource
    private MailingDao mailingDao;

    @Resource
    private ObjectFactory objectFactory;

    @Resource
    private  MimeTypeService mimeTypeService;

    @Resource
    private ConfigService configService;

    @Override
    protected Object invokeInternal(Object o) throws Exception {
        AddMailingImageRequest req = (AddMailingImageRequest) o;
        AddMailingImageResponse res = objectFactory.createAddMailingImageResponse();

        validateParameters(req);

        MailingComponent component = new MailingComponentImpl();
        component.setMailingID(req.getMailingID());
        component.setCompanyID(Utils.getUserCompany());
        component.setType(MailingComponent.TYPE_HOSTED_IMAGE);
        component.setDescription(req.getDescription());
        
        byte[] fileData = Base64Utils.decodeFromString(req.getContent());
		if (fileData.length > configService.getIntegerValue(ConfigValue.MaximumUploadImageSize)) {
			throw new ComponentMaximumSizeExceededException();
		}

        component.setComponentName(req.getFileName());
        component.setBinaryBlock(fileData, mimeTypeService.getMimetypeForFile(req.getFileName()));

        int urlId = saveTrackableLink(req);
        component.setUrlID(urlId);

        int imageComponentId = componentService.addMailingComponent(component);

        res.setID(imageComponentId);
        return res;
    }

    private int saveTrackableLink(AddMailingImageRequest req) {
        String imageUrl = req.getURL();
        int urlId = 0;
        if (StringUtils.isNotBlank(imageUrl)) {
            TrackableLink trackableLink = new ComTrackableLinkImpl();
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
