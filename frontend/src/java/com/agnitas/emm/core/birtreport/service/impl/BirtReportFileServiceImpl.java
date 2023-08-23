/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtreport.service.impl;

import com.agnitas.beans.Mailing;
import com.agnitas.emm.core.birtreport.bean.impl.ComBirtReportSettings;
import com.agnitas.emm.core.birtreport.dto.ReportSettingsType;
import com.agnitas.emm.core.birtreport.service.BirtReportFileService;
import com.agnitas.emm.core.mailing.service.MailingService;
import com.agnitas.messages.I18nString;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class BirtReportFileServiceImpl implements BirtReportFileService {

    private final MailingService mailingService;

    public BirtReportFileServiceImpl(MailingService mailingService) {
        this.mailingService = mailingService;
    }

    @Override
    public String buildLocalizedFileName(ComBirtReportSettings settings, int companyId, Locale locale, String formatName) {
        List<String> mailings = settings.getMailings();
        if (ReportSettingsType.MAILING.equals(settings.getReportSettingsType()) && mailings.size() == 1) {
            int mailingId = Integer.parseInt(mailings.get(0));
            return buildFileNameForSingleMailing(mailingId, companyId, locale, formatName);
        }

        return getLocalizedReportName(
                settings.getReportSettingsType(),
                locale,
                formatName);
    }

    private String buildFileNameForSingleMailing(int mailingId, int companyId, Locale locale, String formatName) {
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

        nameBuilder.append(".")
                .append(formatName);

        return nameBuilder.toString();
    }

    private String getLocalizedReportName(ReportSettingsType type, Locale locale, String format) {
        return String.format("%s.%s", getLocalizedString(type.getTypeMsgKey(), locale), format);
    }

    private String getLocalizedString(String messageKey, Locale locale) {
        return I18nString.getLocaleString(messageKey, locale);
    }
}
