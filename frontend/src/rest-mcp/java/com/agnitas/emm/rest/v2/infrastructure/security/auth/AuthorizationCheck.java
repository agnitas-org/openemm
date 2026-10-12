/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2.infrastructure.security.auth;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.Permission;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@SuppressWarnings("unused") // used in @PermissionAllowed
@Component("authorizationCheck")
public class AuthorizationCheck {

    private static final Logger logger = LogManager.getLogger(AuthorizationCheck.class);

    public boolean hasPermission(Permission... permissions) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Admin admin)) {
            throw new AuthenticationCredentialsNotFoundException("Not authenticated");
        }
        if (!admin.permissionAllowed(Permission.REST_API_SPRING)) {
            logger.error("No permission to use this REST API");
            throw new AuthorizationDeniedException("User is not allowed to access REST api");
        }
        return admin.allowedAll(permissions);
    }
}
