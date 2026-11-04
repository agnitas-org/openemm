/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2.workflow;

import static org.easymock.EasyMock.expect;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.workflow.beans.Workflow;
import com.agnitas.emm.rest.v2.infrastructure.search.PageResponse;
import com.agnitas.testsupport.annotation.MockBean;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

public class _WorkflowV2ControllerTest extends _WorkflowV2ControllerTestSupport {

    private static final int WORKFLOW_ID = 123;
    private static final String BASE_URL = "/workflows";
    private static final String BY_ID_URL = BASE_URL + "/{id}";
    private static final String ACTIVATE_URL = BY_ID_URL + "/activate";
    private static final String DEACTIVATE_URL = BY_ID_URL + "/deactivate";
    private static final String PAUSE_URL = BY_ID_URL + "/pause";

    @MockBean
    private WorkflowRestService workflowRestServiceMock;

    @Nested
    class ListWorkflows {
        @Test
        void shouldReturn403_whenUserHasNoPermission() throws Exception {
            expect(adminMock.allowedAll(Permission.WORKFLOW_SHOW)).andReturn(false);

            MockHttpServletRequestBuilder action = get(BASE_URL);
            assertNotAuthorized(action);
        }

        @Test
        void shouldReturn200() throws Exception {
            expect(adminMock.allowedAll(Permission.WORKFLOW_SHOW)).andReturn(true);
            expect(workflowRestServiceMock.find(PageRequest.of(3, 2), adminMock)).andReturn(new PageResponse<>(List.of(
                    new WorkflowLightResponse(
                            1,
                            "Welcome Track",
                            "Welcome new customers",
                            "open",
                            null,
                            0
                    ),
                    new WorkflowLightResponse(
                            2,
                            "Abandoned Cart",
                            "Recover abandoned carts",
                            "paused",
                            Instant.parse("2026-06-03T18:43:04Z"),
                            4
                    )
            ), new PageResponse.PageMeta(3, 2, 15)));

            Map<String, Object> params = Map.of(
                    "page", 3,
                    "size", 2
            );
            authenticated(params(get(BASE_URL), params)).andExpectAll(
                    status().isOk(),
                    content().json("""
                        {
                            "page": {
                                "number": 3,
                                "size": 2,
                                "totalCount": 15
                            },
                            "list": [
                                {
                                    "id": 1,
                                    "name": "Welcome Track",
                                    "description": "Welcome new customers",
                                    "status": "open"
                                },
                                {
                                    "id": 2,
                                    "name": "Abandoned Cart",
                                    "description": "Recover abandoned carts",
                                    "status": "paused",
                                    "pauseTime": "2026-06-03T18:43:04Z",
                                    "pauseExpirationHours": 4
                                }
                            ]
                        }
                        """
                    )
            ).andDo(documentList());
        }
    }

    @Nested
    class ActivateWorkflow {
        @Test
        void shouldReturn403_whenUserHasNoPermission() throws Exception {
            expect(adminMock.allowedAll(Permission.WORKFLOW_ACTIVATE)).andReturn(false);

            MockHttpServletRequestBuilder action = post(ACTIVATE_URL, WORKFLOW_ID).content("{}");
            assertNotAuthorized(action);
        }

        @Test
        void shouldReturn200() throws Exception {
            expect(adminMock.allowedAll(Permission.WORKFLOW_ACTIVATE)).andReturn(true);
            expect(workflowRestServiceMock.activate(WORKFLOW_ID, COMPANY_ID)).andReturn(new WorkflowStatusResponse(
                    WORKFLOW_ID,
                    Workflow.WorkflowStatus.STATUS_ACTIVE.getName()
            ));

            authenticated(post(ACTIVATE_URL, WORKFLOW_ID)).andExpectAll(
                    status().isOk(),
                    content().json("""
                            {
                              "workflowId": 123,
                              "status": "active"
                            }
                            """
                    )
            ).andDo(documentActivate());
        }

