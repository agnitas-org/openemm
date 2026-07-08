/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtstatistics.service.impl;

import static com.agnitas.messages.I18nString.getLocaleString;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import com.agnitas.beans.Admin;
import com.agnitas.emm.common.MailingType;
import com.agnitas.emm.core.birtstatistics.dao.MonthlyStatisticsDao;
import com.agnitas.emm.core.birtstatistics.dao.MonthlyStatisticsDao.MaildropStatusSummary;
import com.agnitas.emm.core.birtstatistics.dto.MonthlyMailingAmountStatistics;
import com.agnitas.emm.core.birtstatistics.dto.MonthlyMailingDetailStatistics;
import com.agnitas.emm.core.birtstatistics.monthly.dto.MonthlyOverviewResponseData;
import com.agnitas.emm.core.birtstatistics.monthly.dto.MonthlyOverviewResponseData.MailingTypeRow;
import com.agnitas.emm.core.birtstatistics.monthly.form.MonthlyStatisticForm;
import com.agnitas.emm.core.birtstatistics.service.MonthlyStatisticsService;
import com.agnitas.emm.core.commons.dto.DateRange;
import com.agnitas.emm.core.maildrop.MaildropStatus;
import com.agnitas.util.CsvWriter;
import org.apache.commons.collections4.ListUtils;
import org.springframework.stereotype.Service;

@Service
public class MonthlyStatisticsServiceImpl implements MonthlyStatisticsService {

    private static final int ROWS_LIMIT = 10;

    private static final Map<MailingType, MaildropStatus> TYPE_TO_STATUS_MAP = new LinkedHashMap<>();
    static {
        TYPE_TO_STATUS_MAP.put(MailingType.NORMAL, MaildropStatus.WORLD);
        TYPE_TO_STATUS_MAP.put(MailingType.DATE_BASED, MaildropStatus.DATE_BASED);
        TYPE_TO_STATUS_MAP.put(MailingType.ACTION_BASED, MaildropStatus.ACTION_BASED);
        TYPE_TO_STATUS_MAP.put(MailingType.INTERVAL, MaildropStatus.ON_DEMAND);
    }

    private final MonthlyStatisticsDao statisticsDao;

    public MonthlyStatisticsServiceImpl(MonthlyStatisticsDao statisticsDao) {
        this.statisticsDao = statisticsDao;
    }

    @Override
    public byte[] getCsv(MonthlyStatisticForm form, Admin admin) throws Exception {
        List<MonthlyMailingDetailStatistics> rows = getDetailedMailingsStatistics(form, admin);

        return CsvWriter.csv(ListUtils.union(
                List.of(getHeaderForCSv(admin.getLocale())),
                getRowsForCsv(limit(rows, ROWS_LIMIT), admin))
        );
    }

    private static List<String> getHeaderForCSv(Locale locale) {
        return List.of(
                getLocaleString("Date", locale),
                getLocaleString("Mailing", locale),
                getLocaleString("Mailtype", locale),
                getLocaleString("default.Size", locale),
                getLocaleString("statistic.Amount", locale)
        );
    }

    private List<List<String>> getRowsForCsv(List<MonthlyMailingDetailStatistics> rows, Admin admin) {
        return rows.stream()
                .map(row -> Arrays.asList(
                        admin.getDateFormat().format(row.date()),
                        row.shortname(),
                        getLocaleString(row.mailType().getMessageKey(), admin.getLocale()),
                        "%.2f kB".formatted(row.sizeKb()),
                        String.valueOf(row.amount())
                ))
                .toList();
    }

    @Override
    public MonthlyOverviewResponseData getData(MonthlyStatisticForm form, Admin admin) {
        Map<MaildropStatus, MaildropStatusSummary> statusSummaryData = getMaildropStatusStatistics(form, admin);
        List<MonthlyMailingDetailStatistics> detailRows = getDetailedMailingsStatistics(form, admin);

        return new MonthlyOverviewResponseData(
                calcAverageSizeKb(statusSummaryData.values()),
                ListUtils.union(convertToMailingTypeRows(statusSummaryData), List.of(getTotalRow(statusSummaryData.values()))),
                limit(detailRows, ROWS_LIMIT),
                convertToAmountRows(detailRows)
        );
    }

    private Map<MaildropStatus, MaildropStatusSummary> getMaildropStatusStatistics(MonthlyStatisticForm form, Admin admin) {
        return statisticsDao.getMaildropStatusStatistics(
                admin.getAdminID(),
                admin.getCompanyID(),
                prepareDateRange(form.year(), form.month(), admin)
        );
    }

    private List<MonthlyMailingDetailStatistics> getDetailedMailingsStatistics(MonthlyStatisticForm form, Admin admin) {
        return statisticsDao.getDetailedMailingsStatistics(
                admin.getAdminID(),
                admin.getCompanyID(),
                form.type(),
                prepareDateRange(form.year(), form.month(), admin)
        );
    }

    private <T> List<T> limit(Collection<T> list, int limit) {
        return list.stream()
                .limit(limit)
                .toList();
    }

    private DateRange prepareDateRange(int year, int month, Admin admin) {
        ZonedDateTime startDate = LocalDate.of(year, month, 1)
                .atStartOfDay(admin.getZoneId());

        return new DateRange(startDate, startDate.plusMonths(1));
    }

    private double calcAverageSizeKb(Collection<MaildropStatusSummary> rows) {
        return rows.stream()
                .mapToDouble(MaildropStatusSummary::averageSizeKb)
                .sum();
    }

    private List<MailingTypeRow> convertToMailingTypeRows(Map<MaildropStatus, MaildropStatusSummary> statusSummaryData) {
        return TYPE_TO_STATUS_MAP.entrySet().stream()
                .map(e -> {
                    MaildropStatusSummary row = statusSummaryData.get(e.getValue());
                    if (row == null) {
                        return new MailingTypeRow(e.getKey(), 0, 0);
                    }

                    return new MailingTypeRow(e.getKey(), row.mailingsCount(), row.emailsCount());
                })
                .toList();
    }

    private List<MonthlyMailingAmountStatistics> convertToAmountRows(List<MonthlyMailingDetailStatistics> rows) {
        return rows.stream()
                .collect(Collectors.groupingBy(
                        MonthlyMailingDetailStatistics::id,
                        Collectors.toList()
                ))
                .entrySet()
                .stream()
                .map(e ->
                        new MonthlyMailingAmountStatistics(
                                e.getKey(),
                                e.getValue().getFirst().shortname(),
                                e.getValue().stream().mapToInt(MonthlyMailingDetailStatistics::amount).sum()
                        )
                ).toList();
    }

    private MailingTypeRow getTotalRow(Collection<MaildropStatusSummary> rows) {
        return new MailingTypeRow(
                rows.stream().mapToInt(MaildropStatusSummary::mailingsCount).sum(),
                rows.stream().mapToInt(MaildropStatusSummary::emailsCount).sum()
        );
    }

}
