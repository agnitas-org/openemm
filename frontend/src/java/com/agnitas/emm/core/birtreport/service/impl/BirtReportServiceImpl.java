/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtreport.service.impl;

import static com.agnitas.emm.core.birtreport.bean.impl.BirtReportDateRangedSettings.DATE_RANGE_CUSTOM;
import static com.agnitas.emm.core.birtreport.bean.impl.BirtReportDateRangedSettings.DATE_RANGE_KEY;
import static com.agnitas.emm.core.birtreport.bean.impl.BirtReportDateRangedSettings.DATE_RANGE_PREDEFINED;
import static com.agnitas.emm.core.birtreport.bean.impl.BirtReportDateRangedSettings.DATE_RANGE_PREDEFINED_KEY;
import static com.agnitas.emm.core.birtreport.bean.impl.BirtReportMailingSettings.MAILINGS_TO_SEND_KEY;
import static com.agnitas.emm.core.birtreport.bean.impl.BirtReportMailingSettings.MAILING_ACTION_BASED;
import static com.agnitas.emm.core.birtreport.bean.impl.BirtReportMailingSettings.MAILING_DATE_BASED;
import static com.agnitas.emm.core.birtreport.bean.impl.BirtReportMailingSettings.MAILING_FOLLOW_UP;
import static com.agnitas.emm.core.birtreport.bean.impl.BirtReportMailingSettings.MAILING_GENERAL_TYPES_KEY;
import static com.agnitas.emm.core.birtreport.bean.impl.BirtReportMailingSettings.MAILING_INTERVAL_BASED;
import static com.agnitas.emm.core.birtreport.bean.impl.BirtReportMailingSettings.MAILING_NORMAL;
import static com.agnitas.emm.core.birtreport.bean.impl.BirtReportMailingSettings.PERIOD_TYPE_KEY;
import static com.agnitas.emm.core.birtreport.bean.impl.BirtReportSettings.ENABLED_KEY;
import static com.agnitas.emm.core.birtreport.bean.impl.BirtReportSettings.MAILINGS_KEY;
import static com.agnitas.emm.core.birtreport.bean.impl.BirtReportSettings.MAILING_FILTER_KEY;
import static com.agnitas.emm.core.birtreport.bean.impl.BirtReportSettings.MAILING_TYPE_KEY;
import static com.agnitas.emm.core.birtreport.bean.impl.BirtReportSettings.PREDEFINED_ID_KEY;
import static com.agnitas.emm.core.birtreport.bean.impl.BirtReportSettings.PREDEFINED_MAILINGS_KEY;
import static com.agnitas.emm.core.birtreport.bean.impl.BirtReportSettings.SORT_DATE;
import static com.agnitas.emm.core.birtreport.bean.impl.BirtReportSettings.SORT_MAILINGS_KEY;
import static com.agnitas.emm.core.birtreport.bean.impl.BirtReportSettings.SORT_NAME;
import static com.agnitas.emm.core.birtreport.dto.FilterType.FILTER_ARCHIVE;
import static com.agnitas.emm.core.birtreport.dto.FilterType.FILTER_MAILINGLIST;
import static com.agnitas.emm.core.birtreport.dto.FilterType.FILTER_TARGET;
import static com.agnitas.emm.core.birtreport.util.BirtReportSettingsUtils.GENERAL_INFO_GROUP;
import static com.agnitas.emm.core.birtreport.util.BirtReportSettingsUtils.MAILINGS_PREDEFINED;
import static com.agnitas.emm.core.birtreport.util.BirtReportSettingsUtils.MAILING_DEVICES_GROUP;
import static com.agnitas.emm.core.birtreport.util.BirtReportSettingsUtils.MAILING_FORMATS_GROUP;
import static com.agnitas.emm.core.birtreport.util.BirtReportSettingsUtils.MAILING_GENERAL_GROUP;
import static com.agnitas.emm.core.birtreport.util.BirtReportSettingsUtils.MAILING_OPENER_GROUP;
import static com.agnitas.emm.core.birtreport.util.BirtReportSettingsUtils.mailingStatisticProps;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.sql.DataSource;

import com.agnitas.beans.Admin;
import com.agnitas.beans.Campaign;
import com.agnitas.beans.Mailing;
import com.agnitas.dao.CampaignDao;
import com.agnitas.dao.MailingDao;
import com.agnitas.emm.common.MailingType;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.birtreport.bean.BirtReport;
import com.agnitas.emm.core.birtreport.bean.ReportEntry;
import com.agnitas.emm.core.birtreport.bean.impl.BirtReportComparisonSettings;
import com.agnitas.emm.core.birtreport.bean.impl.BirtReportDateRangedSettings;
import com.agnitas.emm.core.birtreport.bean.impl.BirtReportMailingSettings;
import com.agnitas.emm.core.birtreport.bean.impl.BirtReportSettings;
import com.agnitas.emm.core.birtreport.dao.BirtReportDao;
import com.agnitas.emm.core.birtreport.dto.BirtReportDownload;
import com.agnitas.emm.core.birtreport.dto.BirtReportDto;
import com.agnitas.emm.core.birtreport.dto.BirtReportStatisticDto;
import com.agnitas.emm.core.birtreport.dto.BirtReportType;
import com.agnitas.emm.core.birtreport.dto.FilterType;
import com.agnitas.emm.core.birtreport.dto.PeriodType;
import com.agnitas.emm.core.birtreport.dto.PredefinedType;
import com.agnitas.emm.core.birtreport.dto.ReportSettingsType;
import com.agnitas.emm.core.birtreport.forms.BirtReportForm;
import com.agnitas.emm.core.birtreport.forms.BirtReportOverviewFilter;
import com.agnitas.emm.core.birtreport.service.BirtReportFileService;
import com.agnitas.emm.core.birtreport.service.BirtReportService;
import com.agnitas.emm.core.birtreport.util.BirtReportSettingsUtils;
import com.agnitas.emm.core.birtstatistics.service.BirtStatisticsService;
import com.agnitas.messages.Message;
import com.agnitas.service.ExtendedConversionService;
import com.agnitas.service.ServiceResult;
import com.agnitas.beans.MailingBase;
import com.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.core.useractivitylog.bean.UserAction;
import com.agnitas.util.AgnUtils;
import com.agnitas.util.Const;
import com.agnitas.util.DateUtilities;
import com.agnitas.util.DbUtilities;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

