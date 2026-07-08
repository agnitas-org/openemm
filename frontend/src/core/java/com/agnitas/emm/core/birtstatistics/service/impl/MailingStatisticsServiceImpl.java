/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtstatistics.service.impl;

import static com.agnitas.dao.MailingStatisticsDao.isTargetFilterRequired;
import static com.agnitas.messages.I18nString.t;
import static com.agnitas.util.AgnUtils.calculateRate;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.SequencedMap;
import java.util.Set;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.agnitas.beans.Admin;
import com.agnitas.beans.BindingEntry;
import com.agnitas.dao.MailingStatisticsDao;
import com.agnitas.emm.common.MailingType;
import com.agnitas.emm.core.birtstatistics.service.MailingStatisticsService;
import com.agnitas.emm.core.commons.dto.DateRange;
import com.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.core.commons.util.ConfigValue;
import com.agnitas.emm.core.company.service.CompanyService;
import com.agnitas.emm.core.maildrop.MaildropStatus;
import com.agnitas.emm.core.mailing.service.MailingService;
import com.agnitas.emm.core.mobile.bean.DeviceClass;
import com.agnitas.reporting.birt.external.beans.LightTarget;
import com.agnitas.reporting.birt.external.beans.StatisticMetric;
import com.agnitas.reporting.birt.external.dao.LightTargetDao;
import com.agnitas.reporting.birt.external.dataset.CommonKeys;
import com.agnitas.reporting.birt.external.utils.ExpirationUtils;
import com.agnitas.util.DateUtilities;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class MailingStatisticsServiceImpl implements MailingStatisticsService {

    private final MailingStatisticsDao statisticsDao;
    private final MailingService mailingService;
    private final CompanyService companyService;
    private final ConfigService configService;
    private final LightTargetDao lightTargetDao;

    public MailingStatisticsServiceImpl(
            MailingStatisticsDao statisticsDao,
            MailingService mailingService,
            CompanyService companyService,
            ConfigService configService,
            LightTargetDao lightTargetDao) {
        this.statisticsDao = statisticsDao;
        this.mailingService = mailingService;
        this.companyService = companyService;
        this.configService = configService;
        this.lightTargetDao = lightTargetDao;
    }

    @Override
    public Map<Integer, StatisticMetric> getWorldSentMailings(Set<Integer> mailingIds) {
        return computeMetrics(mailingIds, this::getSentWorldCount);
    }

    @Override
    public Map<Integer, StatisticMetric> getMeasuredOpeners(Set<Integer> mailingIds, int companyId) {
        return computeMetrics(mailingIds, id -> getMeasuredOpenersCount(id, companyId));
    }

    @Override
    public Map<Integer, StatisticMetric> getHardBounces(Set<Integer> mailingIds, int companyId) {
        return computeMetrics(mailingIds, id -> getHardBouncesCount(id, companyId));
    }

    @Override
    public Map<Integer, StatisticMetric> getOptOuts(Set<Integer> mailingIds, int companyId) {
        return computeMetrics(mailingIds, id -> getOptOutsCount(id, companyId));
    }

    @Override
    public Map<Integer, StatisticMetric> getClickers(Set<Integer> mailingIds, int companyId) {
        return computeMetrics(mailingIds, id -> getClickersCount(id, companyId));
    }

    @Override
    public Map<Integer, StatisticMetric> getTotalOpeners(
            Set<Integer> mailingIds,
            Map<Integer, StatisticMetric> invisibleOpeners,
            Map<Integer, StatisticMetric> measuredOpeners
    ) {
        return computeMetrics(mailingIds, id -> invisibleOpeners.get(id).value() + measuredOpeners.get(id).value());
    }

    @Override
    public Map<Integer, StatisticMetric> getDelivered(
            Set<Integer> mailingIds,
            int companyId,
            Map<Integer, StatisticMetric> sent,
            Map<Integer, StatisticMetric> hardBounces
    ) {
        return computeMetrics(
                mailingIds,
                id ->
                        getDeliveredCount(
                                id,
                                companyId,
                                sent.get(id).value(),
                                hardBounces.get(id).value()
                        ).orElse(null)
        );
    }

    @Override
    public Map<Integer, StatisticMetric> getSoftBouncesUndeliverable(
            Set<Integer> mailingIds,
            int companyId,
            Map<Integer, StatisticMetric> sent,
            Map<Integer, StatisticMetric> hardBounces
    ) {
        return computeMetrics(
                mailingIds,
                id ->
                        getSoftBouncesUndeliverable(
                                id,
                                companyId,
                                sent.get(id).value(),
                                hardBounces.get(id).value()
                        ).orElse(null)
        );
    }

    @Override
    public Map<Integer, StatisticMetric> getInvisibleOpeners(
            Set<Integer> mailingIds,
            int companyId,
            Map<Integer, StatisticMetric> measuredOpeners,
            Map<Integer, StatisticMetric> sent
    ) {
        return computeMetrics(
                mailingIds,
                id ->
                        getInvisibleOpenersCount(
                                id,
                                companyId,
                                measuredOpeners.get(id).value(),
                                sent.get(id).value(),
                                "",
                                new DateRange()
                        )
        );
    }

    private Map<Integer, StatisticMetric> computeMetrics(
            Set<Integer> mailingIds,
            IntFunction<Number> valueProvider
    ) {
        Map<Integer, Number> countMap = new HashMap<>();
        mailingIds.forEach(id -> {
            Number value = valueProvider.apply(id);
            if (value != null) {
                countMap.put(id, value);
            }
        });

        long totalCount = getTotalCount(countMap);

        return countMap.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> new StatisticMetric(e.getValue().longValue(), calculateRate(e.getValue(), totalCount))
                ));
    }

    private long getTotalCount(Map<Integer, Number> countsMap) {
        return countsMap.values()
                .stream()
                .map(Number::longValue)
                .reduce(0L, Long::sum);
    }

    @Override
    public long getInvisibleOpenersCount(int mailingId, int companyId, long measuredOpeners, long sentCount, String targetSql, DateRange dateRange) {
        long openingClickers = statisticsDao.getOpeningClickers(mailingId, companyId, targetSql, emptySet(), dateRange);
        long nonOpeningClickers = statisticsDao.getNonOpeningClickers(mailingId, companyId, targetSql, emptySet(), dateRange);
        long maximumOverallOpeners = getMaximumOverallOpeners(mailingId, companyId, sentCount, targetSql, dateRange);

        // Only extrapolate invisible openers if the base number "openingClickers per measuredOpeners" is at least 5 percent
        if (openingClickers > 0 && measuredOpeners > 0 && maximumOverallOpeners > 0 && (openingClickers * 100 / measuredOpeners) >= 5) {
            long invisibleOpeners = (measuredOpeners * nonOpeningClickers) / openingClickers;
            // Limit all openers to 100 percent of sentMails, just to not confuse the user
            long maxInvisibleOpeners = maximumOverallOpeners - measuredOpeners;
            return Math.max(0, Math.min(maxInvisibleOpeners, invisibleOpeners));
        }
        return nonOpeningClickers;
    }

    private long getMaximumOverallOpeners(int mailingId, int companyId, long sentCount, String targetSql, DateRange dateRange) {
        if (companyService.isMailtrackingActive(companyId) && !isMailingExpired(mailingId, companyId)) {
            return getDeliveredMailsCount(mailingId, companyId, targetSql, dateRange);
        }
        if (isMailingExpired(mailingId, companyId)) {
            return isTargetFilterRequired(targetSql) ? 0 : sentCount;
        }
        return isTargetFilterRequired(targetSql) ? 0 : sentCount - getBouncesCount(mailingId, companyId);
    }

    @Override
    public Optional<Double> getRevenue(int mailingId, int companyId) {
        if (!statisticsDao.isRevenueTableExists(companyId)) {
            return Optional.empty();
        }

        return Optional.of(getRevenueCount(mailingId, companyId, "", new DateRange()));
    }

    @Override
    public double getRevenueCount(int mailingId, int companyId, String targetSql, DateRange dateRange) {
        return statisticsDao.getRevenue(mailingId, companyId, targetSql, dateRange);
    }

    private Optional<Long> getSoftBouncesUndeliverable(int mailingId, int companyId, long sentCount, long hardBouncesCount) {
        return getSoftBouncesUndeliverable(mailingId, companyId, sentCount, hardBouncesCount, "", new DateRange());
    }

    @Override
    public Optional<Long> getSoftBouncesUndeliverable(int mailingId, int companyId, long sentCount, long hardBouncesCount, String targetSql, DateRange dateRange) {
        if (!companyService.isMailtrackingActive(companyId) || isMailingExpired(mailingId, companyId)) {
            return Optional.empty();
        }

        if (!MailingType.NORMAL.equals(mailingService.getMailingType(mailingId))) {
            return Optional.empty();
        }

        return Optional.of(sentCount - getDeliveredMailsCount(mailingId, companyId, targetSql, dateRange) - hardBouncesCount);
    }

    private Optional<Long> getDeliveredCount(int mailingId, int companyId, long sentCount, long hardBouncesCount) {
        return getDeliveredCount(mailingId, companyId, sentCount, hardBouncesCount, "", new DateRange());
    }

    @Override
    public Optional<Long> getDeliveredCount(
            int mailingId,
            int companyId,
            long sentCount,
            long hardBouncesCount,
            String targetSql,
            DateRange dateRange
    ) {
        if (companyService.isMailtrackingActive(companyId) && statisticsDao.hasSuccessTableData(mailingId, companyId)) {
            return Optional.of(getDeliveredMailsCount(mailingId, companyId, targetSql, dateRange));
        }

        if (isMailingExpired(mailingId, companyId)) {
            return Optional.empty();
        }

        return Optional.of(sentCount - hardBouncesCount);
    }

    @Override
    public boolean isMailingExpired(int mailingId, int companyId) {
        int periodicallySendEntries = statisticsDao.getPeriodicallySendCount(mailingId);
        if (periodicallySendEntries > 0) {
            return false;
        }

        int successExpirationDays = configService.getIntegerValue(ConfigValue.ExpireSuccess, companyId);
        if (successExpirationDays <= 0) {
            return false;
        }

        return statisticsDao.countOfOnceSending(mailingId, DateUtilities.getDateOfDaysAgo(successExpirationDays)) <= 0;
    }

    @Override
    public long getDeliveredMailsCount(int mailingId, int companyId, String targetSql, DateRange dateRange) {
        return statisticsDao.getDeliveredCount(mailingId, companyId, targetSql, emptySet(), dateRange);
    }

    @Override
    public int getAnonymousOpenings(int mailingId, int companyId) {
        return getAnonymousOpenings(mailingId, companyId, new DateRange());
    }

    @Override
    public int getAnonymousOpenings(int mailingId, int companyId, DateRange dateRange) {
        return statisticsDao.getAnonymousOpenings(mailingId, companyId, dateRange);
    }

    @Override
    public int getAnonymousClicks(int mailingId, int companyId) {
        return getAnonymousClicks(mailingId, companyId, new DateRange());
    }

    @Override
    public int getAnonymousClicks(int mailingId, int companyId, DateRange dateRange) {
        return statisticsDao.getAnonymousClicks(mailingId, companyId, dateRange);
    }

    private int getHardBouncesCount(int mailingId, int companyId) {
        return getHardBouncesCount(
                mailingId,
                companyId,
                "",
                new DateRange()
        );
    }

    @Override
    public int getHardBouncesCount(int mailingId, int companyId, String targetSql, DateRange dateRange) {
        if (isMailingBouncesExpire(mailingId, companyId)) {
            return statisticsDao.getHardBouncesCountFromBindings(
                    mailingId,
                    companyId,
                    targetSql,
                    emptySet(),
                    dateRange
            );
        }

        return getBouncesCount(mailingId, companyId, targetSql, dateRange);
    }

    private int getBouncesCount(int mailingId, int companyId) {
        return getBouncesCount(mailingId, companyId, "", new DateRange());
    }

    private int getBouncesCount(int mailingId, int companyId, String targetSql, DateRange dateRange) {
        return statisticsDao.getHardBouncesCount(mailingId, companyId, targetSql, emptySet(), dateRange);
    }

    private int getMeasuredOpenersCount(int mailingId, int companyId) {
        return getMeasuredOpenersCount(mailingId, companyId, "", new DateRange());
    }

    @Override
    public int getMeasuredOpenersCount(int mailingId, int companyId, String targetSql, DateRange dateRange) {
        return getOpenersCount(
                mailingId,
                companyId,
                targetSql,
                dateRange,
                emptySet()
        );
    }

    @Override
    public int getOpenersCount(int mailingId, int companyId, String targetSql, DateRange dateRange, Set<Integer> deviceIds) {
        return statisticsDao.getOpenersCount(
                mailingId,
                companyId,
                targetSql,
                emptySet(),
                dateRange,
                deviceIds
        );
    }

    private int getClickersCount(int mailingId, int companyId) {
        return getClickersCount(
                mailingId,
                companyId,
                "",
                new DateRange()
        );
    }

    @Override
    public int getClickersCount(int mailingId, int companyId, String targetSql, DateRange dateRange) {
        return statisticsDao.getClickersCount(
                mailingId,
                companyId,
                targetSql,
                emptySet(),
                dateRange
        );
    }

    private int getSentWorldCount(int mailingId) {
        return getCountFromMailingAccount(mailingId, Set.of(MaildropStatus.WORLD), new DateRange());
    }

    private int getOptOutsCount(int mailingId, int companyId) {
        return getOptOutsCount(
                mailingId,
                companyId,
                "",
                new DateRange()
        );
    }

    @Override
    public int getOptOutsCount(int mailingId, int companyId, String targetSql, DateRange timestamp) {
        return statisticsDao.getOptOutsCount(mailingId, companyId, targetSql, emptySet(), timestamp);
    }

    private boolean isMailingBouncesExpire(int mailingId, int companyId) {
        return statisticsDao.getMailAgeInDays(mailingId, companyId)
                .filter(age -> configService.getIntegerValue(ConfigValue.ExpireBounce, companyId) < age)
                .isPresent();

    }

    @Override
    public int getSentCount(int mailingId, int companyId, String targetSql) {
        return getSentCount(mailingId, companyId, emptySet(), targetSql, new DateRange());
    }

    @Override
    public int getSentCount(
            int mailingId,
            int companyId,
            Set<MaildropStatus> maildropStatuses,
            String targetSql,
            DateRange dateRange
    ) {
        if (mailingService.getMailingType(mailingId) == MailingType.INTERVAL) {
            return statisticsDao.getSentCountForIntervalMailing(mailingId, companyId, targetSql, dateRange);
        }
        if (!isTargetFilterRequired(targetSql)) {
            // mailing_account_tbl has no customer ids and therefore cannot be used for target group specific numbers
            return getCountFromMailingAccount(mailingId, maildropStatuses, dateRange);
        }
        if (statisticsDao.isMailtrackTableExists(companyId)
            && companyService.isMailtrackingActive(companyId)
            && !isMailingExpired(mailingId, companyId)) {
            return getSentCountFromMailtrackTbl(mailingId, companyId, targetSql, dateRange);
        }
        return -1;
    }

    private int getCountFromMailingAccount(int mailingId, Set<MaildropStatus> maildropStatuses, DateRange dateRange) {
        return statisticsDao.getSentCountFromMailingAccount(mailingId, dateRange, maildropStatuses);
    }

    @Override
    public int getSentCountFromMailtrackTbl(int mailingId, int companyId, String targetSql, DateRange dateRange) {
        return statisticsDao.getSentCountFromMailtrackTbl(mailingId, companyId, targetSql, dateRange);
    }

    @Override
    public boolean isMailingTrackingDataAvailable(int mailingId, int companyId) {
        return companyService.isMailtrackingActive(companyId) && isTrackingExists(mailingId, companyId);
    }

    @Override
    public int getOpeningsCount(int mailingId, int companyId, String targetSql, DateRange dateRange) {
        return statisticsDao.getOpeningsCount(mailingId, companyId, null, targetSql, dateRange);
    }

    @Override
    public int getClicksCount(int mailingId, int companyId, String targetSql, DateRange dateRange) {
        return statisticsDao.getClicksCount(mailingId, companyId, null, targetSql, dateRange);
    }

    @Override
    public boolean isTrackingExists(int mailingId, int companyId) {
        if (mailingService.getMailingType(mailingId) == MailingType.INTERVAL) {
            return statisticsDao.isIntervalTrackDataExists(mailingId, companyId);
        }
        return statisticsDao.hasSuccessTableData(mailingId, companyId);
    }

    @Override
    public boolean isTrackingAvailableForMailing(int mailingId, int companyId) {
        return statisticsDao.isTrackingAvailableForMailing(mailingId, companyId);
    }

    private List<LightTarget> getTargets(Collection<Integer> targetIds, Integer companyId) {
        if (isEmpty(targetIds)) {
            return emptyList();
        }
        return lightTargetDao.get(List.copyOf(targetIds), companyId);
    }

    @Override
    public String getTargetSql(Collection<Integer> targetIds, int companyId) {
        if (isEmpty(targetIds)) {
            return "";
        }
        return targetIds.stream()
                .map(targetId -> lightTargetDao.getTarget(targetId, companyId))
                .map(LightTarget::getTargetSQL)
                .filter(StringUtils::isNotBlank)
                .map(sql -> "(" + sql + ")")
                .collect(Collectors.joining(" OR ", "(", ")"));
    }

    @Override
    public List<LightTarget> getTargetsWithAllSubscribersTarget(Collection<Integer> targetIds, Admin admin) {
        return Stream.concat(
                Stream.of(createAllSubscribersTarget(admin.getLocale())),
                ListUtils.emptyIfNull(getTargets(targetIds, admin.getCompanyID())).stream()
        ).toList();
    }

    @Override
    public SequencedMap<Integer, LightTarget> getTargetsMapWithAllSubscribersTarget(
            Collection<Integer> targetIds,
            Admin admin
    ) {
        return getTargetsWithAllSubscribersTarget(targetIds, admin)
                .stream()
                .collect(Collectors.toMap(
                        LightTarget::getId,
                        Function.identity(),
                        (a, b) -> b,
                        LinkedHashMap::new
                ));
    }

    private static LightTarget createAllSubscribersTarget(Locale locale) {
        LightTarget allSubscribers = new LightTarget();
        allSubscribers.setId(ALL_SUBSCRIBERS_TARGETGROUP_ID);
        allSubscribers.setName(t(CommonKeys.ALL_SUBSCRIBERS, locale));
        allSubscribers.setTargetSQL("1 = 1");
        return allSubscribers;
    }

    @Override
    public Map<DeviceClass, Integer> getOpenersByDevice(
            int mailingId,
            int companyId,
            String targetSql,
            Set<BindingEntry.UserType> userTypes,
            DateRange dateRange
    ) {
        return statisticsDao.getOpenersByDevice(mailingId, companyId, targetSql, userTypes, dateRange);
    }

    @Override
    public Map<DeviceClass, Integer> getClickersByDevice(
            int mailingId,
            int companyId,
            String targetSql,
            Set<BindingEntry.UserType> userTypes,
            DateRange dateRange
    ) {
        return statisticsDao.getClickersByDevice(mailingId, companyId, targetSql, userTypes, dateRange);
    }

    @Override
    public boolean isOnePixelStatisticExpired(LocalDate date) {
        return ExpirationUtils.isDateOlderThenDays(configService.getIntegerValue(ConfigValue.ExpireStatistics), date);
    }

    @Override
    public boolean isSuccessStatisticExpired(LocalDate date) {
        return ExpirationUtils.isDateOlderThenDays(configService.getIntegerValue(ConfigValue.ExpireSuccess), date);
    }
}
