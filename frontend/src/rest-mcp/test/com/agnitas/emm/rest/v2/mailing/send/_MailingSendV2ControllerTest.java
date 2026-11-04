/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2.mailing.send;

import static org.easymock.EasyMock.expect;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.OffsetDateTime;
import java.util.Set;

import com.agnitas.emm.core.Permission;
import com.agnitas.messages.Message;
import com.agnitas.testsupport.annotation.MockBean;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class _MailingSendV2ControllerTest extends _MailingSendV2ControllerTestSupport {

    private static final int MAILING_ID = 123;
    private static final String BASE_URL = "/mailings/{mailingId}";
    private static final String SEND_URL = BASE_URL + "/send";
    private static final String SEND_ADMIN_URL = BASE_URL + "/send-admin";
    private static final String SEND_TEST_URL = BASE_URL + "/send-test";
    private static final String CANCEL_URL = BASE_URL + "/cancel";
    private static final String ACTIVATE_URL = BASE_URL + "/activate";
    private static final String DEACTIVATE_URL = BASE_URL + "/deactivate";

    @MockBean
    private MailingSendRestService mailingSendRestServiceMock;

    @Nested
    class SendWorld {
        @Test
        void shouldReturn200() throws Exception {
            expect(adminMock.allowedAll(Permission.MAILING_SEND_WORLD)).andReturn(true);
            var request = new StartSendMailingRequest(OffsetDateTime.parse("2057-07-21T17:32:28Z"), 10, 200, true);
            expect(mailingSendRestServiceMock.sendWorld(MAILING_ID, adminMock, request))
                    .andReturn(MailingStartResponse.started(Set.of(Message.exact("Warning message"))));

            authenticated(post(SEND_URL, MAILING_ID).content(
                    """
                    {
                        "sendDate": "2057-07-21T17:32:28Z",
                        "stepping": "10",
                        "blockSize": "200",
                        "cleanupAdminAndTestActions": true
                    }
                    """
            )).andExpectAll(
                    status().isOk(),
                    content().json(
                            """
                            {
                              "status": "STARTED",
                              "message": "Mailing started",
                              "warnings": ["Warning message"]
                            }
                            """
                    )
            ).andDo(documentSendWorld());
        }

        @Test
        void shouldReturn403() throws Exception {
            expect(adminMock.allowedAll(Permission.MAILING_SEND_WORLD)).andReturn(false);
            assertNotAuthorized(post(SEND_URL, MAILING_ID));
        }
    }

    @Nested
    class SendAdmin {
        @Test
        void shouldReturn200() throws Exception {
            var request = new SendAdminMailingRequest(true);
            expect(mailingSendRestServiceMock.sendAdmin(MAILING_ID, adminMock, request))
                    .andReturn(MailingStartResponse.started(Set.of(Message.exact("Warning message"))));

            authenticated(post(SEND_ADMIN_URL, MAILING_ID).content(
                    """
                    {
                        "cleanupAdminAndTestActions": true
                    }
                    """
            )).andExpectAll(
                    status().isOk(),
                    content().json(
                            """
                            {
                              "status": "STARTED",
                              "message": "Mailing started",
                              "warnings": ["Warning message"]
                            }
                            """
                    )
            ).andDo(documentSendAdmin());
        }
    }

    @Nested
    class SendTest {
        @Test
        void shouldReturn200() throws Exception {
            var request = new SendTestMailingRequest(true);
            expect(mailingSendRestServiceMock.sendTest(MAILING_ID, adminMock, request))
                    .andReturn(MailingStartResponse.started(Set.of(Message.exact("Warning message"))));

            authenticated(post(SEND_TEST_URL, MAILING_ID).content(
                    """
                    {
                        "cleanupAdminAndTestActions": true
                    }
                    """
            )).andExpectAll(
                    status().isOk(),
                    content().json(
                            """
                            {
                              "status": "STARTED",
                              "message": "Mailing started",
                              "warnings": ["Warning message"]
                            }
                            """
                    )
            ).andDo(documentSendTest());
        }
    }

    @Nested
    class Activate {
        @Test
        void shouldReturn200() throws Exception {
            expect(adminMock.allowedAll(Permission.MAILING_SEND_WORLD)).andReturn(true);
            var request = new StartSendMailingRequest(OffsetDateTime.parse("2057-07-21T17:32:28Z"), 10, 200, true);

            expect(mailingSendRestServiceMock.activate(MAILING_ID, adminMock, request))
                    .andReturn(MailingStartResponse.activated(Set.of(Message.exact("Warning message"))));

            authenticated(post(ACTIVATE_URL, MAILING_ID).content(
                    """
                    {
                        "sendDate": "2057-07-21T17:32:28Z",
                        "stepping": "10",
                        "blockSize": "200",
                        "cleanupAdminAndTestActions": true
                    }
                    """
            )).andExpectAll(
                    status().isOk(),
                    content().json(
                            """
                            {
                              "status": "ACTIVATED",
                              "message": "Mailing activated",
                              "warnings": ["Warning message"]
                            }
                            """
                    )
            ).andDo(documentActivate());
        }

        @Test
        void shouldReturn403() throws Exception {
            expect(adminMock.allowedAll(Permission.MAILING_SEND_WORLD)).andReturn(false);
            assertNotAuthorized(post(ACTIVATE_URL, MAILING_ID));
        }
    }

    @Nested
    class Cancel {
        @Test
        void shouldReturn200() throws Exception {
            expect(adminMock.allowedAll(Permission.MAILING_SEND_WORLD)).andReturn(true);
            expect(mailingSendRestServiceMock.cancel(MAILING_ID, COMPANY_ID))
                    .andReturn(MailingStopResponse.stopped());

            authenticated(post(CANCEL_URL, MAILING_ID)).andExpectAll(
                    status().isOk(),
                    content().json(
                            """
                            {
                              "status": "STOPPED",
                              "message": "Mailing stopped"
                            }
                            """
                    )
            ).andDo(documentCancel());
        }

        @Test
        void shouldReturn403() throws Exception {
            expect(adminMock.allowedAll(Permission.MAILING_SEND_WORLD)).andReturn(false);
            assertNotAuthorized(post(CANCEL_URL, MAILING_ID));
        }
    }

    @Nested
    class Deactivate {
        @Test
        void shouldReturn200() throws Exception {
            expect(adminMock.allowedAll(Permission.MAILING_SEND_WORLD)).andReturn(true);
            expect(mailingSendRestServiceMock.deactivate(MAILING_ID, COMPANY_ID))
                    .andReturn(MailingStopResponse.deactivated());

            authenticated(post(DEACTIVATE_URL, MAILING_ID)).andExpectAll(
                    status().isOk(),
                    content().json(
                            """
                            {
                              "status": "DEACTIVATED",
                              "message": "Mailing deactivated"
                            }
                            """
                    )
            ).andDo(documentDeactivate());
        }

        @Test
        void shouldReturn403() throws Exception {
            expect(adminMock.allowedAll(Permission.MAILING_SEND_WORLD)).andReturn(false);
            assertNotAuthorized(post(DEACTIVATE_URL, MAILING_ID));
        }
    }
}
