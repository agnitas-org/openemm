/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.mcp.v1;

import java.util.Arrays;
import java.util.Optional;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.admin.service.AdminService;
import com.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.core.oauth.service.OAuth2ExternalIdentityService;
import com.agnitas.emm.mcp.v1.infrastructure.exception.McpAuthenticationException;
import com.agnitas.emm.mcp.v1.infrastructure.exception.McpPermissionDeniedException;
import com.agnitas.emm.mcp.v1.infrastructure.exception.McpUserNotFoundException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class McpAdminProvider {

    private static final Logger logger = LogManager.getLogger(McpAdminProvider.class);

    private final OAuth2ExternalIdentityService oAuth2ExternalIdentityService;
    private final ConfigService configService;
    private final AdminService adminService;

    public McpAdminProvider(
            OAuth2ExternalIdentityService oAuth2ExternalIdentityService,
            ConfigService configService,
            AdminService adminService
    ) {
        this.oAuth2ExternalIdentityService = oAuth2ExternalIdentityService;
        this.configService = configService;
        this.adminService = adminService;
    }

    /**
     * Resolves the current admin and checks it holds the given permissions, mirroring
     * {@code @PermissionAllowed} used by the REST v2 controllers. Spring AI's MCP tool invocation
     * does not support custom argument resolvers / method interceptors yet, so tools call this
     * explicitly instead of relying on an annotation + AOP/{@code @PreAuthorize}.
     */
    public Admin requireAdmin(Permission... permissions) {
        Admin admin = getCurrentAdmin();
        checkMcpPermission(admin);

        if (permissions.length > 0 && !admin.allowedAll(permissions)) {
            throw new McpPermissionDeniedException(
                    "User '%s' does not have permission '%s'".formatted(admin.getUsername(), Arrays.toString(permissions))
            );
        }

        return admin;
    }

    public Optional<Admin> resolveCurrentAdmin() {
        try {
            return Optional.of(getCurrentAdmin());
        } catch (McpAuthenticationException | McpUserNotFoundException e) {
            return Optional.empty();
        }
    }

    private Admin getCurrentAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new McpAuthenticationException("Not authenticated");
        }

        if (!(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new McpAuthenticationException("Invalid token type");
        }

        Object sub = jwt.getClaim("sub");
        if (sub == null) {
            throw new McpAuthenticationException("JWT is missing 'sub' claim");
        }

        Object iss = jwt.getClaim("iss");
        if (iss == null) {
            throw new McpAuthenticationException("JWT is missing 'iss' claim");
        }

        String subStr = sub.toString();

        if (configService.isInternalOAuthServer()) {
            return adminService.findByUsername(subStr)
                    .orElseThrow(() -> new McpUserNotFoundException("User '%s' not found".formatted(subStr)));
        }

        return oAuth2ExternalIdentityService.find(iss.toString(), subStr)
                .map(ir -> {
                    Admin admin = adminService.getAdmin(ir.adminId(), ir.companyId());
                    if (admin == null) {
                        logger.error("Admin {} not found for resolved OAuth identity", ir.adminId());
                        throw new McpUserNotFoundException("User not found for resolved identity");
                    }

                    return admin;
                })
                .orElseThrow(() -> new McpUserNotFoundException("OAuth identity not linked to any user account"));
    }

    private void checkMcpPermission(Admin admin) {
        if (!admin.permissionAllowed(Permission.MCP_ENABLE)) {
            throw new McpPermissionDeniedException("User '%s' does not have permission to use MCP".formatted(admin.getUsername()));
        }
    }

}
