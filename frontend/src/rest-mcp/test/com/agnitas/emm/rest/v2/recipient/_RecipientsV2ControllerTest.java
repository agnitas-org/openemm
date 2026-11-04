/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2.recipient;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.isA;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;

import com.agnitas.emm.common.UserStatus;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.emm.rest.v2.infrastructure.bulk.BulkDeleteResult;
import com.agnitas.emm.rest.v2.infrastructure.bulk.BulkOperationResponse;
import com.agnitas.emm.rest.v2.infrastructure.config.json.RestEnumAllowedValues;
import com.agnitas.emm.rest.v2.infrastructure.search.PageResponse;
import com.agnitas.emm.rest.v2.infrastructure.search.PageResponse.PageMeta;
import com.agnitas.emm.rest.v2.recipient.dto.BulkUpsertResponse;
import com.agnitas.emm.rest.v2.recipient.dto.RecipientSubscribeRequest;
import com.agnitas.messages.Message;
import com.agnitas.testsupport.annotation.MockBean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.data.domain.PageRequest;

class _RecipientsV2ControllerTest extends _RecipientsV2ControllerTestSupport {

    @MockBean
    private RecipientRestService recipientServiceMock;

    @BeforeEach
    void setUp() {
        expect(recipientServiceMock.isAllowedToUseHtml(anyObject(), eq(adminMock))).andStubReturn(false);
    }

    @Nested
    class PaginatedList {
        @Test
        void shouldReturn200() throws Exception {
            expect(adminMock.allowedAll(Permission.RECIPIENT_SHOW)).andReturn(true);

            Map<String, String> fieldFilter = Map.of("title", "Professor");
            expect(recipientServiceMock.list(PageRequest.of(3, 2), fieldFilter, adminMock))
                    .andReturn(new PageResponse<>(
                            List.of(
                                    Map.of(
                                            "customer_id", 11,
                                            "firstname", "Albert",
                                            "lastname", "Einstein",
                                            "email", "einstein@agnitas.com",
                                            "title", "Professor",
                                            "custom_profile_field", "filter_value"
                                    ), Map.of(
                                            "customer_id", 12,
                                            "firstname", "Max",
                                            "lastname", "Planck",
                                            "email", "planck@agnitas.com",
                                            "title", "Professor",
                                            "custom_profile_field", "filter_value"
                                    )
                            ), new PageMeta(3, 2, 1200)
                    ));

            Map<String, Object> params = Map.of(
                    "page", "3",
                    "size", "2",
                    "title", "Professor"
            );
            authenticated(params(get("/recipients"), params)).andExpectAll(
                    status().isOk(),
                    content().json(
                            """
                            {
                                "page": {
                                  "number": 3,
                                  "size": 2,
                                  "totalCount": 1200
                                },
                                "list": [
                                    {
                                      "customer_id": 11,
                                      "firstname": "Albert",
                                      "lastname": "Einstein",
                                      "email": "einstein@agnitas.com",
                                      "title": "Professor",
                                      "custom_profile_field": "filter_value"
                                    },
                                    {
                                      "customer_id": 12,
                                      "firstname": "Max",
                                      "lastname": "Planck",
                                      "email": "planck@agnitas.com",
                                      "title": "Professor",
                                      "custom_profile_field": "filter_value"
                                    }
                                ]
                            }
                            """
                    )
            ).andDo(documentList());
        }

        @Test
        void shouldReturn403() throws Exception {
            expect(adminMock.allowedAll(Permission.RECIPIENT_SHOW)).andReturn(false);
            assertNotAuthorized(get("/recipients"));
        }
    }

    @Nested
    class ListIds {
        @Test
        void shouldReturn200() throws Exception {
            expect(adminMock.allowedAll(Permission.RECIPIENT_SHOW)).andReturn(true);
            Map<String, String> fieldFilter = Map.of("title", "Professor");
            expect(recipientServiceMock.listIds(fieldFilter, adminMock)).andReturn(List.of(123, 124, 1234));

            authenticated(get("/recipients/ids").param("title", "Professor"))
                    .andExpectAll(
                            status().isOk(),
                            content().json("[123, 124, 1234]")
                    )
                    .andDo(documentListIds());
        }

