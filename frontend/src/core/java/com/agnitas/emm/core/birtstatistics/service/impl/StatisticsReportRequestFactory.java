/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.birtstatistics.service.impl;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.agnitas.emm.core.birtreport.bean.ReportFormat;
import com.agnitas.emm.core.birtreport.bean.impl.BirtReportComparisonSettings;
import com.agnitas.emm.core.birtreport.bean.impl.BirtReportDateRangedSettings;
import com.agnitas.emm.core.birtreport.bean.impl.BirtReportMailingSettings;
import com.agnitas.emm.core.birtreport.bean.impl.BirtReportRecipientSettings;
import com.agnitas.emm.core.birtreport.bean.impl.BirtReportSettings;
import com.agnitas.emm.core.birtreport.bean.impl.BirtReportTopDomainsSettings;
import com.agnitas.emm.core.birtreport.dto.ComparisonReportRequest;
import com.agnitas.emm.core.birtreport.dto.MailingReportRequest;
import com.agnitas.emm.core.birtreport.dto.RecipientReportRequest;
import com.agnitas.emm.core.birtreport.dto.StatisticsReportRequest;
import com.agnitas.emm.core.birtreport.dto.TopDomainsReportRequest;
import com.agnitas.emm.core.birtreport.util.BirtReportSettingsUtils;
import com.agnitas.util.DateUtilities;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

final class StatisticsReportRequestFactory {

    private StatisticsReportRequestFactory() {
    }

    static StatisticsReportRequest from(
            BirtReportSettings settings,
            ReportFormat format,
            boolean darkmode,
            String language,
            String reportName
    ) {
        Map<String, String> parameters = settings.getReportUrlParameters();

        return switch (settings) {
            case BirtReportComparisonSettings ignored -> new ComparisonReportRequest(
                    toIntList(parameters.get(BirtReportSettings.TARGET_GROUPS_KEY)),
                    toIntList(parameters.get(BirtReportSettings.MAILINGS_KEY)),
                    parameters.get(BirtReportSettings.FIGURES_KEY),
                    parameters.get(BirtReportSettings.SORT_BY_KEY),
                    toInt(parameters.get(BirtReportSettings.MAILING_FILTER_KEY)),
                    toInt(parameters.get(BirtReportSettings.PREDEFINED_ID_KEY)),
                    format,
                    darkmode,
                    language,
                    reportName
            );
            case BirtReportMailingSettings ignored -> new MailingReportRequest(
                    toIntList(parameters.get(BirtReportSettings.TARGET_GROUPS_KEY)),
                    toIntList(parameters.get(BirtReportSettings.MAILINGS_KEY)),
                    parameters.get(BirtReportSettings.FIGURES_KEY),
                    toStartOfDay(parameters.get(BirtReportSettingsUtils.START_DATE)),
                    toEndOfDayExclusive(parameters.get(BirtReportSettingsUtils.END_DATE)),
                    toInt(parameters.get(BirtReportSettings.MAILING_FILTER_KEY)),
                    toInt(parameters.get(BirtReportSettings.PREDEFINED_ID_KEY)),
                    format,
                    darkmode,
                    language,
                    reportName
            );
            case BirtReportTopDomainsSettings ignored -> new TopDomainsReportRequest(
                    toIntList(parameters.get(BirtReportSettings.TARGET_GROUPS_KEY)),
                    toIntList(parameters.get(BirtReportDateRangedSettings.MAILING_LISTS_KEY)),
                    toStartOfDay(parameters.get(BirtReportSettingsUtils.START_DATE)),
                    toEndOfDayExclusive(parameters.get(BirtReportSettingsUtils.END_DATE)),
                    parameters.get(BirtReportSettings.FIGURES_KEY),
                    toInt(parameters.get(BirtStatisticsServiceImpl.MAX_DOMAINS)),
                    Boolean.parseBoolean(parameters.get(BirtStatisticsServiceImpl.IS_TOP_LEVEL_DOMAIN)),
                    format,
                    darkmode,
                    language,
                    reportName
            );
            case BirtReportRecipientSettings ignored -> new RecipientReportRequest(
                    toIntList(parameters.get(BirtReportSettings.TARGET_GROUPS_KEY)),
                    toIntList(parameters.get(BirtReportDateRangedSettings.MAILING_LISTS_KEY)),
                    toStartOfDay(parameters.get(BirtReportSettingsUtils.START_DATE)),
                    toEndOfDayExclusive(parameters.get(BirtReportSettingsUtils.END_DATE)),
                    parameters.get(BirtReportSettings.FIGURES_KEY),
                    format,
                    darkmode,
                    language,
                    reportName
            );
            default -> throw new IllegalArgumentException("Unsupported report settings type: " + settings.getClass());
        };
    }

    private static List<Integer> toIntList(String expression) {
        if (StringUtils.isBlank(expression)) {
            return Collections.emptyList();
        }

        return Arrays.stream(expression.split(","))
                .map(String::trim)
                .filter(StringUtils::isNotEmpty)
                .map(Integer::valueOf)
                .toList();
    }

    private static Instant toStartOfDay(String date) {
        return StringUtils.isBlank(date)
                ? null
                : parseDate(date).atStartOfDay(ZoneId.systemDefault()).toInstant();
    }

    private static Instant toEndOfDayExclusive(String date) {
        return StringUtils.isBlank(date)
                ? null
                : parseDate(date).plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
    }

    private static LocalDate parseDate(String date) {
        return DateUtilities.parseDate(date, DateTimeFormatter.ofPattern(BirtReportSettingsUtils.REPORT_DATE_FORMAT));
    }

    private static int toInt(String value) {
        return NumberUtils.toInt(value, 0);
    }
}
