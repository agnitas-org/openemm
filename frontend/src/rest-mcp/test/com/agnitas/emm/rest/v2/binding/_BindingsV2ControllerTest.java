/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2.binding;

import static java.time.ZoneOffset.UTC;
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

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.agnitas.beans.BindingEntry.UserType;
import com.agnitas.emm.common.UserStatus;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.binding.BindingIdentifier;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.emm.rest.v2.infrastructure.bulk.BulkDeleteResult;
import com.agnitas.emm.rest.v2.infrastructure.bulk.BulkOperationResponse;
import com.agnitas.emm.rest.v2.infrastructure.config.json.RestEnumAllowedValues;
import com.agnitas.emm.rest.v2.infrastructure.search.PageResponse;
import com.agnitas.testsupport.annotation.MockBean;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.data.domain.PageRequest;

class _BindingsV2ControllerTest extends _BindingsV2ControllerTestSupport {

    private static final BindingResponse BINDING_RESPONSE = new BindingResponse(
            1, 101, "W", "EMAIL", "Active", "RecipientImport", "https:/shopsite.de/", 1, 2,
            ZonedDateTime.of(2021, 10, 18, 19, 34, 30, 0, UTC).toInstant(),
            ZonedDateTime.of(2026, 2, 3, 2, 0, 30, 0, UTC).toInstant(), "Action started"
    );

    private static final String RESPONSE_JSON =
            """
            {
              "mailinglistId": 1,
              "recipientId": 101,
              "userType": "W",
              "mediaType": "EMAIL",
              "userStatus": "Active",
              "userRemark": "RecipientImport",
              "referrer": "https:/shopsite.de/",
              "entryMailingId": 1,
              "exitMailingId": 2,
              "creationDate": "2021-10-18T19:34:30Z",
              "changeDate": "2026-02-03T02:00:30Z"
            }
            """;

    @MockBean
    @SuppressWarnings("unused")
    private BindingRestService bindingsServiceMock;

    @Nested
    class PaginatedList {
        @Test
        void shouldReturn200() throws Exception {
            expect(adminMock.allowedAll(Permission.RECIPIENT_SHOW)).andReturn(true);
            var request = new ListBindingsRequest(
                    Set.of(101, 102),
                    Set.of("first@example.com", "second@example.com"),
                    Set.of(1, 2),
                    Set.of(MediaTypes.EMAIL),
                    Set.of(UserStatus.Active, UserStatus.AdminOut),
                    "2026-02-03"
            );
            expect(bindingsServiceMock.find(request, PageRequest.of(3, 2), COMPANY_ID))
                    .andReturn(new PageResponse<>(List.of(
                            new BindingResponse(
                                    1, 101, "W", "EMAIL", "Active", "RecipientImport", "https:/shopsite.de/", 1, 2,
                                    ZonedDateTime.of(2021, 10, 18, 19, 34, 30, 0, UTC).toInstant(),
                                    ZonedDateTime.of(2026, 2, 3, 2, 0, 30, 0, UTC).toInstant(), null
                            ),
                            new BindingResponse(
                                    2, 102, "W", "EMAIL", "AdminOut", "Opt-Out by ADMIN", "", 3, 4,
                                    ZonedDateTime.of(2024, 1, 2, 3, 4, 56, 0, UTC).toInstant(),
                                    ZonedDateTime.of(2026, 2, 3, 20, 13, 41, 0, UTC).toInstant(), null
                            )
                    ), new PageResponse.PageMeta(3, 2, 12)
                    ));

            Map<String, Object> params = Map.of(
                    "page", 3,
                    "size", 2,
                    "recipientIds", List.of(101, 102),
                    "emails", List.of("first@example.com", "second@example.com"),
                    "mailinglistIds", List.of(1, 2),
                    "mediaTypes", List.of("EMAIL"),
                    "userStatuses", List.of("Active", "AdminOut"),
                    "date", "2026-02-03"
            );
            authenticated(params(get("/bindings"), params))
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
                                              "mailinglistId": 1,
                                              "recipientId": 101,
                                              "userType": "W",
                                              "mediaType": "EMAIL",
                                              "userStatus": "Active",
                                              "userRemark": "RecipientImport",
                                              "referrer": "https:/shopsite.de/",
                                              "entryMailingId": 1,
                                              "exitMailingId": 2,
                                              "creationDate": "2021-10-18T19:34:30Z",
                                              "changeDate": "2026-02-03T02:00:30Z"
                                            },
                                            {
                                              "mailinglistId": 2,
                                              "recipientId": 102,
                                              "userType": "W",
                                              "mediaType": "EMAIL",
                                              "userStatus": "AdminOut",
                                              "userRemark": "Opt-Out by ADMIN",
                                              "entryMailingId": 3,
                                              "exitMailingId": 4,
                                              "creationDate": "2024-01-02T03:04:56Z",
                                              "changeDate": "2026-02-03T20:13:41Z"
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
            expect(adminMock.allowedAll(Permission.RECIPIENT_SHOW)).andReturn(false);
            assertNotAuthorized(get("/bindings").content("{}"));
        }

