/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2.mailing.images;

import static com.epages.restdocs.apispec.ResourceDocumentation.headerWithName;
import static com.epages.restdocs.apispec.ResourceSnippetParameters.builder;

import com.agnitas.emm.rest.v2.RestDocsTestSupport;
import com.epages.restdocs.apispec.ParameterDescriptorWithType;
import com.epages.restdocs.apispec.Schema;
import org.springframework.http.HttpHeaders;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.test.web.servlet.ResultHandler;

public abstract class _MailingImagesV2ControllerTestSupport extends RestDocsTestSupport {

    private static final ParameterDescriptorWithType MAILING_ID_PATH_PARAM = intParam("mailingId", "Mailing ID");
    private static final ParameterDescriptorWithType ID_PATH_PARAM = intParam("imageId", "Image component ID");
    private static final ParameterDescriptorWithType FILENAME_REQUEST_PARAM = stringParam("filename", "Image filename");

    private static final ParameterDescriptorWithType[] BY_ID_PATH_PARAMS = {
            MAILING_ID_PATH_PARAM,
            ID_PATH_PARAM,
    };

    public static final FieldDescriptor[] IMAGE_RESPONSE_FIELDS = fields(
            requiredField("id", "Component ID of the image"),
            requiredField("fileName", "File name of the image"),
            optionalField("description", "Description of the image"),
            requiredField("mimeType", "MIME type of the image"),
            requiredField("creationDate", "Creation timestamp"),
            optionalField("link", "URL of the image"),
            requiredField("mobile", "Whether the image is used for mobile"),
            requiredField("sizeBytes", "Size in raw bytes of the image")
    );

    protected final Schema responseSchema = schema("Response");

    ResultHandler documentList() {
        return documentPagination(IMAGE_RESPONSE_FIELDS);
    }

    ResultHandler documentFindById() {
        return document("read-by-Id", builder()
                .summary("Get mailing image base info by component ID")
                .pathParameters(BY_ID_PATH_PARAMS)
                .responseSchema(responseSchema)
                .responseFields(IMAGE_RESPONSE_FIELDS)
        );
    }

    ResultHandler documentFindByName() {
        return document("read-by-name", builder()
                .summary("Get mailing image base info by file name")
                .pathParameters(MAILING_ID_PATH_PARAM)
                .queryParameters(FILENAME_REQUEST_PARAM)
                .responseSchema(responseSchema)
                .responseFields(IMAGE_RESPONSE_FIELDS)
        );
    }

    ResultHandler documentGetContentById() {
        return document("read-content-by-id", builder()
                .summary("Read mailing image content data by component ID")
                .pathParameters(BY_ID_PATH_PARAMS)
                .responseHeaders(
                        headerWithName(HttpHeaders.CONTENT_TYPE).description("Image MIME type"),
                        headerWithName(HttpHeaders.CONTENT_LENGTH).description("Image size in bytes"),
                        headerWithName(HttpHeaders.CONTENT_DISPOSITION).description("Image download information")
                )
        );
    }

    ResultHandler documentGetContentByName() {
        return document("read-content-by-name", builder()
                .summary("Read mailing image content data by file name")
                .pathParameters(MAILING_ID_PATH_PARAM)
                .queryParameters(FILENAME_REQUEST_PARAM)
                .responseHeaders(
                        headerWithName(HttpHeaders.CONTENT_TYPE).description("Image MIME type"),
                        headerWithName(HttpHeaders.CONTENT_LENGTH).description("Image size in bytes"),
                        headerWithName(HttpHeaders.CONTENT_DISPOSITION).description("Image download information")
                )
        );
    }

    ResultHandler documentCreate() {
        return document("create", builder()
                .summary("Upload mailing image")
                .description("Creates an image for a mailing.")
                .pathParameters(MAILING_ID_PATH_PARAM)
                .responseSchema(responseSchema)
                .responseFields(IMAGE_RESPONSE_FIELDS)
                .formParameters(
                        required(fileParam("image", "The image file to upload")),
                        formParam("description", "Image description"),
                        formParam("linkTarget", "Link target for image"),
                        formParam(
                                "mobileVersionOf",
                                "The name of the image for which this image is used as the mobile version."
                        )
                )
        );
    }

    ResultHandler documentDeleteById() {
        return document("delete-by-id", builder()
                .summary("Delete single mailing image by component ID")
                .pathParameters(BY_ID_PATH_PARAMS)
        );
    }

    ResultHandler documentDeleteByName() {
        return document("delete-by-name", builder()
                .summary("Delete single mailing image by image filename")
                .pathParameters(MAILING_ID_PATH_PARAM)
                .queryParameters(FILENAME_REQUEST_PARAM)
        );
    }

    @Override
    protected String getDocsTag() {
        return "Mailing Image";
    }

    @Override
    protected String getDocsIdentifierPrefix() {
        return "mailing-images";
    }
}
