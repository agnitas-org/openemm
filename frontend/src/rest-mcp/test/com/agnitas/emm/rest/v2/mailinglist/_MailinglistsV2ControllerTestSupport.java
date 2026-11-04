/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2.mailinglist;

import static com.epages.restdocs.apispec.ResourceSnippetParameters.builder;

import com.agnitas.emm.rest.v2.ConstrainedFields;
import com.agnitas.emm.rest.v2.RestDocsTestSupport;
import com.agnitas.emm.rest.v2.infrastructure.search.SortEnum;
import com.epages.restdocs.apispec.Schema;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.test.web.servlet.ResultHandler;

abstract class _MailinglistsV2ControllerTestSupport extends RestDocsTestSupport {

    private static final ConstrainedFields CREATE_CONSTRAINTS = new ConstrainedFields(CreateMailinglistRequest.class);
    private static final ConstrainedFields UPDATE_CONSTRAINTS = new ConstrainedFields(PatchMailinglistRequest.class);

    private static final FieldDescriptor[] MAILINGLIST_RESPONSE_FIELDS = {
            requiredField("id", "Unique ID of the mailinglist"),
            requiredField("name", "Name of the mailinglist"),
            requiredField("description", "Description of the mailinglist")
    };

    private static final FieldDescriptor[] CREATE_REQUEST_FIELDS = {
            requiredField(CREATE_CONSTRAINTS, "name", "Display name of the mailinglist"),
            optionalField(CREATE_CONSTRAINTS, "description", "Description of the mailinglist"),
    };

    private static final FieldDescriptor[] UPDATE_REQUEST_FIELDS = {
            optionalField(UPDATE_CONSTRAINTS, "description", "Mailinglist description"),
    };

    private final Schema mailinglistResponseSchema = schema("Response");

    @Override
    protected String getDocsTag() {
        return "Mailing List";
    }

    @Override
    protected String getDocsIdentifierPrefix() {
        return "mailinglists";
    }

    ResultHandler documentList() {
        return documentPagination(
                MAILINGLIST_RESPONSE_FIELDS,
                optionalParam("sort", "Supported %s".formatted(SortEnum.allowedValues(MailinglistsSortKey.class)))
        );
    }

    ResultHandler documentRead() {
        return document("read", builderWithIdParam()
                .summary("Read mailinglist data")
                .responseSchema(mailinglistResponseSchema)
                .responseFields(MAILINGLIST_RESPONSE_FIELDS)
        );
    }

    ResultHandler documentCreate() {
        return document("create", builder()
                .summary("Write new mailinglist")
                .requestSchema(schema("CreateRequest"))
                .requestFields(CREATE_REQUEST_FIELDS)
                .responseSchema(mailinglistResponseSchema)
                .responseFields(MAILINGLIST_RESPONSE_FIELDS)
        );
    }

    ResultHandler documentPatch() {
        return documentPatch(builderWithIdParam()
                .summary("Partially update mailinglist data")
                .requestSchema(schema("UpdateRequest"))
                .requestFields(UPDATE_REQUEST_FIELDS)
                .responseSchema(mailinglistResponseSchema)
                .responseFields(MAILINGLIST_RESPONSE_FIELDS)
        );
    }

    ResultHandler documentDelete() {
        return document("delete", builderWithIdParam()
                .summary("Delete single mailinglist")
        );
    }
}
