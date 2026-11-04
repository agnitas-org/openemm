/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2.mailing.content;

import static com.epages.restdocs.apispec.ResourceSnippetParameters.builder;
import static com.epages.restdocs.apispec.SimpleType.NUMBER;

import com.agnitas.emm.rest.v2.ConstrainedFields;
import com.agnitas.emm.rest.v2.RestDocsTestSupport;
import com.epages.restdocs.apispec.ParameterDescriptorWithType;
import com.epages.restdocs.apispec.Schema;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.test.web.servlet.ResultHandler;

public abstract class _MailingContentV2ControllerTestSupport extends RestDocsTestSupport {

    private static final ParameterDescriptorWithType MAILING_ID_PATH_PARAM = intParam("mailingId", "Mailing ID");
    private static final ParameterDescriptorWithType CONTENT_ID_PATH_PARAM = intParam("contentId", "Content ID");
    private static final ParameterDescriptorWithType CONTENT_NAME_PATH_PARAM = stringParam(
            "contentName",
            "Content name"
    );

    private static final ConstrainedFields CONTENT_UPDATE_CONSTRAINTS = new ConstrainedFields(PatchMailingContentRequest.class);
    private static final ConstrainedFields TARGET_CONTENT_UPDATE_CONSTRAINTS = new ConstrainedFields(
            TargetContentUpsertRequest.class);

    private static final String DELETE_WARN =
            """
            > ⚠️ **Warning:** Deleting content block with this endpoint does **not** automatically remove \
            the corresponding `[agnDYN name="Name" /]` tag from your HTML/Text templates in the frame content.
            >
            > The corresponding block will no longer be visible in the *Content blocks* tab on UI.
            """;

    private static final FieldDescriptor[] MAILING_TARGET_CONTENT_RESPONSE_FIELDS = {
            requiredField("id", "Target content ID"),
            optionalField("targetId", "ID of the target group").type(NUMBER),
            requiredField("order", "Display order"),
            requiredField("text", "Content text"),
    };

    public static final FieldDescriptor[] MAILING_CONTENT_RESPONSE_FIELDS = fields(
            requiredField("id", "Component ID"),
            requiredField("name", "Component name"),
            requiredField("disableLinkExtension", "Disable link extension flag"),
            requiredField("content", "List of mailing target content items"),
            applyPrefix("content[]", MAILING_TARGET_CONTENT_RESPONSE_FIELDS)
    );

    public static final FieldDescriptor[] TARGET_CONTENT_REQUEST_FIELDS = {
            requiredField(TARGET_CONTENT_UPDATE_CONSTRAINTS, "targetId", "ID of the target group"),
            requiredField(TARGET_CONTENT_UPDATE_CONSTRAINTS, "order", "Display order"),
            requiredField(TARGET_CONTENT_UPDATE_CONSTRAINTS, "text", "Content text")
    };

    private static final FieldDescriptor[] UPDATE_REQUEST_FIELDS = fields(
            optionalField(CONTENT_UPDATE_CONSTRAINTS, "disableLinkExtension", "Disable link extension flag"),
            optionalField(CONTENT_UPDATE_CONSTRAINTS, "content", "Array of the content elements for target groups"),
            applyPrefix("content[]", TARGET_CONTENT_REQUEST_FIELDS)
    );

    private final Schema responseSchema = schema("Response");

    @Override
    protected String getDocsTag() {
        return "Mailing Content";
    }

    @Override
    protected String getDocsIdentifierPrefix() {
        return "mailing-contents";
    }

    ResultHandler documentList() {
        return document("list", builder()
                .summary("Get list of the Mailing contents")
                .responseSchema(schema("ListResponse"))
                .responseFields(applyPrefix("[]", MAILING_CONTENT_RESPONSE_FIELDS))
        );
    }

    ResultHandler documentReadById() {
        return document("read-by-id", builder()
                .summary("Read mailing content by ID")
                .pathParameters(MAILING_ID_PATH_PARAM, CONTENT_ID_PATH_PARAM)
                .responseSchema(responseSchema)
                .responseFields(MAILING_CONTENT_RESPONSE_FIELDS)
        );
    }

    ResultHandler documentReadByName() {
        return document("read-by-name", builderWithIdParam()
                .summary("Read mailing content by name")
                .pathParameters(MAILING_ID_PATH_PARAM, CONTENT_NAME_PATH_PARAM)
                .responseSchema(responseSchema)
                .responseFields(MAILING_CONTENT_RESPONSE_FIELDS)
        );
    }

    protected ResultHandler documentDeleteById() {
        return document("delete-by-id", builder()
                .summary("Delete single mailing content by ID")
                .pathParameters(MAILING_ID_PATH_PARAM, CONTENT_ID_PATH_PARAM)
                .description(DELETE_WARN)
        );
    }

    protected ResultHandler documentDeleteByName() {
        return document("delete-by-name", builder()
                .summary("Delete single mailing content by name")
                .pathParameters(MAILING_ID_PATH_PARAM, CONTENT_NAME_PATH_PARAM)
                .description(DELETE_WARN)
        );
    }

    protected ResultHandler documentPatchById() {
        return documentPatch("patch-by-id", CONTENT_ID_PATH_PARAM);
    }

    protected ResultHandler documentPatchByName() {
        return documentPatch("patch-by-name", CONTENT_NAME_PATH_PARAM);
    }

    private ResultHandler documentPatch(String docId, ParameterDescriptorWithType contentPathParam) {
        return document(docId, builder()
                .summary("Partially update mailing content")
                .pathParameters(MAILING_ID_PATH_PARAM, contentPathParam)
                .requestSchema(schema("PatchRequest"))
                .requestFields(UPDATE_REQUEST_FIELDS)
                .responseSchema(responseSchema)
                .responseFields(MAILING_CONTENT_RESPONSE_FIELDS)
        );
    }
}
