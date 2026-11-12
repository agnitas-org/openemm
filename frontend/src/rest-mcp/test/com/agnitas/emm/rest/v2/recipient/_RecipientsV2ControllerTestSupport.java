/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2.recipient;

import static com.epages.restdocs.apispec.ResourceSnippetParameters.builder;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

import java.util.stream.Stream;

import com.agnitas.emm.rest.v2.ConstrainedParams;
import com.agnitas.emm.rest.v2.RestDocsTestSupport;
import com.agnitas.emm.rest.v2.infrastructure.config.json.RestEnumAllowedValues;
import com.epages.restdocs.apispec.ParameterDescriptorWithType;
import com.epages.restdocs.apispec.Schema;
import com.epages.restdocs.apispec.SimpleType;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultHandler;

abstract class _RecipientsV2ControllerTestSupport extends RestDocsTestSupport {

    protected static final ParameterDescriptorWithType EMAIL_PARAM = stringParam("email", "Recipient email address");

    private static final ConstrainedParams CONSTRAINED_PARAMS = new ConstrainedParams(RecipientSubscribeRequest.class);

    private static final ParameterDescriptorWithType[] LIST_FILTER_PARAMS = {
            optionalParam("userTypes", "Comma-Separated Values. %s".formatted(RestEnumAllowedValues.USER_TYPES)),
            optionalParam("userStatus", RestEnumAllowedValues.USER_STATUSES),
            optionalParam("mailinglistId", "ID of the assigned mailinglist"),
            optionalParam("targetId", "ID of the related target group"),
            optionalParam(
                    "eql",
                    """
                    Filter expression in EQL syntax.
                    
                    Use single or double quotes for string values, e.g. `firstname = 'Emma'`.
                  
                    Field names can be backtick-quoted, e.g. `` `email` LIKE '%example.com' ``.
                    
                    Names that match EQL keywords \
                    (e.g. `email`, `status`, `type`, `test`, `on`, `by`, `active`, `admin`) \
                    must be backtick-quoted.
                    """
            ),
    };

    private static final FieldDescriptor[] RECIPIENT_RESPONSE_FIELDS = {
            requiredField("customer_id", "Unique ID of the recipient"),
            requiredField("email", "Recipient email address"),
            requiredField("firstname", "Recipient firstname"),
            requiredField("lastname", "Recipient firstname"),
            requiredField("title", "Standard recipient profile field"),
            requiredField("custom_profile_field", "Custom recipient profile field"),
    };

    private static final FieldDescriptor[] CREATE_REQUEST_FIELDS = {
            requiredField("email", "Recipient email address"),
            requiredField("firstname", "Recipient firstname"),
            requiredField("lastname", "Recipient firstname"),
            optionalField("title", "Professor"),
            optionalField("custom_profile_field", "Custom recipient profile field")
    };

    private static final FieldDescriptor[] BULK_UPSERT_FIELDS = fields(
            fieldWithPath("[]").description("An array of recipient objects"),
            applyPrefix("[]", CREATE_REQUEST_FIELDS)
    );

    private static final FieldDescriptor[] UPDATE_REQUEST_FIELDS = {
            requiredField("firstname", "Recipient firstname")
    };

    private static final ParameterDescriptorWithType[] BULK_SUBSCRIBE_PARAMS = {
            CONSTRAINED_PARAMS.with("mailinglist", "Target mailing list. Use '*' for all or specific ID.").optional(),
            CONSTRAINED_PARAMS
                    .with("mediaType", "Media type to subscribe for. %s".formatted(RestEnumAllowedValues.MEDIATYPES))
                    .optional(),
    };
    private static final ParameterDescriptorWithType[] SINGLE_SUBSCRIBE_PARAMS = Stream.concat(
            Stream.of(BULK_SUBSCRIBE_PARAMS),
            Stream.of(CONSTRAINED_PARAMS.with("status", "Subscription status.", Groups.Single.class).optional())
    ).toArray(ParameterDescriptorWithType[]::new);

    private static final FieldDescriptor[] BULK_UPSERT_RESPONSE = {
            requiredField("total", "Total number of recipients in the request"),
            requiredField("succeeded", "Number of successfully imported recipients"),
            requiredField("failed", "Number of recipients that failed"),
            requiredField("errors", "Summary of error types encountered"),
            requiredField("errors.BLACKLIST_ERROR", "Count of blacklisted emails"),
            requiredField("errors.DATE_ERROR", "Count of invalid date formats"),
            requiredField("errors.GENDER_ERROR", "Count of invalid gender values"),
            requiredField("errors.NUMERIC_ERROR", "Count of invalid numeric values"),
            requiredField("errors.EMAIL_ERROR", "Count of syntactically invalid emails")
    };

    private final Schema responseSchema = schema("Response");
    private final Schema bulkUpsertRequestSchema = schema("BulkUpsertRequest");
    private final Schema bulkUpsertResponseSchema = schema("BulkUpsertResponse");

    @Override
    protected String getDocsIdentifierPrefix() {
        return "recipients";
    }

    @Override
    protected String getDocsTag() {
        return "Recipient";
    }