public class BirtReportServiceImpl implements BirtReportService {
    /** The logger. */
    private static final Logger logger = LogManager.getLogger(BirtReportServiceImpl.class);

    public static final String KEY_START_DATE = "from";
    public static final String KEY_END_DATE = "to";
    public static final int ROWS_NUMBER = 100;

    /** DAO accessing BIRT report data. */
    private BirtReportDao birtReportDao;

    /** DAO accessing mailing data. */
    protected MailingDao mailingDao;

    private DataSource dataSource;

    private ExtendedConversionService conversionService;
    private CampaignDao campaignDao;
    private BirtStatisticsService birtStatisticsService;
    private BirtReportFileService birtReportFileService;
    private ConfigService configService;

    @Override
    public boolean insert(BirtReport report) {
        if (report.isReportActive() == 1) {
            report.setActivationDate(new Date());
        }
        return birtReportDao.insert(report);
    }

    @Override
    public void logSentReport(BirtReport report) {
        if (report.isTriggeredByMailing()) {
            final BirtReportMailingSettings reportMailingSettings = report.getReportMailingSettings();
            final List<Integer> mailingsIdsToSend = reportMailingSettings.getMailingsIdsToSend();
            birtReportDao.insertSentMailings(report.getId(), report.getCompanyID(), mailingsIdsToSend);
            if (reportMailingSettings.getReportSettingAsInt(BirtReportSettings.MAILING_FILTER_KEY) == FilterType.FILTER_MAILING.getKey()) {
                // Only deactivate this report if it is NOT an recurring report like e.g. "last mailing sent of mailinglist"
                birtReportDao.deactivateBirtReport(report.getId());
            }
            if (!mailingsIdsToSend.isEmpty()) {
                reportMailingSettings.getSettingsMap().remove(MAILINGS_TO_SEND_KEY);
            }
        } else {
            birtReportDao.insertSentMailings(report.getId(), report.getCompanyID(), null);
        }
    }

    protected List<MailingBase> getPredefinedMailingsForReports(int companyId, int number, int filterType, int filterValue, MailingType mailingType, String orderKey, int targetId, Set<Integer> adminAltgIds) {
        return mailingDao.getPredefinedMailingsForReports(companyId, number, filterType, filterValue, mailingType, orderKey, targetId, adminAltgIds);
    }

    protected List<MailingBase> getPredefinedMailingsForReports(int companyId, int number, int filterType, int filterValue, MailingType mailingType, String orderKey, Map<String, LocalDate> datesRestriction, int targetId, Set<Integer> adminAltgIds) {
        if (number == 0) {
            Date from = DateUtilities.toDate(datesRestriction.get(KEY_START_DATE), AgnUtils.getSystemTimeZoneId());
            // Include the defined day completely, so use limit of next day 00:00
            Date to = DateUtilities.toDate(datesRestriction.get(KEY_END_DATE).plus(1, ChronoUnit.DAYS), AgnUtils.getSystemTimeZoneId());

            return mailingDao.getPredefinedNormalMailingsForReports(companyId, from, to, filterType, filterValue, orderKey, targetId, adminAltgIds);
        } else {
            return mailingDao.getPredefinedMailingsForReports(companyId, number, filterType, filterValue, mailingType, orderKey, targetId, adminAltgIds);
        }
    }

