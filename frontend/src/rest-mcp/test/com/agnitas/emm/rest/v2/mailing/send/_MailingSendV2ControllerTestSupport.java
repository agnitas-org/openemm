/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2.mailing.send;

import static com.epages.restdocs.apispec.ResourceSnippetParameters.builder;

import java.util.Arrays;
import java.util.stream.Collectors;

import com.agnitas.emm.rest.v2.ConstrainedFields;
import com.agnitas.emm.rest.v2.RestDocsTestSupport;
import com.epages.restdocs.apispec.ParameterDescriptorWithType;
import com.epages.restdocs.apispec.SimpleType;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.test.web.servlet.ResultHandler;

public abstract class _MailingSendV2ControllerTestSupport extends RestDocsTestSupport {

    private static final ConstrainedFields SEND_WORLD_CONSTRAINTS = new ConstrainedFields(StartSendMailingRequest.class);
    private static final ConstrainedFields SEND_TEST_CONSTRAINTS = new ConstrainedFields(SendTestMailingRequest.class);
    private static final ConstrainedFields SEND_ADMIN_CONSTRAINTS = new ConstrainedFields(SendAdminMailingRequest.class);

    private static final ParameterDescriptorWithType ID_PATH_PARAM = intParam("mailingId", "Mailing ID");

    private static final FieldDescriptor[] SEND_WORLD_REQUEST_FIELDS = {
            optionalField(SEND_WORLD_CONSTRAINTS, "sendDate", "Optional date-time of sending (ISO-8601). Not needed for actionbased mailings.").type(SimpleType.STRING),
            optionalField(SEND_WORLD_CONSTRAINTS, "stepping", "Steps in seconds to delay sending of each blocksize"),
            optionalField(SEND_WORLD_CONSTRAINTS, "blockSize", "Max recipients per generated block"),
            optionalField(SEND_WORLD_CONSTRAINTS, "cleanupAdminAndTestActions", "Removes admin and test actions if set to true"),
    };

    private static final FieldDescriptor[] SEND_TEST_REQUEST_FIELDS = {
            optionalField(SEND_TEST_CONSTRAINTS, "cleanupAdminAndTestActions", "Removes admin and test actions if set to true"),
    };

    private static final FieldDescriptor[] SEND_ADMIN_REQUEST_FIELDS = {
            optionalField(SEND_ADMIN_CONSTRAINTS, "cleanupAdminAndTestActions", "Removes admin and test actions if set to true"),
    };

    private static final FieldDescriptor[] SEND_RESPONSE_FIELDS = {
            requiredField("status", "Values: [%s]".formatted(startStatusesCsv())),
            requiredField("message", "Message with details"),
            requiredField("warnings[]", "Warning messages"),
    };

    private static final FieldDescriptor[] CANCEL_RESPONSE_FIELDS = {
            requiredField("status", "Values: [%s]".formatted(stopStatusesCsv())),
            requiredField("message", "Message with details"),
    };

    @Override
    protected String getDocsTag() {
        return "Mailing Delivery";
    }

    @Override
    protected String getDocsIdentifierPrefix() {
        return "mailing-delivery";
    }

    protected ResultHandler documentSendWorld() {
        return document("send-normal", builder()
                .summary("Start delivery of the normal mailing")
                .pathParameters(ID_PATH_PARAM)
                .requestSchema(schema("SendNormalRequest"))
                .requestFields(SEND_WORLD_REQUEST_FIELDS)
                .responseSchema(schema("SendResponse"))
                .responseFields(SEND_RESPONSE_FIELDS)
        );
    }

    protected ResultHandler documentActivate() {
        return document("activate", builder()
                .summary("Activate action- or date-based mailing")
                .pathParameters(ID_PATH_PARAM)
                .requestSchema(schema("SendNormalRequest"))
                .requestFields(SEND_WORLD_REQUEST_FIELDS)
                .responseSchema(schema("SendResponse"))
                .responseFields(SEND_RESPONSE_FIELDS)
        );
    }

    protected ResultHandler documentSendAdmin() {
        return document("send-admin", builder()
                .summary("Send Admin test mailing")
                .pathParameters(ID_PATH_PARAM)
                .requestSchema(schema("SendAdminRequest"))
                .requestFields(SEND_ADMIN_REQUEST_FIELDS)
                .responseSchema(schema("SendResponse"))
                .responseFields(SEND_RESPONSE_FIELDS)
        );
    }

    protected ResultHandler documentSendTest() {
        return document("send-test", builder()
                .summary("Send test mailing")
                .pathParameters(ID_PATH_PARAM)
                .requestSchema(schema("SendTestRequest"))
                .requestFields(SEND_TEST_REQUEST_FIELDS)
                .responseSchema(schema("SendResponse"))
                .responseFields(SEND_RESPONSE_FIELDS)
        );
    }

    protected ResultHandler documentCancel() {
        return document("cancel", builder()
                .summary("Cancel mailing delivery")
                .pathParameters(ID_PATH_PARAM)
                .responseSchema(schema("CancelResponse"))
                .responseFields(CANCEL_RESPONSE_FIELDS)
        );
    }

    protected ResultHandler documentDeactivate() {
        return document("deactivate", builder()
                .summary("")
                .pathParameters(ID_PATH_PARAM)
                .responseSchema(schema("CancelResponse"))
                .responseFields(CANCEL_RESPONSE_FIELDS)
        );
    }

    private static String startStatusesCsv() {
        return Arrays.stream(MailingStartResponse.Status.values())
                .map(Enum::name)
                .collect(Collectors.joining(","));
    }

    private static String stopStatusesCsv() {
        return Arrays.stream(MailingStopResponse.Status.values())
                .map(Enum::name)
                .collect(Collectors.joining(","));
    }
}
