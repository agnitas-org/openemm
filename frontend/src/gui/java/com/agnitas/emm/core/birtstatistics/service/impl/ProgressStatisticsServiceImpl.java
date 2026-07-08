/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtstatistics.service.impl;

import static com.agnitas.emm.core.birtstatistics.mailing.service.TargetGroupEventProgressBuilder.MIXED_DEVICE_CLASS_ID;
import static com.agnitas.messages.I18nString.getLocaleString;
import static java.time.temporal.TemporalAdjusters.firstDayOfMonth;
import static java.time.temporal.TemporalAdjusters.lastDayOfMonth;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SequencedMap;
import java.util.Set;

import com.agnitas.beans.Admin;
import com.agnitas.emm.common.MailingType;
import com.agnitas.emm.core.birtstatistics.dto.MailingProgressStatisticsResponse;
import com.agnitas.emm.core.birtstatistics.dto.TargetGroupEventProgressDTO;
import com.agnitas.emm.core.birtstatistics.mailing.dao.MailingProgressStatsDao;
import com.agnitas.emm.core.birtstatistics.mailing.mapper.ProgressStatisticsMapper;
import com.agnitas.emm.core.birtstatistics.mailing.service.TargetGroupProgressBuilderFactory;
import com.agnitas.emm.core.birtstatistics.model.MailingDeliveryProgressStats;
import com.agnitas.emm.core.birtstatistics.model.TargetGroupEventProgress;
import com.agnitas.emm.core.birtstatistics.service.MailingStatisticsService;
import com.agnitas.emm.core.birtstatistics.service.ProgressStatisticsService;
import com.agnitas.emm.core.commons.dto.DateRange;
import com.agnitas.emm.core.mailing.service.MailingService;
import com.agnitas.emm.core.mobile.bean.DeviceClass;
import com.agnitas.reporting.birt.external.beans.LightTarget;
import com.agnitas.util.CsvWriter;
import com.agnitas.util.ZipUtilities;
import org.apache.commons.collections4.ListUtils;
import org.springframework.stereotype.Service;

@Service
public class ProgressStatisticsServiceImpl implements ProgressStatisticsService {

    private final MailingProgressStatsDao progressStatsDao;
    private final TargetGroupProgressBuilderFactory progressBuilderFactory;
    private final MailingService mailingService;
    private final MailingStatisticsService mailingStatisticsService;
    private final ProgressStatisticsMapper progressMapper;

    public ProgressStatisticsServiceImpl(
            MailingProgressStatsDao progressStatsDao,
            TargetGroupProgressBuilderFactory progressBuilderFactory,
            MailingService mailingService,
            MailingStatisticsService mailingStatisticsService,
            ProgressStatisticsMapper progressMapper
    ) {
        this.progressStatsDao = progressStatsDao;
        this.progressBuilderFactory = progressBuilderFactory;
        this.mailingService = mailingService;
        this.mailingStatisticsService = mailingStatisticsService;
        this.progressMapper = progressMapper;
    }

    @Override
    public byte[] exportMailingStatsToZip(
            int mailingId,
            DateRange dateRange,
            boolean hourScale,
            Set<Integer> targetIds,
            Admin admin
    ) throws Exception {
        SequencedMap<Integer, LightTarget> targets = getTargetGroupsMap(targetIds, admin);

        return ZipUtilities.zip(Map.of(
                "mailing_delivery_progress.csv", getDeliveryProgressCsv(mailingId, dateRange, admin),
                "openings_progress.csv", getOpeningsProgressCsv(mailingId, dateRange, hourScale, targets, admin),
                "clicks_progress.csv", getClicksProgressCsv(mailingId, dateRange, hourScale, targets, admin)
        ));
    }

    private byte[] getDeliveryProgressCsv(int mailingId, DateRange dateRange, Admin admin) throws Exception {
        List<MailingDeliveryProgressStats> rows = getDeliveriesProgress(mailingId, dateRange, admin);
        return CsvWriter.csv(ListUtils.union(
                List.of(getHeaderForDeliveryProgressCsv(admin.getLocale())),
                getDeliveryRowsForCsv(rows, admin))
        );
    }

    private static List<String> getHeaderForDeliveryProgressCsv(Locale locale) {
        return List.of(
                getLocaleString("DeliveryDate", locale),
                getLocaleString("NumberOfMailings", locale)
        );
    }

    private List<List<String>> getDeliveryRowsForCsv(List<MailingDeliveryProgressStats> rows, Admin admin) {
        return rows.stream()
                .map(data -> Arrays.asList(
                        data.sendDate().toInstant().atZone(admin.getZoneId()).format(admin.getDateTimeFormatter()),
                        String.valueOf(data.mailsCount())
                ))
                .toList();
    }

    private byte[] getOpeningsProgressCsv(
            int mailingId,
            DateRange dateRange,
            boolean hourScale,
            SequencedMap<Integer, LightTarget> targetGroups,
            Admin admin
    ) throws Exception {
        List<TargetGroupEventProgress> records = getOpeningsProgress(mailingId, dateRange, hourScale, targetGroups, admin);

        return CsvWriter.csv(ListUtils.union(
                List.of(getHeaderForOpeningsProgressCsv(admin.getLocale())),
                getRowsForCsv(records, hourScale, targetGroups, admin))
        );
    }

