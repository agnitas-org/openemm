/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.restful.v2.infrastructure.config;

import com.agnitas.emm.restful.v2.infrastructure.security.auth.PermissionAllowedExpressionHandler;
import com.agnitas.emm.restful.v2.infrastructure.security.auth.RestAuthenticationEntryPoint;
import com.agnitas.emm.restful.v2.infrastructure.security.auth.RestAuthenticationProvider;
import com.agnitas.emm.restful.v2.infrastructure.security.jwt.filter.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class RestSecurityConfiguration {

    private static final String BASE_PATH = "/rest/v2";

    private final RestAuthenticationProvider restAuthenticationProvider;
    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public RestSecurityConfiguration(RestAuthenticationProvider restAuthenticationProvider,
                                     RestAuthenticationEntryPoint restAuthenticationEntryPoint,
                                     JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.restAuthenticationProvider = restAuthenticationProvider;
        this.restAuthenticationEntryPoint = restAuthenticationEntryPoint;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain restSecurityFilterChain(HttpSecurity http) {
        http
            .securityMatcher(PathPatternRequestMatcher.withDefaults().basePath(BASE_PATH).matcher("/**"))
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(conf -> conf.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authenticationProvider(restAuthenticationProvider)
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers(
                            PathPatternRequestMatcher.withDefaults().basePath(BASE_PATH).matcher("/openapi"),
                            PathPatternRequestMatcher.withDefaults().basePath(BASE_PATH).matcher("/ui/**")
                    ).permitAll() // Allow public access
                    .anyRequest().authenticated())
            .httpBasic(conf -> conf.authenticationEntryPoint(restAuthenticationEntryPoint))
            .addFilterBefore(jwtAuthenticationFilter,  BasicAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public MethodSecurityExpressionHandler methodSecurityExpressionHandler() {
        return new PermissionAllowedExpressionHandler();
    }
}
