/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2.target;

import static java.time.ZoneOffset.UTC;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.target.dto.TargetInclude;
import com.agnitas.emm.rest.v2.infrastructure.search.PageResponse;
import com.agnitas.testsupport.annotation.MockBean;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.data.domain.PageRequest;

class _TargetsV2ControllerTest extends _TargetsV2ControllerTestSupport {

    private static final TargetResponse TARGET_RESPONSE = new TargetResponse(
            123,
            "country de",
            "de recipients",
            "country = de",
            "country = de",
            true,
            ZonedDateTime.of(2021, 10, 18, 19, 34, 30, 0, UTC).toInstant(),
            ZonedDateTime.of(2026, 2, 3, 2, 0, 30, 0, UTC).toInstant(),
            false,
            null
    );
    private static final String RESPONSE_JSON =
            """
            {
                "id": 123,
                "name": "country de",
                "description": "de recipients",
                "sql": "country = de",
                "eql": "country = de",
                "valid": true
            }
            """;

    @SuppressWarnings("unused")
    @MockBean
    private TargetRestService targetRestServiceMock;

    @Nested
    class PaginatedList {
        @Test
        void shouldReturn200() throws Exception {
            expect(adminMock.allowedAll(Permission.TARGETS_SHOW)).andReturn(true);
            expect(targetRestServiceMock.find(PageRequest.of(3, 2), Set.of(TargetInclude.RECIPIENT_COUNT), adminMock))
                    .andReturn(new PageResponse<>(
                            List.of(
                                    new TargetLightResponse(123, "country de", null, 2000),
                                    new TargetLightResponse(124, "country ua", "ua recipients", 1000)
                            ),
                            new PageResponse.PageMeta(3, 2, 12)
                    ));

            Map<String, Object> params = Map.of(
                    "page", 3,
                    "size", 2,
                    "include", "recipientCount"
            );
            authenticated(params(get("/targets"), params))
                    .andExpectAll(
                            status().isOk(),
                            content().json(
                                    """
                                    {
                                        "page": {
                                          "number": 3,
                                          "size": 2,
                                          "totalCount": 12
                                        },
                                        "list": [
                                            {
                                              "id": 123,
                                              "name": "country de",
                                              "recipientCount": 2000
                                            },
                                            {
                                              "id": 124,
                                              "name": "country ua",
                                              "description": "ua recipients",
                                              "recipientCount": 1000
                                            }
                                        ]
                                    }
                                    """
                            )
                    )
                    .andDo(documentList());
        }

        @Test
        void shouldReturn400_whenIncludeParamInvalid() throws Exception {
            authenticated(get("/targets").param("include", "invalid")).andExpectAll(
                    status().isBadRequest(),
                    content().json(
                            """
                            {
                                "status": 400,
                                "detail": "Invalid input.",
                                "errors": [
                                    {
                                        "field": "include",
                                        "message": "Allowed values: [recipientCount]"
                                    }
                                ]
                            }
                            """
                    )
            );
        }

        @Test
        void shouldReturn403() throws Exception {
            expect(adminMock.allowedAll(Permission.TARGETS_SHOW)).andReturn(false);
            assertNotAuthorized(get("/targets"));
        }
    }

    @Nested
    class FindSingle {
        @Test
        void shouldReturn200() throws Exception {
            expect(adminMock.allowedAll(Permission.TARGETS_SHOW)).andReturn(true);
            expect(targetRestServiceMock.find(123, Set.of(TargetInclude.RECIPIENT_COUNT), adminMock)).andReturn(
                    new TargetResponse(
                            123,
                            "country de",
                            "de recipients",
                            "country = de",
                            "country = de",
                            true,
                            ZonedDateTime.of(2021, 10, 18, 19, 34, 30, 0, UTC).toInstant(),
                            ZonedDateTime.of(2026, 2, 3, 2, 0, 30, 0, UTC).toInstant(),
                            true,
                            1000
                    )
            );
            authenticated(get("/targets/{id}", 123).param("include", "RECIPIENT_COUNT"))
                    .andExpectAll(
                            status().isOk(),
                            content().json(
                                    """
                                    {
                                        "id": 123,
                                        "name": "country de",
                                        "sql": "country = de",
                                        "eql": "country = de",
                                        "valid": true,
                                        "isAccessLimiting": true,
                                        "recipientCount": 1000
                                    }
                                    """
                            )
                    )
                    .andDo(documentRead());
        }

