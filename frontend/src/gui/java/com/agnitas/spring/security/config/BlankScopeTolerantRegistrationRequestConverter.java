/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.spring.security.config;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import com.nimbusds.jose.util.JSONObjectUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.server.authorization.oidc.OidcClientRegistration;
import org.springframework.security.oauth2.server.authorization.oidc.authentication.OidcClientRegistrationAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.oidc.http.converter.OidcClientRegistrationHttpMessageConverter;
import org.springframework.security.oauth2.server.authorization.oidc.web.authentication.OidcClientRegistrationAuthenticationConverter;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.util.FileCopyUtils;

/**
 * Dynamic client registration request converter that treats a blank {@code scope} as absent.
 * <p>
 * Spring's default {@link OidcClientRegistrationAuthenticationConverter} rejects a registration request whose
 * {@code scope} is present but blank ("scope cannot be empty"). Generic MCP clients such as MCP Inspector send
 * exactly that when the server advertises no scopes. RFC 7591 treats {@code scope} as optional, so a blank value
 * is dropped before the request is handed to Spring's own parsing; everything else is left to the default
 * behaviour.
 */
class BlankScopeTolerantRegistrationRequestConverter implements AuthenticationConverter {

    private static final String SCOPE = "scope";

    private final AuthenticationConverter defaultConverter = new OidcClientRegistrationAuthenticationConverter();
    private final HttpMessageConverter<OidcClientRegistration> messageConverter = new OidcClientRegistrationHttpMessageConverter();

    @Override
    public Authentication convert(HttpServletRequest request) {
        if (!"POST".equals(request.getMethod())) {
            return defaultConverter.convert(request);
        }

        Authentication principal = SecurityContextHolder.getContext().getAuthentication();
        if (principal == null) {
            throw new IllegalArgumentException("current principal cannot be null");
        }

        try {
            Map<String, Object> claims = JSONObjectUtils.parse(FileCopyUtils.copyToString(request.getReader()));
            if (claims.get(SCOPE) instanceof String scope && scope.isBlank()) {
                claims.remove(SCOPE);
            }

            byte[] body = JSONObjectUtils.toJSONString(claims).getBytes(StandardCharsets.UTF_8);
            OidcClientRegistration registration = messageConverter.read(OidcClientRegistration.class, new HttpInputMessage() {
                @Override
                public InputStream getBody() {
                    return new ByteArrayInputStream(body);
                }

                @Override
                public HttpHeaders getHeaders() {
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    return headers;
                }
            });
            return new OidcClientRegistrationAuthenticationToken(principal, registration);
        } catch (Exception e) {
            throw new OAuth2AuthenticationException(new OAuth2Error(
                    OAuth2ErrorCodes.INVALID_REQUEST,
                    "OpenID Client Registration Error: " + e.getMessage(),
                    "https://openid.net/specs/openid-connect-registration-1_0.html#RegistrationError"), e);
        }
    }

}
