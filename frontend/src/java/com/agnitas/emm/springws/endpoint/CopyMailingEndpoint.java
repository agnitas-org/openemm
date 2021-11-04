/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.springws.endpoint;

import org.agnitas.emm.core.mailing.service.CopyMailingService;
import org.agnitas.emm.springws.endpoint.BaseEndpoint;
import org.agnitas.emm.springws.endpoint.Utils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import com.agnitas.emm.springws.exception.WebServiceInvalidFieldsException;
import com.agnitas.emm.springws.jaxb.CopyMailingRequest;
import com.agnitas.emm.springws.jaxb.CopyMailingResponse;

@Endpoint
public class CopyMailingEndpoint extends BaseEndpoint {
	
    private CopyMailingService copyMailingService;

    @Autowired
    public CopyMailingEndpoint(CopyMailingService copyMailingService) {
        this.copyMailingService = copyMailingService;
    }

    @PayloadRoot(namespace = Utils.NAMESPACE_COM, localPart = "CopyMailingRequest")
    public @ResponsePayload CopyMailingResponse copyMailing(@RequestPayload CopyMailingRequest request) throws Exception {
        validateRequest(request);
        CopyMailingResponse response = new CopyMailingResponse();
        int copyId = copyMailingService.copyMailing(Utils.getUserCompany(), request.getMailingId(), Utils.getUserCompany(), request.getNameOfCopy(), request.getDescriptionOfCopy());
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
