/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtreport.service.impl;

import static com.agnitas.emm.core.birtreport.bean.impl.ComBirtReportMailingSettings.MAILINGS_TO_SEND_KEY;
import static com.agnitas.emm.core.birtreport.bean.impl.ComBirtReportMailingSettings.MAILING_NORMAL;
import static com.agnitas.emm.core.birtreport.bean.impl.ComBirtReportSettings.MAILINGS_KEY;
import static com.agnitas.emm.core.birtreport.bean.impl.ComBirtReportSettings.MAILING_FILTER_KEY;
import static com.agnitas.emm.core.birtreport.bean.impl.ComBirtReportSettings.MAILING_TYPE_KEY;
import static com.agnitas.emm.core.birtreport.bean.impl.ComBirtReportSettings.PREDEFINED_MAILINGS_KEY;
import static com.agnitas.emm.core.birtreport.bean.impl.ComBirtReportSettings.SORT_MAILINGS_KEY;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.agnitas.beans.MailingBase;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.DateUtilities;
import org.agnitas.util.DbUtilities;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.dao.ComMailingDao;
import com.agnitas.emm.core.birtreport.bean.ComBirtReport;
import com.agnitas.emm.core.birtreport.bean.impl.ComBirtReportComparisonSettings;
import com.agnitas.emm.core.birtreport.bean.impl.ComBirtReportMailingSettings;
import com.agnitas.emm.core.birtreport.bean.impl.ComBirtReportSettings;
import com.agnitas.emm.core.birtreport.dao.ComBirtReportDao;
import com.agnitas.emm.core.birtreport.dto.BirtReportType;
import com.agnitas.emm.core.birtreport.dto.PeriodType;
import com.agnitas.emm.core.birtreport.dto.PredefinedType;
import com.agnitas.emm.core.birtreport.service.ComBirtReportService;
import com.agnitas.emm.core.birtreport.util.BirtReportSettingsUtils;

public class ComBirtReportServiceImpl implements ComBirtReportService {
    /** The logger. */
    private static final transient Logger logger = Logger.getLogger(ComBirtReportServiceImpl.class);
    public static final String KEY_START_DATE = "from";
    public static final String KEY_END_DATE = "to";

    /** DAO accessing BIRT report data. */
    private ComBirtReportDao birtReportDao;
    
    /** DAO accessing mailing data. */
    private ComMailingDao mailingDao;

    private DataSource dataSource;

    @Override
    public boolean insert(ComBirtReport report) throws Exception{
        if(report.isReportActive() == 1){
            report.setActivationDate(new Date());
        }
        return birtReportDao.insert(report);
    }

    @Override
    public void logSentReport(ComBirtReport report){
        if (report.isTriggeredByMailing()){
            final ComBirtReportMailingSettings reportMailingSettings = report.getReportMailingSettings();
            final List<Integer> mailingsIdsToSend = reportMailingSettings.getMailingsIdsToSend();
            birtReportDao.insertSentMailings(report.getId(), report.getCompanyID(), mailingsIdsToSend);
            birtReportDao.deactivateBirtReport(report.getId());
            if (!mailingsIdsToSend.isEmpty()){
                reportMailingSettings.getSettingsMap().remove(MAILINGS_TO_SEND_KEY);
            }
        } else {
        	birtReportDao.insertSentMailings(report.getId(), report.getCompanyID(), null);
        }
    }

    protected List<MailingBase> getPredefinedMailingsForReports(@VelocityCheck int companyId, int number, int filterType, int filterValue, int mailingType, String orderKey) {
        return mailingDao.getPredefinedMailingsForReports(companyId, number, filterType, filterValue, mailingType, orderKey);
    }

    protected List<MailingBase> getPredefinedMailingsForReports(@VelocityCheck int companyId, int number, int filterType, int filterValue, int mailingType, String orderKey, Map<String, LocalDate> datesRestriction){
        if (number == 0) {
            Date from = DateUtilities.toDate(datesRestriction.get(KEY_START_DATE), AgnUtils.getSystemTimeZoneId());
            // Include the defined day completely, so use limit of next day 00:00
            Date to = DateUtilities.toDate(datesRestriction.get(KEY_END_DATE).plus(1, ChronoUnit.DAYS), AgnUtils.getSystemTimeZoneId());

            return mailingDao.getPredefinedNormalMailingsForReports(companyId, from, to, filterType, filterValue, orderKey);
        } else {
        	return mailingDao.getPredefinedMailingsForReports(companyId, number, filterType, filterValue, mailingType, orderKey);
        }
    }

