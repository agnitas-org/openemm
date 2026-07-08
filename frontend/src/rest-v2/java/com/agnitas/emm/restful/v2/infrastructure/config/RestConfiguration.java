/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.restful.v2.infrastructure.config;

import java.util.List;

import com.agnitas.emm.restful.v2.infrastructure.ratelimit.RateLimitInterceptor;
import com.agnitas.emm.restful.v2.infrastructure.security.xss.RestXssInterceptor;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openapitools.jackson.nullable.JsonNullableModule;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Profile;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverters;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

@Configuration
@ComponentScan(
    basePackages = "com.agnitas.emm.restful.v2",
    useDefaultFilters = false,
    includeFilters = {
        @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = Controller.class),
        @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = RestController.class),
        @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = ControllerAdvice.class),
        @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = Configuration.class)
    },
    lazyInit = true
)
public class RestConfiguration implements WebMvcConfigurer {

    private static final String[] PUBLIC_PATHS = {
            "/openapi/**",
            "/ui/**"
    };

    private final RestUserArgumentResolver userArgumentResolver;
    private final RestXssInterceptor xssInterceptor;
    private final UserActionLoggingInterceptor userActionLoggingInterceptor;
    private final RateLimitInterceptor rateLimitInterceptor;

    public RestConfiguration(RestUserArgumentResolver userArgumentResolver,
                             RestXssInterceptor xssInterceptor,
                             UserActionLoggingInterceptor userActionLoggingInterceptor,
                             RateLimitInterceptor rateLimitInterceptor) {
        this.userArgumentResolver = userArgumentResolver;
        this.xssInterceptor = xssInterceptor;
        this.userActionLoggingInterceptor = userActionLoggingInterceptor;
        this.rateLimitInterceptor = rateLimitInterceptor;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/ui/**")
                .addResourceLocations("classpath:/static/swagger-ui/")
                .resourceChain(true)
                .addResolver(new PathResourceResolver());
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitInterceptor).excludePathPatterns(PUBLIC_PATHS);
        registry.addInterceptor(xssInterceptor).excludePathPatterns(PUBLIC_PATHS);
        registry.addInterceptor(userActionLoggingInterceptor).excludePathPatterns(PUBLIC_PATHS);
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(userArgumentResolver);
    }

    @Override
    public void configureMessageConverters(HttpMessageConverters.ServerBuilder builder) {
        WebMvcConfigurer.super.configureMessageConverters(builder);
        builder.withJsonConverter(jsonMessageConverter());
    }

    private HttpMessageConverter<?> jsonMessageConverter() {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();

        ObjectMapper mapper = converter.getObjectMapper();
        mapper.registerModule(new JsonNullableModule());
        mapper.enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION);
        mapper.enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        return converter;
    }

    @Bean
    @Profile("!test")
    public ServletRegistrationBean<DispatcherServlet> dispatcherRegistration(DispatcherServlet dispatcherServlet) {
        ServletRegistrationBean<DispatcherServlet> registration = new ServletRegistrationBean<>(dispatcherServlet, "/rest/v2/*");
        registration.setName("rest-v2");
        return registration;
    }

}