        @Test
        void shouldReturn403() throws Exception {
            expect(adminMock.allowedAll(Permission.RECIPIENT_SHOW)).andReturn(false);
            assertNotAuthorized(get("/recipients/ids"));
        }
    }

    @Nested
    class ReadById {
        @Test
        void shouldReturn200() throws Exception {
            expect(adminMock.allowedAll(Permission.RECIPIENT_SHOW)).andReturn(true);
            expect(recipientServiceMock.find(1, adminMock)).andReturn(Map.of(
                    "customer_id", 1,
                    "firstname", "name",
                    "lastname", "Einstein",
                    "custom_profile_field", "value",
                    "title", "Professor",
                    "email", "email@agnitas.test"
            ));

            authenticated(get("/recipients/{id}", 1)).andExpectAll(
                    status().isOk(),
                    content().json(
                            """
                            {
                              "customer_id": 1,
                              "firstname": "name",
                              "lastname": "Einstein",
                              "custom_profile_field": "value",
                              "title": "Professor",
                              "email": "email@agnitas.test"
                            }
                            """
                    )
            ).andDo(documentReadById());
        }

        @Test
        void shouldReturn403() throws Exception {
            expect(adminMock.allowedAll(Permission.RECIPIENT_SHOW)).andReturn(false);
            assertNotAuthorized(get("/recipients/{id}", 1));
        }
    }

    @Nested
    class ReadByEmail {
        @Test
        void shouldReturn200() throws Exception {
            expect(adminMock.allowedAll(Permission.RECIPIENT_SHOW)).andReturn(true);
            expect(recipientServiceMock.find("email@example.com", adminMock)).andReturn(Map.of(
                    "customer_id", 1,
                    "firstname", "name",
                    "lastname", "Einstein",
                    "custom_profile_field", "value",
                    "title", "Professor",
                    "email", "email@example.com"
            ));

            authenticated(get("/recipients/by-email/{email}", "email@example.com"))
                    .andExpectAll(
                            status().isOk(),
                            content().json(
                                    """
                                    {
                                      "customer_id": 1,
                                      "firstname": "name",
                                      "lastname": "Einstein",
                                      "custom_profile_field": "value",
                                      "title": "Professor",
                                      "email": "email@example.com"
                                    }
                                    """
                            )
                    )
                    .andDo(documentReadByEmail());
        }

        @Test
        void shouldReturn403() throws Exception {
            expect(adminMock.allowedAll(Permission.RECIPIENT_SHOW)).andReturn(false);
            assertNotAuthorized(get("/recipients/by-email/{email}", "email@example.com"));
        }
    }

    @Nested
    class Create {
        @Test
        void shouldAllowUnsafeHtmlInBodyField() throws Exception {
            expect(adminMock.allowedAll(Permission.RECIPIENT_CREATE)).andReturn(true);
            Map<String, Object> recipient = Map.of(
                    "email", "email@agnitas.test",
                    "unsafeHtml", "<script>some unsafe html</script>"
            );

            expect(recipientServiceMock.create(eq(recipient), isA(RecipientSubscribeRequest.class), eq(adminMock)))
                    .andReturn(recipient);

            expect(recipientServiceMock.isAllowedToUseHtml("unsafeHtml", adminMock)).andReturn(true);

            authenticated(post("/recipients").content(
                    """
                    {
                      "email": "email@agnitas.test",
                      "unsafeHtml": "<script>some unsafe html</script>"
                    }
                    """
            )).andExpectAll(
                    status().isCreated(),
                    content().json(
                            """
                            {
                              "email": "email@agnitas.test",
                              "unsafeHtml": "<script>some unsafe html</script>"
                            }
                            """
                    )
            );
        }

