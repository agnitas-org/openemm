/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2.mailing;

import static com.agnitas.emm.rest.v2.mailing.content._MailingContentV2ControllerTest.CONTENT_RESPONSE;
import static com.agnitas.emm.rest.v2.mailing.images._MailingImagesV2ControllerTest.IMAGE_RESPONSE;
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
import java.util.Set;
import java.util.stream.Stream;

import com.agnitas.beans.MailingContentType;
import com.agnitas.emm.common.MailingType;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.mailing.exception.MailingNotExistException;
import com.agnitas.emm.rest.v2.infrastructure.config.json.RestEnumAllowedValues;
import com.agnitas.emm.rest.v2.infrastructure.search.PageResponse;
import com.agnitas.emm.rest.v2.mailing.dto.CreateMailingRequest;
import com.agnitas.emm.rest.v2.mailing.dto.FollowupType;
import com.agnitas.emm.rest.v2.mailing.dto.ListTemplatesRequest;
import com.agnitas.emm.rest.v2.mailing.dto.MailFormat;
import com.agnitas.emm.rest.v2.mailing.dto.MailingFullResponse;
import com.agnitas.emm.rest.v2.mailing.dto.MailingLightResponse;
import com.agnitas.emm.rest.v2.mailing.dto.MailingResponse;
import com.agnitas.emm.rest.v2.mailing.dto.OnePixelPosition;
import com.agnitas.emm.rest.v2.mailing.dto.PatchMailingRequest;
import com.agnitas.emm.rest.v2.mailing.dto.RestCharset;
import com.agnitas.emm.rest.v2.mailing.dto.MailingMediaType;
import com.agnitas.testsupport.annotation.MockBean;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.data.domain.PageRequest;

class _StandardTemplatesV2ControllerTest extends _StandardTemplatesV2ControllerTestSupport {

    private static final String TEMPLATE_FULL_JSON =
            """
            {
                "id": 123,
                "name": "Name",
                "description": "Description",
                "contentType": "advertising",
                "mailingType": "FOLLOW_UP",
                "archiveId": 1,
                "openTriggerId": 11,
                "clickTriggerId": 12,
                "mediaTypes": [
                    "EMAIL",
                    "SMS",
                    "RCS"
                ],
                "mailinglistId": 1,
                "targetExpression": "123&456",
                "needsTarget": true,
                "subject": "Subject",
                "preHeader": "Pre-Header",
                "followupType": "OPENER",
                "followupForId": 122,
                "senderAddress": "sender@example.com",
                "senderName": "Sender Name",
                "replyAddress": "reply@example.com",
                "replyName": "Reply Name",
                "textTemplate": "Text Template",
                "htmlTemplate": "<html><body>HTML template</body></html>",
                "charset": "UTF-8",
                "format": "HTML_OFFLINE",
                "measureOpeningRate": "TOP",
                "envelopeEmail": "envelope@example.com",
                "smsTemplate": "SMS Template",
                "smsSenderAddress": "+99999999999",
                "rcsTemplate": "RCS Template",
                "parameters": [
                    {
                        "name": "name",
                        "value": "value",
                        "description": "description"
                    }
                ],
                "links": [
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
                ],
                "contentBlocks": [
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
                ],
                "attachments": [
                    {
                        "id": 123,
                        "name": "example.png",
                        "targetId": 0,
                        "originalSizeBytes": 2097152,
                        "emailSizeBytes": 2097252,
                        "mimeType": "image/png"
                    }
                ],
                "targets": [
                    {
                        "id": 123,
                        "name": "country de",
                        "description": "de recipients",
                        "sql": "country = de",
                        "eql": "country = de",
                        "valid": true
                    }
                ]
            }
            """;

    private static final MailingFullResponse TEMPLATE_FULL_RESPONSE = new MailingFullResponse(
            123,
            "Name",
            "Description",
            MailingContentType.advertising,
            MailingType.FOLLOW_UP,
            1,
            11,
            12,
            List.of(MailingMediaType.EMAIL, MailingMediaType.SMS, MailingMediaType.RCS),
            1,
            "123&456",
            true,
            "Subject",
            "Pre-Header",
            FollowupType.OPENER,
            122,
            "sender@example.com",
            "Sender Name",
            "reply@example.com",
            "Reply Name",
            "Text Template",
            "<html><body>HTML template</body></html>",
            RestCharset.UTF_8,
            MailFormat.HTML_OFFLINE,
            OnePixelPosition.TOP,
            "envelope@example.com",
            "SMS Template",
            "+99999999999",
            "RCS Template",
            null,
            List.of(PARAMETER_RESPONSE),
            List.of(CONTENT_RESPONSE),
            List.of(LINK_RESPONSE),
            List.of(IMAGE_RESPONSE),
            List.of(ATTACHMENT_RESPONSE),
            List.of(TARGET_RESPONSE)
    );

