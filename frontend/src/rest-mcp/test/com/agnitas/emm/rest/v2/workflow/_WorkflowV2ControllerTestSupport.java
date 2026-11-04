/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2.workflow;

import static com.epages.restdocs.apispec.ResourceSnippetParameters.builder;

import com.agnitas.emm.rest.v2.RestDocsTestSupport;
import com.epages.restdocs.apispec.ParameterDescriptorWithType;
import com.epages.restdocs.apispec.Schema;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.test.web.servlet.ResultHandler;

public abstract class _WorkflowV2ControllerTestSupport extends RestDocsTestSupport {

    private static final ParameterDescriptorWithType ID_PATH_PARAM = intParam("id", "Workflow ID");

    private static final FieldDescriptor[] STATUS_RESPONSE_FIELDS = {
            requiredField("workflowId", "Unique ID of the workflow"),
            requiredField("status", "Current workflow status"),
    };

    private static final FieldDescriptor[] WORKFLOW_LIGHT_RESPONSE_FIELDS = {
            requiredField("id", "Unique ID of the workflow"),
            requiredField("name", "Name of workflow"),
            requiredField("description", "Description of workflow"),
            requiredField("status", "Current status of workflow"),
            optionalField("pauseTime", "Timestamp when workflow has been paused"),
            optionalField("pauseExpirationHours", "Configured pause expiration hours"),
    };

    private static final FieldDescriptor[] PAUSE_RESPONSE_FIELDS = fields(
            STATUS_RESPONSE_FIELDS,
            requiredField("pauseTime", "Timestamp when workflow has been paused"),
            requiredField("pauseExpirationHours", "Configured pause expiration hours")
    );

    private final Schema statusResponseSchema = schema("StatusResponse");

    @Override
    protected String getDocsTag() {
        return "Workflow";
    }

    @Override
    protected String getDocsIdentifierPrefix() {
        return "workflows";
    }

    protected ResultHandler documentList() {
        return documentPagination(WORKFLOW_LIGHT_RESPONSE_FIELDS);
    }

    protected ResultHandler documentActivate() {
        return document("activate", builder()
                .summary("Activate a workflow by ID")
                .pathParameters(ID_PATH_PARAM)
                .responseSchema(statusResponseSchema)
                .responseFields(STATUS_RESPONSE_FIELDS)
        );
    }

    protected ResultHandler documentDeactivate() {
        return document("deactivate", builder()
                .summary("Deactivate a workflow by ID")
                .pathParameters(ID_PATH_PARAM)
                .responseSchema(statusResponseSchema)
                .responseFields(STATUS_RESPONSE_FIELDS)
        );
    }

    protected ResultHandler documentPause() {
        return document("pause", builder()
                .summary("Pause a workflow by ID")
                .pathParameters(ID_PATH_PARAM)
                .responseSchema(schema("PauseResponse"))
                .responseFields(PAUSE_RESPONSE_FIELDS)
        );
    }
}
