/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2.forms;

import static com.epages.restdocs.apispec.ResourceSnippetParameters.builder;

import com.agnitas.emm.rest.v2.ConstrainedFields;
import com.agnitas.emm.rest.v2.RestDocsTestSupport;
import com.epages.restdocs.apispec.Schema;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.test.web.servlet.ResultHandler;

public abstract class _FormsV2ControllerTestSupport extends RestDocsTestSupport {

    private static final ConstrainedFields CREATE_CONSTRAINTS = new ConstrainedFields(CreateFormRequest.class);
    private static final ConstrainedFields PATCH_CONSTRAINTS = new ConstrainedFields(PatchFormRequest.class);

    private static final FieldDescriptor[] FROM_RESULT_SETTINGS_FIELDS = {
            requiredField("template", "Template HTML content"),
            optionalField("url", "Redirect URL"),
            requiredField("useUrl", "Whether to use URL redirect"),
    };

    private static final FieldDescriptor[] FORM_LIGHT_RESPONSE_FIELDS = {
            requiredField("id", "Unique ID of the form"),
            requiredField("name", "Name of the form"),
            optionalField("description", "Description of the form"),
    };

    private static final FieldDescriptor[] FORM_RESPONSE_FIELDS = fields(
            FORM_LIGHT_RESPONSE_FIELDS,
            requiredField("startTriggerId", "Trigger ID on form submission success"),
            requiredField("endTriggerId", "Trigger ID on form submission error"),
            requiredField("successSettings", "Settings on success"),
            requiredField("errorSettings", "Settings on error"),
            optionalField("active", "Whether the form is active"),
            optionalField("creationDate", "Form creation timestamp"),
            optionalField("changeDate", "Form last modification timestamp"),
            applyPrefix("successSettings", FROM_RESULT_SETTINGS_FIELDS),
            applyPrefix("errorSettings", FROM_RESULT_SETTINGS_FIELDS)
    );

    private static final FieldDescriptor[] CREATE_REQUEST_FIELDS = fields(
            requiredField(CREATE_CONSTRAINTS, "name", "Name of the form (max 50 characters)"),
            requiredField(CREATE_CONSTRAINTS, "description", "Description of the form"),
            optionalField(CREATE_CONSTRAINTS, "startTriggerId", "To be executed on form start"),
            optionalField(CREATE_CONSTRAINTS, "endTriggerId", "To be executed on form end"),
            optionalField(CREATE_CONSTRAINTS, "successSettings", "Settings on success"),
            optionalField(CREATE_CONSTRAINTS, "errorSettings", "Settings on error"),
            applyPrefix("successSettings", FROM_RESULT_SETTINGS_FIELDS),
            applyPrefix("errorSettings", FROM_RESULT_SETTINGS_FIELDS)
    );

    private static final FieldDescriptor[] PATCH_REQUEST_FIELDS = fields(
            requiredField(PATCH_CONSTRAINTS, "name", "Name of the form (max 50 characters)"),
            optionalField(PATCH_CONSTRAINTS, "description", "Description of the form"),
            optionalField(PATCH_CONSTRAINTS, "startTriggerId", "To be executed on form start"),
            optionalField(PATCH_CONSTRAINTS, "endTriggerId", "To be executed on form end"),
            optionalField(PATCH_CONSTRAINTS, "successSettings", "Settings on success"),
            optionalField(PATCH_CONSTRAINTS, "errorSettings", "Settings on error"),
            applyPrefix("successSettings", FROM_RESULT_SETTINGS_FIELDS),
            applyPrefix("errorSettings", FROM_RESULT_SETTINGS_FIELDS)
    );

    private final Schema responseSchema = schema("Response");

    @Override
    protected String getDocsTag() {
        return "Form";
    }

    @Override
    protected String getDocsIdentifierPrefix() {
        return "forms";
    }

    protected ResultHandler documentList() {
        return documentPagination(FORM_LIGHT_RESPONSE_FIELDS);
    }

    protected ResultHandler documentCreate() {
        return document("create", builder()
                .summary("Create a new form entry")
                .requestSchema(schema("CreateRequest"))
                .requestFields(CREATE_REQUEST_FIELDS)
                .responseSchema(responseSchema)
                .responseFields(FORM_RESPONSE_FIELDS)
        );
    }

    protected ResultHandler documentRead() {
        return document("read", builderWithIdParam()
                .summary("Read form data")
                .responseSchema(responseSchema)
                .responseFields(FORM_RESPONSE_FIELDS)
        );
    }

    protected ResultHandler documentPatch() {
        return documentPatch(builderWithIdParam()
                .summary("Update a form entry")
                .requestSchema(schema("PatchRequest"))
                .requestFields(PATCH_REQUEST_FIELDS)
                .responseSchema(responseSchema)
                .responseFields(FORM_RESPONSE_FIELDS)
        );
    }

    protected ResultHandler documentDelete() {
        return document("delete", builderWithIdParam()
                .summary("Delete single form entry")
        );
    }

    protected ResultHandler documentActivate() {
        return documentExtendedScope("activate", builderWithIdParam()
                .summary("Activate form")
                .responseSchema(responseSchema)
                .responseFields(FORM_RESPONSE_FIELDS)
        );
    }

    protected ResultHandler documentDeactivate() {
        return documentExtendedScope("deactivate", builderWithIdParam()
                .summary("Deactivate form")
                .responseSchema(responseSchema)
                .responseFields(FORM_RESPONSE_FIELDS)
        );
    }
}
