/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2.mailing;


import org.springframework.restdocs.payload.FieldDescriptor;

abstract class _StandardTemplatesV2ControllerTestSupport extends _MailingsV2TestSupportBase {

    protected static final FieldDescriptor[] CREATE_TEMPLATE_REQUEST_FIELDS = fields(
            createField("needsTarget", "Whether generated mailing should only be sent with target group selection"),
            CREATE_REQUEST_FIELDS
    );

    protected static final FieldDescriptor[] PATCH_TEMPLATE_REQUEST_FIELDS = fields(
            createField("needsTarget", "Whether generated mailing should only be sent with target group selection"),
            PATCH_REQUEST_FIELDS
    );

    private static final FieldDescriptor[] TEMPLATE_FULL_RESPONSE_FIELDS = fields(
            RESPONSE_FIELDS,
            DEPENDENCIES_FIELDS
    );

    @Override
    protected String getDocsTag() {
        return "Template";
    }

    @Override
    protected String getDocsIdentifierPrefix() {
        return "standard-templates";
    }

    @Override
    protected FieldDescriptor[] getLightResponseFields() {
        return LIGHT_RESPONSE_FIELDS;
    }

    @Override
    protected FieldDescriptor[] getResponseFields() {
        return RESPONSE_FIELDS;
    }

    @Override
    protected FieldDescriptor[] getCreateRequestFields() {
        return CREATE_TEMPLATE_REQUEST_FIELDS;
    }

    @Override
    protected FieldDescriptor[] getPatchRequestFields() {
        return PATCH_TEMPLATE_REQUEST_FIELDS;
    }

    @Override
    protected FieldDescriptor[] getFullResponseFields() {
        return TEMPLATE_FULL_RESPONSE_FIELDS;
    }
}
