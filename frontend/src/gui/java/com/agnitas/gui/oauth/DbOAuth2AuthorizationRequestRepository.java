/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.gui.oauth;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.oauth.model.OAuthRequestRecord;
import com.agnitas.emm.core.oauth.service.OAuth2AuthorizationRequestService;
import com.agnitas.util.AgnUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.jackson.SecurityJacksonModules;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
public class DbOAuth2AuthorizationRequestRepository implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    private static final Logger logger = LogManager.getLogger(DbOAuth2AuthorizationRequestRepository.class);
    private static final String STATE_PARAM = "state";

    private final OAuth2AuthorizationRequestService service;
    private final JsonMapper jsonMapper;

    @Autowired
    public DbOAuth2AuthorizationRequestRepository(OAuth2AuthorizationRequestService service) {
        this.service = service;
        jsonMapper = JsonMapper.builder()
                .addModules(SecurityJacksonModules.getModules(getClass().getClassLoader()))
                .build();
    }

    @Override
    public void saveAuthorizationRequest(
            OAuth2AuthorizationRequest authRequest,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        if (authRequest == null) {
            String state = request.getParameter(STATE_PARAM);
            if (state != null) {
                service.findAndRemove(state);
            }
            return;
        }

        Admin admin = AgnUtils.getAdmin(request);
        if (admin == null) {
            logger.error("Cannot save OAuth2 authorization request: no authenticated admin in session");
            return;
        }

        service.save(authRequest.getState(), jsonMapper.writeValueAsString(authRequest), admin);
    }

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        String state = request.getParameter(STATE_PARAM);
        if (state == null) {
            return null;
        }

        return service.find(state)
                .map(r -> deserialize(r.requestJson()))
                .orElse(null);
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String state = request.getParameter(STATE_PARAM);
        if (state == null) {
            return null;
        }

        return service.findAndRemove(state)
                .map(r -> {
                    // Pass context to OAuthBindingSuccessHandler within the same request
                    request.setAttribute(OAuthRequestRecord.REQUEST_ATTR, r);
                    return deserialize(r.requestJson());
                })
                .orElse(null);
    }

    private OAuth2AuthorizationRequest deserialize(String json) {
        return jsonMapper.readValue(json, OAuth2AuthorizationRequest.class);
    }
}
