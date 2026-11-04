/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2.user;

import static com.epages.restdocs.apispec.ResourceSnippetParameters.builder;
import static com.epages.restdocs.apispec.SimpleType.NUMBER;

import com.agnitas.emm.rest.v2.RestDocsTestSupport;
import com.epages.restdocs.apispec.ParameterDescriptorWithType;
import com.epages.restdocs.apispec.Schema;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.test.web.servlet.ResultHandler;

public abstract class _UiUsersV2ControllerTestSupport extends RestDocsTestSupport {

    private static final ParameterDescriptorWithType USERNAME_PATH_PARAM = stringParam("username", "Username");

    private static final FieldDescriptor[] USER_LIGHT_RESPONSE_FIELDS = {
            requiredField("id", "Unique user ID"),
            requiredField("clientId", "Company/client ID"),
            requiredField("username", "Username of the user"),
            requiredField("firstname", "First name of the user"),
            requiredField("lastname", "Last name of the user"),
            requiredField("email", "Email address of the user"),
    };

    private static final FieldDescriptor[] USER_RESPONSE_FIELDS = fields(
            requiredField("id", "Unique user ID"),
            requiredField("username", "Username of the user"),
            requiredField("clientId", "Company/client ID"),
            requiredField("company", "Company name"),
            requiredField("gender", "Gender/title prefix"),
            requiredField("title", "User title"),
            requiredField("firstname", "First name of the user"),
            requiredField("lastname", "Last name of the user"),
            requiredField("email", "Email address of the user"),
            requiredField("phone", "Phone number"),
            requiredField("statisticsEmail", "Statistics email address"),
            requiredField("language", "Language code"),
            requiredField("timezone", "Timezone"),
            requiredField("creationDate", "Account creation timestamp"),
            requiredField("lastPasswordChange", "Last password change timestamp"),
            requiredField("lastLoginDate", "Last login timestamp"),
            requiredField("lastNewsDate", "Last newsletter date"),
            requiredField("lastMessageDate", "Last message date"),
            optionalField("groups", "User groups"),
            optionalField("permissions", "User permissions"),
            optionalField("accessLimitingTargetGroupIds", "Access limiting target group IDs"),
            optionalField("disabledMailingListIds", "Disabled mailing list IDs"),
            applyPrefix("groups[]",
                    requiredField("id", "Group ID"),
                    requiredField("name", "Group name"),
                    requiredField("description", "Group description")
            ),
            applyPrefix("permissions[]",
                    requiredField("code", "Permission code"),
                    requiredField("name", "Permission name")
            )
    );

    private static final FieldDescriptor[] CREATE_REQUEST_FIELDS = {
            requiredField("clientId", "Child Company/client ID to create suer for"),
            requiredField("username", "Username of the new user"),
            requiredField("password", "Password for the new user account"),
            requiredField("company", "Company name"),
            optionalField("gender", "Gender/title prefix"),
            optionalField("title", "User title"),
            requiredField("firstName", "First name of the user"),
            requiredField("lastName", "Last name of the user"),
            requiredField("email", "Email address of the user"),
            optionalField("statisticsEmail", "Statistics email address"),
            optionalField("phone", "Phone number"),
            requiredField("language", "Language code in format 'lang_COUNTRY' (e.g., 'en_US')"),
            optionalField("timezone", "Timezone"),
            optionalField("employeeId", "Employee ID"),
            requiredField("groupIds", "User group IDs"),
            optionalField("permissions", "Permission tokens"),
            optionalField("accessLimitingTargetGroupIds", "Access limiting target group IDs"),
            optionalField("accessLimitingTargetGroupId", "Access limiting target group ID").type(NUMBER),
            optionalField("disabledMailingListIds", "Disabled mailing list IDs"),
    };

    private static final FieldDescriptor[] PATCH_REQUEST_FIELDS = {
            optionalField("username", "New username"),
            optionalField("password", "New password"),
            optionalField("company", "Company name"),
            optionalField("gender", "Gender/title prefix"),
            optionalField("title", "User title"),
            optionalField("firstName", "First name"),
            optionalField("lastName", "Last name"),
            optionalField("email", "Email address"),
            optionalField("statisticsEmail", "Statistics email address"),
            optionalField("phone", "Phone number"),
            optionalField("language", "Language code"),
            optionalField("timezone", "Timezone"),
            optionalField("employeeId", "Employee ID"),
            optionalField("groupIds", "User group IDs"),
            optionalField("permissions", "Permission tokens"),
            optionalField("accessLimitingTargetGroupIds", "Access limiting target group IDs"),
            optionalField("accessLimitingTargetGroupId", "Access limiting target group ID").type(NUMBER),
            optionalField("disabledMailingListIds", "Disabled mailing list IDs"),
    };

    private final Schema responseSchema = schema("Response");
    private final Schema patchRequestSchema = schema("PatchRequest");

    @Override
    protected String getDocsTag() {
        return "UI User";
    }

    @Override
    protected String getDocsIdentifierPrefix() {
        return "ui-users";
    }

    protected ResultHandler documentList() {
        return documentPagination(USER_LIGHT_RESPONSE_FIELDS);
    }

    protected ResultHandler documentReadById() {
        return document("read-by-id", builderWithIdParam()
                .summary("Read user data")
                .responseSchema(responseSchema)
                .responseFields(USER_RESPONSE_FIELDS)
        );
    }

    protected ResultHandler documentReadByUsername() {
        return document("read-by-username", builder()
                .summary("Read user data by username")
                .pathParameters(USERNAME_PATH_PARAM)
                .responseSchema(responseSchema)
                .responseFields(USER_RESPONSE_FIELDS)
        );
    }

    protected ResultHandler documentDeleteById() {
        return document("delete-by-id", builderWithIdParam()
                .summary("Delete a user account")
        );
    }

    protected ResultHandler documentDeleteByUsername() {
        return document("delete-by-username", builder()
                .summary("Delete a user account by username")
                .pathParameters(USERNAME_PATH_PARAM)
        );
    }

    protected ResultHandler documentCreate() {
        return document("create", builder()
                .summary("Create a new user account")
                .requestFields(CREATE_REQUEST_FIELDS)
                .requestSchema(schema("CreateRequest"))
                .responseSchema(responseSchema)
                .responseFields(USER_RESPONSE_FIELDS)
        );
    }

    protected ResultHandler documentPatchById() {
        return document("patch-by-id", builderWithIdParam()
                .summary("Update user data")
                .requestSchema(patchRequestSchema)
                .requestFields(PATCH_REQUEST_FIELDS)
                .responseSchema(responseSchema)
                .responseFields(USER_RESPONSE_FIELDS)
        );
    }

    protected ResultHandler documentPatchByUsername() {
        return document("patch-by-username", builder()
                .summary("Update user data by username")
                .pathParameters(USERNAME_PATH_PARAM)
                .requestFields(PATCH_REQUEST_FIELDS)
                .requestSchema(patchRequestSchema)
                .responseSchema(responseSchema)
                .responseFields(USER_RESPONSE_FIELDS)
        );
    }
}
