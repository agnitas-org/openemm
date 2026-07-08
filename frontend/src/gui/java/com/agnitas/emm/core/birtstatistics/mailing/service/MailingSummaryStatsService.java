/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtstatistics.mailing.service;

import java.util.Map;

import com.agnitas.emm.core.birtstatistics.mailing.dto.MailingSummaryStatsOpts;
import com.agnitas.emm.core.birtstatistics.mailing.dto.MailingSummaryStatsResponse;

public interface MailingSummaryStatsService {

    record Stats(
            long sentEmails,
            long deliveredEmails,
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
            long tabled,
            long mobile,
            long smartTv,
            long multi
    ) {
    }

    MailingSummaryStatsResponse.GeneralInfo getGeneralInfo(MailingSummaryStatsOpts opts);

    // target -> metrics
    Map<Integer, Stats> getStats(MailingSummaryStatsOpts opts);
}
