/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.springws.endpoint;

import java.util.Objects;

import com.agnitas.beans.Target;
import com.agnitas.emm.core.target.service.TargetService;
import com.agnitas.emm.springws.exception.WebServiceInvalidFieldsException;
import com.agnitas.emm.springws.jaxb.extended.UpdateTargetGroupRequest;
import com.agnitas.emm.springws.jaxb.extended.UpdateTargetGroupResponse;
import com.agnitas.emm.springws.util.SecurityContextAccess;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

@Endpoint
public class UpdateTargetGroupEndpoint extends BaseEndpoint {

    private final TargetService targetService;
    private final SecurityContextAccess securityContextAccess;

    public UpdateTargetGroupEndpoint(TargetService targetService, SecurityContextAccess securityContextAccess) {
        this.targetService = Objects.requireNonNull(targetService, "targetService");
        this.securityContextAccess = Objects.requireNonNull(securityContextAccess, "securityContextAccess");
    }

    @PayloadRoot(namespace = Namespaces.AGNITAS_COM, localPart = "UpdateTargetGroupRequest")
    public @ResponsePayload UpdateTargetGroupResponse updateTargetGroup(@RequestPayload UpdateTargetGroupRequest request) throws Exception {
        final int companyId = this.securityContextAccess.getWebserviceUserCompanyId();
        
        final int targetId = request.getTargetID();
        final Target target = targetService.getTargetGroup(targetId, companyId);
        fillUpdatedFields(request, target);
        targetService.saveTarget(target);
        
        return new UpdateTargetGroupResponse();
    }

    private void fillUpdatedFields(UpdateTargetGroupRequest request, Target existingTarget) {
        final String description = request.getDescription();
        final String eql = request.getEql();
        final String targetName = request.getTargetName();
        validateRequestFields(description, eql, targetName);
        if (description != null) {
            existingTarget.setTargetDescription(description);
        }
        if (targetName != null) {
            existingTarget.setTargetName(targetName);
        }
        if (eql != null) {
            existingTarget.setEQL(eql);
        }
    }

    private void validateRequestFields(Object... fields) {
        for (Object field : fields) {
            if (field != null && StringUtils.isBlank(field.toString())) {
                throw new WebServiceInvalidFieldsException("Empty fields are not allowed.");
            }
        }
    }
}
