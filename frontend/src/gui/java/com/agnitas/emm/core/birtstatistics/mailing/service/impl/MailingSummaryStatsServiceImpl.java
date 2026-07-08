/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtstatistics.mailing.service.impl;

import static com.agnitas.emm.core.birtstatistics.service.MailingStatisticsService.ALL_SUBSCRIBERS_TARGETGROUP_ID;
import static com.agnitas.messages.I18nString.t;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.agnitas.beans.Mailing;
import com.agnitas.beans.MediatypeEmail;
import com.agnitas.dao.MailingStatisticsDao;
import com.agnitas.emm.common.MailingType;
import com.agnitas.emm.core.birtstatistics.mailing.dao.MailingSummaryStatsDao;
import com.agnitas.emm.core.birtstatistics.mailing.dao.MailingSummaryStatsDao.DispatchInfo;
import com.agnitas.emm.core.birtstatistics.mailing.dto.MailingSummaryStatsOpts;
import com.agnitas.emm.core.birtstatistics.mailing.dto.MailingSummaryStatsResponse.GeneralInfo;
import com.agnitas.emm.core.birtstatistics.mailing.service.MailingSummaryStatsService;
import com.agnitas.emm.core.birtstatistics.service.MailingStatisticsService;
import com.agnitas.emm.core.maildrop.MaildropStatus;
import com.agnitas.emm.core.mobile.bean.DeviceClass;
import com.agnitas.emm.core.target.service.TargetService;
import com.agnitas.reporting.birt.external.beans.LightTarget;
import org.springframework.stereotype.Service;

@Service
public class MailingSummaryStatsServiceImpl implements MailingSummaryStatsService {

    private static final Set<Integer> PROXY_DEVICE_IDS = Set.of(
            1484, // apple proxy device id
            1703  // yahoo proxy device id
    );

    private final MailingStatisticsService mailingStatsService;
    private final TargetService targetService;
    private final MailingSummaryStatsDao summaryStatsDao;

    public MailingSummaryStatsServiceImpl(
            MailingStatisticsService mailingStatsService,
            TargetService targetService,
            MailingSummaryStatsDao summaryStatsDao
    ) {
        this.mailingStatsService = mailingStatsService;
        this.targetService = targetService;
        this.summaryStatsDao = summaryStatsDao;
    }

    @Override
    public GeneralInfo getGeneralInfo(MailingSummaryStatsOpts opts) {
        DispatchInfo dispatchInfo = summaryStatsDao.getDispatchInfo(opts.getMailingId(), opts.getDateRange());
        Optional<MediatypeEmail> emailParam = Optional.ofNullable(opts.getMailing().getEmailParam());
        String noDataMsg = t("mailing.NoEmailDataAvailable", opts.getLocale());
        String sendStart = formatDate(opts.getStartDate(), dispatchInfo::sendStart, opts.getDateFormat());
        String sendEnd = formatDate(opts.getEndDate(), dispatchInfo::sendEnd, opts.getDateFormat());
        return new GeneralInfo(
                emailParam.map(MediatypeEmail::getSubject).orElse(noDataMsg),
                emailParam.map(p -> t("MailType." + p.getMailFormat(), opts.getLocale())).orElse(noDataMsg),
                getMailingTargetNames(opts),
                dispatchInfo.sentCount(),
                getAnonymousCount(opts.getMailingId(), opts.getCompanyId()),
                sendStart,
                sendEnd,
                isActivatable(opts.getMailing()) ? "%s - %s".formatted(sendStart, sendEnd) : null,
                formatDate(dispatchInfo.sendStart(), () -> null, opts.getDateFormat()),
                getSizeInKb(dispatchInfo, opts.getMailingId(), opts.getCompanyId())
        );
    }

    private List<String> getMailingTargetNames(MailingSummaryStatsOpts opts) {
        Collection<Integer> targetGroups = opts.getMailing().getTargetGroups();
        if (isEmpty(targetGroups)) {
            return List.of(t("statistic.all_subscribers", opts.getLocale()));
        }
        return targetService.getTargetNamesByIds(opts.getCompanyId(), new HashSet<>(targetGroups));
    }

    private String formatDate(Date primary, Supplier<Date> fallback, SimpleDateFormat fmt) {
        return Optional.ofNullable(primary)
                .or(() -> Optional.ofNullable(fallback.get()))
                .map(fmt::format)
                .orElse("-");
    }

    private static boolean isActivatable(Mailing mailing) {
        return List.of(
                MailingType.DATE_BASED,
                MailingType.ACTION_BASED,
                MailingType.INTERVAL
        ).contains(mailing.getMailingType());
    }

