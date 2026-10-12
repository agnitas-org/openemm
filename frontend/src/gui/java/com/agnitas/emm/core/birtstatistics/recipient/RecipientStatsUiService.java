/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.birtstatistics.recipient;

import static com.agnitas.messages.I18nString.t;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.agnitas.beans.Admin;
import com.agnitas.emm.common.UserStatus;
import com.agnitas.emm.core.commons.dto.LocalDateRange;
import com.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.core.commons.util.ConfigValue;
import com.agnitas.emm.core.recipientsreport.bean.SummedRecipientStatus;
import com.agnitas.emm.core.stats.recipient.RecipientStatsQuery;
import com.agnitas.emm.core.stats.recipient.RecipientStatsService;
import com.agnitas.emm.core.target.service.TargetService;
import com.agnitas.exception.UiMessageException;
import com.agnitas.messages.Message;
import org.springframework.stereotype.Service;

@Service("recipientStatsUiService")
class RecipientStatsUiService {

    protected static final DateTimeFormatter STATS_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    protected final RecipientStatsUiMapper mapper;
    protected final RecipientStatsService recipientStatsService;
    private final ConfigService configService;
    private final TargetService targetService;

    public RecipientStatsUiService(
            RecipientStatsUiMapper mapper,
            RecipientStatsService recipientStatsService,
            ConfigService configService,
            TargetService targetService
    ) {
        this.mapper = mapper;
        this.recipientStatsService = recipientStatsService;
        this.configService = configService;
        this.targetService = targetService;
    }

    public List<PeriodProgressUiRow> getProgress(RecipientStatsForm form, Admin admin) {
        return recipientStatsService.getDailyProgress(toQuery(form, admin))
                .entrySet().stream()
                .map(entry -> mapper.toUiRow(entry.getValue(), entry.getKey().format(STATS_DATE_FORMATTER)))
                .toList();
    }

    protected RecipientStatsQuery toQuery(RecipientStatsForm form, Admin admin) {
        Set<Integer> altgIds = targetService.getAltgIds(admin);
        LocalDateRange dateRange = resolveDateRange(form, admin);
        return mapper.toQuery(admin.getCompanyID(), form, dateRange, altgIds);
    }

    private LocalDateRange resolveDateRange(RecipientStatsForm form, Admin admin) {
        return switch (form.getDateMode()) {
            case LAST_WEEK -> {
                var now = LocalDate.now();
                yield new LocalDateRange(now.minusWeeks(1), now.minusDays(1), admin.getZoneId());
            }
            case SELECT_PERIOD -> getPeriod(form, admin);
            default -> {
                int year = form.getYear() > 0 ? form.getYear() : LocalDate.now().getYear();
                int month = form.getMonth() > 0 ? form.getMonth() : LocalDate.now().getMonthValue();
                LocalDate start = LocalDate.of(year, month, 1);
                yield new LocalDateRange(start, start.plusMonths(1).minusDays(1), admin.getZoneId());
            }
        };
    }

    private LocalDateRange getPeriod(RecipientStatsForm form, Admin admin) {
        LocalDateRange period = parsePeriod(form, admin);
        if (period.getFrom() == null || period.getTo() == null || period.getFrom().isAfter(period.getTo())) {
            throw new UiMessageException("error.period.format");
        }
        int companyId = admin.getCompanyID();
        int limit = configService.getIntegerValue(ConfigValue.MaximumRecipientDetailPeriodDays, companyId);
        long selectedDays = period.getFrom().until(period.getTo(), ChronoUnit.DAYS);
        if (selectedDays > limit) {
            throw new UiMessageException(Message.of("statistics.maximumRecipientDetailPeriodDays.exceeded", limit));
        }
        return period;
    }

    private LocalDateRange parsePeriod(RecipientStatsForm form, Admin admin) {
        try {
            return new LocalDateRange(
                    parse(form.getStartDate(), admin),
                    parse(form.getEndDate(), admin),
                    admin.getZoneId()
            );
        } catch (Exception e) {
            throw new UiMessageException("error.date.format");
        }
    }

    private LocalDate parse(String value, Admin admin) {
        if (isBlank(value)) {
            return null;
        }
        return LocalDate.parse(value, admin.getDateFormatter());
    }

    public List<StatusInfoUiRow> getStatusInfo(RecipientStatsForm form, Admin admin) {
        return recipientStatsService
                .getStatusInfo(form.getMailinglistId(), form.getTargetId(), admin.getCompanyID())
                .entrySet().stream()
                .map(entry -> new StatusInfoUiRow(
                        entry.getKey(),
                        entry.getValue(),
                        SummedRecipientStatus.isSummed(entry.getKey())
                ))
                .toList();
    }

    public List<LongValueUiRow> getMailFormat(RecipientStatsForm form, Admin admin) {
        return recipientStatsService
                .getMailFormat(toQuery(form, admin))
                .entrySet().stream()
                .map(entry -> new LongValueUiRow(
                        t(entry.getKey().getMessageKey(), admin.getLocale()),
                        entry.getValue()
                ))
                .toList();
    }

    public List<LongValueUiRow> getRecipientStatus(RecipientStatsForm form, Admin admin) {
        Locale locale = admin.getLocale();
        Map<UserStatus, Long> data = recipientStatsService.getRecipientStatus(toQuery(form, admin));
        return List.of(
                new LongValueUiRow(t("recipient.Opt_Ins", locale), data.getOrDefault(UserStatus.Active, 0L)),
                new LongValueUiRow(t("statistic.Opt_Outs", locale),
                        data.getOrDefault(UserStatus.AdminOut, 0L)
                        + data.getOrDefault(UserStatus.UserOut, 0L)
                        + data.getOrDefault(UserStatus.Suspend, 0L)
                ),
                new LongValueUiRow(t("recipient.MailingState2", locale), data.getOrDefault(UserStatus.Bounce, 0L)),
                new LongValueUiRow(t("Opt_In_NotConfirmed", locale), data.getOrDefault(UserStatus.WaitForConfirm, 0L)),
                new LongValueUiRow(t("recipient.MailingState6", locale), data.getOrDefault(UserStatus.Blacklisted, 0L)),
                new LongValueUiRow(t("noMailinglistAssigned", locale), data.getOrDefault(null, 0L))
        );
    }
}
