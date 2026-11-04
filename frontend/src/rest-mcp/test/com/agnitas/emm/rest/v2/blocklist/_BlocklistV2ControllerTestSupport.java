/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2.blocklist;

import static com.epages.restdocs.apispec.ResourceSnippetParameters.builder;

import com.agnitas.emm.rest.v2.RestDocsTestSupport;
import com.epages.restdocs.apispec.ParameterDescriptorWithType;
import com.epages.restdocs.apispec.Schema;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.test.web.servlet.ResultHandler;

public abstract class _BlocklistV2ControllerTestSupport extends RestDocsTestSupport {

    private static final FieldDescriptor[] RESPONSE_FIELDS = {
            requiredField("email", "Blocked email"),
            requiredField("reason", "Blocking reason"),
            requiredField("date", "Blocking time"),
    };

    private static final ParameterDescriptorWithType EMAIL_PATH_PARAM = stringParam("email", "Email address");

    private final Schema responseSchema = schema("Response");

    @Override
    protected String getDocsTag() {
        return "Blocklist";
    }

    @Override
    protected String getDocsIdentifierPrefix() {
        return "blocklist";
    }

    protected ResultHandler documentList() {
        return documentPagination(RESPONSE_FIELDS);
    }

    ResultHandler documentReadByEmail() {
        return document("read-by-email", builderWithIdParam()
                .summary("Read blocklist entry by email")
                .pathParameters(EMAIL_PATH_PARAM)
                .responseSchema(responseSchema)
                .responseFields(RESPONSE_FIELDS)
        );
    }

    protected ResultHandler documentDeleteByEmail() {
        return document("delete-by-email", builder()
                .summary("Delete blocklist entry by email")
                .pathParameters(EMAIL_PATH_PARAM)
        );
    }

    private static final FieldDescriptor[] ADD_REQUEST_FIELDS = {
            requiredField("email", "Email address"),
            optionalField("reason", "Blocking reason"),
    };

    private static final FieldDescriptor[] UPDATE_REQUEST_FIELDS = {
            requiredField("reason", "Blocking reason"),
    };

    ResultHandler documentAddEntry() {
        return document("add-entry", builder()
                .summary("Add blocklist entry")
                .requestSchema(schema("PostRequest"))
                .requestFields(ADD_REQUEST_FIELDS)
                .responseSchema(responseSchema)
                .responseFields(RESPONSE_FIELDS)
        );
    }

    ResultHandler documentUpdateEntry() {
        return document("update-entry", builder()
                .summary("Update blocklist entry")
                .pathParameters(EMAIL_PATH_PARAM)
                .requestSchema(schema("PatchRequest"))
                .requestFields(UPDATE_REQUEST_FIELDS)
                .responseSchema(responseSchema)
                .responseFields(RESPONSE_FIELDS)
        );
    }
}
