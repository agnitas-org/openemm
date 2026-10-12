/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.birtreport.service.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.agnitas.beans.Mailing;
import com.agnitas.beans.MediatypeEmail;
import com.agnitas.beans.TrackableLink;
import com.agnitas.dao.MailingStatisticsDao;
import com.agnitas.emm.core.archive.service.CampaignService;
import com.agnitas.emm.core.birtreport.dto.MailingReportRequest;
import com.agnitas.emm.core.birtreport.model.MailingStatisticsReportData;
import com.agnitas.emm.core.birtreport.model.MailingStatisticsReportData.GeneralInfo;
import com.agnitas.emm.core.birtreport.model.MailingStatisticsReportData.TargetGroupReportData;
import com.agnitas.emm.core.birtreport.model.MailingStatisticsReportData.TargetGroupReportData.BounceReasonStat;
import com.agnitas.emm.core.birtreport.model.MailingStatisticsReportData.TargetGroupReportData.Bounces;
import com.agnitas.emm.core.birtreport.model.MailingStatisticsReportData.TargetGroupReportData.DeviceStat;
import com.agnitas.emm.core.birtreport.model.MailingStatisticsReportData.TargetGroupReportData.DeviceTypeStat;
import com.agnitas.emm.core.birtreport.model.MailingStatisticsReportData.TargetGroupReportData.DomainStat;
import com.agnitas.emm.core.birtreport.model.MailingStatisticsReportData.TargetGroupReportData.DomainStats;
import com.agnitas.emm.core.birtreport.model.MailingStatisticsReportData.TargetGroupReportData.LinkCategoryStat;
import com.agnitas.emm.core.birtreport.model.MailingStatisticsReportData.TargetGroupReportData.LinkClickShare;
import com.agnitas.emm.core.birtreport.model.MailingStatisticsReportData.TargetGroupReportData.LinkClickStats;
import com.agnitas.emm.core.birtreport.model.MailingStatisticsReportData.TargetGroupReportData.LinkClicksByCategory;
import com.agnitas.emm.core.birtreport.model.MailingStatisticsReportData.TargetGroupReportData.LinkStat;
import com.agnitas.emm.core.birtreport.model.MailingStatisticsReportData.TargetGroupReportData.Summary;
import com.agnitas.emm.core.birtreport.service.MailingStatisticsReportService;
import com.agnitas.emm.core.birtstatistics.dto.StatisticMetric;
import com.agnitas.emm.core.birtstatistics.mailing.MailingSummaryStatsService;
import com.agnitas.emm.core.birtstatistics.mailing.MailingSummaryStatsService.DispatchStats;
import com.agnitas.emm.core.birtstatistics.service.MailingBounceStatsService;
import com.agnitas.emm.core.birtstatistics.service.MailingBounceStatsService.BounceStats;
import com.agnitas.emm.core.birtstatistics.service.MailingStatisticsService;
import com.agnitas.emm.core.birtstatistics.service.MailingTopDomainsStatsService;
import com.agnitas.emm.core.birtstatistics.service.MailingTopDomainsStatsService.TopDomainStats;
import com.agnitas.emm.core.birtstatistics.service.MailingTopDomainsStatsService.TopDomainsStatsQuery;
import com.agnitas.emm.core.commons.dto.DateRange;
import com.agnitas.emm.core.maildrop.MaildropStatus;
import com.agnitas.emm.core.mailing.service.MailingService;
import com.agnitas.emm.core.mailinglist.service.MailinglistService;
import com.agnitas.emm.core.mobile.bean.DeviceClass;
import com.agnitas.emm.core.target.service.TargetService;
import com.agnitas.emm.core.trackablelinks.service.TrackableLinkClickStatsService;
import com.agnitas.emm.core.trackablelinks.service.TrackableLinkClickStatsService.ClickCounts;
import com.agnitas.emm.core.trackablelinks.service.TrackableLinkClickStatsService.ShareType;
import com.agnitas.emm.core.trackablelinks.service.TrackableLinkClickStatsService.TargetClickStats;
import com.agnitas.emm.core.trackablelinks.service.TrackableLinkService;
import com.agnitas.reporting.birt.external.beans.LightTarget;
import com.agnitas.reporting.birt.external.utils.BirtReportUtils;
import com.agnitas.reporting.birt.external.utils.BirtReportUtils.BirtReportFigure;
import com.agnitas.util.importvalues.MailType;
import org.springframework.stereotype.Service;

