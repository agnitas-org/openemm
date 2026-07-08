/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtstatistics.mailing.mapper;

import static com.agnitas.emm.core.birtstatistics.mailing.service.TargetGroupEventProgressBuilder.MIXED_DEVICE_CLASS_ID;
import static com.agnitas.reporting.birt.external.dataset.CommonKeys.ALL_SUBSCRIBERS_TARGETGROUPID;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.agnitas.emm.core.birtstatistics.dto.MailingProgressStatisticsResponse;
import com.agnitas.emm.core.birtstatistics.dto.MailingProgressStatisticsResponse.DeliveryDto;
import com.agnitas.emm.core.birtstatistics.dto.TargetGroupEventProgressDTO;
import com.agnitas.emm.core.birtstatistics.dto.TargetGroupEventProgressDTO.DeviceClassEventSeries;
import com.agnitas.emm.core.birtstatistics.dto.TargetGroupEventProgressDTO.EventStatsPoint;
import com.agnitas.emm.core.birtstatistics.model.TargetGroupEventProgress;
import com.agnitas.emm.core.birtstatistics.model.MailingDeliveryProgressStats;
import org.springframework.stereotype.Component;

@Component
public class ProgressStatisticsMapper {

    public MailingProgressStatisticsResponse mapToResponse(
            List<MailingDeliveryProgressStats> deliveries,
            List<TargetGroupEventProgress> openings,
            List<TargetGroupEventProgress> clicks
    ) {
        return new MailingProgressStatisticsResponse(
                mapToDeliveryDTOs(deliveries),
                mapToEvents(openings),
                mapToEvents(clicks)
        );
    }

    public List<TargetGroupEventProgressDTO> mapToEvents(List<TargetGroupEventProgress> records) {
        return groupBy(records, TargetGroupEventProgress::targetGroupId)
                .entrySet()
                .stream()
                .map(e -> mapToTargetGroupEvent(e.getKey(), e.getValue()))
                .toList();
    }

    private List<DeliveryDto> mapToDeliveryDTOs(List<MailingDeliveryProgressStats> deliveries) {
        return deliveries.stream()
                .map(e -> new DeliveryDto(
                        e.mailsCount(),
                        e.sendDate().toInstant()
                ))
                .toList();
    }

    private TargetGroupEventProgressDTO mapToTargetGroupEvent(
            Integer targetGroupId,
            List<TargetGroupEventProgress> records
    ) {
        List<DeviceClassEventSeries> series = groupBy(records, TargetGroupEventProgress::deviceClassId)
                .entrySet()
                .stream()
                .map(this::mapToSeries)
                .toList();

        return new TargetGroupEventProgressDTO(
                targetGroupId == ALL_SUBSCRIBERS_TARGETGROUPID ? null : targetGroupId,
                series
        );
    }

    private DeviceClassEventSeries mapToSeries(Map.Entry<Integer, List<TargetGroupEventProgress>> entry) {
        Integer deviceClassId = entry.getKey().equals(MIXED_DEVICE_CLASS_ID)
                ? null
                : entry.getKey();

        List<EventStatsPoint> points = entry.getValue().stream()
                .map(r ->
                        new EventStatsPoint(
                                r.instant(),
                                r.unique(),
                                r.total(),
                                r.anonymous()
                        )
                )
                .toList();

        return new DeviceClassEventSeries(deviceClassId, points);
    }

    private <T> Map<T, List<TargetGroupEventProgress>> groupBy(
            List<TargetGroupEventProgress> records,
            Function<TargetGroupEventProgress, T> classifier
    ) {
        return records.stream()
                .collect(Collectors.groupingBy(classifier));
    }

}
