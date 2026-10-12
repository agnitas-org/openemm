/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.birtreport.service.impl;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;

import com.agnitas.dao.CompanyDao;
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
import com.agnitas.emm.core.birtreport.model.MailingStatisticsReportData.TargetGroupReportData.LinkClicksByCategory;
import com.agnitas.emm.core.birtreport.model.MailingStatisticsReportData.TargetGroupReportData.LinkStat;
import com.agnitas.emm.core.birtreport.model.MailingStatisticsReportData.TargetGroupReportData.Summary;
import com.agnitas.emm.core.birtreport.service.MailingStatisticsCsvReportService;
import com.agnitas.emm.core.birtreport.service.MailingStatisticsReportService;
import com.agnitas.emm.core.birtstatistics.dto.StatisticMetric;
import com.agnitas.emm.core.birtstatistics.service.MailingStatisticsService.RecipientsByMailFormat;
import com.agnitas.emm.core.mobile.bean.DeviceClass;
import com.agnitas.messages.I18nString;
import com.agnitas.reporting.birt.external.utils.BirtReportUtils;
import com.agnitas.reporting.birt.external.utils.BirtReportUtils.BirtReportFigure;
import com.agnitas.util.CsvWriter;
import com.agnitas.util.importvalues.MailType;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

@Service
public class MailingStatisticsCsvReportServiceImpl implements MailingStatisticsCsvReportService {

    private static final char SEPARATOR = ';';
    private static final char QUOTE = '"';

    private static final String KEY_SHARE = "report.mailing.statistics.share";
    private static final String KEY_TARGETS = "Targets";
    private static final String KEY_SENT_EMAILS = "report.sentMails";
    private static final String KEY_OPENERS_MEASURED = "report.openers.measured";
    private static final String KEY_CLICKERS = "statistic.clicker";
    private static final String KEY_OPENERS = "statistic.opener";
    private static final String KEY_SOFT_BOUNCES = "report.softbounces";
    private static final String KEY_HARD_BOUNCES = "statistic.bounces.hardbounce";
    private static final String KEY_CLICKS = "statistic.Clicks";

    private final MailingStatisticsReportService mailingStatisticsReportService;
    private final CompanyDao companyDao;

    public MailingStatisticsCsvReportServiceImpl(
            MailingStatisticsReportService mailingStatisticsReportService,
            CompanyDao companyDao
    ) {
        this.mailingStatisticsReportService = mailingStatisticsReportService;
        this.companyDao = companyDao;
    }

