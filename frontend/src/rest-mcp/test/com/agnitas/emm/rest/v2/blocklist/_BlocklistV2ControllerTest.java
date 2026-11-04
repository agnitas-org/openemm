/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2.blocklist;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import com.agnitas.emm.core.Permission;
import com.agnitas.emm.rest.v2.infrastructure.search.PageResponse;
import com.agnitas.testsupport.annotation.MockBean;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;

class _BlocklistV2ControllerTest extends _BlocklistV2ControllerTestSupport {

    @MockBean
    private BlocklistRestService blocklistRestServiceMock;

    @Nested
    class PaginatedList {
        @Test
        void shouldReturn200() throws Exception {
            expect(adminMock.allowedAll(Permission.BLACKLIST)).andReturn(true);
            expect(blocklistRestServiceMock.find(
                    PageRequest.of(3, 2),
                    COMPANY_ID
            )).andReturn(new PageResponse<>(
                    List.of(
                            new BlocklistEntryResponse(
                                    "blocked.email1@example.com",
                                    "Delivery permanently blocked",
                                    Instant.parse("2024-01-02T03:04:56Z")
                            ),
                            new BlocklistEntryResponse(
                                    "blocked.email2@example.com",
                                    "Requested to be blocklisted",
                                    Instant.parse("2026-02-03T20:13:41Z")
                            )
                    ), new PageResponse.PageMeta(3, 2, 12)
            ));

            Map<String, Object> params = Map.of(
                    "page", 3,
                    "size", 2
            );
            authenticated(params(get("/blocklist"), params))
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
                                              "email": "blocked.email1@example.com",
                                              "reason": "Delivery permanently blocked",
                                              "date": "2024-01-02T03:04:56Z"
                                            },
                                            {
                                              "email": "blocked.email2@example.com",
                                              "reason": "Requested to be blocklisted",
                                              "date": "2026-02-03T20:13:41Z"
                                            }
                                        ]
                                    }
                                    """
                            )
                    )
                    .andDo(documentList());
        }

        @Test
        void shouldReturn403() throws Exception {
            expect(adminMock.allowedAll(Permission.BLACKLIST)).andReturn(false);
            assertNotAuthorized(get("/blocklist"));
        }
    }

    @Nested
    class GetByEmail {
        @Test
        void shouldReturn200() throws Exception {
            expect(adminMock.allowedAll(Permission.BLACKLIST)).andReturn(true);
            expect(blocklistRestServiceMock.find(
                    "blocked.email@example.com",
                    COMPANY_ID
            )).andReturn(new BlocklistEntryResponse(
                    "blocked.email@example.com",
                    "Delivery permanently blocked",
                    Instant.parse("2024-01-02T03:04:56Z")
            ));

            authenticated(get("/blocklist/{email}", "blocked.email@example.com"))
                    .andExpectAll(
                            status().isOk(),
                            content().json(
                                    """
                                    {
                                      "email": "blocked.email@example.com",
                                      "reason": "Delivery permanently blocked",
                                      "date": "2024-01-02T03:04:56Z"
                                    }
                                    """
                            )
                    )
                    .andDo(documentReadByEmail());
        }

        @Test
        void shouldReturn403() throws Exception {
            expect(adminMock.allowedAll(Permission.BLACKLIST)).andReturn(false);
            assertNotAuthorized(get("/blocklist/{email}", "blocked.email@example.com"));
        }

        @Test
        void shouldReturn400_whenEmailIsEmpty() throws Exception {
            authenticated(get("/blocklist/ ")).andExpectAll(
                    status().isBadRequest(),
                    content().json(
                            """
                            {
                              "status": 400,
                              "detail": "Invalid input.",
                              "errors": [
                                {
                                  "field": "email",
                                  "message": "must not be blank"
                                }
                              ]
                            }
                            """
                    )
            );
        }
    }

    @Nested
    class DeleteByEmail {
        @Test
        void shouldReturn204() throws Exception {
            expect(adminMock.allowedAll(Permission.RECIPIENT_DELETE)).andReturn(true);
            blocklistRestServiceMock.delete("blocked.email@example.com", COMPANY_ID);
            expectLastCall().once();

            authenticated(delete("/blocklist/{email}", "blocked.email@example.com"))
                    .andExpect(status().isNoContent())
                    .andDo(documentDeleteByEmail());
        }

        @Test
        void shouldReturn403() throws Exception {
            expect(adminMock.allowedAll(Permission.RECIPIENT_DELETE)).andReturn(false);
            assertNotAuthorized(delete("/blocklist/{email}", "blocked.email@example.com"));
        }

        @Test
        void shouldReturn400_whenEmailIsEmpty() throws Exception {
            authenticated(delete("/blocklist/ ")).andExpectAll(
                    status().isBadRequest(),
                    content().json(
                            """
                            {
                              "status": 400,
                              "detail": "Invalid input.",
                              "errors": [
                                {
                                  "field": "email",
                                  "message": "must not be blank"
                                }
                              ]
                            }
                            """
                    )
            );
        }
    }

    @Nested
    class AddEntry {
        @Test
        void shouldReturn201() throws Exception {
            expect(adminMock.allowedAll(Permission.BLACKLIST)).andReturn(true);
            expect(blocklistRestServiceMock.addEntry(anyObject(), anyObject())).andReturn(new BlocklistEntryResponse(
                    "new.blocked@example.com",
                    "Delivery permanently blocked",
                    Instant.parse("2024-01-02T03:04:56Z")
            ));

            authenticated(post("/blocklist").content(
                    """
                    {
                      "email": "new.blocked@example.com",
                      "reason": "Delivery permanently blocked"
                    }
                    """
            ))
                    .andExpectAll(
                            status().isCreated(),
                            content().json(
                                    """
                                    {
                                      "email": "new.blocked@example.com",
                                      "reason": "Delivery permanently blocked",
                                      "date": "2024-01-02T03:04:56Z"
                                    }
                                    """
                            )
                    )
                    .andDo(documentAddEntry());
        }

        @Test
        void shouldReturn403() throws Exception {
            expect(adminMock.allowedAll(Permission.BLACKLIST)).andReturn(false);
            assertNotAuthorized(
                    post("/blocklist").content(
                            """
                            {
                              "email": "new.blocked@example.com",
                              "reason": "delivery permanently blocked"
                            }
                            """
                    )
            );
        }

        @Test
        void shouldReturn400_whenEmailIsEmpty() throws Exception {
            authenticated(post("/blocklist").content(
                            """
                            {
                              "email": "",
                              "reason": "Test"
                            }
                            """
                    )
            ).andExpectAll(
                    status().isBadRequest(),
                    content().json(
                            """
                            {
                              "status": 400,
                              "detail": "Invalid input.",
                              "errors": [
                                {
                                  "field": "email",
                                  "message": "must not be blank"
                                }
                              ]
                            }
                            """
                    )
            );
        }

        @Test
        void shouldReturn400_whenEmailIsMissing() throws Exception {
            authenticated(post("/blocklist").content(
                    """
                    {
                      "reason": "Test"
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
                                  "field": "email",
                                  "message": "must not be blank"
                                }
                              ]
                            }
                            """
                    )
            );
        }
    }

    @Nested
    class PatchEntry {
        @Test
        void shouldReturn200() throws Exception {
            expect(adminMock.allowedAll(Permission.BLACKLIST)).andReturn(true);
            expect(blocklistRestServiceMock.updateEntry(anyObject(), anyObject(), anyObject()))
                    .andAnswer(() -> new BlocklistEntryResponse(
                            "blocked.email@example.com",
                            "Requested to be blocklisted",
                            Instant.parse("2024-01-02T03:04:56Z")
                    ));

            authenticated(patch("/blocklist/{email}", "blocked.email@example.com").content(
                    """
                    {
                      "reason": "Requested to be blocklisted"
                    }
                    """
            ))
                    .andExpectAll(
                            status().isOk(),
                            content().json(
                                    """
                                    {
                                      "email": "blocked.email@example.com",
                                      "reason": "Requested to be blocklisted",
                                      "date": "2024-01-02T03:04:56Z"
                                    }
                                    """
                            )
                    )
                    .andDo(documentUpdateEntry());
        }

        @Test
        void shouldReturn403() throws Exception {
            expect(adminMock.allowedAll(Permission.BLACKLIST)).andReturn(false);
            assertNotAuthorized(patch("/blocklist/{email}", "blocked.email@example.com")
                    .content("{ \"reason\": \"Some reason\" }")
            );
        }

        @Test
        void shouldReturn400_whenReasonIsMissing() throws Exception {
            authenticated(patch("/blocklist/blocked.email@example.com").content("{}")).andExpectAll(
                    status().isBadRequest(),
                    content().json(
                            """
                            {
                              "status": 400,
                              "detail": "Invalid input.",
                              "errors": [
                                {
                                  "field": "reason",
                                  "message": "must not be null"
                                }
                              ]
                            }
                            """
                    )
            );
        }

        @Test
        void shouldReturn400_whenEmailIsEmpty() throws Exception {
            authenticated(patch("/blocklist/ ").content(
                    """
                    {
                      "reason": "Test reason"
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
                                  "field": "email",
                                  "message": "must not be blank"
                                }
                              ]
                            }
                            """
                    )
            );
        }
    }
}
