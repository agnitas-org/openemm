/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2.trigger;

import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.isA;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.action.operations.ActionOperationSubscribeCustomerParameters;
import com.agnitas.emm.rest.v2.infrastructure.search.PageResponse;
import com.agnitas.testsupport.annotation.MockBean;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;

class _TriggersV2ControllerTest extends _TriggersV2ControllerTestSupport {

    private static final int TRIGGER_ID = 123;
    private static final String BASE_URL = "/triggers";
    private static final String BY_ID_URL = "%s/{id}".formatted(BASE_URL);
    private static final String EXECUTE_URL = "%s/{id}/execute".formatted(BASE_URL);

    private static final ActionOperationSubscribeCustomerParameters subscribeCustomerAo
            = new ActionOperationSubscribeCustomerParameters();
    private static final TriggerResponse TRIGGER_RESPONSE = new TriggerResponse(
            123,
            "trigger subscription",
            "Description",
            1,
            true,
            false,
            Instant.parse("2024-01-02T03:04:56Z"),
            Instant.parse("2026-02-03T20:13:41Z"),
            List.of(subscribeCustomerAo)
    );
    private static final String RESPONSE_JSON =
            """
            {
                "id": 123,
                "name": "trigger subscription",
                "description": "Description",
                "type": 1,
                "isActive": true,
                "isAdvertising": false,
                "creationDate": "2024-01-02T03:04:56Z",
                "changeDate": "2026-02-03T20:13:41Z",
                "operations": [
                    {
                        "id": 1234,
                        "keyColumn": "firstname",
                        "actionId": 123,
                        "doubleOptIn": true
                    }
                ]
            }
            """;

    static {
        subscribeCustomerAo.setId(1234);
        subscribeCustomerAo.setKeyColumn("firstname");
        subscribeCustomerAo.setActionId(TRIGGER_ID);
        subscribeCustomerAo.setCompanyId(COMPANY_ID);
        subscribeCustomerAo.setDoubleOptIn(true);
    }

    @MockBean
    private TriggerRestService triggerRestServiceMock;

    @Nested
    class PaginatedList {
        @Test
        void shouldReturn200() throws Exception {
            expect(adminMock.allowedAll(Permission.ACTIONS_SHOW)).andReturn(true);
            expect(triggerRestServiceMock.find(PageRequest.of(3, 2), COMPANY_ID))
                    .andReturn(new PageResponse<>(
                            List.of(
                                    new TriggerLightResponse(
                                            1234, "Trigger 1", null, 0, false, true,
                                            Instant.parse("2026-01-01T10:10:10Z"),
                                            Instant.parse("2026-02-02T20:20:20Z")
                                    ),
                                    new TriggerLightResponse(
                                            1235, "Trigger 2", "Description", 1, true, false,
                                            Instant.parse("2025-01-01T10:10:10Z"),
                                            Instant.parse("2026-05-04T20:20:20Z")
                                    )
                            ), new PageResponse.PageMeta(3, 2, 12)
                    ));

            Map<String, Object> params = Map.of(
                    "page", 3,
                    "size", 2
            );
            authenticated(params(get(BASE_URL), params)).andExpectAll(
                    status().isOk(),
                    content().json(
                            """
                            {
                                "page": {
                                  "number": 3,
                                  "size": 2,
                                  "totalCount": 12
                                },
                                "list": [
                                    {
                                        "id": 1234,
                                        "name": "Trigger 1",
                                        "type": 0,
                                        "isActive": false,
                                        "isAdvertising": true,
                                        "creationDate": "2026-01-01T10:10:10Z",
                                        "changeDate": "2026-02-02T20:20:20Z"
                                    },
                                    {
                                        "id": 1235,
                                        "name": "Trigger 2",
                                        "description": "Description",
                                        "type": 1,
                                        "isActive": true,
                                        "isAdvertising": false,
                                        "creationDate": "2025-01-01T10:10:10Z",
                                        "changeDate": "2026-05-04T20:20:20Z"
                                    }
                                ]
                            }
                            """
                    )
            ).andDo(documentList());
        }

        @Test
        void shouldReturn403() throws Exception {
            expect(adminMock.allowedAll(Permission.ACTIONS_SHOW)).andReturn(false);
            assertNotAuthorized(get(BASE_URL));
        }
    }

    @Nested
    class ReadById {
        @Test
        void shouldReturn200() throws Exception {
            expect(adminMock.allowedAll(Permission.ACTIONS_SHOW)).andReturn(true);
            expect(triggerRestServiceMock.find(TRIGGER_ID, adminMock)).andReturn(TRIGGER_RESPONSE);

            authenticated(get(BY_ID_URL, TRIGGER_ID))
                    .andExpectAll(status().isOk(), content().json(RESPONSE_JSON))
                    .andDo(documentRead());
        }

