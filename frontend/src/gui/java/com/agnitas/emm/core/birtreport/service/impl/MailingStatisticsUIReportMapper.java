/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.birtreport.service.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import com.agnitas.emm.core.birtreport.dto.MailingStatisticReportResultDTO;
import com.agnitas.emm.core.birtreport.dto.MailingStatisticReportResultDTO.GeneralInfo;
import com.agnitas.emm.core.birtreport.dto.MailingStatisticReportResultDTO.RecipientsByMailFormat;
import com.agnitas.emm.core.birtreport.dto.MailingStatisticReportResultDTO.TargetGroupReportData;
import com.agnitas.emm.core.birtreport.dto.MailingStatisticReportResultDTO.TargetGroupReportData.BounceReasonStat;
import com.agnitas.emm.core.birtreport.dto.MailingStatisticReportResultDTO.TargetGroupReportData.Bounces;
import com.agnitas.emm.core.birtreport.dto.MailingStatisticReportResultDTO.TargetGroupReportData.DeviceStat;
import com.agnitas.emm.core.birtreport.dto.MailingStatisticReportResultDTO.TargetGroupReportData.DomainStat;
import com.agnitas.emm.core.birtreport.dto.MailingStatisticReportResultDTO.TargetGroupReportData.DomainStats;
import com.agnitas.emm.core.birtreport.dto.MailingStatisticReportResultDTO.TargetGroupReportData.LinkCategoryStat;
import com.agnitas.emm.core.birtreport.dto.MailingStatisticReportResultDTO.TargetGroupReportData.LinkClickShare;
import com.agnitas.emm.core.birtreport.dto.MailingStatisticReportResultDTO.TargetGroupReportData.LinkClickStats;
import com.agnitas.emm.core.birtreport.dto.MailingStatisticReportResultDTO.TargetGroupReportData.LinkClicksByCategory;
import com.agnitas.emm.core.birtreport.dto.MailingStatisticReportResultDTO.TargetGroupReportData.LinkStat;
import com.agnitas.emm.core.birtreport.dto.MailingStatisticReportResultDTO.TargetGroupReportData.Summary;
import com.agnitas.emm.core.birtreport.model.MailingStatisticsReportData;
import com.agnitas.emm.core.birtstatistics.service.MailingStatisticsService;
import com.agnitas.emm.core.mobile.bean.DeviceClass;
import org.springframework.stereotype.Component;

@Component
public class MailingStatisticsUIReportMapper {

    public MailingStatisticReportResultDTO toResultDto(
            String thumbnailUrl,
            MailingStatisticsReportData data,
            Locale locale
    ) {
        List<TargetGroupReportData> targetGroupsData = data.targetGroupsData() != null
                ? data.targetGroupsData().stream().map(this::toTargetGroupReportData).toList()
                : null;

        return new MailingStatisticReportResultDTO(
                thumbnailUrl,
                data.generalInfo() != null ? toGeneralInfo(data.generalInfo(), locale) : null,
                data.recipientsByMailingType() != null ? toRecipientsByMailFormat(data.recipientsByMailingType()) : null,
                targetGroupsData
        );
    }

    private GeneralInfo toGeneralInfo(MailingStatisticsReportData.GeneralInfo src, Locale locale) {
        return new GeneralInfo(
                src.id(),
                src.name(),
                src.description(),
                src.archive(),
                src.mailType(),
                src.avgMailSizeKb(),
                src.subject(),
                src.mailinglist(),
                src.targetGroups(),
                src.recipientsCount(),
                src.anonymousRecipientsCount(),
                formatDate(src.sendingStarted(), locale),
                formatDate(src.sendingFinished(), locale),
                src.senderName(),
                src.senderEmail(),
                src.replyName(),
                src.replyEmail()
        );
    }

    private RecipientsByMailFormat toRecipientsByMailFormat(MailingStatisticsService.RecipientsByMailFormat source) {
        return new RecipientsByMailFormat(source.html(), source.text(), source.offlineHtml());
    }

    private TargetGroupReportData toTargetGroupReportData(MailingStatisticsReportData.TargetGroupReportData src) {
        return new TargetGroupReportData(
                src.targetId(),
                src.targetName(),
                toSummary(src.summary()),
                src.openersByDeviceType() != null ? toDeviceTypeStat(src.openersByDeviceType()) : null,
                src.clickersByDeviceType() != null ? toDeviceTypeStat(src.clickersByDeviceType()) : null,
                src.openersByDeviceName() != null ? toDeviceStatsByClass(src.openersByDeviceName()) : null,
                src.clickersByDeviceName() != null ? toDeviceStatsByClass(src.clickersByDeviceName()) : null,
                src.linkClicks() != null ? toLinkClicksByCategory(src.linkClicks()) : null,
                src.bounces() != null ? toBounces(src.bounces()) : null,
                src.domainStats() != null ? toDomainStats(src.domainStats()) : null
        );
    }

