/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.birtstatistics.mailing;

import static com.agnitas.dao.MailingStatisticsDao.PROXY_DEVICE_IDS;
import static com.agnitas.emm.core.birtstatistics.service.MailingStatisticsService.ALL_SUBSCRIBERS_TARGETGROUP_ID;
import static com.agnitas.messages.I18nString.t;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.agnitas.beans.Admin;
import com.agnitas.beans.Mailing;
import com.agnitas.beans.MediatypeEmail;
import com.agnitas.emm.common.MailingType;
import com.agnitas.emm.core.birtstatistics.mailing.MailingSummaryStatsDao.DispatchInfo;
import com.agnitas.emm.core.birtstatistics.service.MailingStatisticsService;
import com.agnitas.emm.core.commons.dto.DateRange;
import com.agnitas.emm.core.maildrop.MaildropStatus;
import com.agnitas.emm.core.mobile.bean.DeviceClass;
import com.agnitas.emm.core.target.service.TargetService;
import com.agnitas.reporting.birt.external.beans.LightTarget;
import com.agnitas.util.CsvWriter;
import org.springframework.stereotype.Service;

@Service
public class MailingSummaryStatsServiceImpl implements MailingSummaryStatsService {

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.00%");

    private final MailingStatisticsService mailingStatsService;
    private final TargetService targetService;
    private final MailingSummaryStatsDao summaryStatsDao;
    private final MailingSummaryStatsOptsFactory factory;

    public MailingSummaryStatsServiceImpl(
            MailingStatisticsService mailingStatsService,
            TargetService targetService,
            MailingSummaryStatsDao summaryStatsDao,
            MailingSummaryStatsOptsFactory factory
    ) {
        this.mailingStatsService = mailingStatsService;
        this.targetService = targetService;
        this.summaryStatsDao = summaryStatsDao;
        this.factory = factory;
    }

    @Override
    public byte[] csv(int mailingId, Admin admin) throws Exception {
        MailingSummaryStatsOpts opts = factory.createOpts(mailingId, admin);
        return csv(opts, admin);
    }

    @Override
    public byte[] csv(MailingSummaryStatsOpts opts, Admin admin) throws Exception {
        return CsvWriter.csv(
                getHeadersForCsv(admin.getLocale()),
                getCsvRows(getStats(opts), opts)
        );
    }

    private static List<String> getHeadersForCsv(Locale locale) {
        return Stream.of("birt.Target", "grid.mediapool.category", "value", "Brutto", "Netto")
                .map(key -> t(key, locale))
                .toList();
    }

    private List<List<String>> getCsvRows(Map<Integer, Stats> targetStatsMap, MailingSummaryStatsOpts opts) {
        return Arrays.stream(CsvCategory.values())
                .flatMap(category -> targetStatsMap.entrySet().stream()
                        .filter(entry -> isAllowedCsvRow(category, entry, opts))
                        .map(entry -> getCsvRow(
                                category,
                                entry.getKey(),
                                mapDeviceStatsForCsv(category, entry.getValue()),
                                opts
                        )))
                .toList();
    }

    private static boolean isAllowedCsvRow(
            CsvCategory category,
            Map.Entry<Integer, Stats> entry,
            MailingSummaryStatsOpts opts
    ) {
        if (category == CsvCategory.UNDELIVERABLE && skipUndeliverable(opts)) {
            return false;
        }
        if (category == CsvCategory.DELIVERED_EMAILS && entry.getValue().deliveredEmails() == null) {
            return false;
        }
        return (category != CsvCategory.ANONYMOUS_OPENINGS && category != CsvCategory.ANONYMOUS_CLICKS)
               || entry.getKey() == MailingStatisticsService.ALL_SUBSCRIBERS_TARGETGROUP_ID;
    }

    private static boolean skipUndeliverable(MailingSummaryStatsOpts opts) {
        return opts.getMailing().getMailingType() != MailingType.NORMAL
               || !opts.isMailtrackingActive()
               || opts.isMailingExpired();
    }

    private static List<String> getCsvRow(
            CsvCategory category,
            Integer targetId,
            CsvCategory.Value data,
            MailingSummaryStatsOpts opts
    ) {
        return List.of(
                opts.getTargetName(targetId),
                t(category.getMsgKey(), opts.getLocale()),
                formatCsvValue(data.val()),
                DECIMAL_FORMAT.format(Math.max(0, data.gross() / 100.0)),
                DECIMAL_FORMAT.format(Math.max(0, data.net() / 100.0))
        );
    }

    private static String formatCsvValue(Number value) {
        return value == null || value.longValue() <= 0
                ? "0"
                : BigDecimal.valueOf(value.doubleValue())
                .stripTrailingZeros()
                .toPlainString();
    }

