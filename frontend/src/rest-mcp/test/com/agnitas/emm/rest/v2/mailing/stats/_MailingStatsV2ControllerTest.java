/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2.mailing.stats;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.agnitas.emm.core.Permission;
import com.agnitas.testsupport.annotation.MockBean;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class _MailingStatsV2ControllerTest extends _MailingStatsV2ControllerTestSupport {

    private static final int MAILING_ID = 123;
    private static final String BASE_URL = "/mailings/{mailingId}/statistics";

    public static final MailingStatsResponse RESPONSE = new MailingStatsResponse(
            100, 80, 60,
            50, 5, 40, new MailingStatsResponse.ByDeviceClass(10, 10, 10, 10, 10),
            15, 20, 5, new MailingStatsResponse.ByDeviceClass(5, 5, 5, 5, 5),
            2, 3, 0.5
    );

    @MockBean
    private MailingStatsRestService statsRestServiceMock;

    @Nested
    class ReadStatistics {
        @Test
        void shouldReturn200() throws Exception {
            expect(adminMock.allowedAll(Permission.STATS_SHOW)).andReturn(true);
            expectLastCall();
            expect(statsRestServiceMock.getStats(MAILING_ID, adminMock)).andReturn(RESPONSE);

            authenticated(get(BASE_URL, MAILING_ID)).andExpectAll(
                    status().isOk(),
                    content().json(
                        """
                        {
                            "sentEmails": 100,
                            "openersMeasured": 80,
                            "clickersWithoutOpening": 60,
                            "openings": 50,
                            "openingsAnonymous": 5,
                            "openersExtrapolated": 40,
                            "openersByDeviceClass": {
                                "desktop": 10,
                                "tablet": 10,
                                "mobile": 10,
                                "smartTv": 10,
                                "multiple": 10
                            },
                            "clickers": 15,
                            "clicks": 20,
                            "clicksAnonymous": 5,
                            "clickersByDeviceClass": {
                                "desktop": 5,
                                "tablet": 5,
                                "mobile": 5,
                                "smartTv": 5,
                                "multiple": 5
                            },
                            "unsubscribers": 2,
                            "hardBounces": 3,
                            "revenue": 0.5
                        }
                        """
                    )
            ).andDo(documentRead());
        }

        @Test
        void shouldReturn403() throws Exception {
            expect(adminMock.allowedAll(Permission.STATS_SHOW)).andReturn(false);
            assertNotAuthorized(get(BASE_URL, MAILING_ID));
        }
    }
}
