/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2.mailing;

import static com.agnitas.emm.rest.v2.mailing.attachments._MailingAttachmentsV2ControllerTestSupport.ATTACHMENT_RESPONSE_FIELDS;
import static com.agnitas.emm.rest.v2.mailing.content._MailingContentV2ControllerTestSupport.MAILING_CONTENT_RESPONSE_FIELDS;
import static com.agnitas.emm.rest.v2.mailing.images._MailingImagesV2ControllerTestSupport.IMAGE_RESPONSE_FIELDS;
import static com.agnitas.emm.rest.v2.mailing.links._MailingLinksV2ControllerTestSupport.LINK_RESPONSE_FIELDS;
import static com.agnitas.emm.rest.v2.target._TargetsV2ControllerTestSupport.TARGET_RESPONSE_FIELDS;
import static com.epages.restdocs.apispec.ResourceSnippetParameters.builder;
import static java.time.ZoneOffset.UTC;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.agnitas.emm.rest.v2.ConstrainedFields;
import com.agnitas.emm.rest.v2.RestDocsTestSupport;
import com.agnitas.emm.rest.v2.infrastructure.config.json.RestEnumAllowedValues;
import com.agnitas.emm.rest.v2.infrastructure.search.SortEnum;
import com.agnitas.emm.rest.v2.mailing.attachments.AttachmentResponse;
import com.agnitas.emm.rest.v2.mailing.dto.CreateMailingParameterRequest;
import com.agnitas.emm.rest.v2.mailing.dto.CreateMailingRequest;
import com.agnitas.emm.rest.v2.mailing.dto.MailingParameterResponse;
import com.agnitas.emm.rest.v2.mailing.dto.PatchMailingRequest;
import com.agnitas.emm.rest.v2.mailing.dto.MailingMediaType;
import com.agnitas.emm.rest.v2.mailing.dto.UpdateMailingParameterRequest;
import com.agnitas.emm.rest.v2.mailing.links.LinkExtensionResponse;
import com.agnitas.emm.rest.v2.mailing.links.LinkResponse;
import com.agnitas.emm.rest.v2.mailing.links.LinkRetargeting;
import com.agnitas.emm.rest.v2.mailing.links.LinkTracking;
import com.agnitas.emm.rest.v2.target.TargetResponse;
import com.epages.restdocs.apispec.ParameterDescriptorWithType;
import com.epages.restdocs.apispec.Schema;
import com.epages.restdocs.apispec.SimpleType;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.snippet.Attributes;
import org.springframework.test.web.servlet.ResultHandler;

public abstract class _MailingsV2TestSupportBase extends RestDocsTestSupport {

    private static final ConstrainedFields CREATE_CONSTRAINTS = new ConstrainedFields(CreateMailingRequest.class);
    private static final ConstrainedFields CREATE_PARAM_CONSTRAINTS =
            new ConstrainedFields(CreateMailingParameterRequest.class);
    private static final ConstrainedFields UPDATE_CONSTRAINTS = new ConstrainedFields(PatchMailingRequest.class);

    protected static final AttachmentResponse ATTACHMENT_RESPONSE = new AttachmentResponse(
            123,
            "example.png",
            0,
            2097152,
            2097252,
            "image/png"
    );

    public static final LinkResponse LINK_RESPONSE = new LinkResponse(
            123,
            "https://example.com",
            "Example link",
            LinkTracking.TEXT_AND_HTML,
            LinkRetargeting.SET_COOKIE,
            10,
            true,
            true,
            List.of(new LinkExtensionResponse("name", "value"))
    );

    protected static final TargetResponse TARGET_RESPONSE = new TargetResponse(
            123,
            "country de",
            "de recipients",
            "country = de",
            "country = de",
            true,
            ZonedDateTime.of(2021, 10, 18, 19, 34, 30, 0, UTC).toInstant(),
            ZonedDateTime.of(2026, 2, 3, 2, 0, 30, 0, UTC).toInstant(),
            false,
            null
    );

