/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.birtstatistics.service.impl;

import static com.agnitas.messages.I18nString.t;
import static com.agnitas.util.AgnUtils.calculateRate;
import static java.util.Collections.emptyMap;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import com.agnitas.emm.core.birtstatistics.dao.MailingTopDomainsStatsDao;
import com.agnitas.emm.core.birtstatistics.dao.MailingTopDomainsStatsDao.Filter;
import com.agnitas.emm.core.birtstatistics.dao.MailingTopDomainsStatsDao.TargetFilter;
import com.agnitas.emm.core.birtstatistics.dto.StatisticMetric;
import com.agnitas.emm.core.birtstatistics.service.MailingStatisticsService;
import com.agnitas.emm.core.birtstatistics.service.MailingTopDomainsStatsService;
import com.agnitas.reporting.birt.external.beans.LightTarget;
import org.springframework.stereotype.Service;

@Service
public class MailingTopDomainsStatsServiceImpl implements MailingTopDomainsStatsService {

    private final MailingTopDomainsStatsDao topDomainsStatsDao;
    private final MailingStatisticsService mailingStatsService;

    public MailingTopDomainsStatsServiceImpl(
            MailingTopDomainsStatsDao topDomainsStatsDao,
            MailingStatisticsService mailingStatsService
    ) {
        this.topDomainsStatsDao = topDomainsStatsDao;
        this.mailingStatsService = mailingStatsService;
    }

    @Override
    public TopDomainStats getStats(TopDomainsStatsQuery query) {
        List<LightTarget> targets = mailingStatsService.getTargetsWithAllSubscribersTarget(
                query.targetIds(), query.companyId(), query.locale()
        );

        Filter filter = new Filter(query.mailingId(), query.companyId(), query.isTopLevel());
        List<TargetFilter> targetFilters = targets.stream()
                .map(t -> new TargetFilter(t.getId(), t.getTargetSQL()))
                .toList();

        Map<Integer, Map<String, Integer>> sentEmailsByTarget = mailingStatsService.isTrackingExists(query.mailingId(), query.companyId())
                ? topDomainsStatsDao.getSentEmails(filter, targetFilters)
                : emptyMap();
        Map<Integer, Integer> sentTotalByTarget = totalsByTarget(sentEmailsByTarget);

        return new TopDomainStats(
                toMetricsByTarget(sentEmailsByTarget, null, query.limit(), query.locale()),
                toMetricsByTarget(topDomainsStatsDao.getHardBounces(filter, targetFilters), sentTotalByTarget, query.limit(), query.locale()),
                toMetricsByTarget(topDomainsStatsDao.getSoftBounces(filter, targetFilters), sentTotalByTarget, query.limit(), query.locale()),
                toMetricsByTarget(topDomainsStatsDao.getOpeners(filter, targetFilters), null, query.limit(), query.locale()),
                toMetricsByTarget(topDomainsStatsDao.getClickers(filter, targetFilters), null, query.limit(), query.locale())
        );
    }

    private Map<Integer, Integer> totalsByTarget(Map<Integer, Map<String, Integer>> allDomainsByTarget) {
        return allDomainsByTarget.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().values().stream().mapToInt(Integer::intValue).sum()
                ));
    }

    private Map<Integer, Map<String, StatisticMetric>> toMetricsByTarget(
            Map<Integer, Map<String, Integer>> allDomainsByTarget,
            Map<Integer, Integer> rateBaseByTarget,
            int limit,
            Locale locale
    ) {
        Map<Integer, Map<String, StatisticMetric>> result = new LinkedHashMap<>();
        allDomainsByTarget.forEach((targetId, allDomains) -> {
            int total = allDomains.values().stream().mapToInt(Integer::intValue).sum();
            int rateBase = rateBaseByTarget != null ? rateBaseByTarget.getOrDefault(targetId, 0) : total;
            result.put(targetId, toMetrics(allDomains, total, rateBase, limit, locale));
        });
        return result;
    }

    private Map<String, StatisticMetric> toMetrics(
            Map<String, Integer> allDomains, int trueTotal, int rateBase, int limit, Locale locale
    ) {
        Map<String, Integer> topDomains = allDomains.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(limit)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

        putOtherDomainsEntry(topDomains, trueTotal, locale);

        return domainToMetricsMap(topDomains, rateBase);
    }

    private static Map<String, StatisticMetric> domainToMetricsMap(Map<String, Integer> perDomain, long rateBase) {
        return perDomain.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> new StatisticMetric(e.getValue(), calculateRate(e.getValue(), rateBase)),
                (a, b) -> a, LinkedHashMap::new
        ));
    }

    private static void putOtherDomainsEntry(Map<String, Integer> values, int total, Locale locale) {
        if (total <= 0) {
            return;
        }
        values.put(t("statistic.Other", locale), total - values.values()
                .stream()
                .mapToInt(Integer::intValue)
                .sum());
    }
}
