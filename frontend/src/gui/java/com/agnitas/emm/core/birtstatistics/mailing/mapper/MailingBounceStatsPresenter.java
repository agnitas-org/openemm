/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtstatistics.mailing.mapper;

import static com.agnitas.messages.I18nString.t;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.birtstatistics.dto.EmailBounceDetail;
import com.agnitas.emm.core.birtstatistics.mailing.forms.MailingStatisticForm;
import com.agnitas.emm.core.birtstatistics.service.MailingBounceStatsService;
import com.agnitas.emm.core.birtstatistics.service.MailingBounceStatsService.BounceStats;
import com.agnitas.emm.core.birtstatistics.service.MailingBounceStatsService.BounceStatsRequest;
import com.agnitas.util.CsvWriter;
import com.agnitas.util.importvalues.Gender;
import org.springframework.stereotype.Component;

@Component
public class MailingBounceStatsPresenter {

    private final MailingBounceStatsService bounceStatsService;
    private final MailingStatsMapper mailingStatsMapper;

    public MailingBounceStatsPresenter(
            MailingBounceStatsService bounceStatsService,
            MailingStatsMapper mailingStatsMapper
    ) {
        this.bounceStatsService = bounceStatsService;
        this.mailingStatsMapper = mailingStatsMapper;
    }

    public BounceStats ui(int mailingId, MailingStatisticForm form, Admin admin) {
        BounceStatsRequest req = mailingStatsMapper.toBounceRequest(mailingId, admin, form);
        return bounceStatsService.getStats(req);
    }

    public byte[] csv(int mailingId, MailingStatisticForm form, Admin admin) throws Exception {
        List<EmailBounceDetail> softBounces = bounceStatsService.getSoftBouncesWithDetailAndEmail(
                mailingId, admin.getCompanyID(), form.getTargetIds()
        );
        List<EmailBounceDetail> hardBounces = bounceStatsService.getHardBouncesWithDetailAndEmail(
                mailingId, admin.getCompanyID(), form.getTargetIds()
        );
        return CsvWriter.csv(Stream.concat(
                Stream.of(Stream.of(
                                "Customer_ID",
                                "Gender",
                                "birt.Firstname",
                                "birt.Lastname",
                                "mailing.MediaType.0",
                                "report.bounce.type",
                                "report.bounce.reason"
                        )
                        .map(key -> t(key, admin.getLocale()))
                        .toList()),
                Stream.concat(
                        bouncesToCsvRows(softBounces, "bounces.softbounce", admin.getLocale()),
                        bouncesToCsvRows(hardBounces, "statistic.bounces.hardbounce", admin.getLocale())
                ).sorted(Comparator.comparing(row -> row.get(6)))
        ).toList());
    }

    private Stream<List<String>> bouncesToCsvRows(List<EmailBounceDetail> bounces, String typeMsgKey, Locale locale) {
        return bounces.stream().map(bounce -> bounceToCsvRow(bounce, typeMsgKey, locale));
    }

    private List<String> bounceToCsvRow(EmailBounceDetail bounce, String typeMsgKey, Locale locale) {
        return Arrays.asList(
                String.valueOf(bounce.customerId()),
                Gender.toLocaleStr(bounce.gender(), locale),
                bounce.firstname(),
                bounce.lastname(),
                bounce.email(),
                t(typeMsgKey, locale),
                t("bounces.detail." + bounce.code(), locale)
        );
    }
}