        @Test
        void shouldReturn201() throws Exception {
            RecipientSubscribeRequest subscribeRequest = new RecipientSubscribeRequest(
                    "3",
                    MediaTypes.EMAIL,
                    UserStatus.Active
            );
            Map<String, Object> expectedInput = Map.of(
                    "email", "email@agnitas.test",
                    "firstname", "firstname",
                    "lastname", "lastname",
                    "title", "Professor",
                    "custom_profile_field", "value"
            );
            expect(adminMock.allowedAll(Permission.RECIPIENT_CREATE)).andReturn(true);
            expect(recipientServiceMock.create(expectedInput, subscribeRequest, adminMock))
                    .andReturn(Map.of(
                            "customer_id", 2,
                            "email", "email@agnitas.test",
                            "firstname", "firstname",
                            "lastname", "lastname",
                            "title", "Professor",
                            "custom_profile_field", "value"
                    ));

            authenticated(post("/recipients")
                    .param("mailinglist", "3")
                    .param("status", UserStatus.Active.name())
                    .param("mediaType", MediaTypes.EMAIL.name())
                    .content(
                            """
                            {
                              "email": "email@agnitas.test",
                              "firstname": "firstname",
                              "lastname": "lastname",
                              "title": "Professor",
                              "custom_profile_field": "value"
                            }
                            """
                    )
            ).andExpectAll(
                    status().isCreated(),
                    content().json(
                            """
                            {
                              "customer_id": 2,
                              "email": "email@agnitas.test",
                              "custom_profile_field": "value",
                              "title": "Professor",
                              "firstname": "firstname",
                              "lastname": "lastname",
                            }
                            """
                    )
            ).andDo(documentCreate());
        }

        @ParameterizedTest
        @ValueSource(strings = {"-1", "0", "*1", "a", ""})
        void shouldReturn400_whenMailinglistToSubscribeIsInvalid(String mailinglistParam) throws Exception {
            authenticated(post("/recipients")
                    .param("mailinglist", mailinglistParam)
                    .content(
                            """
                            {
                              "email": "email@agnitas.test"
                            }
                            """
                    )
            ).andExpectAll(
                    status().isBadRequest(),
                    content().json(
                            """
                            {
                              "status": 400,
                              "errors": [
                                {
                                  "field": "mailinglist",
                                  "message": "mailinglist must be '*' or ID"
                                }
                              ]
                            }
                            """
                    )
            );
        }

        @ParameterizedTest
        @ValueSource(strings = {"Active1", "-1", "0", "8", "a"})
        void shouldReturn400_whenSubscribeUserStatusIsInvalid(String status) throws Exception {
            authenticated(post("/recipients")
                    .param("mailinglist", "123")
                    .param("status", status)
                    .content(
                            """
                            {
                              "email": "email@agnitas.test"
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
                                  "field": "status",
                                  "message": "%s"
                                }
                              ]
                            }l
                            """.formatted(RestEnumAllowedValues.USER_STATUSES)
                    )
            );
        }

        @ParameterizedTest
        @ValueSource(strings = {"FAX", "-1", "1", "3", "9", "a"})
        void shouldReturn400_whenSubscribeMediatypeIsInvalid(String mediaType) throws Exception {
            authenticated(post("/recipients")
                    .param("mailinglist", "123")
                    .param("mediaType", mediaType)
                    .content(
                            """
                            {
                              "email": "email@agnitas.test"
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
                                  "field": "mediaType",
                                  "message": "%s"
                                }
                              ]
                            }
                            """.formatted(RestEnumAllowedValues.MEDIATYPES)
                    )
            );
        }

        @Test
        void shouldReturn200_whenRecipientBulkCreateHasMixedResults() throws Exception {
            expect(adminMock.allowedAll(Permission.RECIPIENT_CREATE, Permission.IMPORT_MODE_ADD)).andReturn(true);
            RecipientSubscribeRequest subscribeRequest = new RecipientSubscribeRequest("3", MediaTypes.EMAIL, null);

            expect(recipientServiceMock.create(
                    List.of(
                            Map.of(
                                    "email", "recipient1@agnitas.test",
                                    "firstname", "firstname1",
                                    "lastname", "lastname1",
                                    "title", "Professor",
                                    "custom_profile_field", "value1"
                            ),
                            Map.of(
                                    "email", "recipient2 @ invalid email",
                                    "firstname", "firstname2",
                                    "lastname", "lastname2",
                                    "title", "Professor",
                                    "custom_profile_field", "value2"
                            )
                    ), subscribeRequest, adminMock
            )).andReturn(new BulkUpsertResponse(
                    2, 1, 1, Map.of(
                    "BLACKLIST_ERROR", 0,
                    "DATE_ERROR", 0,
                    "GENDER_ERROR", 0,
                    "NUMERIC_ERROR", 0,
                    "EMAIL_ERROR", 1
            )
            ));

            authenticated(post("/recipients/bulk")
                    .param("mailinglist", "3")
                    .param("mediaType", MediaTypes.EMAIL.name())
                    .content(
                            """
                            [
                              {
                                "email": "recipient1@agnitas.test",
                                "firstname": "firstname1",
                                "lastname": "lastname1",
                                "title": "Professor",
                                "custom_profile_field": "value1"
                              },
                              {
                                "email": "recipient2 @ invalid email",
                                "firstname": "firstname2",
                                "lastname": "lastname2",
                                "title": "Professor",
                                "custom_profile_field": "value2"
                              }
                            ]
                            """
                    )
            ).andExpectAll(
                    status().isOk(),
                    content().json(
                            """
                            {
                              "total": 2,
                              "succeeded": 1,
                              "failed": 1,
                              "errors": {
                                  "BLACKLIST_ERROR": 0,
                                  "DATE_ERROR": 0,
                                  "GENDER_ERROR": 0,
                                  "NUMERIC_ERROR": 0,
                                  "EMAIL_ERROR": 1
                              }
                            }
                            """
                    )
            ).andDo(documentBulkCreate());
        }

