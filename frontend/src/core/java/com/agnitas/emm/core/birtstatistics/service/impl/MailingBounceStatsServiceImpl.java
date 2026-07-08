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
import static java.util.Collections.emptySet;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.agnitas.emm.core.birtstatistics.dao.MailingBounceStatsDao;
import com.agnitas.emm.core.birtstatistics.dto.EmailBounceDetail;
import com.agnitas.emm.core.birtstatistics.service.MailingBounceStatsService;
import com.agnitas.emm.core.birtstatistics.service.MailingStatisticsService;
import com.agnitas.emm.core.bounce.Bounce;
import com.agnitas.emm.core.commons.dto.DateRange;
import com.agnitas.emm.core.company.service.CompanyService;
import com.agnitas.reporting.birt.external.beans.LightTarget;
import com.agnitas.reporting.birt.external.beans.StatisticMetric;
import com.agnitas.util.AgnUtils;
import org.springframework.stereotype.Service;

@Service
public class MailingBounceStatsServiceImpl implements MailingBounceStatsService {

    private final MailingBounceStatsDao bounceStatsDao;
    private final MailingStatisticsService mailingStatsService;
    private final CompanyService companyService;

    public MailingBounceStatsServiceImpl(
            MailingBounceStatsDao bounceStatsDao,
            MailingStatisticsService mailingStatistsService,
            CompanyService companyService
    ) {
        this.bounceStatsDao = bounceStatsDao;
        this.mailingStatsService = mailingStatistsService;
        this.companyService = companyService;
    }

    @Override
    public BounceStats getStats(BounceStatsRequest req) {
        Map<Integer, Map<String, StatisticMetric>> hardBounces = getBounces(
                req,
                target -> getHardBounces(req, target.getTargetSQL()),
                "report.total"
        );
        return new BounceStats(
                getBounces(req, target -> getSoftBounces(req, target.getTargetSQL()), "birt.qualified.softbounces"),
                hardBounces,
                getUndelivered(req, hardBounces)
        );
    }

    private Map<Integer, Map<String, StatisticMetric>> getBounces(
            BounceStatsRequest req,
            Function<LightTarget, Map<Bounce, Integer>> getMetricsFunc,
            String totalMsgKey
    ) {
        Map<Integer, Map<Bounce, Integer>> bounces = targetToStatsMap(req.targets(), getMetricsFunc);
        long total = getTotal(bounces.get(MailingStatisticsService.ALL_SUBSCRIBERS_TARGETGROUP_ID));
        return bounces.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> toMetrics(e.getValue(), total, req.locale(), totalMsgKey)
        ));
    }

    private Map<Integer, StatisticMetric> getUndelivered(
            BounceStatsRequest req,
            Map<Integer, Map<String, StatisticMetric>> hardBounces
    ) {
        if (!companyService.isMailtrackingActive(req.companyId())) {
            return emptyMap();
        }
        return req.targets().stream().collect(Collectors.toMap(
                LightTarget::getId,
                m -> getUndelivered(req, m, hardBounces)
        ));
    }

    private StatisticMetric getUndelivered(
            BounceStatsRequest req,
            LightTarget target,
            Map<Integer, Map<String, StatisticMetric>> hardBounces
    ) {
        long sentCount = mailingStatsService.getSentCount(req.mailingId(), req.companyId(), emptySet(), target.getTargetSQL(), new DateRange());
        long numberDeliveredMailings = mailingStatsService.getDeliveredMailsCount(req.mailingId(), req.companyId(), target.getTargetSQL(), new DateRange());
        long targetTotal = hardBounces.get(target.getId()).values().stream().mapToLong(StatisticMetric::value).sum();
        long value = sentCount - numberDeliveredMailings - targetTotal;
        if (value < 0) {
            value = 0;
        }
        return new StatisticMetric(value, AgnUtils.calculateRate(value, targetTotal)) ;
    }

    private static int getTotal(Map<Bounce, Integer> bounces) {
        return bounces.values()
                .stream()
                .mapToInt(Integer::intValue)
                .sum();
    }

    private Map<Bounce, Integer> getSoftBounces(BounceStatsRequest req, String targetSql) {
        return bounceStatsDao.getSoftBounces(req.mailingId(), req.companyId(), req.withAltg(targetSql));
    }

    private Map<Bounce, Integer> getHardBounces(BounceStatsRequest req, String targetSql) {
        Map<Bounce, Integer> all = bounceStatsDao.getHardBounces(req.mailingId(), req.companyId(), req.withAltg(targetSql));

        Map<Boolean, Map<Bounce, Integer>> partitioned = all.entrySet().stream().collect(Collectors.partitioningBy(
                e -> is510bounce(e.getKey()),
                Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)
        ));
        Map<Bounce, Integer> result = partitioned.get(false);
        int bounce510count = partitioned.get(true).values().stream().mapToInt(Integer::intValue).sum();
        result.put(Bounce.OTHER_HARD_BOUNCE, bounce510count);
        return result;
    }

    // detail = 510 + all unparseable userRemarks as other hardBounces
    private static boolean is510bounce(Bounce bounce) {
        return bounce.getCode() <= 0 || bounce.getCode() == Bounce.OTHER_HARD_BOUNCE.getId();
    }

    private Map<Integer, Map<Bounce, Integer>> targetToStatsMap(
            List<LightTarget> targets,
            Function<LightTarget, Map<Bounce, Integer>> getMetricsFunc
    ) {
        return targets.stream().collect(Collectors.toMap(
                LightTarget::getId,
                getMetricsFunc,
                (a, b) -> a, LinkedHashMap::new
        ));
    }

    private Map<String, StatisticMetric> toMetrics(Map<Bounce, Integer> bounces, long rateBase, Locale locale, String totalMsgKey) {
        LinkedHashMap<String, StatisticMetric> metrics = bounces.entrySet().stream().collect(Collectors.toMap(
                e -> e.getKey().getDetailMsg(locale.getLanguage()),
                e -> new StatisticMetric(e.getValue(), calculateRate(e.getValue(), rateBase)),
                (a, b) -> a, LinkedHashMap::new
        ));
        int total = getTotal(bounces);
        metrics.put(
                t(totalMsgKey, locale),
                new StatisticMetric(total, AgnUtils.calculateRate(total, rateBase))
        );
        return metrics;
    }

    @Override
    public List<EmailBounceDetail> getSoftBouncesWithDetailAndEmail(
            int mailingId,
            int companyId,
            List<Integer> targetIds
    ){
        String targetSql = mailingStatsService.getTargetSql(targetIds, companyId);
        return bounceStatsDao.getSoftBouncesWithDetailAndEmail(mailingId, companyId, targetSql);
    }

    @Override
    public List<EmailBounceDetail> getHardBouncesWithDetailAndEmail(
            int mailingId,
            int companyId,
            List<Integer> targetIds
    ){
        String targetSql = mailingStatsService.getTargetSql(targetIds, companyId);
        return bounceStatsDao.getHardBouncesWithDetailAndEmail(mailingId, companyId, targetSql);
    }
}
