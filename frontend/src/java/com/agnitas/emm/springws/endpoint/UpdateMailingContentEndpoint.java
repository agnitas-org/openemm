/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.springws.endpoint;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

import org.agnitas.emm.core.component.service.ComponentModel;
import org.agnitas.emm.springws.endpoint.BaseEndpoint;
import org.agnitas.emm.springws.endpoint.MailingEditableCheck;
import org.agnitas.emm.springws.endpoint.Utils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.util.StringUtils;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import com.agnitas.emm.core.components.service.ComComponentService;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.emm.springws.jaxb.UpdateMailingContentRequest;
import com.agnitas.emm.springws.jaxb.UpdateMailingContentResponse;

@Endpoint
public class UpdateMailingContentEndpoint extends BaseEndpoint {

    private final ComComponentService componentService;
	private final MailingEditableCheck mailingEditableCheck;

    public UpdateMailingContentEndpoint(@Qualifier("componentService") ComComponentService componentService, final MailingEditableCheck mailingEditableCheck) {
        this.componentService = Objects.requireNonNull(componentService);
		this.mailingEditableCheck = Objects.requireNonNull(mailingEditableCheck);
    }

    @PayloadRoot(namespace = Utils.NAMESPACE_COM, localPart = "UpdateMailingContentRequest")
    public @ResponsePayload UpdateMailingContentResponse updateMailingContent(@RequestPayload UpdateMailingContentRequest request) throws Exception {
        if (request.getMailingID() <= 0) {
            throw new IllegalArgumentException("Invalid mailing ID");
        }

        if (!MediaTypes.EMAIL.isComponentNameForMediaType(request.getComponentName())) {
            throw new IllegalArgumentException("Invalid component name");
        }
        
        this.mailingEditableCheck.requireMailingEditable(request.getMailingID(), Utils.getUserCompany());

        componentService.updateMailingContent(parseModel(request));
        return new UpdateMailingContentResponse();
    }

    private ComponentModel parseModel(UpdateMailingContentRequest request) {
        ComponentModel model = new ComponentModel();

        model.setCompanyId(Utils.getUserCompany());
        model.setMailingId(request.getMailingID());
        model.setComponentName(request.getComponentName());

        String content = request.getNewContent();
        model.setData(StringUtils.isEmpty(content) ? new byte[0] : content.getBytes(StandardCharsets.UTF_8));

        return model;
    }
}