        @Test
        void shouldReturn200_whenAllEntriesSuccessfullyCreated() throws Exception {
            expect(adminMock.allowedAll(Permission.RECIPIENT_CREATE, Permission.IMPORT_MODE_ADD)).andReturn(true);
            expect(recipientServiceMock.create(
                    List.of(
                            Map.of("email", "recipient1@agnitas.test"),
                            Map.of("email", "recipient2@agnitas.test")
                    ), new RecipientSubscribeRequest(null, null, null), adminMock
            )).andReturn(new BulkUpsertResponse(2, 2, 0, Map.of()));

            authenticated(post("/recipients/bulk").content(
                    """
                    [
                      {
                        "email": "recipient1@agnitas.test"
                      },
                      {
                        "email": "recipient2@agnitas.test"
                      }
                    ]
                    """
            )).andExpectAll(
                    status().isOk(),
                    content().json(
                            """
                            {
                              "total": 2,
                              "succeeded": 2,
                              "failed": 0
                            }
                            """
                    )
            );
        }

        @Test
        void shouldReturn400_whenStatusParameterUsed() throws Exception {
            authenticated(post("/recipients/bulk")
                    .param("status", "Active")
                    .content(
                            """
                            [
                              {
                                "email": "recipient1@agnitas.test"
                              },
                              {
                                "email": "recipient2@agnitas.test"
                              }
                            ]
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
                                  "field": "status",
                                  "message": "Parameter status is not allowed for bulk creation of recipient. Use the import mode to define which subscription status to set"
                                }
                              ]
                            }
                            """
                    )
            );
        }

        @Test
        void shouldReturn403() throws Exception {
            expect(adminMock.allowedAll(Permission.RECIPIENT_CREATE)).andReturn(false);
            assertNotAuthorized(post("/recipients").content("{}"));
        }
    }

    @Nested
    class PatchByEmail {
        @Test
        void shouldReturn200() throws Exception {
            expect(adminMock.allowedAll(Permission.RECIPIENT_CHANGE)).andReturn(true);
            var request = new RecipientSubscribeRequest("123", MediaTypes.EMAIL, UserStatus.Active);
            Map<String, Object> fields = Map.of("firstname", "New");
            expect(recipientServiceMock.updatePartially("email@example.com", fields, request, adminMock))
                    .andReturn(Map.of(
                            "customer_id", 1,
                            "firstname", "New",
                            "lastname", "Recipient",
                            "custom_profile_field", "value",
                            "title", "Professor",
                            "email", "email@example.com"
                    ));

            Map<String, Object> params = Map.of(
                    "mailinglist", 123,
                    "mediaType", "EMAIL",
                    "status", 1
            );
            authenticated(params(patch("/recipients/by-email/{email}", "email@example.com"), params).content(
                    """
                    {
                      "firstname": "New"
                    }
                    """
            ))
                    .andExpectAll(
                            status().isOk(),
                            content().json(
                                    """
                                    {
                                      "customer_id": 1,
                                      "firstname": "New",
                                      "lastname": "Recipient",
                                      "custom_profile_field": "value",
                                      "title": "Professor",
                                      "email": "email@example.com"
                                    }
                                    """
                            )
                    )
                    .andDo(documentPatchByEmail());
        }