    private static List<String> getHeaderForOpeningsProgressCsv(Locale locale) {
        return List.of(
                getLocaleString("Date", locale),
                getLocaleString("OpeningsNet", locale),
                getLocaleString("OpeningsGross", locale),
                getLocaleString("statistic.openings.anonym", locale),
                getLocaleString("Target", locale),
                getLocaleString("recipient.deviceType", locale)
        );
    }

    private byte[] getClicksProgressCsv(
            int mailingId,
            DateRange dateRange,
            boolean hourScale,
            SequencedMap<Integer, LightTarget> targetGroups,
            Admin admin
    ) throws Exception {
        List<TargetGroupEventProgress> records = getClicksProgress(mailingId, dateRange, hourScale, targetGroups, admin);

        return CsvWriter.csv(ListUtils.union(
                List.of(getHeaderForClicksProgressCsv(admin.getLocale())),
                getRowsForCsv(records, hourScale, targetGroups, admin))
        );
    }

    private static List<String> getHeaderForClicksProgressCsv(Locale locale) {
        return List.of(
                getLocaleString("Date", locale),
                getLocaleString("statistic.clicker", locale),
                getLocaleString("statistic.Clicks", locale),
                getLocaleString("statistic.clicks.anonym", locale),
                getLocaleString("Target", locale),
                getLocaleString("recipient.deviceType", locale)
        );
    }

    private List<List<String>> getRowsForCsv(
            List<TargetGroupEventProgress> rows,
            boolean hourScale,
            SequencedMap<Integer, LightTarget> targetGroups,
            Admin admin
    ) {
        return rows.stream()
                .map(data -> Arrays.asList(
                        formatDate(data.instant(), hourScale, admin),
                        String.valueOf(data.unique()),
                        String.valueOf(data.total()),
                        String.valueOf(data.anonymous()),
                        targetGroups.get(data.targetGroupId()).getName(),
                        getDeviceClassName(data.deviceClassId(), admin.getLocale())
                ))
                .toList();
    }

    private static String formatDate(Instant date, boolean hourScale, Admin admin) {
        DateTimeFormatter formatter = hourScale
                ? admin.getDateTimeFormatter()
                : admin.getDateFormatter();

        return date.atZone(admin.getZoneId()).format(formatter);
    }

    private static String getDeviceClassName(Integer deviceClassId, Locale locale) {
        return deviceClassId.equals(MIXED_DEVICE_CLASS_ID)
                ? getLocaleString("report.device.mixed", locale)
                : getLocaleString(DeviceClass.fromId(deviceClassId).getMessageKey(), locale);
    }

    @Override
    public List<TargetGroupEventProgressDTO> getLinkStatistics(
            int linkId,
            DateRange dateRange,
            boolean hourScale,
            Set<Integer> targetIds,
            Admin admin
    ) {
        List<TargetGroupEventProgress> records = getLinkClicksProgress(
                linkId,
                dateRange,
                hourScale,
                getTargetGroupsMap(targetIds, admin),
                admin
        );

        return progressMapper.mapToEvents(records);
    }

    @Override
    public DateRange getDefaultLinkStatisticsDateRange() {
        return new DateRange(
                ZonedDateTime.now().with(firstDayOfMonth()),
                ZonedDateTime.now().with(lastDayOfMonth())
        );
    }

    @Override
    public MailingProgressStatisticsResponse getMailingStatistics(
            int mailingId,
            DateRange dateRange,
            boolean hourScale,
            Set<Integer> targetIds,
            Admin admin
    ) {
        SequencedMap<Integer, LightTarget> targets = getTargetGroupsMap(targetIds, admin);

        return progressMapper.mapToResponse(
                getDeliveriesProgress(mailingId, dateRange, admin),
                getOpeningsProgress(mailingId, dateRange, hourScale, targets, admin),
                getClicksProgress(mailingId, dateRange, hourScale, targets, admin)
        );
    }

    private List<MailingDeliveryProgressStats> getDeliveriesProgress(int mailingId, DateRange timestamp, Admin admin) {
        boolean isDateBasedMailing = MailingType.DATE_BASED.equals(mailingService.getMailingType(mailingId));
        return progressStatsDao.getDeliveries(mailingId, admin.getCompanyID(), timestamp, isDateBasedMailing);
    }

    private List<TargetGroupEventProgress> getOpeningsProgress(
            int mailingId,
            DateRange dateRange,
            boolean hourScale,
            SequencedMap<Integer, LightTarget> targets,
            Admin admin
    ) {
        return progressBuilderFactory.openings(
                mailingId,
                admin.getCompanyID(),
                dateRange,
                targets,
                hourScale
        ).build();
    }

    private List<TargetGroupEventProgress> getClicksProgress(
            int mailingId,
            DateRange dateRange,
            boolean hourScale,
            SequencedMap<Integer, LightTarget> targets,
            Admin admin
    ) {
        return progressBuilderFactory.clicks(
                mailingId,
                admin.getCompanyID(),
                dateRange,
                targets,
                hourScale
        ).build();
    }

    private List<TargetGroupEventProgress> getLinkClicksProgress(
            int mailingId,
            DateRange dateRange,
            boolean hourScale,
            SequencedMap<Integer, LightTarget> targets,
            Admin admin
    ) {
        return progressBuilderFactory.linkClicks(
                mailingId,
                admin.getCompanyID(),
                dateRange,
                targets,
                hourScale
        ).build();
    }

    private SequencedMap<Integer, LightTarget> getTargetGroupsMap(Set<Integer> targetIds, Admin admin) {
        return mailingStatisticsService.getTargetsMapWithAllSubscribersTarget(targetIds, admin);
    }

}