        @Test
        void shouldReturn403() throws Exception {
            expect(adminMock.allowedAll(Permission.ACTIONS_SHOW)).andReturn(false);
            assertNotAuthorized(get(BY_ID_URL, TRIGGER_ID));
        }
    }

    @Nested
    class Create {
        @Test
        void shouldReturn201() throws Exception {
            expect(adminMock.allowedAll(Permission.ACTIONS_CHANGE)).andReturn(true);
            expect(triggerRestServiceMock.create(isA(CreateTriggerRequest.class), eq(adminMock)))
                    .andReturn(TRIGGER_RESPONSE);

            authenticated(post(BASE_URL).content(
                    """
                    {
                        "name": "trigger name",
                        "description": "Description",
                        "isActive": false,
                        "isAdvertising": true,
                        "type": 1,
                        "operations": [
                            {
                                "type": "SubscribeCustomer",
                                "keyColumn": "firstname",
                                "doubleOptIn": true,
                                "doubleCheck": true
                            }
                        ]
                    }
                    """
            )).andExpectAll(
                    status().isCreated(),
                    content().json(RESPONSE_JSON)
            ).andDo(documentCreate());
        }

        @Test
        void shouldReturn403() throws Exception {
            expect(adminMock.allowedAll(Permission.ACTIONS_CHANGE)).andReturn(false);
            assertNotAuthorized(post(BASE_URL).content(
                    """
                    {
                        "name": "trigger name",
                        "type": 1
                    }
                    """
            ));
        }
    }

    @Nested
    class PatchById {
        @Test
        void shouldReturn200() throws Exception {
            expect(adminMock.allowedAll(Permission.ACTIONS_CHANGE)).andReturn(true);
            expect(triggerRestServiceMock.patch(eq(TRIGGER_ID), isA(PatchTriggerRequest.class), eq(adminMock)))
                    .andReturn(TRIGGER_RESPONSE);

            authenticated(patch(BY_ID_URL, TRIGGER_ID).content(
                    """
                    {
                        "name": "trigger name",
                        "description": "Description",
                        "isActive": false,
                        "isAdvertising": true,
                        "type": 1,
                        "operations": [
                            {
                                "type": "SubscribeCustomer",
                                "keyColumn": "firstname",
                                "doubleOptIn": true,
                                "doubleCheck": true
                            }
                        ]
                    }
                    """
            )).andExpectAll(
                    status().isOk(),
                    content().json(RESPONSE_JSON)
            ).andDo(documentPatch());
        }

        @Test
        void shouldReturn403() throws Exception {
            expect(adminMock.allowedAll(Permission.ACTIONS_CHANGE)).andReturn(false);
            assertNotAuthorized(patch(BY_ID_URL, TRIGGER_ID).content("{}"));
        }
    }

    @Nested
    class Delete {
        @Test
        void shouldReturn204() throws Exception {
            expect(adminMock.allowedAll(Permission.ACTIONS_DELETE)).andReturn(true);
            triggerRestServiceMock.delete(TRIGGER_ID, COMPANY_ID);
            expectLastCall().once();

            authenticated(delete(BY_ID_URL, TRIGGER_ID))
                    .andExpect(status().isNoContent())
                    .andDo(documentDelete());
        }

        @Test
        void shouldReturn403() throws Exception {
            expect(adminMock.allowedAll(Permission.ACTIONS_DELETE)).andReturn(false);
            assertNotAuthorized(delete(BY_ID_URL, TRIGGER_ID));
        }
    }

    @Nested
    class Execute {
        @Test
        void shouldReturn200() throws Exception {
            expect(adminMock.allowedAll(Permission.ACTIONS_SHOW)).andReturn(true);
            triggerRestServiceMock.execute(TRIGGER_ID, COMPANY_ID, Map.of("triggerRequestParam", "value"));
            expectLastCall().once();

            authenticated(post(EXECUTE_URL, TRIGGER_ID).content(
                    """
                    {
                        "triggerRequestParam": "value"
                    }
                    """
            ))
                    .andExpect(status().isOk())
                    .andDo(documentExecute());
        }

        @Test
        void shouldReturn403() throws Exception {
            expect(adminMock.allowedAll(Permission.ACTIONS_SHOW)).andReturn(false);
            assertNotAuthorized(post(EXECUTE_URL, TRIGGER_ID).content("{}"));
        }
    }
}
