/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.birtstatistics.service.impl;

import static com.agnitas.messages.I18nString.getLocaleString;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.agnitas.beans.Admin;
import com.agnitas.beans.MailingBase;
import com.agnitas.emm.core.birtstatistics.dto.MailingComparisonStatisticsData;
import com.agnitas.emm.core.birtstatistics.dto.StatisticMetric;
import com.agnitas.emm.core.birtstatistics.service.MailingComparisonStatisticsService;
import com.agnitas.emm.core.birtstatistics.service.MailingRawMetrics;
import com.agnitas.emm.core.birtstatistics.service.MailingStatisticsService;
import com.agnitas.emm.core.commons.dto.DateRange;
import com.agnitas.emm.core.maildrop.MaildropStatus;
import com.agnitas.emm.core.mailing.service.MailingService;
import com.agnitas.util.CsvWriter;
import org.springframework.stereotype.Service;

@Service
public class MailingComparisonStatisticsServiceImpl implements MailingComparisonStatisticsService {

    private final MailingService mailingService;
    private final MailingStatisticsService statisticsService;

    public MailingComparisonStatisticsServiceImpl(
            MailingService mailingService,
            MailingStatisticsService statisticsService
    ) {
        this.mailingService = mailingService;
        this.statisticsService = statisticsService;
    }

    @Override
    public byte[] csv(Set<Integer> ids, Admin admin) throws Exception {
        return CsvWriter.csv(
                getHeaderForCsv(admin.getLocale()),
                getRowsForCsv(ids, admin)
        );
    }

    private static List<String> getHeaderForCsv(Locale locale) {
        return List.of(
                getLocaleString("Mailing", locale),
                getLocaleString("statistic.mails.sent", locale),
                getLocaleString("statistic.mails.delivered", locale),
                getLocaleString("statistic.opener", locale),
                getLocaleString("statistic.opener", locale) + " %",
                getLocaleString("statistic.clicker", locale),
                getLocaleString("statistic.clicker", locale) + " %",
                getLocaleString("statistic.Opt_Outs", locale),
                getLocaleString("statistic.Opt_Outs", locale) + " %",
                getLocaleString("statistic.Bounces", locale),
                getLocaleString("statistic.Bounces", locale) + " %",
                getLocaleString("statistic.revenue", locale)
        );
    }

    private List<List<String>> getRowsForCsv(Set<Integer> ids, Admin admin) {
        return getData(ids, admin.getCompanyID())
                .stream()
                .map(data -> Arrays.asList(
                        data.name(),
                        String.valueOf(data.sentEmails().value()),
                        String.valueOf(data.acceptedEmails().value()),
                        String.valueOf(data.measuredOpeners().value()),
                        data.measuredOpeners().formatRate(),
                        String.valueOf(data.clickers().value()),
                        data.clickers().formatRate(),
                        String.valueOf(data.optOuts().value()),
                        data.optOuts().formatRate(),
                        String.valueOf(data.hardBounces().value()),
                        data.hardBounces().formatRate(),
                        "%.2f".formatted(Optional.ofNullable(data.revenue()).orElse(0d))
                ))
                .toList();
    }

    @Override
    public List<MailingComparisonStatisticsData> getData(Set<Integer> mailingIds, int companyId) {
        Map<Integer, MailingBase> mailingsInfos = mailingService.getMailingsInfoForComparison(mailingIds, companyId);

        return mailingIds
                .stream()
                .map(id -> {
                    MailingBase mailing = mailingsInfos.get(id);
                    MailingRawMetrics raw = statisticsService.getRawMetrics(id, companyId, Set.of(MaildropStatus.WORLD), null, new DateRange());

                    return new MailingComparisonStatisticsData(
                            id,
                            mailing.getShortname(),
                            mailing.getDescription(),
                            mailing.getSenddate(),
                            StatisticMetric.of(raw.totalOpeners(), raw.sentEmails()),
                            StatisticMetric.of(raw.measuredOpeners(), raw.sentEmails()),
                            StatisticMetric.of(raw.invisibleOpeners(), raw.sentEmails()),
                            raw.anonymousOpenersCount(),
                            new StatisticMetric(raw.sentEmails(), 100),
                            raw.acceptedEmails() == null ? null : StatisticMetric.of(raw.acceptedEmails(), raw.sentEmails()),
                            StatisticMetric.of(raw.optOuts(), raw.sentEmails()),
                            StatisticMetric.of(raw.hardBounces(), raw.sentEmails()),
                            StatisticMetric.of(raw.clickers(), raw.sentEmails()),
                            raw.anonymousClickersCount(),
                            raw.softBounces() == null ? null : StatisticMetric.of(raw.softBounces(), raw.sentEmails()),
                            raw.revenue()
                    );
                })
                .toList();
    }

}
