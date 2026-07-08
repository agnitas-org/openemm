/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.springws.endpoint.mailinglist;

import java.util.Objects;

import com.agnitas.emm.core.mailinglist.dto.MailinglistDto;
import com.agnitas.emm.core.mailinglist.exception.MailinglistException;
import com.agnitas.emm.core.mailinglist.exception.MailinglistNotExistException;
import com.agnitas.emm.core.mailinglist.service.MailinglistService;
import com.agnitas.emm.springws.endpoint.BaseEndpoint;
import com.agnitas.emm.springws.endpoint.Namespaces;
import com.agnitas.emm.springws.jaxb.UpdateMailinglistRequest;
import com.agnitas.emm.springws.jaxb.UpdateMailinglistResponse;
import com.agnitas.emm.springws.util.SecurityContextAccess;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

@Endpoint
public class UpdateMailinglistEndpoint extends BaseEndpoint {
	
    private final MailinglistService mailinglistService;
    private final SecurityContextAccess securityContextAccess;

    @Autowired
    public UpdateMailinglistEndpoint(
            MailinglistService mailinglistService,
            SecurityContextAccess securityContextAccess
    ) {
        this.mailinglistService = Objects.requireNonNull(mailinglistService, "mailinglistService");
        this.securityContextAccess = Objects.requireNonNull(securityContextAccess, "securityContextAccess");
    }

    @PayloadRoot(namespace = Namespaces.AGNITAS_ORG, localPart = "UpdateMailinglistRequest")
    public @ResponsePayload UpdateMailinglistResponse updateMailinglist(@RequestPayload UpdateMailinglistRequest request) {
        int companyId = this.securityContextAccess.getWebserviceUserCompanyId();
        if (request.getMailingListId() <= 0) {
            throw new MailinglistException(request.getMailingListId(), companyId, "mailinglist id value should be > 0");
        }

    	final MailinglistDto mailinglist = new MailinglistDto();
        mailinglist.setId(request.getMailingListId());
        mailinglist.setShortname(request.getShortname());
        mailinglist.setDescription(request.getDescription());

        final int resultID = mailinglistService.saveMailinglist(companyId, mailinglist);

        if(resultID == 0) {
            throw new MailinglistNotExistException(request.getMailingListId(), companyId);
        }

        return new UpdateMailinglistResponse();
    }
}
