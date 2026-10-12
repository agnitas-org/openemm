/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.mcp.v1.infrastructure.config;

import java.util.concurrent.atomic.AtomicReference;

import com.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.core.commons.util.ConfigValue;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.springaicommunity.mcp.security.server.config.McpServerOAuth2Configurer;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.filter.ForwardedHeaderFilter;

@Configuration
public class McpSecurityConfiguration {

    private static final String[] MCP_MATCHERS = {
            "/mcp/**",
            "/.well-known/**"
    };

    private final ConfigService configService;

    public McpSecurityConfiguration(ConfigService configService) {
        this.configService = configService;
    }

    @Bean
    public FilterRegistrationBean<ForwardedHeaderFilter> forwardedHeaderFilter() {
        FilterRegistrationBean<ForwardedHeaderFilter> registration =
                new FilterRegistrationBean<>(new ForwardedHeaderFilter());
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }

    @Bean
    @Order(1)
    public SecurityFilterChain mcpSecurityFilterChain(HttpSecurity http) {
        String issuer = configService.getValue(ConfigValue.OAuth2Issuer);

        if (StringUtils.isBlank(issuer)) {
            return http
                    .securityMatcher(MCP_MATCHERS)
                    .authorizeHttpRequests(auth -> auth.anyRequest().denyAll())
                    .build();
        }

        return http
                .securityMatcher(MCP_MATCHERS)
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .with(
                        McpServerOAuth2Configurer.mcpServerOAuth2(),
                        mcpAuthorization -> {
                            mcpAuthorization.authorizationServer(issuer);
                            mcpAuthorization.jwtDecoder(lazyJwtDecoder(issuer));
                            mcpAuthorization.validateAudienceClaim(false);
                        }
                )
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .build();
    }

    private static JwtDecoder lazyJwtDecoder(String issuer) {
        return new JwtDecoder() {

            private final AtomicReference<JwtDecoder> delegate = new AtomicReference<>();

            @Override
            public @NonNull Jwt decode(@NonNull String token) throws JwtException {
                JwtDecoder decoder = delegate.get();
                if (decoder == null) {
                    synchronized (this) {
                        decoder = delegate.get();
                        if (decoder == null) {
                            decoder = NimbusJwtDecoder.withIssuerLocation(issuer).build();
                            delegate.set(decoder);
                        }
                    }
                }
                return decoder.decode(token);
            }
        };
    }

}