        @Test
        void shouldReturn400_whenIdIsZero() throws Exception {
            authenticated(post(ACTIVATE_URL, 0)).andExpectAll(
                    status().isBadRequest(),
                    content().json("""
                            {
                                "status": 400,
                                "detail": "Invalid input.",
                                "errors": [
                                    {
                                        "field": "workflowId",
                                        "message": "must be greater than 0"
                                    }
                                ]
                            }
                            """
                    )
            );
        }
    }

    @Nested
    class DeactivateWorkflow {
        @Test
        void shouldReturn403_whenUserHasNoPermission() throws Exception {
            expect(adminMock.allowedAll(Permission.WORKFLOW_ACTIVATE)).andReturn(false);

            MockHttpServletRequestBuilder action = post(DEACTIVATE_URL, WORKFLOW_ID).content("{}");
            assertNotAuthorized(action);
        }

        @Test
        void shouldReturn200() throws Exception {
            expect(adminMock.allowedAll(Permission.WORKFLOW_ACTIVATE)).andReturn(true);
            expect(workflowRestServiceMock.deactivate(WORKFLOW_ID, COMPANY_ID)).andReturn(new WorkflowStatusResponse(
                    WORKFLOW_ID,
                    Workflow.WorkflowStatus.STATUS_INACTIVE.getName()
            ));

            authenticated(post(DEACTIVATE_URL, WORKFLOW_ID)).andExpectAll(
                    status().isOk(),
                    content().json("""
                            {
                              "workflowId": 123,
                              "status": "inactive"
                            }
                            """
                    )
            ).andDo(documentDeactivate());
        }

        @Test
        void shouldReturn400_whenIdIsZero() throws Exception {
            authenticated(post(DEACTIVATE_URL, 0)).andExpectAll(
                    status().isBadRequest(),
                    content().json("""
                            {
                                "status": 400,
                                "detail": "Invalid input.",
                                "errors": [
                                    {
                                        "field": "workflowId",
                                        "message": "must be greater than 0"
                                    }
                                ]
                            }
                            """
                    )
            );
        }
    }

    @Nested
    class PauseWorkflow {
        @Test
        void shouldReturn403_whenUserHasNoPermission() throws Exception {
            expect(adminMock.allowedAll(Permission.WORKFLOW_ACTIVATE)).andReturn(false);

            MockHttpServletRequestBuilder action = post(PAUSE_URL, WORKFLOW_ID).content("{}");
            assertNotAuthorized(action);
        }

        @Test
        void shouldReturn200() throws Exception {
            expect(adminMock.allowedAll(Permission.WORKFLOW_ACTIVATE)).andReturn(true);
            expect(workflowRestServiceMock.pause(WORKFLOW_ID, COMPANY_ID)).andReturn(new WorkflowPauseResponse(
                    WORKFLOW_ID,
                    Workflow.WorkflowStatus.STATUS_PAUSED.getName(),
                    Instant.parse("2026-06-03T18:43:04Z"),
                    3
            ));

            authenticated(post(PAUSE_URL, WORKFLOW_ID)).andExpectAll(
                    status().isOk(),
                    content().json("""
                            {
                              "workflowId": 123,
                              "status": "paused",
                              "pauseTime": "2026-06-03T18:43:04Z",
                              "pauseExpirationHours": 3
                            }
                            """
                    )
            ).andDo(documentPause());
        }

        @Test
        void shouldReturn400_whenIdIsZero() throws Exception {
            authenticated(post(PAUSE_URL, 0)).andExpectAll(
                    status().isBadRequest(),
                    content().json("""
                            {
                                "status": 400,
                                "detail": "Invalid input.",
                                "errors": [
                                    {
                                        "field": "workflowId",
                                        "message": "must be greater than 0"
                                    }
                                ]
                            }
                            """
                    )
            );
        }
    }
}
