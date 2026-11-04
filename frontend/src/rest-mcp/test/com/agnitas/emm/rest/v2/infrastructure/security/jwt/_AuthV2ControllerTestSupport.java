/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2.infrastructure.security.jwt;
import static com.epages.restdocs.apispec.ResourceSnippetParameters.builder;

import com.agnitas.emm.rest.v2.RestDocsTestSupport;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.test.web.servlet.ResultHandler;

abstract class _AuthV2ControllerTestSupport extends RestDocsTestSupport {

    private static final FieldDescriptor[] JWT_RESPONSE_FIELDS = {
            requiredField("token", "Generated JWT token string"),
            requiredField("validity", "Time till this token is valid"),
    };

    @Override
    protected String getDocsTag() {
        return "Authentication";
    }

    @Override
    protected String getDocsIdentifierPrefix() {
        return "auth";
    }

    ResultHandler documentCreateJwt() {
        return document("create-jwt", builder()
                .summary("Create JWT token")
                .responseFields(JWT_RESPONSE_FIELDS)
                .responseSchema(schema("JwtResponse"))
                .responseFields()
        );
    }
}
