/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2.mailinglist;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;

import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.mailinglist.exception.MailinglistNotExistException;
import com.agnitas.emm.rest.v2.infrastructure.search.PageResponse;
import com.agnitas.emm.rest.v2.infrastructure.search.PageResponse.PageMeta;
import com.agnitas.testsupport.annotation.MockBean;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

class _MailinglistsV2ControllerTest extends _MailinglistsV2ControllerTestSupport {

    @MockBean
    private MailinglistRestService mailinglistServiceMock;

    @Nested
    class PaginatedList {
        @Test
        void shouldReturn200() throws Exception {
            expect(adminMock.allowedAll(Permission.MAILINGLIST_SHOW)).andReturn(true);

            expect(mailinglistServiceMock.findAll(PageRequest.of(2, 10, Sort.by(Sort.Direction.DESC, "name")), COMPANY_ID))
                    .andReturn(new PageResponse<>(
                            List.of(
                                    new MailinglistResponse(2, "name 2", "description 2"),
                                    new MailinglistResponse(1, "name 1", "description 1")
                            ),
                            new PageMeta(2, 10, 12)
                    ));

            Map<String, Object> params = Map.of(
                    "page", "2",
                    "size", "10",
                    "sort", "name,desc"
            );
            authenticated(params(get("/mailinglists"), params)).andExpectAll(
                    status().isOk(),
                    content().json(
                            """
                            {
                                "page": {
                                  "number": 2,
                                  "size": 10,
                                  "totalCount": 12
                                },
                                "list": [
                                    {
                                        "id": 2,
                                        "name": "name 2",
                                        "description": "description 2"
                                    },
                                    {
                                        "id": 1,
                                        "name": "name 1",
                                        "description": "description 1"
                                    }
                                ]
                            }
                            """
                    )
            ).andDo(documentList());
        }

        @Test
        void shouldReturn400_whenSortInvalid() throws Exception {
            authenticated(get("/mailinglists").param("sort", "invalid")).andExpectAll(
                    status().isBadRequest(),
                    content().json(
                            """
                            {
                                "status": 400,
                                "detail": "Invalid input.",
                                "errors": [
                                    {
                                        "field": "sort",
                                        "message": "Invalid sort value 'invalid'. Allowed values: [name, description, id]"
                                    }
                                ]
                            }
                            """
                    )
            );
        }

        @Test
        void shouldReturn403() throws Exception {
            expect(adminMock.allowedAll(Permission.MAILINGLIST_SHOW)).andReturn(false);
            assertNotAuthorized(get("/mailinglists"));
        }
    }

    @Nested
    class FindSingle {
        @Test
        void shouldReturn200() throws Exception {
            expect(adminMock.allowedAll(Permission.MAILINGLIST_SHOW)).andReturn(true);
            expectLastCall();
            MailinglistResponse mailinglist = new MailinglistResponse(1, "test name", "description");
            expect(mailinglistServiceMock.getById(1, COMPANY_ID)).andReturn(mailinglist);

            authenticated(get("/mailinglists/{id}", 1)).andExpectAll(
                    status().isOk(),
                    content().json(
                            """
                            {
                                "id": 1,
                                "name": "test name",
                                "description": "description"
                            }
                            """
                    )
            ).andDo(documentRead());
        }

        @Test
        void shouldReturn400_whenIdIsZero() throws Exception {
            authenticated(get("/mailinglists/0")).andExpectAll(
                    status().is4xxClientError(),
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
            expect(adminMock.allowedAll(Permission.MAILINGLIST_SHOW)).andReturn(false);
            assertNotAuthorized(get("/mailinglists/{id}", 1));
        }

        @Test
        void shouldReturn404() throws Exception {
            expect(adminMock.allowedAll(Permission.MAILINGLIST_SHOW)).andReturn(true);
            expect(mailinglistServiceMock.getById(2, COMPANY_ID)).andThrow(new MailinglistNotExistException(2, COMPANY_ID));

            authenticated(get("/mailinglists/{id}", 2)).andExpectAll(
                    status().isNotFound(),
                    content().json(
                            """
                            {
                                "status": 404,
                                "detail": "Unknown mailinglist ID 2 (company ID %d)",
                            }
                            """.formatted(COMPANY_ID)
                    )
            );
        }
    }

