/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.birtstatistics.mailing.mapper;

import static com.agnitas.emm.core.birtstatistics.service.MailingStatisticsService.ALL_SUBSCRIBERS_MSG_KEY;
import static com.agnitas.emm.core.birtstatistics.service.MailingStatisticsService.ALL_SUBSCRIBERS_TARGETGROUP_ID;
import static com.agnitas.messages.I18nString.t;

import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.birtstatistics.dto.StatisticMetric;
import com.agnitas.emm.core.birtstatistics.mailing.forms.MailingStatisticForm;
import com.agnitas.emm.core.birtstatistics.service.MailingTopDomainsStatsService;
import com.agnitas.emm.core.birtstatistics.service.MailingTopDomainsStatsService.TopDomainStats;
import com.agnitas.emm.core.birtstatistics.service.MailingTopDomainsStatsService.TopDomainsStatsQuery;
import com.agnitas.emm.core.target.service.TargetService;
import com.agnitas.reporting.birt.external.beans.LightTarget;
import com.agnitas.util.CsvWriter;
import org.springframework.stereotype.Component;

@Component
public class MailingTopDomainsUiService {

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.00%");

    private final MailingTopDomainsStatsService topDomainsStatsService;
    private final MailingStatsUiMapper mailingStatsMapper;
    private final TargetService targetService;

    public MailingTopDomainsUiService(
            MailingTopDomainsStatsService topDomainsStatsService,
            MailingStatsUiMapper mailingStatsMapper,
            TargetService targetService
    ) {
        this.topDomainsStatsService = topDomainsStatsService;
        this.mailingStatsMapper = mailingStatsMapper;
        this.targetService = targetService;
    }

    public TopDomainStats ui(int mailingId, MailingStatisticForm form, Admin admin) {
        TopDomainsStatsQuery query = mailingStatsMapper.toTopDomainsQuery(mailingId, admin, form);
        return topDomainsStatsService.getStats(query);
    }

    private record CsvRow(
            String category,
            String domain,
            String targetName,
            StatisticMetric metric
    ) {}

    public byte[] csv(int mailingId, MailingStatisticForm form, Admin admin) throws Exception {
        TopDomainsStatsQuery query = mailingStatsMapper.toTopDomainsQuery(mailingId, admin, form);
        TopDomainStats data = topDomainsStatsService.getStats(query);

        return CsvWriter.csv(Stream.concat(
                Stream.of(getHeadersForCsv(admin.getLocale())),
                Stream.of(
                        getCsvRows("report.sentMails", data.sentEmails(), query),
                        getCsvRows("statistic.bounces.hardbounce", data.hardBounces(), query),
                        getCsvRows("report.softbounces", data.softBounces(), query),
                        getCsvRows("statistic.opener", data.openers(), query),
                        getCsvRows("statistic.clicker", data.clickers(), query)
                ).flatMap(List::stream)
        ).toList());
    }

    private static List<String> getHeadersForCsv(Locale locale) {
        return Stream.of("grid.mediapool.category", "statistic.domain", "birt.Target", "value", "rate")
                .map(key -> t(key, locale))
                .toList();
    }

    private List<List<String>> getCsvRows(
            String msgKey,
            Map<Integer, Map<String, StatisticMetric>> data,
            TopDomainsStatsQuery query
    ) {
        return data.entrySet().stream()
                .flatMap(targetEntry -> targetEntry.getValue().entrySet().stream()
                        .filter(domainEntry -> domainEntry.getValue() != null)
                        .map(domainEntry -> new CsvRow(
                                t(msgKey, query.locale()),
                                domainEntry.getKey(),
                                getTargetName(targetEntry.getKey(), query.companyId(), query.locale()),
                                domainEntry.getValue()
                        ))
                )
                .sorted(
                        Comparator
                                .comparing((CsvRow r) -> t("statistic.Other", query.locale()).equals(r.domain()) ? 1 : 0)
                                .thenComparing(r -> r.metric().value(), Comparator.reverseOrder())
                                .thenComparing(CsvRow::domain)
                )
                .map(row -> List.of(
                        row.category(),
                        row.domain(),
                        row.targetName(),
                        String.valueOf(row.metric().value()),
                        DECIMAL_FORMAT.format(row.metric().rate() / 100)
                ))
                .toList();
    }

    private String getTargetName(int targetId, int companyId, Locale locale) {
        if (targetId == ALL_SUBSCRIBERS_TARGETGROUP_ID) {
            return t(ALL_SUBSCRIBERS_MSG_KEY, locale);
        }
        return targetService.getTargetName(targetId, companyId);
    }

    public static String getTargetName(List<LightTarget> targets, int targetId) {
        return targets.stream()
                .filter(target -> target.getId() == targetId)
                .findFirst()
                .map(LightTarget::getName)
                .orElse(null);
    }
}
