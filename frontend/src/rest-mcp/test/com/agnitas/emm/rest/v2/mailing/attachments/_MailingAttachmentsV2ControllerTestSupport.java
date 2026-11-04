/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2.mailing.attachments;

import static com.epages.restdocs.apispec.ResourceDocumentation.headerWithName;
import static com.epages.restdocs.apispec.ResourceSnippetParameters.builder;

import com.agnitas.emm.rest.v2.RestDocsTestSupport;
import com.agnitas.emm.rest.v2.infrastructure.config.json.RestEnumAllowedValues;
import com.epages.restdocs.apispec.ParameterDescriptorWithType;
import com.epages.restdocs.apispec.Schema;
import com.epages.restdocs.apispec.SimpleType;
import org.springframework.http.HttpHeaders;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.test.web.servlet.ResultHandler;

public abstract class _MailingAttachmentsV2ControllerTestSupport extends RestDocsTestSupport {

    private static final ParameterDescriptorWithType MAILING_ID_PATH_PARAM = intParam("mailingId", "Mailing ID");
    private static final ParameterDescriptorWithType FILENAME_PATH_PARAM = stringParam("filename", "Attachment filename");
    private static final ParameterDescriptorWithType ID_PATH_PARAM = intParam("attachmentId", "Attachment component ID");

    private static final ParameterDescriptorWithType[] BY_NAME_PATH_PARAMS = {
            MAILING_ID_PATH_PARAM,
            FILENAME_PATH_PARAM,
    };

    private static final ParameterDescriptorWithType[] BY_ID_PATH_PARAMS = {
            MAILING_ID_PATH_PARAM,
            ID_PATH_PARAM,
    };

    public static final FieldDescriptor[] ATTACHMENT_RESPONSE_FIELDS = {
            requiredField("id", "ID of the attachment"),
            requiredField("name", "File name of the attachment"),
            requiredField("targetId", "ID of the selected target group"),
            requiredField("originalSizeBytes", "Original size in raw bytes of the attachment"),
            requiredField("emailSizeBytes", "Size in raw bytes of the attachment in email"),
            requiredField("mimeType", "MIME type of the attachment")
    };

    protected final Schema responseSchema = schema("Response");

    ResultHandler documentList() {
        return documentPagination(ATTACHMENT_RESPONSE_FIELDS);
    }

    ResultHandler documentFindById() {
        return document("read-by-id", builder()
                .summary("Read mailing attachment file info by ID")
                .pathParameters(BY_ID_PATH_PARAMS)
                .responseSchema(responseSchema)
                .responseFields(ATTACHMENT_RESPONSE_FIELDS)
        );
    }

    ResultHandler documentFindByName() {
        return document("read-by-name", builder()
                .summary("Read mailing attachment file info by filename")
                .pathParameters(BY_NAME_PATH_PARAMS)
                .responseSchema(responseSchema)
                .responseFields(ATTACHMENT_RESPONSE_FIELDS)
        );
    }

    ResultHandler documentGetContentById() {
        return document("read-content-by-id", builder()
                .summary("Read mailing attachments file content by ID")
                .pathParameters(BY_ID_PATH_PARAMS)
                .responseHeaders(
                        headerWithName(HttpHeaders.CONTENT_TYPE).description("MIME type of the attachment"),
                        headerWithName(HttpHeaders.CONTENT_LENGTH).description("Attachment size in bytes"),
                        headerWithName(HttpHeaders.CONTENT_DISPOSITION).description("Attachment download information")
                )
        );
    }

    ResultHandler documentGetContentByName() {
        return document("read-content-by-name", builder()
                .summary("Read mailing attachments file content by filename")
                .pathParameters(BY_NAME_PATH_PARAMS)
                .responseHeaders(
                        headerWithName(HttpHeaders.CONTENT_TYPE).description("MIME type of the attachment"),
                        headerWithName(HttpHeaders.CONTENT_LENGTH).description("Attachment size in bytes"),
                        headerWithName(HttpHeaders.CONTENT_DISPOSITION).description("Attachment download information")
                )
        );
    }

    ResultHandler documentCreate() {
        return document("create", builder()
                .summary("Upload mailing attachment")
                .description("Creates an attachment for a mailing.")
                .pathParameters(MAILING_ID_PATH_PARAM)
                .responseSchema(responseSchema)
                .responseFields(ATTACHMENT_RESPONSE_FIELDS)
                .formParameters(
                        formParam("name", "Display name of the attachment"),
                        formParam("type", RestEnumAllowedValues.ATTACHMENT_TYPES),
                        positive(formParam("targetId", "Restricts the attachment to a specific target group"))
                                .type(SimpleType.NUMBER),
                        required(fileParam("attachment", "The file to be uploaded")),
                        fileParam("pdfBackgroundFile", "PDF background file")
                )
        );
    }

    ResultHandler documentDeleteById() {
        return document("delete-by-id", builder()
                .summary("Delete single mailing attachment by ID")
                .pathParameters(BY_ID_PATH_PARAMS)
        );
    }

    ResultHandler documentDeleteByName() {
        return document("delete-by-name", builder()
                .summary("Delete single mailing attachment by filename")
                .pathParameters(BY_NAME_PATH_PARAMS)
        );
    }

    @Override
    protected String getDocsTag() {
        return "Mailing Attachment";
    }

    @Override
    protected String getDocsIdentifierPrefix() {
        return "mailing-attachments";
    }
}
