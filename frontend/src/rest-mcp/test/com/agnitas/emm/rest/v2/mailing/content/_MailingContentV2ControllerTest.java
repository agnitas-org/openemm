/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2.mailing.content;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.stream.Stream;

import com.agnitas.emm.core.Permission;
import com.agnitas.testsupport.annotation.MockBean;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

public class _MailingContentV2ControllerTest extends _MailingContentV2ControllerTestSupport {

    private static final int MAILING_ID = 123;
    private static final int CONTENT_ID = 234;
    private static final String CONTENT_NAME = "Dyn name";
    private static final String BASE_URL = "/mailings/{mailingId}/contents";
    private static final String BY_ID_URL = "%s/{contentId}".formatted(BASE_URL);
    private static final String BY_NAME_URL = "%s/by-name/{contentName}".formatted(BASE_URL);

    public static final MailingContentResponse CONTENT_RESPONSE = new MailingContentResponse(
            CONTENT_ID, CONTENT_NAME, true,
            List.of(new TargetContentResponse(
                    1234, 3, 1, "text content"
            ))
    );

    @MockBean
    private MailingContentRestService contentService;

    @Nested
    class ListContents {
        @Test
        void shouldReturn200() throws Exception {
            expect(adminMock.allowedAll(Permission.MAILING_CONTENT_SHOW)).andReturn(true);
            expect(contentService.list(MAILING_ID, COMPANY_ID)).andReturn(List.of(
                    new MailingContentResponse(
                            CONTENT_ID, "Dyn name 1", true,
                            List.of(new TargetContentResponse(1234, 0, 1, "text content"))
                    ),
                    new MailingContentResponse(
                            CONTENT_ID + 1, "Dyn name 2", true,
                            List.of(
                                    new TargetContentResponse(1235, 2, 1, "text content for target 2"),
                                    new TargetContentResponse(1234, 0, 2, "text content for all")
                            )
                    )
            ));

            authenticated(get(BASE_URL, MAILING_ID)).andExpectAll(
                    status().isOk(),
                    content().json(
                            """
                            [
                                {
                                  "id": 234,
                                  "name": "Dyn name 1",
                                  "content": [
                                    {
                                      "id": 1234,
                                      "order": 1,
                                      "text": "text content",
                                    }
                                  ]
                                },
                                {
                                  "id": 235,
                                  "name": "Dyn name 2",
                                  "disableLinkExtension": true,
                                  "content": [
                                    {
                                      "id": 1235,
                                      "targetId": 2,
                                      "order": 1,
                                      "text": "text content for target 2",
                                    },
                                    {
                                      "id": 1234,
                                      "order": 2,
                                      "text": "text content for all",
                                    }
                                  ]
                                }
                            ]
                            """
                    )
            ).andDo(documentList());
        }

        @Test
        void shouldReturn403() throws Exception {
            expect(adminMock.allowedAll(Permission.MAILING_CONTENT_SHOW)).andReturn(false);
            assertNotAuthorized(get(BASE_URL, MAILING_ID));
        }
    }

    @Nested
    class ReadById {
        @Test
        void shouldReturn200() throws Exception {
            expect(adminMock.allowedAll(Permission.MAILING_CONTENT_SHOW)).andReturn(true);
            expect(contentService.getById(CONTENT_ID, MAILING_ID, COMPANY_ID)).andReturn(CONTENT_RESPONSE);

            authenticated(get(BY_ID_URL, MAILING_ID, CONTENT_ID)).andExpectAll(
                    status().isOk(),
                    content().json(
                            """
                            {
                              "id": 234,
                              "name": "Dyn name",
                              "disableLinkExtension": true,
                              "content": [
                                {
                                  "id": 1234,
                                  "targetId": 3,
                                  "order": 1,
                                  "text": "text content",
                                }
                              ]
                            }
                            """
                    )
            ).andDo(documentReadById());
        }

        @Test
        void shouldReturn403() throws Exception {
            expect(adminMock.allowedAll(Permission.MAILING_CONTENT_SHOW)).andReturn(false);
            assertNotAuthorized(get(BY_ID_URL, MAILING_ID, CONTENT_ID));
        }
    }

    @Nested
    class ReadByName {
        @Test
        void shouldReturn200() throws Exception {
            expect(adminMock.allowedAll(Permission.MAILING_CONTENT_SHOW)).andReturn(true);
            expect(contentService.getByName(CONTENT_NAME, MAILING_ID, COMPANY_ID)).andReturn(CONTENT_RESPONSE);

            authenticated(get(BY_NAME_URL, MAILING_ID, CONTENT_NAME)).andExpectAll(
                    status().isOk(),
                    content().json(
                            """
                            {
                              "id": 234,
                              "name": "Dyn name",
                              "disableLinkExtension": true,
                              "content": [
                                {
                                  "id": 1234,
                                  "targetId": 3,
                                  "order": 1,
                                  "text": "text content",
                                }
                              ]
                            }
                            """
                    )
            ).andDo(documentReadByName());
        }

