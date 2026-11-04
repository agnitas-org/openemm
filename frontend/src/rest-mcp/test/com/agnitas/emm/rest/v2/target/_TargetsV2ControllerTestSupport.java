/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2.target;

import static com.epages.restdocs.apispec.ResourceSnippetParameters.builder;

import com.agnitas.emm.core.target.validation.TargetGroupConstraints;
import com.agnitas.emm.rest.v2.ConstrainedFields;
import com.agnitas.emm.rest.v2.RestDocsTestSupport;
import com.agnitas.emm.rest.v2.infrastructure.config.json.RestEnumAllowedValues;
import com.epages.restdocs.apispec.ParameterDescriptorWithType;
import com.epages.restdocs.apispec.Schema;
import com.epages.restdocs.apispec.SimpleType;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.test.web.servlet.ResultHandler;

public abstract class _TargetsV2ControllerTestSupport extends RestDocsTestSupport {

    private static final ConstrainedFields CREATE_CONSTRAINTS = new ConstrainedFields(CreateTargetRequest.class);
    private static final ConstrainedFields PATCH_CONSTRAINTS = new ConstrainedFields(PatchTargetRequest.class);
    private static final ParameterDescriptorWithType INCLUDE_PARAM =
            stringParam("include", RestEnumAllowedValues.TARGET_INCLUDE_FIELDS).optional();

    private static final FieldDescriptor[] TARGET_LIGHT_RESPONSE_FIELDS = {
            requiredField("id", "Unique ID of the target group"),
            requiredField("name", "Name of target group"),
            optionalField("description", "Description of target group"),
            optionalField("recipientCount", "Number of recipients in this target group").type(SimpleType.STRING)
    };

    public static final FieldDescriptor[] TARGET_RESPONSE_FIELDS = fields(
            TARGET_LIGHT_RESPONSE_FIELDS,
            requiredField("sql", "Definition of target in SQL syntax"),
            requiredField("eql", "Definition of target in SQL syntax"),
            requiredField("valid", "Whether target is valid"),
            requiredField("creationDate", "Target creation date"),
            requiredField("changeDate", "Target change date"),
            optionalField("isAccessLimiting", "Is Access Limiting Target Group").type(SimpleType.BOOLEAN)
    );

    private static final FieldDescriptor[] CREATE_REQUEST_FIELDS = {
            requiredField(CREATE_CONSTRAINTS, "name", "Name of target group"),
            optionalField(CREATE_CONSTRAINTS, "description", "Description of target group"),
            requiredField(CREATE_CONSTRAINTS, "eql", "Definition of target in SQL syntax"),
    };

    private static final FieldDescriptor[] PATCH_REQUEST_FIELDS = {
            requiredField(PATCH_CONSTRAINTS, "name", "Name of target group"),
            optionalField(PATCH_CONSTRAINTS, "description", "Description of target group"),
            requiredField(PATCH_CONSTRAINTS, "eql", "Definition of target in SQL syntax"),
    };

    private final Schema responseSchema = schema("Response");

    @Override
    protected String getDocsTag() {
        return "Target Group";
    }

    @Override
    protected String getDocsIdentifierPrefix() {
        return "targets";
    }

    protected ResultHandler documentList() {
        return documentPagination(
                TARGET_LIGHT_RESPONSE_FIELDS,
                INCLUDE_PARAM,
                optionalParam(
                        "size",
                        """
                        Number of items per page. \
                        Must not exceed %d when 'recipientCount' is requested via 'include'."""
                                .formatted(TargetGroupConstraints.MAX_PAGE_SIZE_WITH_RECIPIENT_COUNT)
                )
        );
    }

    protected ResultHandler documentCreate() {
        return document("create", builder()
                .summary("Create a new target group entry")
                .requestSchema(schema("CreateRequest"))
                .requestFields(CREATE_REQUEST_FIELDS)
                .responseSchema(responseSchema)
                .responseFields(TARGET_RESPONSE_FIELDS)
        );
    }

    protected ResultHandler documentRead() {
        return document("read", builder()
                .summary("Read target group data")
                .pathParameters(getIdParam())
                .queryParameters(INCLUDE_PARAM)
                .responseSchema(responseSchema)
                .responseFields(TARGET_RESPONSE_FIELDS)
        );
    }

    protected ResultHandler documentPatch() {
        return documentPatch(builder()
                .summary("Partially update target group data")
                .pathParameters(getIdParam())
                .requestSchema(schema("PatchRequest"))
                .requestFields(PATCH_REQUEST_FIELDS)
                .responseSchema(responseSchema)
                .responseFields(TARGET_RESPONSE_FIELDS)
        );
    }

    protected ResultHandler documentDelete() {
        return document("delete", builder()
                .summary("Delete single target group entry")
                .pathParameters(getIdParam())
        );
    }
}
