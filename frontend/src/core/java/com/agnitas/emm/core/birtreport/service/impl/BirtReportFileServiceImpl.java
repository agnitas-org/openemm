/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.birtreport.service.impl;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import com.agnitas.beans.Mailing;
import com.agnitas.emm.core.birtreport.bean.impl.BirtReportSettings;
import com.agnitas.emm.core.birtreport.dto.ReportSettingsType;
import com.agnitas.emm.core.birtreport.service.BirtReportFileService;
import com.agnitas.emm.core.birtreport.bean.ReportFormat;
import com.agnitas.emm.core.mailing.service.MailingService;
import com.agnitas.messages.I18nString;

public class BirtReportFileServiceImpl implements BirtReportFileService {

    private final MailingService mailingService;

    public BirtReportFileServiceImpl(MailingService mailingService) {
        this.mailingService = mailingService;
    }

    @Override
    public String buildLocalizedFileName(BirtReportSettings settings, int companyId, Locale locale, ReportFormat format) {
        List<String> mailings = settings.getMailings();
        if (ReportSettingsType.MAILING.equals(settings.getReportSettingsType()) && mailings.size() == 1) {
            int mailingId = Integer.parseInt(mailings.getFirst());
            if (mailingService.exists(mailingId, companyId)) {
                return buildFileNameForSingleMailing(mailingId, companyId, locale, format);
            }
        }

        return getLocalizedReportName(
                settings.getReportSettingsType(),
                locale,
                format
        );
    }

    private String buildFileNameForSingleMailing(int mailingId, int companyId, Locale locale, ReportFormat format) {
        Mailing mailing = mailingService.getMailing(companyId, mailingId);

        StringBuilder nameBuilder = new StringBuilder()
                .append(getLocalizedString("Mailing", locale))
                .append("_")
                .append(mailing.getShortname());

        if (mailing.getSenddate() != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", locale);
            String sendDateStr = dateFormat.format(mailing.getSenddate());

            nameBuilder.append("_")
                    .append(sendDateStr);
        }

        nameBuilder.append(".").append(format.name().toLowerCase());

        return nameBuilder.toString();
    }

    private String getLocalizedReportName(ReportSettingsType type, Locale locale, ReportFormat format) {
        return String.format("%s.%s", getLocalizedString(type.getTypeMsgKey(), locale), format.name().toLowerCase());
    }

    private String getLocalizedString(String messageKey, Locale locale) {
        return I18nString.getLocaleString(messageKey, locale);
    }
}
