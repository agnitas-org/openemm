/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.birtstatistics.mailing;

import static com.agnitas.util.AgnUtils.calculateRate;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.commons.dto.DateRange;
import com.agnitas.reporting.birt.external.beans.LightTarget;

public interface MailingSummaryStatsService {

    record Stats(
            long sentEmails,
            Long deliveredEmails,
            long openingsGross,
            long anonymousOpenings,
            Openers openers,
            long clicks,
            int anonymousClicks,
            Clickers clickers,
            int optOuts,
            int hardBounces,
            long undelivered,
            double revenue,
            double activityRate
    ) {
    }

    record Openers(
            long measured,
            long proxy,
            long invisible,
            long total,
            ByDevice byDevice
    ) {}

    record Clickers(
            long total,
            ByDevice byDevice
    ) {}

    record ByDevice(
            long pc,
            long tablet,
            long mobile,
            long smartTv,
            long multi
    ) {
    }

    record GeneralInfo(
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

    record DispatchStats(
            long sentCount,
            long anonymousRecipientsCount,
            Date sendingStarted,
            Date sendingFinished,
            long sizeInKb
    ) {
    }

    enum CsvCategory {
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


    byte[] csv(int mailingId, Admin admin) throws Exception;

    byte[] csv(MailingSummaryStatsOpts opts, Admin admin) throws Exception;

    GeneralInfo getGeneralInfo(MailingSummaryStatsOpts opts);

    DispatchStats getDispatchStats(int mailingId, int companyId, DateRange dateRange);

    // target -> metrics
    Map<Integer, Stats> getStats(MailingSummaryStatsOpts opts);

    Stats getStats(LightTarget target, MailingSummaryStatsOpts opts);
}
