/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtstatistics.service;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SequencedMap;
import java.util.Set;

import com.agnitas.beans.Admin;
import com.agnitas.beans.BindingEntry;
import com.agnitas.emm.core.commons.dto.DateRange;
import com.agnitas.emm.core.maildrop.MaildropStatus;
import com.agnitas.emm.core.mobile.bean.DeviceClass;
import com.agnitas.reporting.birt.external.beans.LightTarget;
import com.agnitas.reporting.birt.external.beans.StatisticMetric;

public interface MailingStatisticsService {

    int ALL_SUBSCRIBERS_TARGETGROUP_ID = 1;

    Map<Integer, StatisticMetric> getWorldSentMailings(Set<Integer> mailingIds);

    Map<Integer, StatisticMetric> getMeasuredOpeners(Set<Integer> mailingIds, int companyId);

    Map<Integer, StatisticMetric> getHardBounces(Set<Integer> mailingIds, int companyId);

    Map<Integer, StatisticMetric> getOptOuts(Set<Integer> mailingIds, int companyId);

    Map<Integer, StatisticMetric> getClickers(Set<Integer> mailingIds, int companyId);

    Map<Integer, StatisticMetric> getDelivered(
            Set<Integer> mailingIds,
            int companyId,
            Map<Integer, StatisticMetric> sent,
            Map<Integer, StatisticMetric> hardBounces
    );

    Map<Integer, StatisticMetric> getSoftBouncesUndeliverable(
            Set<Integer> mailingIds,
            int companyId,
            Map<Integer, StatisticMetric> sent,
            Map<Integer, StatisticMetric> hardBounces
    );

    Map<Integer, StatisticMetric> getInvisibleOpeners(
            Set<Integer> mailingIds,
            int companyId,
            Map<Integer, StatisticMetric> measuredOpeners,
            Map<Integer, StatisticMetric> sent
    );

    Map<Integer, StatisticMetric> getTotalOpeners(
            Set<Integer> mailingIds,
            Map<Integer, StatisticMetric> invisibleOpeners,
            Map<Integer, StatisticMetric> measuredOpeners
    );

    double getRevenueCount(int mailingId, int companyId, String targetSql, DateRange dateRange);

    Optional<Long> getSoftBouncesUndeliverable(int mailingId, int companyId, long sentCount, long hardBouncesCount, String targetSql, DateRange dateRange);

    Optional<Long> getDeliveredCount(
            int mailingId,
            int companyId,
            long sentCount,
            long hardBouncesCount,
            String targetSql,
            DateRange dateRange
    );

    boolean isMailingExpired(int mailingId, int companyId);

    long getDeliveredMailsCount(int mailingId, int companyId, String targetSql, DateRange dateRange);

    int getAnonymousOpenings(int mailingId, int companyId);

    int getAnonymousOpenings(int mailingId, int companyId, DateRange dateRange);

    int getAnonymousClicks(int mailingId, int companyId);

    long getInvisibleOpenersCount(int mailingId, int companyId, long measuredOpeners, long sentCount, String targetSql, DateRange dateRange);

    Optional<Double> getRevenue(int mailingId, int companyId);

    int getAnonymousClicks(int mailingId, int companyId, DateRange dateRange);

    int getHardBouncesCount(int mailingId, int companyId, String targetSql, DateRange dateRange);

    int getMeasuredOpenersCount(int mailingId, int companyId, String targetSql, DateRange dateRange);

    int getOpenersCount(int mailingId, int companyId, String targetSql, DateRange dateRange, Set<Integer> deviceIds);

    int getClickersCount(int mailingId, int companyId, String targetSql, DateRange dateRange);

    int getOptOutsCount(int mailingId, int companyId, String targetSql, DateRange timestamp);

    int getSentCount(int mailingId, int companyId, String targetSql);

    int getSentCount(
            int mailingId,
            int companyId,
            Set<MaildropStatus> maildropStatuses,
            String targetSql,
            DateRange dateRange
    );

    int getSentCountFromMailtrackTbl(int mailingId, int companyId, String targetSql, DateRange dateRange);

    boolean isMailingTrackingDataAvailable(int mailingId, int companyId);

    int getOpeningsCount(int mailingId, int companyId, String targetSql, DateRange dateRange);

    int getClicksCount(int mailingId, int companyId, String targetSql, DateRange dateRange);

    boolean isTrackingExists(int mailingId, int companyId);

    boolean isTrackingAvailableForMailing(int mailingId, int companyId);

    String getTargetSql(Collection<Integer> targetIds, int companyId);

    List<LightTarget> getTargetsWithAllSubscribersTarget(Collection<Integer> targetIds, Admin admin);

    SequencedMap<Integer, LightTarget> getTargetsMapWithAllSubscribersTarget(Collection<Integer> targetIds, Admin admin);

    Map<DeviceClass, Integer> getOpenersByDevice(
            int mailingId,
            int companyId,
            String targetSql,
            Set<BindingEntry.UserType> userTypes,
            DateRange dateRange
    );

    Map<DeviceClass, Integer> getClickersByDevice(
            int mailingId,
            int companyId,
            String targetSql,
            Set<BindingEntry.UserType> userTypes,
            DateRange dateRange
    );

    boolean isOnePixelStatisticExpired(LocalDate date);

    boolean isSuccessStatisticExpired(LocalDate date);
}
