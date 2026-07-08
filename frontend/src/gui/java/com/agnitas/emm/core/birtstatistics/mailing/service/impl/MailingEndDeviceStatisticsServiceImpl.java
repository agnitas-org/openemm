/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtstatistics.mailing.service.impl;

import static com.agnitas.messages.I18nString.getLocaleString;
import static com.agnitas.util.AgnUtils.calculateRate;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.birtstatistics.dao.EndDeviceStatisticsDao;
import com.agnitas.emm.core.birtstatistics.dto.EndDeviceStatisticsResponse;
import com.agnitas.emm.core.birtstatistics.mailing.mapper.MailingEndDeviceStatisticsMapper;
import com.agnitas.emm.core.birtstatistics.mailing.service.MailingEndDeviceStatisticsService;
import com.agnitas.emm.core.birtstatistics.model.MailingEndDeviceStatEntry;
import com.agnitas.emm.core.birtstatistics.service.MailingStatisticsService;
import com.agnitas.emm.core.commons.dto.DateRange;
import com.agnitas.emm.core.maildrop.MaildropStatus;
import com.agnitas.reporting.birt.external.beans.LightTarget;
import com.agnitas.reporting.birt.external.beans.StatisticMetric;
import com.agnitas.reporting.birt.external.dataset.CommonKeys;
import com.agnitas.util.CsvWriter;
import com.agnitas.util.DbUtilities;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class MailingEndDeviceStatisticsServiceImpl implements MailingEndDeviceStatisticsService {

    private static final int MAX_DEVICES_COUNT = 10;

    private final EndDeviceStatisticsDao endDeviceStatisticsDao;
    private final MailingStatisticsService mailingStatisticsService;
    private final MailingEndDeviceStatisticsMapper endDeviceStatisticsMapper;

    public MailingEndDeviceStatisticsServiceImpl(
            EndDeviceStatisticsDao endDeviceStatisticsDao,
            MailingStatisticsService mailingStatisticsService,
            MailingEndDeviceStatisticsMapper endDeviceStatisticsMapper
    ) {
        this.endDeviceStatisticsDao = endDeviceStatisticsDao;
        this.mailingStatisticsService = mailingStatisticsService;
        this.endDeviceStatisticsMapper = endDeviceStatisticsMapper;
    }

    @Override
    public byte[] getCsvData(int mailingId, Set<Integer> targetGroupIds, Admin admin) throws Exception {
        List<MailingEndDeviceStatEntry> entries = getStatsEntries(mailingId, targetGroupIds, admin);
        return CsvWriter.csv(ListUtils.union(
                List.of(getHeaderForCsv(admin.getLocale())),
                getRowsForCsv(entries)
        ));
    }

    private List<String> getHeaderForCsv(Locale locale) {
        return List.of(
                getLocaleString("statistic.device_name", locale),
                getLocaleString("statistic.openings", locale),
                getLocaleString("statistic.Clicks", locale)
        );
    }

    private List<List<String>> getRowsForCsv(List<MailingEndDeviceStatEntry> rows) {
        // This is most likely a bug, but in GWUA-6955 asked to make it the same as in the old statistics
        rows = ListUtils.union(filterByTargetGroup(rows, CommonKeys.ALL_SUBSCRIBERS_TARGETGROUPID), rows);

        return rows.stream()
                .map(row -> Arrays.asList(
                        row.deviceName(),
                        String.valueOf(row.openings().value()),
                        String.valueOf(row.clicks().value())
                ))
                .toList();
    }

    private List<MailingEndDeviceStatEntry> filterByTargetGroup(List<MailingEndDeviceStatEntry> rows, int targetGroupId) {
        return rows.stream()
                .filter(r -> r.targetGroupId() == targetGroupId)
                .toList();
    }

    @Override
    public EndDeviceStatisticsResponse getStatistics(int mailingId, Set<Integer> targetGroupIds, Admin admin) {
        List<MailingEndDeviceStatEntry> entries = getStatsEntries(mailingId, targetGroupIds, admin);
        return endDeviceStatisticsMapper.mapToResponse(entries);
    }

    private List<MailingEndDeviceStatEntry> getStatsEntries(int mailingId, Set<Integer> targetGroupIds, Admin admin) {
        return mailingStatisticsService.getTargetsWithAllSubscribersTarget(targetGroupIds, admin)
                .stream()
                .flatMap(tg -> getStatsEntriesByTargetGroup(mailingId, tg, admin).stream())
                .toList();
    }

    private List<MailingEndDeviceStatEntry> getStatsEntriesByTargetGroup(
            int mailingId,
            LightTarget targetGroup,
            Admin admin
    ) {
        String targetSql = getTargetSql(targetGroup);

        int sentCount = getSentCount(mailingId, admin, targetSql);
        String unknownDeviceName = getLocaleString("statistic.unknown_devices", admin.getLocale());

        return endDeviceStatisticsDao.getData(mailingId, admin.getCompanyID(), targetSql, MAX_DEVICES_COUNT)
                .stream()
                .map(d ->
                        new MailingEndDeviceStatEntry(
                                targetGroup.getId(),
                                d.deviceClassId(),
                                StringUtils.defaultIfBlank(d.deviceName(), unknownDeviceName),
                                new StatisticMetric(d.openings(), calculateRate(d.openings(), sentCount)),
                                new StatisticMetric(d.clicks(), calculateRate(d.clicks(), sentCount))
                        )
                )
                .toList();
    }

    private String getTargetSql(LightTarget targetGroup) {
        return DbUtilities.isTautologicWhereClause(targetGroup.getTargetSQL())
                ? ""
                : targetGroup.getTargetSQL();
    }

    private int getSentCount(int mailingId, Admin admin, String targetSql) {
        List<MaildropStatus> statuses = MaildropStatus.allExcept(
                MaildropStatus.TEST,
                MaildropStatus.ADMIN,
                MaildropStatus.PREDELIVERY
        );

        return mailingStatisticsService.getSentCount(
                mailingId,
                admin.getCompanyID(),
                Set.copyOf(statuses),
                targetSql,
                new DateRange()
        );
    }

}