    private static final String TEMPLATE_RESPONSE_JSON =
            """
            {
                "id": 123,
                "name": "Name",
                "description": "Description",
                "contentType": "advertising",
                "mailingType": "FOLLOW_UP",
                "archiveId": 1,
                "openTriggerId": 11,
                "clickTriggerId": 12,
                "mediaTypes": [
                    "EMAIL",
                    "SMS",
                    "RCS"
                ],
                "mailinglistId": 1,
                "targetExpression": "123&456",
                "needsTarget": true,
                "subject": "Subject",
                "preHeader": "Pre-Header",
                "followupType": "OPENER",
                "followupForId": 122,
                "senderAddress": "sender@example.com",
                "senderName": "Sender Name",
                "replyAddress": "reply@example.com",
                "replyName": "Reply Name",
                "textTemplate": "Plain text template",
                "htmlTemplate": "<html><body>HTML template</body></html>",
                "charset": "UTF-8",
                "format": "HTML",
                "measureOpeningRate": "TOP",
                "envelopeEmail": "envelope@example.com",
                "smsTemplate": "SMS template",
                "smsSenderAddress": "+99999999999",
                "rcsTemplate": "RCS template",
                "parameters": [
                    {
                        "name": "name",
                        "value": "value",
                        "description": "description"
                    }
                ]
            }
            """;

    private static final MailingResponse TEMPLATE_RESPONSE = new MailingResponse(
            123,
            "Name",
            "Description",
            MailingContentType.advertising,
            MailingType.FOLLOW_UP,
            1,
            11,
            12,
            List.of(MailingMediaType.EMAIL, MailingMediaType.SMS, MailingMediaType.RCS),
            1,
            "123&456",
            true,
            "Subject",
            "Pre-Header",
            FollowupType.OPENER,
            122,
            "sender@example.com",
            "Sender Name",
            "reply@example.com",
            "Reply Name",
            "Plain text template",
            "<html><body>HTML template</body></html>",
            RestCharset.UTF_8,
            MailFormat.HTML,
            OnePixelPosition.TOP,
            "envelope@example.com",
            "SMS template",
            "+99999999999",
            "RCS template",
            null,
            null,
            List.of(PARAMETER_RESPONSE)
    );

    protected static final String TEMPLATE_LIGHT_JSON =
            """
            {
                "id": 123,
                "type": "NORMAL",
                "name": "Name",
                "description": "Description"
            }
            """;

    protected static final MailingLightResponse TEMPLATE_LIGHT_RESPONSE = new MailingLightResponse(
            123,
            "NORMAL",
            "Name",
            "Description",
            null,
            null
    );

    @MockBean
    private MailingRestService mailingRestServiceMock;

