/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

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
import com.agnitas.emm.core.birtstatistics.service.MailingComparisonStatisticsService;
import com.agnitas.emm.core.birtstatistics.service.MailingStatisticsService;
import com.agnitas.emm.core.mailing.service.MailingService;
import com.agnitas.reporting.birt.external.beans.MailingComparisonStatisticsData;
import com.agnitas.reporting.birt.external.beans.StatisticMetric;
import com.agnitas.util.CsvWriter;
import org.apache.commons.collections4.ListUtils;
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
        return CsvWriter.csv(ListUtils.union(
                List.of(getHeaderForCsv(admin.getLocale())),
                getRowsForCsv(ids, admin))
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
        Map<Integer, StatisticMetric> sent = statisticsService.getWorldSentMailings(mailingIds);
        Map<Integer, StatisticMetric> measuredOpeners = statisticsService.getMeasuredOpeners(mailingIds, companyId);
        Map<Integer, StatisticMetric> hardBounces = statisticsService.getHardBounces(mailingIds, companyId);
        Map<Integer, StatisticMetric> optOuts = statisticsService.getOptOuts(mailingIds, companyId);
        Map<Integer, StatisticMetric> clickers = statisticsService.getClickers(mailingIds, companyId);
        Map<Integer, StatisticMetric> delivered = statisticsService.getDelivered(mailingIds, companyId, sent, hardBounces);
        Map<Integer, StatisticMetric> softBouncesUndeliverable = statisticsService.getSoftBouncesUndeliverable(mailingIds, companyId, sent, hardBounces);
        Map<Integer, StatisticMetric> invisibleOpeners = statisticsService.getInvisibleOpeners(mailingIds, companyId, measuredOpeners, sent);
        Map<Integer, StatisticMetric> totalOpeners = statisticsService.getTotalOpeners(mailingIds, invisibleOpeners, measuredOpeners);

        return mailingIds
                .stream()
                .map(id -> {
                    MailingBase mailing = mailingsInfos.get(id);

                    return new MailingComparisonStatisticsData(
                            id,
                            mailing.getShortname(),
                            mailing.getDescription(),
                            mailing.getSenddate(),
                            totalOpeners.get(id),
                            measuredOpeners.get(id),
                            invisibleOpeners.get(id),
                            statisticsService.getAnonymousOpenings(id, companyId),
                            sent.get(id),
                            delivered.get(id),
                            optOuts.get(id),
                            hardBounces.get(id),
                            clickers.get(id),
                            statisticsService.getAnonymousClicks(id, companyId),
                            softBouncesUndeliverable.get(id),
                            statisticsService.getRevenue(id, companyId).orElse(null)
                    );
                })
                .toList();
    }

}
