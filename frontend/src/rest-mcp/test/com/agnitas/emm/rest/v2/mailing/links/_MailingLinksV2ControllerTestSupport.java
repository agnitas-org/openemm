/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2.mailing.links;

import static com.epages.restdocs.apispec.ResourceSnippetParameters.builder;

import com.agnitas.emm.rest.v2.ConstrainedFields;
import com.agnitas.emm.rest.v2.RestDocsTestSupport;
import com.agnitas.emm.rest.v2.infrastructure.config.json.RestEnumAllowedValues;
import com.epages.restdocs.apispec.ParameterDescriptorWithType;
import com.epages.restdocs.apispec.Schema;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.test.web.servlet.ResultHandler;

public abstract class _MailingLinksV2ControllerTestSupport extends RestDocsTestSupport {

    private static final ConstrainedFields PATCH_LINK_CONSTRAINTS = new ConstrainedFields(PatchLinkRequest.class);
    private static final ConstrainedFields LINK_EXTENSION_CONSTRAINTS = new ConstrainedFields(CreateLinkExtensionRequest.class);

    private static final ParameterDescriptorWithType LINK_ID_PATH_PARAM = intParam("linkId", "Link ID");
    private static final ParameterDescriptorWithType MAILING_ID_PATH_PARAM = intParam("mailingId", "Mailing ID");

    private static final ParameterDescriptorWithType[] IDENTIFIER_PATH_PARAMS = {
            LINK_ID_PATH_PARAM,
            MAILING_ID_PATH_PARAM,
    };

    protected static final FieldDescriptor[] EXTENSION_RESPONSE_FIELDS = fields(
            requiredField("name", "Extension name"),
            optionalField("value", "Extension value")
    );

    protected static final FieldDescriptor[] PATCH_EXTENSION_FIELDS = fields(
            requiredField(LINK_EXTENSION_CONSTRAINTS, "name", "Extension name"),
            optionalField(LINK_EXTENSION_CONSTRAINTS, "value", "Extension value")
    );

    public static final FieldDescriptor[] LINK_RESPONSE_FIELDS = fields(
            requiredField("id", "Link ID"),
            requiredField("url", "Full link URL"),
            requiredField("description", "Link name/description"),
            requiredField("tracking", RestEnumAllowedValues.LINK_TRACKINGS),
            requiredField("retargeting", RestEnumAllowedValues.LINK_RETARGETINGS),
            optionalField("triggerId", "Associated trigger/action ID"),
            requiredField("static", "Whether this is a static link"),
            requiredField("administrative", "Whether this is an administrative link"),
            optionalField("extensions", "Additional name-value pairs"),
            applyPrefix("extensions[]", EXTENSION_RESPONSE_FIELDS)
    );

    protected static final FieldDescriptor[] PATCH_LINK_FIELDS = fields(
            patchField("url", "Full link URL"),
            patchField("description", "Link name/description"),
            patchField("tracking", RestEnumAllowedValues.LINK_TRACKINGS),
            patchField("retargeting", RestEnumAllowedValues.LINK_RETARGETINGS),
            patchField("triggerId", "Associated trigger/action ID"),
            patchField("static", "Whether this is a static link"),
            patchField("administrative", "Whether this is an administrative link"),
            patchField("extensions", "Additional name-value pairs"),
            applyPrefix("extensions[]", PATCH_EXTENSION_FIELDS)
    );

    protected final Schema responseSchema = schema("Response");

    ResultHandler documentList() {
        return documentPagination(LINK_RESPONSE_FIELDS);
    }

    ResultHandler documentFind() {
        return document("read", builder()
                .summary("Read mailing link data")
                .pathParameters(IDENTIFIER_PATH_PARAMS)
                .responseSchema(responseSchema)
                .responseFields(LINK_RESPONSE_FIELDS)
        );
    }

    ResultHandler documentPatch() {
        return documentPatch(builder()
                .summary("Partially update mailing link data")
                .pathParameters(IDENTIFIER_PATH_PARAMS)
                .requestSchema(schema("PatchRequest"))
                .requestFields(PATCH_LINK_FIELDS)
                .responseSchema(responseSchema)
                .responseFields(LINK_RESPONSE_FIELDS)
        );
    }

    @Override
    protected String getDocsTag() {
        return "Mailing Link";
    }

    @Override
    protected String getDocsIdentifierPrefix() {
        return "mailing-links";
    }

    private static FieldDescriptor patchField(String name, String description) {
        return optionalField(PATCH_LINK_CONSTRAINTS, name, description);
    }
}