        public static Stream<Arguments> provideInvalidInput() {
            return Stream.of(
                    Arguments.of(Map.of("recipientIds", 0), "recipientIds[]", "must be greater than 0"),
                    Arguments.of(Map.of("mailinglistIds", 0), "mailinglistIds[]", "must be greater than 0"),
                    Arguments.of(Map.of("mediaTypes", "INVALID"), "mediaTypes", RestEnumAllowedValues.MEDIATYPES),
                    Arguments.of(Map.of("mediaTypes", List.of("", "EMAIL")), "mediaTypes[]", "must not be null"),
                    Arguments.of(Map.of("userStatuses", "INVALID"),
                            "userStatuses",
                            RestEnumAllowedValues.USER_STATUSES
                    ),
                    Arguments.of(
                            Map.of("date", "ABC"),
                            "date",
                            "Date must match pattern YYYY-MM-DD with optional '_' wildcards"
                    )
            );
        }

        @ParameterizedTest
        @MethodSource("provideInvalidInput")
        void shouldReturn400(
                Map<String, Object> params,
                String field,
                String message
        ) throws Exception {
            authenticated(params(get("/bindings"), params)).andExpectAll(
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
                            """.formatted(field, message)
                    )
            );
        }
    }

    @Nested
    class FindSingle {
        @Test
        void shouldReturn200() throws Exception {
            expect(adminMock.allowedAll(Permission.RECIPIENT_SHOW)).andReturn(true);
            expect(bindingsServiceMock.find(new BindingIdentifier(101, 1, MediaTypes.EMAIL), COMPANY_ID))
                    .andReturn(BINDING_RESPONSE);

            authenticated(get("/bindings/{recipientId}/{mailinglistId}/{mediaType}", 101, 1, 0))
                    .andExpectAll(
                            status().isOk(),
                            content().json(RESPONSE_JSON)
                    )
                    .andDo(documentRead());
        }

        public static Stream<Arguments> provideInvalidInput() {
            return Stream.of(
                    Arguments.of(0, 1, "EMAIL", "recipientId", "must be greater than 0"),
                    Arguments.of(1, 0, "EMAIL", "mailinglistId", "must be greater than 0"),
                    Arguments.of(1, 1, "ABC", "mediaType", RestEnumAllowedValues.MEDIATYPES)
            );
        }

        @ParameterizedTest
        @MethodSource("provideInvalidInput")
        void shouldReturn400(
                int recipientId,
                int mailinglistId,
                String mediaType,
                String field,
                String message
        ) throws Exception {
            authenticated(get(
                    "/bindings/{recipientId}/{mailinglistId}/{mediaType}",
                    recipientId, mailinglistId, mediaType
            ))
                    .andExpectAll(
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
                                    """.formatted(field, message)
                            )
                    );
        }