    @Nested
    class Create {
        @Test
        void shouldReturn201() throws Exception {
            var createDto = new CreateMailinglistRequest("new name", "new description");
            var response = new MailinglistResponse(2, "new name", "new description");
            expect(adminMock.allowedAll(Permission.MAILINGLIST_CHANGE)).andReturn(true);
            expect(mailinglistServiceMock.create(createDto, COMPANY_ID)).andReturn(response);

            authenticated(post("/mailinglists")
                    .content(
                            """
                            {
                                "name": "new name",
                                "description": "new description"
                            }
                            """
                    )
            ).andExpectAll(
                    status().isCreated(),
                    content().json(
                            """
                            {
                                "id": 2,
                                "name": "new name",
                                "description": "new description"
                            }
                            """
                    )
            ).andDo(documentCreate());
        }

        @Test
        void shouldReturn403() throws Exception {
            expect(adminMock.allowedAll(Permission.MAILINGLIST_CHANGE)).andReturn(false);
            assertNotAuthorized(post("/mailinglists")
                    .content(
                            """
                            {
                                "name": "new name",
                                "description": "new description"
                            }
                            """
                    )
            );
        }
    }

    @Nested
    class Patch {
        @Test
        void shouldReturn200() throws Exception {
            var updateDto = new PatchMailinglistRequest(JsonNullable.undefined(), JsonNullable.of("Updated Description"));
            var response = new MailinglistResponse(2, "Old Name", "Updated Description");
            expect(adminMock.allowedAll(Permission.MAILINGLIST_CHANGE)).andReturn(true);
            expect(mailinglistServiceMock.updatePartially(2, COMPANY_ID, updateDto)).andReturn(response);

            authenticated(patch("/mailinglists/{id}", 2)
                    .content(
                            """
                            {
                                "description": "Updated Description"
                            }
                            """
                    )
            ).andExpectAll(
                    status().isOk(),
                    content().json(
                            """
                            {
                                "id": 2,
                                "name": "Old Name",
                                "description": "Updated Description"
                            }
                            """
                    )
            ).andDo(documentPatch());
        }

        @Test
        void shouldReturn400_whenShortnameIsTooShort() throws Exception {
            authenticated(patch("/mailinglists/{id}", 2)
                    .content(
                            """
                            {
                                "name": "ab"
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
                                        "field": "name",
                                        "message": "size must be between 3 and 2147483647"
                                    }
                                ]
                            }
                            """
                    )
            );
        }

        @Test
        void shouldReturn400_whenDescriptionIsTooLong() throws Exception {
            String tooLongDescription = RandomStringUtils.secure().nextAlphabetic(2001);

            authenticated(patch("/mailinglists/{id}", 1)
                    .content(
                            """
                            {
                                "description": "%s"
                            }
                            """.formatted(tooLongDescription)
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
                                        "field": "description",
                                        "message": "size must be between 0 and 2000"
                                    }
                                ]
                            }
                            """
                    )
            );
        }

        @Test
        void shouldReturn403() throws Exception {
            expect(adminMock.allowedAll(Permission.MAILINGLIST_CHANGE)).andReturn(false);
            assertNotAuthorized(
                    patch("/mailinglists/{id}", 2)
                            .content(
                                    """
                                    {
                                      "name": "Updated Name",
                                      "description": "Updated Description"
                                    }
                                    """
                            )
            );
        }
    }

    @Nested
    class Delete {
        @Test
        void shouldReturn204() throws Exception {
            expect(adminMock.allowedAll(Permission.MAILINGLIST_DELETE)).andReturn(true);
            mailinglistServiceMock.delete(2, adminMock);
            expectLastCall();

            authenticated(delete("/mailinglists/{id}", 2))
                    .andExpect(status().isNoContent())
                    .andDo(documentDelete());
        }

        @Test
        void shouldReturn403() throws Exception {
            expect(adminMock.allowedAll(Permission.MAILINGLIST_DELETE)).andReturn(false);
            assertNotAuthorized(delete("/mailinglists/{id}", 1));
        }
    }
}