    protected static final MailingParameterResponse PARAMETER_RESPONSE = new MailingParameterResponse(
            "name",
            "value",
            "description",
            null
    );

    protected static final CreateMailingParameterRequest CREATE_PARAMETER_REQUEST = new CreateMailingParameterRequest(
            "name",
            "value",
            "description"
    );

    protected static final UpdateMailingParameterRequest UPDATE_PARAMETER_REQUEST = new UpdateMailingParameterRequest(
            "name",
            "value",
            "description"
    );

    protected static final ParameterDescriptorWithType VIEW_PARAM = optionalParam("view",
            "Allowed values: [light, full]"
    );

    protected static final FieldDescriptor[] LIGHT_RESPONSE_FIELDS = {
            requiredField("id", "Unique ID of the mailing"),
            requiredField("type", "Type of the mailing"),
            requiredField("name", "Name of the mailing"),
            requiredField("description", "Description of the mailing"),
    };

    protected static final FieldDescriptor[] MAILING_PARAMETER_RESPONSE_FIELDS = {
            requiredField("name", "Parameter name"),
            optionalField("value", "Parameter value"),
            optionalField("description", "Parameter description"),
            optionalField("creationDate", "Parameter creation date").type(SimpleType.STRING)
    };

    private static final FieldDescriptor[] MAILING_PARAMETER_REQUEST_FIELDS = {
            requiredField(CREATE_PARAM_CONSTRAINTS, "name", "Parameter name"),
            optionalField(CREATE_PARAM_CONSTRAINTS, "value", "Parameter value"),
            optionalField(CREATE_PARAM_CONSTRAINTS, "description", "Parameter description")
    };

    protected static final FieldDescriptor[] PATCH_REQUEST_FIELDS = fields(
            updateField("name", "Display name of the mailing"),
            updateField("description", "Description of the mailing"),
            updateField("mailinglistId", "ID of the assigned mailinglist"),
            updateField("mailingType", RestEnumAllowedValues.MAILING_TYPES),
            updateField("contentType", RestEnumAllowedValues.MAILING_CONTENT_TYPES),
            updateField("subject", "Subject of the mailing"),
            updateField("preHeader", "Pre-Header of the mailing"),
            updateField("senderAddress", "Sender email address"),
            updateField("replyAddress", "Replay email address"),
            updateField("targetExpression", "Target expression of the mailing"),
            updateField("needsTarget", "Whether generated mailing should only be sent with target group selection")
                    .type(SimpleType.BOOLEAN),
            updateField("openTriggerId", "Open Action ID"),
            updateField("clickTriggerId", "Click Action ID"),
            updateField("followupType", RestEnumAllowedValues.FOLLOWUP_TYPES),
            updateField("followupForId", "ID of the mailing this is a follow-up for"),
            updateField("senderName", "Sender full name"),
            updateField("replyName", "Reply full name"),
            updateField("textTemplate", "Text template"),
            updateField("htmlTemplate", "HTML template"),
            updateField("smsTemplate", "SMS template"),
            updateField("rcsTemplate", "RCS template"),
            updateField("archiveId", "Archive ID"),
            updateField("mediaTypes", RestEnumAllowedValues.REST_MEDIATYPES)
                    .type(JsonFieldType.ARRAY)
                    .attributes(
                            Attributes.key("items").value(Map.of(
                                    "type", "string",
                                    "enum", Arrays.stream(MailingMediaType.values()).map(Enum::name).toList()
                            ))
                    ),
            updateField("charset", RestEnumAllowedValues.CHARSETS),
            updateField("format", RestEnumAllowedValues.MAIL_FORMATS),
            updateField("measureOpeningRate", RestEnumAllowedValues.ONE_PIXEL_POSITIONS),
            updateField("envelopeEmail", "Envelope email address"),
            updateField("smsSenderAddress", "SMS sender address"),
            applyPrefix("parameters[]", MAILING_PARAMETER_REQUEST_FIELDS)
    );