        @Test
        void shouldReturn403() throws Exception {
            expect(adminMock.allowedAll(Permission.MAILING_CONTENT_SHOW)).andReturn(false);
            assertNotAuthorized(get(BY_NAME_URL, MAILING_ID, CONTENT_NAME));
        }
    }

    @Nested
    class PatchById {
        @Test
        void shouldReturn200() throws Exception {
            expect(adminMock.allowedAll(Permission.MAILING_CHANGE)).andReturn(true);
            var request = new PatchMailingContentRequest(
                    JsonNullable.of(true),
                    JsonNullable.of(List.of(new TargetContentUpsertRequest(3, 1, "text content")))
            );
            expect(contentService.patch(CONTENT_ID, MAILING_ID, request, adminMock)).andReturn(CONTENT_RESPONSE);

            authenticated(patch(BY_ID_URL, MAILING_ID, CONTENT_ID).content(
                    """
                    {
                      "disableLinkExtension": true,
                      "content": [
                        {
                          "targetId": 3,
                          "order": 1,
                          "text": "text content"
                        }
                      ]
                    }
                    """
            )).andExpectAll(
                    status().isOk(),
                    content().json(
                            """
                            {
                              "id": 234,
                              "name": "Dyn name",
                              "disableLinkExtension": true,
                              "content": [
                                {
                                  "id": 1234,
                                  "targetId": 3,
                                  "order": 1,
                                  "text": "text content"
                                }
                              ]
                            }
                            """
                    )
            ).andDo(documentPatchById());
        }

        @Test
        void shouldReturn403() throws Exception {
            expect(adminMock.allowedAll(Permission.MAILING_CHANGE)).andReturn(false);
            assertNotAuthorized(patch(BY_ID_URL, MAILING_ID, CONTENT_ID).content("{}"));
        }
    }

    @Nested
    class PatchByName {
        @Test
        void shouldReturn200() throws Exception {
            expect(adminMock.allowedAll(Permission.MAILING_CHANGE)).andReturn(true);
            var request = new PatchMailingContentRequest(
                    JsonNullable.of(true),
                    JsonNullable.of(List.of(new TargetContentUpsertRequest(3, 1, "text content")))
            );
            expect(contentService.patch(CONTENT_NAME, MAILING_ID, request, adminMock)).andReturn(CONTENT_RESPONSE);

            authenticated(patch(BY_NAME_URL, MAILING_ID, CONTENT_NAME).content(
                    """
                    {
                      "disableLinkExtension": true,
                      "content": [
                        {
                          "targetId": 3,
                          "order": 1,
                          "text": "text content"
                        }
                      ]
                    }
                    """
            )).andExpectAll(
                    status().isOk(),
                    content().json(
                            """
                            {
                              "id": 234,
                              "name": "Dyn name",
                              "disableLinkExtension": true,
                              "content": [
                                {
                                  "id": 1234,
                                  "targetId": 3,
                                  "order": 1,
                                  "text": "text content"
                                }
                              ]
                            }
                            """
                    )
            ).andDo(documentPatchByName());
        }

        @Test
        void shouldReturn403() throws Exception {
            expect(adminMock.allowedAll(Permission.MAILING_CHANGE)).andReturn(false);
            assertNotAuthorized(patch(BY_NAME_URL, MAILING_ID, CONTENT_NAME).content("{}"));
        }
    }

    @Nested
    class Delete {
        @Test
        void shouldReturn204_onDeleteById() throws Exception {
            expect(adminMock.allowedAll(Permission.MAILING_CHANGE)).andReturn(true);
            contentService.delete(CONTENT_ID, MAILING_ID, COMPANY_ID);
            expectLastCall();

            authenticated(delete(BY_ID_URL, MAILING_ID, CONTENT_ID))
                    .andExpect(status().isNoContent())
                    .andDo(documentDeleteById());
        }

        @Test
        void shouldReturn204_onDeleteByName() throws Exception {
            expect(adminMock.allowedAll(Permission.MAILING_CHANGE)).andReturn(true);
            contentService.delete(CONTENT_NAME, MAILING_ID, COMPANY_ID);
            expectLastCall();

            authenticated(delete(BY_NAME_URL, MAILING_ID, CONTENT_NAME))
                    .andExpect(status().isNoContent())
                    .andDo(documentDeleteByName());
        }

        @Test
        void shouldReturn403() throws Exception {
            expect(adminMock.allowedAll(Permission.MAILING_CHANGE)).andReturn(false);
            assertNotAuthorized(delete(BY_ID_URL, MAILING_ID, CONTENT_ID));
        }
    }

