/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2.trigger;

import static com.epages.restdocs.apispec.ResourceSnippetParameters.builder;

import java.util.stream.Stream;

import com.agnitas.emm.core.action.operations.ActionOperationType;
import com.agnitas.emm.rest.v2.ConstrainedFields;
import com.agnitas.emm.rest.v2.RestDocsTestSupport;
import com.agnitas.emm.rest.v2.infrastructure.config.json.RestEnumAllowedValues;
import com.epages.restdocs.apispec.Schema;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultHandler;

public abstract class _TriggersV2ControllerTestSupport extends RestDocsTestSupport {

    private static final ConstrainedFields CREATE_CONSTRAINTS = new ConstrainedFields(CreateTriggerRequest.class);
    private static final ConstrainedFields PATCH_CONSTRAINTS = new ConstrainedFields(PatchTriggerRequest.class);

    private static final FieldDescriptor[] TRIGGER_LIGHT_RESPONSE_FIELDS = {
            requiredField("id", "Unique ID of the trigger"),
            requiredField("name", "Name of trigger"),
            optionalField("description", "Description of trigger"),
            requiredField("type", "Trigger type code: 0(Link), 1(Form), 9(All)"),
            requiredField("isActive", "Whether trigger is active or not"),
            requiredField("isAdvertising", "Whether trigger is advertising or not"),
            requiredField("creationDate", "Trigger creation date"),
            requiredField("changeDate", "Trigger change date")
    };

    private static final FieldDescriptor[] AO_EXAMPLE_FIELDS = {
            requiredField("id", "Action operation ID"),
            requiredField("actionId", "Trigger ID"),
            requiredField("companyId", "Company ID"),
            optionalField("doubleCheck", "Checks, if recipient already exists"),
            optionalField("doubleOptIn", "If state is set to DOI - waiting for confirm (5)"),
            requiredField("keyColumn", "Column double_check, e.g. email"),
            requiredField("operationType", "Defines type"),
            requiredField("readonly", "If trigger operation is readonly"),
    };

    private static final FieldDescriptor[] TRIGGER_RESPONSE_FIELDS = fields(
            TRIGGER_LIGHT_RESPONSE_FIELDS,
            requiredField("operations", "List of trigger operations").type(JsonFieldType.ARRAY),
            Stream.of(applyPrefix("operations[]", AO_EXAMPLE_FIELDS))
                    .map(FieldDescriptor::optional)
                    .toArray(FieldDescriptor[]::new)
    );

    private static final FieldDescriptor[] CREATE_REQUEST_FIELDS = fields(
            requiredField(CREATE_CONSTRAINTS, "name", "Name of trigger"),
            optionalField(CREATE_CONSTRAINTS, "description", "Comment on trigger"),
            requiredField(CREATE_CONSTRAINTS, "type", RestEnumAllowedValues.TRIGGER_TYPES),
            optionalField(CREATE_CONSTRAINTS, "isActive", "Whether trigger is active or not"),
            optionalField(CREATE_CONSTRAINTS,
                    "isAdvertising",
                    "If set to true - will no be executed for the recipient with the tracking veto flag"
            ),
            optionalField(CREATE_CONSTRAINTS, "operations", "List of trigger operations"),
            applyPrefix("operations[]",
                    optionalField(CREATE_CONSTRAINTS, "doubleCheck", "Checks, if recipient already exists"),
                    optionalField(CREATE_CONSTRAINTS,
                            "doubleOptIn",
                            "If state is set to DOI - waiting for confirm (5)"
                    ),
                    requiredField(CREATE_CONSTRAINTS, "keyColumn", "Column double_check, e.g. email"),
                    requiredField(CREATE_CONSTRAINTS,
                            "type",
                            "Allowed values: %s".formatted(ActionOperationType.getAllowedValuesStr())
                    )
            )
    );

    private static final FieldDescriptor[] PATCH_REQUEST_FIELDS = fields(
            optionalField(PATCH_CONSTRAINTS, "name", "Name of trigger"),
            optionalField(PATCH_CONSTRAINTS, "description", "Comment on trigger"),
            optionalField(PATCH_CONSTRAINTS, "type", RestEnumAllowedValues.TRIGGER_TYPES),
            optionalField(PATCH_CONSTRAINTS, "isActive", "Whether trigger is active or not"),
            optionalField(PATCH_CONSTRAINTS,
                    "isAdvertising",
                    "If set to true - will no be executed for the recipient with the tracking veto flag"
            ),
            optionalField(PATCH_CONSTRAINTS, "operations", "List of trigger operations"),
            applyPrefix("operations[]",
                    optionalField(PATCH_CONSTRAINTS, "doubleCheck", "Checks, if recipient already exists"),
                    optionalField(PATCH_CONSTRAINTS, "doubleOptIn", "If state is set to DOI - waiting for confirm (5)"),
                    optionalField(PATCH_CONSTRAINTS, "keyColumn", "Column double_check, e.g. email"),
                    optionalField(PATCH_CONSTRAINTS,
                            "type",
                            "Allowed values: %s".formatted(ActionOperationType.getAllowedValuesStr())
                    )
            )
    );

    private final Schema responseSchema = schema("Response");

    @Override
    protected String getDocsTag() {
        return "Trigger";
    }

    @Override
    protected String getDocsIdentifierPrefix() {
        return "triggers";
    }

    protected ResultHandler documentList() {
        return documentPagination(TRIGGER_LIGHT_RESPONSE_FIELDS);
    }

    protected ResultHandler documentRead() {
        return document("read", builder()
                .summary("Read trigger data")
                .pathParameters(getIdParam())
                .responseSchema(responseSchema)
                .responseFields(TRIGGER_RESPONSE_FIELDS)
        );
    }

    protected ResultHandler documentCreate() {
        return document("create", builder()
                .summary("Create a new trigger entry")
                .requestSchema(schema("CreateRequest"))
                .requestFields(CREATE_REQUEST_FIELDS)
                .responseSchema(responseSchema)
                .responseFields(TRIGGER_RESPONSE_FIELDS)
        );
    }

    protected ResultHandler documentPatch() {
        return documentPatch(builder()
                .summary("Partially update trigger data")
                .pathParameters(getIdParam())
                .requestSchema(schema("PatchRequest"))
                .requestFields(PATCH_REQUEST_FIELDS)
                .responseSchema(responseSchema)
                .responseFields(TRIGGER_RESPONSE_FIELDS)
        );
    }

    protected ResultHandler documentDelete() {
        return document("delete", builder()
                .summary("Delete single trigger")
                .pathParameters(getIdParam())
        );
    }

    protected ResultHandler documentExecute() {
        return document("execute", builder()
                .summary("Execute a trigger by ID")
                .requestFields(optionalField("triggerRequestParam", "Action request parameter"))
                .requestSchema(schema("executionRequest"))
                .pathParameters(getIdParam())
        );
    }
}