    protected static final FieldDescriptor[] RESPONSE_FIELDS = fields(
            requiredField("id", "Unique ID of the mailing"),
            requiredField("name", "Name of the mailing"),
            optionalField("description", "Description of the mailing"),
            requiredField("contentType", RestEnumAllowedValues.MAILING_CONTENT_TYPES),
            requiredField("mailingType", RestEnumAllowedValues.MAILING_TYPES),
            optionalField("archiveId", "ID of the archive"),
            optionalField("openTriggerId", "Mailing open action ID"),
            optionalField("clickTriggerId", "Mailing click action ID"),
            requiredField("mediaTypes[]", RestEnumAllowedValues.REST_MEDIATYPES),
            requiredField("mailinglistId", "ID of the assigned mailinglist"),
            requiredField("targetExpression", "Target expression of the mailing"),
            optionalField(
                    "needsTarget",
                    "Whether generated mailing should only be sent with target group selection"
            ),
            requiredField("subject", "Subject of the mailing"),
            requiredField("preHeader", "Pre-header of the mailing"),
            optionalField("followupType", RestEnumAllowedValues.FOLLOWUP_TYPES),
            optionalField("followupForId", "ID of the mailing this is a follow-up for"),
            requiredField("senderAddress", "Sender email address"),
            requiredField("senderName", "Sender name"),
            requiredField("replyAddress", "Reply email address"),
            requiredField("replyName", "Reply name"),
            optionalField("textTemplate", "Text template"),
            optionalField("htmlTemplate", "HTML template"),
            requiredField("charset", RestEnumAllowedValues.CHARSETS),
            requiredField("format", RestEnumAllowedValues.MAIL_FORMATS),
            requiredField("measureOpeningRate", RestEnumAllowedValues.ONE_PIXEL_POSITIONS),
            optionalField("envelopeEmail", "Envelope email address"),
            optionalField("smsTemplate", "SMS template"),
            optionalField("smsSenderAddress", "SMS sender address"),
            optionalField("rcsTemplate", "RCS template"),
            applyPrefix("parameters[]", MAILING_PARAMETER_RESPONSE_FIELDS)
    );

    protected static final FieldDescriptor[] DEPENDENCIES_FIELDS = fields(
            applyPrefix("contentBlocks[].", MAILING_CONTENT_RESPONSE_FIELDS),
            applyPrefix("targets[].", TARGET_RESPONSE_FIELDS),
            applyPrefix("images[].", IMAGE_RESPONSE_FIELDS),
            applyPrefix("attachments[].", ATTACHMENT_RESPONSE_FIELDS),
            applyPrefix("links[].", LINK_RESPONSE_FIELDS)
    );

    protected static final FieldDescriptor[] CREATE_REQUEST_FIELDS = fields(
            createField("name", "Display name of the mailing"),
            createField("description", "Description of the mailing"),
            createField("contentType", RestEnumAllowedValues.MAILING_CONTENT_TYPES),
            createField("mailingType", RestEnumAllowedValues.MAILING_TYPES),
            createField("archiveId", "Archive ID"),
            createField("mediaTypes", "List of media types. %s".formatted(RestEnumAllowedValues.REST_MEDIATYPES)),
            createField("openTriggerId", "Open Action ID of the mailing"),
            createField("clickTriggerId", "Click Action ID of the mailing"),
            createField("mailinglistId", "ID of the assigned mailinglist"),
            createField("targetExpression", "Target expression of the mailing"),
            createField("subject", "Subject of the mailing"),
            createField("preHeader", "Pre-Header of the mailing"),
            createField("senderAddress", "Sender email address"),
            createField("senderName", "Sender name"),
            createField("replyAddress", "Reply email address"),
            createField("replyName", "Reply name"),
            createField("followupType", RestEnumAllowedValues.FOLLOWUP_TYPES),
            createField("followupForId", "ID of the mailing this follow-up belongs to"),
            createField("textTemplate", "Text template of the mailing"),
            createField("htmlTemplate", "HTML template of the mailing"),
            createField("charset", RestEnumAllowedValues.CHARSETS),
            createField("format", RestEnumAllowedValues.MAIL_FORMATS),
            createField("measureOpeningRate", RestEnumAllowedValues.ONE_PIXEL_POSITIONS),
            createField("envelopeEmail", "Envelope email address"),
            createField("smsTemplate", "SMS template of the mailing"),
            createField("smsSenderAddress", "SMS sender address"),
            createField("rcsTemplate", "RCS template of the mailing"),
            applyPrefix("parameters[]", MAILING_PARAMETER_REQUEST_FIELDS)
    );

