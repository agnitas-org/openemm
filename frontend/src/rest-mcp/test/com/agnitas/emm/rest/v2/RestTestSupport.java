/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2;

import static org.easymock.EasyMock.expect;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.stream.StreamSupport;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.rest.v2.infrastructure.config.RestConfiguration;
import com.agnitas.emm.rest.v2.infrastructure.security.filter.CachedBodyFilter;
import com.agnitas.testsupport.EmmEasyMockSupport;
import com.agnitas.testsupport.extension.EmmControllerExtension;
import com.agnitas.testsupport.initializer.MockBeansInitializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.AbstractMockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@WebAppConfiguration
@ContextConfiguration(classes = {
        RestConfiguration.class,
        RestTestConfig.class,
}, initializers = MockBeansInitializer.class)
@ExtendWith(EmmControllerExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public abstract class RestTestSupport extends EmmEasyMockSupport {

    protected static final int COMPANY_ID = 2;

    @RegisterExtension
    final RestDocumentationExtension restDocs = new RestDocumentationExtension(System.getProperty(
            "restdocs.outputDir",
            "docs/rest/v2/generated-snippets"
    ));

    @Autowired
    protected WebApplicationContext appContext;

    protected final Admin adminMock = createMock(Admin.class);
    private MockMvc mockMvc;

    @BeforeEach
    void setUp(RestDocumentationContextProvider restDocumentation) {
        expect(adminMock.getCompanyID()).andStubReturn(COMPANY_ID);
        expect(adminMock.getAdminID()).andStubReturn(3);
        expect(adminMock.getLocale()).andStubReturn(Locale.ENGLISH);
        expect(adminMock.permissionAllowed(Permission.REST_API_SPRING)).andStubReturn(true);
        mockMvc = getMockMvc(restDocumentation);
    }

    protected ResultActions authenticated(AbstractMockHttpServletRequestBuilder<?> requestBuilder) throws Exception {
        return authenticated(requestBuilder, MediaType.APPLICATION_JSON);
    }

    protected ResultActions authenticated(
            AbstractMockHttpServletRequestBuilder<?> requestBuilder,
            MediaType mediaType
    ) throws Exception {
        replayAll();
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(adminMock, null, Collections.emptyList()));
        ResultActions perform = mockMvc.perform(requestBuilder
                .header("Authorization", "Basic auth")
                .contentType(mediaType));
        SecurityContextHolder.clearContext();
        return perform;
    }

    protected MockMvc getMockMvc(RestDocumentationContextProvider restDocumentation) {
        return MockMvcBuilders
                .webAppContextSetup(appContext)
                .defaultRequest(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/")
                        .with(request -> {
                            String currentUri = request.getRequestURI();
                            if (currentUri != null && !currentUri.startsWith("/rest/v2")) {
                                request.setRequestURI("/rest/v2" + currentUri);
                            }
                            return request;
                        })
                )
                .addFilter(new CachedBodyFilter())
                .apply(documentationConfiguration(restDocumentation))
                .build();
    }

    public void assertNotAuthorized(AbstractMockHttpServletRequestBuilder<?> request) throws Exception {
        assertNotAuthorized(request, MediaType.APPLICATION_JSON);
    }
    public void assertNotAuthorized(AbstractMockHttpServletRequestBuilder<?> request, MediaType mediaType) throws Exception {
        authenticated(request, mediaType).andExpectAll(
                status().isForbidden(),
                content().json("""
                        {
                            "status": 403,
                            "detail": "Forbidden"
                        }
                        """
                )
        );
    }

    public static <T extends AbstractMockHttpServletRequestBuilder<T>> T params(T builder, Map<String, ?> params) {
        params.forEach((name, value) -> builder.param(name, toStrings(value)));
        return builder;
    }

    private static String[] toStrings(Object value) {
        return switch (value) {
            case null -> new String[] {""};
            case Object[] array -> Arrays.stream(array).map(RestTestSupport::str).toArray(String[]::new);
            case Iterable<?> iterable -> StreamSupport.stream(iterable.spliterator(), false)
                    .map(RestTestSupport::str).toArray(String[]::new);
            default -> new String[] {value.toString()};
        };
    }

    private static String str(Object o) {
        return o == null ? "" : o.toString();
    }
}
