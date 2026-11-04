/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2.mailing.links;

import static org.easymock.EasyMock.expect;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.agnitas.emm.core.Permission;
import com.agnitas.emm.rest.v2.infrastructure.config.json.RestEnumAllowedValues;
import com.agnitas.emm.rest.v2.infrastructure.search.PageResponse;
import com.agnitas.emm.rest.v2.infrastructure.search.PageResponse.PageMeta;
import com.agnitas.testsupport.annotation.MockBean;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.data.domain.PageRequest;

class _MailingLinksV2ControllerTest extends _MailingLinksV2ControllerTestSupport {

    private static final LinkResponse LINK_RESPONSE = new LinkResponse(
            123,
            "https://example.com",
            "Example link",
            LinkTracking.TEXT_AND_HTML,
            LinkRetargeting.SET_COOKIE,
            10,
            true,
            true,
            List.of(new LinkExtensionResponse("name", "value"))
    );

    private static final String RESPONSE_JSON =
            """
            {
                "id": 123,
                "url": "https://example.com",
                "description": "Example link",
                "tracking": "TEXT_AND_HTML",
                "retargeting": "SET_COOKIE",
                "triggerId": 10,
                "static": true,
                "administrative": true,
                "extensions": [
                    {
                        "name": "name",
                        "value": "value"
                    }
                ]
            }
            """;

    @MockBean
    private MailingLinkRestService restServiceMock;

    @Nested
    class PaginatedList {
        @Test
        void shouldReturn200() throws Exception {
            expect(adminMock.allowedAll(Permission.MAILING_CONTENT_SHOW)).andReturn(true);
            expect(restServiceMock.list(4, PageRequest.of(3, 2), adminMock)).andReturn(new PageResponse<>(
                    List.of(
                            LINK_RESPONSE,
                            new LinkResponse(
                                    124,
                                    "https://example.com/other",
                                    "Other link",
                                    LinkTracking.NOT_TRACKABLE,
                                    LinkRetargeting.WITHOUT,
                                    0,
                                    false,
                                    false,
                                    List.of()
                            )
                    ),
                    new PageMeta(3, 2, 50)
            ));
            Map<String, Object> params = Map.of(
                    "page", "3",
                    "size", "2"
            );
            authenticated(params(get("/mailings/{mailingId}/links", 4), params)).andExpectAll(
                    status().isOk(),
                    content().json(
                            """
                            {
                                "page": {
                                    "number": 3,
                                    "size": 2,
                                    "totalCount": 50
                                },
                                "list": [
                                    {
                                        "id": 123,
                                        "url": "https://example.com",
                                        "description": "Example link",
                                        "tracking": "TEXT_AND_HTML",
                                        "retargeting": "SET_COOKIE",
                                        "triggerId": 10,
                                        "static": true,
                                        "administrative": true,
                                        "extensions": [
                                            {
                                                "name": "name",
                                                "value": "value"
                                            }
                                        ]
                                    },
                                    {
                                        "id": 124,
                                        "url": "https://example.com/other",
                                        "description": "Other link",
                                        "tracking": "NOT_TRACKABLE",
                                        "retargeting": "WITHOUT",
                                        "static": false,
                                        "administrative": false,
                                        "extensions": []
                                    }
                                ]
                            }
                            """
                    )
            ).andDo(documentList());
        }

