/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.components.service.impl;

import com.agnitas.beans.Admin;
import com.agnitas.beans.Mailing;
import com.agnitas.emm.core.birtreport.bean.BirtReportFactory;
import com.agnitas.emm.core.birtreport.bean.BirtReport;
import com.agnitas.emm.core.birtreport.bean.impl.BirtReportMailingSettings;
import com.agnitas.emm.core.birtreport.bean.impl.BirtReportSettings;
import com.agnitas.emm.core.birtreport.dto.BirtReportType;
import com.agnitas.emm.core.birtreport.dto.FilterType;
import com.agnitas.emm.core.birtreport.dto.PeriodType;
import com.agnitas.emm.core.birtreport.dto.PredefinedType;
import com.agnitas.emm.core.birtreport.dto.ReportSettingsType;
import com.agnitas.emm.core.birtreport.service.BirtReportService;
import com.agnitas.emm.core.birtreport.util.BirtReportSettingsUtils;
import com.agnitas.emm.core.components.service.MailingReportScheduleService;
import com.agnitas.messages.I18nString;
import com.agnitas.util.DateUtilities;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static com.agnitas.emm.core.birtreport.bean.impl.BirtReportSettings.SORT_NAME;
import static com.agnitas.emm.core.birtreport.util.BirtReportSettingsUtils.GENERAL_INFO_GROUP;
import static com.agnitas.emm.core.birtreport.util.BirtReportSettingsUtils.Properties.CONVERSION_RATE;
import static com.agnitas.emm.core.birtreport.util.BirtReportSettingsUtils.mailingStatisticProps;

public class MailingReportScheduleServiceImpl implements MailingReportScheduleService {

    private static final Logger logger = LogManager.getLogger(MailingReportScheduleServiceImpl.class);
    private static final List<BirtReportType> ALLOWED_REPORT_TYPES = List.of(
            BirtReportType.TYPE_AFTER_MAILING_24HOURS,
            BirtReportType.TYPE_AFTER_MAILING_48HOURS,
            BirtReportType.TYPE_AFTER_MAILING_WEEK
    );

    private final BirtReportFactory birtReportFactory;
    private final BirtReportService birtReportService;

    @Autowired
    public MailingReportScheduleServiceImpl(BirtReportFactory birtReportFactory, BirtReportService birtReportService) {
        this.birtReportFactory = birtReportFactory;
        this.birtReportService = birtReportService;
    }

    /**
     * Creates and setup report with mailing type @see {@link ReportSettingsType#MAILING}
     * <p>
     * report settings don't consider because:
     * "send_date" and "end_date" are not used by {@link BirtReport#isTriggeredByMailing()} reports
     * "creation_date" is set by DB
     * "activation_date" is set when saving activated report
     * <p>
     * Report settings should contains 'predefineMailing' @see {@link BirtReportSettings#PREDEFINED_ID_KEY} prop to be included in send report's list
     * Keep in mind, 'selectedMailings' prop will be overwritten by @see {@link BirtReportService#getReportsToSend(int, List, List)}
     */
    @Override
    public void scheduleNewReport(List<String> emails, Admin admin, Mailing mailing, Date sendDate, BirtReportType reportType) {
        // Create the report itself
        BirtReport report = birtReportFactory.createReport();

        Date mailingSendDate;
        if (sendDate != null) {
            mailingSendDate = sendDate;
        } else if (mailing.getPlanDate() != null) {
            mailingSendDate = mailing.getPlanDate();
        } else {
            mailingSendDate = new Date();
        }

        Date reportEndDate = null;


        boolean isAllowedReportType = ALLOWED_REPORT_TYPES.stream()
                .anyMatch(t -> t.equals(reportType));

        if (isAllowedReportType) {
            reportEndDate = DateUtilities.addDaysToDate(mailingSendDate, 15);
        } else {
            String allowedTypes = ALLOWED_REPORT_TYPES.stream()
                    .map(Enum::toString)
                    .collect(Collectors.joining(", "));

            logger.error(MessageFormat.format("Invalid report type. Report type must be: {0} but was {1}", allowedTypes, reportType));
        }

        setupReportData(report, reportType, reportEndDate, mailing, admin, emails);
        setupMailingReportSettings(report.getReportMailingSettings(), mailing.getId());

        birtReportService.insert(report);
    }

