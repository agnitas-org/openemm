/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.restful.v2.infrastructure.security.jwt;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import com.agnitas.beans.Admin;
import com.agnitas.emm.restful.v2.infrastructure.security.jwt.service.JwtTokenService;
import com.agnitas.emm.restful.v2.infrastructure.security.jwt.dto.JwtDto;
import com.agnitas.util.HttpUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginRestV2Controller {

    private final JwtTokenService jwtTokenService;

    public LoginRestV2Controller(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    @PostMapping("/login")
    public JwtDto loginForJwt(Admin admin, HttpServletRequest request) {
        if (isNotBlank(HttpUtils.getAuthorizationToken(request))) {
            throw new BadCredentialsException("Already authenticated. Use basic auth to create new JWT token.");
        }
        return jwtTokenService.generateToken(admin, request);
    }
}
