/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2.binding;

import static com.agnitas.emm.rest.v2.infrastructure.config.json.RestEnumAllowedValues.MEDIATYPES;
import static com.agnitas.emm.rest.v2.infrastructure.config.json.RestEnumAllowedValues.USER_STATUSES;
import static com.agnitas.emm.rest.v2.infrastructure.config.json.RestEnumAllowedValues.USER_TYPES;
import static com.epages.restdocs.apispec.ResourceSnippetParameters.builder;

import com.agnitas.emm.rest.v2.ConstrainedFields;
import com.agnitas.emm.rest.v2.RestDocsTestSupport;
import com.epages.restdocs.apispec.ParameterDescriptorWithType;
import com.epages.restdocs.apispec.Schema;
import com.epages.restdocs.apispec.SimpleType;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.test.web.servlet.ResultHandler;

public abstract class _BindingsV2ControllerTestSupport extends RestDocsTestSupport {

    private static final ConstrainedFields CREATE_CONSTRAINTS = new ConstrainedFields(CreateBindingRequest.class);
    private static final ConstrainedFields UPDATE_CONSTRAINTS = new ConstrainedFields(PatchBindingRequest.class);
    private static final ConstrainedFields BULK_DELETE_CONSTRAINTS = new ConstrainedFields(BulkDeleteBindingRequest.class);

    private static final ParameterDescriptorWithType RECIPIENT_ID_PATH_PARAM = intParam("recipientId", "Recipient ID");
    private static final ParameterDescriptorWithType MAILINGLIST_ID_PATH_PARAM = intParam("mailinglistId", "Mailinglist ID");
    private static final ParameterDescriptorWithType MEDIATYPE_PATH_PARAM = intParam("mediaType", MEDIATYPES).optional();

    private static final ParameterDescriptorWithType[] IDENTIFIER_PATH_PARAMS = {
            RECIPIENT_ID_PATH_PARAM,
            MAILINGLIST_ID_PATH_PARAM,
            MEDIATYPE_PATH_PARAM
    };

    private static final FieldDescriptor[] BINDING_RESPONSE_FIELDS = {
            requiredField("recipientId", "Unique ID of the recipient"),
            requiredField("mailinglistId", "ID of the referenced mailinglist"),
            requiredField("mediaType", "Media type of subscription"),
            requiredField("creationDate", "Date-time of creation (ISO-8601)"),
            requiredField("changeDate", "Date-time of change (ISO-8601)"),
            optionalField("userType", "Subscription recipient type"),
            optionalField("userStatus", "Subscription status of recipient"),
            optionalField("userRemark", "Descriptive text for this binding entry"),
            optionalField("referrer", "Http referrer for later analysis"),
            optionalField("entryMailingId", "Mailing ID which lead to this subscription"),
            optionalField("exitMailingId", "Mailing ID which lead to this unsubscription"),
            optionalField("actionExecutionInfo", "If action executed, started or execution error message").type(SimpleType.STRING),
    };

    private static final ConstrainedFields BULK_UPSERT_CONSTRAINTS = new ConstrainedFields(BulkUpsertBindingRequest.class);

    private static final FieldDescriptor[] BULK_DELETE_REQUEST_FIELDS = {
            requiredField(BULK_DELETE_CONSTRAINTS, "recipientIds", "Recipient IDs to delete subscriptions for"),
            requiredField(BULK_DELETE_CONSTRAINTS, "mailinglistIds", "Mailinglist IDs to delete subscriptions for"),
            optionalField(BULK_DELETE_CONSTRAINTS, "mediaTypes", MEDIATYPES)
    };

    private static final FieldDescriptor[] BULK_UPSERT_REQUEST_FIELDS = {
            requiredField(BULK_UPSERT_CONSTRAINTS, "recipientIds", "Recipient IDs to upsert bindings for"),
            requiredField(BULK_UPSERT_CONSTRAINTS, "mailinglistIds", "Mailinglist IDs to upsert bindings for"),
            requiredField(BULK_UPSERT_CONSTRAINTS, "mediaTypes", MEDIATYPES),
            requiredField(BULK_UPSERT_CONSTRAINTS, "status", USER_STATUSES)
    };

    private static final FieldDescriptor[] BULK_UPSERT_RESPONSE_FIELDS = {
            requiredField("total", "Total number of upsert operations performed"),
            requiredField("notChanged", "Number of bindings that were already up-to-date"),
            requiredField("inserted", "Number of new bindings created"),
            requiredField("updated", "Number of existing bindings updated"),
            requiredField("results", "Root object containing the nested results map"),
            requiredField("results.*", "Map grouped by Media Type"),
            requiredField("results.*.*", "Map grouped by Mailinglist ID"),
            requiredField("results.*.*.*", "Upsert status ('inserted', 'updated', or 'not changed') mapped by Recipient ID")
    };