    private boolean checkReportToSend(final BirtReport birtReport) {
        try {
            if (birtReport.isTriggeredByMailing()) {
                BirtReportMailingSettings mailingSettings = birtReport.getReportMailingSettings();
                if (!mailingSettings.isEnabled()) {
                    return false;
                }
                BirtReportType reportType = BirtReportType.getTypeByCode(birtReport.getReportType());

                if (reportType == null) {
                    throw new RuntimeException("Invalid report type");
                }

                final Date activationDate = birtReport.getActivationDate();
                final Calendar startCalendar = new GregorianCalendar();
                startCalendar.setTime(activationDate);
                final Calendar endCalendar = new GregorianCalendar();
                endCalendar.setTime(new Date());

                switch (reportType) {
                    case TYPE_AFTER_MAILING_24HOURS:
                        startCalendar.add(GregorianCalendar.DATE, -1);
                        endCalendar.add(GregorianCalendar.DATE, -1);
                        break;
                    case TYPE_AFTER_MAILING_48HOURS:
                        startCalendar.add(GregorianCalendar.DATE, -2);
                        endCalendar.add(GregorianCalendar.DATE, -2);
                        break;
                    case TYPE_AFTER_MAILING_WEEK:
                        startCalendar.add(GregorianCalendar.DATE, -7);
                        endCalendar.add(GregorianCalendar.DATE, -7);
                        break;
                    default:
                        throw new RuntimeException("Invalid report type");
                }

                final Date startDate = startCalendar.getTime();
                final Date endDate = endCalendar.getTime();

                int filterId = mailingSettings.getReportSettingAsInt(MAILING_FILTER_KEY);
                int filterValue = mailingSettings.getReportSettingAsInt(BirtReportSettings.PREDEFINED_ID_KEY);

                final List<Integer> mailingsToSend = mailingDao.getBirtReportMailingsToSend(birtReport.getCompanyID(), birtReport.getId(), startDate, endDate, filterId, filterValue);
                mailingSettings.setMailingsToSend(mailingsToSend);
                return !mailingsToSend.isEmpty();
            } else {
                BirtReportMailingSettings mailingSettings = birtReport.getReportMailingSettings();
                updateSelectedMailingIds(mailingSettings, birtReport.getCompanyID(), 0, null);
                final List<Integer> mailingsToSend = mailingSettings.getMailingsIdsToSend();
                boolean doMailingReport = mailingSettings.isEnabled() && !mailingsToSend.isEmpty();

                BirtReportComparisonSettings comparisonSettings = birtReport.getReportComparisonSettings();
                updateSelectedComparisonIds(comparisonSettings, birtReport.getCompanyID(), 0, null);
                List<String> compareMailings = comparisonSettings.getMailings();
                boolean doComparisonReport = comparisonSettings.isEnabled() && CollectionUtils.isNotEmpty(compareMailings);

                boolean doRecipientReport = birtReport.getReportRecipientSettings().isEnabled();
                boolean doTopDomainsReport = birtReport.getReportTopDomainsSettings().isEnabled();

                return doComparisonReport || doMailingReport || doRecipientReport || doTopDomainsReport;
            }
        } catch (Exception e) {
            logger.error("Cannot check status of report: " + birtReport.getId(), e);
            return false;
        }
    }

    protected void updateSelectedMailingIds(BirtReportMailingSettings reportMailingSettings, int companyId, int targetId, Set<Integer> adminAltgIds) {
        List<Integer> mailingIds;
        int mailingType = reportMailingSettings.getMailingType();
        int mailingGeneralType = reportMailingSettings.getMailingGeneralType();
        if (mailingGeneralType == MAILING_NORMAL) {
            if (mailingType == BirtReportMailingSettings.MAILING_PREDEFINED) {
                int filterValue = 0;
                int filterType = reportMailingSettings.getReportSettingAsInt(MAILING_FILTER_KEY, FilterType.FILTER_NO_FILTER.getKey());
                int numOfMailings = getLastNumberValue(reportMailingSettings.getPredefinedMailings());
                String sortOrder = reportMailingSettings.getReportSettingAsString(SORT_MAILINGS_KEY);
                if (filterType == FilterType.FILTER_ARCHIVE.getKey() || filterType == FilterType.FILTER_MAILINGLIST.getKey() || filterType == FilterType.FILTER_TARGET.getKey()) {
                    filterValue = reportMailingSettings.getReportSettingAsInt(BirtReportMailingSettings.PREDEFINED_ID_KEY);
                }
                mailingIds = getPredefinedMailingsForReports(companyId, numOfMailings, filterType, filterValue, null, sortOrder, targetId, adminAltgIds)
                        .stream()
                        .map(MailingBase::getId)
                        .collect(Collectors.toList());
                reportMailingSettings.setMailingsToSend(mailingIds);
            }
        } else if (mailingGeneralType == MAILING_ACTION_BASED || mailingGeneralType == MAILING_DATE_BASED
                || mailingGeneralType == MAILING_INTERVAL_BASED || mailingGeneralType == MAILING_FOLLOW_UP) {
            Object reportSetting = reportMailingSettings.getReportSetting(MAILINGS_KEY);
            reportMailingSettings.setReportSetting(MAILINGS_TO_SEND_KEY, reportSetting);
        }
    }

    protected void updateSelectedComparisonIds(BirtReportComparisonSettings reportComparisonSettings, int companyId, int targetId, Set<Integer> adminAltgIds) {
        updateSelectedComparisonIds(reportComparisonSettings, null, companyId, targetId, adminAltgIds);
    }

    protected void updateSelectedComparisonIds(BirtReportComparisonSettings reportComparisonSettings, DateTimeFormatter formatter, int companyId, int targetId, Set<Integer> adminAltgIds) {
        int mailingType = reportComparisonSettings.getReportSettingAsInt(MAILING_TYPE_KEY);

        if (mailingType == BirtReportSettings.MAILINGS_PREDEFINED) {
            String sortOrder = reportComparisonSettings.getReportSettingAsString(SORT_MAILINGS_KEY);
            int numOfMailings = reportComparisonSettings.getReportSettingAsInt(PREDEFINED_MAILINGS_KEY);
            int filterType = reportComparisonSettings.getReportSettingAsInt(MAILING_FILTER_KEY, FilterType.FILTER_NO_FILTER.getKey());
            int filterValue = 0;
            if ((filterType == FilterType.FILTER_ARCHIVE.getKey() || filterType == FilterType.FILTER_MAILINGLIST.getKey() || filterType == FilterType.FILTER_TARGET.getKey())) {
                filterValue = reportComparisonSettings.getReportSettingAsInt(BirtReportComparisonSettings.PREDEFINED_ID_KEY);
            }
            if (numOfMailings >= 0) {
                PeriodType periodType = PeriodType.getTypeByKey(reportComparisonSettings.getPeriodType());
                Map<String, LocalDate> datesRestriction = getDatesRestrictionMap(periodType, reportComparisonSettings.getSettingsMap(), formatter);
                String[] mailingIds = getPredefinedMailingsForReports(companyId, numOfMailings, filterType, filterValue, null, sortOrder, datesRestriction, targetId, adminAltgIds)
                        .stream()
                        .map(mailing -> Integer.toString(mailing.getId()))
                        .toArray(String[]::new);

                reportComparisonSettings.setMailings(mailingIds);
            }
        }
    }