        @Test
        void shouldReturn403() throws Exception {
            expect(adminMock.allowedAll(Permission.RECIPIENT_SHOW)).andReturn(false);
            assertNotAuthorized(get("/bindings/1/1/EMAIL").content("{}"));
        }
    }

    @Nested
    class DeleteSingle {
        @ParameterizedTest
        @CsvSource(value = {
                "1, 1, 0",
                "1000, 1000, 0",
                "101, 1, 0",
                "101, 1, EMAIL",
//                "101, 1, SMS",
//                "101, 1, POST",
        })
        void shouldReturn204(int recipientId, int mailinglistId, String mediaType) throws Exception {
            expect(adminMock.allowedAll(Permission.RECIPIENT_CHANGE)).andReturn(true);
            bindingsServiceMock.delete(new BindingIdentifier(recipientId, mailinglistId, MediaTypes.parse(mediaType)),
                    COMPANY_ID
            );
            expectLastCall().once();

            authenticated(delete(
                    "/bindings/{recipientId}/{mailinglistId}/{mediaType}",
                    recipientId, mailinglistId, mediaType
            ))
                    .andExpect(status().isNoContent())
                    .andDo(documentDelete());
        }

        @ParameterizedTest
        @CsvSource({
                "0, 1, 0",
                "1000, 0, 2",
                "101, 1, ABC",
                "101, 1, 1",
        })
        void shouldReturn400(int recipientId, int mailinglistId, String mediaType) throws Exception {
            authenticated(delete(
                    "/bindings/{recipientId}/{mailinglistId}/{mediaType}",
                    recipientId, mailinglistId, mediaType
            ))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void shouldReturn403() throws Exception {
            expect(adminMock.allowedAll(Permission.RECIPIENT_CHANGE)).andReturn(false);
            assertNotAuthorized(delete("/bindings/101/1/EMAIL"));
        }
    }

    @Nested
    class DeleteAllForMailinglist {
        @Test
        void shouldReturn204() throws Exception {
            expect(adminMock.allowedAll(Permission.RECIPIENT_CHANGE)).andReturn(true);
            bindingsServiceMock.delete(new BindingIdentifier(101, 1, null), COMPANY_ID);
            expectLastCall().once();

            authenticated(delete("/bindings/{recipientId}/{mailinglistId}", 101, 1))
                    .andExpect(status().isNoContent());
        }
    }

    @Nested
    class DeleteBulk {
        @Test
        void shouldReturn200_whenBulkDeleteHasMixedResults() throws Exception {
            expect(adminMock.allowedAll(Permission.RECIPIENT_CHANGE)).andReturn(true);
            expect(bindingsServiceMock.delete(new BulkDeleteBindingRequest(
                            List.of(101, 102), Set.of(1, 2), Set.of(MediaTypes.EMAIL)
                    ), COMPANY_ID
            )).andReturn(new BulkOperationResponse<>(
                    2, 1, 1,
                    List.of(BulkDeleteResult.deleted(101), BulkDeleteResult.notFound(102))
            ));

            authenticated(delete("/bindings/bulk").content(
                    """
                    {
                        "recipientIds": [101, 102],
                        "mailinglistIds": [1, 2],
                        "mediaTypes": ["EMAIL"]
                    }
                    """
            ))
                    .andExpectAll(
                            status().isOk(),
                            content().json(
                                    """
                                    {
                                      "total": 2,
                                      "succeeded": 1,
                                      "failed": 1,
                                      "results": [
                                        { "id": 101, "status": "DELETED" },
                                        {
                                          "id": 102,
                                          "status": "NOT_FOUND",
                                          "errors": [{ "message": "Not found" }]
                                        }
                                      ]
                                    }
                                    """
                            )
                    )
                    .andDo(documentBulkDelete());
        }

        public static Stream<Arguments> provideInvalidInput() {
            return Stream.of(
                    Arguments.of("""
                                 {
                                     "recipientIds": [101, 102],
                                     "mailinglistIds": []
                                 }
                                 """,
                            "mailinglistIds",
                            "size must be between 1 and 1000"
                    ),
                    Arguments.of("""
                                 {
                                     "recipientIds": [101, 102]
                                 }
                                 """,
                            "mailinglistIds",
                            "must not be null"
                    ),
                    Arguments.of("""
                                 {
                                     "mailinglistIds": [1]
                                 }
                                 """,
                            "recipientIds",
                            "must not be null"
                    ),
                    Arguments.of("""
                                 {
                                     "recipientIds": [],
                                     "mailinglistIds": [1, 2]
                                 }
                                 """,
                            "recipientIds",
                            "size must be between 1 and 1000"
                    ),
                    Arguments.of("""
                                 {
                                     "recipientIds": [1, -2],
                                     "mailinglistIds": [1, 2]
                                 }
                                 """,
                            "recipientIds[1]",
                            "must be greater than 0"
                    ),
                    Arguments.of("""
                                 {
                                     "recipientIds": [1, 2],
                                     "mailinglistIds": [1, -2]
                                 }
                                 """,
                            "mailinglistIds[]",
                            "must be greater than 0"
                    ),
                    Arguments.of("""
                                 {
                                     "recipientIds": %s,
                                     "mailinglistIds": [1, 2]
                                 }
                                 """.formatted(IntStream.rangeClosed(1, 2002).boxed().toList()),
                            "recipientIds",
                            "size must be between 1 and 1000"
                    ),
                    Arguments.of("""
                                 {
                                     "recipientIds": [101, 102],
                                     "mailinglistIds": %s
                                 }
                                 """.formatted(IntStream.rangeClosed(1, 2002).boxed().toList()),
                            "mailinglistIds",
                            "size must be between 1 and 1000"
                    ),
                    Arguments.of("""
                                 {
                                     "recipientIds": [101, 102],
                                     "mailinglistIds": [1, 2],
                                     "mediaTypes": ["EMAIL", "a"]
                                 }
                                 """,
                            "mediaTypes",
                            RestEnumAllowedValues.MEDIATYPES
                    )
            );
        }

        @ParameterizedTest
        @MethodSource("provideInvalidInput")
        void shouldReturn400_onBulkDeleteWithInvalidInput(String input, String field, String message) throws Exception {
            authenticated(delete("/bindings/bulk").content(input)).andExpectAll(
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
                            """.formatted(field, message)
                    )
            );
        }

        @Test
        void shouldReturn403() throws Exception {
            expect(adminMock.allowedAll(Permission.RECIPIENT_CHANGE)).andReturn(false);
            assertNotAuthorized(delete("/bindings/bulk").content(
                    """
                    {
                        "recipientIds": [1],
                        "mailinglistIds": [1]
                    }
                    """
            ));
        }
    }

    @Nested
    class Create {
        public static Stream<Arguments> provideInvalidInput() {
            return Stream.of(
                    Arguments.of("""
                                 {
                                     "recipientId": 101,
                                     "userStatus": "Active",
                                     "mediaType": "EMAIL"
                                 }
                                 """,
                            "mailinglistId",
                            "must not be null"
                    ),
                    Arguments.of("""
                                 {
                                     "mailinglistId": 0,
                                     "recipientId": 101,
                                     "userStatus": "Active",
                                     "mediaType": "EMAIL"
                                 }
                                 """,
                            "mailinglistId",
                            "must be greater than 0"
                    ),
                    Arguments.of("""
                                 {
                                     "mailinglistId": 1,
                                     "userStatus": "Active",
                                     "mediaType": 0
                                 }
                                 """,
                            "recipientId",
                            "must not be null"
                    ),
                    Arguments.of("""
                                 {
                                     "recipientId": 0,
                                     "mailinglistId": 1,
                                     "userStatus": "Active",
                                     "mediaType": "EMAIL"
                                 }
                                 """,
                            "recipientId",
                            "must be greater than 0"
                    ),
                    Arguments.of("""
                                 {
                                     "recipientId": 101,
                                     "mailinglistId": 1,
                                     "userStatus": "Active",
                                     "mediaType": "ABC"
                                 }
                                 """,
                            "mediaType",
                            RestEnumAllowedValues.MEDIATYPES
                    ),
                    Arguments.of("""
                                 {
                                     "recipientId": 101,
                                     "mailinglistId": 1,
                                     "userStatus": "Active",
                                     "userType": "ABC",
                                     "mediaType": 2
                                 }
                                 """,
                            "userType",
                            RestEnumAllowedValues.USER_TYPES
                    ),
                    Arguments.of("""
                                 {
                                     "recipientId": 101,
                                     "mailinglistId": 1,
                                     "userStatus": "ABC",
                                     "mediaType": "EMAIL"
                                 }
                                 """,
                            "userStatus",
                            RestEnumAllowedValues.USER_STATUSES
                    ),
                    Arguments.of("""
                                 {
                                     "recipientId": 101,
                                     "mailinglistId": 1,
                                     "mediaType": "EMAIL"
                                 }
                                 """,
                            "userStatus",
                            "must not be null"
                    )
            );
        }

        @ParameterizedTest
        @MethodSource("provideInvalidInput")
        void shouldReturn400_onCreateWithInvalidInput(String input, String field, String message) throws Exception {
            authenticated(post("/bindings").content(input)).andExpectAll(
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
                            """.formatted(field, message)
                    )
            );
        }

        @Test
        void shouldReturn201() throws Exception {
            expect(adminMock.allowedAll(Permission.RECIPIENT_CHANGE)).andReturn(true);
            expect(bindingsServiceMock.create(new CreateBindingRequest(
                            1,
                            101,
                            UserStatus.Active,
                            MediaTypes.EMAIL,
                            UserType.World,
                            "RecipientImport",
                            "https:/shopsite.de/",
                            1,
                            2,
                            3,
                            true
                    ), COMPANY_ID
            )).andReturn(BINDING_RESPONSE);

            authenticated(post("/bindings").content(
                    """
                    {
                        "recipientId": 101,
                        "mailinglistId": 1,
                        "mediaType": "EMAIL",
                        "userStatus": "Active",
                        "userType": "W",
                        "userRemark": "RecipientImport",
                        "referrer": "https:/shopsite.de/",
                        "entryMailingId": 1,
                        "exitMailingId": 2,
                        "triggerId": 3,
                        "runActionAsynchronous": true
                    }
                    """
            ))
                    .andExpectAll(
                            status().isCreated(),
                            content().json(
                                    """
                                    {
                                      "mailinglistId": 1,
                                      "recipientId": 101,
                                      "userType": "W",
                                      "mediaType": "EMAIL",
                                      "userStatus": "Active",
                                      "userRemark": "RecipientImport",
                                      "referrer": "https:/shopsite.de/",
                                      "entryMailingId": 1,
                                      "exitMailingId": 2,
                                      "creationDate": "2021-10-18T19:34:30Z",
                                      "changeDate": "2026-02-03T02:00:30Z",
                                      "actionExecutionInfo": "Action started"
                                    }
                                    """
                            )
                    )
                    .andDo(documentCreate());
        }
    }

    @Nested
    class PatchSingle {
        @Test
        void shouldReturn200_onPartialUpdate() throws Exception {
            var request = new PatchBindingRequest(
                    JsonNullable.of(UserStatus.Active),
                    JsonNullable.of(UserType.World),
                    JsonNullable.of("RecipientImport"),
                    JsonNullable.of("https:/shopsite.de"),
                    JsonNullable.of(1),
                    JsonNullable.of(2)
            );
            BindingIdentifier identifier = new BindingIdentifier(
                    101,
                    1,
                    MediaTypes.EMAIL
            );
            expect(adminMock.allowedAll(Permission.RECIPIENT_CHANGE)).andReturn(true);
            expect(bindingsServiceMock.update(identifier, request, COMPANY_ID)).andReturn(BINDING_RESPONSE);

            authenticated(patch("/bindings/{recipientId}/{mailinglistId}/{mediaType}", 101, 1, "EMAIL").content(
                    """
                    {
                        "userStatus": "Active",
                        "userType": "W",
                        "userRemark": "RecipientImport",
                        "referrer": "https:/shopsite.de",
                        "entryMailingId": 1,
                        "exitMailingId": 2
                    }
                    """
            ))
                    .andExpectAll(
                            status().isOk(),
                            content().json(RESPONSE_JSON)
                    )
                    .andDo(documentPatch());
        }

        public static Stream<Arguments> provideInvalidInput() {
            return Stream.of(
                    Arguments.of("""
                                 {
                                     "userStatus": "invalid"
                                 }
                                 """,
                            "userStatus",
                            RestEnumAllowedValues.USER_STATUSES
                    ),
                    Arguments.of("""
                                 {
                                     "userStatus": null
                                 }
                                 """,
                            "userStatus",
                            "must not be null"
                    ),
                    Arguments.of("""
                                 {
                                     "userType": "invalid"
                                 }
                                 """,
                            "userType",
                            RestEnumAllowedValues.USER_TYPES
                    ),
                    Arguments.of("""
                                 {
                                     "entryMailingId": 0
                                 }
                                 """,
                            "entryMailingId",
                            "must be greater than 0"
                    ),
                    Arguments.of("""
                                 {
                                     "exitMailingId": -1
                                 }
                                 """,
                            "exitMailingId",
                            "must be greater than 0"
                    ),
                    Arguments.of("""
                                 {
                                     "exitMailingId": "abc"
                                 }
                                 """,
                            "exitMailingId",
                            "Invalid value. Expected Integer"
                    )
            );
        }

        @ParameterizedTest
        @MethodSource("provideInvalidInput")
        void shouldReturn400(String bodyJson, String expectedField, String expectedError) throws Exception {
            authenticated(
                    patch("/bindings/{recipientId}/{mailinglistId}/{mediaType}", 101, 1, "EMAIL").content(bodyJson)
            ).andExpectAll(
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
                            """.formatted(expectedField, expectedError)
                    )
            );
        }

        @Test
        void shouldReturn403() throws Exception {
            expect(adminMock.allowedAll(Permission.RECIPIENT_CHANGE)).andReturn(false);
            assertNotAuthorized(patch("/bindings/101/1/EMAIL").content("{}"));
        }
    }

    @Nested
    class BulkUpsert {
        @Test
        void shouldReturn200() throws Exception {
            expect(adminMock.allowedAll(Permission.RECIPIENT_CHANGE)).andReturn(true);
            expect(bindingsServiceMock.upsert(isA(BulkUpsertBindingRequest.class), eq(adminMock)))
                    .andReturn(new BulkUpsertBindingResponse(
                            4, 1, 1, 2,
                            Map.of(
                                    "EMAIL", Map.of(
                                            1, Map.of(
                                                    101, "inserted",
                                                    102, "updated"
                                            )
                                    )
                                    // todo fix
//                                    ,
//                                    "SMS", Map.of(
//                                            1, Map.of(
//                                                    101, "not changed",
//                                                    102, "not changed"
//                                            )
//                                    )
                            )
                    ));

            authenticated(post("/bindings/bulk").content(
                    """
                    {
                        "recipientIds": [101, 102],
                        "mailinglistIds": [1],
                        "mediaTypes": ["EMAIL"],
                        "status": "Active"
                    }
                    """
            ))
                    .andExpectAll(
                            status().isOk(),
                            content().json(
                                    """
                                    {
                                      "total": 4,
                                      "inserted": 1,
                                      "updated": 1,
                                      "notChanged": 2,
                                      "results": {
                                        "EMAIL": {
                                          "1": {
                                            "101": "inserted",
                                            "102": "updated"
                                          }
                                        }
                                      }
                                    }
                                    """
                            )
                    )
                    .andDo(documentBulkUpsert());
        }

        public static Stream<Arguments> provideInvalidInputForBulkUpsert() {
            return Stream.of(
                    Arguments.of("""
                                 {
                                     "recipientIds": [101],
                                     "mailinglistIds": [],
                                     "mediaTypes": ["EMAIL"],
                                     "status": "Active"
                                 }
                                 """,
                            "mailinglistIds",
                            "size must be between 1 and 1000"
                    ),
                    Arguments.of("""
                                 {
                                     "recipientIds": [101],
                                     "mailinglistIds": [1],
                                     "mediaTypes": ["EMAIL"],
                                     "status": "Invalid"
                                 }
                                 """,
                            "status",
                            RestEnumAllowedValues.USER_STATUSES
                    ),
                    Arguments.of("""
                                 {
                                     "recipientIds": [101],
                                     "mailinglistIds": [1],
                                     "mediaTypes": ["ABC"],
                                     "status": "Active"
                                 }
                                 """,
                            "mediaTypes",
                            RestEnumAllowedValues.MEDIATYPES
                    ),
                    Arguments.of("""
                                 {
                                     "recipientIds": [],
                                     "mailinglistIds": [1],
                                     "mediaTypes": ["EMAIL"],
                                     "status": "Active"
                                 }
                                 """,
                            "recipientIds",
                            "size must be between 1 and 1000"
                    ),
                    Arguments.of("""
                                 {
                                     "mailinglistIds": [1],
                                     "mediaTypes": ["EMAIL"],
                                     "status": "Active"
                                 }
                                 """,
                            "recipientIds",
                            "must not be null"
                    ),
                    Arguments.of("""
                                 {
                                     "recipientIds": [1],
                                     "mailinglistIds": [1],
                                     "mediaTypes": ["EMAIL"]
                                 }
                                 """,
                            "status",
                            "must not be null"
                    ),
                    Arguments.of("""
                                 {
                                     "recipientIds": %s,
                                     "mailinglistIds": [1],
                                     "mediaTypes": ["EMAIL"],
                                     "status": "Active"
                                 }
                                 """.formatted(IntStream.rangeClosed(1, 2002).boxed().toList()),
                            "recipientIds",
                            "size must be between 1 and 1000"
                    ),
                    Arguments.of("""
                                 {
                                     "recipientIds": [1],
                                     "mailinglistIds": [1, 2],
                                     "mediaTypes": null,
                                     "status": "Active"
                                 }
                                 """,
                            "mediaTypes",
                            "must not be null"
                    ),
                    Arguments.of("""
                                 {
                                     "recipientIds": [1, -2],
                                     "mailinglistIds": [1],
                                     "mediaTypes": ["EMAIL"],
                                     "status": "Active"
                                 }
                                 """,
                            "recipientIds[]",
                            "must be greater than 0"
                    ),
                    Arguments.of("""
                                 {
                                     "recipientIds": [1],
                                     "mailinglistIds": [1, -2],
                                     "mediaTypes": ["EMAIL"],
                                     "status": "Active"
                                 }
                                 """,
                            "mailinglistIds[]",
                            "must be greater than 0"
                    )
            );
        }

        @ParameterizedTest
        @MethodSource("provideInvalidInputForBulkUpsert")
        void shouldReturn400_onBulkUpsertWithInvalidInput(String input, String field, String message) throws Exception {
            authenticated(post("/bindings/bulk").content(input)).andExpectAll(
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
                            """.formatted(field, message)
                    )
            );
        }

        @Test
        void shouldReturn403() throws Exception {
            expect(adminMock.allowedAll(Permission.RECIPIENT_CHANGE)).andReturn(false);
            assertNotAuthorized(post("/bindings/bulk").content(
                    """
                    {
                        "recipientIds": [1],
                        "mailinglistIds": [1],
                        "mediaTypes": ["EMAIL"],
                        "status": "Active"
                    }
                    """
            ));
        }
    }
}
