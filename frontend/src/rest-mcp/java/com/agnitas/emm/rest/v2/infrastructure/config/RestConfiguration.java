/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2.infrastructure.config;

import java.util.List;

import com.agnitas.beans.BindingEntry.UserType;
import com.agnitas.beans.MailingContentType;
import com.agnitas.emm.common.MailingStatus;
import com.agnitas.emm.common.MailingType;
import com.agnitas.emm.common.UserStatus;
import com.agnitas.emm.core.action.bean.EmmActionType;
import com.agnitas.emm.core.components.form.AttachmentType;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.emm.rest.v2.infrastructure.bulk.ImportMode;
import com.agnitas.emm.rest.v2.infrastructure.config.json.EnumConverterFactory;
import com.agnitas.emm.rest.v2.infrastructure.config.json.EnumDeserializer;
import com.agnitas.emm.rest.v2.infrastructure.config.json.MessageSerializer;
import com.agnitas.emm.rest.v2.infrastructure.ratelimit.RateLimitInterceptor;
import com.agnitas.emm.rest.v2.infrastructure.security.xss.RestXssInterceptor;
import com.agnitas.emm.rest.v2.mailing.dto.FollowupType;
import com.agnitas.emm.rest.v2.mailing.dto.MailFormat;
import com.agnitas.emm.rest.v2.mailing.dto.OnePixelPosition;
import com.agnitas.emm.rest.v2.mailing.dto.RestCharset;
import com.agnitas.emm.rest.v2.mailing.dto.RestMediatype;
import com.agnitas.emm.rest.v2.mailing.links.LinkRetargeting;
import com.agnitas.emm.rest.v2.mailing.links.LinkTracking;
import com.agnitas.emm.rest.v2.table.RefTableColumnType;
import com.agnitas.messages.Message;
import com.agnitas.util.importvalues.Gender;
import org.jspecify.annotations.NonNull;
import org.openapitools.jackson.nullable.JsonNullableJackson3Module;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.converter.HttpMessageConverters.ServerBuilder;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.stereotype.Controller;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerTypePredicate;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;
import tools.jackson.core.StreamReadFeature;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;

@Configuration
@ComponentScan(
    basePackages = "com.agnitas.emm.rest.v2",
    useDefaultFilters = false,
    includeFilters = {
        @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = Controller.class),
        @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = RestController.class),
        @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = ControllerAdvice.class),
        @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = Configuration.class)
    },
    lazyInit = true
)
@EnableSpringDataWebSupport
public class RestConfiguration implements WebMvcConfigurer {

    private static final String API_BASE = "/rest/v2";

    private static final String[] PUBLIC_PATHS = {
            API_BASE + "/openapi/**",
            API_BASE + "/ui/**"
    };

    private final RestUserArgumentResolver userArgumentResolver;
    private final RestXssInterceptor xssInterceptor;
    private final UserActionLoggingInterceptor userActionLoggingInterceptor;
    private final RateLimitInterceptor rateLimitInterceptor;
    private final EnumConverterFactory enumConverterFactory;

    public RestConfiguration(
            RestUserArgumentResolver userArgumentResolver,
            RestXssInterceptor xssInterceptor,
            UserActionLoggingInterceptor userActionLoggingInterceptor,
            RateLimitInterceptor rateLimitInterceptor,
            EnumConverterFactory enumConverterFactory
    ) {
        this.userArgumentResolver = userArgumentResolver;
        this.xssInterceptor = xssInterceptor;
        this.userActionLoggingInterceptor = userActionLoggingInterceptor;
        this.rateLimitInterceptor = rateLimitInterceptor;
        this.enumConverterFactory = enumConverterFactory;
    }

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.addPathPrefix(
                API_BASE,
                HandlerTypePredicate.forBasePackage("com.agnitas.emm.rest.v2")
        );
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(API_BASE + "/ui/**")
                .addResourceLocations("classpath:/static/swagger-ui/")
                .resourceChain(true)
                .addResolver(new PathResourceResolver());
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitInterceptor).addPathPatterns(API_BASE + "/**").excludePathPatterns(PUBLIC_PATHS);
        registry.addInterceptor(xssInterceptor).addPathPatterns(API_BASE + "/**").excludePathPatterns(PUBLIC_PATHS);
        registry.addInterceptor(userActionLoggingInterceptor).addPathPatterns(API_BASE + "/**").excludePathPatterns(PUBLIC_PATHS);
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverterFactory(enumConverterFactory);
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        PageableHandlerMethodArgumentResolver pageableResolver = new PageableHandlerMethodArgumentResolver();
        pageableResolver.setMaxPageSize(200);
        pageableResolver.setFallbackPageable(PageRequest.of(1, 20));
        resolvers.add(pageableResolver);
        resolvers.add(userArgumentResolver);
    }

    @Override
    public void configureMessageConverters(@NonNull ServerBuilder builder) {
        WebMvcConfigurer.super.configureMessageConverters(builder);
        SimpleModule enumDeserializers = new SimpleModule()
                .addDeserializer(MediaTypes.class, new EnumDeserializer<>(MediaTypes.class))
                .addDeserializer(UserStatus.class, new EnumDeserializer<>(UserStatus.class))
                .addDeserializer(UserType.class, new EnumDeserializer<>(UserType.class))
                .addDeserializer(MailingContentType.class, new EnumDeserializer<>(MailingContentType.class))
                .addDeserializer(MailingType.class, new EnumDeserializer<>(MailingType.class))
                .addDeserializer(EmmActionType.class, new EnumDeserializer<>(EmmActionType.class))
                .addDeserializer(ImportMode.class, new EnumDeserializer<>(ImportMode.class))
                .addDeserializer(RefTableColumnType.class, new EnumDeserializer<>(RefTableColumnType.class))
                .addDeserializer(MailingStatus.class, new EnumDeserializer<>(MailingStatus.class))
                .addDeserializer(RestCharset.class, new EnumDeserializer<>(RestCharset.class))
                .addDeserializer(OnePixelPosition.class, new EnumDeserializer<>(OnePixelPosition.class))
                .addDeserializer(MailFormat.class, new EnumDeserializer<>(MailFormat.class))
                .addDeserializer(FollowupType.class, new EnumDeserializer<>(FollowupType.class))
                .addDeserializer(RestMediatype.class, new EnumDeserializer<>(RestMediatype.class))
                .addDeserializer(LinkTracking.class, new EnumDeserializer<>(LinkTracking.class))
                .addDeserializer(AttachmentType.class, new EnumDeserializer<>(AttachmentType.class))
                .addDeserializer(LinkRetargeting.class, new EnumDeserializer<>(LinkRetargeting.class))
                .addDeserializer(Gender.class, new EnumDeserializer<>(Gender.class));
        builder.withJsonConverter(new JacksonJsonHttpMessageConverter(JsonMapper.builder()
                .addModule(new JsonNullableJackson3Module())
                .addModule(enumDeserializers)
                .addModule(new SimpleModule("MessageModule").addSerializer(Message.class, new MessageSerializer()))
                .enable(StreamReadFeature.STRICT_DUPLICATE_DETECTION)
                .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .build()));
    }

    @Bean
    public LocalValidatorFactoryBean validator() {
        LocalValidatorFactoryBean factory = new LocalValidatorFactoryBean();
        factory.setProviderClass(org.hibernate.validator.HibernateValidator.class);
        return factory;
    }

    @Override
    public org.springframework.validation.Validator getValidator() {
        return validator();
    }
}