    protected Map<String, LocalDate> getDatesRestrictionMap(PeriodType periodType, Map<String, Object> settings, DateTimeFormatter dateTimeFormatter) {
        if (Objects.isNull(periodType)) {
            return new HashMap<>();
        }

        LocalDate from;
        LocalDate to;

        switch (periodType) {
            case DATE_RANGE_CUSTOM:
                String startDate = BirtReportSettingsUtils.getSettingsProperty(settings, BirtReportSettingsUtils.START_DATE);
                String stopDate = BirtReportSettingsUtils.getSettingsProperty(settings, BirtReportSettingsUtils.END_DATE);

                if (StringUtils.isNotEmpty(startDate) && StringUtils.isNotEmpty(stopDate)) {
                    if (dateTimeFormatter != null) {
                        from = LocalDate.parse(startDate, dateTimeFormatter);
                        to = LocalDate.parse(stopDate, dateTimeFormatter);
                    } else {
                        // Information about date format is lost, TODO: store a unified date string
                        try {
                            from = LocalDate.parse(startDate, DateTimeFormatter.ofPattern(DateUtilities.YYYY_MM_DD));
                        } catch (DateTimeParseException e1) {
                            try {
                                from = LocalDate.parse(startDate, DateTimeFormatter.ofPattern(DateUtilities.DD_MM_YYYY));
                            } catch (DateTimeParseException e2) {
                                throw new RuntimeException("Unknown start date format");
                            }
                        }

                        try {
                            to = LocalDate.parse(stopDate, DateTimeFormatter.ofPattern(DateUtilities.YYYY_MM_DD));
                        } catch (DateTimeParseException e1) {
                            try {
                                to = LocalDate.parse(stopDate, DateTimeFormatter.ofPattern(DateUtilities.DD_MM_YYYY));
                            } catch (DateTimeParseException e2) {
                                throw new RuntimeException("Unknown stop date format");
                            }
                        }
                    }
                } else {
                    from = LocalDate.now();
                    to = LocalDate.now();
                }
                break;
            case DATE_RANGE_30DAYS:
                from = LocalDate.now().minusDays(30);
                to = LocalDate.now();
                break;
            case DATE_RANGE_LAST_MONTH:
                from = LocalDate.now().minusMonths(1);
                from = from.withDayOfMonth(1);
                to = from.plusMonths(1);
                break;
            case DATE_RANGE_WEEK:
                from = LocalDate.now().minusWeeks(1);
                to = LocalDate.now();
                break;
            case DATE_RANGE_DAY:
                from = LocalDate.now().minusDays(1);
                to = LocalDate.now();
                break;
            case DATE_RANGE_THREE_MONTH:
                from = LocalDate.now().minusDays(1);
                to = LocalDate.now();
                break;
            default:
                from = null;
                to = null;
        }

        Map<String, LocalDate> datesRestriction = new HashMap<>();
        datesRestriction.put(KEY_START_DATE, from);
        datesRestriction.put(KEY_END_DATE, to);
        return datesRestriction;
    }

    @Override
    public boolean isExistedBenchmarkMailingTbls() {
        return DbUtilities.checkIfTableExists(dataSource, "benchmark_mailing_tbl") && DbUtilities.checkIfTableExists(dataSource, "benchmark_mailing_stat_tbl");
    }

    /**
     * Set DAO accessing BIRT report data.
     *
     * @param birtReportDao DAO accessing BIRT report data
     */
    public void setBirtReportDao(BirtReportDao birtReportDao) {
        this.birtReportDao = birtReportDao;
    }

