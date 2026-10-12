/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.mailinglist;

import static com.helger.commons.mock.CommonsAssert.assertEquals;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;

import java.time.LocalDate;
import java.time.Month;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.birtstatistics.dto.RecipientProgressDetails;
import com.agnitas.emm.core.birtstatistics.recipient.PeriodProgressUiRow;
import com.agnitas.emm.core.stats.recipient.RecipientStatsQuery;
import com.agnitas.emm.core.stats.recipient.RecipientStatsService;
import com.agnitas.emm.core.target.service.TargetService;
import com.agnitas.testsupport.EmmEasyMockSupport;
import com.agnitas.testsupport.annotation.InjectMocks;
import com.agnitas.testsupport.extension.EmmMockExtension;
import com.agnitas.testsupport.utils.MvcTestUtilities;
import org.easymock.Mock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(EmmMockExtension.class)
class _MailinglistUiServiceTest extends EmmEasyMockSupport {

    @Mock
    private RecipientStatsService recipientStatsServiceMock;
    @Mock
    private TargetService targetServiceMock;
    @Mock
    private Admin adminMock;

    @InjectMocks
    private MailinglistUiService underTest;

    @BeforeEach
    void setUp() {
        MvcTestUtilities.setupAdmin(adminMock);
    }

    @Nested
    class GetStats {
        @Test
        void shouldReturnStatsForEntireMonth() {
            // given
            Map<LocalDate, RecipientProgressDetails> data = IntStream.rangeClosed(1, 31).boxed()
                    .collect(Collectors.toMap(
                            day -> LocalDate.of(2026, Month.JULY, day),
                            day -> createProgressDetails(day, day + 1, day + 2, day + 3, day + 4)
                    ));
            expect(targetServiceMock.getAltgIds(adminMock)).andReturn(Set.of(101, 102));
            expect(recipientStatsServiceMock.getDailyProgress(isA(RecipientStatsQuery.class))).andReturn(data);
            replayAll();

            // when
            var actual = underTest.getStats(123, 7, 2026, adminMock);

            // then
            assertEquals(31, actual.size());
            for (int i = 0; i < 31; i++) {
                PeriodProgressUiRow row = actual.get(i);
                int expectedDay = 31 - i;
                assertEquals(String.format("%02d.07.2026", expectedDay), row.period());
                assertEquals(expectedDay, row.optIns());
                assertEquals(expectedDay + 1, row.optOuts());
                assertEquals(expectedDay + 2, row.bounced());
                assertEquals(expectedDay + 3, row.optInWithoutConfirm());
                assertEquals(expectedDay + 4, row.blocklisted());
            }
        }
    }

    private RecipientProgressDetails createProgressDetails(
            long optIns,
            long optOuts,
            long bounced,
            long optInWithoutConfirm,
            long blocklisted
    ) {
        RecipientProgressDetails details = new RecipientProgressDetails();
        details.setOptIns(optIns);
        details.setOptOuts(optOuts);
        details.setBounced(bounced);
        details.setOptInWithoutConfirm(optInWithoutConfirm);
        details.setBlocklisted(blocklisted);
        return details;
    }
}
