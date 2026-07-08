/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.restful.v2.infrastructure.security.jwt.service.impl;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.Date;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.core.commons.util.ConfigValue;
import com.agnitas.emm.core.useractivitylog.dao.RestfulUserActivityLogDao;
import com.agnitas.emm.restful.v2.infrastructure.security.jwt.dto.JwtDto;
import com.agnitas.emm.restful.v2.infrastructure.security.jwt.service.JwtTokenService;
import com.agnitas.util.AgnUtils;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenServiceImpl implements JwtTokenService {

    private final ConfigService configService;
    private final RestfulUserActivityLogDao restfulUserActivityLogDao;

    public JwtTokenServiceImpl(ConfigService configService, RestfulUserActivityLogDao restfulUserActivityLogDao) {
        this.configService = configService;
        this.restfulUserActivityLogDao = restfulUserActivityLogDao;
    }

    @Override
    public void validate(String token, int companyId) {
        if (token == null || token.isBlank()) {
            throw new BadCredentialsException("Missing JWT token");
        }
        DecodedJWT decoded = JWT.decode(token);
        String username = decoded.getClaim("username").asString();
        if (username == null) {
            throw new BadCredentialsException("JWT token missing username claim");
        }

        String secret = configService.getValue(ConfigValue.RestfulJwtSecret, companyId);
        if (isBlank(secret)) {
            throw new BadCredentialsException("JWT auth not supported (no shared secret configured)");
        }

        Algorithm.HMAC512(secret).verify(decoded); // will throw JWTVerificationException on failure

        Date expiresAt = decoded.getExpiresAt();
        if (expiresAt == null || expiresAt.before(new Date())) {
            throw new BadCredentialsException("JWT token expired");
        }
    }

    @Override
    public JwtDto generateToken(Admin admin, HttpServletRequest request) {
        int validityMinutes = getValidityMinutes(admin.getCompanyID());
        String secret = getSecret(admin.getCompanyID());
        Date now = new Date();
        Date expiry = DateUtils.addMinutes(now, validityMinutes);
        String token = JWT.create()
            .withIssuedAt(now)
            .withExpiresAt(expiry)
            .withClaim("username", getUsernameClaim(admin))
            .sign(Algorithm.HMAC512(secret));
        writeUserLoginUal(admin, request);
        return new JwtDto(token, expiry.toInstant());
    }

    private String getSecret(int companyId) {
        String secret = configService.getValue(ConfigValue.RestfulJwtSecret, companyId);
        if (isBlank(secret)) {
            throw new AuthenticationServiceException("JWT login is not supported (no shared secret)");
        }
        return secret;
    }

    private int getValidityMinutes(int companyId) {
        int validityMinutes = configService.getIntegerValue(ConfigValue.RestfulJwtValidityMinutes, companyId);
        if (validityMinutes <= 0) {
            throw new AuthenticationServiceException("Login for JWT is not supported");
        }
        return validityMinutes;
    }

    private static String getUsernameClaim(Admin admin) {
        String usernameClaim = admin.getUsername();
        if (admin.getSupervisor() != null) {
            usernameClaim = usernameClaim + "/" + admin.getSupervisor().getSupervisorName();
        }
        return usernameClaim;
    }

    private void writeUserLoginUal(Admin admin, HttpServletRequest request) {
        restfulUserActivityLogDao.addAdminUseOfFeature(admin, "restful/login", new Date());
        String host = isBlank(request.getHeader("Host")) ?  AgnUtils.getHostName() : request.getHeader("Host");
        restfulUserActivityLogDao.writeUserActivityLog(
            request.getRequestURI(), admin.getFullUsername(),
            request.getMethod(), host, admin);
    }
}
