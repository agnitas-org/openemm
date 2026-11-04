/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2.user;

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
import java.util.Set;

import com.agnitas.emm.core.Permission;
import com.agnitas.emm.rest.v2.infrastructure.search.PageResponse;
import com.agnitas.testsupport.annotation.MockBean;
import com.agnitas.util.importvalues.Gender;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.data.domain.PageRequest;

class _UiUsersV2ControllerTest extends _UiUsersV2ControllerTestSupport {

    private static final UserGroupResponse GROUP = new UserGroupResponse(1, "Editors", "User group for editors");
    private static final UserPermissionResponse PERMISSION = new UserPermissionResponse("forms.show", "Show Forms");
    private static final UiUserResponse USER_RESPONSE = new UiUserResponse(
            42,
            "emma",
            1,
            "My Corp",
            "Mr",
            "Dr",
            "Emma",
            "Clark",
            "emma@example.com",
            "+1234567890",
            "stats@example.com",
            "en_US",
            "America/New_York",
            Instant.parse("2024-01-15T10:30:00Z"),
            Instant.parse("2024-06-01T08:00:00Z"),
            Instant.parse("2024-07-20T12:00:00Z"),
            Instant.parse("2024-03-10T09:00:00Z"),
            Instant.parse("2024-07-25T14:30:00Z"),
            List.of(GROUP),
            List.of(PERMISSION),
            Set.of(100, 200),
            Set.of(300, 400)
    );

    private static final String RESPONSE_JSON =
            """
            {
                "id": 42,
                "username": "emma",
                "clientId": 1,
                "company": "My Corp",
                "gender": "Mr",
                "title": "Dr",
                "firstname": "Emma",
                "lastname": "Clark",
                "email": "emma@example.com",
                "phone": "+1234567890",
                "statisticsEmail": "stats@example.com",
                "language": "en_US",
                "timezone": "America/New_York",
                "creationDate": "2024-01-15T10:30:00Z",
                "lastPasswordChange": "2024-06-01T08:00:00Z",
                "lastLoginDate": "2024-07-20T12:00:00Z",
                "lastNewsDate": "2024-03-10T09:00:00Z",
                "lastMessageDate": "2024-07-25T14:30:00Z",
                "groups": [
                    {
                        "id": 1,
                        "name": "Editors",
                        "description": "User group for editors"
                    }
                ],
                "permissions": [
                    {
                        "code": "forms.show",
                        "name": "Show Forms"
                    }
                ],
                "accessLimitingTargetGroupIds": [100, 200],
                "disabledMailingListIds": [300, 400]
            }
            """;

    private static final String CREATE_REQUEST_JSON =
            """
            {
                "clientId": 1,
                "username": "emma",
                "password": "securePass123",
                "company": "My Corp",
                "gender": "MALE",
                "title": "Dr.",
                "firstName": "Emma",
                "lastName": "Clark",
                "email": "emma@example.com",
                "statisticsEmail": "stats@example.com",
                "phone": "+1234567890",
                "language": "en_US",
                "timezone": "America/New_York",
                "employeeId": 42,
                "groupIds": [1, 2],
                "permissions": ["forms.show", "forms.edit"],
                "accessLimitingTargetGroupIds": [100, 200],
                "disabledMailingListIds": [300, 400]
            }
            """;

    private static final String PATCH_REQUEST_JSON =
            """
            {
                "username": "emma_updated",
                "password": "newSecurePass123",
                "company": "My Corp",
                "gender": "MALE",
                "title": "Dr.",
                "firstName": "Emma",
                "lastName": "Clark",
                "email": "emma.new@example.com",
                "statisticsEmail": "stats@example.com",
                "phone": "+1234567890",
                "language": "en_US",
                "timezone": "America/New_York",
                "employeeId": 42,
                "groupIds": [1, 2],
                "permissions": ["forms.show", "forms.edit"],
                "accessLimitingTargetGroupIds": [100, 200],
                "disabledMailingListIds": [300, 400]
            }
            """;

    private static final CreateUiUserRequest CREATE_REQUEST = new CreateUiUserRequest(
            1,
            "emma",
            "securePass123",
            "My Corp",
            Gender.MALE,
            "Dr.",
            "Emma",
            "Clark",
            "emma@example.com",
            "stats@example.com",
            "+1234567890",
            "en_US",
            "America/New_York",
            42,
            Set.of(1, 2),
            Set.of("forms.show", "forms.edit"),
            Set.of(100, 200),
            null,
            Set.of(300, 400)
    );

