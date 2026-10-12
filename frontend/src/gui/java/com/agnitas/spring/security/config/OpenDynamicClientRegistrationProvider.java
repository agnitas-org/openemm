/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.spring.security.config;

import java.time.Duration;

import org.jspecify.annotations.NonNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.oidc.OidcClientRegistration;
import org.springframework.security.oauth2.server.authorization.oidc.authentication.OidcClientRegistrationAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.oidc.converter.OidcClientRegistrationRegisteredClientConverter;
import org.springframework.security.oauth2.server.authorization.oidc.converter.RegisteredClientOidcClientRegistrationConverter;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;

// Allows any client to register dynamically without a prior access token.
// Required by the MCP Authorization spec (2025-11-05) for OAuth2 clients like Claude Code.
class OpenDynamicClientRegistrationProvider implements AuthenticationProvider {

    private final RegisteredClientRepository registeredClientRepository;
    private final Converter<OidcClientRegistration, RegisteredClient> toRegisteredClient;
    private final Converter<RegisteredClient, OidcClientRegistration> toClientRegistration;

    OpenDynamicClientRegistrationProvider(RegisteredClientRepository repository) {
        this.registeredClientRepository = repository;
        toRegisteredClient = new OidcClientRegistrationRegisteredClientConverter();
        toClientRegistration = new RegisteredClientOidcClientRegistrationConverter();
    }

    @Override
    public Authentication authenticate(@NonNull Authentication authentication) throws AuthenticationException {
        OidcClientRegistrationAuthenticationToken token = (OidcClientRegistrationAuthenticationToken) authentication;
        if (token.getClientRegistration() == null) {
            return null;
        }

        OidcClientRegistration clientReg = token.getClientRegistration();
        RegisteredClient registeredClient = toRegisteredClient.convert(clientReg);

        registeredClient = RegisteredClient.from(registeredClient)
                .clientSecret(null)
                .clientSecretExpiresAt(null)
                .clientAuthenticationMethods(methods -> {
                    methods.clear();
                    methods.add(ClientAuthenticationMethod.NONE);
                })
                .clientSettings(ClientSettings.builder().requireProofKey(true).build())
                .build();

        if (!registeredClient.getScopes().contains(OidcScopes.OPENID)) {
            registeredClient = RegisteredClient.from(registeredClient)
                    .scope(OidcScopes.OPENID)
                    .build();
        }

        // Spring's OAuth2RefreshTokenGenerator never issues a refresh token to a public
        // (ClientAuthenticationMethod.NONE) client, regardless of granted authorization
        // grant types - a deliberate default, not something to override for an openly
        // self-registerable client. So the access token's default 5-minute lifetime is the
        // client's *entire* session length before it must fall back to a full interactive
        // re-authorization. Extend it well beyond the default to make that a rare event
        // instead of a routine one.
        registeredClient = RegisteredClient.from(registeredClient)
                .tokenSettings(TokenSettings.withSettings(registeredClient.getTokenSettings().getSettings())
                        .accessTokenTimeToLive(Duration.ofHours(12))
                        .build())
                .build();

        registeredClientRepository.save(registeredClient);
        OidcClientRegistration response = toClientRegistration.convert(registeredClient);
        OidcClientRegistrationAuthenticationToken result = new OidcClientRegistrationAuthenticationToken(
                (Authentication) token.getPrincipal(),
                response
        );
        result.setAuthenticated(true);
        return result;
    }

    @Override
    public boolean supports(@NonNull Class<?> authentication) {
        return OidcClientRegistrationAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
