/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.spring.security.config;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.UUID;

import com.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.core.commons.util.ConfigValue;
import com.agnitas.emm.core.logon.service.LogonService;
import com.agnitas.emm.core.oauth.dao.OAuth2RegistrationClientDao;
import com.agnitas.spring.security.oauth2.AdminLogonAuthenticationProvider;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.authorization.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.context.AuthorizationServerContextHolder;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.savedrequest.CookieRequestCache;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.util.UriComponentsBuilder;

@Configuration
public class AuthorizationServerConfiguration {

    private final ConfigService configService;
    private final LogonService logonService;
    private final OAuth2RegistrationClientDao oauth2RegistrationClientDao;

    public AuthorizationServerConfiguration(
            ConfigService configService,
            LogonService logonService,
            OAuth2RegistrationClientDao oauth2RegistrationClientDao
    ) {
        this.configService = configService;
        this.logonService = logonService;
        this.oauth2RegistrationClientDao = oauth2RegistrationClientDao;
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SecurityFilterChain authorizationServerFilterChain(HttpSecurity http) {
        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer = new OAuth2AuthorizationServerConfigurer();
        RequestMatcher endpointsMatcher = authorizationServerConfigurer.getEndpointsMatcher();

        http.securityMatcher(endpointsMatcher);
        http.with(authorizationServerConfigurer, configurer ->
                configurer.oidc(oidc -> oidc
                        .clientRegistrationEndpoint(endpoint -> endpoint
                                .clientRegistrationRequestConverters(converters -> {
                                    converters.clear();
                                    converters.add(new BlankScopeTolerantRegistrationRequestConverter());
                                })
                                .authenticationProviders(providers -> {
                                    providers.clear();
                                    providers.add(new OpenDynamicClientRegistrationProvider(registeredClientRepository()));
                                })
                        )
                ).authorizationServerMetadataEndpoint(metadata -> metadata
                        .authorizationServerMetadataCustomizer(builder -> builder
                                .clientRegistrationEndpoint(
                                        UriComponentsBuilder.fromUriString(AuthorizationServerContextHolder.getContext().getIssuer())
                                                .path("/connect/register")
                                                .build()
                                                .toUriString()
                                )
                        )
                ));

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.POST, "/connect/register").permitAll()
                .anyRequest().authenticated()
        );

        http.requestCache(rc -> rc.requestCache(new CookieRequestCache()));
        http.csrf(csrf -> csrf.ignoringRequestMatchers(endpointsMatcher));
        http.exceptionHandling(e -> e.defaultAuthenticationEntryPointFor(
                new LoginUrlAuthenticationEntryPoint("/oauth/logon.action"),
                new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
        ));

        return http.build();
    }

    @Bean
    public AdminLogonAuthenticationProvider adminLogonAuthenticationProvider() {
        return new AdminLogonAuthenticationProvider(logonService);
    }

    @Bean
    public RegisteredClientRepository registeredClientRepository() {
        OAuth2RegisteredClientRepository repo = new OAuth2RegisteredClientRepository(oauth2RegistrationClientDao);

        if (!configService.isInternalOAuthServer() && configService.isOAuthConfigured()) {
            registerConfiguredClient(repo);
        }

        return repo;
    }

    private void registerConfiguredClient(RegisteredClientRepository repo) {
        String clientId = configService.getValue(ConfigValue.OAuth2ServerClientId);

        String stableId = UUID.nameUUIDFromBytes(clientId.getBytes(StandardCharsets.UTF_8)).toString();
        String issuer = configService.getValue(ConfigValue.OAuth2Issuer);

        RegisteredClient client = RegisteredClient.withId(stableId)
                .clientId(clientId)
                .clientName(clientId)
                .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
                .clientSettings(ClientSettings.builder().requireProofKey(true).build())
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .redirectUri(issuer + "/login/oauth2/code/" + OAuth2ConfigConstants.EMM_REGISTRATION_ID)
                .scope(OidcScopes.OPENID)
                .scope(OidcScopes.PROFILE)
                .build();

        repo.save(client);
    }

    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        String issuer = configService.getValue(ConfigValue.OAuth2Issuer);
        AuthorizationServerSettings.Builder builder = AuthorizationServerSettings.builder();
        if (StringUtils.isNotBlank(issuer)) {
            builder.issuer(issuer);
        }
        return builder.build();
    }

    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        try {
            KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
            gen.initialize(2048);
            KeyPair kp = gen.generateKeyPair();
            RSAKey rsaKey = new RSAKey.Builder((RSAPublicKey) kp.getPublic())
                    .privateKey((RSAPrivateKey) kp.getPrivate())
                    .keyID(UUID.randomUUID().toString())
                    .build();
            return new ImmutableJWKSet<>(new JWKSet(rsaKey));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to generate RSA signing key", e);
        }
    }

    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

}