    private static final PatchUiUserRequest PATCH_REQUEST = new PatchUiUserRequest(
            JsonNullable.of("emma_updated"),
            JsonNullable.of("newSecurePass123"),
            JsonNullable.of("My Corp"),
            JsonNullable.of(Gender.MALE),
            JsonNullable.of("Dr."),
            JsonNullable.of("Emma"),
            JsonNullable.of("Clark"),
            JsonNullable.of("emma.new@example.com"),
            JsonNullable.of("stats@example.com"),
            JsonNullable.of("+1234567890"),
            JsonNullable.of("en_US"),
            JsonNullable.of("America/New_York"),
            JsonNullable.of("42"),
            JsonNullable.of(Set.of(1, 2)),
            JsonNullable.of(Set.of("forms.show", "forms.edit")),
            JsonNullable.of(Set.of(100, 200)),
            JsonNullable.undefined(),
            JsonNullable.of(Set.of(300, 400))
    );

    @MockBean
    private UiUserRestService usersRestServiceMock;

    @Nested
    class PaginatedList {
        @Test
        void shouldReturn200() throws Exception {
            expect(adminMock.allowedAll(Permission.ADMIN_SHOW)).andReturn(true);
            expect(usersRestServiceMock.list(PageRequest.of(0, 10), adminMock)).andReturn(new PageResponse<>(
                    List.of(
                            new UiUserLightResponse(42, 2, "emma", "Emma", "Clark", "emma@example.com"),
                            new UiUserLightResponse(43, 3, "jane.smith", "Jane", "Smith", "jane.smith@example.com")
                    ), new PageResponse.PageMeta(0, 10, 25)
            ));

            Map<String, Object> params = Map.of(
                    "page", 0,
                    "size", 10
            );
            authenticated(params(get("/users"), params)).andExpectAll(
                    status().isOk(),
                    content().json(
                            """
                            {
                                "page": {
                                    "number": 0,
                                    "size": 10,
                                    "totalCount": 25
                                },
                                "list": [
                                    {
                                        "id": 42,
                                        "clientId": 2,
                                        "username": "emma",
                                        "firstname": "Emma",
                                        "lastname": "Clark",
                                        "email": "emma@example.com"
                                    },
                                    {
                                        "id": 43,
                                        "clientId": 3,
                                        "username": "jane.smith",
                                        "firstname": "Jane",
                                        "lastname": "Smith",
                                        "email": "jane.smith@example.com"
                                    }
                                ]
                            }
                            """
                    )
            ).andDo(documentList());
        }

        @Test
        void shouldReturn403() throws Exception {
            expect(adminMock.allowedAll(Permission.ADMIN_SHOW)).andReturn(false);
            assertNotAuthorized(get("/users"));
        }
    }

    @Nested
    class GetUserById {
        @Test
        void shouldReturn200() throws Exception {
            expect(adminMock.allowedAll(Permission.ADMIN_SHOW)).andReturn(true);
            expect(usersRestServiceMock.find(42, COMPANY_ID)).andReturn(USER_RESPONSE);

            authenticated(get("/users/{id}", 42)).andExpectAll(
                    status().isOk(),
                    content().json(RESPONSE_JSON)
            ).andDo(documentReadById());
        }

        @Test
        void shouldReturn400_whenIdIsZero() throws Exception {
            authenticated(get("/users/{id}", 0)).andExpectAll(
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
            expect(adminMock.allowedAll(Permission.ADMIN_SHOW)).andReturn(false);
            assertNotAuthorized(get("/users/{id}", 42));
        }
    }

    @Nested
    class GetUserByUsername {
        @Test
        void shouldReturn200() throws Exception {
            expect(adminMock.allowedAll(Permission.ADMIN_SHOW)).andReturn(true);
            expect(usersRestServiceMock.find("emma", COMPANY_ID)).andReturn(USER_RESPONSE);

            authenticated(get("/users/by-name/{username}", "emma")).andExpectAll(
                    status().isOk(),
                    content().json(RESPONSE_JSON)
            ).andDo(documentReadByUsername());
        }

        @Test
        void shouldReturn400_whenUsernameIsBlank() throws Exception {
            authenticated(get("/users/by-name/ ")).andExpectAll(
                    status().isBadRequest(),
                    content().json(
                            """
                            {
                                "status": 400,
                                "detail": "Invalid input.",
                                "errors": [
                                    {
                                        "field": "username",
                                        "message": "must not be blank"
                                    }
                                ]
                            }
                            """
                    )
            );
        }

