/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtstatistics.mailing.dto;

import static com.agnitas.util.AgnUtils.calculateRate;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public record MailingSummaryStatsResponse(
        Metadata metadata,
        Data data
) {
    public record Metadata(
            Map<Integer, String> targets,
            Map<KeyFigure, MetricMeta> keyFigures,
            Map<EventDeviceType, MetricMeta> deviceTypes
    ) {
        public record MetricMeta(
                String label,
                String color
        ) {}
    }

    public record Data(
            GeneralInfo generalInfo,
            List<UiTargetReactions> reactions,
            Map<Integer, Map<KeyFigure, KeyFigure.Value>> keyFigures,     // Target ID -> (Metric -> Value)
            Map<Integer, Map<EventDeviceType, EventDeviceType.Value>> openersByDevice,
            Map<Integer, Map<EventDeviceType, EventDeviceType.Value>> clickersByDevice
    ) {
    }

    public record GeneralInfo(
            String subject,
            String mailFormat,
            List<String> targets,
            long sentCount,
            long anonymousCount,
            String sendStart,
            String sendEnd,
            String period,
            String dispatchTime,
            long sizeInKb
    ) {
    }

    public record UiTargetReactions(
            String target,
            String openings,
            String clicks,
            String rate
    ) {
    }

    public enum KeyFigure {
        SENT_EMAILS("statistic.mails.sent", "darkest-blue", false, false),
        DELIVERED_EMAILS("statistic.mails.delivered", "blue", true, false),
        MEASURED_OPENERS("report.opens.measured", "dark-cyan", false, true),
        PROXY_OPENERS("statistic.opener.proxy", "light-cyan", false, false),
        OPENERS_INVISIBLE("report.opens.invisible", "cyan", false, true),
        OPENERS_TOTAL("statistic.opener.total", "darkest-cyan", false, true),
        ANONYMOUS_OPENINGS("statistic.openings.anonym", "light-blue", false, true),
        CLICKERS("statistic.clicker", "dark-yellow", false, true),
        ANONYMOUS_CLICKS("statistic.clicks.anonym", "yellow", false, true),
        OPT_OUTS("statistic.Opt_Outs", "violet", false, false),
        HARD_BOUNCES("statistic.bounces.hardbounce", "dark-red", false, false),
        UNDELIVERABLE("report.softbounces.undeliverable", "light-violet", false, false),
        REVENUE("statistic.revenue", "darkest-violet", false, false);

        private final String msgKey;
        private final String presentationColor;
        private final boolean needSuccessExpirationCheck;
        private final boolean needOnepixelExpirationCheck;

        KeyFigure(
                String msgKey,
                String presentationColor,
                boolean needSuccessExpirationCheck,
                boolean needOnepixelExpirationCheck
        ) {
            this.msgKey = msgKey;
            this.presentationColor = presentationColor;
            this.needSuccessExpirationCheck = needSuccessExpirationCheck;
            this.needOnepixelExpirationCheck = needOnepixelExpirationCheck;
        }

        public String getPresentationColor() {
            return presentationColor;
        }

        public String getMsgKey() {
            return msgKey;
        }

        public boolean isNeedOnepixelExpirationCheck() {
            return needOnepixelExpirationCheck;
        }

        public boolean isNeedSuccessExpirationCheck() {
            return needSuccessExpirationCheck;
        }

        public record Value(
                Number val,
                double rate, // sent base rate
                double deliveredRate,
                Map<KeyFigure, Value> partialValues,
                boolean expired
        ) {
            public Value(Number val, long rateBaseLine, long deliveredRateBaseLine, boolean expired) {
                this(
                        val,
                        rateBaseLine < 0 ? -1 : calculateRate(val, rateBaseLine),
                        calculateRate(val, deliveredRateBaseLine),
                        new EnumMap<>(KeyFigure.class),
                        expired
                );
            }
        }
    }

    public enum EventDeviceType {
        TOTAL("report.form.measured", "darkest-blue"),
        PC("PC", "lime"),
        TABLET("report.device.tablet", "dark-cyan"),
        MOBILE("report.device.mobile", "light-violet"),
        SMART_TV("report.device.smarttv", "dark-red"),
        MULTIPLE("report.device.mixed.4", "blue");

        private final String msgKey;
        private final String presentationColor;

        EventDeviceType(String msgKey, String presentationColor) {
            this.msgKey = msgKey;
            this.presentationColor = presentationColor;
        }

        public String getMsgKey() {
            return msgKey;
        }

        public String getPresentationColor() {
            return presentationColor;
        }

        public record Value(
                Number val,
                double rate
        ) {
            public Value(Number val, long rateBaseLine) {
                this(
                        val,
                        rateBaseLine < 0 ? -1 : calculateRate(val, rateBaseLine)
                );
            }
        }
    }

    public enum CsvCategory {
        SENT_EMAILS("statistic.mails.sent"),
        DELIVERED_EMAILS("statistic.mails.delivered"),
        MEASURED_OPENERS("report.opens.measured"),
        OPENERS_INVISIBLE("report.opens.invisible"),
        OPENERS_TOTAL("statistic.opener.total"),
        CLICKERS("statistic.clicker"),
        OPT_OUTS("statistic.Opt_Outs"),
        HARD_BOUNCES("statistic.bounces.hardbounce"),
        UNDELIVERABLE("report.softbounces.undeliverable"),
        REVENUE("statistic.revenue"),
        OPENERS_PC("statistic.opener.pc"),
        OPENERS_TABLET("statistic.opener.tablet"),
        OPENERS_MOBILE("statistic.opener.mobile"),
        OPENERS_SMART_TV("statistic.opener.smarttv"),
        OPENERS_MULTI("report.opens.pc.and.mobile"),
        CLICKERS_TRACKED("statistic.clicker"),
        CLICKERS_PC("statistic.clicker.pc"),
        CLICKERS_TABLET("statistic.clicker.tablet"),
        CLICKERS_MOBILE("statistic.clicker.mobile"),
        CLICKERS_SMART_TV("statistic.clicker.smarttv"),
        CLICKERS_MULTI("statistic.clicker.multiple"),
        OPENINGS_GROSS("statistic.MeasuredOpeningsGross"),
        CLICKS_GROSS("statistic.Clicks"),
        ACTIVITY_RATE("statistic.ActivityRate"),
        ANONYMOUS_OPENINGS("statistic.openings.anonym"),
        ANONYMOUS_CLICKS("statistic.clicks.anonym"),
        PROXY_OPENERS("statistic.opener.proxy");

        private final String msgKey;

        CsvCategory(String msgKey) {
            this.msgKey = msgKey;
        }

        public String getMsgKey() {
            return msgKey;
        }

        public record Value(
                Number val,
                double gross,
                double net
        ) {
            public Value(Number val, long rateBaseLine, long deliveredRateBaseLine) {
                this(
                        val,
                        rateBaseLine < 0 ? -1 : calculateRate(val, rateBaseLine),
                        rateBaseLine < 0 ? -1 : calculateRate(val, deliveredRateBaseLine)
                );
            }
        }
    }
}
