/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.birtstatistics.service;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.SequencedMap;
import java.util.Set;

import com.agnitas.beans.Admin;
import com.agnitas.beans.BindingEntry.UserType;
import com.agnitas.dao.MailingStatisticsDao;
import com.agnitas.emm.common.MailingType;
import com.agnitas.emm.core.commons.dto.DateRange;
import com.agnitas.emm.core.maildrop.MaildropStatus;
import com.agnitas.emm.core.mobile.bean.DeviceClass;
import com.agnitas.reporting.birt.external.beans.LightTarget;

public interface MailingStatisticsService {

    int ALL_SUBSCRIBERS_TARGETGROUP_ID = 1;
    String ALL_SUBSCRIBERS_MSG_KEY = "statistic.all_subscribers";

    record ByDeviceClass(long pc, long tablet, long mobile, long smartTv, long multi) {
    }

    record RecipientsByMailFormat(long html, long text, long offlineHtml) {
    }

    ByDeviceClass toByDeviceClass(Map<DeviceClass, Integer> byDeviceClass, long total);

    int getSentWorldCount(int mailingId);

    RecipientsByMailFormat getRecipientsByMailFormat(int mailingId, MailingType mailingType, DateRange dateRange);

    MailingRawMetrics getRawMetrics(int mailingId, int companyId, Set<MaildropStatus> maildropStatuses, String targetSql, DateRange dateRange);

    int getOptOutsCount(int mailingId, int companyId);

    double getRevenueCount(int mailingId, int companyId, String targetSql, DateRange dateRange);

    long getAvgMailingSizeKb(int mailingId, int companyId);

    Optional<Long> getSoftBouncesUndeliverable(int mailingId, int companyId, long sentCount, long hardBouncesCount);

    Optional<Long> getSoftBouncesUndeliverable(int mailingId, int companyId, long sentCount, long hardBouncesCount, String targetSql, DateRange dateRange);

    Optional<Long> getDeliveredCount(int mailingId, int companyId, long sentCount, long hardBouncesCount);

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

    long getInvisibleOpenersCount(int mailingId, int companyId, int measuredOpenersCount, int sentCount);

    long getInvisibleOpenersCount(int mailingId, int companyId, long measuredOpeners, long sentCount, String targetSql, DateRange dateRange);

    Optional<Double> getRevenue(int mailingId, int companyId);

    Optional<Double> getRevenue(int mailingId, int companyId, DateRange dateRange);

    int getAnonymousClicks(int mailingId, int companyId, DateRange dateRange);

    int getHardBouncesCount(int mailingId, int companyId);

    int getHardBouncesCount(int mailingId, int companyId, String targetSql, DateRange dateRange);

    int getMeasuredOpenersCount(int mailingId, int companyId);

    int getMeasuredOpenersCount(int mailingId, int companyId, String targetSql, DateRange dateRange);

    int getOpenersCount(int mailingId, int companyId, String targetSql, DateRange dateRange, Set<Integer> deviceIds);

    int getClickersCount(int mailingId, int companyId);

    int getClickersCount(int mailingId, int companyId, String targetSql, DateRange dateRange);

    int getOptOutsCount(int mailingId, int companyId, String targetSql, DateRange timestamp);

    int getSentCount(int mailingId, int companyId, Set<MaildropStatus> maildropStatuses);

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

    List<LightTarget> getTargetsWithAllSubscribersTarget(Collection<Integer> targetIds, Admin admin);

    List<LightTarget> getTargetsWithAllSubscribersTarget(
            Collection<Integer> targetIds,
            int companyId,
            Locale locale
    );

    SequencedMap<Integer, LightTarget> getTargetsMapWithAllSubscribersTarget(Collection<Integer> targetIds, Admin admin);

    Map<DeviceClass, Integer> getOpenersByDevice(int mailingId, int companyId, DateRange dateRange);

    Map<DeviceClass, Integer> getOpenersByDevice(
            int mailingId,
            int companyId,
            String targetSql,
            DateRange dateRange
    );

    Map<DeviceClass, Integer> getClickersByDevice(int mailingId, int companyId, DateRange dateRange);

    Map<DeviceClass, Integer> getClickersByDevice(
            int mailingId,
            int companyId,
            String targetSql,
            DateRange dateRange
    );

    List<MailingStatisticsDao.DeviceCount> getOpenerDeviceCounts(
            int mailingId,
            int companyId,
            List<LightTarget> targets,
            DeviceClass deviceClass,
            DateRange dateRange,
            int maxCount
    );

    List<MailingStatisticsDao.DeviceCount> getClickerDeviceCounts(
            int mailingId,
            int companyId,
            List<LightTarget> targets,
            DeviceClass deviceClass,
            DateRange dateRange,
            int maxCount
    );

    boolean isOnePixelStatisticExpired(LocalDate date);

    boolean isSuccessStatisticExpired(LocalDate date);
}
