/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.restful.v2.infrastructure.security.jwt.filter;

import static java.util.Collections.emptyList;

import java.io.IOException;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.logon.service.LogonService;
import com.agnitas.emm.restful.v2.infrastructure.security.auth.RestAuthenticationEntryPoint;
import com.agnitas.emm.restful.v2.infrastructure.security.jwt.service.JwtTokenService;
import com.agnitas.util.HttpUtils;
import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final LogonService logonService;
    private final JwtTokenService jwtTokenService;
    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;

    public JwtAuthenticationFilter(LogonService logonService,
                                   JwtTokenService jwtTokenService,
                                   RestAuthenticationEntryPoint restAuthenticationEntryPoint) {
        this.logonService = logonService;
        this.jwtTokenService = jwtTokenService;
        this.restAuthenticationEntryPoint = restAuthenticationEntryPoint;
    }

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String jwtStr = HttpUtils.getAuthorizationToken(request);

        if (jwtStr == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            DecodedJWT jwt = JWT.decode(jwtStr);
            Admin admin = logonService.getAdminByUsername(jwt.getClaim("username").asString());

            if (!admin.permissionAllowed(Permission.REST_V2_MIGRATION)) {
                throw new InsufficientAuthenticationException("User is not allowed to access rest api");
            }

            jwtTokenService.validate(jwt.getToken(), admin.getCompanyID());
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                admin, null, emptyList()
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
            restAuthenticationEntryPoint.commence(
                request, response, new InsufficientAuthenticationException(e.getMessage(), e)
            );
            return;
        }
        filterChain.doFilter(request, response);
    }
}
