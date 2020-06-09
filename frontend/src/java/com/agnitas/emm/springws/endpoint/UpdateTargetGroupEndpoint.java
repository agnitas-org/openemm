/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.springws.endpoint;

import javax.annotation.Resource;

import org.agnitas.emm.springws.endpoint.Utils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.ws.server.endpoint.AbstractMarshallingPayloadEndpoint;

import com.agnitas.beans.ComTarget;
import com.agnitas.emm.core.target.service.ComTargetService;
import com.agnitas.emm.springws.exception.WebServiceInvalidFieldsException;
import com.agnitas.emm.springws.jaxb.ObjectFactory;
import com.agnitas.emm.springws.jaxb.UpdateTargetGroupRequest;

public class UpdateTargetGroupEndpoint extends AbstractMarshallingPayloadEndpoint {

    private ObjectFactory objectFactory;

    @Resource
    private ComTargetService targetService;

    @Override
    protected Object invokeInternal(Object requestAsObject) throws Exception {
        UpdateTargetGroupRequest request = (UpdateTargetGroupRequest) requestAsObject;
        int companyId = Utils.getUserCompany();
        int targetId = request.getTargetID();
        ComTarget target = targetService.getTargetGroup(targetId, companyId);
        fillUpdatedFields(request, target);
        targetService.saveTarget(target);
        return objectFactory.createUpdateTargetGroupResponse();
    }

    private void fillUpdatedFields(UpdateTargetGroupRequest request, ComTarget existingTarget) {
        String description = request.getDescription();
        String eql = request.getEql();
        String targetName = request.getTargetName();
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

    @Required
    public void setObjectFactory(ObjectFactory objectFactory) {
        this.objectFactory = objectFactory;
    }
}
