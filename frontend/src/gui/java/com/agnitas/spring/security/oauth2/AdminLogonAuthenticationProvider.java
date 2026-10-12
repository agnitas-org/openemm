/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.spring.security.oauth2;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.commons.password.PasswordState;
import com.agnitas.emm.core.logon.service.LogonService;
import com.agnitas.service.ServiceResult;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.FactorGrantedAuthority;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class AdminLogonAuthenticationProvider implements AuthenticationProvider {

    private static final String UNKNOWN_IP = "unknown";

    private final LogonService logonService;

    public AdminLogonAuthenticationProvider(LogonService logonService) {
        this.logonService = logonService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = Optional.ofNullable(authentication.getCredentials())
                .map(Object::toString)
                .orElse(null);

        ServiceResult<Admin> result = logonService.authenticate(username, password, resolveClientIp());
        if (!result.isSuccess()) {
            throw new BadCredentialsException("Invalid username or password");
        }

        Admin admin = result.getResult();
        if (!admin.isRestful()) {
            throw new DisabledException("Web login is not permitted for GUI-only accounts");
        }

        PasswordState state = logonService.getPasswordState(admin);
        switch (state) {
            case EXPIRED_LOCKED ->
                    throw new LockedException("Password has expired and the account is locked");
            case EXPIRED ->
                    throw new CredentialsExpiredException("Password has expired and must be changed before logging in");
            case ONE_TIME ->
                    throw new CredentialsExpiredException("A one-time password must be changed before logging in");
            case VALID, EXPIRING -> {
                // allowed
            }
        }

        FactorGrantedAuthority factor = FactorGrantedAuthority.withAuthority(FactorGrantedAuthority.PASSWORD_AUTHORITY)
                .issuedAt(Instant.now())
                .build();

        return UsernamePasswordAuthenticationToken.authenticated(username, null, List.of(factor));
    }

    @Override
    public boolean supports(@NonNull Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }

    private String resolveClientIp() {
        try {
            return Optional.ofNullable((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                    .map(a -> a.getRequest().getRemoteAddr())
                    .orElse(UNKNOWN_IP);
        } catch (Exception e) {
            return UNKNOWN_IP;
        }
    }

}
