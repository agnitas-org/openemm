/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtstatistics.recipient;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.stream.Collectors;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.birtstatistics.dto.RecipientProgress;
import com.agnitas.emm.core.commons.dto.LocalDateRange;
import com.agnitas.emm.core.stats.recipient.RecipientStatsQuery;
import com.agnitas.emm.core.stats.recipient.RecipientStatsService;
import com.agnitas.exception.UiMessageException;
import com.agnitas.util.AgnUtils;
import org.springframework.stereotype.Service;

@Service
class RecipientStatsUiService {

    private static final DateTimeFormatter STATS_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final RecipientStatsUiMapper mapper;
    private final RecipientStatsService recipientStatsService;

    public RecipientStatsUiService(
            RecipientStatsUiMapper mapper,
            RecipientStatsService recipientStatsService
    ) {
        this.mapper = mapper;
        this.recipientStatsService = recipientStatsService;
    }

    public Map<String, RecipientProgress> getProgress(RecipientStatsForm form, Admin admin) {
        LocalDateRange dateRange = resolveDateRange(form, admin);
        RecipientStatsQuery query = mapper.toQuery(form, dateRange);

        Map<LocalDate, RecipientProgress> stats = recipientStatsService.getDailyProgress(admin.getCompanyID(), query);
        return stats.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> entry.getKey().format(STATS_DATE_FORMATTER),
                        Map.Entry::getValue,
                        (existing, replacement) -> existing
                ));
    }

    private LocalDateRange resolveDateRange(RecipientStatsForm form, Admin admin) {
        validatePeriod(form.getStartDate(), form.getEndDate(), admin);
        return switch (form.getDateMode()) {
            case LAST_WEEK -> {
                var now = LocalDate.now();
                yield new LocalDateRange(
                        now.minusWeeks(1),
                        now.minusDays(1)
                );
            }
            case SELECT_PERIOD -> new LocalDateRange(
                    parse(form.getStartDate(), admin),
                    parse(form.getEndDate(), admin)
            );
            default -> {
                var start = LocalDate.of(form.getYear(), form.getMonth(), 1);
                yield new LocalDateRange(
                        start,
                        start.plusMonths(1).minusDays(1)
                );
            }
        };
    }

    private void validatePeriod(String startDate, String endDate, Admin admin) {
        if (isBlank(startDate) && isBlank(endDate)){
            return;
        }
        String pattern = admin.getDateFormat().toPattern();
        if (!AgnUtils.isDateValid(startDate, pattern) || !AgnUtils.isDateValid(endDate, pattern)) {
            throw new UiMessageException("error.date.format");
        }

        if (!AgnUtils.isDatePeriodValid(startDate, endDate, pattern)) {
            throw new UiMessageException("error.period.format");
        }
    }

    private LocalDate parse(String value, Admin admin) {
        if (isBlank(value)) {
            return null;
        }
        return LocalDate.parse(value, admin.getDateFormatter());
    }
}
