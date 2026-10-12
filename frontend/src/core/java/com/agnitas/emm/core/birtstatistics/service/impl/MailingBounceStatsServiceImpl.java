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
import java.util.function.Function;
import java.util.stream.Collectors;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.birtstatistics.dao.MailingBounceStatsDao;
import com.agnitas.emm.core.birtstatistics.dao.MailingBounceStatsDao.Filter;
import com.agnitas.emm.core.birtstatistics.dto.EmailBounceDetail;
import com.agnitas.emm.core.birtstatistics.dto.StatisticMetric;
import com.agnitas.emm.core.birtstatistics.service.MailingBounceStatsService;
import com.agnitas.emm.core.birtstatistics.service.MailingStatisticsService;
import com.agnitas.emm.core.bounce.Bounce;
import com.agnitas.emm.core.commons.dto.DateRange;
import com.agnitas.emm.core.company.service.CompanyService;
import com.agnitas.emm.core.maildrop.MaildropStatus;
import com.agnitas.emm.core.target.service.TargetService;
import com.agnitas.reporting.birt.external.beans.LightTarget;
import com.agnitas.util.AgnUtils;
import org.springframework.stereotype.Service;

@Service
public class MailingBounceStatsServiceImpl implements MailingBounceStatsService {

    private static final String HARD_BOUNCES_TOTAL_KEY = "report.total";

    private final MailingBounceStatsDao bounceStatsDao;
    private final MailingStatisticsService mailingStatsService;
    private final CompanyService companyService;
    private final MailingStatsMapper mailingStatsMapper;
    private final TargetService targetService;

    public MailingBounceStatsServiceImpl(
            MailingBounceStatsDao bounceStatsDao,
            MailingStatisticsService mailingStatistsService,
            CompanyService companyService,
            MailingStatsMapper mailingStatsMapper,
            TargetService targetService
    ) {
        this.bounceStatsDao = bounceStatsDao;
        this.mailingStatsService = mailingStatistsService;
        this.companyService = companyService;
        this.mailingStatsMapper = mailingStatsMapper;
        this.targetService = targetService;
    }

    @Override
    public BounceStats getStats(int mailingId, List<Integer> targetIds, Admin admin) {
        return getStats(
                mailingId,
                admin.getCompanyID(),
                mailingStatsService.getTargetsWithAllSubscribersTarget(targetIds, admin),
                admin.getLocale(),
                targetService.getTargetSqlCombinedWithOr(targetService.getAltgIds(admin), admin.getCompanyID())
        );
    }

    @Override
    public BounceStats getStats(int mailingId, int companyId, List<LightTarget> targets, Locale locale) {
        return getStats(mailingId, companyId, targets, locale, "");
    }

    private BounceStats getStats(int mailingId, int companyId, List<LightTarget> targets, Locale locale, String altgSql) {
        BounceStatsOpts opts = new BounceStatsOpts(mailingId, companyId, altgSql, targets, locale);

        Map<Integer, Map<String, StatisticMetric>> hardBounces = getBounces(
                opts,
                target -> getHardBounces(opts, target.getTargetSQL()),
                HARD_BOUNCES_TOTAL_KEY
        );
        return new BounceStats(
                getBounces(opts, target -> getSoftBounces(opts, target.getTargetSQL()), "birt.qualified.softbounces"),
                hardBounces,
                getUndelivered(opts, hardBounces)
        );
    }

    private Map<Integer, Map<String, StatisticMetric>> getBounces(
            BounceStatsOpts opts,
            Function<LightTarget, Map<Bounce, Integer>> getMetricsFunc,
            String totalMsgKey
    ) {
        Map<Integer, Map<Bounce, Integer>> bounces = targetToStatsMap(opts.targets(), getMetricsFunc);
        long total = getTotal(bounces.get(MailingStatisticsService.ALL_SUBSCRIBERS_TARGETGROUP_ID));
        return bounces.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> toMetrics(e.getValue(), total, opts.locale(), totalMsgKey)
        ));
    }

    private Map<Integer, StatisticMetric> getUndelivered(
            BounceStatsOpts opts,
            Map<Integer, Map<String, StatisticMetric>> hardBounces
    ) {
        if (!companyService.isMailtrackingActive(opts.companyId())) {
            return emptyMap();
        }
        return opts.targets().stream().collect(Collectors.toMap(
                LightTarget::getId,
                m -> getUndelivered(opts, m, hardBounces)
        ));
    }

    private StatisticMetric getUndelivered(
            BounceStatsOpts opts,
            LightTarget target,
            Map<Integer, Map<String, StatisticMetric>> hardBounces
    ) {
        long sentCount = mailingStatsService.getSentCount(opts.mailingId(), opts.companyId(), MaildropStatus.realSendStatuses(), target.getTargetSQL(), new DateRange());
        long numberDeliveredMailings = mailingStatsService.getDeliveredMailsCount(opts.mailingId(), opts.companyId(), target.getTargetSQL(), new DateRange());
        long hardBouncesTotal = hardBounces.get(target.getId()).get(t(HARD_BOUNCES_TOTAL_KEY, opts.locale())).value();
        long value = Math.max(0, sentCount - numberDeliveredMailings - hardBouncesTotal);

        return new StatisticMetric(value, AgnUtils.calculateRate(value, hardBouncesTotal));
    }

    private static int getTotal(Map<Bounce, Integer> bounces) {
        return bounces.values()
                .stream()
                .mapToInt(Integer::intValue)
                .sum();
    }

    private Map<Bounce, Integer> getSoftBounces(BounceStatsOpts opts, String targetSql) {
        Filter filter = mailingStatsMapper.toBounceFilter(opts, opts.withAltg(targetSql));
        Map<Bounce, Integer> dbBounces = bounceStatsDao.getSoftBounces(filter);
        Map<Bounce, Integer> result = new LinkedHashMap<>();
        Bounce.SOFT_BOUNCES.forEach(b -> result.put(b, 0)); // pre-fill 0s to include all

        dbBounces.forEach((bounce, count) -> result.merge(bounce, count, Integer::sum));
        return result;
    }

    private Map<Bounce, Integer> getHardBounces(BounceStatsOpts opts, String targetSql) {
        Filter filter = mailingStatsMapper.toBounceFilter(opts, opts.withAltg(targetSql));
        Map<Bounce, Integer> all = bounceStatsDao.getHardBounces(filter);
        Map<Bounce, Integer> result = new LinkedHashMap<>();
        Bounce.HARD_BOUNCES.forEach(b -> result.put(b, 0)); // pre-fill 0s to include all

        all.forEach((bounce, count) -> {
            if (is510bounce(bounce)) {
                result.merge(Bounce.OTHER_HARD_BOUNCE, count, Integer::sum);
            } else {
                result.merge(bounce, count, Integer::sum);
            }
        });
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
        String targetSql = targetService.getTargetSqlCombinedWithOr(targetIds, companyId);
        return bounceStatsDao.getSoftBouncesWithDetailAndEmail(new Filter(mailingId, companyId, targetSql));
    }

    @Override
    public List<EmailBounceDetail> getHardBouncesWithDetailAndEmail(
            int mailingId,
            int companyId,
            List<Integer> targetIds
    ){
        String targetSql = targetService.getTargetSqlCombinedWithOr(targetIds, companyId);
        return bounceStatsDao.getHardBouncesWithDetailAndEmail(new Filter(mailingId, companyId, targetSql));
    }
}