        @Test
        void shouldReturn403() throws Exception {
            expect(adminMock.allowedAll(Permission.ADMIN_SHOW)).andReturn(false);
            assertNotAuthorized(get("/users/by-name/{username}", "emma"));
        }
    }

    @Nested
    class DeleteUserById {
        @Test
        void shouldReturn204() throws Exception {
            expect(adminMock.allowedAll(Permission.ADMIN_DELETE)).andReturn(true);
            usersRestServiceMock.delete(42, COMPANY_ID);
            expectLastCall().once();

            authenticated(delete("/users/{id}", 42))
                    .andExpect(status().isNoContent())
                    .andDo(documentDeleteById());
        }

        @Test
        void shouldReturn400_whenIdIsZero() throws Exception {
            authenticated(delete("/users/{id}", 0)).andExpectAll(
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
            expect(adminMock.allowedAll(Permission.ADMIN_DELETE)).andReturn(false);
            assertNotAuthorized(delete("/users/{id}", 42));
        }
    }

    @Nested
    class DeleteUserByUsername {
        @Test
        void shouldReturn204() throws Exception {
            expect(adminMock.allowedAll(Permission.ADMIN_DELETE)).andReturn(true);
            usersRestServiceMock.delete("emma", COMPANY_ID);
            expectLastCall().once();

            authenticated(delete("/users/by-name/{username}", "emma"))
                    .andExpect(status().isNoContent())
                    .andDo(documentDeleteByUsername());
        }

        @Test
        void shouldReturn400_whenUsernameIsBlank() throws Exception {
            authenticated(delete("/users/by-name/ ")).andExpectAll(
                    status().isBadRequest(),
                    content().json(
                            """
                            {
                                "status": 400,
                                "detail": "Invalid input.",
                                "errors": [
                                    {
                                        "field": "username",
                                        "message": "must not be blank"
                                    }
                                ]
                            }
                            """
                    )
            );
        }

        @Test
        void shouldReturn403() throws Exception {
            expect(adminMock.allowedAll(Permission.ADMIN_DELETE)).andReturn(false);
            assertNotAuthorized(delete("/users/by-name/{username}", "emma"));
        }
    }

    @Nested
    class PatchUserById {
        @Test
        void shouldReturn200() throws Exception {
            expect(adminMock.allowedAll(Permission.ADMIN_CHANGE)).andReturn(true);
            expect(usersRestServiceMock.patch(42, PATCH_REQUEST, adminMock)).andReturn(USER_RESPONSE);

            authenticated(patch("/users/{id}", 42).content(PATCH_REQUEST_JSON))
                    .andExpectAll(status().isOk(), content().json(RESPONSE_JSON))
                    .andDo(documentPatchById());
        }

