/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.springws.endpoint.mailing;

import static com.agnitas.beans.impl.MailingComponentImpl.COMPONENT_NAME_MAX_LENGTH;

import java.util.Base64;
import java.util.Objects;

import com.agnitas.beans.Mailing;
import com.agnitas.beans.TrackableLink;
import com.agnitas.beans.impl.TrackableLinkImpl;
import com.agnitas.dao.MailingComponentDao;
import com.agnitas.dao.MailingDao;
import com.agnitas.dao.TrackableLinkDao;
import com.agnitas.emm.core.components.service.ComponentService;
import com.agnitas.emm.core.thumbnails.service.ThumbnailService;
import com.agnitas.service.MimeTypeService;
import com.agnitas.beans.MailingComponent;
import com.agnitas.beans.MailingComponentType;
import com.agnitas.beans.impl.MailingComponentImpl;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.core.component.service.ComponentMaximumSizeExceededException;
import org.agnitas.emm.core.mailing.service.MailingNotExistException;
import com.agnitas.emm.springws.endpoint.BaseEndpoint;
import com.agnitas.emm.springws.endpoint.Namespaces;
import com.agnitas.emm.springws.jaxb.AddMailingImageRequest;
import com.agnitas.emm.springws.jaxb.AddMailingImageResponse;
import com.agnitas.emm.springws.util.SecurityContextAccess;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

@Endpoint
public class AddMailingImageEndpoint extends BaseEndpoint {
	
	/** The logger. */
	private static final transient Logger LOGGER = LogManager.getLogger(AddMailingFromTemplateEndpoint.class);

	private final ThumbnailService thumbnailService;
    private ComponentService componentService;
    private TrackableLinkDao trackableLinkDao;
    private MailingDao mailingDao;
    private  MimeTypeService mimeTypeService;
    private ConfigService configService;
    private SecurityContextAccess securityContextAccess;
    private MailingComponentDao mailingComponentDao;

    public AddMailingImageEndpoint(@Qualifier("componentService") ComponentService componentService, TrackableLinkDao trackableLinkDao, MailingDao mailingDao, MimeTypeService mimeTypeService, ConfigService configService, final ThumbnailService thumbnailService, final SecurityContextAccess securityContextAccess, final MailingComponentDao mailingComponentDao) {
        this.componentService = Objects.requireNonNull(componentService, "componentService");
        this.trackableLinkDao = Objects.requireNonNull(trackableLinkDao, "trackableLinkDao");
        this.mailingDao = Objects.requireNonNull(mailingDao, "mailingDao");
        this.mimeTypeService = Objects.requireNonNull(mimeTypeService, "mimeTypeService, \"\");");
        this.configService = Objects.requireNonNull(configService, "configService");
		this.thumbnailService = Objects.requireNonNull(thumbnailService, "thumbnailService");
		this.securityContextAccess = Objects.requireNonNull(securityContextAccess, "securityContextAccess");
		this.mailingComponentDao = Objects.requireNonNull(mailingComponentDao, "mailingComponentDao");
    }

    @PayloadRoot(namespace = Namespaces.AGNITAS_ORG, localPart = "AddMailingImageRequest")
    public @ResponsePayload AddMailingImageResponse addMailingImage(@RequestPayload AddMailingImageRequest request) {
    	final int companyID = this.securityContextAccess.getWebserviceUserCompanyId();
    	

        validateParameters(request);

        final MailingComponent component = new MailingComponentImpl();
        component.setMailingID(request.getMailingID());
        component.setCompanyID(companyID);
        component.setType(MailingComponentType.HostedImage);
        component.setDescription(request.getDescription());

        byte[] fileData = Base64.getDecoder().decode(request.getContent());
		if (fileData.length > configService.getIntegerValue(ConfigValue.MaximumUploadImageSize)) {
			throw new ComponentMaximumSizeExceededException();
		}

        component.setComponentName(request.getFileName());
        component.setBinaryBlock(fileData, mimeTypeService.getMimetypeForFile(request.getFileName()));

        final int urlId = saveTrackableLink(request);
        component.setUrlID(urlId);

        final boolean replaceExisting = request.isReplaceExisting() == null ? false : request.isReplaceExisting();
        	
        final int imageComponentId = replaceExisting
        		? addOrReplaceComponent(component)
        		: componentService.addMailingComponent(component);
        
		try {
			this.thumbnailService.updateMailingThumbnailByWebservice(companyID, request.getMailingID());
		} catch(final Exception e) {
			LOGGER.error(String.format("Error updating thumbnail of mailing %d", request.getMailingID()), e);
		}
        
		final AddMailingImageResponse res = new AddMailingImageResponse();
        res.setID(imageComponentId);
        
        return res;
    }
    
    private int addOrReplaceComponent(final MailingComponent component) {
    	final MailingComponent existingComponent = this.mailingComponentDao.getMailingComponentByName(component.getMailingID(), component.getCompanyID(), component.getComponentName());
    	
		if (existingComponent == null || existingComponent.getType() != component.getType()) {
			return componentService.addMailingComponent(component);
		} else {
			component.setId(existingComponent.getId());
			mailingComponentDao.saveMailingComponent(component);
			
			return component.getId();
		}
    }

    private int saveTrackableLink(AddMailingImageRequest req) {
    	final int companyId = this.securityContextAccess.getWebserviceUserCompanyId();
    	final int defaultLinkTrackingMode = this.configService.getIntegerValue(ConfigValue.TrackableLinkDefaultTracking, companyId);
    	
        String imageUrl = req.getURL();
        int urlId = 0;
        if (StringUtils.isNotBlank(imageUrl)) {
            TrackableLink trackableLink = new TrackableLinkImpl();
            trackableLink.setCompanyID(companyId);
            trackableLink.setMailingID(req.getMailingID());
            trackableLink.setFullUrl(imageUrl);
            trackableLink.setUsage(defaultLinkTrackingMode);
            trackableLink.setActionID(0);
            urlId = trackableLinkDao.saveTrackableLink(trackableLink);
        }
        return urlId;
    }

    private void validateParameters(AddMailingImageRequest req) {
    	final int companyId = this.securityContextAccess.getWebserviceUserCompanyId();

        if(!isValidMailingId(req.getMailingID(), companyId)) {
            throw new MailingNotExistException(companyId, req.getMailingID());
        }

        if (!isValidMandatoryFields(req.getContent(), req.getFileName())) {
            throw new IllegalArgumentException();
        }
        
        if (StringUtils.length(req.getFileName()) > COMPONENT_NAME_MAX_LENGTH) {
            throw new IllegalArgumentException();
        }
    }

    private boolean isValidMailingId(int mailingID, int companyID) {
    	final Mailing mailing = mailingDao.getMailing(mailingID, companyID);
        return mailing != null && mailing.getId() != 0;
    }

    private boolean isValidMandatoryFields(String content, String filename) {
        return StringUtils.isNotBlank(content) && StringUtils.isNotBlank(filename);
    }
}