    private int getAnonymousCount(int mailingId, int companyId) {
        if (!mailingStatsService.isTrackingAvailableForMailing(mailingId, companyId)) {
            return 0;
        }
        return summaryStatsDao.getAnonymousUsersCount(mailingId, companyId);
    }

    private long getSizeInKb(DispatchInfo dispatchInfo, int mailingId, int companyId) {
        if (dispatchInfo.sentCount() <= 0) {
            return 0;
        }
        long numberOfBytes = dispatchInfo.bytes()
                             + summaryStatsDao.getRdirTrafficAgrSize(mailingId, companyId)
                             + summaryStatsDao.getRdirTrafficAmountSize(mailingId, companyId);
        return numberOfBytes / dispatchInfo.sentCount() / 1024;
    }

    @Override // target -> metrics
    public Map<Integer, Stats> getStats(MailingSummaryStatsOpts opts) {
        return opts.getTargets().stream().collect(Collectors.toMap(
                LightTarget::getId,
                t -> getStats(t, opts),
                (a, b) -> a, LinkedHashMap::new
        ));
    }

    private Stats getStats(LightTarget target, MailingSummaryStatsOpts opts) {
        long sent = getSentCount(target, opts);
        int hardBounces = getHardBouncesCount(target, opts);
        int openings = getOpeningsCount(target, opts);
        int clicks = getClicksCount(target, opts);
        return new Stats(
                sent,
                getDeliveredEmailsCount(target, opts, sent, hardBounces),
                openings,
                getAnonymousOpeningsCount(target, opts),
                getOpeners(target, opts, sent),
                clicks,
                getAnonymousClicksCount(target, opts),
                getClickers(target, opts),
                getOptOutsCount(target, opts),
                hardBounces,
                getSoftBouncesUndeliverable(target, opts, sent, hardBounces),
                getRevenueCount(target, opts),
                openings == 0 ? 0.0 : (double) clicks / openings
        );
    }

    private int getClicksCount(LightTarget target, MailingSummaryStatsOpts opts) {
        if (opts.hasPeriod() && opts.isOnepixelStatisticExpired()) {
            return -1;
        }
        return mailingStatsService.getClicksCount(
                opts.getMailingId(),
                opts.getCompanyId(),
                target.getTargetSQL(),
                opts.getDateRange()
        );
    }

    private int getOpeningsCount(LightTarget target, MailingSummaryStatsOpts opts) {
        if (opts.hasPeriod() && opts.isOnepixelStatisticExpired()) {
            return -1;
        }
        return mailingStatsService.getOpeningsCount(
                opts.getMailingId(),
                opts.getCompanyId(),
                target.getTargetSQL(),
                opts.getDateRange()
        );
    }

    private Openers getOpeners(LightTarget target, MailingSummaryStatsOpts opts, long sentCount) {
        long measured = getMeasuredOpenersCount(target, opts);
        long invisible = getInvisibleOpenersCount(target, measured, sentCount, opts);
        long total = measured + invisible;

        ByDevice byDevice = getInteractionByDevice(getOpenersByDevice(target, opts), measured);
        return new Openers(
                measured,
                getProxyOpenersCount(target, opts),
                invisible,
                total,
                byDevice
        );
    }

    private Clickers getClickers(LightTarget target, MailingSummaryStatsOpts opts) {
        int total = getClickersCount(target, opts);
        ByDevice byDevice = getInteractionByDevice(getClickersByDevice(target, opts), total);
        return new Clickers(total, byDevice);
    }

    private ByDevice getInteractionByDevice(Map<DeviceClass, Integer> map, long total) {
        long multi = total;
        for (DeviceClass deviceClass : MailingStatisticsDao.AVAILABLE_DEVICE_CLASSES) {
            if (map.containsKey(deviceClass)) {
                multi -= map.get(deviceClass);
            }
        }
        return new ByDevice(
                map.get(DeviceClass.DESKTOP),
                map.get(DeviceClass.TABLET),
                map.get(DeviceClass.MOBILE),
                map.get(DeviceClass.SMARTTV),
                multi
        );
    }

    private Map<DeviceClass, Integer> getOpenersByDevice(LightTarget target, MailingSummaryStatsOpts opts) {
        return mailingStatsService.getOpenersByDevice(
                opts.getMailingId(),
                opts.getCompanyId(),
                target.getTargetSQL(),
                Collections.emptySet(),
                opts.getDateRange()
        );
    }

    private Map<DeviceClass, Integer> getClickersByDevice(LightTarget target, MailingSummaryStatsOpts opts) {
        return mailingStatsService.getClickersByDevice(
                opts.getMailingId(),
                opts.getCompanyId(),
                target.getTargetSQL(),
                Collections.emptySet(),
                opts.getDateRange()
        );
    }

