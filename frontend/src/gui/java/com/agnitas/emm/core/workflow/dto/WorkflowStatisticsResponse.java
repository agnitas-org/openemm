/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.workflow.dto;

import java.util.List;
import java.util.Map;

import com.agnitas.emm.core.birtstatistics.dto.StatisticMetric;

public record WorkflowStatisticsResponse(
        Metrics total,
        List<MailingData> mailings,
        Map<DeviceType, StatisticMetric> openersByDevice,
        Map<DeviceType, StatisticMetric> clickersByDevice,
        OptimizationInfo optimizationInfo
) {

    public record MailingData(int mailingId, String name, Metrics data) {

    }

    public record Metrics(
            StatisticMetric totalOpeners,
            StatisticMetric measuredOpeners,
            StatisticMetric invisibleOpeners,
            long anonymousOpenersCount,
            StatisticMetric sentEmails,
            StatisticMetric acceptedEmails,
            StatisticMetric optOuts,
            StatisticMetric hardBounces,
            StatisticMetric clickers,
            long anonymousClickersCount,
            StatisticMetric softBounces,
            Double revenue,
            long avgMailingSize,
            Map<DeviceType, StatisticMetric> openersByDeviceType,
            Map<DeviceType, StatisticMetric> clickersByDeviceType,
            Long openingsGross,
            Long clicksGross,
            Double activityRate
    ) {

    }

    public record OptimizationInfo(List<GroupInfo> groups) {

        public record GroupInfo(
                int mailingId,
                String label,
                String subject,
                String sendDate,
                boolean finalMailing,
                boolean winner
        ) {
        }
    }

    public enum DeviceType {
        TOTAL, PC, TABLET, MOBILE, SMART_TV, MULTIPLE
    }
}
