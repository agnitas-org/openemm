/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.springws.endpoint;

import org.agnitas.emm.springws.endpoint.BaseEndpoint;
import org.agnitas.emm.springws.endpoint.Utils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import com.agnitas.beans.ComTarget;
import com.agnitas.beans.impl.ComTargetImpl;
import com.agnitas.emm.core.target.service.ComTargetService;
import com.agnitas.emm.springws.exception.WebServiceInvalidFieldsException;
import com.agnitas.emm.springws.jaxb.AddTargetGroupRequest;
import com.agnitas.emm.springws.jaxb.AddTargetGroupResponse;

@Endpoint
public class AddTargetGroupEndpoint extends BaseEndpoint {

    private ComTargetService targetService;

    @Autowired
    public AddTargetGroupEndpoint(ComTargetService targetService) {
        this.targetService = targetService;
    }

    @PayloadRoot(namespace = Utils.NAMESPACE_COM, localPart = "AddTargetGroupRequest")
    public @ResponsePayload AddTargetGroupResponse addTargetGroup(@RequestPayload AddTargetGroupRequest request) throws Exception {
        validateRequest(request);
        ComTarget target = createTargetFromRequest(request);
        int savedTargetId = targetService.saveTarget(target);
        AddTargetGroupResponse response = new AddTargetGroupResponse();
        response.setTargetId(savedTargetId);
        return response;
    }

    private ComTarget createTargetFromRequest(AddTargetGroupRequest request) {
        ComTarget target = new ComTargetImpl();
        target.setTargetName(request.getName());
        target.setTargetDescription(request.getDescription());
        target.setEQL(request.getEql());
        target.setCompanyID(Utils.getUserCompany());
        return target;
    }

    private void validateRequest(AddTargetGroupRequest request) {
        String name = request.getName();
        String eql = request.getEql();
        if (StringUtils.isBlank(name)) {
            throw new WebServiceInvalidFieldsException("Name field is not specified or blank.");
        }
        if (StringUtils.isBlank(eql)) {
            throw new WebServiceInvalidFieldsException("Eql field is not specified or blank.");
        }
    }
}
