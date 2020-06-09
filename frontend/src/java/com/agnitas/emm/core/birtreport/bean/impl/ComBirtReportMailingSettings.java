/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtreport.bean.impl;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.agnitas.util.EmmCalendar;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.agnitas.emm.core.birtreport.bean.ComBirtReport;
import com.agnitas.emm.core.birtreport.dto.BirtReportType;
import com.agnitas.emm.core.birtreport.dto.ReportSettingsType;
import com.agnitas.emm.core.birtreport.util.BirtReportSettingsUtils;
import com.agnitas.reporting.birt.external.utils.BirtReporUtils;

public class ComBirtReportMailingSettings extends ComBirtReportSettings {
    @SuppressWarnings("unused")
	private static final transient Logger logger = Logger.getLogger(ComBirtReportMailingSettings.class);

    public static final String ACTION_MAILING_ID_KEY = "actionMailingId";
    public static final String MAILINGS_TO_SEND_KEY = "mailingsToSend";
    public static final String MAILING_GENERAL_TYPES_KEY = "mailingGeneralType";
    
    public static final String PERIOD_TYPE_KEY = "periodType";
    
    public static final int MAILING_PREDEFINED = 1;
    public static final int MAILING_ACTION_OR_DATE_BASED = 2;
    public static final int MAILING_NORMAL = 3;
    public static final int MAILING_ACTION_BASED = 4;
    public static final int MAILING_DATE_BASED = 5;
    public static final int DATE_RANGE_WEEK = 6;
    public static final int DATE_RANGE_MONTH = 7;
    public static final int DATE_RANGE_CUSTOM = 8;
    public static final int DATE_RANGE_DAY = 9;

    @Override
    public void loadDefaults() {
        super.loadDefaults();
        setReportSetting(MAILING_GENERAL_TYPES_KEY, MAILING_NORMAL);
        setReportSetting(MAILING_TYPE_KEY, MAILING_PREDEFINED);
        setReportSetting(PERIOD_TYPE_KEY, DATE_RANGE_WEEK);
        setReportSetting(SORT_MAILINGS_KEY, SORT_NAME);
    }

    @Override
    public ReportSettingsType getReportSettingsType() {
        return ReportSettingsType.MAILING;
    }

    @Override
    public String getReportName(String reportFormat) {
        if (StringUtils.equalsIgnoreCase(ComBirtReport.FORMAT_CSV, reportFormat)) {
            return "mailing_statistic_csv.rptdesign";
        } else {
            return "mailing_statistic.rptdesign";
        }
    }

    @Override
    public Map<String, String> getReportUrlParameters() {
        Map<String, String> parameters = new HashMap<>();
        String targetGroupsExpression = generateExpression(getTargetGroups());
        if (StringUtils.isNotEmpty(targetGroupsExpression)) {
            parameters.put(TARGET_GROUPS_KEY, targetGroupsExpression);
        }
        parameters.put(MAILINGS_KEY, generateExpression(getMailings()));
        parameters.put(FIGURES_KEY, BirtReporUtils.packFigures(getSettingsMap()));
    
        if(!BirtReportSettingsUtils.equalParameter(getReportSettingAsString(MAILING_GENERAL_TYPES_KEY), MAILING_NORMAL)) {
            parameters.putAll(getDateRange());
        }
        return parameters;
    }

    @Override
    public Set<String> getMissingReportParameters(int intervalReportType) {
        BirtReportType type = BirtReportType.getTypeByCode(intervalReportType);
        if(type != null) {
            switch (type) {
                case TYPE_AFTER_MAILING_24HOURS:
                case TYPE_AFTER_MAILING_48HOURS:
                case TYPE_AFTER_MAILING_WEEK:
                    return Collections.emptySet();
                default:
                    // nothing do
            }
        }
        return super.getMissingReportParameters();
    }

    public int getPeriodType() {
        return getReportSettingAsInt(PERIOD_TYPE_KEY);
    }
    
    public void setMailingsToSend(List<Integer> mailingsToSend) {
        List<String> mailings = new ArrayList<>(mailingsToSend.size());
        for (Integer mailingId : mailingsToSend) {
            mailings.add(String.valueOf(mailingId));
        }
        String mailingIds = generateExpression(mailings);
        setReportSetting(MAILINGS_TO_SEND_KEY, mailingIds);
        setReportSetting(MAILINGS_KEY, mailingIds);
    }
    
    public String getMailingsToSend() {
        return getReportSettingAsString(MAILINGS_TO_SEND_KEY);
    }
    
    public int getMailingType() {
        return getReportSettingAsInt(MAILING_TYPE_KEY);
    }
    
    public int getMailingGeneralType() {
        return getReportSettingAsInt(MAILING_GENERAL_TYPES_KEY, MAILING_NORMAL);
    }
    
    public String getPredefinedMailings() {
        return getReportSettingAsString(PREDEFINED_MAILINGS_KEY);
    }
    
    public List<Integer> getMailingsIdsToSend() {
        return parseExpressionAsInt(getMailingsToSend());
    }
    
    private Map<String, String> getDateRange() {
        Map<String, String> dateRange =  new HashMap<>();
        String startDate = "";
        String stopDate = "";
        final SimpleDateFormat reportDateFormat = new SimpleDateFormat(BirtReportSettingsUtils.REPORT_DATE_FORMAT);
        final Calendar calendar = new GregorianCalendar();
        calendar.setTime(new Date());

        // If Normal mailing selected we shouldn't add any values of startDate and stopDate to URL params
        if (!BirtReportSettingsUtils.equalParameter(getReportSettingAsString(MAILING_GENERAL_TYPES_KEY), MAILING_NORMAL)) {
            switch (getPeriodType()) {
                case DATE_RANGE_DAY:
                    SimpleDateFormat dayDateFormat = new SimpleDateFormat(BirtReportSettingsUtils.REPORT_DATE_FORMAT_FOR_DAY);
                    calendar.add(EmmCalendar.DAY_OF_YEAR, -1);
                    setDayStart(calendar);
                    startDate = dayDateFormat.format(calendar.getTime());
                    setDayEnd(calendar);
                    stopDate = dayDateFormat.format(calendar.getTime());
                    break;
                case DATE_RANGE_WEEK:
                    stopDate = reportDateFormat.format(calendar.getTime());
                    calendar.add(EmmCalendar.DAY_OF_YEAR, -7);
                    calendar.add(EmmCalendar.SECOND, -1);
                    startDate = reportDateFormat.format(calendar.getTime());
                    break;
                case DATE_RANGE_MONTH:
                    stopDate = reportDateFormat.format(calendar.getTime());
                    calendar.add(EmmCalendar.DAY_OF_YEAR, -30);
                    calendar.add(EmmCalendar.SECOND, -1);
                    startDate = reportDateFormat.format(calendar.getTime());
                    break;
                case DATE_RANGE_CUSTOM:
                    startDate = getReportSettingAsString(BirtReportSettingsUtils.START_DATE);
                    stopDate = getReportSettingAsString(BirtReportSettingsUtils.END_DATE);
                    break;
				default:
					throw new RuntimeException("Invalid period type");
            }
        }
        dateRange.put(BirtReportSettingsUtils.START_DATE, startDate);
        dateRange.put(BirtReportSettingsUtils.END_DATE, stopDate);

        return dateRange;
    }

    private void setDayStart(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
    }

    private void setDayEnd(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
    }
}