    @Nested
    class InvalidInput {
        public static Stream<Arguments> provideAllEndpointsWithInvalidMailingId() {
            return Stream.of(
                    get(BASE_URL, 0),
                    get(BY_ID_URL, 0, CONTENT_ID),
                    get(BY_NAME_URL, 0, CONTENT_NAME),
                    delete(BY_ID_URL, 0, CONTENT_ID),
                    delete(BY_NAME_URL, 0, CONTENT_NAME),
                    patch(BY_ID_URL, 0, CONTENT_ID).content("{}")
            ).map(Arguments::of);
        }

        @ParameterizedTest
        @MethodSource("provideAllEndpointsWithInvalidMailingId")
        void shouldReturn400_whenMailingIdInvalid(MockHttpServletRequestBuilder request) throws Exception {
            expect(adminMock.allowedAll(Permission.MAILING_CHANGE)).andStubReturn(true);
            expect(adminMock.allowedAll(Permission.MAILING_CONTENT_SHOW)).andStubReturn(true);

            authenticated(request).andExpectAll(
                    status().isBadRequest(),
                    content().json(
                            """
                            {
                                "status": 400,
                                "detail": "Invalid input.",
                                "errors": [
                                    {
                                        "field": "mailingId",
                                        "message": "must be greater than 0"
                                    }
                                ]
                            }
                            """
                    )
            );
        }

        public static Stream<Arguments> provideAllEndpointsWithInvalidContentId() {
            return Stream.of(
                    get(BY_ID_URL, MAILING_ID, 0),
                    delete(BY_ID_URL, MAILING_ID, 0),
                    patch(BY_ID_URL, MAILING_ID, 0).content("{}")
            ).map(Arguments::of);
        }

        @ParameterizedTest
        @MethodSource("provideAllEndpointsWithInvalidContentId")
        void shouldReturn400_whenContentIdInvalid(MockHttpServletRequestBuilder request) throws Exception {
            expect(adminMock.allowedAll(Permission.MAILING_CHANGE)).andStubReturn(true);
            expect(adminMock.allowedAll(Permission.MAILING_CONTENT_SHOW)).andStubReturn(true);

            authenticated(request).andExpectAll(
                    status().isBadRequest(),
                    content().json(
                            """
                            {
                                "status": 400,
                                "detail": "Invalid input.",
                                "errors": [
                                    {
                                        "field": "contentId",
                                        "message": "must be greater than 0"
                                    }
                                ]
                            }
                            """
                    )
            );
        }

        public static Stream<Arguments> provideAllEndpointsWithInvalidContentName() {
            return Stream.of(
                    get(BY_NAME_URL, MAILING_ID, " "),
                    delete(BY_NAME_URL, MAILING_ID, " ")
            ).map(Arguments::of);
        }

        @ParameterizedTest
        @MethodSource("provideAllEndpointsWithInvalidContentName")
        void shouldReturn400_whenContentNameInvalid(MockHttpServletRequestBuilder request) throws Exception {
            expect(adminMock.allowedAll(Permission.MAILING_CHANGE)).andStubReturn(true);
            expect(adminMock.allowedAll(Permission.MAILING_CONTENT_SHOW)).andStubReturn(true);

            authenticated(request).andExpectAll(
                    status().isBadRequest(),
                    content().json(
                            """
                            {
                                "status": 400,
                                "detail": "Invalid input.",
                                "errors": [
                                    {
                                        "field": "contentName",
                                        "message": "must not be blank"
                                    }
                                ]
                            }
                            """
                    )
            );
        }

        @Test
        void shouldReturn400_whenTargetContentTargetIdInvalid() throws Exception {
            authenticated(patch(BY_ID_URL, MAILING_ID, CONTENT_ID).content(
                    """
                    {
                      "content": [
                        {
                          "targetId": -1,
                          "order": 1,
                          "text": "text content"
                        }
                      ]
                    }
                    """
            )).andExpectAll(
                    status().isBadRequest(),
                    content().json(
                            """
                            {
                                "status": 400,
                                "detail": "Invalid input.",
                                "errors": [
                                    {
                                        "field": "content[0].targetId",
                                        "message": "must be greater than or equal to 0"
                                    }
                                ]
                            }
                            """
                    )
            );
        }

        @Test
        void shouldReturn400_whenTargetContentOrderInvalid() throws Exception {
            authenticated(patch(BY_ID_URL, MAILING_ID, CONTENT_ID).content(
                    """
                    {
                      "content": [
                        {
                          "targetId": 0,
                          "order": 0,
                          "text": "text content"
                        }
                      ]
                    }
                    """
            )).andExpectAll(
                    status().isBadRequest(),
                    content().json(
                            """
                            {
                                "status": 400,
                                "detail": "Invalid input.",
                                "errors": [
                                    {
                                        "field": "content[0].order",
                                        "message": "must be greater than 0"
                                    }
                                ]
                            }
                            """
                    )
            );
        }
    }
}