    /**
     * Set DAO accessing mailing data.
     *
     * @param mailingDao DAO accessing mailing data
     */
    public void setMailingDao(MailingDao mailingDao) {
        this.mailingDao = mailingDao;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public boolean announceStart(BirtReport birtReport) {
        return birtReportDao.announceStart(birtReport);
    }

    @Override
    public void announceEnd(BirtReport birtReport) {
        birtReportDao.announceEnd(birtReport);
    }

    @Override
    public int getRunningReportsByHost(String hostName) {
        return birtReportDao.getRunningReportsByHost(hostName);
    }

    @Override
    public List<BirtReport> getReportsToSend(int maximumNumberOfReports, List<Integer> includedCompanyIds, List<Integer> excludedCompanyIds) {
        if (maximumNumberOfReports <= 0) {
            return new ArrayList<>();
        }

        final List<BirtReport> reportsList = birtReportDao.getReportsToSend(includedCompanyIds, excludedCompanyIds);
        List<BirtReport> reportsToSend = new ArrayList<>();
        for (BirtReport birtReport : reportsList) {
            if (reportsToSend.size() < maximumNumberOfReports) {
                if (checkReportToSend(birtReport)) {
                    reportsToSend.add(birtReport);
                } else if (birtReport.getNextStart() != null) {
                    // Time triggered reports, which do have an additional unfulfilled condition for being sent will be retried after next interval
                    // (e.g. reports for mailings of a defined mailinglists to be sent within the last week, when no fitting mailing was sent in that week)
                    birtReport.setNextStart(DateUtilities.calculateNextJobStart(birtReport.getIntervalpattern()));
                    birtReportDao.announceStart(birtReport);
                    birtReportDao.announceEnd(birtReport);
                }
            }
        }
        return reportsToSend;
    }

    @Override
    public List<ReportEntry> findAllByEmailPart(String email, int companyID) {
        return birtReportDao.findAllByEmailPart(email, companyID);
    }

    @Override
    public List<ReportEntry> findAllByEmailPart(String email) {
        return birtReportDao.findAllByEmailPart(email);
    }

    @Override
    @Transactional
    public void storeBirtReportEmailRecipients(List<String> emails, int reportId) {
        birtReportDao.storeBirtReportEmailRecipients(emails, reportId);
    }

    @Override
    public void restore(Set<Integer> bulkIds, int companyId) {
        birtReportDao.restore(bulkIds, companyId);
    }

    @Override
    public Map<BirtReportType, Integer> getMailingAutomaticReportIdsMap(int mailingId, int companyId) {
        return birtReportDao.getMailingAutomaticReportIdsMap(mailingId, companyId);
    }

    @Override
    public List<String> getMailingAutomaticReportEmails(Collection<Integer> reportIds) {
        return birtReportDao.getMailingAutomaticReportEmails(reportIds);
    }

    @Override
    public void deleteExpired(Date expireDate, int companyId) {
        birtReportDao.getMarkedAsDeletedBefore(expireDate, companyId)
            .forEach(reportId -> birtReportDao.deleteReport(reportId, companyId));
    }

    @Override
    public BirtReportDto getBirtReport(Admin admin, int reportId) {
        int companyId = admin.getCompanyID();
        BirtReport birtReport = getBirtReport(reportId, companyId);

        if (birtReport != null) {
            if (!admin.permissionAllowed(Permission.DEEPTRACKING)) {
                birtReport.setSettingParameter(ReportSettingsType.COMPARISON, BirtReportSettingsUtils.Properties.CONVERSION_RATE.getPropName(), false);
            }

            if (!AgnUtils.isMailTrackingAvailable(admin) && ReportSettingsType.getTypeByCode(birtReport.getActiveTab()).isMailTrackingRequired()) {
                birtReport.setActiveTab(ReportSettingsType.COMPARISON.getKey());
            }
        }

        return conversionService.convert(birtReport, BirtReportDto.class);
    }

    @Override
    public BirtReport getBirtReport(int reportId, int companyId) {
        return birtReportDao.get(reportId, companyId);
    }

    @Override
    public BirtReportDownload evaluate(BirtReportForm form, Admin admin) {
        BirtReportDto dto = conversionService.convert(form, BirtReportDto.class);
        BirtReport report = conversionService.convert(dto, BirtReport.class);
        if (report == null) {
            throw new IllegalStateException("Can't get report from form during evaluation");
        }

        ReportSettingsType type = getBirtReportSettingsTypeForEvaluation(form.getActiveTab(), admin);
        BirtReportSettings settings = report.getSetting(type);
        updateSelectedReportSettings(admin, settings);
        Set<String> missingParameters = settings.getMissingReportParameters(report.getReportType());

        if (!missingParameters.isEmpty()) {
            return null;
        }
        BirtReportDownload birtReportDownload = new BirtReportDownload();
        birtReportDownload.setReportId(report.getId());

        BirtReportStatisticDto statisticDto = conversionService.convert(report, BirtReportStatisticDto.class);
        String reportUrl = birtStatisticsService.getReportStatisticsUrl(admin, statisticDto);
        if (logger.isInfoEnabled()) {
            logger.info("Birt report statistic evaluation, report URL: {}", reportUrl);
        }
        birtReportDownload.setBirtFileUrl(reportUrl);

        String localizedFileName = birtReportFileService.buildLocalizedFileName(
            settings, admin.getCompanyID(), admin.getLocale(), report.getFormatName());

        birtReportDownload.setFileName(localizedFileName);
        birtReportDownload.setShortname(report.getShortname());
        return birtReportDownload;
    }

    @Override
    public ReportSettingsType getBirtReportSettingsTypeForEvaluation(int activeTab, Admin admin) {
        int typeCode = getSettingsTypeKeyToEvaluate(activeTab, admin);
        return ReportSettingsType.getTypeByCode(typeCode);
    }

    private int getSettingsTypeKeyToEvaluate(int activeTab, Admin admin) {
        if (!AgnUtils.isMailTrackingAvailable(admin)
                && ReportSettingsType.getTypeByCode(activeTab).isMailTrackingRequired()) {
            return ReportSettingsType.COMPARISON.getKey();
        }
        return activeTab;
    }

    private boolean markDeleted(int reportId, int companyId) {
        return birtReportDao.markDeleted(reportId, companyId);
    }

    @Override
    public boolean deleteReport(int companyId, int reportId) {
        return birtReportDao.deleteReport(companyId, reportId);
    }

    @Override
    public PaginatedListImpl<ReportEntry> getPaginatedReportList(int companyId, String sort, String sortOrder, int page, int rownums) {
        return birtReportDao.getPaginatedReportList(companyId, sort, sortOrder, page, rownums);
    }

    @Override
    public PaginatedListImpl<ReportEntry> getPaginatedReportList(BirtReportOverviewFilter filter, int companyId) {
        return birtReportDao.getPaginatedReportList(filter, companyId);
    }

    @Override
    public List<Campaign> getCampaignList(int companyId) {
        final String sort = "upper( shortname )";
        final int order = 1;
        return campaignDao.getCampaignList(companyId, sort, order);
    }

    @Override
    public Map<String, LocalDate> getDatesRestrictionMap(Admin admin, ReportSettingsType type, SimpleDateFormat dateFormat, Map<String, Object> settings) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateFormat.toPattern());

