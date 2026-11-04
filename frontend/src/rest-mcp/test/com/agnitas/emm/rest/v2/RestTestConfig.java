/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2;

import com.agnitas.emm.core.logon.service.LogonService;
import com.agnitas.emm.rest.v2.infrastructure.config.RestUserArgumentResolver;
import com.agnitas.emm.rest.v2.infrastructure.config.UserActionLoggingInterceptor;
import com.agnitas.emm.rest.v2.infrastructure.config.json.EnumConverterFactory;
import com.agnitas.emm.rest.v2.infrastructure.exception.RestValidationFieldTranslator;
import com.agnitas.emm.rest.v2.infrastructure.ratelimit.RateLimitInterceptor;
import com.agnitas.emm.rest.v2.infrastructure.ratelimit.RestV2QuotaService;
import com.agnitas.emm.rest.v2.infrastructure.security.auth.AuthorizationCheck;
import com.agnitas.emm.rest.v2.infrastructure.security.auth.RestAuthenticationEntryPoint;
import com.agnitas.emm.rest.v2.infrastructure.security.auth.RestAuthenticationProvider;
import com.agnitas.emm.rest.v2.infrastructure.security.jwt.filter.JwtAuthenticationFilter;
import com.agnitas.emm.rest.v2.infrastructure.security.xss.RestXssInterceptor;
import com.agnitas.emm.rest.v2.infrastructure.security.xss.RestXssPreventerHelper;
import com.agnitas.service.UserActivityLogService;
import org.easymock.EasyMock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@EnableWebMvc
class RestTestConfig {

    @Bean
    public static MethodValidationPostProcessor methodValidationPostProcessor() {
        return new MethodValidationPostProcessor();
    }

    @Bean
    UserActivityLogService userActivityLogService() {
        return EasyMock.createMock(UserActivityLogService.class);
    }

    @Bean
    UserActionLoggingInterceptor UserActionLoggingInterceptor(UserActivityLogService userActivityLogService) {
        return new UserActionLoggingInterceptor(userActivityLogService);
    }

    @Bean
    EnumConverterFactory CodeEnumConverterFactory() {
        return new EnumConverterFactory();
    }

    @Bean
    RestUserArgumentResolver restUserArgumentResolver() {
        return new RestUserArgumentResolver();
    }

    @Bean
    JwtAuthenticationFilter jwtAuthenticationFilter() {
        return EasyMock.createMock(JwtAuthenticationFilter.class);
    }

    @Bean
    RestAuthenticationProvider restAuthenticationProvider() {
        return EasyMock.createMock(RestAuthenticationProvider.class);
    }

    @Bean
    LogonService logonService() {
        return EasyMock.createMock(LogonService.class);
    }

    @Bean
    RestAuthenticationEntryPoint restAuthenticationEntryPoint() {
        return new RestAuthenticationEntryPoint(null);
    }

    @Bean
    RestXssPreventerHelper restXssPreventerHelper() {
        return new RestXssPreventerHelper();
    }

    @Bean
    RestXssInterceptor restXssInterceptor(RestXssPreventerHelper preventerHelper) {
        return new RestXssInterceptor(preventerHelper);
    }

    @Bean
    RestV2QuotaService restV2QuotaService() {
        return EasyMock.createMock(RestV2QuotaService.class);
    }

    @Bean
    RateLimitInterceptor rateLimitInterceptor(RestV2QuotaService quotaService) {
        return new RateLimitInterceptor(quotaService);
    }

    @Bean
    AuthorizationCheck authorizationCheck() {
        return new AuthorizationCheck();
    }

    @Bean
    RestValidationFieldTranslator restValidationFieldTranslator() {
        return new RestValidationFieldTranslator();
    }
}