    private final Schema responseSchema = schema("Response");
    private final Schema lightResponseSchema = schema("ResponseLight");
    private final Schema fullResponseSchema = schema("ResponseFull");

    ResultHandler documentList() {
        return documentPagination(
                getLightResponseFields(),
                getListQueryParams()
        );
    }

    protected ParameterDescriptorWithType[] getListQueryParams() {
        return new ParameterDescriptorWithType[]{
                optionalParam("sort", "Supported %s".formatted(SortEnum.allowedValues(MailingSortKey.class))),
                optionalParam("name", "Filter name"),
                optionalParam("mailinglistIds", "Filter Mailinglist IDs"),
                optionalParam("types", "Comma-Separated Values. %s".formatted(RestEnumAllowedValues.MAILING_TYPES))};
    }

    ResultHandler documentFind() {
        return document("read", builderWithIdParam()
                .summary("Read %s data".formatted(getDocsTagLc()))
                .queryParameters(VIEW_PARAM)
                .responseSchema(responseSchema)
                .responseFields(getResponseFields())
        );
    }

    ResultHandler documentFindLight() {
        return document("read-light", builderWithIdParam()
                .summary("Read %s data (light return JSON version)".formatted(getDocsTagLc()))
                .queryParameters(VIEW_PARAM)
                .responseSchema(lightResponseSchema)
                .responseFields(getLightResponseFields())
        );
    }

    ResultHandler documentFindFull() {
        return document("read-full", builderWithIdParam()
                .summary("Read %s data (full return JSON version)".formatted(getDocsTagLc()))
                .queryParameters(VIEW_PARAM)
                .responseSchema(fullResponseSchema)
                .responseFields(getFullResponseFields())
        );
    }

    ResultHandler documentCreate() {
        return document("create", builder()
                .summary("Write new " + getDocsTagLc())
                .requestSchema(schema("CreateRequest"))
                .requestFields(getCreateRequestFields())
                .responseSchema(responseSchema)
                .responseFields(getResponseFields())
        );
    }

    ResultHandler documentCreateFromTemplate() {
        return document("from-template", builder()
                .pathParameters(intParam("templateId", "ID of the mailing template"))
                .summary("Create new mailing from template")
                .responseSchema(lightResponseSchema)
                .responseFields(getLightResponseFields())
        );
    }

    ResultHandler documentCopy() {
        return document("copy", builderWithIdParam()
                .summary("Copy %s by ID".formatted(getDocsTagLc()))
                .responseSchema(lightResponseSchema)
                .responseFields(getLightResponseFields())
        );
    }

    ResultHandler documentPatch() {
        return documentPatch(builderWithIdParam()
                .summary("Partially update %s data".formatted(getDocsTag()))
                .requestSchema(schema("PatchRequest"))
                .requestFields(getPatchRequestFields())
                .responseSchema(responseSchema)
                .responseFields(getResponseFields())
        );
    }

    ResultHandler documentDelete() {
        return document("delete", builderWithIdParam()
                .summary("Delete single %s".formatted(getDocsTagLc()))
        );
    }

    protected abstract FieldDescriptor[] getLightResponseFields();

    protected abstract FieldDescriptor[] getResponseFields();

    protected abstract FieldDescriptor[] getCreateRequestFields();

    protected abstract FieldDescriptor[] getPatchRequestFields();

    protected abstract FieldDescriptor[] getFullResponseFields();

    private static FieldDescriptor updateField(String name, String description) {
        return optionalField(UPDATE_CONSTRAINTS, name, description);
    }

    protected static FieldDescriptor createField(String name, String description) {
        return requiredField(CREATE_CONSTRAINTS, name, description);
    }
}