        if (type == ReportSettingsType.RECIPIENT || type == ReportSettingsType.TOP_DOMAIN) {
            return getDatesRangedRestrictionMap(settings, formatter);
        }

        PeriodType periodType = PeriodType.getTypeByStringKey(BirtReportSettingsUtils.getSettingsProperty(settings, PERIOD_TYPE_KEY));
        return getDatesRestrictionMap(periodType, settings, formatter);
    }

    @Override
    public String getReportName(int companyId, int reportId) {
        return birtReportDao.getReportName(companyId, reportId);
    }

    @Override
    public boolean isReportExist(int companyId, int reportId) {
        return birtReportDao.isReportExist(companyId, reportId);
    }

    @Override
    public List<MailingBase> getFilteredMailings(Admin admin, int filterType, int filterValue, MailingType mailingType) {
        if (Objects.isNull(mailingType)) {
            return Collections.emptyList();
        }

        final FilterType filter = FilterType.getFilterTypeByKey(filterType);
        Set<Integer> adminAltgIds = null;
        int altgId = 0;

        if (configService.isExtendedAltgEnabled(admin.getCompanyID())) {
            adminAltgIds = filter == FilterType.FILTER_TARGET ? null : admin.getAltgIds();
        } else if (filter != FilterType.FILTER_TARGET) {
            altgId = getAltgId(admin);
        }

        if (mailingType == MailingType.NORMAL) {
            if (filter != FILTER_ARCHIVE && filter != FILTER_MAILINGLIST && filter != FILTER_TARGET) {
                return mailingDao.getSentWorldMailingsForReports(admin.getCompanyID(), ROWS_NUMBER, altgId, adminAltgIds);
            }
        }

        return mailingDao.getPredefinedMailingsForReports(admin.getCompanyID(), ROWS_NUMBER, filterType, filterValue, mailingType, "", altgId, adminAltgIds);
    }

    @Override
    public int saveBirtReport(Admin admin, BirtReportDto birtReport) {
        int companyId = admin.getCompanyID();
        BirtReport report = conversionService.convert(birtReport, BirtReport.class);
        report.setLanguage(admin.getLocale().getLanguage());
        report.setCompanyID(companyId);

        if (birtReport.getEndDate() != null) {
            SimpleDateFormat dateFormat = DateUtilities.getFormat(DateUtilities.YYYY_MM_DD, AgnUtils.getTimeZone(admin));
            Date endDate = DateUtilities.parse(DateUtilities.format(birtReport.getEndDate(), dateFormat), dateFormat);
            birtReport.setEndDate(endDate);
        }

        boolean isActive = isReportEnabled(admin, birtReport);
        report.setReportActive(BooleanUtils.toInteger(isActive));

        updateActivationDate(companyId, report);

        if (birtReport.getId() == 0) {
            birtReportDao.insert(report);
        } else {
            if (!AgnUtils.isMailTrackingAvailable(admin)) {
                List<Integer> deactivateSettings =
                        Arrays.stream(ReportSettingsType.values())
                                .filter(ReportSettingsType::isMailTrackingRequired)
                                .map(ReportSettingsType::getKey).collect(Collectors.toList());
                //just deactivate settings that require mail tracking and don't touch saved settings
                birtReportDao.update(report, deactivateSettings);
            } else {
                birtReportDao.update(report);
            }
        }

        return report.getId();
    }

    @Override
    public boolean isReportEnabled(Admin admin, BirtReportDto birtReport) {
        List<Object> values = birtReport.getSettings().values().stream()
                .map(settings -> settings.get(ENABLED_KEY))
                .collect(Collectors.toList());

        return conversionService.convert(values, Object.class, Boolean.class).stream()
                .filter(BooleanUtils::isTrue)
                .findFirst().orElse(false);
    }

    @Override
    public void copySampleReports(int toCompanyId, int fromCompanyId) {
        for (int reportId : birtReportDao.getSampleReportIds(fromCompanyId)) {
            BirtReport report = getBirtReport(reportId, fromCompanyId);
            report.setId(0);
            report.setCompanyID(toCompanyId);
            report.setReportActive(0);
            report.setActivationDate(null);
            report.setNextStart(null);

            report.getSettings().forEach(s -> s.getSettingsMap().remove(ENABLED_KEY));

            birtReportDao.insert(report);
        }
    }

    @Override
    public List<String> getNames(Set<Integer> ids, int companyID) {
        return ids.stream()
                .filter(id -> isReportExist(companyID, id))
                .map(id -> getReportName(companyID, id))
                .collect(Collectors.toList());
    }

    @Override
    public ServiceResult<UserAction> markDeleted(Set<Integer> ids, int companyID) {
        List<Integer> deletedIds = ids.stream()
                .filter(id -> isReportExist(companyID, id))
                .filter(id -> markDeleted(id, companyID))
                .collect(Collectors.toList());

        return ServiceResult.success(
                new UserAction(
                        "delete reports",
                        String.format("Reports IDs %s", StringUtils.join(deletedIds, ", "))
                ),
                Message.of(Const.Mvc.SELECTION_DELETED_MSG)
        );
    }

    @Override
    public BirtReportForm createSingleMailingStatisticsReportForm(int mailingId, Admin admin) {
        Mailing mailing = mailingDao.getMailing(mailingId, admin.getCompanyID());
        BirtReportForm report = new BirtReportForm();
        report.setShortname("Single mailing statistics report");
        report.setType(BirtReportType.TYPE_AFTER_MAILING_24HOURS.getKey());
        report.setActiveTab(ReportSettingsType.MAILING.getKey());
        report.setEmailSubject(String.format("Your mailing statistic report of mailing '%s'", mailing.getShortname()));
        report.setEmailAddresses(admin.getEmail());
        report.setSettings(Map.of(ReportSettingsType.MAILING, getParamsForSingleMailingStatisticReport(mailing)));
        return report;
    }

    private Map<String, Object> getParamsForSingleMailingStatisticReport(Mailing mailing) {
        int mailingType = MAILING_NORMAL;
        if (mailing.getMailingType().equals(MailingType.ACTION_BASED)) {
            mailingType = MAILING_ACTION_BASED;
        } else if (mailing.getMailingType().equals(MailingType.DATE_BASED)) {
            mailingType = MAILING_DATE_BASED;
        } else if (mailing.getMailingType().equals(MailingType.INTERVAL)) {
            mailingType = MAILING_INTERVAL_BASED;
        } else if (mailing.getMailingType().equals(MailingType.FOLLOW_UP)) {
            mailingType = MAILING_FOLLOW_UP;
        }

        Map<String, Object> params = Stream.of(mailingStatisticProps.get(GENERAL_INFO_GROUP), MAILING_FORMATS_GROUP, MAILING_GENERAL_GROUP, MAILING_OPENER_GROUP, MAILING_DEVICES_GROUP)
                .flatMap(Collection::stream)
                .collect(Collectors.toMap(BirtReportSettingsUtils.Properties::getPropName, p -> true));

        params.put(MAILINGS_KEY, String.valueOf(mailing.getId()));
        params.put(MAILING_TYPE_KEY, mailingType);
        params.put(MAILING_GENERAL_TYPES_KEY, mailingType);

        return params;
    }

    @Override
    public void preloadMailingsByRestriction(Admin admin, ReportSettingsType type, Map<String, Object> settingsByType, Map<String, LocalDate> dateRestrictions) {
        int mailingType = NumberUtils.toInt((String) settingsByType.get(MAILING_TYPE_KEY), MAILINGS_PREDEFINED);

        switch (type) {
            case COMPARISON:
                if (mailingType == MAILINGS_PREDEFINED) {
                    int comparisonNumOfMailings = NumberUtils.toInt(BirtReportSettingsUtils.getSettingsProperty(settingsByType, PREDEFINED_MAILINGS_KEY));
                    settingsByType.put(SORT_MAILINGS_KEY, SORT_DATE);
                    settingsByType.put(MAILINGS_KEY, preloadMailingsByRestrictions(admin, comparisonNumOfMailings, dateRestrictions, settingsByType));
                }
                break;
            case MAILING:
                int mailingGeneralType = NumberUtils.toInt((String) settingsByType.get(MAILING_GENERAL_TYPES_KEY), MAILING_NORMAL);
                if (mailingGeneralType == MAILING_NORMAL && mailingType == MAILINGS_PREDEFINED) {
                    int numOfMailings = getLastNumberValue(BirtReportSettingsUtils.getSettingsProperty(settingsByType, PREDEFINED_MAILINGS_KEY));
                    List<String> expression = preloadMailingsByRestrictions(admin, numOfMailings, null, settingsByType);
                    settingsByType.put(MAILINGS_TO_SEND_KEY, expression);
                    settingsByType.put(MAILINGS_KEY, expression);
                } else if (mailingGeneralType == MAILING_ACTION_BASED || mailingGeneralType == MAILING_DATE_BASED
                        || mailingGeneralType == MAILING_INTERVAL_BASED || mailingGeneralType == MAILING_FOLLOW_UP) {
                    List<String> reportSetting = BirtReportSettingsUtils.getSettingsPropertyList(settingsByType, MAILINGS_KEY);
                    settingsByType.put(MAILINGS_TO_SEND_KEY, reportSetting);
                }
                break;
            default:
                //nothing to do
        }
    }

    @Override
    public void deactivateAllDeliveries(int reportId) {
        birtReportDao.deactivateReportSettings(reportId, ReportSettingsType.keys());
    }

    @Override
    public boolean hasActiveDelivery(int reportId) {
        return birtReportDao.hasActiveDelivery(reportId, ReportSettingsType.keys());
    }

    private int getAltgId(final Admin admin) {
        if (admin.isAccessLimitedBySimpleALTG()) {
            return admin.getAccessLimitingTargetGroupID();
        }

        return 0;
    }

    private void updateSelectedReportSettings(Admin admin, BirtReportSettings reportSettings) {
        updateSelectedReportSettings(admin, reportSettings, null);
    }

    private void updateSelectedReportSettings(Admin admin, BirtReportSettings reportSettings, DateTimeFormatter formatter) {
        int companyId = admin.getCompanyID();
        if (configService.isExtendedAltgEnabled(companyId)) {
            Set<Integer> adminAltgIds = admin.getAltgIds();
            switch (reportSettings.getReportSettingsType()) {
                case COMPARISON:
                    updateSelectedComparisonIds((BirtReportComparisonSettings) reportSettings, formatter, companyId, 0, adminAltgIds);
                    break;
                case MAILING:
                    updateSelectedMailingIds((BirtReportMailingSettings) reportSettings, companyId, 0, adminAltgIds);
                    break;

                default:
                    //nothing to do
                    break;
            }
        } else {
            switch (reportSettings.getReportSettingsType()) {
                case COMPARISON:
                    updateSelectedComparisonIds((BirtReportComparisonSettings) reportSettings, formatter, companyId, getAltgId(admin), null);
                    break;
                case MAILING:
                    updateSelectedMailingIds((BirtReportMailingSettings) reportSettings, companyId, getAltgId(admin), null);
                    break;

                default:
                    //nothing to do
                    break;
            }
        }
    }

    private void updateActivationDate(int companyId, BirtReport report) {
        boolean isActive = report.isReportActive() == 1;
        int birtReportId = report.getId();
        if (birtReportId == 0) {
            report.setActivationDate(isActive ? new Date() : null);
        } else {
            if (isActive) {
                Date activationDate = birtReportDao.getReportActivationDay(companyId, birtReportId);
                if (activationDate == null) {
                    activationDate = new Date();
                }
                report.setActivationDate(activationDate);
            } else {
                report.setActivationDate(null);
            }
        }
    }

    private List<String> preloadMailingsByRestrictions(Admin admin, int numOfMailings, Map<String, LocalDate> datesRestriction, Map<String, Object> settingsByType) {
        FilterType filterType = FilterType.getFilterTypeByStringKey(BirtReportSettingsUtils.getSettingsProperty(settingsByType, MAILING_FILTER_KEY));
        int filterValue = 0;
        if (filterType == FILTER_ARCHIVE || filterType == FILTER_MAILINGLIST || filterType == FILTER_TARGET) {
            filterValue = NumberUtils.toInt(BirtReportSettingsUtils.getSettingsProperty(settingsByType, PREDEFINED_ID_KEY));
        }

        if (configService.isExtendedAltgEnabled(admin.getCompanyID())) {
            Set<Integer> adminAltgIds = filterType == FilterType.FILTER_TARGET ? null : admin.getAltgIds();

            if (numOfMailings >= 0 && datesRestriction != null) {
                return getPredefinedMailingsForReports(admin.getCompanyID(), numOfMailings, filterType.getKey(), filterValue, null, SORT_DATE, datesRestriction, 0, adminAltgIds)
                        .stream()
                        .map(mailing -> Integer.toString(mailing.getId()))
                        .collect(Collectors.toList());
            } else {
                return getPredefinedMailingsForReports(admin.getCompanyID(), numOfMailings, filterType.getKey(), filterValue, null, SORT_NAME, 0, adminAltgIds)
                        .stream()
                        .map(mailing -> Integer.toString(mailing.getId()))
                        .collect(Collectors.toList());
            }
        } else {
            final int altgId = filterType == FilterType.FILTER_TARGET ? 0 : getAltgId(admin);

            if (numOfMailings >= 0 && datesRestriction != null) {
                return getPredefinedMailingsForReports(admin.getCompanyID(), numOfMailings, filterType.getKey(), filterValue, null, SORT_DATE, datesRestriction, altgId, null)
                        .stream()
                        .map(mailing -> Integer.toString(mailing.getId()))
                        .collect(Collectors.toList());
            } else {
                return getPredefinedMailingsForReports(admin.getCompanyID(), numOfMailings, filterType.getKey(), filterValue, null, SORT_NAME, altgId, null)
                        .stream()
                        .map(mailing -> Integer.toString(mailing.getId()))
                        .collect(Collectors.toList());
            }
        }
    }

    private Map<String, LocalDate> getDatesRangedRestrictionMap(Map<String, Object> settings, DateTimeFormatter formatter) {
        int dateRange = NumberUtils.toInt(BirtReportSettingsUtils.getSettingsProperty(settings, DATE_RANGE_KEY));
        PeriodType type = null;

        switch (dateRange) {
            case DATE_RANGE_CUSTOM:
                type = PeriodType.DATE_RANGE_CUSTOM;
                break;
            case DATE_RANGE_PREDEFINED:
                switch (NumberUtils.toInt(BirtReportSettingsUtils.getSettingsProperty(settings, DATE_RANGE_PREDEFINED_KEY))) {
                    case BirtReportDateRangedSettings.DATE_RANGE_PREDEFINED_WEEK:
                        type = PeriodType.DATE_RANGE_WEEK;
                        break;
                    case BirtReportDateRangedSettings.DATE_RANGE_PREDEFINED_30_DAYS:
                        type = PeriodType.DATE_RANGE_30DAYS;
                        break;
                    case BirtReportDateRangedSettings.DATE_RANGE_PREDEFINED_LAST_MONTH:
                        type = PeriodType.DATE_RANGE_LAST_MONTH;
                        break;
                    case BirtReportDateRangedSettings.DATE_RANGE_PREDEFINED_THREE_MONTHS:
                        type = PeriodType.DATE_RANGE_THREE_MONTH;
                        break;
                    default:
                        //nothing do
                        break;
                }
                break;
            default:
                // nothing do
                break;
        }

        return getDatesRestrictionMap(type, settings, formatter);
    }

    private int getLastNumberValue(String predefinedMailings) {
        return PredefinedType.getLastNumberValue(predefinedMailings);
    }

    public void setBirtStatisticsService(BirtStatisticsService birtStatisticsService) {
        this.birtStatisticsService = birtStatisticsService;
    }

    public void setCampaignDao(CampaignDao campaignDao) {
        this.campaignDao = campaignDao;
    }

    public void setConversionService(ExtendedConversionService conversionService) {
        this.conversionService = conversionService;
    }

    public void setBirtReportFileService(BirtReportFileService birtReportFileService) {
        this.birtReportFileService = birtReportFileService;
    }

    public void setConfigService(ConfigService configService) {
        this.configService = configService;
    }
}
