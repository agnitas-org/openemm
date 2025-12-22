/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.springws.endpoint;

import java.util.Objects;

import com.agnitas.emm.core.mailing.service.CopyMailingService;
import com.agnitas.emm.core.thumbnails.service.ThumbnailService;
import com.agnitas.emm.springws.exception.WebServiceInvalidFieldsException;
import com.agnitas.emm.springws.jaxb.extended.CopyMailingRequest;
import com.agnitas.emm.springws.jaxb.extended.CopyMailingResponse;
import com.agnitas.emm.springws.util.SecurityContextAccess;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

@Endpoint
public class CopyMailingEndpoint extends BaseEndpoint {
	
	private final ThumbnailService thumbnailService;
    private final CopyMailingService copyMailingService;
    private final SecurityContextAccess securityContextAccess;

    @Autowired
    public CopyMailingEndpoint(
            CopyMailingService copyMailingService,
            ThumbnailService thumbnailService,
            SecurityContextAccess securityContextAccess
    ) {
        this.copyMailingService = Objects.requireNonNull(copyMailingService, "copyMailingService");
		this.thumbnailService = Objects.requireNonNull(thumbnailService, "thumbnailService");
		this.securityContextAccess = Objects.requireNonNull(securityContextAccess, "securityContextAccess");
   }

    @PayloadRoot(namespace = Namespaces.AGNITAS_COM, localPart = "CopyMailingRequest")
    public @ResponsePayload CopyMailingResponse copyMailing(@RequestPayload CopyMailingRequest request) {
        validateRequest(request);
        
        final int companyID = this.securityContextAccess.getWebserviceUserCompanyId();
        final int copyId = copyMailingService.copyMailing(companyID, request.getMailingId(), companyID, request.getNameOfCopy(), request.getDescriptionOfCopy());
        
        thumbnailService.tryUpdateMailingThumbnailByWebservice(companyID, copyId);

		final CopyMailingResponse response = new CopyMailingResponse();
		response.setCopyId(copyId);
        
        return response;
    }

    private void validateRequest(CopyMailingRequest request) {
        String nameOfCopy = request.getNameOfCopy();
        if (StringUtils.isBlank(nameOfCopy)) {
            throw new WebServiceInvalidFieldsException("Field nameOfCopy is empty.");
        }
    }
}