    public static CsvCategory.Value mapDeviceStatsForCsv(CsvCategory type, Stats stats) {
        long grossBase = stats.sentEmails();
        long netBase = stats.deliveredEmails() == null ? 0 : stats.deliveredEmails();

        return switch (type) {
            case SENT_EMAILS -> new CsvCategory.Value(stats.sentEmails(), grossBase, stats.deliveredEmails() == null ? 0 : grossBase);
            case DELIVERED_EMAILS -> new CsvCategory.Value(stats.deliveredEmails(), grossBase, grossBase);
            case MEASURED_OPENERS -> new CsvCategory.Value(stats.openers().measured(), grossBase, netBase);
            case OPENERS_INVISIBLE -> new CsvCategory.Value(stats.openers().invisible(), grossBase, netBase);
            case OPENERS_TOTAL -> new CsvCategory.Value(stats.openers().total(), grossBase, netBase);
            case CLICKERS -> new CsvCategory.Value(stats.clickers().total(), grossBase, netBase);
            case OPT_OUTS -> new CsvCategory.Value(stats.optOuts(), grossBase, netBase);
            case HARD_BOUNCES -> new CsvCategory.Value(stats.hardBounces(), grossBase, netBase);
            case UNDELIVERABLE -> new CsvCategory.Value(stats.undelivered(), grossBase, netBase);
            case REVENUE -> new CsvCategory.Value(stats.revenue() * 100, grossBase, netBase);
            case OPENERS_PC -> new CsvCategory.Value(stats.openers().byDevice().pc(), stats.openers().measured(), netBase);
            case OPENERS_TABLET -> new CsvCategory.Value(stats.openers().byDevice().tablet(), stats.openers().measured(), netBase);
            case OPENERS_MOBILE -> new CsvCategory.Value(stats.openers().byDevice().mobile(), stats.openers().measured(), netBase);
            case OPENERS_SMART_TV -> new CsvCategory.Value(stats.openers().byDevice().smartTv(), stats.openers().measured(), netBase);
            case OPENERS_MULTI -> new CsvCategory.Value(stats.openers().byDevice().multi(), stats.openers().measured(), netBase);
            case CLICKERS_TRACKED -> new CsvCategory.Value(stats.clickers().total(), stats.clickers().total(), netBase);
            case CLICKERS_PC -> new CsvCategory.Value(stats.clickers().byDevice().pc(), stats.clickers().total(), netBase);
            case CLICKERS_TABLET -> new CsvCategory.Value(stats.clickers().byDevice().tablet(), stats.clickers().total(), netBase);
            case CLICKERS_MOBILE -> new CsvCategory.Value(stats.clickers().byDevice().mobile(), stats.clickers().total(), netBase);
            case CLICKERS_SMART_TV -> new CsvCategory.Value(stats.clickers().byDevice().smartTv(), stats.clickers().total(), netBase);
            case CLICKERS_MULTI -> new CsvCategory.Value(stats.clickers().byDevice().multi(), stats.clickers().total(), netBase);
            case OPENINGS_GROSS -> new CsvCategory.Value(stats.openingsGross(), -1, -1);
            case CLICKS_GROSS -> new CsvCategory.Value(stats.clicks(), -1, -1);
            case ACTIVITY_RATE -> new CsvCategory.Value(0, stats.activityRate() * 100, 0);
            case ANONYMOUS_OPENINGS -> new CsvCategory.Value(stats.anonymousOpenings(), -1, -1);
            case ANONYMOUS_CLICKS -> new CsvCategory.Value(stats.anonymousClicks(), -1, -1);
            case PROXY_OPENERS -> new CsvCategory.Value(stats.openers().proxy(), -1, -1);
        };
    }

    @Override
    public DispatchStats getDispatchStats(int mailingId, int companyId, DateRange dateRange) {
        DispatchInfo dispatchInfo = summaryStatsDao.getDispatchInfo(mailingId, dateRange);
        return new DispatchStats(
                dispatchInfo.sentCount(),
                getAnonymousCount(mailingId, companyId),
                dispatchInfo.sendStart(),
                dispatchInfo.sendEnd(),
                getSizeInKb(dispatchInfo, mailingId, companyId)
        );
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

    private long getAnonymousCount(int mailingId, int companyId) {
        if (!mailingStatsService.isTrackingAvailableForMailing(mailingId, companyId)) {
            return 0;
        }
        return summaryStatsDao.getAnonymousRecipientsCount(mailingId, companyId);
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

    @Override
    public Stats getStats(LightTarget target, MailingSummaryStatsOpts opts) {
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
        long measuredOpeners = getMeasuredOpenersCount(target, opts);
        long invisible = getInvisibleOpenersCount(target, sentCount, measuredOpeners, opts);
        long total = measuredOpeners + invisible;

        ByDevice byDevice = getInteractionByDevice(getOpenersByDevice(target, opts), measuredOpeners);
        return new Openers(
                measuredOpeners,
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
        MailingStatisticsService.ByDeviceClass byDeviceClass = mailingStatsService.toByDeviceClass(map, total);
        return new ByDevice(
                byDeviceClass.pc(),
                byDeviceClass.tablet(),
                byDeviceClass.mobile(),
                byDeviceClass.smartTv(),
                byDeviceClass.multi()
        );
    }

    private Map<DeviceClass, Integer> getOpenersByDevice(LightTarget target, MailingSummaryStatsOpts opts) {
        return mailingStatsService.getOpenersByDevice(
                opts.getMailingId(),
                opts.getCompanyId(),
                target.getTargetSQL(),
                opts.getDateRange()
        );
    }

    private Map<DeviceClass, Integer> getClickersByDevice(LightTarget target, MailingSummaryStatsOpts opts) {
        return mailingStatsService.getClickersByDevice(
                opts.getMailingId(),
                opts.getCompanyId(),
                target.getTargetSQL(),
                opts.getDateRange()
        );
    }

    private Long getDeliveredEmailsCount(
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
        ).orElse(null);
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

        return mailingStatsService.getSentCount(
                opts.getMailingId(),
                opts.getCompanyId(),
                MaildropStatus.realSendStatuses(),
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
            long hardBouncesCount
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
