/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.spring.web.config;

import java.util.List;

import com.agnitas.emm.core.admin.service.AdminService;
import com.agnitas.emm.core.logon.web.resolver.LogonStateBundleArgumentResolver;
import com.agnitas.emm.core.recipient.imports.wizard.web.RecipientImportWizardStepsInterceptor;
import com.agnitas.emm.core.workflow.web.WorkflowParametersArgumentResolver;
import com.agnitas.emm.core.workflow.web.WorkflowParamsRedirectionInterceptor;
import com.agnitas.service.PollingService;
import com.agnitas.spring.web.view.tiles3.ApacheTilesView;
import com.agnitas.spring.web.view.tiles3.TilesConfigurer;
import com.agnitas.web.mvc.AdminPreferencesArgumentResolver;
import com.agnitas.web.mvc.PollableMethodReturnValueHandler;
import com.agnitas.web.mvc.PopupsArgumentResolver;
import com.agnitas.web.perm.AdminArgumentResolver;
import com.agnitas.web.perm.AuthorizationInterceptor;
import com.agnitas.web.perm.XssInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.core.Ordered;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.view.UrlBasedViewResolver;

@Configuration
@EnableWebMvc
@ComponentScan(
        basePackages = {"com.agnitas"},
        useDefaultFilters = false,
        includeFilters = {
                @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = Controller.class),
                @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = ControllerAdvice.class)
        },
        lazyInit = true
)
public class WebConfiguration implements WebMvcConfigurer {

    private final AdminService adminService;
    private final PollingService pollingService;

    @Autowired
    public WebConfiguration(AdminService adminService, PollingService pollingService) {
        this.adminService = adminService;
        this.pollingService = pollingService;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuthorizationInterceptor(adminService))
                .addPathPatterns("/**")
                .excludePathPatterns("/assets/**", "/form.action", "/form.do");

        registry.addInterceptor(new XssInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns("/assets/**", "/form.action", "/form.do");

        // Workflow params redirection
        registry.addInterceptor(new WorkflowParamsRedirectionInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns("/assets/**");

        // Recipient import wizard steps
        registry.addInterceptor(new RecipientImportWizardStepsInterceptor())
                .addPathPatterns("/recipient/import/wizard/step/*")
                .excludePathPatterns("/assets/**");
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new AdminArgumentResolver());
        resolvers.add(new PopupsArgumentResolver());
        resolvers.add(new AdminPreferencesArgumentResolver());
        resolvers.add(new WorkflowParametersArgumentResolver());
        resolvers.add(new LogonStateBundleArgumentResolver());
    }

    @Override
    public void addReturnValueHandlers(List<HandlerMethodReturnValueHandler> handlers) {
        handlers.add(new PollableMethodReturnValueHandler(pollingService));
    }

    @Bean
    public ViewResolver viewResolver() {
        UrlBasedViewResolver viewResolver = new UrlBasedViewResolver();
        viewResolver.setViewClass(ApacheTilesView.class);
        viewResolver.setOrder(Ordered.HIGHEST_PRECEDENCE);

        return viewResolver;
    }

    @Bean
    public TilesConfigurer tilesConfigurer() {
        TilesConfigurer tilesConfigurer = new TilesConfigurer();
        tilesConfigurer.setDefinitions("/WEB-INF/tiles-defs.xml");

        return tilesConfigurer;
    }

    @Bean
    public MappingJackson2HttpMessageConverter jsonMessageConverter() {
        return new MappingJackson2HttpMessageConverter();
    }

}