    ResultHandler documentList() {
        return documentPagination(
                RECIPIENT_RESPONSE_FIELDS,
                ArrayUtils.add(
                        LIST_FILTER_PARAMS,
                        optionalParam(
                                "sort",
                                "Sort as `column,asc|desc` (default direction: asc) e.g. `?sort=lastname,desc`"
                        )
                )
        );
    }

    ResultHandler documentListIds() {
        return document("list-ids", builder()
                .summary("Get array of recipient IDs")
                .description("Returns IDs of all recipients matching the filters, without pagination.")
                .queryParameters(LIST_FILTER_PARAMS)
                .responseSchema(schema("RecipientIdsResponse"))
                .responseFields(
                        fieldWithPath("[]").type(JsonFieldType.ARRAY).description("IDs of all matching recipients")
                )
        );
    }

    ResultHandler documentReadById() {
        return document("read-by-id", builderWithIdParam()
                .summary("Read recipient data by ID")
                .responseSchema(responseSchema)
                .responseFields(RECIPIENT_RESPONSE_FIELDS)
        );
    }

    ResultHandler documentReadByEmail() {
        return document("read-by-email", builder()
                .pathParameters(EMAIL_PARAM)
                .summary("Read recipient data by email")
                .responseSchema(responseSchema)
                .responseFields(RECIPIENT_RESPONSE_FIELDS)
        );
    }

    ResultHandler documentCreate() {
        return document("create", builder()
                .summary("Create a new recipient")
                .queryParameters(SINGLE_SUBSCRIBE_PARAMS)
                .requestSchema(schema("CreateRequest"))
                .requestFields(CREATE_REQUEST_FIELDS)
                .responseSchema(responseSchema)
                .responseFields(RECIPIENT_RESPONSE_FIELDS)
        );
    }

    ResultHandler documentBulkCreate() {
        return document("bulk-create", builder()
                .summary("Bulk create recipients")
                .queryParameters(BULK_SUBSCRIBE_PARAMS)
                .requestSchema(bulkUpsertRequestSchema)
                .requestFields(fields(
                        fieldWithPath("[]").description("An array of recipient objects"),
                        applyPrefix("[]", CREATE_REQUEST_FIELDS)
                ))
                .responseSchema(bulkUpsertResponseSchema)
                .responseFields(BULK_UPSERT_RESPONSE)
        );
    }

    ResultHandler documentBulkUpsert() {
        return document("bulk-upsert", builder()
                .summary("Bulk upsert recipients")
                .queryParameters(BULK_SUBSCRIBE_PARAMS)
                .requestSchema(bulkUpsertRequestSchema)
                .requestFields(BULK_UPSERT_FIELDS)
                .responseSchema(bulkUpsertResponseSchema)
                .responseFields(BULK_UPSERT_RESPONSE)
        );
    }

    ResultHandler documentPatchById() {
        return documentPatch("patch-by-id", builderWithIdParam()
                .summary("Partially update recipient data by ID")
                .queryParameters(
                        param("mailinglist", "The ID of the mailing list to subscribe to. Use '*' for all lists"),
                        param("mediaType", RestEnumAllowedValues.MEDIATYPES),
                        param("status", RestEnumAllowedValues.USER_STATUSES)
                )
                .requestSchema(schema("UpdateRequest"))
                .requestFields(UPDATE_REQUEST_FIELDS)
                .responseSchema(responseSchema)
                .responseFields(RECIPIENT_RESPONSE_FIELDS)
        );
    }

    ResultHandler documentPatchByEmail() {
        return documentPatch("patch-by-email", builder()
                .pathParameters(EMAIL_PARAM)
                .summary("Partially update recipient data by email")
                .queryParameters(
                        param("mailinglist", "The ID of the mailing list to subscribe to. Use '*' for all lists"),
                        param("mediaType", RestEnumAllowedValues.MEDIATYPES),
                        param("status", RestEnumAllowedValues.USER_STATUSES)
                )
                .requestSchema(schema("UpdateRequest"))
                .requestFields(UPDATE_REQUEST_FIELDS)
                .responseSchema(responseSchema)
                .responseFields(RECIPIENT_RESPONSE_FIELDS)
        );
    }

    ResultHandler documentDeleteById() {
        return document("delete", builderWithIdParam()
                .summary("Delete single recipient by ID")
        );
    }

    ResultHandler documentDeleteByEmail() {
        return document("delete-by-email", builder()
                .pathParameters(EMAIL_PARAM)
                .summary("Delete single recipient by email")
        );
    }

    ResultHandler documentBulkDelete() {
        return document("bulk-delete", builder()
                .summary("Bulk delete recipients")
                .description("Deletes multiple recipients by their IDs and returns a detailed status for each.")
                .requestSchema(BULK_DELETE_REQUEST_SCHEMA)
                .requestFields(requiredField("ids[]", "Recipient ID").type(SimpleType.NUMBER))
                .responseSchema(BULK_DELETE_RESPONSE_SCHEMA)
                .responseFields(BULK_DELETE_RESULT_FIELDS)
        );
    }
}
