/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.birtstatistics.mailing.mapper;

import static com.agnitas.messages.I18nString.t;
import static com.agnitas.util.AgnUtils.calculateRate;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;

import com.agnitas.beans.Admin;
import com.agnitas.emm.common.MailingType;
import com.agnitas.emm.core.birtstatistics.mailing.MailingSummaryStatsOpts;
import com.agnitas.emm.core.birtstatistics.mailing.MailingSummaryStatsOptsFactory;
import com.agnitas.emm.core.birtstatistics.mailing.MailingSummaryStatsService;
import com.agnitas.emm.core.birtstatistics.mailing.MailingSummaryStatsService.ByDevice;
import com.agnitas.emm.core.birtstatistics.mailing.MailingSummaryStatsService.Stats;
import com.agnitas.emm.core.birtstatistics.mailing.dto.MailingStatisticDto;
import com.agnitas.emm.core.birtstatistics.mailing.dto.MailingSummaryStatsResponse;
import com.agnitas.emm.core.birtstatistics.mailing.dto.MailingSummaryStatsResponse.Data;
import com.agnitas.emm.core.birtstatistics.mailing.dto.MailingSummaryStatsResponse.EventDeviceType;
import com.agnitas.emm.core.birtstatistics.mailing.dto.MailingSummaryStatsResponse.KeyFigure;
import com.agnitas.emm.core.birtstatistics.mailing.dto.MailingSummaryStatsResponse.KeyFigure.Value;
import com.agnitas.emm.core.birtstatistics.mailing.dto.MailingSummaryStatsResponse.Metadata;
import com.agnitas.emm.core.birtstatistics.mailing.dto.MailingSummaryStatsResponse.Metadata.MetricMeta;
import com.agnitas.emm.core.birtstatistics.mailing.dto.MailingSummaryStatsResponse.UiTargetReactions;
import com.agnitas.reporting.birt.external.beans.LightTarget;
import org.springframework.stereotype.Component;

@Component
public class MailingSummaryStatsUiService {

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.00%");

    private final MailingSummaryStatsOptsFactory statsOptsFactory;
    private final MailingSummaryStatsService summaryStatsService;

    public MailingSummaryStatsUiService(
            MailingSummaryStatsOptsFactory statsOptsFactory,
            MailingSummaryStatsService summaryStatsService
    ) {
        this.statsOptsFactory = statsOptsFactory;
        this.summaryStatsService = summaryStatsService;
    }

    public MailingSummaryStatsResponse ui(MailingStatisticDto statDto, Admin admin) {
        MailingSummaryStatsOpts opts = statsOptsFactory.createOpts(statDto, admin);
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
        long netBase = stat.deliveredEmails() == null ? 0: stat.deliveredEmails();
        Map<KeyFigure, Value> values = new LinkedHashMap<>();

        for (KeyFigure type : KeyFigure.values()) {
            boolean isExpired = isExpired(type, opts);
            if (type == KeyFigure.UNDELIVERABLE && skipUndeliverable(opts)
                || type == KeyFigure.DELIVERED_EMAILS && stat.deliveredEmails() == null) {
                continue;
            }
            Value val = switch (type) {
                case SENT_EMAILS -> new Value(stat.sentEmails(), grossBase, stat.deliveredEmails() == null ? 0 : grossBase, isExpired);
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

    private static boolean skipUndeliverable(MailingSummaryStatsOpts opts) {
        return opts.getMailing().getMailingType() != MailingType.NORMAL
                || !opts.isMailtrackingActive()
                || opts.isMailingExpired();
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
                case TABLET -> new EventDeviceType.Value(byDevice.tablet(), total);
                case MOBILE -> new EventDeviceType.Value(byDevice.mobile(), total);
                case SMART_TV -> new EventDeviceType.Value(byDevice.smartTv(), total);
                case MULTIPLE -> new EventDeviceType.Value(byDevice.multi(), total);
            };
            values.put(type, val);
        }
        return values;
    }

    public byte[] csv(MailingStatisticDto statDto, Admin admin) throws Exception {
        MailingSummaryStatsOpts opts = statsOptsFactory.createOpts(statDto, admin);
        return summaryStatsService.csv(opts, admin);
    }
}
