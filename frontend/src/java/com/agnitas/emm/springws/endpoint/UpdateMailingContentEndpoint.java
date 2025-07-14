/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.springws.endpoint;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

import com.agnitas.emm.core.components.service.ComponentService;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.emm.core.thumbnails.service.ThumbnailService;
import com.agnitas.emm.springws.jaxb.extended.UpdateMailingContentRequest;
import com.agnitas.emm.springws.jaxb.extended.UpdateMailingContentResponse;
import org.agnitas.emm.core.component.service.ComponentModel;
import com.agnitas.emm.springws.endpoint.BaseEndpoint;
import com.agnitas.emm.springws.endpoint.MailingEditableCheck;
import com.agnitas.emm.springws.endpoint.Namespaces;
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
public class UpdateMailingContentEndpoint extends BaseEndpoint {

	private static final Logger LOGGER = LogManager.getLogger(UpdateMailingContentEndpoint.class);

	private final ThumbnailService thumbnailService;
    private final ComponentService componentService;
	private final MailingEditableCheck mailingEditableCheck;
	private final SecurityContextAccess securityContextAccess;

    public UpdateMailingContentEndpoint(@Qualifier("componentService") ComponentService componentService, MailingEditableCheck mailingEditableCheck, ThumbnailService thumbnailService, SecurityContextAccess securityContextAccess) {
        this.componentService = Objects.requireNonNull(componentService, "componentService");
		this.mailingEditableCheck = Objects.requireNonNull(mailingEditableCheck, "mailingEditableCheck");
		this.thumbnailService = Objects.requireNonNull(thumbnailService, "thumbnailService");
		this.securityContextAccess = Objects.requireNonNull(securityContextAccess, "securityContextAccess");
    }

    @PayloadRoot(namespace = Namespaces.AGNITAS_COM, localPart = "UpdateMailingContentRequest")
    public @ResponsePayload UpdateMailingContentResponse updateMailingContent(@RequestPayload UpdateMailingContentRequest request) throws Exception {
        if (request.getMailingID() <= 0) {
            throw new IllegalArgumentException("Invalid mailing ID");
        }

        if (!MediaTypes.EMAIL.isComponentNameForMediaType(request.getComponentName())) {
            throw new IllegalArgumentException("Invalid component name");
        }
        
        final int companyID = securityContextAccess.getWebserviceUserCompanyId();
        
        this.mailingEditableCheck.requireMailingEditable(request.getMailingID(), companyID);
        componentService.updateMailingContent(parseModel(request));
        
		try {
			this.thumbnailService.updateMailingThumbnailByWebservice(companyID, request.getMailingID());
		} catch(final Exception e) {
			LOGGER.error(String.format("Error updating thumbnail of mailing %d", request.getMailingID()), e);
		}
        
        return new UpdateMailingContentResponse();
    }

    private ComponentModel parseModel(UpdateMailingContentRequest request) {
        ComponentModel model = new ComponentModel();

        model.setCompanyId(securityContextAccess.getWebserviceUserCompanyId());
        model.setMailingId(request.getMailingID());
        model.setComponentName(request.getComponentName());

        String content = request.getNewContent();
        model.setData(StringUtils.isEmpty(content) ? new byte[0] : content.getBytes(StandardCharsets.UTF_8));

        return model;
    }
}
