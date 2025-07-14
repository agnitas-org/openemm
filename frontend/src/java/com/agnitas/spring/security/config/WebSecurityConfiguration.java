/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.spring.security.config;

import com.agnitas.spring.security.web.AgnCsrfFilter;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;

import java.util.Objects;

/**
 * Code based web security configuration.
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfiguration {

    private static final Logger LOGGER = LogManager.getLogger(WebSecurityConfiguration.class);

    /** Configuration service. */
    private final ConfigService configService;

    /**
     * Creates a new instance.
     *
     * @param configService configuration service.
     */
    @Autowired
    public WebSecurityConfiguration(ConfigService configService) {
        this.configService = Objects.requireNonNull(configService, "config service");
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(conf -> {
            if (!isCsrfProtectionEnabled()) {
                conf.disable();
                LOGGER.warn("CSRF protection disabled");
            } else {
                modifySecurityFilterChain(http);
                LOGGER.info("***** CSRF protection ENABLED *****");
            }
        });

        allowSessionUrlRewriting(http);
        disableXFrameOptions(http);

        return http.build();
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

    private void allowSessionUrlRewriting(HttpSecurity http) throws Exception {
        http.sessionManagement(conf -> conf.enableSessionUrlRewriting(true));
    }

    private void disableXFrameOptions(HttpSecurity http) throws Exception {
        http.headers(conf -> conf.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable));
    }

    private void modifySecurityFilterChain(HttpSecurity http) {
        http.addFilterBefore(new AgnCsrfFilter(), CsrfFilter.class);
    }
}
