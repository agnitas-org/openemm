/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtstatistics.mailing.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.SequencedMap;
import java.util.stream.Stream;

import com.agnitas.emm.core.birtstatistics.model.CustomerEventStats;
import com.agnitas.emm.core.birtstatistics.model.TargetGroupEventProgress;
import com.agnitas.emm.core.birtstatistics.model.MailingProgressStatisticFilter;
import com.agnitas.emm.core.commons.dto.DateRange;
import com.agnitas.emm.core.mobile.bean.DeviceClass;
import com.agnitas.reporting.birt.external.beans.LightTarget;
import com.agnitas.util.DateUtilities;
import com.agnitas.util.DbUtilities;

public abstract class TargetGroupEventProgressBuilder {

    public static final Integer MIXED_DEVICE_CLASS_ID = 0;
    protected static final Comparator<TargetGroupEventProgress> COMPARATOR = Comparator
            .comparing(TargetGroupEventProgress::instant)
            .thenComparing(TargetGroupEventProgress::deviceClassId)
            .thenComparing(TargetGroupEventProgress::targetGroupId);

    protected final int entityId;
    protected final int companyId;
    protected final DateRange dateRange;
    protected SequencedMap<Integer, LightTarget> targets;
    protected final boolean hourScale;

    protected TargetGroupEventProgressBuilder(
            int entityId,
            int companyId,
            DateRange dateRange,
            SequencedMap<Integer, LightTarget> targets,
            boolean hourScale
    ) {
        this.entityId = entityId;
        this.companyId = companyId;
        this.dateRange = dateRange;
        this.targets = targets;
        this.hourScale = hourScale;
    }

    public final List<TargetGroupEventProgress> build() {
        return prepareTimeBuckets().stream()
                .flatMap(dr ->
                        targets.keySet().stream()
                                .flatMap(targetId ->
                                        Stream.concat(
                                                Stream.of(getMixedDeviceClassRecord(dr, targetId)),
                                                getKnownDeviceClassesRecords(dr, targetId).stream()
                                        )
                                )
                )
                .sorted(COMPARATOR)
                .toList();
    }

    protected abstract CustomerEventStats getMultiDeviceCounts(MailingProgressStatisticFilter filter);

    protected abstract CustomerEventStats getSingleDeviceCounts(MailingProgressStatisticFilter filter);

    private TargetGroupEventProgress mapToRecord(
            CustomerEventStats counts, Integer targetGroupId, Integer deviceClassId, Date timestamp
    ) {
        return new TargetGroupEventProgress(
                timestamp.toInstant(),
                counts.uniqueCount(),
                counts.totalCount() + counts.anonymousCount(),
                counts.anonymousCount(),
                targetGroupId,
                deviceClassId
        );
    }

    private TargetGroupEventProgress getMixedDeviceClassRecord(DateRange dateRange, Integer targetGroupId) {
        MailingProgressStatisticFilter filter = buildFilter(targetGroupId, null, dateRange);

        CustomerEventStats counts = getMultiDeviceCounts(filter);
        return mapToRecord(counts, targetGroupId, MIXED_DEVICE_CLASS_ID, dateRange.getFrom());
    }

    private List<TargetGroupEventProgress> getKnownDeviceClassesRecords(DateRange dateRange, Integer targetGroupId) {
        List<TargetGroupEventProgress> result = new ArrayList<>();

        for (DeviceClass deviceClass : DeviceClass.getOnlyKnownDeviceClasses()) {
            MailingProgressStatisticFilter filter = buildFilter(targetGroupId, deviceClass, dateRange);

            CustomerEventStats counts = getSingleDeviceCounts(filter);
            result.add(mapToRecord(counts, targetGroupId, deviceClass.getId(), dateRange.getFrom()));
        }

        return result;
    }

    private List<DateRange> prepareTimeBuckets() {
        List<DateRange> result = new ArrayList<>();

        Date from = dateRange.getFrom();
        while (from.before(dateRange.getTo())) {
            Date to = hourScale
                    ? DateUtilities.addMinutesToDate(from, 60)
                    : DateUtilities.addDaysToDate(from, 1);

            result.add(new DateRange(from, to));
            from = to;
        }

        return result;
    }

    private MailingProgressStatisticFilter buildFilter(
            Integer targetGroupId, DeviceClass deviceClass, DateRange timestampRange
    ) {
        return new MailingProgressStatisticFilter.Builder()
                .entityId(entityId)
                .companyId(companyId)
                .targetSql(getTargetSQL(targetGroupId))
                .dateRange(timestampRange)
                .deviceClass(deviceClass)
                .build();
    }

    private String getTargetSQL(Integer targetGroupId) {
        String targetSQL = targets.get(targetGroupId).getTargetSQL();
        return DbUtilities.isTautologicWhereClause(targetSQL) ? "" : targetSQL;
    }

}
