/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2.mailing.attachments;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.components.form.AttachmentType;
import com.agnitas.emm.rest.v2.infrastructure.search.PageResponse;
import com.agnitas.testsupport.annotation.MockBean;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

class _MailingAttachmentsV2ControllerTest extends _MailingAttachmentsV2ControllerTestSupport {

    private static final AttachmentResponse ATTACHMENT_RESPONSE = new AttachmentResponse(
            123,
            "example.png",
            0,
            2097152,
            2097252,
            "image/png"
    );
    private static final AttachmentResponse ATTACHMENT_2_RESPONSE = new AttachmentResponse(
            124,
            "file.pdf",
            12,
            102400,
            102500,
            "application/pdf"
    );

    private static final String RESPONSE_JSON =
            """
            {
                "id": 123,
                "name": "example.png",
                "targetId": 0,
                "originalSizeBytes": 2097152,
                "emailSizeBytes": 2097252,
                "mimeType": "image/png"
            }
            """;

    @MockBean
    private MailingAttachmentRestService restServiceMock;

    @Nested
    class PaginatedList {
        @Test
        void shouldReturn200() throws Exception {
            expect(adminMock.allowedAll(Permission.MAILING_ATTACHMENTS_SHOW)).andReturn(true);
            expect(restServiceMock.list(4, PageRequest.of(3, 2), adminMock)).andReturn(new PageResponse<>(
                    List.of(ATTACHMENT_RESPONSE, ATTACHMENT_2_RESPONSE),
                    new PageResponse.PageMeta(3, 2, 50)
            ));
            Map<String, Object> params = Map.of(
                    "page", "3",
                    "size", "2"
            );
            authenticated(params(get("/mailings/{mailingId}/attachments", 4), params)).andExpectAll(
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
                                        "name": "example.png",
                                        "targetId": 0,
                                        "originalSizeBytes": 2097152,
                                        "emailSizeBytes": 2097252,
                                        "mimeType": "image/png"
                                    },
                                    {
                                        "id": 124,
                                        "name": "file.pdf",
                                        "targetId": 12,
                                        "originalSizeBytes": 102400,
                                        "emailSizeBytes": 102500,
                                        "mimeType": "application/pdf"
                                    }
                                ]
                            }
                            """
                    )
            ).andDo(documentList());
        }

        @Test
        void shouldReturn400_whenMailingIdIsZero() throws Exception {
            authenticated(get("/mailings/{mailingId}/attachments", 0)).andExpectAll(
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

        @Test
        void shouldReturn403() throws Exception {
            expect(adminMock.allowedAll(Permission.MAILING_ATTACHMENTS_SHOW)).andReturn(false);
            assertNotAuthorized(get("/mailings/{mailingId}/attachments", 1));
        }
    }

    @Nested
    class FindById {
        @Test
        void shouldReturn200() throws Exception {
            expect(adminMock.allowedAll(Permission.MAILING_ATTACHMENTS_SHOW)).andReturn(true);
            expect(restServiceMock.find(123, 4, adminMock)).andReturn(ATTACHMENT_RESPONSE);
            authenticated(get("/mailings/{mailingId}/attachments/{attachmentId}", 4, 123)).andExpectAll(
                    status().isOk(),
                    content().json(RESPONSE_JSON)
            ).andDo(documentFindById());
        }

        @Test
        void shouldReturn400_whenMailingIdZero() throws Exception {
            authenticated(get("/mailings/{mailingId}/attachments/{attachmentId}", 0, 123)).andExpectAll(
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

        @Test
        void shouldReturn400_whenAttachmentIdIsZero() throws Exception {
            authenticated(get("/mailings/{mailingId}/attachments/{attachmentId}", 4, 0)).andExpectAll(
                    status().isBadRequest(),
                    content().json(
                            """
                            {
                                "status": 400,
                                "detail": "Invalid input.",
                                "errors": [
                                    {
                                        "field": "attachmentId",
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
            expect(adminMock.allowedAll(Permission.MAILING_ATTACHMENTS_SHOW)).andReturn(false);
            assertNotAuthorized(get("/mailings/{mailingId}/attachments/{attachmentId}", 4, 123));
        }
    }

    @Nested
    class FindByName {
        @Test
        void shouldReturn200() throws Exception {
            expect(adminMock.allowedAll(Permission.MAILING_ATTACHMENTS_SHOW)).andReturn(true);
            expect(restServiceMock.find("example.png", 4, adminMock)).andReturn(ATTACHMENT_RESPONSE);
            authenticated(get("/mailings/{mailingId}/attachments/by-name/{filename}", 4, "example.png")).andExpectAll(
                    status().isOk(),
                    content().json(RESPONSE_JSON)
            ).andDo(documentFindByName());
        }

        @Test
        void shouldReturn400_whenMailingIdZero() throws Exception {
            authenticated(get("/mailings/{mailingId}/attachments/by-name/{filename}", 0, "abc.jpeg")).andExpectAll(
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

        @Test
        void shouldReturn400_whenFilenameIsBlank() throws Exception {
            authenticated(get("/mailings/{mailingId}/attachments/by-name/{filename}", 4, " ")).andExpectAll(
                    status().isBadRequest(),
                    content().json(
                            """
                            {
                                "status": 400,
                                "detail": "Invalid input.",
                                "errors": [
                                    {
                                        "field": "filename",
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
            expect(adminMock.allowedAll(Permission.MAILING_ATTACHMENTS_SHOW)).andReturn(false);
            assertNotAuthorized(get("/mailings/{mailingId}/attachments/by-name/{filename}", 123, "abc.jpeg"));
        }
    }

    @Nested
    class GetFileContentById {
        @Test
        void shouldReturn200() throws Exception {
            expect(adminMock.allowedAll(Permission.MAILING_ATTACHMENTS_SHOW)).andReturn(true);
            expect(restServiceMock.getContent(123, 4, adminMock)).andReturn(ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("application/pdf"))
                    .contentLength(1024)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"file.pdf\"")
                    .body(new InputStreamResource(new ByteArrayInputStream("pdf".getBytes(StandardCharsets.UTF_8)))));
            expectLastCall().times(1);

            authenticated(get("/mailings/{mailingId}/attachments/{attachmentId}/content", 4, 123))
                    .andExpectAll(
                            status().isOk(),
                            content().contentType(MediaType.APPLICATION_PDF),
                            header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"file.pdf\""),
                            header().longValue(HttpHeaders.CONTENT_LENGTH, 1024),
                            content().bytes("pdf".getBytes())
                    )
                    .andDo(documentGetContentById());
        }

        @Test
        void shouldReturn400_whenMailingIdIsZero() throws Exception {
            authenticated(get("/mailings/{mailingId}/attachments/{attachmentId}/content", 0, 123)).andExpectAll(
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

        @Test
        void shouldReturn400_whenAttachmentIdIsZero() throws Exception {
            authenticated(get("/mailings/{mailingId}/attachments/{attachmentId}/content", 4, 0)).andExpectAll(
                    status().isBadRequest(),
                    content().json(
                            """
                            {
                                "status": 400,
                                "detail": "Invalid input.",
                                "errors": [
                                    {
                                        "field": "attachmentId",
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
            expect(adminMock.allowedAll(Permission.MAILING_ATTACHMENTS_SHOW)).andReturn(false);
            assertNotAuthorized(get("/mailings/{mailingId}/attachments/{attachmentId}/content", 1, 123));
        }
    }

    @Nested
    class GetFileContentByName {
        @Test
        void shouldReturn200() throws Exception {
            expect(adminMock.allowedAll(Permission.MAILING_ATTACHMENTS_SHOW)).andReturn(true);
            expect(restServiceMock.getContent("file.pdf", 4, adminMock)).andReturn(ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("application/pdf"))
                    .contentLength(1024)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"file.pdf\"")
                    .body(new InputStreamResource(new ByteArrayInputStream("pdf".getBytes(StandardCharsets.UTF_8)))));
            expectLastCall().times(1);

            authenticated(get("/mailings/{mailingId}/attachments/by-name/{filename}/content", 4, "file.pdf"))
                    .andExpectAll(
                            status().isOk(),
                            content().contentType(MediaType.APPLICATION_PDF),
                            header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"file.pdf\""),
                            header().longValue(HttpHeaders.CONTENT_LENGTH, 1024),
                            content().bytes("pdf".getBytes())
                    )
                    .andDo(documentGetContentByName());
        }

        @Test
        void shouldReturn400_whenMailingIdIsZero() throws Exception {
            authenticated(get("/mailings/{mailingId}/attachments/by-name/{filename}/content", 0, "file.pdf")).andExpectAll(
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

        @Test
        void shouldReturn400_whenFilenameIsBlank() throws Exception {
            authenticated(get("/mailings/{mailingId}/attachments/by-name/{filename}/content", 4, " ")).andExpectAll(
                    status().isBadRequest(),
                    content().json(
                            """
                            {
                                "status": 400,
                                "detail": "Invalid input.",
                                "errors": [
                                    {
                                        "field": "filename",
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
            expect(adminMock.allowedAll(Permission.MAILING_ATTACHMENTS_SHOW)).andReturn(false);
            assertNotAuthorized(get("/mailings/{mailingId}/attachments/by-name/{filename}/content", 1, "file.pdf"));
        }
    }

    @Nested
    class Create {
        @Test
        void shouldReturn201() throws Exception {
            expect(adminMock.allowedAll(Permission.MAILING_ATTACHMENTS_SHOW)).andReturn(true);
            MockMultipartFile file = new MockMultipartFile(
                    "attachment",
                    "file.pdf",
                    "application/pdf",
                    "pdf-content".getBytes(StandardCharsets.UTF_8)
            );
            UploadAttachmentRequest request = new UploadAttachmentRequest(
                    "file.pdf",
                    file,
                    AttachmentType.NORMAL,
                    null,
                    12
            );
            expect(restServiceMock.create(request, 4, adminMock)).andReturn(ATTACHMENT_2_RESPONSE);
            expectLastCall().times(1);

            authenticated(
                    multipart("/mailings/{mailingId}/attachments", 4)
                            .file(file)
                            .param("name", "file.pdf")
                            .param("type", "NORMAL")
                            .param("targetId", "12"),
                    MediaType.MULTIPART_FORM_DATA
            )
                    .andExpectAll(status().isCreated(), content().json(
                                    """
                                        {
                                            "id": 124,
                                            "name": "file.pdf",
                                            "targetId": 12,
                                            "originalSizeBytes": 102400,
                                            "emailSizeBytes": 102500,
                                            "mimeType": "application/pdf"
                                        }
                                    """
                            )
                    )
                    .andDo(documentCreate());
        }

        @Test
        void shouldReturn400_whenMailingIdIsZero() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                    "attachment", "file.pdf", "application/pdf", "pdf".getBytes(StandardCharsets.UTF_8));

            authenticated(
                    multipart("/mailings/{mailingId}/attachments", 0)
                            .file(file)
                            .param("name", "file.pdf"),
                    MediaType.MULTIPART_FORM_DATA
            )
                    .andExpectAll(
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

        @Test
        void shouldReturn403() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                    "attachment", "file.pdf", "application/pdf", "pdf".getBytes(StandardCharsets.UTF_8));

            expect(adminMock.allowedAll(Permission.MAILING_ATTACHMENTS_SHOW)).andReturn(false);
            assertNotAuthorized(
                    multipart("/mailings/{mailingId}/attachments", 4)
                    .file(file)
                    .param("name", "file.pdf"),
                    MediaType.MULTIPART_FORM_DATA
            );
        }
    }

    @Nested
    class DeleteById {
        @Test
        void shouldReturn204() throws Exception {
            expect(adminMock.allowedAll(Permission.MAILING_ATTACHMENTS_SHOW)).andReturn(true);
            restServiceMock.delete(123, 4, adminMock);
            expectLastCall().times(1);

            authenticated(delete("/mailings/{mailingId}/attachments/{attachmentId}", 4, 123))
                    .andExpect(status().isNoContent())
                    .andDo(documentDeleteById());
        }

        @Test
        void shouldReturn400_whenMailingIdIsZero() throws Exception {
            authenticated(delete("/mailings/{mailingId}/attachments/{attachmentId}", 0, 123)).andExpectAll(
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


        @Test
        void shouldReturn400_whenAttachmentIdIsZero() throws Exception {
            authenticated(delete("/mailings/{mailingId}/attachments/{attachmentId}", 4, 0)).andExpectAll(
                    status().isBadRequest(),
                    content().json(
                            """
                            {
                                "status": 400,
                                "detail": "Invalid input.",
                                "errors": [
                                    {
                                        "field": "attachmentId",
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
            expect(adminMock.allowedAll(Permission.MAILING_ATTACHMENTS_SHOW)).andReturn(false);
            assertNotAuthorized(delete("/mailings/{mailingId}/attachments/{attachmentId}", 1, 123));
        }
    }

    @Nested
    class DeleteByName {
        @Test
        void shouldReturn204() throws Exception {
            expect(adminMock.allowedAll(Permission.MAILING_ATTACHMENTS_SHOW)).andReturn(true);
            restServiceMock.delete("file.pdf", 4, adminMock);
            expectLastCall().times(1);

            authenticated(delete("/mailings/{mailingId}/attachments/by-name/{filename}", 4, "file.pdf"))
                    .andExpect(status().isNoContent())
                    .andDo(documentDeleteByName());
        }

        @Test
        void shouldReturn400_whenMailingIdIsZero() throws Exception {
            authenticated(delete("/mailings/{mailingId}/attachments/by-name/{filename}", 0, "file.pdf")).andExpectAll(
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


        @Test
        void shouldReturn400_whenFilenameIsBlank() throws Exception {
            authenticated(delete("/mailings/{mailingId}/attachments/by-name/{filename}", 4, " ")).andExpectAll(
                    status().isBadRequest(),
                    content().json(
                            """
                            {
                                "status": 400,
                                "detail": "Invalid input.",
                                "errors": [
                                    {
                                        "field": "filename",
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
            expect(adminMock.allowedAll(Permission.MAILING_ATTACHMENTS_SHOW)).andReturn(false);
            assertNotAuthorized(delete("/mailings/{mailingId}/attachments/by-name/{filename}", 1, "file.pdf"));
        }
    }
}
