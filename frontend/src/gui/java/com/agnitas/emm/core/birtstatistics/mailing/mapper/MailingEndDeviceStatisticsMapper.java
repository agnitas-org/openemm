/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtstatistics.mailing.mapper;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.agnitas.emm.core.birtstatistics.dto.EndDeviceStatisticsResponse;
import com.agnitas.emm.core.birtstatistics.dto.EndDeviceStatisticsResponse.DeviceStat;
import com.agnitas.emm.core.birtstatistics.model.MailingEndDeviceStatEntry;
import com.agnitas.emm.core.mobile.bean.DeviceClass;
import com.agnitas.reporting.birt.external.beans.StatisticMetric;
import com.agnitas.reporting.birt.external.dataset.CommonKeys;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

@Component
public class MailingEndDeviceStatisticsMapper {

    private static final DeviceClass UNKNOWN_DEVICE_CLASS = DeviceClass.UNKNOWN_DESKTOP;

    private enum MetricType {
        OPENINGS, CLICKS
    }

    public EndDeviceStatisticsResponse mapToResponse(List<MailingEndDeviceStatEntry> entries) {
        Map<DeviceClass, Map<String, List<MailingEndDeviceStatEntry>>> grouped =
                entries.stream()
                        .collect(Collectors.groupingBy(
                                e -> DeviceClass.fromIdWithDefault(e.deviceClassId(), UNKNOWN_DEVICE_CLASS),
                                Collectors.groupingBy(MailingEndDeviceStatEntry::deviceName)
                        ));

        return new EndDeviceStatisticsResponse(
                buildMetric(grouped, MetricType.OPENINGS),
                buildMetric(grouped, MetricType.CLICKS)
        );
    }

    private EndDeviceStatisticsResponse.DeviceMetric buildMetric(
            Map<DeviceClass, Map<String, List<MailingEndDeviceStatEntry>>> grouped,
            MetricType metricType
    ) {
        return new EndDeviceStatisticsResponse.DeviceMetric(
                buildDeviceStats(grouped.get(DeviceClass.DESKTOP), metricType),
                buildDeviceStats(grouped.get(DeviceClass.MOBILE), metricType),
                buildDeviceStats(grouped.get(DeviceClass.TABLET), metricType),
                buildDeviceStats(grouped.get(DeviceClass.SMARTTV), metricType),
                buildDeviceStats(grouped.get(UNKNOWN_DEVICE_CLASS), metricType)
        );
    }

    private List<DeviceStat> buildDeviceStats(
            Map<String, List<MailingEndDeviceStatEntry>> devices,
            MetricType metricType
    ) {
        if (MapUtils.isEmpty(devices)) {
            return Collections.emptyList();
        }

        return devices.entrySet()
                .stream()
                .map(e -> {
                    StatisticMetric allRecipients = null;
                    Map<Integer, StatisticMetric> targetGroups = new HashMap<>();

                    for (MailingEndDeviceStatEntry row : e.getValue()) {
                        StatisticMetric metric = switch (metricType) {
                            case OPENINGS -> row.openings();
                            case CLICKS -> row.clicks();
                        };

                        if (row.targetGroupId() == CommonKeys.ALL_SUBSCRIBERS_TARGETGROUPID) {
                            allRecipients = metric;
                        } else {
                            targetGroups.put(row.targetGroupId(), metric);
                        }
                    }

                    return new DeviceStat(e.getKey(), allRecipients, targetGroups);
                })
                .filter(s -> s.allRecipients().value() > 0)
                .sorted(Comparator.<DeviceStat>comparingLong(s -> s.allRecipients().value()).reversed())
                .toList();
    }
}
