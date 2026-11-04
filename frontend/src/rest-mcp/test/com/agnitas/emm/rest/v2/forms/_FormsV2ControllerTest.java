/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2.forms;

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
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.data.domain.PageRequest;

class _FormsV2ControllerTest extends _FormsV2ControllerTestSupport {

    private static final FormResponse FORM_RESPONSE = new FormResponse(
            42,
            "Registration_Form",
            "Public registration form",
            100,
            200,
            new FormResultSettingsResponse("Thank you template", "https://example.com/success", true),
            new FormResultSettingsResponse("Error template", "https://example.com/error", false),
            false,
            Instant.parse("2024-01-15T10:30:00Z"),
            Instant.parse("2024-06-20T14:45:00Z")
    );
    private static final String RESPONSE_JSON =
            """
            {
                "id": 42,
                "name": "Registration_Form",
                "description": "Public registration form",
                "startTriggerId": 100,
                "endTriggerId": 200,
                "successSettings": {
                    "template": "Thank you template",
                    "url": "https://example.com/success",
                    "useUrl": true
                },
                "errorSettings": {
                    "template": "Error template",
                    "url": "https://example.com/error",
                    "useUrl": false
                },
                "active": false,
                "creationDate": "2024-01-15T10:30:00Z",
                "changeDate": "2024-06-20T14:45:00Z"
            }
            """;

    @MockBean
    private FormRestService formRestServiceMock;

