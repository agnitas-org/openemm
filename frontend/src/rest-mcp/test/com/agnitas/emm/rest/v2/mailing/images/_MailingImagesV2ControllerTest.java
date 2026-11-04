/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2.mailing.images;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import com.agnitas.emm.core.Permission;
import com.agnitas.emm.rest.v2.infrastructure.search.PageResponse;
import com.agnitas.testsupport.annotation.MockBean;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

public class _MailingImagesV2ControllerTest extends _MailingImagesV2ControllerTestSupport {

    public static final ImageResponse IMAGE_RESPONSE = new ImageResponse(
            123,
            "image.png",
            "Example image",
            "image/png",
            Instant.parse("2024-01-01T00:00:00Z"),
            "https://example.com/link/target",
            false,
            2097152
    );

    private static final ImageResponse IMAGE2_RESPONSE = new ImageResponse(
            124,
            "mobile.jpeg",
            null,
            "image/jpeg",
            Instant.parse("2024-02-02T00:00:00Z"),
            null,
            true,
            102400
    );

    @MockBean
    private MailingImageRestService restServiceMock;

    @Nested
    class PaginatedList {
        @Test
        void shouldReturn200() throws Exception {
            expect(adminMock.allowedAll(Permission.MAILING_CONTENT_SHOW)).andReturn(true);
            expect(restServiceMock.list(4, PageRequest.of(3, 2), adminMock)).andReturn(new PageResponse<>(
                    List.of(IMAGE_RESPONSE, IMAGE2_RESPONSE),
                    new PageResponse.PageMeta(3, 2, 50)
            ));
            Map<String, Object> params = Map.of(
                    "page", "3",
                    "size", "2"
            );
            authenticated(params(get("/mailings/{mailingId}/images", 4), params)).andExpectAll(
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
                                        "fileName": "image.png",
                                        "description": "Example image",
                                        "mimeType": "image/png",
                                        "creationDate": "2024-01-01T00:00:00Z",
                                        "link": "https://example.com/link/target",
                                        "mobile": false,
                                        "sizeBytes": 2097152
                                    },
                                    {
                                        "id": 124,
                                        "fileName": "mobile.jpeg",
                                        "mimeType": "image/jpeg",
                                        "creationDate": "2024-02-02T00:00:00Z",
                                        "mobile": true,
                                        "sizeBytes": 102400
                                    }
                                ]
                            }
                            """
                    )
            ).andDo(documentList());
        }

        @Test
        void shouldReturn400_whenMailingIdIsZero() throws Exception {
            authenticated(get("/mailings/{mailingId}/images", 0)).andExpectAll(
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
            expect(adminMock.allowedAll(Permission.MAILING_CONTENT_SHOW)).andReturn(false);
            assertNotAuthorized(get("/mailings/{mailingId}/images", 1));
        }
    }

    @Nested
    class FindById {
        @Test
        void shouldReturn200() throws Exception {
            expect(adminMock.allowedAll(Permission.MAILING_CONTENT_SHOW)).andReturn(true);
            expect(restServiceMock.find(123, 4, adminMock)).andReturn(IMAGE_RESPONSE);
            authenticated(get("/mailings/{mailingId}/images/{imageId}", 4, 123)).andExpectAll(
                    status().isOk(),
                    content().json(
                            """
                            {
                                "id": 123,
                                "fileName": "image.png",
                                "description": "Example image",
                                "mimeType": "image/png",
                                "creationDate": "2024-01-01T00:00:00Z",
                                "link": "https://example.com/link/target",
                                "mobile": false,
                                "sizeBytes": 2097152
                            }
                            """
                    )
            ).andDo(documentFindById());
        }

        @Test
        void shouldReturn400_whenMailingIdIsZero() throws Exception {
            authenticated(get("/mailings/{mailingId}/images/{imageId}", 0, 123)).andExpectAll(
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
        void shouldReturn400_whenImageIdIsZero() throws Exception {
            authenticated(get("/mailings/{mailingId}/images/{imageId}", 1, 0)).andExpectAll(
                    status().isBadRequest(),
                    content().json(
                            """
                            {
                                "status": 400,
                                "detail": "Invalid input.",
                                "errors": [
                                    {
                                        "field": "imageId",
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
            assertNotAuthorized(get("/mailings/{mailingId}/images/{imageId}", 1, 123));
        }
    }

    @Nested
    class FindByName {
        @Test
        void shouldReturn200() throws Exception {
            expect(adminMock.allowedAll(Permission.MAILING_CONTENT_SHOW)).andReturn(true);
            expect(restServiceMock.find("image.png", 4, adminMock)).andReturn(IMAGE_RESPONSE);
            authenticated(get("/mailings/{mailingId}/images/by-name?filename=image.png", 4)).andExpectAll(
                    status().isOk(),
                    content().json(
                            """
                            {
                                "id": 123,
                                "fileName": "image.png",
                                "description": "Example image",
                                "mimeType": "image/png",
                                "creationDate": "2024-01-01T00:00:00Z",
                                "link": "https://example.com/link/target",
                                "mobile": false,
                                "sizeBytes": 2097152
                            }
                            """
                    )
            ).andDo(documentFindByName());
        }

        @Test
        void shouldReturn400_whenMailingIdIsZero() throws Exception {
            authenticated(get("/mailings/{mailingId}/images/by-name?filename=image.png", 0)).andExpectAll(
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
        void shouldReturn400_whenFilenameBlank() throws Exception {
            authenticated(get("/mailings/{mailingId}/images/by-name?filename=", 4)).andExpectAll(
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
            expect(adminMock.allowedAll(Permission.MAILING_CONTENT_SHOW)).andReturn(false);
            assertNotAuthorized(get("/mailings/{mailingId}/images/by-name?filename=image.png", 123));
        }
    }

    @Nested
    class GetContentById {
        @Test
        void shouldReturn200() throws Exception {
            String disposition = ContentDisposition.inline()
                    .filename("image.png", StandardCharsets.UTF_8)
                    .build()
                    .toString();
            expect(adminMock.allowedAll(Permission.MAILING_CONTENT_SHOW)).andReturn(true);
            expect(restServiceMock.getContent(eq(123), eq(4), eq(adminMock), anyObject(), anyObject()))
                    .andReturn(ResponseEntity
                            .ok()
                            .header(HttpHeaders.CONTENT_DISPOSITION, disposition)
                            .body("image".getBytes())
                    );
            authenticated(get("/mailings/{mailingId}/images/{imageId}/content", 4, 123))
                    .andExpectAll(
                            status().isOk(),
                            content().contentType("application/octet-stream"),
                            header().string(HttpHeaders.CONTENT_DISPOSITION, disposition),
                            header().longValue(HttpHeaders.CONTENT_LENGTH, 5),
                            content().bytes("image".getBytes())
                    )
                    .andDo(documentGetContentById());
        }

        @Test
        void shouldReturn400_whenMailingIdIsZero() throws Exception {
            authenticated(get("/mailings/{mailingId}/images/{imageId}/content", 0, 123)).andExpectAll(
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
        void shouldReturn400_whenImageIdIsZero() throws Exception {
            authenticated(get("/mailings/{mailingId}/images/{imageId}/content", 1, 0)).andExpectAll(
                    status().isBadRequest(),
                    content().json(
                            """
                            {
                                "status": 400,
                                "detail": "Invalid input.",
                                "errors": [
                                    {
                                        "field": "imageId",
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
            assertNotAuthorized(get("/mailings/{mailingId}/images//{imageId}/content", 1, 123));
        }
    }

    @Nested
    class GetContentByName {
        @Test
        void shouldReturn200() throws Exception {
            String disposition = ContentDisposition.inline()
                    .filename("image.png", StandardCharsets.UTF_8)
                    .build()
                    .toString();
            expect(adminMock.allowedAll(Permission.MAILING_CONTENT_SHOW)).andReturn(true);
            expect(restServiceMock.getContent(eq("image.png"), eq(4), eq(adminMock), anyObject(), anyObject()))
                    .andReturn(ResponseEntity
                            .ok()
                            .header(HttpHeaders.CONTENT_DISPOSITION, disposition)
                            .body("image".getBytes())
                    );
            authenticated(get("/mailings/{mailingId}/images/by-name/content?filename=image.png", 4))
                    .andExpectAll(
                            status().isOk(),
                            content().contentType("application/octet-stream"),
                            header().string(HttpHeaders.CONTENT_DISPOSITION, disposition),
                            header().longValue(HttpHeaders.CONTENT_LENGTH, 5),
                            content().bytes("image".getBytes())
                    )
                    .andDo(documentGetContentByName());
        }

        @Test
        void shouldReturn400_whenMailingIdIsZero() throws Exception {
            authenticated(get("/mailings/{mailingId}/images/by-name/content?filename=image.png", 0)).andExpectAll(
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
            authenticated(get("/mailings/{mailingId}/images/by-name/content?filename=", 4)).andExpectAll(
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
            expect(adminMock.allowedAll(Permission.MAILING_CONTENT_SHOW)).andReturn(false);
            assertNotAuthorized(get("/mailings/{mailingId}/images/by-name/content?filename=image.png", 1));
        }
    }

    @Nested
    class Create {
        @Test
        void shouldReturn201() throws Exception {
            expect(adminMock.allowedAll(Permission.MAILING_COMPONENTS_CHANGE)).andReturn(true);
            var image = new MockMultipartFile("image", "mobile.png", "image/png", "image".getBytes());
            var request = new UploadMailingImageRequest(
                    "Mobile image",
                    "https://example.com/link/target",
                    "desktop.png",
                    image
            );
            var response = new ImageResponse(
                    124,
                    "mobile.png",
                    "Mobile image",
                    "image/png",
                    Instant.parse("2024-02-02T00:00:00Z"),
                    "https://example.com/link/target",
                    true,
                    102400
            );
            expect(restServiceMock.create(request, 4, adminMock)).andReturn(response);
            expectLastCall().times(1);

            authenticated(
                    multipart("/mailings/{mailingId}/images", 4)
                            .file(image)
                            .param("description", "Mobile image")
                            .param("linkTarget", "https://example.com/link/target")
                            .param("mobileVersionOf", "desktop.png"),
                    MediaType.MULTIPART_FORM_DATA
            )
                    .andExpectAll(status().isCreated(), content().json(
                                    """
                                    {
                                        "id": 124,
                                        "fileName": "mobile.png",
                                        "description": "Mobile image",
                                        "mimeType": "image/png",
                                        "creationDate": "2024-02-02T00:00:00Z",
                                        "link": "https://example.com/link/target",
                                        "mobile": true,
                                        "sizeBytes": 102400
                                    }
                                    """
                            )
                    )
                    .andDo(documentCreate());
        }

        @Test
        void shouldReturn400_whenMailingIdIsZero() throws Exception {
            var file = new MockMultipartFile("image", "image.png", "image/png", "image".getBytes());
            authenticated(multipart("/mailings/{mailingId}/images", 0).file(file), MediaType.MULTIPART_FORM_DATA)
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
            expect(adminMock.allowedAll(Permission.MAILING_COMPONENTS_CHANGE)).andReturn(false);
            var file = new MockMultipartFile("image", "image.png", "image/png", "image".getBytes());
            assertNotAuthorized(
                    multipart("/mailings/{mailingId}/images", 4).file(file),
                    MediaType.MULTIPART_FORM_DATA
            );
        }
    }

    @Nested
    class DeleteById {
        @Test
        void shouldReturn204() throws Exception {
            expect(adminMock.allowedAll(Permission.MAILING_COMPONENTS_CHANGE)).andReturn(true);
            restServiceMock.delete(123, 4, adminMock);
            expectLastCall().times(1);

            authenticated(delete("/mailings/{mailingId}/images/{imageId}", 4, 123))
                    .andExpect(status().isNoContent())
                    .andDo(documentDeleteById());
        }

        @Test
        void shouldReturn400_whenMailingIdIsZero() throws Exception {
            authenticated(delete("/mailings/{mailingId}/images/{imageId}", 0, 123)).andExpectAll(
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
        void shouldReturn400_whenImageIdIsZero() throws Exception {
            authenticated(delete("/mailings/{mailingId}/images/{imageId}", 1, 0)).andExpectAll(
                    status().isBadRequest(),
                    content().json(
                            """
                            {
                                "status": 400,
                                "detail": "Invalid input.",
                                "errors": [
                                    {
                                        "field": "imageId",
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
            expect(adminMock.allowedAll(Permission.MAILING_COMPONENTS_CHANGE)).andReturn(false);
            assertNotAuthorized(delete("/mailings/{mailingId}/images/{imageId}", 1, 123));
        }
    }

    @Nested
    class DeleteByName {
        @Test
        void shouldReturn204() throws Exception {
            expect(adminMock.allowedAll(Permission.MAILING_COMPONENTS_CHANGE)).andReturn(true);
            restServiceMock.delete("image.png", 4, adminMock);
            expectLastCall().times(1);

            authenticated(delete("/mailings/{mailingId}/images/by-name?filename=image.png", 4))
                    .andExpect(status().isNoContent())
                    .andDo(documentDeleteByName());
        }

        @Test
        void shouldReturn400_whenMailingIdIsZero() throws Exception {
            authenticated(delete("/mailings/{mailingId}/images/by-name?filename=image.png", 0)).andExpectAll(
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
        void shouldReturn400_whenFilenameBlank() throws Exception {
            authenticated(delete("/mailings/{mailingId}/images/by-name?filename=", 4)).andExpectAll(
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
            expect(adminMock.allowedAll(Permission.MAILING_COMPONENTS_CHANGE)).andReturn(false);
            assertNotAuthorized(delete("/mailings/{mailingId}/images/by-name?filename=image.png", 1));
        }
    }
}
