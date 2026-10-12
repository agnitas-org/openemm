/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.gui.oauth;

import java.io.IOException;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.admin.service.AdminService;
import com.agnitas.emm.core.logon.service.AdminSessionService;
import com.agnitas.emm.core.oauth.exception.OAuthIdentityAlreadyLinkedException;
import com.agnitas.emm.core.oauth.model.OAuthRequestRecord;
import com.agnitas.emm.core.oauth.service.OAuth2ExternalIdentityService;
import com.agnitas.util.AgnUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class OAuthBindingSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger logger = LogManager.getLogger(OAuthBindingSuccessHandler.class);

    private final AdminService adminService;
    private final OAuth2ExternalIdentityService oAuth2ExternalIdentityService;
    private final AdminSessionService adminSessionService;

    @Autowired
    public OAuthBindingSuccessHandler(
            AdminService adminService,
            OAuth2ExternalIdentityService oAuth2ExternalIdentityService,
            AdminSessionService adminSessionService
    ) {
        this.adminService = adminService;
        this.oAuth2ExternalIdentityService = oAuth2ExternalIdentityService;
        this.adminSessionService = adminSessionService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        if (!(authentication instanceof OAuth2AuthenticationToken token)) {
            logger.error("OAuth binding failed: unexpected authentication type: {}", authentication.getClass().getName());
            throw new IllegalStateException("Unexpected authentication type: " + authentication.getClass().getName());
        }

        OAuth2User principal = token.getPrincipal();
        if (principal == null) {
            logger.error("OAuth binding failed: OAuth2AuthenticationToken has no principal");
            throw new IllegalStateException("OAuth2AuthenticationToken has no principal");
        }

        String sub = principal.getName();
        if (StringUtils.isEmpty(sub)) {
            logger.error("OAuth binding failed: OAuth2 principal name (sub) is empty");
            throw new IllegalStateException("OAuth2 principal name (sub) is empty");
        }

        Object iss = principal.getAttribute("iss");
        if (iss == null) {
            logger.error("OAuth binding failed: OAuth2 principal is missing 'iss' attribute");
            throw new IllegalStateException("OAuth2 principal is missing 'iss' attribute");
        }

        String issuer = iss.toString();

        if (!(request.getAttribute(OAuthRequestRecord.REQUEST_ATTR) instanceof OAuthRequestRecord reqRecord)) {
            logger.error("OAuth binding failed: OAuthRequestRecord missing from request attributes");
            redirectToLogin(request, response);
            return;
        }

        Admin admin = adminService.getAdmin(reqRecord.adminId(), reqRecord.companyId());
        if (admin == null) {
            logger.error("OAuth binding failed: admin {} not found", reqRecord.adminId());
            redirectToLogin(request, response);
            return;
        }

        logger.info("OAuth binding: sub={}, iss={}, adminId={}", sub, issuer, admin.getAdminID());

        try {
            oAuth2ExternalIdentityService.link(issuer, sub, admin.getAdminID(), admin.getCompanyID());
        } catch (OAuthIdentityAlreadyLinkedException e) {
            logger.warn("OAuth binding conflict: sub={}, iss={} already linked to adminId={}, attempted by adminId={}",
                    sub, issuer, e.getLinkedAdminId(), admin.getAdminID());
            sendRedirect("/user/self/oauth/link-conflict.action", request, response);
            return;
        }

        Admin sessionAdmin = AgnUtils.getAdmin(request);
        if (sessionAdmin == null || sessionAdmin.getAdminID() != admin.getAdminID()) {
            establishSession(request, response, admin);
            adminSessionService.fillSession(admin, request, response);
        }

        sendRedirect("/user/self/oauth/linked.action", request, response);
    }

    private void redirectToLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        sendRedirect("/logon.action", request, response);
    }

    private void sendRedirect(String url, HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendRedirect(response.encodeRedirectURL(request.getContextPath() + url));
    }

    private void establishSession(HttpServletRequest request, HttpServletResponse response, Admin admin) {
        request.getSession(true);
        adminSessionService.fillSession(admin, request, response);
    }

}
