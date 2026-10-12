/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.birtreport.model;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.agnitas.emm.core.birtstatistics.dto.StatisticMetric;
import com.agnitas.emm.core.birtstatistics.service.MailingStatisticsService;
import com.agnitas.emm.core.mobile.bean.DeviceClass;
import com.agnitas.util.importvalues.MailType;

public record MailingStatisticsReportData(
        GeneralInfo generalInfo,
        MailingStatisticsService.RecipientsByMailFormat recipientsByMailingType,
        List<TargetGroupReportData> targetGroupsData
) {

    public record GeneralInfo(
            int id,
            String name,
            String description,
            String archive,
            MailType mailType,
            Long avgMailSizeKb,
            String subject,
            String mailinglist,
            List<String> targetGroups,
            Long recipientsCount,
            Long anonymousRecipientsCount,
            Date sendingStarted,
            Date sendingFinished,
            String senderName,
            String senderEmail,
            String replyName,
            String replyEmail
    ) {
    }

    public record TargetGroupReportData(
            int targetId,
            String targetName,
            Summary summary,
            DeviceTypeStat openersByDeviceType,
            DeviceTypeStat clickersByDeviceType,
            Map<DeviceClass, List<DeviceStat>> openersByDeviceName,
            Map<DeviceClass, List<DeviceStat>> clickersByDeviceName,
            LinkClicksByCategory linkClicks,
            Bounces bounces,
            DomainStats domainStats
    ) {

        public record Summary(
                StatisticMetric sentMails,
                StatisticMetric emailsAccepted,
                StatisticMetric openersMeasured,
                StatisticMetric openersProxy,
                StatisticMetric openersInvisible,
                StatisticMetric openersTotal,
                Long anonymousOpenings,
                StatisticMetric clickers,
                StatisticMetric clicks,
                Long anonymousClicks,
                StatisticMetric optOuts,
                StatisticMetric softBounces,
                StatisticMetric hardBounces,
                Double revenue
        ) {
        }

        public record DeviceTypeStat(
                StatisticMetric pc,
                StatisticMetric tablet,
                StatisticMetric mobile,
                StatisticMetric smartTv,
                StatisticMetric multi
        ) {
        }

        public record DeviceStat(String deviceName, StatisticMetric value, boolean other) {
        }

        public record LinkClickShare(StatisticMetric clicks, StatisticMetric clickers) {
        }

        public record LinkClickStats(LinkClickShare total, LinkClickShare mobile, LinkClickShare anonymous) {
        }

        public record LinkStat(int linkId, String url, LinkClickStats stats) {
        }

        public record LinkCategoryStat(LinkClickStats summary, List<LinkStat> links) {
        }

        public record LinkClicksByCategory(LinkCategoryStat leadingLinks, LinkCategoryStat administrativeLinks) {
        }

        public record BounceReasonStat(String reason, StatisticMetric value) {
        }

        public record Bounces(
                List<BounceReasonStat> softBounces,
                List<BounceReasonStat> hardBounces,
                StatisticMetric undelivered
        ) {
        }

        public record DomainStat(String domain, StatisticMetric value) {
        }

        public record DomainStats(
                List<DomainStat> emailsSent,
                List<DomainStat> hardBounces,
                List<DomainStat> softBounces,
                List<DomainStat> openers,
                List<DomainStat> clickers
        ) {
        }
    }
}