        @Test
        void shouldReturn403() throws Exception {
            expect(adminMock.allowedAll(Permission.RECIPIENT_CHANGE)).andReturn(false);
            assertNotAuthorized(patch("/recipients/by-email/{email}", "email@example.com").content("{}"));
        }
    }

    @Nested
    class PatchById {
        @Test
        void shouldReturn200() throws Exception {
            var request = new RecipientSubscribeRequest("123", MediaTypes.EMAIL, UserStatus.Active);
            expect(adminMock.allowedAll(Permission.RECIPIENT_CHANGE)).andReturn(true);
            Map<String, Object> fields = Map.of("firstname", "New");
            expect(recipientServiceMock.updatePartially(2, fields, request, adminMock))
                    .andReturn(Map.of(
                            "customer_id", 2,
                            "firstname", "New",
                            "lastname", "Recipient",
                            "custom_profile_field", "value",
                            "title", "Professor",
                            "email", "email@agnitas.test"
                    ));

            Map<String, Object> params = Map.of(
                    "mailinglist", 123,
                    "mediaType", "EMAIL",
                    "status", 1
            );
            authenticated(params(patch("/recipients/{id}", 2), params).content(
                    """
                    {
                      "firstname": "New"
                    }
                    """
            ))
                    .andExpectAll(
                            status().isOk(),
                            content().json(
                                    """
                                    {
                                      "customer_id": 2,
                                      "firstname": "New",
                                      "lastname": "Recipient",
                                      "custom_profile_field": "value",
                                      "title": "Professor",
                                      "email": "email@agnitas.test"
                                    }
                                    """
                            )
                    )
                    .andDo(documentPatchById());
        }

        @Test
        void shouldReturn403() throws Exception {
            expect(adminMock.allowedAll(Permission.RECIPIENT_CHANGE)).andReturn(false);
            assertNotAuthorized(patch("/recipients/{id}", 1).content("{}"));
        }
    }

    @Nested
    class DeleteByEmail {
        @Test
        void shouldReturn204() throws Exception {
            expect(adminMock.allowedAll(Permission.RECIPIENT_DELETE)).andReturn(true);
            recipientServiceMock.delete("email@example.com", adminMock);
            expectLastCall();

            authenticated(delete("/recipients/by-email/{email}", "email@example.com"))
                    .andExpect(status().isNoContent())
                    .andDo(documentDeleteByEmail());
        }

        @Test
        void shouldReturn403() throws Exception {
            expect(adminMock.allowedAll(Permission.RECIPIENT_DELETE)).andReturn(false);
            assertNotAuthorized(delete("/recipients/by-email/{email}", "email@example.com"));
        }
    }

    @Nested
    class DeleteById {
        @Test
        void shouldReturn204() throws Exception {
            expect(adminMock.allowedAll(Permission.RECIPIENT_DELETE)).andReturn(true);
            recipientServiceMock.delete(2, adminMock);
            expectLastCall();

            authenticated(delete("/recipients/{id}", 2))
                    .andExpect(status().isNoContent())
                    .andDo(documentDeleteById());
        }

        @Test
        void shouldReturn200_whenBulkDeleteHasMixedResults() throws Exception {
            expect(adminMock.allowedAll(Permission.RECIPIENT_DELETE)).andReturn(true);
            expect(recipientServiceMock.delete(List.of(101, 102, 103), adminMock)).andReturn(BulkOperationResponse.from(
                    List.of(
                            BulkDeleteResult.deleted(101),
                            BulkDeleteResult.notFound(102),
                            BulkDeleteResult.failed(103, Message.exact("Error"))
                    )
            ));

            authenticated(delete("/recipients/bulk").content(
                    """
                    {
                        "ids": [101, 102, 103]
                    }
                    """
            )).andExpectAll(
                    status().isOk(),
                    content().json(
                            """
                            {
                              "total": 3,
                              "succeeded": 1,
                              "failed": 2,
                              "results": [
                                { "id": 101, "status": "DELETED" },
                                {
                                  "id": 102,
                                  "status": "NOT_FOUND",
                                  "errors": [{ "message": "Not found" }]
                                },
                                {
                                  "id": 103,
                                  "status": "FAILED",
                                  "errors": [{ "message": "Error" }]
                                }
                              ]
                            }
                            """
                    )
            ).andDo(documentBulkDelete());
        }

