/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.springws.endpoint.dyntarget;

import javax.annotation.Resource;

import org.agnitas.emm.springws.endpoint.Utils;
import org.agnitas.emm.springws.jaxb.ListTargetgroupsRequest;
import org.agnitas.emm.springws.jaxb.ListTargetgroupsResponse;
import org.agnitas.emm.springws.jaxb.ObjectFactory;
import org.springframework.ws.server.endpoint.AbstractMarshallingPayloadEndpoint;

import com.agnitas.beans.TargetLight;
import com.agnitas.emm.core.target.service.ComTargetService;

public class ListTargetgroupsEndpoint extends AbstractMarshallingPayloadEndpoint {

    @Resource
    private ObjectFactory objectFactory;
    @Resource
    private ComTargetService targetService;

    @Override
    protected Object invokeInternal(Object o) throws Exception {
        @SuppressWarnings("unused")
        ListTargetgroupsRequest request = (ListTargetgroupsRequest) o;
        ListTargetgroupsResponse response = objectFactory.createListTargetgroupsResponse();

        for (TargetLight target : targetService.getTargetLights(Utils.getUserCompany())) {
            ListTargetgroupsResponse.Item targetgroup = objectFactory.createListTargetgroupsResponseItem();
            targetgroup.setId(target.getId());
            targetgroup.setName(target.getTargetName());
            response.getItem().add(targetgroup);
        }

        return response;
    }
}
