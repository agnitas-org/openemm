/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.springws.endpoint.dyntarget;

import java.util.Objects;

import com.agnitas.emm.springws.endpoint.BaseEndpoint;
import com.agnitas.emm.springws.endpoint.Namespaces;
import com.agnitas.emm.springws.jaxb.ListTargetgroupsRequest;
import com.agnitas.emm.springws.jaxb.ListTargetgroupsResponse;
import com.agnitas.emm.springws.util.SecurityContextAccess;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import com.agnitas.beans.TargetLight;
import com.agnitas.emm.core.target.service.TargetService;

@Endpoint
public class ListTargetgroupsEndpoint extends BaseEndpoint {

    private final TargetService targetService;
    private final SecurityContextAccess securityContextAccess;

    public ListTargetgroupsEndpoint(TargetService targetService, final SecurityContextAccess securityContextAccess) {
        this.targetService = Objects.requireNonNull(targetService, "targetService");
        this.securityContextAccess = Objects.requireNonNull(securityContextAccess, "securityContextAccess");
    }

    @PayloadRoot(namespace = Namespaces.AGNITAS_ORG, localPart = "ListTargetgroupsRequest")
    public @ResponsePayload ListTargetgroupsResponse listTargetgroups(@RequestPayload ListTargetgroupsRequest request) {
    	final ListTargetgroupsResponse response = new ListTargetgroupsResponse();

        for (final TargetLight target : targetService.getWsTargetLights(this.securityContextAccess.getWebserviceUserCompanyId())) {
            final ListTargetgroupsResponse.Item targetgroup = new ListTargetgroupsResponse.Item();
            targetgroup.setId(target.getId());
            targetgroup.setName(target.getTargetName());
            response.getItem().add(targetgroup);
        }

        return response;
    }
}
