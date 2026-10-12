/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.mailinglist;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.birtstatistics.recipient.PeriodProgressUiRow;
import com.agnitas.emm.core.birtstatistics.recipient.RecipientStatsUiMapper;
import com.agnitas.emm.core.commons.dto.LocalDateRange;
import com.agnitas.emm.core.stats.recipient.RecipientStatsQuery;
import com.agnitas.emm.core.stats.recipient.RecipientStatsService;
import com.agnitas.emm.core.target.service.TargetService;
import org.springframework.stereotype.Service;

@Service
public class MailinglistUiService {

    protected static final DateTimeFormatter STATS_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    protected final RecipientStatsUiMapper mapper;
    protected final RecipientStatsService recipientStatsService;
    private final TargetService targetService;

    public MailinglistUiService(
            RecipientStatsUiMapper mapper,
            RecipientStatsService recipientStatsService,
            TargetService targetService
    ) {
        this.mapper = mapper;
        this.recipientStatsService = recipientStatsService;
        this.targetService = targetService;
    }

    public List<PeriodProgressUiRow> getStats(int mailinglistId, int month, int year, Admin admin) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate from = yearMonth.atDay(1);
        LocalDate to = yearMonth.atEndOfMonth();
        RecipientStatsQuery recipientStatsQuery = new RecipientStatsQuery(
                admin.getCompanyID(),
                0,
                mailinglistId,
                targetService.getAltgIds(admin),
                null,
                new LocalDateRange(from, to, admin.getZoneId())
        );
        return recipientStatsService.getDailyProgress(recipientStatsQuery)
                .entrySet().stream()
                .map(entry -> mapper.toUiRow(entry.getValue(), entry.getKey().format(STATS_DATE_FORMATTER)))
                .toList();
    }
}
