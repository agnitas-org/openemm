/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.springws.endpoint.dyntarget;

import org.agnitas.emm.springws.endpoint.BaseEndpoint;
import org.agnitas.emm.springws.endpoint.Utils;
import org.agnitas.emm.springws.jaxb.ListTargetgroupsRequest;
import org.agnitas.emm.springws.jaxb.ListTargetgroupsResponse;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import com.agnitas.beans.TargetLight;
import com.agnitas.emm.core.target.service.ComTargetService;

@Endpoint
public class ListTargetgroupsEndpoint extends BaseEndpoint {

    private ComTargetService targetService;

    public ListTargetgroupsEndpoint(ComTargetService targetService) {
        this.targetService = targetService;
    }

    @PayloadRoot(namespace = Utils.NAMESPACE_ORG, localPart = "ListTargetgroupsRequest")
    public @ResponsePayload ListTargetgroupsResponse listTargetgroups(@RequestPayload ListTargetgroupsRequest request) {
        ListTargetgroupsResponse response = new ListTargetgroupsResponse();

        for (TargetLight target : targetService.getWsTargetLights(Utils.getUserCompany())) {
            ListTargetgroupsResponse.Item targetgroup = new ListTargetgroupsResponse.Item();
            targetgroup.setId(target.getId());
            targetgroup.setName(target.getTargetName());
            response.getItem().add(targetgroup);
        }

        return response;
    }
}
