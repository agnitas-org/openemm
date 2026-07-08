/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtstatistics.service.impl;

import static com.agnitas.messages.I18nString.t;
import static com.agnitas.util.AgnUtils.calculateRate;
import static java.util.Collections.emptyMap;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.agnitas.emm.core.birtstatistics.dao.MailingTopDomainsStatsDao;
import com.agnitas.emm.core.birtstatistics.service.MailingStatisticsService;
import com.agnitas.emm.core.birtstatistics.service.MailingTopDomainsStatsService;
import com.agnitas.emm.core.commons.dto.DateRange;
import com.agnitas.reporting.birt.external.beans.LightTarget;
import com.agnitas.reporting.birt.external.beans.StatisticMetric;
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
    public TopDomainStats getStats(TopDomainsStatsRequest req) {
        return new TopDomainStats(
                getSentMailings(req),
                targetToStatsMap(req.targets(), target -> getHardBounces(req, target.getTargetSQL())),
                targetToStatsMap(req.targets(), target -> getSoftBounces(req, target.getTargetSQL())),
                targetToStatsMap(req.targets(), target -> getOpeners(req, target.getTargetSQL())),
                targetToStatsMap(req.targets(), target -> getClickers(req, target.getTargetSQL()))
        );
    }

    private Map<Integer, Map<String, StatisticMetric>> getSentMailings(TopDomainsStatsRequest req) {
        if (!mailingStatsService.isTrackingExists(req.mailingId(), req.companyId())) {
            return emptyMap();
        }
        return targetToStatsMap(req.targets(), target -> getSentMailings(req, target.getTargetSQL()));
    }

    private Map<String, StatisticMetric> getSentMailings(TopDomainsStatsRequest req, String targetSql) {
        int total = getSentCount(req.mailingId(), req.companyId(), targetSql);
        Map<String, Integer> perDomain = topDomainsStatsDao.getSentEmails(req.filter(), targetSql);
        putOtherDomainsEntry(perDomain, total, req.locale());
        return domainToMetricsMap(perDomain, total);
    }

    private int getSentCount(int mailingId, int companyId, String targetSql) {
        return mailingStatsService.getSentCountFromMailtrackTbl(mailingId, companyId, targetSql, new DateRange());
    }

    private Map<String, StatisticMetric> getHardBounces(TopDomainsStatsRequest req, String targetSql) {
        int rateBase = getSentCount(req.mailingId(), req.companyId(), targetSql);
        int total = topDomainsStatsDao.getHardBouncesTotal(req.filter(), targetSql);
        Map<String, Integer> perDomain = topDomainsStatsDao.getHardBounces(req.filter(), targetSql);
        putOtherDomainsEntry(perDomain, total, req.locale());
        return domainToMetricsMap(perDomain, rateBase);
    }

    private Map<String, StatisticMetric> getSoftBounces(TopDomainsStatsRequest req, String targetSql) {
        int rateBase = getSentCount(req.mailingId(), req.companyId(), targetSql);
        int total = topDomainsStatsDao.getSoftBouncesTotal(req.filter(), targetSql);
        Map<String, Integer> perDomain = topDomainsStatsDao.getSoftBounces(req.filter(), targetSql);
        putOtherDomainsEntry(perDomain, total, req.locale());
        return domainToMetricsMap(perDomain, rateBase);
    }

    private Map<String, StatisticMetric> getOpeners(TopDomainsStatsRequest req, String targetSql) {
        int total = topDomainsStatsDao.getOpenersTotal(req.mailingId(), req.companyId(), targetSql);
        Map<String, Integer> perDomain = topDomainsStatsDao.getOpeners(req.filter(), targetSql);
        putOtherDomainsEntry(perDomain, total, req.locale());
        return domainToMetricsMap(perDomain, total);
    }

    private Map<String, StatisticMetric> getClickers(TopDomainsStatsRequest req, String targetSql) {
        int total = topDomainsStatsDao.getClickersTotal(req.mailingId(), req.companyId(), targetSql);
        Map<String, Integer> perDomain = topDomainsStatsDao.getClickers(req.filter(), targetSql);
        putOtherDomainsEntry(perDomain, total, req.locale());
        return domainToMetricsMap(perDomain, total);
    }

    private Map<Integer, Map<String, StatisticMetric>> targetToStatsMap(
            List<LightTarget> targets,
            Function<LightTarget, Map<String, StatisticMetric>> getMetricsFunc
    ) {
        return targets.stream().collect(Collectors.toMap(
                LightTarget::getId,
                getMetricsFunc,
                (a, b) -> a, LinkedHashMap::new
        ));
    }

    private static Map<String, StatisticMetric> domainToMetricsMap(Map<String, Integer> perDomain, int rateBase) {
        return perDomain.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> new StatisticMetric(e.getValue(), calculateRate(e.getValue(), rateBase)),
                (a, b) -> a, LinkedHashMap::new
        ));
    }

    private static void putOtherDomainsEntry(Map<String, Integer> values, int total, Locale locale) {
        values.put(t("statistic.Other", locale), total - values.values()
                .stream()
                .mapToInt(Integer::intValue)
                .sum());
    }
}