    private static final FieldDescriptor[] CREATE_REQUEST_FIELDS = {
            requiredField(CREATE_CONSTRAINTS, "mailinglistId", "Id of the referenced mailinglist"),
            requiredField(CREATE_CONSTRAINTS, "recipientId", "Numeric ID of the recipient to create binding for"),
            optionalField(CREATE_CONSTRAINTS, "mediaType", MEDIATYPES),
            requiredField(CREATE_CONSTRAINTS, "userStatus", USER_STATUSES),
            optionalField(CREATE_CONSTRAINTS, "userType", USER_TYPES),
            optionalField(CREATE_CONSTRAINTS, "userRemark", "Descriptive text for this binding entry"),
            optionalField(CREATE_CONSTRAINTS, "referrer", "Http referrer for later analysis"),
            optionalField(CREATE_CONSTRAINTS, "entryMailingId", "Mailing ID which lead to this subscription"),
            optionalField(CREATE_CONSTRAINTS, "exitMailingId", "Mailing ID which lead to this unsubscription"),
            optionalField(CREATE_CONSTRAINTS, "triggerId", "Trigger id to be executed after creation oder update of binding item"),
            optionalField(CREATE_CONSTRAINTS, "runActionAsynchronous", "Optional setting to run action asynchronous"),
    };

    private static final FieldDescriptor[] UPDATE_REQUEST_FIELDS = {
            optionalField(UPDATE_CONSTRAINTS, "userStatus", USER_STATUSES),
            optionalField(UPDATE_CONSTRAINTS, "userType", USER_TYPES),
            optionalField(UPDATE_CONSTRAINTS, "userRemark", "Descriptive text for this binding entry"),
            optionalField(UPDATE_CONSTRAINTS, "referrer", "Http referrer for later analysis"),
            optionalField(UPDATE_CONSTRAINTS, "entryMailingId", "Mailing ID which lead to this subscription"),
            optionalField(UPDATE_CONSTRAINTS, "exitMailingId", "Mailing ID which lead to this unsubscription")
    };

    private final Schema responseSchema = schema("Response");

    @Override
    protected String getDocsTag() {
        return "Binding";
    }

    @Override
    protected String getDocsIdentifierPrefix() {
        return "bindings";
    }

    ResultHandler documentList() {
        return documentPagination(
                BINDING_RESPONSE_FIELDS,
                optionalParam("recipientIds", "List of recipient IDs. Combined with parameter emails with OR"),
                optionalParam("emails", "List of recipient emails. Combined with parameter recipientIds with OR"),
                optionalParam("mailinglistIds", "List of mailinglist IDs"),
                optionalParam("mediaTypes", "List of media types. %s".formatted(MEDIATYPES)),
                optionalParam("userStatuses", "List of user statuses. %s".formatted(USER_STATUSES)),
                optionalParam("date", "Pattern YYYY-MM-DD with optional '_' wildcards")
        );
    }

    protected ResultHandler documentCreate() {
        return document("create", builder()
                .summary("Create a new binding entry")
                .requestSchema(schema("CreateRequest"))
                .requestFields(CREATE_REQUEST_FIELDS)
                .responseSchema(responseSchema)
                .responseFields(BINDING_RESPONSE_FIELDS)
        );
    }

    protected ResultHandler documentRead() {
        return document("read", builder()
                .summary("Read recipient binding data")
                .pathParameters(IDENTIFIER_PATH_PARAMS)
                .responseSchema(responseSchema)
                .responseFields(BINDING_RESPONSE_FIELDS)
        );
    }

    protected ResultHandler documentDelete() {
        return document("delete", builder()
                .summary("Delete single binding entry")
                .pathParameters(IDENTIFIER_PATH_PARAMS)
        );
    }

    ResultHandler documentPatch() {
        return documentPatch(builder()
                .summary("Partially update binding data")
                .pathParameters(IDENTIFIER_PATH_PARAMS)
                .requestSchema(schema("PatchRequest"))
                .requestFields(UPDATE_REQUEST_FIELDS)
                .responseSchema(responseSchema)
                .responseFields(BINDING_RESPONSE_FIELDS)
        );
    }

    protected ResultHandler documentBulkDelete() {
        return document("bulk-delete", builder()
                .summary("Bulk delete bindings")
                .description("Bulk deletion of existing subscriptions")
                .requestSchema(schema("BulkDeleteRequest"))
                .requestFields(BULK_DELETE_REQUEST_FIELDS)
                .responseSchema(BULK_DELETE_RESPONSE_SCHEMA)
                .responseFields(BULK_DELETE_RESULT_FIELDS)
        );
    }

    protected ResultHandler documentBulkUpsert() {
        return document("bulk-upsert", builder()
                .summary("Bulk upsert bindings")
                .description("Bulk update existing or create new subscriptions")
                .requestSchema(schema("BulkUpsertRequest"))
                .requestFields(BULK_UPSERT_REQUEST_FIELDS)
                .responseSchema(schema("BulkUpsertResponse"))
                .responseFields(BULK_UPSERT_RESPONSE_FIELDS)
        );
    }
}
