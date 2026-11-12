/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2.mailing;

import com.agnitas.emm.rest.v2.infrastructure.config.json.RestEnumAllowedValues;
import com.epages.restdocs.apispec.ParameterDescriptorWithType;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.restdocs.payload.FieldDescriptor;

abstract class _MailingsV2ControllerTestSupport extends _MailingsV2TestSupportBase {

    protected static final FieldDescriptor[] MAILING_LIGHT_RESPONSE_FIELDS = fields(
            LIGHT_RESPONSE_FIELDS,
            optionalField("sendDate", "Mailing send date"),
            optionalField("sentEmails", "Number of sent emails")
    );

    protected static final FieldDescriptor[] MAILING_RESPONSE_FIELDS = fields(
            RESPONSE_FIELDS,
            optionalField("sendDate", "Mailing send date"),
            optionalField("sentEmails", "Number of sent emails")
    );

    private static final FieldDescriptor[] MAILING_FULL_RESPONSE_FIELDS = fields(
            MAILING_RESPONSE_FIELDS,
            DEPENDENCIES_FIELDS
    );

    @Override
    protected String getDocsIdentifierPrefix() {
        return "mailings";
    }

    @Override
    protected String getDocsTag() {
        return "Mailing";
    }

    @Override
    protected ParameterDescriptorWithType[] getListQueryParams() {
        return ArrayUtils.add(
                super.getListQueryParams(),
                optionalParam("statuses", "Comma-Separated Values. %s".formatted(RestEnumAllowedValues.MAILING_STATUSES))
        );
    }

    @Override
    protected FieldDescriptor[] getLightResponseFields() {
        return MAILING_LIGHT_RESPONSE_FIELDS;
    }

    @Override
    protected FieldDescriptor[] getResponseFields() {
        return MAILING_RESPONSE_FIELDS;
    }

    @Override
    protected FieldDescriptor[] getCreateRequestFields() {
        return CREATE_REQUEST_FIELDS;
    }

    @Override
    protected FieldDescriptor[] getPatchRequestFields() {
        return PATCH_REQUEST_FIELDS;
    }

    @Override
    protected FieldDescriptor[] getFullResponseFields() {
        return MAILING_FULL_RESPONSE_FIELDS;
    }
}