    @Override
    public byte[] build(MailingReportRequest request, int companyId) {
        Locale locale = request.locale();
        List<BirtReportFigure> figures = BirtReportUtils.unpackFigures(request.figures());
        Map<Integer, MailingStatisticsReportData> mailingsData = mailingStatisticsReportService.getReportData(request, companyId);

        StringBuilder csv = new StringBuilder();
        appendReportHeader(csv, request, companyId, mailingsData, locale);

        for (Integer mailingId : request.mailings()) {
            MailingStatisticsReportData data = mailingsData.get(mailingId);
            if (data != null) {
                appendMailingSection(csv, data, figures, locale);
            }
        }

        trimTrailingBlankRow(csv);

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static void trimTrailingBlankRow(StringBuilder csv) {
        if (csv.length() >= 2 && csv.charAt(csv.length() - 1) == '\n' && csv.charAt(csv.length() - 2) == '\n') {
            csv.setLength(csv.length() - 1);
        }
    }

    private void appendReportHeader(
            StringBuilder csv,
            MailingReportRequest request,
            int companyId,
            Map<Integer, MailingStatisticsReportData> mailingsData,
            Locale locale
    ) {
        row(csv, "EMM Report", t("birt.report.mailingStatisticsIndividually", locale));
        emptyRow(csv);
        row(csv, t("report.mailing.statistics.reportdatum", locale), new SimpleDateFormat("M/d/yyyy", locale).format(new Date()));
        row(csv, t("report.recipient.statistics.account", locale), companyDao.getShortName(companyId));
        row(csv, "Name", request.reportName());
        row(csv, "Selected mailings", buildSelectedMailingsValue(request.mailings(), mailingsData));
        emptyRow(csv);
    }

    private String buildSelectedMailingsValue(
            List<Integer> mailingIds,
            Map<Integer, MailingStatisticsReportData> mailingsData
    ) {
        return mailingIds.stream()
                .map(mailingsData::get)
                .filter(Objects::nonNull)
                .map(data -> data.generalInfo().name())
                .collect(Collectors.joining(", "));
    }

    private void appendMailingSection(
            StringBuilder csv,
            MailingStatisticsReportData data,
            List<BirtReportFigure> figures,
            Locale locale
    ) {
        appendGeneralInfo(csv, data.generalInfo(), figures, locale);
        emptyRow(csv);

        List<TargetGroupReportData> targetGroups = data.targetGroupsData();
        if (CollectionUtils.isEmpty(targetGroups)) {
            return;
        }

        appendSummary(csv, targetGroups, figures, locale);
        appendMailFormat(csv, data.recipientsByMailingType(), data.generalInfo(), figures, locale);
        appendDeviceTypes(csv, targetGroups, figures, locale);
        appendDeviceNames(csv, targetGroups, figures, locale);
        appendLinkClicks(csv, targetGroups, figures, locale);
        appendBounces(csv, targetGroups, figures, locale);
        appendDomains(csv, targetGroups, figures, locale);
    }

    // region General info

    private record GeneralInfoField(
            BirtReportFigure figure,
            String labelKey,
            BiFunction<GeneralInfo, Locale, Object> valueExtractor
    ) {
    }

    private static final List<GeneralInfoField> GENERAL_INFO_FIELDS = List.of(
            new GeneralInfoField(
                    BirtReportFigure.MAILING_NAME,
                    "mailing.searchName",
                    (info, locale) -> info.name()
            ),
            new GeneralInfoField(
                    BirtReportFigure.MAILING_ID,
                    "MailingId",
                    (info, locale) -> info.id()
            ),
            new GeneralInfoField(
                    BirtReportFigure.DESCRIPTION,
                    "Description",
                    (info, locale) -> info.description()
            ),
            new GeneralInfoField(
                    BirtReportFigure.ARCHIVE,
                    "mailing.archive",
                    (info, locale) -> info.archive()
            ),
            new GeneralInfoField(
                    BirtReportFigure.MAIL_FORMAT,
                    "mailing.Mailtype",
                    (info, locale) -> mailTypeLabel(info.mailType(), locale)
            ),
            new GeneralInfoField(
                    BirtReportFigure.AVERAGE_SIZE,
                    "AverageMailSize",
                    (info, locale) -> info.avgMailSizeKb()
            ),
            new GeneralInfoField(
                    BirtReportFigure.SUBJECT,
                    "mailing.Subject",
                    (info, locale) -> info.subject()
            ),
            new GeneralInfoField(
                    BirtReportFigure.MAILING_LIST,
                    "birt.mailinglist",
                    (info, locale) -> info.mailinglist()
            ),
            new GeneralInfoField(
                    BirtReportFigure.TARGET_GROUPS,
                    KEY_TARGETS,
                    MailingStatisticsCsvReportServiceImpl::targetGroupsValue
            ),
            new GeneralInfoField(
                    BirtReportFigure.NR_OF_RECIPIENTS_WITH_TRACK_VETO,
                    "report.numberRecipients.notracking",
                    (info, locale) -> "%s (%s)".formatted(
                            nullToEmpty(info.recipientsCount()),
                            nullToEmpty(info.anonymousRecipientsCount())
                    )
            ),
            new GeneralInfoField(
                    BirtReportFigure.SENDING_STARTED,
                    "report.sendingStarted",
                    (info, locale) -> formatDate(info.sendingStarted(), locale)
            ),
            new GeneralInfoField(
                    BirtReportFigure.SENDING_FINISHED,
                    "report.sendingFinished",
                    (info, locale) -> formatDate(info.sendingFinished(), locale)
            ),
            new GeneralInfoField(
                    BirtReportFigure.SENDER_INFO,
                    "mailing.sender.info",
                    (info, locale) -> nameEmail(info.senderName(), info.senderEmail())
            ),
            new GeneralInfoField(
                    BirtReportFigure.REPLY_TO_INFO,
                    "mailing.reply.info",
                    (info, locale) -> nameEmail(info.replyName(), info.replyEmail())
            )
    );

    private void appendGeneralInfo(StringBuilder csv, GeneralInfo info, List<BirtReportFigure> figures, Locale locale) {
        if (info == null) {
            return;
        }

        row(csv, t("settings.general.information", locale));

        for (GeneralInfoField field : GENERAL_INFO_FIELDS) {
            if (figures.contains(field.figure())) {
                row(csv, t(field.labelKey(), locale), field.valueExtractor().apply(info, locale));
            }
        }
    }

    private static String mailTypeLabel(MailType mailType, Locale locale) {
        if (mailType == null) {
            return "";
        }
        return t(mailType.getMessageKey(), locale);
    }

    private static String targetGroupsValue(GeneralInfo info, Locale locale) {
        if (info.targetGroups() == null || info.targetGroups().isEmpty()) {
            return t("statistic.all_subscribers", locale);
        }
        return String.join(", ", info.targetGroups());
    }

    private static String nameEmail(String name, String email) {
        if (name == null || name.isBlank()) {
            return email == null ? "" : email;
        }
        return "%s <%s>".formatted(name, email == null ? "" : email);
    }

    private static String formatDate(Date date, Locale locale) {
        if (date == null) {
            return "";
        }
        return new SimpleDateFormat("MMM dd yyyy hh:mm:ss a", locale).format(date);
    }

    // endregion

    // region Summary

    private record SummaryMetric(
            BirtReportFigure figure,
            String labelKey,
            Function<Summary, StatisticMetric> extractor
    ) {
    }

    private static final List<SummaryMetric> SUMMARY_METRICS = List.of(
            new SummaryMetric(BirtReportFigure.SENT_MAILS, KEY_SENT_EMAILS, Summary::sentMails),
            new SummaryMetric(BirtReportFigure.EMAILS_ACCEPTED, "statistic.mails.delivered", Summary::emailsAccepted),
            new SummaryMetric(BirtReportFigure.OPENERS_MEASURED, KEY_OPENERS_MEASURED, Summary::openersMeasured),
            new SummaryMetric(BirtReportFigure.OPENERS_INVISIBLE, "statistic.opener.invisible", Summary::openersInvisible),
            new SummaryMetric(BirtReportFigure.OPENERS_TOTAL, "statistic.opener.total", Summary::openersTotal),
            new SummaryMetric(BirtReportFigure.CLICKERS_TOTAL, KEY_CLICKERS, Summary::clickers),
            new SummaryMetric(BirtReportFigure.CLICKERS_AFTER_DEVICE, KEY_CLICKS, Summary::clicks),
            new SummaryMetric(BirtReportFigure.SIGNED_OFF, "statistic.Opt_Outs", Summary::optOuts),
            new SummaryMetric(BirtReportFigure.SOFTBOUNCES, KEY_SOFT_BOUNCES, Summary::softBounces),
            new SummaryMetric(BirtReportFigure.HARDBOUNCES, KEY_HARD_BOUNCES, Summary::hardBounces)
    );

    private void appendSummary(
            StringBuilder csv,
            List<TargetGroupReportData> targetGroups,
            List<BirtReportFigure> figures,
            Locale locale
    ) {
        List<SummaryMetric> metrics = SUMMARY_METRICS.stream()
                .filter(m -> figures.contains(m.figure()))
                .toList();

        boolean hasAnonymousOpenings = figures.contains(BirtReportFigure.OPENINGS_ANONYMOUS);
        boolean hasAnonymousClicks = figures.contains(BirtReportFigure.CLICKS_ANONYMOUS);
        boolean hasRevenue = figures.contains(BirtReportFigure.REVENUE);
        if (metrics.isEmpty() && !hasAnonymousOpenings && !hasAnonymousClicks && !hasRevenue) {
            return;
        }

        List<String> header = targetGroupsHeader(t("Summary", locale), targetGroups, locale);
        row(csv, header.toArray());
        row(csv, blankRow(header).toArray());

        for (SummaryMetric metric : metrics) {
            appendSummaryMetricRow(csv, metric, targetGroups, locale);
        }

        if (hasAnonymousOpenings) {
            row(csv, t("statistic.openings.anonym", locale), targetGroups.getFirst().summary().anonymousOpenings());
        }
        if (hasAnonymousClicks) {
            row(csv, t("statistic.clicks.anonym", locale), targetGroups.getFirst().summary().anonymousClicks());
        }
        if (hasRevenue) {
            appendRevenueRow(csv, targetGroups, locale);
        }
        emptyRow(csv);
    }

    private void appendSummaryMetricRow(
            StringBuilder csv,
            SummaryMetric metric,
            List<TargetGroupReportData> targetGroups,
            Locale locale
    ) {
        List<Object> values = new ArrayList<>();
        values.add(t(metric.labelKey(), locale));
        for (TargetGroupReportData tg : targetGroups) {
            StatisticMetric value = orZero(metric.extractor().apply(tg.summary()));
            values.add(value.value());
            values.add(value.formatRate());
        }
        row(csv, values.toArray());
    }

    private void appendRevenueRow(StringBuilder csv, List<TargetGroupReportData> targetGroups, Locale locale) {
        List<Object> values = new ArrayList<>();
        values.add(t("statistic.revenue", locale));
        for (TargetGroupReportData tg : targetGroups) {
            values.add("%.2f".formatted(nullToZero(tg.summary().revenue())));
            values.add("");
        }
        row(csv, values.toArray());
    }

    // endregion

    // region Mail type

    private record MailFormatField(
            BirtReportFigure figure,
            MailType mailType,
            ToLongFunction<RecipientsByMailFormat> valueExtractor
    ) {
    }

    private static final List<MailFormatField> MAIL_FORMAT_FIELDS = List.of(
            new MailFormatField(BirtReportFigure.HTML, MailType.HTML, RecipientsByMailFormat::html),
            new MailFormatField(BirtReportFigure.TEXT, MailType.TEXT, RecipientsByMailFormat::text),
            new MailFormatField(BirtReportFigure.OFFLINE_HTML, MailType.HTML_OFFLINE, RecipientsByMailFormat::offlineHtml)
    );

    private void appendMailFormat(
            StringBuilder csv,
            RecipientsByMailFormat recipients,
            GeneralInfo generalInfo,
            List<BirtReportFigure> figures,
            Locale locale
    ) {
        List<MailFormatField> fields = MAIL_FORMAT_FIELDS.stream()
                .filter(f -> figures.contains(f.figure()))
                .toList();

        if (recipients == null || fields.isEmpty()) {
            return;
        }

        long total = Optional.ofNullable(generalInfo)
                .map(GeneralInfo::recipientsCount)
                .orElseGet(() -> recipients.html() + recipients.text() + recipients.offlineHtml());

        row(csv, "Mailing type", "Recipients", t(KEY_SHARE, locale));
        for (MailFormatField field : fields) {
            long value = field.valueExtractor().applyAsLong(recipients);
            row(csv, t(field.mailType().getMessageKey(), locale), value, formatShare(value, total));
        }
        emptyRow(csv);
    }

    // endregion

    // region Device types

    private record DeviceTypeSlot(String messageKey, Function<DeviceTypeStat, StatisticMetric> extractor) {
    }

    private static final List<DeviceTypeSlot> DEVICE_TYPE_SLOTS = List.of(
            new DeviceTypeSlot(DeviceClass.DESKTOP.getMessageKey(), DeviceTypeStat::pc),
            new DeviceTypeSlot(DeviceClass.TABLET.getMessageKey(), DeviceTypeStat::tablet),
            new DeviceTypeSlot(DeviceClass.MOBILE.getMessageKey(), DeviceTypeStat::mobile),
            new DeviceTypeSlot(DeviceClass.SMARTTV.getMessageKey(), DeviceTypeStat::smartTv),
            new DeviceTypeSlot("report.device.mixed", DeviceTypeStat::multi)
    );

    private void appendDeviceTypes(
            StringBuilder csv,
            List<TargetGroupReportData> targetGroups,
            List<BirtReportFigure> figures,
            Locale locale
    ) {
        boolean hasOpeners = figures.contains(BirtReportFigure.OPENERS_AFTER_DEVICE);
        boolean hasClickers = figures.contains(BirtReportFigure.CLICKERS_AFTER_DEVICE);
        if (!hasOpeners && !hasClickers) {
            return;
        }

        List<String> header = targetGroupsHeader("by device types", targetGroups, locale);
        row(csv, header.toArray());
        row(csv, blankRow(header).toArray());

        if (hasOpeners) {
            appendDeviceTypeGroup(csv, targetGroups, locale, TargetGroupReportData::openersByDeviceType,
                    tg -> tg.summary().openersMeasured(), t(KEY_OPENERS_MEASURED, locale), t(KEY_OPENERS, locale));
        }
        if (hasClickers) {
            appendDeviceTypeGroup(csv, targetGroups, locale, TargetGroupReportData::clickersByDeviceType,
                    tg -> tg.summary().clickers(), t(KEY_CLICKERS, locale), t(KEY_CLICKERS, locale));
        }
        if (hasOpeners) {
            appendOpenersProxyRow(csv, targetGroups, locale);
        }
        emptyRow(csv);
    }

    private void appendDeviceTypeGroup(
            StringBuilder csv,
            List<TargetGroupReportData> targetGroups,
            Locale locale,
            Function<TargetGroupReportData, DeviceTypeStat> extractor,
            Function<TargetGroupReportData, StatisticMetric> totalExtractor,
            String totalLabel,
            String rowPrefix
    ) {
        appendDeviceTypeRow(csv, targetGroups, totalLabel,
                tg -> new StatisticMetric(orZero(totalExtractor.apply(tg)).value(), 100.0));

        for (DeviceTypeSlot slot : DEVICE_TYPE_SLOTS) {
            appendDeviceTypeRow(csv, targetGroups, "%s (%s)".formatted(rowPrefix, t(slot.messageKey(), locale)),
                    tg -> {
                        DeviceTypeStat stat = extractor.apply(tg);
                        return stat == null ? null : slot.extractor().apply(stat);
                    });
        }
    }

    private void appendDeviceTypeRow(
            StringBuilder csv,
            List<TargetGroupReportData> targetGroups,
            String label,
            Function<TargetGroupReportData, StatisticMetric> extractor
    ) {
        List<Object> values = new ArrayList<>();
        values.add(label);
        for (TargetGroupReportData tg : targetGroups) {
            StatisticMetric metric = orZero(extractor.apply(tg));
            values.add(metric.value());
            values.add(metric.formatRate());
        }
        row(csv, values.toArray());
    }

    private void appendOpenersProxyRow(StringBuilder csv, List<TargetGroupReportData> targetGroups, Locale locale) {
        List<Object> values = new ArrayList<>();
        values.add(t("statistic.opener.proxy", locale));
        for (TargetGroupReportData tg : targetGroups) {
            values.add(orZero(tg.summary().openersProxy()).value());
            values.add("");
        }
        row(csv, values.toArray());
    }

    // endregion

    // region Device names

    private void appendDeviceNames(
            StringBuilder csv,
            List<TargetGroupReportData> targetGroups,
            List<BirtReportFigure> figures,
            Locale locale
    ) {
        if (figures.contains(BirtReportFigure.OPENERS_DEVICES)) {
            for (DeviceClass deviceClass : DeviceClass.getOnlyKnownDeviceClasses()) {
                appendDeviceNameTable(csv, targetGroups, locale, TargetGroupReportData::openersByDeviceName, deviceClass,
                        deviceClassLabel(deviceClass, locale) + " " + t(KEY_OPENERS_MEASURED, locale));
            }
        }

        if (figures.contains(BirtReportFigure.CLICKER_DEVICES)) {
            for (DeviceClass deviceClass : DeviceClass.getOnlyKnownDeviceClasses()) {
                appendDeviceNameTable(csv, targetGroups, locale, TargetGroupReportData::clickersByDeviceName, deviceClass,
                        deviceClassLabel(deviceClass, locale) + " " + t(KEY_CLICKERS, locale));
            }
        }
    }

    private String deviceClassLabel(DeviceClass deviceClass, Locale locale) {
        return t(deviceClass.getMessageKey(), locale);
    }

    private void appendDeviceNameTable(
            StringBuilder csv,
            List<TargetGroupReportData> targetGroups,
            Locale locale,
            Function<TargetGroupReportData, Map<DeviceClass, List<DeviceStat>>> extractor,
            DeviceClass deviceClass,
            String title
    ) {
        Map<Integer, List<DeviceStat>> statsByTarget = new LinkedHashMap<>();
        Map<String, String> deviceNames = new LinkedHashMap<>();
        for (TargetGroupReportData tg : targetGroups) {
            List<DeviceStat> stats = Optional.ofNullable(extractor.apply(tg))
                    .map(byClass -> byClass.get(deviceClass))
                    .orElse(List.of());
            statsByTarget.put(tg.targetId(), stats);
            for (DeviceStat stat : stats) {
                deviceNames.putIfAbsent(
                        stat.deviceName(),
                        stat.other() ? t("statistic.Other", locale) : stat.deviceName()
                );
            }
        }

        if (deviceNames.isEmpty()) {
            return;
        }

        row(csv, title);
        row(csv, targetGroupsHeader("", targetGroups, locale).toArray());

        for (Map.Entry<String, String> entry : deviceNames.entrySet()) {
            appendDeviceNameRow(csv, entry, targetGroups, statsByTarget);
        }
        emptyRow(csv);
    }

    private void appendDeviceNameRow(
            StringBuilder csv,
            Map.Entry<String, String> entry,
            List<TargetGroupReportData> targetGroups,
            Map<Integer, List<DeviceStat>> statsByTarget
    ) {
        List<Object> values = new ArrayList<>();
        values.add(entry.getValue());
        for (TargetGroupReportData tg : targetGroups) {
            DeviceStat match = statsByTarget.get(tg.targetId()).stream()
                    .filter(s -> Objects.equals(entry.getKey(), s.deviceName()))
                    .findFirst()
                    .orElse(null);

            StatisticMetric value = orZero(match == null ? null : match.value());
            values.add(value.value());
            values.add(value.formatRate());
        }
        row(csv, values.toArray());
    }

    // endregion

    // region Link clicks

    private enum LinkCategory {
        LEADING(LinkClicksByCategory::leadingLinks),
        ADMINISTRATIVE(LinkClicksByCategory::administrativeLinks);

        private final Function<LinkClicksByCategory, LinkCategoryStat> extractor;

        LinkCategory(Function<LinkClicksByCategory, LinkCategoryStat> extractor) {
            this.extractor = extractor;
        }

        List<LinkStat> linksOf(LinkClicksByCategory byCategory) {
            return Optional.ofNullable(byCategory)
                    .map(extractor)
                    .map(LinkCategoryStat::links)
                    .orElseGet(List::of);
        }
    }

    private void appendLinkClicks(
            StringBuilder csv,
            List<TargetGroupReportData> targetGroups,
            List<BirtReportFigure> figures,
            Locale locale
    ) {
        if (!figures.contains(BirtReportFigure.ACTIVATE_LINK_STATISTICS)) {
            return;
        }

        TargetGroupReportData reference = targetGroups.getFirst();
        if (reference.linkClicks() == null) {
            return;
        }

        boolean hasAnyLinks = Arrays.stream(LinkCategory.values())
                .anyMatch(category -> !category.linksOf(reference.linkClicks()).isEmpty());
        if (!hasAnyLinks) {
            return;
        }

        row(csv, "Click statistics per link");
        emptyRow(csv);

        row(csv, "No.", t("URL", locale), t(KEY_TARGETS, locale), t(KEY_CLICKS, locale),
                t(KEY_CLICKERS, locale), t(KEY_CLICKS, locale) + " %", t(KEY_CLICKERS, locale) + " %");

        int number = 1;
        for (LinkCategory category : LinkCategory.values()) {
            number = appendLinkClicksCategory(
                    csv,
                    targetGroups,
                    category,
                    category.linksOf(reference.linkClicks()),
                    number
            );
        }
        emptyRow(csv);
    }

    private int appendLinkClicksCategory(
            StringBuilder csv,
            List<TargetGroupReportData> targetGroups,
            LinkCategory category,
            List<LinkStat> links,
            int startNumber
    ) {
        int number = startNumber;
        for (LinkStat link : links) {
            for (TargetGroupReportData tg : targetGroups) {
                LinkClickShare total = findLinkTotal(tg, category, link.linkId());
                StatisticMetric clicks = orZero(total == null ? null : total.clicks());
                StatisticMetric clickers = orZero(total == null ? null : total.clickers());

                row(
                        csv,
                        number,
                        link.url(),
                        tg.targetName(),
                        clicks.value(),
                        clickers.value(),
                        clicks.formatRate(),
                        clickers.formatRate()
                );
            }
            number++;
        }
        return number;
    }

    private LinkClickShare findLinkTotal(TargetGroupReportData tg, LinkCategory category, int linkId) {
        if (tg.linkClicks() == null) {
            return null;
        }
        return category.linksOf(tg.linkClicks())
                .stream()
                .filter(l -> l.linkId() == linkId)
                .findFirst()
                .map(l -> l.stats().total())
                .orElse(null);
    }

    // endregion

    // region Bounces

    private record BounceCategory(BirtReportFigure figure, String labelKey,
                                  Function<Bounces, List<BounceReasonStat>> extractor) {
    }

    private static final List<BounceCategory> BOUNCE_CATEGORIES = List.of(
            new BounceCategory(BirtReportFigure.SOFTBOUNCES, KEY_SOFT_BOUNCES, Bounces::softBounces),
            new BounceCategory(BirtReportFigure.HARDBOUNCES, KEY_HARD_BOUNCES, Bounces::hardBounces)
    );

    private void appendBounces(
            StringBuilder csv,
            List<TargetGroupReportData> targetGroups,
            List<BirtReportFigure> figures,
            Locale locale
    ) {
        TargetGroupReportData reference = targetGroups.getFirst();
        if (reference.bounces() == null) {
            return;
        }

        for (BounceCategory category : BOUNCE_CATEGORIES) {
            List<BounceReasonStat> reasons = category.extractor().apply(reference.bounces());
            if (figures.contains(category.figure()) && CollectionUtils.isNotEmpty(reasons)) {
                row(csv, t(category.labelKey(), locale));
                appendBounceReasons(csv, reasons);
                emptyRow(csv);
            }
        }
    }

    private void appendBounceReasons(StringBuilder csv, List<BounceReasonStat> reasons) {
        for (BounceReasonStat reason : reasons) {
            StatisticMetric value = orZero(reason.value());
            row(csv, reason.reason(), value.value(), value.formatRate());
        }
    }

    // endregion

    // region Domains

    private record DomainCategory(String labelKey, Function<DomainStats, List<DomainStat>> extractor) {
    }

    private static final List<DomainCategory> DOMAIN_CATEGORIES = List.of(
            new DomainCategory(KEY_SENT_EMAILS, DomainStats::emailsSent),
            new DomainCategory(KEY_HARD_BOUNCES, DomainStats::hardBounces),
            new DomainCategory(KEY_SOFT_BOUNCES, DomainStats::softBounces),
            new DomainCategory(KEY_OPENERS, DomainStats::openers),
            new DomainCategory(KEY_CLICKERS, DomainStats::clickers)
    );

    private void appendDomains(
            StringBuilder csv,
            List<TargetGroupReportData> targetGroups,
            List<BirtReportFigure> figures,
            Locale locale
    ) {
        if (!figures.contains(BirtReportFigure.TOP_DOMAINS)) {
            return;
        }

        boolean hasAnyDomainData = targetGroups.stream().anyMatch(tg -> tg.domainStats() != null);
        if (!hasAnyDomainData) {
            return;
        }

        row(csv, t("Domains", locale));
        row(csv, "Category", "Domain", t(KEY_TARGETS, locale), "Value", "Percent");

        for (DomainCategory category : DOMAIN_CATEGORIES) {
            for (TargetGroupReportData tg : targetGroups) {
                if (tg.domainStats() == null) {
                    continue;
                }
                for (DomainStat domainStat : category.extractor().apply(tg.domainStats())) {
                    StatisticMetric value = orZero(domainStat.value());
                    row(
                            csv,
                            t(category.labelKey(), locale),
                            domainStat.domain(),
                            tg.targetName(),
                            value.value(),
                            value.formatRate()
                    );
                }
            }
        }
        emptyRow(csv);
    }

    // endregion

    private static String t(String key, Locale locale) {
        return I18nString.t(key, locale);
    }

    private static void row(StringBuilder csv, Object... values) {
        csv.append(CsvWriter.getCsvLine(SEPARATOR, QUOTE, values)).append('\n');
    }

    private static void emptyRow(StringBuilder csv) {
        row(csv);
    }

    private static List<String> targetGroupsHeader(
            String firstCell,
            List<TargetGroupReportData> targetGroups,
            Locale locale
    ) {
        List<String> header = new ArrayList<>();
        header.add(firstCell);
        for (TargetGroupReportData tg : targetGroups) {
            header.add(tg.targetName());
            header.add(t(KEY_SHARE, locale));
        }
        return header;
    }

    private static List<String> blankRow(List<?> header) {
        return header.stream().map(v -> "").toList();
    }

    private static StatisticMetric orZero(StatisticMetric value) {
        return value != null ? value : new StatisticMetric(0, 0);
    }

    private static double nullToZero(Double value) {
        return value == null ? 0 : value;
    }

    private static String nullToEmpty(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private static String formatShare(long value, long total) {
        return StatisticMetric.of(value, total).formatRate();
    }

}
