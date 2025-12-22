/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.springws.endpoint;

import java.util.Objects;

import com.agnitas.beans.Target;
import com.agnitas.beans.impl.TargetImpl;
import com.agnitas.emm.core.target.service.TargetService;
import com.agnitas.emm.springws.exception.WebServiceInvalidFieldsException;
import com.agnitas.emm.springws.jaxb.extended.AddTargetGroupRequest;
import com.agnitas.emm.springws.jaxb.extended.AddTargetGroupResponse;
import com.agnitas.emm.springws.util.SecurityContextAccess;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

@Endpoint
public class AddTargetGroupEndpoint extends BaseEndpoint {

    private final TargetService targetService;
    private final SecurityContextAccess securityContextAccess;

    @Autowired
    public AddTargetGroupEndpoint(TargetService targetService, SecurityContextAccess securityContextAccess) {
        this.targetService = Objects.requireNonNull(targetService, "targetService");
        this.securityContextAccess = Objects.requireNonNull(securityContextAccess, "securityContextAccess");
    }

    @PayloadRoot(namespace = Namespaces.AGNITAS_COM, localPart = "AddTargetGroupRequest")
    public @ResponsePayload AddTargetGroupResponse addTargetGroup(@RequestPayload AddTargetGroupRequest request) throws Exception {
        validateRequest(request);
        Target target = createTargetFromRequest(request);
        int savedTargetId = targetService.saveTarget(target);
        AddTargetGroupResponse response = new AddTargetGroupResponse();
        response.setTargetId(savedTargetId);
        return response;
    }

    private Target createTargetFromRequest(AddTargetGroupRequest request) {
        Target target = new TargetImpl();
        target.setTargetName(request.getName());
        target.setTargetDescription(request.getDescription());
        target.setEQL(request.getEql());
        target.setCompanyID(this.securityContextAccess.getWebserviceUserCompanyId());
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