        @Test
        void shouldReturn400_whenIdIsZero() throws Exception {
            authenticated(patch("/users/{id}", 0).content(PATCH_REQUEST_JSON))
                    .andExpectAll(
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
    }

    @Nested
    class PatchUserByUsername {
        @Test
        void shouldReturn200() throws Exception {
            expect(adminMock.allowedAll(Permission.ADMIN_CHANGE)).andReturn(true);
            expect(usersRestServiceMock.patch("emma", PATCH_REQUEST, adminMock)).andReturn(USER_RESPONSE);

            authenticated(patch("/users/by-name/{username}", "emma").content(PATCH_REQUEST_JSON))
                    .andExpectAll(status().isOk(), content().json(RESPONSE_JSON))
                    .andDo(documentPatchByUsername());
        }

        @Test
        void shouldReturn400_whenUsernameIsBlank() throws Exception {
            authenticated(patch("/users/by-name/ ").content(PATCH_REQUEST_JSON))
                    .andExpectAll(
                            status().isBadRequest(),
                            content().json(
                                    """
                                    {
                                        "status": 400,
                                        "detail": "Invalid input.",
                                        "errors": [
                                            {
                                                "field": "username",
                                                "message": "must not be blank"
                                            }
                                        ]
                                    }
                                    """
                            )
                    );
        }

        @Test
        void shouldReturn403() throws Exception {
            expect(adminMock.allowedAll(Permission.ADMIN_CHANGE)).andReturn(false);
            assertNotAuthorized(patch("/users/by-name/{username}", "emma").content(PATCH_REQUEST_JSON));
        }
    }

    @Nested
    class CreateUser {
        @Test
        void shouldReturn201() throws Exception {
            expect(adminMock.allowedAll(Permission.ADMIN_NEW)).andReturn(true);
            expect(usersRestServiceMock.create(CREATE_REQUEST, adminMock)).andReturn(USER_RESPONSE);

            authenticated(post("/users").content(CREATE_REQUEST_JSON))
                    .andExpectAll(status().isCreated(), content().json(RESPONSE_JSON))
                    .andDo(documentCreate());
        }

        @Test
        void shouldReturn400_whenBodyIsMissing() throws Exception {
            authenticated(post("/users").content("{}"))
                    .andExpectAll(
                            status().isBadRequest(),
                            content().json(
                                    """
                                    {
                                        "status": 400,
                                        "detail": "Invalid input.",
                                        "errors": [
                                            {
                                                "field": "username",
                                                "message": "must not be blank"
                                            },
                                            {
                                                "field": "password",
                                                "message": "must not be blank"
                                            },
                                            {
                                                "field": "company",
                                                "message": "must not be blank"
                                            },
                                            {
                                                "field": "firstName",
                                                "message": "must not be blank"
                                            },
                                            {
                                                "field": "lastName",
                                                "message": "must not be blank"
                                            },
                                            {
                                                "field": "email",
                                                "message": "must not be blank"
                                            },
                                            {
                                                "field": "language",
                                                "message": "must not be blank"
                                            },
                                            {
                                                "field": "groupIds",
                                                "message": "must not be empty"
                                            }
                                        ]
                                    }
                                    """
                            )
                    );
        }

        @Test
        void shouldReturn400_whenEmailIsInvalid() throws Exception {
            authenticated(post("/users").content(
                    """
                    {
                        "username": "emma",
                        "password": "pass",
                        "company": "My Corp",
                        "firstName": "Emma",
                        "lastName": "Clark",
                        "email": "emma example.com",
                        "language": "en_US",
                        "groupIds": [1]
                    }
                    """))
                    .andExpectAll(
                            status().isBadRequest(),
                            content().json(
                                    """
                                    {
                                        "status": 400,
                                        "detail": "Invalid input.",
                                        "errors": [
                                            {
                                                "field": "email",
                                                "message": "must be a well-formed email address"
                                            }
                                        ]
                                    }
                                    """
                            )
                    );
        }

        @Test
        void shouldReturn400_whenUsernameIsTooShort() throws Exception {
            authenticated(post("/users").content(
                    """
                    {
                        "username": "ab",
                        "password": "pass",
                        "company": "My Corp",
                        "firstName": "Emma",
                        "lastName": "Clark",
                        "email": "emma@example.com",
                        "language": "en_US",
                        "groupIds": [1]
                    }
                    """))
                    .andExpectAll(
                            status().isBadRequest(),
                            content().json(
                                    """
                                    {
                                        "status": 400,
                                        "detail": "Invalid input.",
                                        "errors": [
                                            {
                                                "field": "username",
                                                "message": "size must be between 3 and 180"
                                            }
                                        ]
                                    }
                                    """
                            )
                    );
        }

        @Test
        void shouldReturn400_whenBothAccessLimitingFieldsAreSet() throws Exception {
            authenticated(post("/users").content(
                    """
                    {
                        "username": "emma",
                        "password": "pass",
                        "company": "My Corp",
                        "firstName": "Emma",
                        "lastName": "Clark",
                        "email": "emma@example.com",
                        "language": "en_US",
                        "groupIds": [1],
                        "accessLimitingTargetGroupIds": [100, 200],
                        "accessLimitingTargetGroupId": 50
                    }
                    """))
                    .andExpectAll(
                            status().isBadRequest(),
                            content().json(
                                    """
                                    {
                                        "status": 400,
                                        "detail": "Invalid input.",
                                        "errors": [
                                            {
                                                "field": "accessLimitingTargetGroup",
                                                "message": "Specify either accessLimitingTargetGroupIds or accessLimitingTargetGroupId, but not both"
                                            }
                                        ]
                                    }
                                    """
                            )
                    );
        }

        @Test
        void shouldReturn403() throws Exception {
            expect(adminMock.allowedAll(Permission.ADMIN_NEW)).andReturn(false);
            assertNotAuthorized(post("/users").content(CREATE_REQUEST_JSON));
        }
    }
}
