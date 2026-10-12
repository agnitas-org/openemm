/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.spring.security.config;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import com.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.core.commons.util.ConfigValue;
import com.agnitas.gui.oauth.DbOAuth2AuthorizationRequestRepository;
import com.agnitas.gui.oauth.OAuthBindingSuccessHandler;
import com.agnitas.spring.security.oauth2.AdminLogonAuthenticationProvider;
import com.agnitas.spring.security.web.AgnCsrfFilter;
import com.auth0.jwt.JWT;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.FactorGrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.ClientRegistrations;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.security.web.savedrequest.CookieRequestCache;

@Configuration
@EnableWebSecurity
public class WebSecurityConfiguration {

    private static final Logger LOGGER = LogManager.getLogger(WebSecurityConfiguration.class);

    private final ConfigService configService;
    private final OAuthBindingSuccessHandler authBindingSuccessHandler;
    private final DbOAuth2AuthorizationRequestRepository authorizationRequestRepository;
    private final AdminLogonAuthenticationProvider adminLogonAuthenticationProvider;

    @Autowired
    public WebSecurityConfiguration(
            ConfigService configService,
            OAuthBindingSuccessHandler authBindingSuccessHandler,
            DbOAuth2AuthorizationRequestRepository authorizationRequestRepository,
            @Lazy AdminLogonAuthenticationProvider adminLogonAuthenticationProvider
    ) {
        this.configService = configService;
        this.authBindingSuccessHandler = authBindingSuccessHandler;
        this.authorizationRequestRepository = authorizationRequestRepository;
        this.adminLogonAuthenticationProvider = adminLogonAuthenticationProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http.csrf(conf -> {
            if (!isCsrfProtectionEnabled()) {
                conf.disable();
                LOGGER.warn("CSRF protection disabled");
            } else {
                modifySecurityFilterChain(http);
                LOGGER.info("***** CSRF protection ENABLED *****");
            }
        });

        if (isExternalOAuthConfigured()) {
            configureOAuth(http);
        }

        http.requestCache(rc -> rc.requestCache(new CookieRequestCache()));
        http.formLogin(form -> form.loginPage("/oauth/logon.action").loginProcessingUrl("/oauth/logon.action"));
        http.authenticationProvider(adminLogonAuthenticationProvider);

        allowSessionUrlRewriting(http);
        disableXFrameOptions(http);

        return http.build();
    }

    private void configureOAuth(HttpSecurity http) {
        http.oauth2Login(oauth ->
                oauth
                        .loginPage("/oauth/logon.action")
                        .authorizationEndpoint(endpoint ->
                                endpoint.authorizationRequestRepository(authorizationRequestRepository))
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(oAuth2UserService())
                                .oidcUserService(oidcUserService()))
                        .failureHandler(
                                (request, response, exception) ->
                                        LOGGER.error("OAuth2 authentication failed", exception))
                        .successHandler(authBindingSuccessHandler)
        );
    }

    private OidcUserService oidcUserService() {
        return new OidcUserService() {
            @Override
            public OidcUser loadUser(OidcUserRequest userRequest) {
                OidcUser user = super.loadUser(userRequest);
                Instant authTime = userRequest.getIdToken().getAuthenticatedAt();
                if (authTime == null) {
                    authTime = Instant.now();
                }
                FactorGrantedAuthority factor = FactorGrantedAuthority
                        .withAuthority(FactorGrantedAuthority.PASSWORD_AUTHORITY)
                        .issuedAt(authTime)
                        .build();
                Set<GrantedAuthority> authorities = new LinkedHashSet<>(user.getAuthorities());
                authorities.add(factor);
                return new DefaultOidcUser(authorities, user.getIdToken(), user.getUserInfo());
            }
        };
    }

    @Bean
    public OAuth2UserService<OAuth2UserRequest, OAuth2User> oAuth2UserService() {
        return userRequest -> {
            Map<String, Object> attributes = JWT.decode(userRequest.getAccessToken().getTokenValue())
                    .getClaims()
                    .entrySet()
                    .stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            e -> e.getValue().as(Object.class)
                    ));

            return new DefaultOAuth2User(
                    List.of(new SimpleGrantedAuthority("ROLE_USER")),
                    attributes,
                    "sub"
            );
        };
    }

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        if (!isExternalOAuthConfigured()) {
            return null;
        }

        String issuerLocation = configService.getValue(ConfigValue.OAuth2Issuer);
        String clientId = configService.getValue(ConfigValue.OAuth2ServerClientId);

        // Defer fromIssuerLocation() HTTP discovery to first login attempt so startup
        // succeeds even when the issuer URL is this same server (not yet fully up)
        return new ClientRegistrationRepository() {

            private final AtomicReference<ClientRegistrationRepository> delegate = new AtomicReference<>();

            @Override
            public ClientRegistration findByRegistrationId(@NonNull String registrationId) {
                ClientRegistrationRepository repo = delegate.get();
                if (repo == null) {
                    synchronized (this) {
                        repo = delegate.get();
                        if (repo == null) {
                            repo = new InMemoryClientRegistrationRepository(
                                ClientRegistrations.fromIssuerLocation(issuerLocation)
                                    .registrationId(OAuth2ConfigConstants.EMM_REGISTRATION_ID)
                                    .clientId(clientId)
                                    .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
                                    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                                    .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                                    .scope("openid")
                                    .build()
                            );
                            delegate.set(repo);
                        }
                    }
                }
                return repo.findByRegistrationId(registrationId);
            }
        };
    }

    /**
     * Create HTTP application firewall.
     * <p>
     * Firewall is configured to allow semicolons in URLs.
     *
     * @return HTTP application firewall.
     */
    @Bean
    public HttpFirewall customHttpFirewall() {
        StrictHttpFirewall firewall = new StrictHttpFirewall();
        firewall.setAllowSemicolon(true);
        return firewall;
    }

    /**
     * Checks if CSRF protection is enabled by database-driven configuration.
     *
     * @return <code>true</code> if CSRF protection is enabled
     */
    private boolean isCsrfProtectionEnabled() {
        return configService.getBooleanValue(ConfigValue.Security.CsrfProtectionEnabled);
    }

    private void allowSessionUrlRewriting(HttpSecurity http) {
        http.sessionManagement(conf -> conf.enableSessionUrlRewriting(true));
    }

    private void disableXFrameOptions(HttpSecurity http) {
        http.headers(conf ->
                conf.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable));
    }

    private void modifySecurityFilterChain(HttpSecurity http) {
        http.addFilterBefore(new AgnCsrfFilter(), CsrfFilter.class);
    }

    private boolean isExternalOAuthConfigured() {
        return configService.isOAuthConfigured() && !configService.isInternalOAuthServer();
    }

}
