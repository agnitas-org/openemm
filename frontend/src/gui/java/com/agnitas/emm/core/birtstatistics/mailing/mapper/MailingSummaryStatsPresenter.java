/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtstatistics.mailing.mapper;

import static com.agnitas.messages.I18nString.t;
import static com.agnitas.util.AgnUtils.calculateRate;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.agnitas.beans.Admin;
import com.agnitas.emm.common.MailingType;
import com.agnitas.emm.core.birtstatistics.mailing.dto.MailingStatisticDto;
import com.agnitas.emm.core.birtstatistics.mailing.dto.MailingSummaryStatsOpts;
import com.agnitas.emm.core.birtstatistics.mailing.dto.MailingSummaryStatsResponse;
import com.agnitas.emm.core.birtstatistics.mailing.dto.MailingSummaryStatsResponse.CsvCategory;
import com.agnitas.emm.core.birtstatistics.mailing.dto.MailingSummaryStatsResponse.Data;
import com.agnitas.emm.core.birtstatistics.mailing.dto.MailingSummaryStatsResponse.EventDeviceType;
import com.agnitas.emm.core.birtstatistics.mailing.dto.MailingSummaryStatsResponse.KeyFigure;
import com.agnitas.emm.core.birtstatistics.mailing.dto.MailingSummaryStatsResponse.KeyFigure.Value;
import com.agnitas.emm.core.birtstatistics.mailing.dto.MailingSummaryStatsResponse.Metadata;
import com.agnitas.emm.core.birtstatistics.mailing.dto.MailingSummaryStatsResponse.Metadata.MetricMeta;
import com.agnitas.emm.core.birtstatistics.mailing.dto.MailingSummaryStatsResponse.UiTargetReactions;
import com.agnitas.emm.core.birtstatistics.mailing.service.MailingSummaryStatsService;
import com.agnitas.emm.core.birtstatistics.mailing.service.MailingSummaryStatsService.ByDevice;
import com.agnitas.emm.core.birtstatistics.mailing.service.MailingSummaryStatsService.Stats;
import com.agnitas.emm.core.birtstatistics.service.MailingStatisticsService;
import com.agnitas.emm.core.company.service.CompanyService;
import com.agnitas.emm.core.mailing.service.MailingBaseService;
import com.agnitas.reporting.birt.external.beans.LightTarget;
import com.agnitas.util.CsvWriter;
import org.apache.commons.collections4.ListUtils;
import org.springframework.stereotype.Component;

@Component
public class MailingSummaryStatsPresenter {

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.00%");

    private final MailingSummaryStatsService summaryStatsService;
    private final MailingStatisticsService mailingStatsService;
    private final MailingBaseService mailingBaseService;
    private final CompanyService companyService;

    public MailingSummaryStatsPresenter(MailingSummaryStatsService summaryStatsService,
                                        MailingStatisticsService mailingStatsService,
                                        MailingBaseService mailingBaseService,
                                        CompanyService companyService) {
        this.summaryStatsService = summaryStatsService;
        this.mailingStatsService = mailingStatsService;
        this.mailingBaseService = mailingBaseService;
        this.companyService = companyService;
    }

    private MailingSummaryStatsOpts toOptions(MailingStatisticDto statDto, Admin admin) {
        boolean periodExists = statDto.isDateRangeExists();
        LocalDate periodStart = periodExists ? statDto.getDateTimeRange().getFrom().toLocalDate() : null;

        return MailingSummaryStatsOpts.builder()
                .targets(mailingStatsService.getTargetsWithAllSubscribersTarget(statDto.selectedTargetsIds(), admin))
                .dateRange(statDto.getDateRange(admin.getZoneId()))
                .mailing(mailingBaseService.getMailing(admin.getCompanyID(), statDto.getMailingId()))
                .mailtrackingActive(companyService.isMailtrackingActive(admin.getCompanyID()))
                .mailtrackingActive(mailingStatsService.isTrackingExists(statDto.getMailingId(), admin.getCompanyID()))
                .mailingExpired(mailingStatsService.isMailingExpired(statDto.getMailingId(), admin.getCompanyID()))
                .onepixelStatisticExpired(periodExists && mailingStatsService.isOnePixelStatisticExpired(periodStart))
                .successStatisticExpired(periodExists && mailingStatsService.isSuccessStatisticExpired(periodStart))
                .admin(admin)
                .build();
    }