    private void setupReportData(BirtReport report, BirtReportType reportType, Date endDate, Mailing mailing,
                                 Admin admin, List<String> emails) {
        report.setReportType(reportType.getKey());
        report.setActiveTab(ReportSettingsType.MAILING.getKey()); // Used by UI only?
        report.setLanguage(admin.getAdminLang());
        report.setHidden(true);

        report.setCompanyID(admin.getCompanyID());
        report.setShortname(buildReportName(reportType, mailing, admin.getLocale()));
        report.setReportActive(1);
        report.setFormat(BirtReport.FORMAT_PDF_INDEX);
        report.setEmailRecipientList(emails);
        report.setEmailSubject(buildReportEmailSubject(admin.getLocale()));
        report.setEmailDescription(buildReportEmailBody(mailing, admin.getLocale()));

        if (endDate != null) {
            report.setEndDate(endDate);
        }
    }

    private void setupMailingReportSettings(BirtReportMailingSettings reportMailingSettings, int mailingId) {
        // Create report parameters
        List<BirtReportSettingsUtils.Properties> figures = new ArrayList<>();
        figures.addAll(mailingStatisticProps.get(GENERAL_INFO_GROUP));
        figures.addAll(BirtReportSettingsUtils.MAILING_FORMATS_GROUP);
        figures.addAll(BirtReportSettingsUtils.MAILING_OPENER_GROUP);
        figures.addAll(BirtReportSettingsUtils.MAILING_GENERAL_GROUP);
        figures.addAll(BirtReportSettingsUtils.MAILING_DEVICES_GROUP);

        figures.forEach(figure -> {
            boolean isActive = figure != CONVERSION_RATE;
            reportMailingSettings.setReportSetting(figure.getPropName(), isActive);
        });

        reportMailingSettings.setReportSetting(BirtReportSettings.ENABLED_KEY, true);
        reportMailingSettings.setReportSetting(BirtReportSettings.MAILING_FILTER_KEY, FilterType.FILTER_MAILING.getKey());
        reportMailingSettings.setReportSetting(BirtReportMailingSettings.MAILING_GENERAL_TYPES_KEY, BirtReportMailingSettings.MAILING_NORMAL);
        reportMailingSettings.setReportSetting(BirtReportSettings.MAILING_TYPE_KEY, BirtReportSettingsUtils.MAILINGS_CUSTOM);
        reportMailingSettings.setReportSetting(BirtReportMailingSettings.PERIOD_TYPE_KEY, PeriodType.DATE_RANGE_WEEK.getKey());
        reportMailingSettings.setReportSetting(BirtReportSettings.PREDEFINED_MAILINGS_KEY, PredefinedType.PREDEFINED_LAST_ONE.getValue());

        reportMailingSettings.setReportSetting(BirtReportSettings.PREDEFINED_ID_KEY, mailingId);
        reportMailingSettings.setReportSetting(BirtReportSettings.MAILINGS_KEY, Integer.toString(mailingId));
        reportMailingSettings.setReportSetting(BirtReportSettings.TARGETS_KEY, "");
        reportMailingSettings.setReportSetting(BirtReportSettings.SORT_MAILINGS_KEY, SORT_NAME);
    }

    private String buildReportName(BirtReportType reportType, Mailing mailing, Locale locale) {
        final String reportTypeMessageKey = reportTypeToMessageKey(reportType);
        final String reportTypeString = (reportTypeMessageKey != null) ? I18nString.getLocaleString(reportTypeMessageKey, locale) : "";

        return I18nString.getLocaleString("report.mailing.afterSending.title", locale, reportTypeString, mailing.getId(), mailing.getShortname());
    }

    private String reportTypeToMessageKey(BirtReportType reportType) {
        switch (reportType) {
            case TYPE_AFTER_MAILING_24HOURS:
                return "report.after.mailing.24hours";

            case TYPE_AFTER_MAILING_48HOURS:
                return "report.after.mailing.48hours";

            case TYPE_AFTER_MAILING_WEEK:
                return "report.after.mailing.week";

            default:
                return null;
        }
    }

    private static String buildReportEmailSubject(Locale locale) {
        return I18nString.getLocaleString("report.after.mailing.emailSubject", locale);
    }

    private static String buildReportEmailBody(Mailing mailing, Locale locale) {
        return I18nString.getLocaleString("report.after.mailing.emailBody", locale, mailing.getShortname());
    }
}