        @Test
        void shouldReturn400_whenIdIsZero() throws Exception {
            authenticated(get("/targets/{id}", 0)).andExpectAll(
                    status().isBadRequest(),
                    content().json(
                            """
                            {
                                "status": 400,
                                "detail": "Invalid input.",
                                "errors": [
                                    {
                                        "field": "id",
                                        "message": "must be greater than 0"
                                    }
                                ]
                            }
                            """
                    )
            );
        }

        @Test
        void shouldReturn400_whenIncludeParamInvalid() throws Exception {
            authenticated(get("/targets/{id}", 123).param("include", "invalid")).andExpectAll(
                    status().isBadRequest(),
                    content().json(
                            """
                            {
                                "status": 400,
                                "detail": "Invalid input.",
                                "errors": [
                                    {
                                        "field": "include",
                                        "message": "Allowed values: [recipientCount]"
                                    }
                                ]
                            }
                            """
                    )
            );
        }

        @Test
        void shouldReturn403() throws Exception {
            expect(adminMock.allowedAll(Permission.TARGETS_SHOW)).andReturn(false);
            assertNotAuthorized(get("/targets/{id}", 1));
        }
    }

    @Nested
    class Create {
        @Test
        void shouldReturn201() throws Exception {
            expect(adminMock.allowedAll(Permission.TARGETS_CHANGE)).andReturn(true);
            CreateTargetRequest request = new CreateTargetRequest("country de", "de recipients", "country = de");
            expect(targetRestServiceMock.create(request, adminMock)).andReturn(TARGET_RESPONSE);

            authenticated(post("/targets").content(
                    """
                    {
                        "name": "country de",
                        "description": "de recipients",
                        "eql": "country = de"
                    }
                    """
            ))
                    .andExpectAll(
                            status().isCreated(),
                            content().json(RESPONSE_JSON)
                    )
                    .andDo(documentCreate());
        }

        @Test
        void shouldReturn403() throws Exception {
            expect(adminMock.allowedAll(Permission.TARGETS_CHANGE)).andReturn(false);
            assertNotAuthorized(post("/targets").content(
                    """
                    {
                        "name": "test",
                        "eql": "country = de"
                    }
                    """
            ));
        }
    }

    @Nested
    class Patch {
        @Test
        void shouldReturn200() throws Exception {
            expect(adminMock.allowedAll(Permission.TARGETS_CHANGE)).andReturn(true);

            var request = new PatchTargetRequest(
                    JsonNullable.of("country de"),
                    JsonNullable.of("de recipients"),
                    JsonNullable.of("country = de")
            );
            expect(targetRestServiceMock.patch(123, request, adminMock)).andReturn(TARGET_RESPONSE);

            authenticated(patch("/targets/{id}", 123).content(
                    """
                    {
                        "name": "country de",
                        "description": "de recipients",
                        "eql": "country = de"
                    }
                    """
            ))
                    .andExpectAll(
                            status().isOk(),
                            content().json(RESPONSE_JSON)
                    )
                    .andDo(documentPatch());
        }

        @Test
        void shouldReturn400_whenIdIsZero() throws Exception {
            authenticated(patch("/targets/{id}", 0).content("{}")).andExpectAll(
                    status().isBadRequest(),
                    content().json(
                            """
                            {
                                "status": 400,
                                "detail": "Invalid input.",
                                "errors": [
                                    {
                                        "field": "id",
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
            expect(adminMock.allowedAll(Permission.TARGETS_CHANGE)).andReturn(false);
            assertNotAuthorized(patch("/targets/{id}", 1).content("{}"));
        }
    }

    @Nested
    class Delete {
        @Test
        void shouldReturn204() throws Exception {
            expect(adminMock.allowedAll(Permission.TARGETS_DELETE)).andReturn(true);
            targetRestServiceMock.delete(123, adminMock);
            expectLastCall().once();

            authenticated(delete("/targets/{id}", 123))
                    .andExpect(status().isNoContent())
                    .andDo(documentDelete());
        }


        @Test
        void shouldReturn400_whenIdIsZero() throws Exception {
            authenticated(delete("/targets/{id}", 0)).andExpectAll(
                    status().isBadRequest(),
                    content().json(
                            """
                            {
                                "status": 400,
                                "detail": "Invalid input.",
                                "errors": [
                                    {
                                        "field": "id",
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
            expect(adminMock.allowedAll(Permission.TARGETS_DELETE)).andReturn(false);
            assertNotAuthorized(delete("/targets/{id}", 1));
        }
    }
}