    private boolean checkReportToSend(final ComBirtReport birtReport) {
        try {
            if (birtReport.isTriggeredByMailing()) {
                ComBirtReportMailingSettings mailingSettings = birtReport.getReportMailingSettings();
                if (!mailingSettings.isEnabled()) {
                    return false;
                }
                BirtReportType reportType = BirtReportType.getTypeByCode(birtReport.getReportType());
                
                if(reportType == null) {
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

                int filter = mailingSettings.getReportSettingAsInt(MAILING_FILTER_KEY,  BirtReportSettingsUtils.FILTER_NO_FILTER_VALUE);
                int filterValue = mailingSettings.getReportSettingAsInt(ComBirtReportSettings.PREDEFINED_ID_KEY);

                final List<Integer> mailingsToSend = mailingDao.getBirtReportMailingsToSend(birtReport.getCompanyID(), birtReport.getId(), startDate, endDate, filter, filterValue);
                mailingSettings.setMailingsToSend(mailingsToSend);
                return !mailingsToSend.isEmpty();
            } else {
                ComBirtReportMailingSettings mailingSettings = birtReport.getReportMailingSettings();
                updateSelectedMailingIds(mailingSettings, birtReport.getCompanyID());
                final List<Integer> mailingsToSend = mailingSettings.getMailingsIdsToSend();
                boolean doMailingReport = mailingSettings.isEnabled() && !mailingsToSend.isEmpty();

                ComBirtReportComparisonSettings comparisonSettings = birtReport.getReportComparisonSettings();
                updateSelectedComparisonIds(comparisonSettings, birtReport.getCompanyID());
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

    protected void updateSelectedMailingIds(ComBirtReportMailingSettings reportMailingSettings, int companyId) {
        List<Integer> mailingIds;
        int mailingType = reportMailingSettings.getMailingType();
        int mailingGeneralType = reportMailingSettings.getMailingGeneralType();
        if (mailingGeneralType == MAILING_NORMAL) {
            if (mailingType == ComBirtReportMailingSettings.MAILING_PREDEFINED) {
                int filterValue = 0;
                int filterType = reportMailingSettings.getReportSettingAsInt(MAILING_FILTER_KEY, BirtReportSettingsUtils.FILTER_NO_FILTER_VALUE);
                int numOfMailings = getLastNumberValue(reportMailingSettings.getPredefinedMailings());
                String sortOrder = reportMailingSettings.getReportSettingAsString(SORT_MAILINGS_KEY);
                if (filterType == BirtReportSettingsUtils.FILTER_ARCHIVE_VALUE || filterType == BirtReportSettingsUtils.FILTER_MAILINGLIST_VALUE){
                    filterValue = reportMailingSettings.getReportSettingAsInt(ComBirtReportMailingSettings.PREDEFINED_ID_KEY);
                }
                mailingIds = getPredefinedMailingsForReports(companyId, numOfMailings, filterType, filterValue, -1, sortOrder)
                        .stream()
                        .map(MailingBase::getId)
                        .collect(Collectors.toList());
                reportMailingSettings.setMailingsToSend(mailingIds);
            }
        } else if(mailingGeneralType == ComBirtReportMailingSettings.MAILING_ACTION_BASED || mailingGeneralType == ComBirtReportMailingSettings.MAILING_DATE_BASED){
            Object reportSetting = reportMailingSettings.getReportSetting(MAILINGS_KEY);
            reportMailingSettings.setReportSetting(MAILINGS_TO_SEND_KEY,reportSetting);
        }
    }

    protected void updateSelectedComparisonIds(ComBirtReportComparisonSettings reportComparisonSettings, int companyId) {
       updateSelectedComparisonIds(reportComparisonSettings, null, companyId);
    }

    protected void updateSelectedComparisonIds(ComBirtReportComparisonSettings reportComparisonSettings, DateTimeFormatter formatter, int companyId) {
        int mailingType = reportComparisonSettings.getReportSettingAsInt(MAILING_TYPE_KEY);

        if (mailingType == ComBirtReportSettings.MAILINGS_PREDEFINED) {
            String sortOrder = reportComparisonSettings.getReportSettingAsString(SORT_MAILINGS_KEY);
            int numOfMailings = reportComparisonSettings.getReportSettingAsInt(PREDEFINED_MAILINGS_KEY);
            int filterType = reportComparisonSettings.getReportSettingAsInt(MAILING_FILTER_KEY, BirtReportSettingsUtils.FILTER_NO_FILTER_VALUE);
            int filterValue = 0;
            if ((filterType == BirtReportSettingsUtils.FILTER_ARCHIVE_VALUE || filterType == BirtReportSettingsUtils.FILTER_MAILINGLIST_VALUE)) {
                filterValue = reportComparisonSettings.getReportSettingAsInt(ComBirtReportComparisonSettings.PREDEFINED_ID_KEY);
            }
            if (numOfMailings >= 0) {
                PeriodType periodType = PeriodType.getTypeByKey(reportComparisonSettings.getPeriodType());
                Map<String, LocalDate> datesRestriction = getDatesRestrictionMap(periodType, reportComparisonSettings.getSettingsMap(), formatter);
                String[] mailingIds = getPredefinedMailingsForReports(companyId, numOfMailings, filterType, filterValue, -1, sortOrder, datesRestriction)
                        .stream()
                        .map(mailing -> Integer.toString(mailing.getId()))
                        .toArray(String[]::new);

                reportComparisonSettings.setMailings(mailingIds);
            }
        }
    }

    protected Map<String, LocalDate> getDatesRestrictionMap(PeriodType periodType, Map<String, Object> settings, DateTimeFormatter dateTimeFormatter) {
        if(Objects.isNull(periodType)) {
            return new HashMap<>();
        }

        LocalDate to = LocalDate.now();
        LocalDate from = LocalDate.now();

        switch (periodType) {
            case DATE_RANGE_CUSTOM:
                String startDate = BirtReportSettingsUtils.getSettingsProperty(settings, BirtReportSettingsUtils.START_DATE);
                String stopDate = BirtReportSettingsUtils.getSettingsProperty(settings, BirtReportSettingsUtils.END_DATE);

                if (StringUtils.isNotEmpty(startDate) && StringUtils.isNotEmpty(stopDate)){
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
                }
                break;
            case DATE_RANGE_MONTH:
                from = to.minusMonths(1);
                break;
            case DATE_RANGE_WEEK:
                from = to.minusWeeks(1);
                break;
            case DATE_RANGE_DAY:
                from = to.minusDays(1);
                break;
            case DATE_RANGE_THREE_MONTH:
                from = to.minusDays(1);
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

    private int getLastNumberValue(String predefinedMailings) {
        return PredefinedType.getLastNumberValue(predefinedMailings);
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
    @Required
    public void setBirtReportDao(ComBirtReportDao birtReportDao) {
        this.birtReportDao = birtReportDao;
    }

    /**
     * Set DAO accessing mailing data.
     * 
     * @param mailingDao DAO accessing mailing data
     */
    @Required
    public void setMailingDao(ComMailingDao mailingDao) {
        this.mailingDao = mailingDao;
    }

    public ComMailingDao getMailingDao() {
        return mailingDao;
    }

    @Required
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

	@Override
	public boolean announceStart(ComBirtReport birtReport) {
		return birtReportDao.announceStart(birtReport);
	}

	@Override
	public void announceEnd(ComBirtReport birtReport) {
		birtReportDao.announceEnd(birtReport);
	}

	@Override
	public int getRunningReportsByHost(String hostName) {
		return birtReportDao.getRunningReportsByHost(hostName);
	}

	@Override
	public List<ComBirtReport> getReportsToSend(int maximumNumberOfReports, List<Integer> includedCompanyIds, List<Integer> excludedCompanyIds) {
		if (maximumNumberOfReports <= 0) {
			return new ArrayList<>();
		} else {
			final List<ComBirtReport> reportsList =  birtReportDao.getReportsToSend(includedCompanyIds, excludedCompanyIds);
			List<ComBirtReport> reportsToSend = new ArrayList<>();
			for (ComBirtReport birtReport : reportsList) {
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
	}
}
