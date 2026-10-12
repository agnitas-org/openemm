/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2.infrastructure.security.jwt;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import com.agnitas.beans.Admin;
import com.agnitas.emm.rest.v2.infrastructure.exception.BadRequestException;
import com.agnitas.emm.rest.v2.infrastructure.security.jwt.dto.JwtResponse;
import com.agnitas.emm.rest.v2.infrastructure.security.jwt.service.JwtRestService;
import com.agnitas.util.HttpUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthV2Controller {

    private final JwtRestService jwtRestService;

    public AuthV2Controller(JwtRestService jwtRestService) {
        this.jwtRestService = jwtRestService;
    }

    @PostMapping("/jwt")
    public JwtResponse generateJwtToken(Admin admin, HttpServletRequest request) {
        if (isNotBlank(HttpUtils.getAuthorizationToken(request))) {
            throw new BadRequestException("Already authenticated. Use basic auth to create new JWT token.");
        }
        return jwtRestService.generateToken(admin, request);
    }
}
