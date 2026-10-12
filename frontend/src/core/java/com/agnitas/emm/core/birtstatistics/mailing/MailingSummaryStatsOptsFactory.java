/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.birtstatistics.mailing;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;

import com.agnitas.beans.Admin;
import com.agnitas.beans.Mailing;
import com.agnitas.emm.core.birtstatistics.mailing.dto.MailingStatisticDto;
import com.agnitas.emm.core.birtstatistics.service.MailingStatisticsService;
import com.agnitas.emm.core.commons.dto.DateRange;
import com.agnitas.emm.core.commons.dto.DateTimeRange;
import com.agnitas.emm.core.company.service.CompanyService;
import com.agnitas.emm.core.mailing.service.MailingBaseService;
import org.springframework.stereotype.Component;

@Component
public class MailingSummaryStatsOptsFactory {

    private final MailingStatisticsService mailingStatsService;
    private final MailingBaseService mailingBaseService;
    private final CompanyService companyService;

    public MailingSummaryStatsOptsFactory(
            MailingStatisticsService mailingStatsService,
            MailingBaseService mailingBaseService,
            CompanyService companyService
    ) {
        this.mailingStatsService = mailingStatsService;
        this.mailingBaseService = mailingBaseService;
        this.companyService = companyService;
    }

    public MailingSummaryStatsOpts createOpts(int mailingId, Admin admin, Collection<Integer> targets, DateTimeRange dateTimeRange) {
        Mailing mailing = mailingBaseService.getMailing(admin.getCompanyID(), mailingId);
        boolean periodExists = dateTimeRange != null && dateTimeRange.getFrom() != null;
        LocalDate periodStart = periodExists ? dateTimeRange.getFrom().toLocalDate() : null;

        return MailingSummaryStatsOpts.builder()
                .targets(mailingStatsService.getTargetsWithAllSubscribersTarget(targets, admin))
                .dateRange(DateRange.from(dateTimeRange, admin.getZoneId()))
                .mailing(mailing)
                .mailtrackingActive(companyService.isMailtrackingActive(admin.getCompanyID()))
                .mailingExpired(mailingStatsService.isMailingExpired(mailing.getId(), admin.getCompanyID()))
                .onepixelStatisticExpired(periodExists && mailingStatsService.isOnePixelStatisticExpired(periodStart))
                .successStatisticExpired(periodExists && mailingStatsService.isSuccessStatisticExpired(periodStart))
                .admin(admin)
                .build();
    }

    public MailingSummaryStatsOpts createOpts(int mailingId, Admin admin) {
        return createOpts(mailingId, admin, Collections.emptyList(), null);
    }

    public MailingSummaryStatsOpts createOpts(MailingStatisticDto statDto, Admin admin) {
        return createOpts(statDto.getMailingId(), admin, statDto.selectedTargetsIds(), statDto.getDateTimeRange());
    }
}