    public MailingSummaryStatsResponse ui(MailingStatisticDto statDto, Admin admin) {
        MailingSummaryStatsOpts opts = toOptions(statDto, admin);
        return new MailingSummaryStatsResponse(
                getMetadata(opts),
                getData(opts)
        );
    }

    private static Metadata getMetadata(MailingSummaryStatsOpts opts) {
        Map<Integer, String> targetNames = opts.getTargets().stream().collect(Collectors.toMap(
                LightTarget::getId,
                LightTarget::getName, (a, b) -> a, LinkedHashMap::new
        ));
        Map<KeyFigure, MetricMeta> keyFigures = Arrays.stream(KeyFigure.values()).collect(Collectors.toMap(
                f -> f,
                f -> new MetricMeta(t(f.getMsgKey(), opts.getLocale()), f.getPresentationColor()),
                (a, b) -> a,
                LinkedHashMap::new
        ));
        Map<EventDeviceType, MetricMeta> deviceTypes = Arrays.stream(EventDeviceType.values()).collect(Collectors.toMap(
                f -> f,
                f -> new MetricMeta(t(f.getMsgKey(), opts.getLocale()), f.getPresentationColor()),
                (a, b) -> a,
                LinkedHashMap::new
        ));
        return new Metadata(targetNames, keyFigures, deviceTypes);
    }

    private Data getData(MailingSummaryStatsOpts opts) {
        Map<Integer, Stats> stats = summaryStatsService.getStats(opts);
        return new Data(
                summaryStatsService.getGeneralInfo(opts),
                getReactionsSummary(stats, opts),
                getKeyFigures(stats, opts),
                getByDeviceType(stats, s -> s.openers().byDevice(), s -> s.openers().measured()),
                getByDeviceType(stats, s -> s.clickers().byDevice(), s -> s.clickers().total())
        );
    }

    private List<UiTargetReactions> getReactionsSummary(Map<Integer, Stats> stats, MailingSummaryStatsOpts opts) {
        String naMsg = t("NotAvailableShort", opts.getLocale());
        return stats.entrySet().stream()
                .map(e -> {
                    long openings = e.getValue().openingsGross();
                    long clicks = e.getValue().clicks();
                    return new UiTargetReactions(
                            opts.getTargetName(e.getKey()),
                            valueToString(openings, naMsg),
                            valueToString(clicks, naMsg),
                            rateToString(openings, clicks)
                    );
                }).toList();
    }

    private String valueToString(long value, String naMsg) {
        return value < 0 ? naMsg : String.valueOf(value);
    }

    private String rateToString(long openings, long clicks) {
        return DECIMAL_FORMAT.format(openings <= 0 ? 0 : calculateRate(clicks, openings) / 100.0);
    }

