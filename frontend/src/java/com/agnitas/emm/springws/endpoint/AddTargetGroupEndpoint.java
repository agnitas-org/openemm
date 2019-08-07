/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.springws.endpoint;

import javax.annotation.Resource;

import org.agnitas.emm.springws.endpoint.Utils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.ws.server.endpoint.AbstractMarshallingPayloadEndpoint;

import com.agnitas.beans.ComTarget;
import com.agnitas.beans.impl.ComTargetImpl;
import com.agnitas.emm.core.target.service.ComTargetService;
import com.agnitas.emm.springws.exception.WebServiceInvalidFieldsException;
import com.agnitas.emm.springws.jaxb.AddTargetGroupRequest;
import com.agnitas.emm.springws.jaxb.AddTargetGroupResponse;
import com.agnitas.emm.springws.jaxb.ObjectFactory;

@SuppressWarnings("deprecation")
public class AddTargetGroupEndpoint extends AbstractMarshallingPayloadEndpoint {

    private ObjectFactory objectFactory;

    @Resource
    private ComTargetService targetService;

    @Override
    protected Object invokeInternal(Object requestObject) throws Exception {
        AddTargetGroupRequest request = (AddTargetGroupRequest) requestObject;
        validateRequest(request);
        ComTarget target = createTargetFromRequest(request);
        int savedTargetId = targetService.saveTarget(target);
        AddTargetGroupResponse response = objectFactory.createAddTargetGroupResponse();
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

    @Required
    public void setObjectFactory(ObjectFactory objectFactory) {
        this.objectFactory = objectFactory;
    }
}