    private Summary toSummary(MailingStatisticsReportData.TargetGroupReportData.Summary src) {
        return new Summary(
                src.sentMails(),
                src.emailsAccepted(),
                src.openersMeasured(),
                src.openersProxy(),
                src.openersInvisible(),
                src.openersTotal(),
                src.anonymousOpenings(),
                src.clickers(),
                src.anonymousClicks(),
                src.optOuts(),
                src.softBounces(),
                src.hardBounces(),
                src.revenue()
        );
    }

    private TargetGroupReportData.DeviceTypeStat toDeviceTypeStat(
            MailingStatisticsReportData.TargetGroupReportData.DeviceTypeStat src
    ) {
        return new TargetGroupReportData.DeviceTypeStat(
                src.pc(),
                src.tablet(),
                src.mobile(),
                src.smartTv(),
                src.multi()
        );
    }

    private Map<DeviceClass, List<DeviceStat>> toDeviceStatsByClass(
            Map<DeviceClass, List<MailingStatisticsReportData.TargetGroupReportData.DeviceStat>> src
    ) {
        return src.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().stream().map(this::toDeviceStat).toList()
                ));
    }

    private DeviceStat toDeviceStat(MailingStatisticsReportData.TargetGroupReportData.DeviceStat src) {
        return new DeviceStat(src.deviceName(), src.value(), src.other());
    }

    private LinkClicksByCategory toLinkClicksByCategory(
            MailingStatisticsReportData.TargetGroupReportData.LinkClicksByCategory src
    ) {
        return new LinkClicksByCategory(
                toLinkCategoryStat(src.leadingLinks()),
                toLinkCategoryStat(src.administrativeLinks())
        );
    }

    private LinkCategoryStat toLinkCategoryStat(MailingStatisticsReportData.TargetGroupReportData.LinkCategoryStat src) {
        return new LinkCategoryStat(
                toLinkClickStats(src.summary()),
                src.links().stream().map(this::toLinkStat).toList()
        );
    }

    private LinkStat toLinkStat(MailingStatisticsReportData.TargetGroupReportData.LinkStat src) {
        return new LinkStat(src.linkId(), src.url(), toLinkClickStats(src.stats()));
    }

    private LinkClickStats toLinkClickStats(MailingStatisticsReportData.TargetGroupReportData.LinkClickStats src) {
        return new LinkClickStats(
                toLinkClickShare(src.total()),
                toLinkClickShare(src.mobile()),
                toLinkClickShare(src.anonymous())
        );
    }

    private LinkClickShare toLinkClickShare(MailingStatisticsReportData.TargetGroupReportData.LinkClickShare src) {
        return new LinkClickShare(src.clicks(), src.clickers());
    }

    private Bounces toBounces(MailingStatisticsReportData.TargetGroupReportData.Bounces src) {
        return new Bounces(
                src.softBounces() != null ? toBounceReasonStats(src.softBounces()) : null,
                src.hardBounces() != null ? toBounceReasonStats(src.hardBounces()) : null,
                src.undelivered()
        );
    }

    private List<BounceReasonStat> toBounceReasonStats(List<MailingStatisticsReportData.TargetGroupReportData.BounceReasonStat> src) {
        return src.stream()
                .map(s -> new BounceReasonStat(s.reason(), s.value()))
                .toList();
    }

    private DomainStats toDomainStats(MailingStatisticsReportData.TargetGroupReportData.DomainStats src) {
        return new DomainStats(
                toDomainStatList(src.emailsSent()),
                toDomainStatList(src.hardBounces()),
                toDomainStatList(src.softBounces()),
                toDomainStatList(src.openers()),
                toDomainStatList(src.clickers())
        );
    }

    private List<DomainStat> toDomainStatList(List<MailingStatisticsReportData.TargetGroupReportData.DomainStat> src) {
        return src.stream()
                .map(s -> new DomainStat(s.domain(), s.value()))
                .toList();
    }

    private String formatDate(Date date, Locale locale) {
        if (date == null) {
            return null;
        }

        return new SimpleDateFormat("MMM dd yyyy hh:mm:ss a", locale).format(date);
    }

}