@Service
public class MailingStatisticsReportServiceImpl implements MailingStatisticsReportService {

    private static final Set<BirtReportFigure> GENERAL_INFO_FIGURES = EnumSet.of(
            BirtReportFigure.MAILING_NAME, BirtReportFigure.MAILING_ID, BirtReportFigure.DESCRIPTION,
            BirtReportFigure.ARCHIVE, BirtReportFigure.MAIL_FORMAT, BirtReportFigure.AVERAGE_SIZE,
            BirtReportFigure.SUBJECT, BirtReportFigure.MAILING_LIST, BirtReportFigure.TARGET_GROUPS,
            BirtReportFigure.NR_OF_RECIPIENTS_WITH_TRACK_VETO, BirtReportFigure.SENDING_STARTED,
            BirtReportFigure.SENDING_FINISHED, BirtReportFigure.SENDER_INFO, BirtReportFigure.REPLY_TO_INFO
    );

    private static final Set<BirtReportFigure> MAILING_TYPE_FIGURES = EnumSet.of(
            BirtReportFigure.HTML, BirtReportFigure.TEXT, BirtReportFigure.OFFLINE_HTML
    );

    private static final Set<BirtReportFigure> DISPATCH_STATS_FIGURES = EnumSet.of(
            BirtReportFigure.AVERAGE_SIZE, BirtReportFigure.NR_OF_RECIPIENTS_WITH_TRACK_VETO,
            BirtReportFigure.SENDING_STARTED, BirtReportFigure.SENDING_FINISHED
    );

    private static final Set<BirtReportFigure> TARGET_GROUP_FIGURES = EnumSet.of(
            BirtReportFigure.SENT_MAILS, BirtReportFigure.EMAILS_ACCEPTED,
            BirtReportFigure.OPENERS_MEASURED, BirtReportFigure.OPENERS_INVISIBLE,
            BirtReportFigure.OPENERS_TOTAL, BirtReportFigure.OPENINGS_ANONYMOUS,
            BirtReportFigure.CLICKERS_TOTAL, BirtReportFigure.CLICKS_ANONYMOUS,
            BirtReportFigure.SIGNED_OFF, BirtReportFigure.HARDBOUNCES,
            BirtReportFigure.SOFTBOUNCES, BirtReportFigure.REVENUE,
            BirtReportFigure.OPENERS_AFTER_DEVICE, BirtReportFigure.CLICKERS_AFTER_DEVICE,
            BirtReportFigure.OPENERS_DEVICES, BirtReportFigure.CLICKER_DEVICES,
            BirtReportFigure.ACTIVATE_LINK_STATISTICS, BirtReportFigure.TOP_DOMAINS
    );

    private static final Set<BirtReportFigure> BOUNCES_FIGURES = EnumSet.of(
            BirtReportFigure.SOFTBOUNCES, BirtReportFigure.HARDBOUNCES
    );

    private static final Set<BirtReportFigure> RATE_FIGURES = EnumSet.of(
            BirtReportFigure.SENT_MAILS, BirtReportFigure.EMAILS_ACCEPTED,
            BirtReportFigure.OPENERS_MEASURED, BirtReportFigure.OPENERS_INVISIBLE,
            BirtReportFigure.OPENERS_TOTAL, BirtReportFigure.CLICKERS_TOTAL,
            BirtReportFigure.SIGNED_OFF, BirtReportFigure.HARDBOUNCES, BirtReportFigure.SOFTBOUNCES
    );

    private static final int MAX_DEVICES_PER_CLASS = 10;
    private static final int TOP_DOMAINS_LIMIT = 15;

    private final MailingService mailingService;
    private final MailinglistService mailinglistService;
    private final CampaignService campaignService;
    private final TargetService targetService;
    private final MailingSummaryStatsService mailingSummaryStatsService;
    private final MailingStatisticsService mailingStatisticsService;
    private final TrackableLinkService trackableLinkService;
    private final TrackableLinkClickStatsService trackableLinkClickStatsService;
    private final MailingBounceStatsService mailingBounceStatsService;
    private final MailingTopDomainsStatsService mailingTopDomainsStatsService;