    @Nested
    class PaginatedList {
        @Test
        void shouldReturn200_onPaginatedList() throws Exception {
            expect(adminMock.allowedAll(Permission.TEMPLATE_SHOW)).andReturn(true);
            ListTemplatesRequest request = new ListTemplatesRequest(
                    "name",
                    Set.of(MailingType.NORMAL, MailingType.DATE_BASED),
                    Set.of(1, 2)
            );
            expect(mailingRestServiceMock.listTemplates(request, PageRequest.of(3, 2), adminMock))
                    .andReturn(new PageResponse<>(
                            List.of(
                                    new MailingLightResponse(1, "NORMAL", "Name 1", "Description 1", null, null),
                                    new MailingLightResponse(2, "DATE_BASED", "Name 2", "Description 2", null, null)
                            ), new PageResponse.PageMeta(3, 2, 1200)
                    ));

            Map<String, Object> params = Map.of(
                    "page", "3",
                    "size", "2",
                    "name", "name",
                    "mailinglistIds", "1,2",
                    "types", "NORMAL,DATE_BASED"
            );
            authenticated(params(get("/templates"), params)).andExpectAll(
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
                                        "id": 1,
                                        "type": "NORMAL",
                                        "name": "Name 1",
                                        "description": "Description 1"
                                    },
                                    {
                                        "id": 2,
                                        "type": "DATE_BASED",
                                        "name": "Name 2",
                                        "description": "Description 2"
                                    }
                                ]
                            }
                            """
                    )
            ).andDo(documentList());
        }

        @Test
        void shouldReturn403() throws Exception {
            expect(adminMock.allowedAll(Permission.TEMPLATE_SHOW)).andReturn(false);
            assertNotAuthorized(get("/templates"));
        }
    }

    @Nested
    class FindSingle {
        @Test
        void shouldReturn200() throws Exception {
            expect(adminMock.allowedAll(Permission.TEMPLATE_SHOW)).andReturn(true);
            expect(mailingRestServiceMock.findTemplate(123, 2)).andReturn(TEMPLATE_RESPONSE);

            authenticated(get("/templates/{id}", 123))
                    .andExpectAll(
                            status().isOk(),
                            content().json(TEMPLATE_RESPONSE_JSON)
                    )
                    .andDo(documentFind());
        }

        @Test
        void shouldReturn400_whenIdIsZero() throws Exception {
            authenticated(get("/templates/{id}", 0)).andExpectAll(
                    status().isBadRequest(),
                    content().json("""
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
            expect(adminMock.allowedAll(Permission.TEMPLATE_SHOW)).andReturn(false);
            assertNotAuthorized(get("/templates/{id}", 1));
        }
    }

    @Nested
    class FindSingleFull {
        @Test
        void shouldReturn200() throws Exception {
            expect(adminMock.allowedAll(Permission.MAILING_SHOW)).andReturn(true);
            expect(mailingRestServiceMock.findTemplateFull(123, adminMock)).andReturn(TEMPLATE_FULL_RESPONSE);

            authenticated(get("/templates/{id}", 123).param("view", "full")).andExpectAll(
                    status().isOk(),
                    content().json(TEMPLATE_FULL_JSON)
            ).andDo(documentFindFull());
        }

        @Test
        void shouldReturn400_whenIdIsZero() throws Exception {
            authenticated(get("/templates/{id}", 0).param("view", "full")).andExpectAll(
                    status().isBadRequest(),
                    content().json("""
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
            expect(adminMock.allowedAll(Permission.MAILING_SHOW)).andReturn(false);
            assertNotAuthorized(get("/templates/{id}", 1).param("view", "full"));
        }

        @Test
        void shouldReturn404() throws Exception {
            expect(adminMock.allowedAll(Permission.MAILING_SHOW)).andReturn(true);
            expect(mailingRestServiceMock.findTemplateFull(2, adminMock))
                    .andThrow(new MailingNotExistException(COMPANY_ID, 2));

            authenticated(get("/templates/{id}", 2).param("view", "full")).andExpectAll(
                    status().isNotFound(),
                    content().json(
                            """
                            {
                                "status": 404,
                                "detail": "Mailing does not exist. CompanyID: %d, MailingID: 2",
                            }
                            """.formatted(COMPANY_ID)
                    )
            );
        }
    }

    @Nested
    class FindSingleLight {
        @Test
        void shouldReturn200() throws Exception {
            expect(adminMock.allowedAll(Permission.MAILING_SHOW)).andReturn(true);
            expect(mailingRestServiceMock.findTemplateLight(123, 2)).andReturn(TEMPLATE_LIGHT_RESPONSE);

            authenticated(get("/templates/{id}", 123).param("view", "light"))
                    .andExpectAll(
                            status().isOk(),
                            content().json(TEMPLATE_LIGHT_JSON)
                    )
                    .andDo(documentFindLight());
        }

        @Test
        void shouldReturn400_whenIdIsZero() throws Exception {
            authenticated(get("/templates/{id}", 0).param("view", "light")).andExpectAll(
                    status().isBadRequest(),
                    content().json("""
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
            expect(adminMock.allowedAll(Permission.MAILING_SHOW)).andReturn(false);
            assertNotAuthorized(get("/templates/{id}", 1).param("view", "light"));
        }

        @Test
        void shouldReturn404() throws Exception {
            expect(adminMock.allowedAll(Permission.MAILING_SHOW)).andReturn(true);
            expect(mailingRestServiceMock.findTemplateLight(2, 2)).andThrow(new MailingNotExistException(
                    COMPANY_ID,
                    2
            ));

            authenticated(get("/templates/{id}", 2).param("view", "light")).andExpectAll(
                    status().isNotFound(),
                    content().json(
                            """
                            {
                                "status": 404,
                                "detail": "Mailing does not exist. CompanyID: %d, MailingID: 2",
                            }
                            """.formatted(COMPANY_ID)
                    )
            );
        }
    }

    @Nested
    class Copy {
        @Test
        void shouldReturn201() throws Exception {
            expect(adminMock.allowedAll(Permission.TEMPLATE_CHANGE)).andReturn(true);
            expect(mailingRestServiceMock.copyTemplate(123, 2)).andReturn(TEMPLATE_LIGHT_RESPONSE);

            authenticated(post("/templates/{id}/copy", 123)).andExpectAll(
                    status().isCreated(),
                    content().json(TEMPLATE_LIGHT_JSON)
            ).andDo(documentCopy());
        }

        @Test
        void shouldReturn400_whenIdIsZero() throws Exception {
            authenticated(post("/templates/{id}/copy", 0)).andExpectAll(
                    status().isBadRequest(),
                    content().json("""
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
            expect(adminMock.allowedAll(Permission.TEMPLATE_CHANGE)).andReturn(false);
            assertNotAuthorized(post("/templates/{id}/copy", 1));
        }
    }

    @Nested
    class Create {
        @Test
        void shouldReturn201() throws Exception {
            expect(adminMock.allowedAll(Permission.TEMPLATE_CHANGE)).andReturn(true);
            expect(mailingRestServiceMock.createTemplate(
                    new CreateMailingRequest(
                            "Name",
                            "Description",
                            MailingContentType.advertising,
                            MailingType.FOLLOW_UP,
                            1,
                            List.of(MailingMediaType.EMAIL, MailingMediaType.SMS, MailingMediaType.RCS),
                            11,
                            12,
                            1,
                            "123&456",
                            true,
                            "Subject",
                            "Pre-Header",
                            "sender@example.com",
                            "Sender Name",
                            "reply@example.com",
                            "Reply Name",
                            FollowupType.OPENER,
                            122,
                            "Text template",
                            "<html>HTML template</html>",
                            RestCharset.UTF_8,
                            MailFormat.HTML,
                            OnePixelPosition.TOP,
                            "envelope@example.com",
                            "SMS template",
                            "+99999999999",
                            "RCS template",
                            List.of(CREATE_PARAMETER_REQUEST)
                    ), adminMock
            )).andReturn(TEMPLATE_RESPONSE);

            authenticated(post("/templates").content(
                    """
                    {
                        "name": "Name",
                        "description": "Description",
                        "contentType": "advertising",
                        "mailingType": "FOLLOW_UP",
                        "archiveId": 1,
                        "openTriggerId": 11,
                        "clickTriggerId": 12,
                        "mediaTypes": [
                            "EMAIL",
                            "SMS",
                            "RCS"
                        ],
                        "mailinglistId": 1,
                        "targetExpression": "123&456",
                        "needsTarget": true,
                        "subject": "Subject",
                        "preHeader": "Pre-Header",
                        "followupType": "OPENER",
                        "followupForId": 122,
                        "senderAddress": "sender@example.com",
                        "senderName": "Sender Name",
                        "replyAddress": "reply@example.com",
                        "replyName": "Reply Name",
                        "textTemplate": "Text template",
                        "htmlTemplate": "<html>HTML template</html>",
                        "charset": "UTF-8",
                        "format": "HTML",
                        "measureOpeningRate": "TOP",
                        "envelopeEmail": "envelope@example.com",
                        "smsTemplate": "SMS template",
                        "smsSenderAddress": "+99999999999",
                        "rcsTemplate": "RCS template",
                        "parameters": [
                            {
                                "name": "name",
                                "value": "value",
                                "description": "description"
                            }
                        ]
                    }
                    """
            ))
                    .andExpectAll(
                            status().isCreated(),
                            content().json(TEMPLATE_RESPONSE_JSON)
                    )
                    .andDo(documentCreate());
        }

        public static Stream<Arguments> provideInvalidInputForCreate() {
            return Stream.of(
                    Arguments.of(
                            """
                            {
                              "mailingType": "NORMAL",
                              "mailinglistId": 1
                            }
                            """,
                            "name",
                            "must not be blank"
                    ),
                    Arguments.of(
                            """
                            {
                              "name": "   ",
                              "mailingType": "NORMAL",
                              "mailinglistId": 1
                            }
                            """,
                            "name",
                            "must not be blank"
                    ),
                    Arguments.of(
                            """
                            {
                              "name": "ab",
                              "mailingType": "NORMAL",
                              "mailinglistId": 1
                            }
                            """,
                            "name",
                            "size must be between 3 and 99"
                    ),
                    Arguments.of(
                            """
                            {
                              "name": "valid name",
                              "mailinglistId": 1,
                              "description": "%s",
                              "mailingType": "NORMAL"
                            }
                            """.formatted(RandomStringUtils.secure().nextAlphabetic(501)),
                            "description",
                            "size must be between 0 and 500"
                    ),
                    Arguments.of(
                            """
                            {
                              "name": "valid name",
                              "mailinglistId": 1,
                              "subject": "a",
                              "mailingType": "NORMAL"
                            }
                            """,
                            "subject",
                            "size must be between 2 and 2147483647"
                    ),
                    Arguments.of(
                            """
                            {
                              "name": "valid name",
                              "mailinglistId": 1,
                              "senderAddress": "invalid email.com",
                              "mailingType": "NORMAL"
                            }
                            """,
                            "senderAddress",
                            "must be a well-formed email address"
                    ),
                    Arguments.of(
                            """
                            {
                              "name": "valid name",
                              "mailinglistId": 1,
                              "replyAddress": "invalid email.com",
                              "mailingType": "NORMAL"
                            }
                            """,
                            "replyAddress",
                            "must be a well-formed email address"
                    ),
                    Arguments.of(
                            """
                            {
                              "name": "valid name",
                              "mailinglistId": 1,
                              "envelopeEmail": "invalid-envelope",
                              "mailingType": "NORMAL"
                            }
                            """,
                            "envelopeEmail",
                            "must be a well-formed email address"
                    ),
                    Arguments.of(
                            """
                            {
                              "name": "valid name",
                              "mailinglistId": 1,
                              "senderName": "%s",
                              "mailingType": "NORMAL"
                            }
                            """.formatted(RandomStringUtils.secure().nextAlphabetic(256)),
                            "senderName",
                            "size must be between 0 and 255"
                    ),
                    Arguments.of(
                            """
                            {
                              "name": "valid name",
                              "mailinglistId": 1,
                              "replyName": "%s",
                              "mailingType": "NORMAL"
                            }
                            """.formatted(RandomStringUtils.secure().nextAlphabetic(256)),
                            "replyName",
                            "size must be between 0 and 255"
                    ),
                    Arguments.of(
                            """
                            {
                              "name": "valid name",
                              "mailingType": "NORMAL",
                              "mailinglistId": 1,
                              "mediaTypes": []
                            }
                            """,
                            "mediaTypes",
                            "must not be empty"
                    ),
                    Arguments.of(
                            """
                            {
                              "name": "valid name",
                              "mailinglistId": 1,
                              "mailingType": "NORMAL",
                              "archiveId": 0,
                              "subject": "Subject"
                            }
                            """,
                            "archiveId",
                            "must be greater than 0"
                    ),
                    Arguments.of(
                            """
                            {
                              "name": "valid name",
                              "mailinglistId": 1,
                              "mailingType": "NORMAL",
                              "openTriggerId": 0,
                              "subject": "Subject"
                            }
                            """,
                            "openTriggerId",
                            "must be greater than 0"
                    ),
                    Arguments.of(
                            """
                            {
                              "name": "valid name",
                              "mailinglistId": 1,
                              "mailingType": "NORMAL",
                              "clickTriggerId": 0,
                              "subject": "Subject"
                            }
                            """,
                            "clickTriggerId",
                            "must be greater than 0"
                    ),
                    Arguments.of(
                            """
                            {
                              "name": "valid name",
                              "mailingType": "NORMAL",
                              "mailinglistId": 0,
                              "subject": "Subject"
                            }
                            """,
                            "mailinglistId",
                            "must be greater than 0"
                    ),
                    Arguments.of(
                            """
                            {
                              "name": "valid name",
                              "mailingType": "NORMAL",
                              "subject": "Subject"
                            }
                            """,
                            "mailinglistId",
                            "must not be null"
                    ),
                    Arguments.of(
                            """
                            {
                              "name": "valid name",
                              "mailingType": "FOLLOW_UP",
                              "mailinglistId": 1,
                              "followupType": "OPENER",
                              "followupForId": 0,
                              "subject": "Subject"
                            }
                            """,
                            "followupForId",
                            "must be greater than 0"
                    ),
                    Arguments.of(
                            """
                            {
                              "name": "valid name",
                              "mailingType": "FOLLOW_UP",
                              "mailinglistId": 1,
                              "followupType": "OPENER",
                              "subject": "Subject"
                            }
                            """,
                            "followupForIdPresentForFollowUp",
                            "followupForId must be specified for follow-up mailings"
                    ),
                    Arguments.of(
                            """
                            {
                              "name": "valid name",
                              "mailingType": "FOLLOW_UP",
                              "mailinglistId": 1,
                              "followupForId": 1
                            }
                            """,
                            "followupTypePresentForFollowUp",
                            "followupType must be specified for follow-up mailings"
                    ),
                    Arguments.of(
                            """
                            {
                              "name": "valid name",
                              "mailingType": "invalid",
                              "mailinglistId": 1,
                              "subject": "Subject"
                            }
                            """,
                            "mailingType",
                            RestEnumAllowedValues.MAILING_TYPES
                    ),
                    Arguments.of(
                            """
                            {
                              "name": "valid name",
                              "contentType": "invalid",
                              "mailingType": "NORMAL",
                              "mailinglistId": 1,
                              "subject": "Subject"
                            }
                            """,
                            "contentType",
                            RestEnumAllowedValues.MAILING_CONTENT_TYPES
                    ),
                    Arguments.of(
                            """
                            {
                              "name": "valid name",
                              "mailingType": "NORMAL",
                              "mailinglistId": 1,
                              "charset": "invalid"
                            }
                            """,
                            "charset",
                            RestEnumAllowedValues.CHARSETS
                    ),
                    Arguments.of(
                            """
                            {
                              "name": "valid name",
                              "mailingType": "NORMAL",
                              "mailinglistId": 1,
                              "format": "invalid"
                            }
                            """,
                            "format",
                            RestEnumAllowedValues.MAIL_FORMATS
                    ),
                    Arguments.of(
                            """
                            {
                              "name": "valid name",
                              "mailingType": "NORMAL",
                              "mailinglistId": 1,
                              "followupType": "invalid"
                            }
                            """,
                            "followupType",
                            RestEnumAllowedValues.FOLLOWUP_TYPES
                    ),
                    Arguments.of(
                            """
                            {
                              "name": "valid name",
                              "mailingType": "NORMAL",
                              "mailinglistId": 1,
                              "measureOpeningRate": "invalid"
                            }
                            """,
                            "measureOpeningRate",
                            RestEnumAllowedValues.ONE_PIXEL_POSITIONS
                    ),
                    Arguments.of(
                            """
                            {
                              "name": "valid name",
                              "mailingType": "NORMAL",
                              "mailinglistId": 1,
                              "mediaTypes": ["invalid"]
                            }
                            """,
                            "mediaTypes",
                            RestEnumAllowedValues.REST_MEDIATYPES
                    ),
                    Arguments.of(
                            """
                            {
                              "name": "valid name",
                              "mailingType": "NORMAL",
                              "mailinglistId": 1,
                              "mediaTypes": [null]
                            }
                            """,
                            "mediaTypes[0]",
                            "must not be null"
                    ),
                    Arguments.of(
                            """
                            {
                              "name": "valid name",
                              "mailingType": "NORMAL",
                              "mailinglistId": 1,
                              "mediaTypes": []
                            }
                            """,
                            "mediaTypes",
                            "must not be empty"
                    ),
                    Arguments.of(
                            """
                            {
                              "name": "valid name",
                              "mailinglistId": 1,
                              "mediaTypes": ["SMS"],
                              "mailingType": "NORMAL",
                              "smsSenderAddress": "12"
                            }
                            """,
                            "smsSenderAddress",
                            "size must be between 3 and 2147483647"
                    )
            );
        }

        @ParameterizedTest
        @MethodSource("provideInvalidInputForCreate")
        void shouldReturn400(String body, String field, String expectedError) throws Exception {
            authenticated(post("/mailings").content(body)).andExpectAll(
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
        void shouldReturn403() throws Exception {
            expect(adminMock.allowedAll(Permission.TEMPLATE_CHANGE)).andReturn(false);
            assertNotAuthorized(post("/templates").content(
                    """
                    {
                      "name": "new name",
                      "mailingType": "NORMAL",
                      "mailinglistId": 1
                    }
                    """
            ));
        }
    }

    @Nested
    class Patch {
        @Test
        void shouldReturn200_onPartialUpdate() throws Exception {
            var updateDto = new PatchMailingRequest(
                    JsonNullable.of("Name"),
                    JsonNullable.of("Description"),
                    JsonNullable.of(MailingContentType.advertising),
                    JsonNullable.of(MailingType.FOLLOW_UP),
                    JsonNullable.of(10),
                    JsonNullable.of(11),
                    JsonNullable.of(12),
                    JsonNullable.of(List.of(MailingMediaType.EMAIL, MailingMediaType.SMS, MailingMediaType.RCS)),
                    JsonNullable.of(1),
                    JsonNullable.of("123&456"),
                    JsonNullable.of(true),
                    JsonNullable.of("Subject"),
                    JsonNullable.of("PreHeader"),
                    JsonNullable.of(FollowupType.NON_CLICKER),
                    JsonNullable.of(111),
                    JsonNullable.of("sender@example.com"),
                    JsonNullable.of("Sender Name"),
                    JsonNullable.of("reply@example.com"),
                    JsonNullable.of("Reply Name"),
                    JsonNullable.of("Text Template"),
                    JsonNullable.of("<html>HTML Template</html>"),
                    JsonNullable.of(RestCharset.UTF_8),
                    JsonNullable.of(MailFormat.HTML_OFFLINE),
                    JsonNullable.of(OnePixelPosition.TOP),
                    JsonNullable.of("envelope@example.com"),
                    JsonNullable.of("SMS Template"),
                    JsonNullable.of("+09912345678"),
                    JsonNullable.of("RCS Template"),
                    JsonNullable.of(List.of(UPDATE_PARAMETER_REQUEST))
            );
            expect(adminMock.allowedAll(Permission.TEMPLATE_CHANGE)).andReturn(true);
            expect(mailingRestServiceMock.patchTemplate(2, updateDto, adminMock)).andReturn(TEMPLATE_RESPONSE);

            authenticated(patch("/templates/{id}", 2)
                    .content(
                            """
                            {
                                "name": "Name",
                                "description": "Description",
                                "mailinglistId": 1,
                                "mailingType": "FOLLOW_UP",
                                "contentType": "advertising",
                                "subject": "Subject",
                                "followupType": "NON_CLICKER",
                                "followupForId": 111,
                                "preHeader": "PreHeader",
                                "senderAddress": "sender@example.com",
                                "senderName": "Sender Name",
                                "replyAddress": "reply@example.com",
                                "replyName": "Reply Name",
                                "targetExpression": "123&456",
                                "needsTarget": true,
                                "textTemplate": "Text Template",
                                "htmlTemplate": "<html>HTML Template</html>",
                                "smsTemplate": "SMS Template",
                                "rcsTemplate": "RCS Template",
                                "openTriggerId": 11,
                                "clickTriggerId": 12,
                                "archiveId": 10,
                                "mediaTypes": ["EMAIL", "SMS", "RCS"],
                                "charset": "UTF_8",
                                "format": "HTML_OFFLINE",
                                "measureOpeningRate": "TOP",
                                "envelopeEmail": "envelope@example.com",
                                "smsSenderAddress": "+09912345678",
                                "parameters": [
                                    {
                                        "name": "name",
                                        "value": "value",
                                        "description": "description"
                                    }
                                ]
                            }
                            """
                    )
            )
                    .andExpectAll(status().isOk(), content().json(TEMPLATE_RESPONSE_JSON))
                    .andDo(documentPatch());
        }

        public static Stream<Arguments> provideInvalidInputForPatch() {
            return Stream.of(
                    Arguments.of(
                            """
                            { "name": null }
                            """,
                            "name",
                            "must not be blank"
                    ),
                    Arguments.of(
                            """
                            { "name": "   " }
                            """,
                            "name",
                            "must not be blank"
                    ),
                    Arguments.of(
                            """
                            { "name": "ab" }
                            """,
                            "name",
                            "size must be between 3 and 99"
                    ),
                    Arguments.of(
                            """
                            { "name": "valid name",
                              "description": "%s"
                            }
                            """.formatted(RandomStringUtils.secure().nextAlphabetic(501)),
                            "description",
                            "size must be between 0 and 500"
                    ),
                    Arguments.of(
                            """
                            { "mailingType": "invalid" }
                            """,
                            "mailingType",
                            RestEnumAllowedValues.MAILING_TYPES
                    ),
                    Arguments.of(
                            """
                            { "contentType": "invalid" }
                            """,
                            "contentType",
                            RestEnumAllowedValues.MAILING_CONTENT_TYPES
                    ),
                    Arguments.of(
                            """
                            { "mailinglistId": 0 }
                            """,
                            "mailinglistId",
                            "must be greater than 0"
                    ),
                    Arguments.of(
                            """
                            { "archiveId": 0 }
                            """,
                            "archiveId",
                            "must be greater than 0"
                    ),
                    Arguments.of(
                            """
                            { "openTriggerId": 0 }
                            """,
                            "openTriggerId",
                            "must be greater than 0"
                    ),
                    Arguments.of(
                            """
                            { "clickTriggerId": 0 }
                            """,
                            "clickTriggerId",
                            "must be greater than 0"
                    ),
                    Arguments.of(
                            """
                            { "subject": "a" }
                            """,
                            "subject",
                            "size must be between 2 and 2147483647"
                    ),
                    Arguments.of(
                            """
                            { "senderAddress": "invalid email.com" }
                            """,
                            "senderAddress",
                            "must be a well-formed email address"
                    ),
                    Arguments.of(
                            """
                            { "replyAddress": "invalid email.com" }
                            """,
                            "replyAddress",
                            "must be a well-formed email address"
                    ),
                    Arguments.of(
                            """
                            { "envelopeEmail": "invalid-envelope" }
                            """,
                            "envelopeEmail",
                            "must be a well-formed email address"
                    ),
                    Arguments.of(
                            """
                            { "senderName": "%s" }
                            """.formatted(RandomStringUtils.secure().nextAlphabetic(256)),
                            "senderName",
                            "size must be between 0 and 255"
                    ),
                    Arguments.of(
                            """
                            { "replyName": "%s" }
                            """.formatted(RandomStringUtils.secure().nextAlphabetic(256)),
                            "replyName",
                            "size must be between 0 and 255"
                    ),
                    Arguments.of(
                            """
                            { "charset": "invalid" }
                            """,
                            "charset",
                            RestEnumAllowedValues.CHARSETS
                    ),
                    Arguments.of(
                            """
                            { "format": "invalid" }
                            """,
                            "format",
                            RestEnumAllowedValues.MAIL_FORMATS
                    ),
                    Arguments.of(
                            """
                            { "measureOpeningRate": "invalid" }
                            """,
                            "measureOpeningRate",
                            RestEnumAllowedValues.ONE_PIXEL_POSITIONS
                    ),
                    Arguments.of(
                            """
                            { "followupType": "invalid" }
                            """,
                            "followupType",
                            RestEnumAllowedValues.FOLLOWUP_TYPES
                    ),
                    Arguments.of(
                            """
                            { "followupForId": 0 }
                            """,
                            "followupForId",
                            "must be greater than 0"
                    ),
                    Arguments.of(
                            """
                            { "mailinglistId": null }
                            """,
                            "mailinglistId",
                            "must not be null"
                    ),
                    Arguments.of(
                            """
                            { "smsSenderAddress": "12" }
                            """,
                            "smsSenderAddress",
                            "size must be between 3 and 2147483647"
                    )
            );
        }

        @ParameterizedTest
        @MethodSource("provideInvalidInputForPatch")
        void shouldReturn400(String body, String field, String expectedError) throws Exception {
            authenticated(patch("/templates/{id}", 123).content(body)).andExpectAll(
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
        void shouldReturn400_whenIdIsZero() throws Exception {
            authenticated(patch("/templates/{id}", 0).content("{}")).andExpectAll(
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
            expect(adminMock.allowedAll(Permission.MAILING_CHANGE)).andReturn(false);
            assertNotAuthorized(patch("/mailings/{id}", 1).content("{}"));
        }
    }

    @Nested
    class Delete {
        @Test
        void shouldReturn204() throws Exception {
            expect(adminMock.allowedAll(Permission.TEMPLATE_DELETE)).andReturn(true);
            mailingRestServiceMock.deleteTemplate(2, adminMock);
            expectLastCall();

            authenticated(delete("/templates/{id}", 2))
                    .andExpect(status().isNoContent())
                    .andDo(documentDelete());
        }

        @Test
        void shouldReturn400_whenIdIsZero() throws Exception {
            authenticated(delete("/templates/{id}", 0)).andExpectAll(
                    status().isBadRequest(),
                    content().json("""
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
            expect(adminMock.allowedAll(Permission.TEMPLATE_DELETE)).andReturn(false);
            assertNotAuthorized(delete("/templates/{id}", 1));
        }
    }
}