        @Test
        void shouldReturn400_whenMailingIdIsZero() throws Exception {
            authenticated(get("/mailings/{mailingId}/links", 0)).andExpectAll(
                    status().isBadRequest(),
                    content().json("""
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

        @Test
        void shouldReturn403() throws Exception {
            expect(adminMock.allowedAll(Permission.MAILING_CONTENT_SHOW)).andReturn(false);
            assertNotAuthorized(get("/mailings/{mailingId}/links", 1));
        }
    }

    @Nested
    class FindSingle {
        @Test
        void shouldReturn200() throws Exception {
            expect(adminMock.allowedAll(Permission.MAILING_CONTENT_SHOW)).andReturn(true);
            expect(restServiceMock.find(123, 4, adminMock)).andReturn(LINK_RESPONSE);
            authenticated(get("/mailings/{mailingId}/links/{linkId}", 4, 123)).andExpectAll(
                    status().isOk(),
                    content().json(RESPONSE_JSON)
            ).andDo(documentFind());
        }

        @Test
        void shouldReturn400_whenMailingIdZero() throws Exception {
            authenticated(get("/mailings/{mailingId}/links/{linkId}", 0, 4)).andExpectAll(
                    status().isBadRequest(),
                    content().json("""
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

        @Test
        void shouldReturn400_whenLinkIdZero() throws Exception {
            authenticated(get("/mailings/{mailingId}/links/{linkId}", 4, 0)).andExpectAll(
                    status().isBadRequest(),
                    content().json("""
                                   {
                                       "status": 400,
                                       "detail": "Invalid input.",
                                       "errors": [
                                           {
                                               "field": "linkId",
                                               "message": "must be greater than 0"
                                           }
                                       ]
                                   }
                                   """
                    )
            );
        }

        @Test
        void shouldReturn403() throws Exception {
            expect(adminMock.allowedAll(Permission.MAILING_CONTENT_SHOW)).andReturn(false);
            assertNotAuthorized(get("/mailings/{mailingId}/links/{linkId}", 123, 4));
        }
    }

    @Nested
    class Patch {
        @Test
        void shouldReturn200() throws Exception {
            var updateDto = new PatchLinkRequest(
                    JsonNullable.of("https://example.com"),
                    JsonNullable.of("Example link"),
                    JsonNullable.of(LinkTracking.TEXT_AND_HTML),
                    JsonNullable.of(LinkRetargeting.SET_COOKIE),
                    JsonNullable.of(10),
                    JsonNullable.of(true),
                    JsonNullable.of(true),
                    JsonNullable.of(List.of(new CreateLinkExtensionRequest("name", "value")))
            );
            expect(adminMock.allowedAll(Permission.MAILING_CONTENT_SHOW)).andReturn(true);
            expect(restServiceMock.patch(4, 123, updateDto, adminMock)).andReturn(LINK_RESPONSE);

            authenticated(patch("/mailings/{mailingId}/links/{linkId}", 123, 4).content(
                    """
                    {
                        "url": "https://example.com",
                        "description": "Example link",
                        "tracking": "TEXT_AND_HTML",
                        "retargeting": "SET_COOKIE",
                        "triggerId": 10,
                        "static": true,
                        "administrative": true,
                        "extensions": [
                            {
                                "name": "name",
                                "value": "value"
                            }
                        ]
                    }
                    """
            ))
                    .andExpectAll(status().isOk(), content().json(RESPONSE_JSON))
                    .andDo(documentPatch());
        }

        public static Stream<Arguments> provideInvalidBodyForPatch() {
            return Stream.of(
                    Arguments.of(
                            """
                            { "url": null }
                            """,
                            "url",
                            "must not be blank"
                    ),
                    Arguments.of(
                            """
                            { "url": "" }
                            """,
                            "url",
                            "must not be blank"
                    ),
                    Arguments.of(
                            """
                            { "url": "   " }
                            """,
                            "url",
                            "must not be blank"
                    ),
                    Arguments.of(
                            """
                            { "retargeting": "invalid" }
                            """,
                            "retargeting",
                            RestEnumAllowedValues.LINK_RETARGETINGS
                    ),
                    Arguments.of(
                            """
                            { "tracking": "invalid" }
                            """,
                            "tracking",
                            RestEnumAllowedValues.LINK_TRACKINGS
                    ),
                    Arguments.of(
                            """
                            {
                                "extensions": [{ "name": "", "value": "1234" }]
                            }
                            """,
                            "extensions[0].name",
                            "must not be blank"
                    )
            );
        }

        @ParameterizedTest
        @MethodSource("provideInvalidBodyForPatch")
        void shouldReturn400(String body, String field, String expectedError) throws Exception {
            authenticated(patch("/mailings/{mailingId}/links/{linkId}", 123, 4).content(body)).andExpectAll(
                    status().isBadRequest(),
                    content().json(
                            """
                            {
                                "status": 400,
                                "detail": "Invalid input.",
                                "errors": [
                                    {
                                        "field": "%s",
                                        "message": "%s"
                                    }
                                ]
                            }
                            """.formatted(field, expectedError)
                    )
            );
        }

        @Test
        void shouldReturn400_whenMailingIdZero() throws Exception {
            authenticated(patch("/mailings/{mailingId}/links/{linkId}", 0, 4).content(
                    """
                    { "url": "https://example.com" }
                    """
            )).andExpectAll(
                    status().isBadRequest(),
                    content().json("""
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

        @Test
        void shouldReturn400_whenLinkIdZero() throws Exception {
            authenticated(patch("/mailings/{mailingId}/links/{linkId}", 4, 0).content(
                    """
                    { "url": "https://example.com" }
                    """
            )).andExpectAll(
                    status().isBadRequest(),
                    content().json("""
                                   {
                                       "status": 400,
                                       "detail": "Invalid input.",
                                       "errors": [
                                           {
                                               "field": "linkId",
                                               "message": "must be greater than 0"
                                           }
                                       ]
                                   }
                                   """
                    )
            );
        }

        @Test
        void shouldReturn403() throws Exception {
            expect(adminMock.allowedAll(Permission.MAILING_CONTENT_SHOW)).andReturn(false);
            assertNotAuthorized(patch("/mailings/{mailingId}/links/{linkId}", 4, 123).content("{}"));
        }
    }
}