    public MailingStatisticsReportServiceImpl(
            MailingService mailingService,
            MailinglistService mailinglistService,
            CampaignService campaignService,
            TargetService targetService,
            MailingSummaryStatsService mailingSummaryStatsService,
            MailingStatisticsService mailingStatisticsService,
            TrackableLinkService trackableLinkService,
            TrackableLinkClickStatsService trackableLinkClickStatsService,
            MailingBounceStatsService mailingBounceStatsService,
            MailingTopDomainsStatsService mailingTopDomainsStatsService
    ) {
        this.mailingService = mailingService;
        this.mailinglistService = mailinglistService;
        this.campaignService = campaignService;
        this.targetService = targetService;
        this.mailingSummaryStatsService = mailingSummaryStatsService;
        this.mailingStatisticsService = mailingStatisticsService;
        this.trackableLinkService = trackableLinkService;
        this.trackableLinkClickStatsService = trackableLinkClickStatsService;
        this.mailingBounceStatsService = mailingBounceStatsService;
        this.mailingTopDomainsStatsService = mailingTopDomainsStatsService;
    }

    @Override
    public Map<Integer, MailingStatisticsReportData> getReportData(MailingReportRequest request, int companyId) {
        Set<BirtReportFigure> figures = Set.copyOf(BirtReportUtils.unpackFigures(request.figures()));
        DateRange dateRange = toDateRange(request.startDate(), request.stopDate());
        Locale locale = request.locale();

        return request.mailings().stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        mailingId -> getMailingReportData(mailingId, companyId, request.targetGroups(), figures, dateRange, locale)
                ));
    }

    private MailingStatisticsReportData getMailingReportData(
            int mailingId, int companyId, List<Integer> targetGroups, Set<BirtReportFigure> figures, DateRange dateRange, Locale locale
    ) {
        Mailing mailing = mailingService.getMailing(companyId, mailingId);

        GeneralInfo generalInfo = anyFigureAllowed(figures, GENERAL_INFO_FIGURES)
                ? getMailingGeneralInfo(mailing, figures, dateRange)
                : null;

        MailingStatisticsService.RecipientsByMailFormat recipientsByMailingType = anyFigureAllowed(figures, MAILING_TYPE_FIGURES)
                ? mailingStatisticsService.getRecipientsByMailFormat(mailingId, mailing.getMailingType(), dateRange)
                : null;

        List<TargetGroupReportData> targetGroupsData = anyFigureAllowed(figures, TARGET_GROUP_FIGURES)
                ? getTargetGroupsReportData(mailing, targetGroups, figures, dateRange, locale)
                : null;

        return new MailingStatisticsReportData(generalInfo, recipientsByMailingType, targetGroupsData);
    }

    private List<TargetGroupReportData> getTargetGroupsReportData(
            Mailing mailing,
            List<Integer> targetGroups,
            Set<BirtReportFigure> figures,
            DateRange dateRange,
            Locale locale
    ) {
        List<LightTarget> targets = mailingStatisticsService.getTargetsWithAllSubscribersTarget(
                targetGroups, mailing.getCompanyID(), locale);

        int mailingId = mailing.getId();
        int companyId = mailing.getCompanyID();

        Map<DeviceClass, List<MailingStatisticsDao.DeviceCount>> openerDeviceCountsByClass = figures.contains(BirtReportFigure.OPENERS_DEVICES)
                ? getDeviceCountsByClass(mailingId, companyId, targets, dateRange, mailingStatisticsService::getOpenerDeviceCounts)
                : Map.of();

        Map<DeviceClass, List<MailingStatisticsDao.DeviceCount>> clickerDeviceCountsByClass = figures.contains(BirtReportFigure.CLICKER_DEVICES)
                ? getDeviceCountsByClass(mailingId, companyId, targets, dateRange, mailingStatisticsService::getClickerDeviceCounts)
                : Map.of();

        Map<Integer, LinkClicksByCategory> linkClicksByTarget = figures.contains(BirtReportFigure.ACTIVATE_LINK_STATISTICS)
                ? getLinkClicksByTarget(mailingId, companyId, targets, dateRange)
                : Map.of();

        Map<Integer, Bounces> bouncesByTarget = anyFigureAllowed(figures, BOUNCES_FIGURES)
                ? getBouncesByTarget(mailingId, companyId, targets, locale, figures)
                : Map.of();

        Map<Integer, DomainStats> domainStatsByTarget = figures.contains(BirtReportFigure.TOP_DOMAINS)
                ? getDomainStatsByTarget(mailingId, companyId, targetGroups, locale)
                : Map.of();

        return targets.stream()
                .map(target ->
                        getTargetGroupReportData(
                                target,
                                mailing,
                                figures,
                                dateRange,
                                openerDeviceCountsByClass,
                                clickerDeviceCountsByClass,
                                linkClicksByTarget,
                                bouncesByTarget,
                                domainStatsByTarget
                        )
                )
                .toList();
    }

    private Map<Integer, Bounces> getBouncesByTarget(
            int mailingId, int companyId, List<LightTarget> targets, Locale locale, Set<BirtReportFigure> figures
    ) {
        BounceStats stats = mailingBounceStatsService.getStats(mailingId, companyId, targets, locale);

        Map<Integer, Bounces> result = new HashMap<>();
        for (LightTarget target : targets) {
            int targetId = target.getId();

            List<BounceReasonStat> softBounces = figures.contains(BirtReportFigure.SOFTBOUNCES)
                    ? toBounceReasonStats(stats.softBounces().get(targetId))
                    : null;
            List<BounceReasonStat> hardBounces = figures.contains(BirtReportFigure.HARDBOUNCES)
                    ? toBounceReasonStats(stats.hardBounces().get(targetId))
                    : null;
            StatisticMetric undelivered = figures.contains(BirtReportFigure.SOFTBOUNCES)
                    ? stats.undelivered().get(targetId)
                    : null;

            result.put(targetId, new Bounces(softBounces, hardBounces, undelivered));
        }

        return result;
    }

    private static List<BounceReasonStat> toBounceReasonStats(Map<String, StatisticMetric> metrics) {
        if (metrics == null) {
            return List.of();
        }

        return metrics.entrySet().stream()
                .map(e -> new BounceReasonStat(e.getKey(), e.getValue()))
                .toList();
    }

    private Map<Integer, DomainStats> getDomainStatsByTarget(
            int mailingId, int companyId, List<Integer> targetGroups, Locale locale
    ) {
        TopDomainStats stats = mailingTopDomainsStatsService.getStats(new TopDomainsStatsQuery(
                mailingId, companyId, TOP_DOMAINS_LIMIT, false, locale, targetGroups
        ));

        Map<Integer, DomainStats> result = new HashMap<>();
        Set<Integer> targetIds = new LinkedHashSet<>(stats.sentEmails().keySet());
        targetIds.addAll(stats.hardBounces().keySet());
        targetIds.addAll(stats.softBounces().keySet());
        targetIds.addAll(stats.openers().keySet());
        targetIds.addAll(stats.clickers().keySet());

        for (Integer targetId : targetIds) {
            result.put(targetId, new DomainStats(
                    toDomainStats(stats.sentEmails().get(targetId)),
                    toDomainStats(stats.hardBounces().get(targetId)),
                    toDomainStats(stats.softBounces().get(targetId)),
                    toDomainStats(stats.openers().get(targetId)),
                    toDomainStats(stats.clickers().get(targetId))
            ));
        }

        return result;
    }

    private static List<DomainStat> toDomainStats(Map<String, StatisticMetric> domains) {
        if (domains == null) {
            return List.of();
        }

        return domains.entrySet().stream()
                .map(e -> new DomainStat(e.getKey(), e.getValue()))
                .toList();
    }

    private Map<Integer, LinkClicksByCategory> getLinkClicksByTarget(
            int mailingId, int companyId, List<LightTarget> targets, DateRange dateRange
    ) {
        List<TrackableLink> links = trackableLinkService.getTrackableLinks(companyId, mailingId, false);
        List<Integer> allLinkIds = links.stream().map(TrackableLink::getId).toList();
        List<TrackableLink> leadingLinks = links.stream().filter(link -> !link.isAdminLink()).toList();
        List<TrackableLink> administrativeLinks = links.stream().filter(TrackableLink::isAdminLink).toList();

        Map<Integer, TargetClickStats> statsByTarget = trackableLinkClickStatsService.getClickCountsByTarget(
                mailingId, companyId, targets, dateRange
        );

        Map<Integer, LinkClicksByCategory> result = new HashMap<>();
        for (LightTarget target : targets) {
            TargetClickStats stats = statsByTarget.get(target.getId());

            Map<ShareType, ClickCounts> totalsByShare = new EnumMap<>(ShareType.class);
            for (ShareType share : ShareType.values()) {
                totalsByShare.put(share, stats.aggregate(share, allLinkIds));
            }

            result.put(target.getId(), new LinkClicksByCategory(
                    toLinkCategoryStat(leadingLinks, stats, totalsByShare),
                    toLinkCategoryStat(administrativeLinks, stats, totalsByShare)
            ));
        }

        return result;
    }

    private LinkCategoryStat toLinkCategoryStat(
            List<TrackableLink> categoryLinks, TargetClickStats stats, Map<ShareType, ClickCounts> totalsByShare
    ) {
        List<Integer> linkIds = categoryLinks.stream().map(TrackableLink::getId).toList();
        LinkClickStats summary = toLinkClickStats(share -> stats.aggregate(share, linkIds), totalsByShare);

        List<LinkStat> links = categoryLinks.stream()
                .map(link -> new LinkStat(
                        link.getId(),
                        link.getFullUrl(),
                        toLinkClickStats(share -> stats.forLink(share, link.getId()), totalsByShare)
                ))
                .toList();

        return new LinkCategoryStat(summary, links);
    }

    private LinkClickStats toLinkClickStats(Function<ShareType, ClickCounts> countsByShare, Map<ShareType, ClickCounts> totalsByShare) {
        return new LinkClickStats(
                toLinkClickShare(countsByShare.apply(ShareType.TOTAL), totalsByShare.get(ShareType.TOTAL)),
                toLinkClickShare(countsByShare.apply(ShareType.MOBILE), totalsByShare.get(ShareType.MOBILE)),
                toLinkClickShare(countsByShare.apply(ShareType.ANONYMOUS), totalsByShare.get(ShareType.ANONYMOUS))
        );
    }

    private LinkClickShare toLinkClickShare(ClickCounts counts, ClickCounts totals) {
        return new LinkClickShare(
                StatisticMetric.of(counts.clicks(), totals.clicks()),
                StatisticMetric.of(counts.clickers(), totals.clickers())
        );
    }

    @FunctionalInterface
    private interface DeviceCountsQuery {
        List<MailingStatisticsDao.DeviceCount> get(
                int mailingId,
                int companyId,
                List<LightTarget> targets,
                DeviceClass deviceClass,
                DateRange dateRange,
                int maxCount
        );
    }

    private Map<DeviceClass, List<MailingStatisticsDao.DeviceCount>> getDeviceCountsByClass(
            int mailingId, int companyId, List<LightTarget> targets, DateRange dateRange, DeviceCountsQuery query
    ) {
        Map<DeviceClass, List<MailingStatisticsDao.DeviceCount>> result = new EnumMap<>(DeviceClass.class);
        for (DeviceClass deviceClass : DeviceClass.getOnlyKnownDeviceClasses()) {
            result.put(deviceClass, query.get(mailingId, companyId, targets, deviceClass, dateRange, MAX_DEVICES_PER_CLASS));
        }
        return result;
    }

    private static DateRange toDateRange(Instant startDate, Instant stopDate) {
        if (startDate == null || stopDate == null) {
            return null;
        }

        return new DateRange(Date.from(startDate), Date.from(stopDate));
    }

    private GeneralInfo getMailingGeneralInfo(
            Mailing mailing,
            Set<BirtReportFigure> figures,
            DateRange dateRange
    ) {
        int mailingId = mailing.getId();

        DispatchStats dispatchStats = anyFigureAllowed(figures, DISPATCH_STATS_FIGURES)
                ? mailingSummaryStatsService.getDispatchStats(mailingId, mailing.getCompanyID(), dateRange)
                : null;

        return new GeneralInfo(
                mailing.getId(),
                mailing.getShortname(),
                mailing.getDescription(),
                figures.contains(BirtReportFigure.ARCHIVE) ? getArchiveName(mailing) : null,
                getEmailMediaTypePropertyOrNull(mailing, me -> MailType.getFromInt(me.getMailFormat())),
                dispatchStats != null ? dispatchStats.sizeInKb() : null,
                getEmailMediaTypePropertyOrNull(mailing, MediatypeEmail::getSubject),
                figures.contains(BirtReportFigure.MAILING_LIST) ? mailinglistService.getMailinglistName(mailing.getMailinglistID(), mailing.getCompanyID()) : null,
                figures.contains(BirtReportFigure.TARGET_GROUPS) ? targetService.getTargetNamesByIds(mailing.getCompanyID(), Set.copyOf(mailing.getTargetGroups())) : null,
                dispatchStats != null ? dispatchStats.sentCount() : null,
                dispatchStats != null ? dispatchStats.anonymousRecipientsCount() : null,
                dispatchStats != null ? dispatchStats.sendingStarted() : null,
                dispatchStats != null ? dispatchStats.sendingFinished() : null,
                getEmailMediaTypePropertyOrNull(mailing, MediatypeEmail::getFromFullname),
                getEmailMediaTypePropertyOrNull(mailing, MediatypeEmail::getFromEmail),
                getEmailMediaTypePropertyOrNull(mailing, MediatypeEmail::getReplyFullname),
                getEmailMediaTypePropertyOrNull(mailing, MediatypeEmail::getReplyEmail)
        );
    }

    private TargetGroupReportData getTargetGroupReportData(
            LightTarget target,
            Mailing mailing,
            Set<BirtReportFigure> figures,
            DateRange dateRange,
            Map<DeviceClass, List<MailingStatisticsDao.DeviceCount>> openerDeviceCountsByClass,
            Map<DeviceClass, List<MailingStatisticsDao.DeviceCount>> clickerDeviceCountsByClass,
            Map<Integer, LinkClicksByCategory> linkClicksByTarget,
            Map<Integer, Bounces> bouncesByTarget,
            Map<Integer, DomainStats> domainStatsByTarget
    ) {
        int mailingId = mailing.getId();
        int companyId = mailing.getCompanyID();
        String targetSql = target.getTargetSQL();

        long sentMails = anyFigureAllowed(figures, RATE_FIGURES)
                ? mailingStatisticsService.getSentCount(mailingId, companyId, MaildropStatus.realSendStatuses(), targetSql, dateRange)
                : 0;

        boolean ratesAvailable = sentMails >= 0;

        boolean needHardBounces = ratesAvailable && (figures.contains(BirtReportFigure.HARDBOUNCES)
                || figures.contains(BirtReportFigure.EMAILS_ACCEPTED)
                || figures.contains(BirtReportFigure.SOFTBOUNCES));
        long hardBounces = needHardBounces
                ? mailingStatisticsService.getHardBouncesCount(mailingId, companyId, targetSql, dateRange)
                : 0;

        StatisticMetric emailsAccepted = ratesAvailable && figures.contains(BirtReportFigure.EMAILS_ACCEPTED)
                ? mailingStatisticsService.getDeliveredCount(mailingId, companyId, sentMails, hardBounces, targetSql, dateRange)
                .map(accepted -> StatisticMetric.of(accepted, sentMails))
                .orElse(null)
                : null;

        boolean needMeasuredOpeners = ratesAvailable && (figures.contains(BirtReportFigure.OPENERS_MEASURED)
                || figures.contains(BirtReportFigure.OPENERS_INVISIBLE)
                || figures.contains(BirtReportFigure.OPENERS_TOTAL)
                || figures.contains(BirtReportFigure.OPENERS_AFTER_DEVICE)
                || figures.contains(BirtReportFigure.OPENERS_DEVICES));
        long openersMeasured = needMeasuredOpeners
                ? mailingStatisticsService.getMeasuredOpenersCount(mailingId, companyId, targetSql, dateRange)
                : 0;

        StatisticMetric openersProxy = ratesAvailable && figures.contains(BirtReportFigure.OPENERS_MEASURED)
                ? StatisticMetric.of(
                mailingStatisticsService.getOpenersCount(mailingId, companyId, targetSql, dateRange, MailingStatisticsDao.PROXY_DEVICE_IDS),
                sentMails)
                : null;

        boolean needInvisibleOpeners = ratesAvailable && (figures.contains(BirtReportFigure.OPENERS_INVISIBLE)
                || figures.contains(BirtReportFigure.OPENERS_TOTAL));
        long openersInvisible = needInvisibleOpeners
                ? mailingStatisticsService.getInvisibleOpenersCount(mailingId, companyId, openersMeasured, sentMails, targetSql, dateRange)
                : 0;

        StatisticMetric openersTotal = ratesAvailable && figures.contains(BirtReportFigure.OPENERS_TOTAL)
                ? StatisticMetric.of(openersMeasured + openersInvisible, sentMails)
                : null;

        Long anonymousOpenings = figures.contains(BirtReportFigure.OPENINGS_ANONYMOUS)
                ? (long) mailingStatisticsService.getAnonymousOpenings(mailingId, companyId, dateRange)
                : null;

        boolean needClickersTotal = ratesAvailable && (figures.contains(BirtReportFigure.CLICKERS_TOTAL)
                || figures.contains(BirtReportFigure.CLICKERS_AFTER_DEVICE)
                || figures.contains(BirtReportFigure.CLICKER_DEVICES));
        long clickersTotal = needClickersTotal
                ? mailingStatisticsService.getClickersCount(mailingId, companyId, targetSql, dateRange)
                : 0;
        StatisticMetric clickers = ratesAvailable && figures.contains(BirtReportFigure.CLICKERS_TOTAL)
                ? StatisticMetric.of(clickersTotal, sentMails)
                : null;

        StatisticMetric clicks = ratesAvailable && figures.contains(BirtReportFigure.CLICKERS_AFTER_DEVICE)
                ? new StatisticMetric(mailingStatisticsService.getClicksCount(mailingId, companyId, targetSql, dateRange), 0)
                : null;

        Long anonymousClicks = figures.contains(BirtReportFigure.CLICKS_ANONYMOUS)
                ? (long) mailingStatisticsService.getAnonymousClicks(mailingId, companyId, dateRange)
                : null;

        StatisticMetric optOuts = ratesAvailable && figures.contains(BirtReportFigure.SIGNED_OFF)
                ? StatisticMetric.of(mailingStatisticsService.getOptOutsCount(mailingId, companyId, targetSql, dateRange), sentMails)
                : null;

        StatisticMetric softBounces = ratesAvailable && figures.contains(BirtReportFigure.SOFTBOUNCES)
                ? mailingStatisticsService.getSoftBouncesUndeliverable(mailingId, companyId, sentMails, hardBounces, targetSql, dateRange)
                .map(value -> StatisticMetric.of(value, sentMails))
                .orElse(null)
                : null;

        Double revenue = figures.contains(BirtReportFigure.REVENUE)
                ? mailingStatisticsService.getRevenueCount(mailingId, companyId, targetSql, dateRange)
                : null;

        MailingStatisticsService.ByDeviceClass openersByClass = ratesAvailable
                && (figures.contains(BirtReportFigure.OPENERS_AFTER_DEVICE) || figures.contains(BirtReportFigure.OPENERS_DEVICES))
                ? mailingStatisticsService.toByDeviceClass(
                        mailingStatisticsService.getOpenersByDevice(mailingId, companyId, targetSql, dateRange), openersMeasured)
                : null;

        MailingStatisticsService.ByDeviceClass clickersByClass = ratesAvailable
                && (figures.contains(BirtReportFigure.CLICKERS_AFTER_DEVICE) || figures.contains(BirtReportFigure.CLICKER_DEVICES))
                ? mailingStatisticsService.toByDeviceClass(
                        mailingStatisticsService.getClickersByDevice(mailingId, companyId, targetSql, dateRange), clickersTotal)
                : null;

        DeviceTypeStat openersByDeviceType = openersByClass != null && figures.contains(BirtReportFigure.OPENERS_AFTER_DEVICE)
                ? toDeviceTypeStat(openersByClass, openersMeasured)
                : null;

        DeviceTypeStat clickersByDeviceType = clickersByClass != null && figures.contains(BirtReportFigure.CLICKERS_AFTER_DEVICE)
                ? toDeviceTypeStat(clickersByClass, clickersTotal)
                : null;

        Map<DeviceClass, List<DeviceStat>> openersByDeviceName = openersByClass != null && figures.contains(BirtReportFigure.OPENERS_DEVICES)
                ? toDeviceStatsByClass(target.getId(), openersByClass, openerDeviceCountsByClass)
                : null;

        Map<DeviceClass, List<DeviceStat>> clickersByDeviceName = clickersByClass != null && figures.contains(BirtReportFigure.CLICKER_DEVICES)
                ? toDeviceStatsByClass(target.getId(), clickersByClass, clickerDeviceCountsByClass)
                : null;

        Summary summary = new Summary(
                ratesAvailable && figures.contains(BirtReportFigure.SENT_MAILS) ? new StatisticMetric(sentMails, 100) : null,
                emailsAccepted,
                ratesAvailable && figures.contains(BirtReportFigure.OPENERS_MEASURED) ? StatisticMetric.of(openersMeasured, sentMails) : null,
                openersProxy,
                ratesAvailable && figures.contains(BirtReportFigure.OPENERS_INVISIBLE) ? StatisticMetric.of(openersInvisible, sentMails) : null,
                openersTotal,
                anonymousOpenings,
                clickers,
                clicks,
                anonymousClicks,
                optOuts,
                softBounces,
                ratesAvailable && figures.contains(BirtReportFigure.HARDBOUNCES) ? StatisticMetric.of(hardBounces, sentMails) : null,
                revenue
        );

        return new TargetGroupReportData(
                target.getId(),
                target.getName(),
                summary,
                openersByDeviceType,
                clickersByDeviceType,
                openersByDeviceName,
                clickersByDeviceName,
                linkClicksByTarget.get(target.getId()),
                bouncesByTarget.get(target.getId()),
                domainStatsByTarget.get(target.getId())
        );
    }

    private DeviceTypeStat toDeviceTypeStat(MailingStatisticsService.ByDeviceClass byDeviceClass, long total) {
        return new DeviceTypeStat(
                StatisticMetric.of(byDeviceClass.pc(), total),
                StatisticMetric.of(byDeviceClass.tablet(), total),
                StatisticMetric.of(byDeviceClass.mobile(), total),
                StatisticMetric.of(byDeviceClass.smartTv(), total),
                StatisticMetric.of(byDeviceClass.multi(), total)
        );
    }

    private Map<DeviceClass, List<DeviceStat>> toDeviceStatsByClass(
            int targetId,
            MailingStatisticsService.ByDeviceClass byDeviceClass,
            Map<DeviceClass, List<MailingStatisticsDao.DeviceCount>> deviceCountsByClass
    ) {
        Map<DeviceClass, List<DeviceStat>> result = new EnumMap<>(DeviceClass.class);
        result.put(DeviceClass.DESKTOP, toDeviceStats(targetId, byDeviceClass.pc(), deviceCountsByClass.get(DeviceClass.DESKTOP)));
        result.put(DeviceClass.TABLET, toDeviceStats(targetId, byDeviceClass.tablet(), deviceCountsByClass.get(DeviceClass.TABLET)));
        result.put(DeviceClass.MOBILE, toDeviceStats(targetId, byDeviceClass.mobile(), deviceCountsByClass.get(DeviceClass.MOBILE)));
        result.put(DeviceClass.SMARTTV, toDeviceStats(targetId, byDeviceClass.smartTv(), deviceCountsByClass.get(DeviceClass.SMARTTV)));
        return result;
    }

    private List<DeviceStat> toDeviceStats(
            int targetId, long classTotal, List<MailingStatisticsDao.DeviceCount> topDevices
    ) {
        if (classTotal <= 0) {
            return List.of();
        }

        List<DeviceStat> stats = new ArrayList<>();
        long shown = 0;
        for (MailingStatisticsDao.DeviceCount device : topDevices) {
            long count = device.countsByTargetId().getOrDefault(targetId, 0L);
            if (count <= 0) {
                continue;
            }
            stats.add(new DeviceStat(device.deviceName(), StatisticMetric.of(count, classTotal), false));
            shown += count;
        }

        long other = classTotal - shown;
        if (other > 0) {
            stats.add(new DeviceStat(null, StatisticMetric.of(other, classTotal), true));
        }

        return stats;
    }

    private static boolean anyFigureAllowed(Set<BirtReportFigure> figures, Set<BirtReportFigure> candidates) {
        return !Collections.disjoint(figures, candidates);
    }

    private String getArchiveName(Mailing mailing) {
        return campaignService.findName(mailing.getCampaignID(), mailing.getCompanyID())
                .orElse(null);
    }

    private <T> T getEmailMediaTypePropertyOrNull(Mailing mailing, Function<MediatypeEmail, T> getter) {
        return Optional.ofNullable(mailing.getEmailParam())
                .map(getter)
                .orElse(null);
    }

}