    @Nested
    class PaginatedList {
        @Test
        void shouldReturn200() throws Exception {
            expect(adminMock.allowedAll(Permission.FORMS_SHOW)).andReturn(true);
            expect(formRestServiceMock.list(PageRequest.of(3, 2), COMPANY_ID)).andReturn(new PageResponse<>(
                    List.of(
                            new FormLightResponse(42, "Registration Form", "Public registration form"),
                            new FormLightResponse(43, "Contact Form", null)
                    ), new PageResponse.PageMeta(3, 2, 12)
            ));

            Map<String, Object> params = Map.of(
                    "page", 3,
                    "size", 2
            );
            authenticated(params(get("/forms"), params)).andExpectAll(
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
                                        "id": 42,
                                        "name": "Registration Form",
                                        "description": "Public registration form"
                                    },
                                    {
                                        "id": 43,
                                        "name": "Contact Form"
                                    }
                                ]
                            }
                            """
                    )
            ).andDo(documentList());
        }

        @Test
        void shouldReturn403() throws Exception {
            expect(adminMock.allowedAll(Permission.FORMS_SHOW)).andReturn(false);
            assertNotAuthorized(get("/forms"));
        }
    }

    @Nested
    class GetForm {
        @Test
        void shouldReturn200() throws Exception {
            expect(adminMock.allowedAll(Permission.FORMS_SHOW)).andReturn(true);
            expect(formRestServiceMock.find(42, COMPANY_ID)).andReturn(FORM_RESPONSE);

            authenticated(get("/forms/{id}", 42)).andExpectAll(
                    status().isOk(),
                    content().json(RESPONSE_JSON)
            ).andDo(documentRead());
        }

        @Test
        void shouldReturn400_whenIdIsZero() throws Exception {
            authenticated(get("/forms/{id}", 0)).andExpectAll(
                    status().isBadRequest(),
                    content().json(
                            """
                            {
                                "status": 400,
                                "detail": "Invalid input.",
                                "errors": [
                                    {
                                        "field": "formId",
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
            expect(adminMock.allowedAll(Permission.FORMS_SHOW)).andReturn(false);
            assertNotAuthorized(get("/forms/{id}", 1));
        }
    }

    @Nested
    class CreateForm {
        @Test
        void shouldReturn201() throws Exception {
            expect(adminMock.allowedAll(Permission.FORMS_CHANGE)).andReturn(true);

            CreateFormRequest request = new CreateFormRequest(
                    "Registration_Form",
                    "Public registration form",
                    100,
                    200,
                    new FormResultSettingsRequest("Thank you template", "https://example.com/success", true),
                    new FormResultSettingsRequest("Error template", "https://example.com/error", false)
            );
            expect(formRestServiceMock.create(request, adminMock)).andReturn(FORM_RESPONSE);

            authenticated(post("/forms").content(
                    """
                    {
                        "name": "Registration_Form",
                        "description": "Public registration form",
                        "startTriggerId": 100,
                        "endTriggerId": 200,
                        "successSettings": {
                            "template": "Thank you template",
                            "url": "https://example.com/success",
                            "useUrl": true
                        },
                        "errorSettings": {
                            "template": "Error template",
                            "url": "https://example.com/error",
                            "useUrl": false
                        }
                    }
                    """
            )).andExpectAll(
                    status().isCreated(),
                    content().json(RESPONSE_JSON)
            ).andDo(documentCreate());
        }

        @Test
        void shouldReturn400_whenNameIsMissing() throws Exception {
            authenticated(post("/forms").content(
                    """
                    {
                        "description": "Missing name",
                        "successSettings": {
                            "template": "Template"
                        },
                        "errorSettings": {
                            "template": "Template"
                        }
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
                                  "field": "name",
                                  "message": "must not be null"
                                }
                              ]
                            }
                            """
                    )
            );
        }

        @Test
        void shouldReturn400_whenNameIsTooLong() throws Exception {
            authenticated(post("/forms").content(
                    """
                    {
                        "name": "%s",
                        "description": "Name too long",
                        "successSettings": {
                            "template": "Template"
                        },
                        "errorSettings": {
                            "template": "Template"
                        }
                    }
                    """
                            .formatted("a".repeat(51)))
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
                                  "message": "size must be between 1 and 50"
                                }
                              ]
                            }
                            """
                    )
            );
        }

        @Test
        void shouldReturn400_whenNameIsEmpty() throws Exception {
            authenticated(post("/forms").content(
                    """
                    {
                        "name": " ",
                        "successSettings": {
                            "template": "Template"
                        },
                        "errorSettings": {
                            "template": "Template"
                        }
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
                                  "field": "name",
                                  "message": "Must contain only letters, numbers, hyphens (-), and underscores (_)"
                                }
                              ]
                            }
                            """
                    )
            );
        }

        @Test
        void shouldReturn400_whenNameContainsInvalidCharacters() throws Exception {
            authenticated(post("/forms").content(
                    """
                    {
                        "name": "Form With Spaces",
                        "description": "Invalid characters in name",
                        "successSettings": {
                            "template": "Template"
                        },
                        "errorSettings": {
                            "template": "Template"
                        }
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
                                  "field": "name",
                                  "message": "Must contain only letters, numbers, hyphens (-), and underscores (_)"
                                }
                              ]
                            }
                            """
                    )
            );
        }

        @Test
        void shouldReturn400_whenStartTriggerIdIsZero() throws Exception {
            authenticated(post("/forms").content(
                    """
                    {
                        "name": "ValidName",
                        "description": "Test",
                        "startTriggerId": 0,
                        "endTriggerId": 200,
                        "successSettings": {
                            "template": "Template"
                        },
                        "errorSettings": {
                            "template": "Template"
                        }
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
                                  "field": "startTriggerId",
                                  "message": "must be greater than 0"
                                }
                              ]
                            }
                            """
                    )
            );
        }

        @Test
        void shouldReturn400_whenEndTriggerIdIsNegative() throws Exception {
            authenticated(post("/forms").content(
                    """
                    {
                        "name": "ValidName",
                        "description": "Test",
                        "startTriggerId": 100,
                        "endTriggerId": -1,
                         "successSettings": {
                            "template": "Template"
                        },
                        "errorSettings": {
                            "template": "Template"
                        }
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
                                  "field": "endTriggerId",
                                  "message": "must be greater than 0"
                                }
                              ]
                            }
                            """
                    )
            );
        }

        @Test
        void shouldReturn400_whenSuccessSettingsIsNull() throws Exception {
            authenticated(post("/forms").content(
                    """
                    {
                        "name": "ValidName",
                        "description": "Test",
                        "startTriggerId": 100,
                        "endTriggerId": 200,
                        "successSettings": null,
                        "errorSettings": {
                            "template": "Template"
                        }
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
                                  "field": "successSettings",
                                  "message": "must not be null"
                                }
                              ]
                            }
                            """
                    )
            );
        }

        @Test
        void shouldReturn400_whenErrorSettingsIsNull() throws Exception {
            authenticated(post("/forms").content(
                    """
                    {
                        "name": "ValidName",
                        "description": "Test",
                        "startTriggerId": 100,
                        "endTriggerId": 200,
                        "successSettings": {
                            "template": "Template"
                        },
                        "errorSettings": null
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
                                  "field": "errorSettings",
                                  "message": "must not be null"
                                }
                              ]
                            }
                            """
                    )
            );
        }

        @Test
        void shouldReturn400_whenUseUrlIsTrueButUrlIsInvalid() throws Exception {
            authenticated(post("/forms").content(
                    """
                    {
                        "name": "ValidName",
                        "description": "Test",
                        "startTriggerId": 100,
                        "endTriggerId": 200,
                        "successSettings": {
                            "url": "not a valid url",
                            "useUrl": true
                        },
                        "errorSettings": {
                            "template": "Template",
                            "useUrl": false
                        }
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
                                  "field": "successSettings.validUrl",
                                  "message": "url must be a valid URL"
                                }
                              ]
                            }
                            """
                    )
            );
        }

        @Test
        void shouldReturn400_whenUseUrlIsFalseAndTemplateIsBlank() throws Exception {
            authenticated(post("/forms").content(
                    """
                    {
                        "name": "ValidName",
                        "description": "Test",
                        "startTriggerId": 100,
                        "endTriggerId": 200,
                        "successSettings": {
                            "template": "",
                            "useUrl": false
                        },
                        "errorSettings": {
                            "template": "Template",
                            "useUrl": false
                        }
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
                                  "field": "successSettings.validTemplate",
                                  "message": "template must not be blank"
                                }
                              ]
                            }
                            """
                    )
            );
        }

        @Test
        void shouldReturn403() throws Exception {
            expect(adminMock.allowedAll(Permission.FORMS_CHANGE)).andReturn(false);
            assertNotAuthorized(post("/forms").content(
                    """
                    {
                        "name": "form",
                        "description": "test description",
                        "successSettings": {
                            "template": "Template"
                        },
                        "errorSettings": {
                            "template": "Template"
                        }
                    }
                    """
            ));
        }
    }

    @Nested
    class PatchForm {
        @Test
        void shouldReturn200() throws Exception {
            expect(adminMock.allowedAll(Permission.FORMS_CHANGE)).andReturn(true);
            PatchFormRequest request = new PatchFormRequest(
                    JsonNullable.of("Registration_Form"),
                    JsonNullable.of("Public registration form"),
                    JsonNullable.of(100),
                    JsonNullable.of(200),
                    JsonNullable.of(new FormResultSettingsRequest(
                            "Thank you template",
                            "https://example.com/success",
                            true
                    )),
                    JsonNullable.of(new FormResultSettingsRequest("Error template", "https://example.com/error", false))
            );
            expect(formRestServiceMock.patch(42, request, adminMock)).andReturn(FORM_RESPONSE);

            authenticated(patch("/forms/{id}", 42).content(
                    """
                    {
                        "name": "Registration_Form",
                        "description": "Public registration form",
                        "startTriggerId": 100,
                        "endTriggerId": 200,
                        "successSettings": {
                            "template": "Thank you template",
                            "url": "https://example.com/success",
                            "useUrl": true
                        },
                        "errorSettings": {
                            "template": "Error template",
                            "url": "https://example.com/error",
                            "useUrl": false
                        }
                    }
                    """
            ))
                    .andExpectAll(status().isOk(), content().json(RESPONSE_JSON))
                    .andDo(documentPatch());
        }

        @Test
        void shouldReturn400_whenIdIsZero() throws Exception {
            authenticated(patch("/forms/{id}", 0).content(
                    """
                    {
                        "name": "form",
                        "successSettings": {
                           "template": "Template"
                        },
                        "errorSettings": {
                            "template": "Template"
                        }
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
                                        "field": "formId",
                                        "message": "must be greater than 0"
                                    }
                                ]
                            }
                            """
                    )
            );
        }

        @Test
        void shouldReturn400_whenNameIsTooLong() throws Exception {
            authenticated(patch("/forms/{id}", 42).content(
                    """
                    {
                        "name": "%s",
                        "successSettings": {
                            "template": "Template"
                        },
                        "errorSettings": {
                            "template": "Template"
                        }
                    }
                    """
                            .formatted("a".repeat(51)))
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
                                  "message": "size must be between 1 and 50"
                                }
                              ]
                            }
                            """
                    )
            );
        }

        @Test
        void shouldReturn400_whenNameIsEmpty() throws Exception {
            authenticated(patch("/forms/{id}", 42).content(
                    """
                    {
                        "name": " ",
                        "successSettings": {
                            "template": "Template"
                        },
                        "errorSettings": {
                            "template": "Template"
                        }
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
                                  "field": "name",
                                  "message": "Must contain only letters, numbers, hyphens (-), and underscores (_)"
                                }
                              ]
                            }
                            """
                    )
            );
        }

        @Test
        void shouldReturn400_whenSuccessSettingsIsNull() throws Exception {
            authenticated(patch("/forms/{id}", 42).content(
                    """
                    {
                        "name": "ValidName",
                        "successSettings": null,
                        "errorSettings": {
                            "template": "Template"
                        }
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
                                  "field": "successSettings",
                                  "message": "must not be null"
                                }
                              ]
                            }
                            """
                    )
            );
        }

        @Test
        void shouldReturn400_whenErrorSettingsIsNull() throws Exception {
            authenticated(patch("/forms/{id}", 42).content(
                    """
                    {
                        "name": "ValidName",
                        "successSettings": {
                            "template": "Template"
                        },
                        "errorSettings": null
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
                                  "field": "errorSettings",
                                  "message": "must not be null"
                                }
                              ]
                            }
                            """
                    )
            );
        }

        @Test
        void shouldReturn400_whenUseUrlIsTrueButUrlIsInvalid() throws Exception {
            authenticated(patch("/forms/{id}", 42).content(
                    """
                    {
                        "name": "ValidName",
                        "successSettings": {
                            "template": "Template",
                            "url": "not a valid url",
                            "useUrl": true
                        },
                        "errorSettings": {
                            "template": "Template",
                            "useUrl": false
                        }
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
                                  "field": "successSettings.validUrl",
                                  "message": "url must be a valid URL"
                                }
                              ]
                            }
                            """
                    )
            );
        }

        @Test
        void shouldReturn400_whenUseUrlIsFalseAndTemplateIsBlank() throws Exception {
            authenticated(patch("/forms/{id}", 42).content(
                    """
                    {
                        "name": "ValidName",
                        "successSettings": {
                            "template": "",
                            "useUrl": false
                        },
                        "errorSettings": {
                            "template": "Template",
                            "useUrl": false
                        }
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
                                  "field": "successSettings.validTemplate",
                                  "message": "template must not be blank"
                                }
                              ]
                            }
                            """
                    )
            );
        }

        @Test
        void shouldReturn400_whenStartTriggerIdIsZero() throws Exception {
            authenticated(patch("/forms/{id}", 42).content(
                    """
                    {
                        "name": "ValidName",
                        "startTriggerId": 0,
                        "endTriggerId": 200,
                        "successSettings": {
                            "template": "Template"
                        },
                        "errorSettings": {
                            "template": "Template"
                        }
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
                                  "field": "startTriggerId",
                                  "message": "must be greater than 0"
                                }
                              ]
                            }
                            """
                    )
            );
        }

        @Test
        void shouldReturn400_whenEndTriggerIdIsNegative() throws Exception {
            authenticated(patch("/forms/{id}", 42).content(
                    """
                    {
                        "name": "ValidName",
                        "startTriggerId": 100,
                        "endTriggerId": -1,
                        "successSettings": {
                            "template": "Template"
                        },
                        "errorSettings": {
                            "template": "Template"
                        }
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
                                  "field": "endTriggerId",
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
            expect(adminMock.allowedAll(Permission.FORMS_CHANGE)).andReturn(false);
            assertNotAuthorized(patch("/forms/{id}", 1).content("{}"));
        }
    }

    @Nested
    class DeleteForm {
        @Test
        void shouldReturn204() throws Exception {
            expect(adminMock.allowedAll(Permission.FORMS_DELETE)).andReturn(true);
            formRestServiceMock.delete(42, COMPANY_ID);
            expectLastCall().once();

            authenticated(delete("/forms/{id}", 42))
                    .andExpect(status().isNoContent())
                    .andDo(documentDelete());
        }

        @Test
        void shouldReturn400_whenIdIsZero() throws Exception {
            authenticated(delete("/forms/{id}", 0)).andExpectAll(
                    status().isBadRequest(),
                    content().json(
                            """
                            {
                                "status": 400,
                                "detail": "Invalid input.",
                                "errors": [
                                    {
                                        "field": "formId",
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
            expect(adminMock.allowedAll(Permission.FORMS_DELETE)).andReturn(false);
            assertNotAuthorized(delete("/forms/{id}", 1));
        }
    }

    @Nested
    class ActivateForm {
        @Test
        void shouldReturn200() throws Exception {
            expect(adminMock.allowedAll(Permission.FORMS_CHANGE)).andReturn(true);
            expect(formRestServiceMock.activate(42, adminMock)).andReturn(new FormResponse(
                    42,
                    "Registration_Form",
                    "Public registration form",
                    100,
                    200,
                    new FormResultSettingsResponse("Thank you template", "https://example.com/success", true),
                    new FormResultSettingsResponse("Error template", "https://example.com/error", false),
                    true,
                    Instant.parse("2024-01-15T10:30:00Z"),
                    Instant.parse("2024-06-20T14:45:00Z")
            ));
            authenticated(post("/forms/{id}/activate", 42)).andExpectAll(
                    status().isOk(),
                    content().json(
                            """
                            {
                                "name": "Registration_Form",
                                "description": "Public registration form",
                                "startTriggerId": 100,
                                "endTriggerId": 200,
                                "active": true,
                                "successSettings": {
                                    "template": "Thank you template",
                                    "url": "https://example.com/success",
                                    "useUrl": true
                                },
                                "errorSettings": {
                                    "template": "Error template",
                                    "url": "https://example.com/error",
                                    "useUrl": false
                                }
                            }
                            """
                    )
            ).andDo(documentActivate());
        }

        @Test
        void shouldReturn400_whenIdIsZero() throws Exception {
            authenticated(post("/forms/{id}/activate", 0)).andExpectAll(
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
            expect(adminMock.allowedAll(Permission.FORMS_CHANGE)).andReturn(false);
            assertNotAuthorized(post("/forms/{id}/activate", 1));
        }
    }

    @Nested
    class DeactivateForm {
        @Test
        void shouldReturn200() throws Exception {
            expect(adminMock.allowedAll(Permission.FORMS_CHANGE)).andReturn(true);
            expect(formRestServiceMock.deactivate(42, adminMock)).andReturn(new FormResponse(
                    42,
                    "Registration_Form",
                    "Public registration form",
                    100,
                    200,
                    new FormResultSettingsResponse("Thank you template", "https://example.com/success", true),
                    new FormResultSettingsResponse("Error template", "https://example.com/error", false),
                    false,
                    Instant.parse("2024-01-15T10:30:00Z"),
                    Instant.parse("2024-06-20T14:45:00Z")
            ));
            authenticated(post("/forms/{id}/deactivate", 42)).andExpectAll(
                    status().isOk(),
                    content().json(
                            """
                            {
                                "name": "Registration_Form",
                                "description": "Public registration form",
                                "startTriggerId": 100,
                                "endTriggerId": 200,
                                "active": false,
                                "successSettings": {
                                    "template": "Thank you template",
                                    "url": "https://example.com/success",
                                    "useUrl": true
                                },
                                "errorSettings": {
                                    "template": "Error template",
                                    "url": "https://example.com/error",
                                    "useUrl": false
                                }
                            }
                            """
                    )
            ).andDo(documentDeactivate());
        }

        @Test
        void shouldReturn400_whenIdIsZero() throws Exception {
            authenticated(post("/forms/{id}/deactivate", 0)).andExpectAll(
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
            expect(adminMock.allowedAll(Permission.FORMS_CHANGE)).andReturn(false);
            assertNotAuthorized(post("/forms/{id}/deactivate", 1));
        }
    }
}