    public Map<Integer, Map<KeyFigure, Value>> getKeyFigures(Map<Integer, Stats> stats, MailingSummaryStatsOpts opts) {
        return stats
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> mapKeyFiguresForUi(e.getValue(), opts)
                ));
    }

    public static Map<KeyFigure, Value> mapKeyFiguresForUi(Stats stat, MailingSummaryStatsOpts opts) {
        long grossBase = stat.sentEmails();
        long netBase = stat.deliveredEmails();
        Map<KeyFigure, Value> values = new LinkedHashMap<>();

        for (KeyFigure type : KeyFigure.values()) {
            boolean isExpired = isExpired(type, opts);
            if (type == KeyFigure.UNDELIVERABLE && opts.getMailing().getMailingType() != MailingType.NORMAL) {
                continue;
            }
            Value val = switch (type) {
                case SENT_EMAILS -> new Value(stat.sentEmails(), grossBase, grossBase, isExpired);
                case DELIVERED_EMAILS -> new Value(stat.deliveredEmails(), grossBase, grossBase, isExpired);
                case MEASURED_OPENERS -> new Value(stat.openers().measured(), grossBase, netBase, isExpired);
                case PROXY_OPENERS -> new Value(stat.openers().proxy(), grossBase, netBase, isExpired);
                case OPENERS_INVISIBLE -> new Value(stat.openers().invisible(), grossBase, netBase, isExpired);
                case OPENERS_TOTAL -> new Value(stat.openers().total(), grossBase, netBase, isExpired);
                case ANONYMOUS_OPENINGS -> new Value(stat.anonymousOpenings(), grossBase, netBase, isExpired);
                case CLICKERS -> new Value(stat.clickers().total(), grossBase, netBase, isExpired);
                case ANONYMOUS_CLICKS -> new Value(stat.anonymousClicks(), grossBase, netBase, isExpired);
                case OPT_OUTS -> new Value(stat.optOuts(), grossBase, netBase, isExpired);
                case HARD_BOUNCES -> new Value(stat.hardBounces(), grossBase, netBase, isExpired);
                case UNDELIVERABLE -> new Value(stat.undelivered(), grossBase, netBase, isExpired);
                case REVENUE -> new Value(round(stat), -1, -1, isExpired);
            };
            if (type == KeyFigure.PROXY_OPENERS) {
                values.get(KeyFigure.MEASURED_OPENERS).partialValues().put(KeyFigure.PROXY_OPENERS, val);
            } else {
                values.put(type, val);
            }
        }
        return values;
    }

    private static double round(Stats stat) {
        return Math.round(stat.revenue() * 100.0) / 100.0;
    }

    private static boolean isExpired(KeyFigure keyFigure, MailingSummaryStatsOpts opts) {
        return (opts.isSuccessStatisticExpired() && keyFigure.isNeedSuccessExpirationCheck())
               || (opts.isOnepixelStatisticExpired() && keyFigure.isNeedOnepixelExpirationCheck());
    }

    private Map<Integer, Map<EventDeviceType, EventDeviceType.Value>> getByDeviceType(
            Map<Integer, Stats> stats,
            Function<Stats, ByDevice> byDevice,
            ToLongFunction<Stats> total
    ) {
        return stats.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> mapDeviceStatsForUi(byDevice.apply(e.getValue()), total.applyAsLong(e.getValue()))
                ));
    }

    public static Map<EventDeviceType, EventDeviceType.Value> mapDeviceStatsForUi(ByDevice byDevice, long total) {
        Map<EventDeviceType, EventDeviceType.Value> values = new LinkedHashMap<>();
        for (EventDeviceType type : EventDeviceType.values()) {
            EventDeviceType.Value val = switch (type) {
                case TOTAL -> new EventDeviceType.Value(total, total);
                case PC -> new EventDeviceType.Value(byDevice.pc(), total);
                case TABLET -> new EventDeviceType.Value(byDevice.tabled(), total);
                case MOBILE -> new EventDeviceType.Value(byDevice.mobile(), total);
                case SMART_TV -> new EventDeviceType.Value(byDevice.smartTv(), total);
                case MULTIPLE -> new EventDeviceType.Value(byDevice.multi(), total);
            };
            values.put(type, val);
        }
        return values;
    }

    public byte[] csv(MailingStatisticDto statDto, Admin admin) throws Exception {
        MailingSummaryStatsOpts opts = toOptions(statDto, admin);
        return CsvWriter.csv(ListUtils.union(
                List.of(getHeadersForCsv(admin.getLocale())),
                getCsvRows(summaryStatsService.getStats(opts), opts))
        );
    }

    private static List<String> getHeadersForCsv(Locale locale) {
        return Stream.of("birt.Target", "grid.mediapool.category", "value", "Brutto", "Netto")
                .map(key -> t(key, locale))
                .toList();
    }

    private List<List<String>> getCsvRows(Map<Integer, Stats> targetStatsMap, MailingSummaryStatsOpts opts) {
        return Arrays.stream(CsvCategory.values())
                .flatMap(category -> targetStatsMap.entrySet().stream()
                        .filter(entry -> isAllowedCsvRow(category, entry.getKey()))
                        .map(entry -> getCsvRow(
                                category,
                                entry.getKey(),
                                mapDeviceStatsForCsv(entry.getValue()).get(category),
                                opts
                        )))
                .toList();
    }

    private static boolean isAllowedCsvRow(CsvCategory category, int targetId) {
        return (category != CsvCategory.ANONYMOUS_OPENINGS && category != CsvCategory.ANONYMOUS_CLICKS)
                || targetId == MailingStatisticsService.ALL_SUBSCRIBERS_TARGETGROUP_ID;
    }

    private static List<String> getCsvRow(
            CsvCategory category,
            Integer targetId,
            CsvCategory.Value data,
            MailingSummaryStatsOpts opts
    ) {
        return List.of(
                opts.getTargetName(targetId),
                t(category.getMsgKey(), opts.getLocale()),
                formatCsvValue(data.val()),
                DECIMAL_FORMAT.format(Math.max(0, data.gross() / 100.0)),
                DECIMAL_FORMAT.format(Math.max(0, data.net() / 100.0))
        );
    }

    private static String formatCsvValue(Number value) {
        return value == null || value.longValue() <= 0
                ? "0"
                : BigDecimal.valueOf(value.doubleValue())
                .stripTrailingZeros()
                .toPlainString();
    }

    public static Map<CsvCategory, CsvCategory.Value> mapDeviceStatsForCsv(Stats stats) {
        Map<CsvCategory, CsvCategory.Value> values = new EnumMap<>(CsvCategory.class);
        long grossBase = stats.sentEmails();
        long netBase = stats.deliveredEmails();

        for (CsvCategory type : CsvCategory.values()) {
            CsvCategory.Value val = switch (type) {
                case SENT_EMAILS -> new CsvCategory.Value(stats.sentEmails(), grossBase, grossBase);
                case DELIVERED_EMAILS -> new CsvCategory.Value(stats.deliveredEmails(), grossBase, grossBase);
                case MEASURED_OPENERS -> new CsvCategory.Value(stats.openers().measured(), grossBase, netBase);
                case OPENERS_INVISIBLE -> new CsvCategory.Value(stats.openers().invisible(), grossBase, netBase);
                case OPENERS_TOTAL -> new CsvCategory.Value(stats.openers().total(), grossBase, netBase);
                case CLICKERS -> new CsvCategory.Value(stats.clickers().total(), grossBase, netBase);
                case OPT_OUTS -> new CsvCategory.Value(stats.optOuts(), grossBase, netBase);
                case HARD_BOUNCES -> new CsvCategory.Value(stats.hardBounces(), grossBase, netBase);
                case UNDELIVERABLE -> new CsvCategory.Value(stats.undelivered(), grossBase, netBase);
                case REVENUE -> new CsvCategory.Value(stats.revenue() * 100, grossBase, netBase);
                case OPENERS_PC -> new CsvCategory.Value(stats.openers().byDevice().pc(), stats.openers().measured(), netBase);
                case OPENERS_TABLET -> new CsvCategory.Value(stats.openers().byDevice().tabled(), stats.openers().measured(), netBase);
                case OPENERS_MOBILE -> new CsvCategory.Value(stats.openers().byDevice().mobile(), stats.openers().measured(), netBase);
                case OPENERS_SMART_TV -> new CsvCategory.Value(stats.openers().byDevice().smartTv(), stats.openers().measured(), netBase);
                case OPENERS_MULTI -> new CsvCategory.Value(stats.openers().byDevice().multi(), stats.openers().measured(), netBase);
                case CLICKERS_TRACKED -> new CsvCategory.Value(stats.clickers().total(), stats.clickers().total(), netBase);
                case CLICKERS_PC -> new CsvCategory.Value(stats.clickers().byDevice().pc(), stats.clickers().total(), netBase);
                case CLICKERS_TABLET -> new CsvCategory.Value(stats.clickers().byDevice().tabled(), stats.clickers().total(), netBase);
                case CLICKERS_MOBILE -> new CsvCategory.Value(stats.clickers().byDevice().mobile(), stats.clickers().total(), netBase);
                case CLICKERS_SMART_TV -> new CsvCategory.Value(stats.clickers().byDevice().smartTv(), stats.clickers().total(), netBase);
                case CLICKERS_MULTI -> new CsvCategory.Value(stats.clickers().byDevice().multi(), stats.clickers().total(), netBase);
                case OPENINGS_GROSS -> new CsvCategory.Value(stats.openingsGross(), -1, -1);
                case CLICKS_GROSS -> new CsvCategory.Value(stats.clicks(), -1, -1);
                case ACTIVITY_RATE -> new CsvCategory.Value(0, stats.activityRate() * 100, 0);
                case ANONYMOUS_OPENINGS -> new CsvCategory.Value(stats.anonymousOpenings(), -1, -1);
                case ANONYMOUS_CLICKS -> new CsvCategory.Value(stats.anonymousClicks(), -1, -1);
                case PROXY_OPENERS -> new CsvCategory.Value(stats.openers().proxy(), -1, -1);
            };
            values.put(type, val);
        }
        return values;
    }
}