    private long getDeliveredEmailsCount(
            LightTarget target,
            MailingSummaryStatsOpts opts,
            long sentCount,
            long hardBouncesCount
    ) {
        return mailingStatsService.getDeliveredCount(
                opts.getMailingId(),
                opts.getCompanyId(),
                sentCount,
                hardBouncesCount,
                target.getTargetSQL(),
                opts.getDateRange()
        ).orElse(0L);
    }

    private double getRevenueCount(LightTarget target, MailingSummaryStatsOpts opts) {
        return mailingStatsService.getRevenueCount(
                opts.getMailingId(),
                opts.getCompanyId(),
                target.getTargetSQL(), opts.getDateRange());
    }

    private int getAnonymousClicksCount(LightTarget target, MailingSummaryStatsOpts opts) {
        if (target.getId() != ALL_SUBSCRIBERS_TARGETGROUP_ID) {
            return -1;
        }
        return mailingStatsService.getAnonymousClicks(
                opts.getMailingId(),
                opts.getCompanyId(),
                opts.getDateRange()
        );
    }

    private int getClickersCount(LightTarget target, MailingSummaryStatsOpts opts) {
        return mailingStatsService.getClickersCount(
                opts.getMailingId(),
                opts.getCompanyId(),
                target.getTargetSQL(),
                opts.getDateRange()
        );
    }

    private int getHardBouncesCount(LightTarget target, MailingSummaryStatsOpts opts) {
        return mailingStatsService.getHardBouncesCount(
                opts.getMailingId(),
                opts.getCompanyId(),
                target.getTargetSQL(),
                opts.getDateRange()
        );
    }

    private int getOptOutsCount(LightTarget target, MailingSummaryStatsOpts opts) {
        return mailingStatsService.getOptOutsCount(
                opts.getMailingId(), opts.getCompanyId(),
                target.getTargetSQL(), opts.getDateRange()
        );
    }

    private int getMeasuredOpenersCount(LightTarget target, MailingSummaryStatsOpts opts) {
        return mailingStatsService.getMeasuredOpenersCount(
                opts.getMailingId(),
                opts.getCompanyId(),
                target.getTargetSQL(),
                opts.getDateRange()
        );
    }

    private long getSentCount(LightTarget target, MailingSummaryStatsOpts opts) {
        if (target.getId() != ALL_SUBSCRIBERS_TARGETGROUP_ID
            && !mailingStatsService.isMailingTrackingDataAvailable(opts.getMailingId(), opts.getCompanyId())) {
            return -1;
        }

        List<MaildropStatus> statuses = MaildropStatus.allExcept(
                MaildropStatus.TEST,
                MaildropStatus.ADMIN,
                MaildropStatus.PREDELIVERY
        );

        return mailingStatsService.getSentCount(
                opts.getMailingId(),
                opts.getCompanyId(),
                Set.copyOf(statuses),
                target.getTargetSQL(),
                opts.getDateRange()
        );
    }

    private int getProxyOpenersCount(LightTarget target, MailingSummaryStatsOpts opts) {
        return mailingStatsService.getOpenersCount(
                opts.getMailingId(), opts.getCompanyId(),
                target.getTargetSQL(), opts.getDateRange(),
                PROXY_DEVICE_IDS
        );
    }

    private long getAnonymousOpeningsCount(LightTarget target, MailingSummaryStatsOpts opts) {
        if (target.getId() != ALL_SUBSCRIBERS_TARGETGROUP_ID) {
            return -1;
        }
        return mailingStatsService.getAnonymousOpenings(
                opts.getMailingId(),
                opts.getCompanyId(),
                opts.getDateRange()
        );
    }

    private long getInvisibleOpenersCount(
            LightTarget target,
            long sentEmails,
            long measuredOpeners,
            MailingSummaryStatsOpts opts
    ) {
        return mailingStatsService.getInvisibleOpenersCount(
                opts.getMailingId(), opts.getCompanyId(),
                measuredOpeners, sentEmails,
                target.getTargetSQL(), opts.getDateRange()
        );
    }

    private long getSoftBouncesUndeliverable(
            LightTarget target,
            MailingSummaryStatsOpts opts,
            long sentCount,
            int hardBouncesCount
    ) {
        return mailingStatsService.getSoftBouncesUndeliverable(
                opts.getMailingId(),
                opts.getCompanyId(),
                sentCount,
                hardBouncesCount,
                target.getTargetSQL(),
                opts.getDateRange()
        ).orElse(0L);
    }
}