        @Test
        void shouldReturn400_whenNegativeIdIsPassedOnBulkDelete() throws Exception {
            authenticated(delete("/recipients/bulk").content(
                    """
                    {
                      "ids": [-1]
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
                                  "field": "ids[]",
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
            expect(adminMock.allowedAll(Permission.RECIPIENT_DELETE)).andReturn(false);
            assertNotAuthorized(delete("/recipients/{id}", 2));
        }
    }

    @Nested
    class BulkUpsert {
        @Test
        void shouldReturn200_whenAllEntriesSuccessfullyUpserted() throws Exception {
            expect(adminMock.allowedAll(
                    Permission.RECIPIENT_CHANGE,
                    Permission.IMPORT_MODE_ADD_UPDATE
            )).andReturn(true);
            expect(recipientServiceMock.addOrUpdatePartially(
                    List.of(
                            Map.of("email", "recipient1@agnitas.test", "name", "updated1"),
                            Map.of("email", "recipient2@agnitas.test", "name", "updated2")
                    ), new RecipientSubscribeRequest(null, null, null), adminMock
            )).andReturn(new BulkUpsertResponse(2, 2, 0, Map.of()));

            authenticated(patch("/recipients/bulk").content(
                    """
                    [
                      {
                        "email": "recipient1@agnitas.test",
                        "name": "updated1"
                      },
                      {
                        "email": "recipient2@agnitas.test",
                        "name": "updated2"
                      }
                    ]
                    """
            )).andExpectAll(
                    status().isOk(),
                    content().json(
                            """
                            {
                              "total": 2,
                              "succeeded": 2,
                              "failed": 0
                            }
                            """
                    )
            );
        }

        @Test
        void shouldReturn200_whenBulkUpdateHasMixedResults() throws Exception {
            expect(adminMock.allowedAll(
                    Permission.RECIPIENT_CHANGE,
                    Permission.IMPORT_MODE_ADD_UPDATE
            )).andReturn(true);
            expect(recipientServiceMock.addOrUpdatePartially(
                    List.of(
                            Map.of(
                                    "email", "recipient1@agnitas.test",
                                    "firstname", "updated1",
                                    "lastname", "lastname1",
                                    "title", "Professor",
                                    "custom_profile_field", "value1"
                            ),
                            Map.of(
                                    "email", "recipient2 @ invalid email",
                                    "firstname", "updated2",
                                    "lastname", "lastname2",
                                    "title", "Professor",
                                    "custom_profile_field", "value2"
                            )
                    ), new RecipientSubscribeRequest(null, null, null), adminMock
            )).andReturn(new BulkUpsertResponse(
                    2, 1, 1, Map.of(
                    "BLACKLIST_ERROR", 0,
                    "DATE_ERROR", 0,
                    "GENDER_ERROR", 0,
                    "NUMERIC_ERROR", 0,
                    "EMAIL_ERROR", 1
            )
            ));

            authenticated(patch("/recipients/bulk").content(
                    """
                    [
                      {
                        "email": "recipient1@agnitas.test",
                        "firstname": "updated1",
                        "lastname": "lastname1",
                        "title": "Professor",
                        "custom_profile_field": "value1"
                      },
                      {
                        "email": "recipient2 @ invalid email",
                        "firstname": "updated2",
                        "lastname": "lastname2",
                        "title": "Professor",
                        "custom_profile_field": "value2"
                      }
                    ]
                    """
            )).andExpectAll(
                    status().isOk(),
                    content().json(
                            """
                            {
                              "total": 2,
                              "succeeded": 1,
                              "failed": 1,
                              "errors": {
                                  "BLACKLIST_ERROR": 0,
                                  "DATE_ERROR": 0,
                                  "GENDER_ERROR": 0,
                                  "NUMERIC_ERROR": 0,
                                  "EMAIL_ERROR": 1
                              }
                            }
                            """
                    )
            ).andDo(documentBulkUpsert());
        }

        @Test
        void shouldReturn403() throws Exception {
            expect(adminMock.allowedAll(
                    Permission.RECIPIENT_CHANGE,
                    Permission.IMPORT_MODE_ADD_UPDATE
            )).andReturn(false);
            assertNotAuthorized(patch("/recipients/bulk").content("[]"));
        }
    }
}
