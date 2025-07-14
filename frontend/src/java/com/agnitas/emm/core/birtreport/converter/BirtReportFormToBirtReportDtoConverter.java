/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtreport.converter;

import static com.agnitas.emm.core.birtreport.bean.impl.BirtReportMailingSettings.MAILINGS_TO_SEND_KEY;
import static com.agnitas.emm.core.birtreport.bean.impl.BirtReportSettings.ENABLED_KEY;
import static com.agnitas.emm.core.birtreport.bean.impl.BirtReportSettings.MAILINGLISTS_KEY;
import static com.agnitas.emm.core.birtreport.bean.impl.BirtReportSettings.MAILINGS_KEY;
import static com.agnitas.emm.core.birtreport.bean.impl.BirtReportSettings.SORT_MAILINGS_KEY;
import static com.agnitas.emm.core.birtreport.bean.impl.BirtReportSettings.SORT_NAME;
import static com.agnitas.emm.core.birtreport.bean.impl.BirtReportSettings.TARGETS_KEY;
import static com.agnitas.emm.core.birtreport.util.BirtReportSettingsUtils.END_DATE;
import static com.agnitas.emm.core.birtreport.util.BirtReportSettingsUtils.START_DATE;
import static com.agnitas.emm.core.birtreport.util.BirtReportSettingsUtils.packProperties;

import java.util.HashMap;
import java.util.Map;

import com.agnitas.util.AgnUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import com.agnitas.emm.core.birtreport.dto.BirtReportDto;
import com.agnitas.emm.core.birtreport.dto.BirtReportType;
import com.agnitas.emm.core.birtreport.dto.ReportSettingsType;
import com.agnitas.emm.core.birtreport.forms.BirtReportForm;
import com.agnitas.emm.core.birtreport.util.BirtReportSettingsUtils;

@Component
public class BirtReportFormToBirtReportDtoConverter implements Converter<BirtReportForm, BirtReportDto> {
    
    @Override
    public BirtReportDto convert(BirtReportForm form) {
        BirtReportDto report = new BirtReportDto();
        report.setId(form.getReportId());
        report.setShortname(form.getShortname());
        report.setDescription(form.getDescription());
        
        report.setEmailRecipientList(AgnUtils.removeObsoleteItemsFromList(AgnUtils.splitAndTrimList(form.getEmailAddresses())));
        report.setEmailSubject(form.getEmailSubject());
        report.setEmailDescription(form.getEmailDescription());

        if (hasActivatedDelivery(form)) {
            resolveReportType(form, report);
        }
        
        report.setFormat(form.getFormat());
        report.setActiveTab(form.getActiveTab());

        report.setSettings(convertSettings(form.getSettings()));

        return report;
    }
    
    private Map<ReportSettingsType, Map<String, Object>> convertSettings(Map<ReportSettingsType, Map<String, Object>> settings) {
        Map<ReportSettingsType, Map<String, Object>> map = new HashMap<>();
        for (ReportSettingsType type : ReportSettingsType.values()) {
            map.put(type, getReportSettings(type, settings.getOrDefault(type, new HashMap<>())));
        }
    
        return map;
    }
    
    private void resolveReportType(BirtReportForm form, BirtReportDto report) {
        int reportTypeCode = form.getType();
        BirtReportType reportType = BirtReportType.getTypeByCode(reportTypeCode);
    
        if (reportType == null) {
            report.setType(reportTypeCode);
            return;
        }
        
        String intervalpattern;
        switch (reportType) {
            case TYPE_DAILY:
                report.setType(reportTypeCode);
                intervalpattern = String.format("%02d%02d", form.getSendDate().get().getHour(), form.getSendDate().get().getMinute());
                report.setIntervalpattern(intervalpattern);
                break;
            case TYPE_WEEKLY:
                report.setType(reportTypeCode);
                intervalpattern = BirtReportSettingsUtils.getDayOfWeekPattern(form.getSendWeekDays().entrySet())
                		+ String.format(":%02d%02d", form.getSendDate().get().getHour(), form.getSendDate().get().getMinute());
                report.setIntervalpattern(intervalpattern);
                break;
            case TYPE_BIWEEKLY:
                report.setType(reportTypeCode);
                intervalpattern = BirtReportSettingsUtils.getDayOfWeekPattern(form.getSendWeekDays().entrySet())
                		+ String.format("Ev:%02d%02d", form.getSendDate().get().getHour(), form.getSendDate().get().getMinute());
                report.setIntervalpattern(intervalpattern);
                break;
            case TYPE_MONTHLY_FIRST:
            case TYPE_MONTHLY_15TH:
            case TYPE_MONTHLY_LAST:
            	// "reportType" only shows the value TYPE_MONTHLY_FIRST for defined monthly report. "form.getReportMonthly()" contains the day of month to execute the report
            	switch (BirtReportType.getTypeByCode(form.getReportMonthly())) {
	                case TYPE_MONTHLY_FIRST:
	                    report.setType(form.getReportMonthly());
	                    intervalpattern = String.format("M01:%02d%02d", form.getSendDate().get().getHour(), form.getSendDate().get().getMinute());
	                    report.setIntervalpattern(intervalpattern);
	                    break;
	                case TYPE_MONTHLY_15TH:
	                    report.setType(form.getReportMonthly());
	                    intervalpattern = String.format("M15:%02d%02d", form.getSendDate().get().getHour(), form.getSendDate().get().getMinute());
	                    report.setIntervalpattern(intervalpattern);
	                    break;
	                case TYPE_MONTHLY_LAST:
		                report.setType(form.getReportMonthly());
		                intervalpattern = String.format("M99:%02d%02d", form.getSendDate().get().getHour(), form.getSendDate().get().getMinute());
		                report.setIntervalpattern(intervalpattern);
		                break;
	                default:
	                	// Fallback to TYPE_MONTHLY_FIRST
	                    intervalpattern = String.format("M01:%02d%02d", form.getSendDate().get().getHour(), form.getSendDate().get().getMinute());
	                    report.setIntervalpattern(intervalpattern);
	                    break;
            	}
                break;
            case TYPE_AFTER_MAILING_24HOURS:
                report.setType(form.getAfterMailing());
                report.setIntervalpattern("");
                break;
            case TYPE_AFTER_MAILING_48HOURS:
                report.setType(form.getAfterMailing());
                report.setIntervalpattern("");
                break;
            case TYPE_AFTER_MAILING_WEEK:
                report.setType(form.getAfterMailing());
                report.setIntervalpattern("");
                break;
            default:
                report.setType(reportTypeCode);
                report.setIntervalpattern("");
        }
    }

    private static boolean hasActivatedDelivery(BirtReportForm form) {
        return form.getSettings().values().stream()
                .map(settings -> settings.get("enabled"))
                .anyMatch("true"::equals);
    }
    
    private Map<String, Object> getReportSettings(ReportSettingsType type, Map<String, Object> settingsByType) {
        Map<String, Object> parameters = new HashMap<>(settingsByType);
    
        parameters.put(ENABLED_KEY, settingsByType.getOrDefault(ENABLED_KEY, false));
        parameters.put(START_DATE, settingsByType.getOrDefault(START_DATE, ""));
        parameters.put(END_DATE, settingsByType.getOrDefault(END_DATE, ""));
        parameters.put(TARGETS_KEY, BirtReportSettingsUtils.convertListToString(parameters, TARGETS_KEY));
        parameters.putAll(packProperties(type,  settingsByType));

        switch (type) {
            case COMPARISON:
                parameters.put(MAILINGS_KEY, BirtReportSettingsUtils.convertListToString(parameters, MAILINGS_KEY));
                parameters.put(SORT_MAILINGS_KEY, settingsByType.getOrDefault(SORT_MAILINGS_KEY, SORT_NAME));
                break;
            case MAILING:
                parameters.put(MAILINGS_KEY, BirtReportSettingsUtils.convertListToString(parameters, MAILINGS_KEY));
                parameters.put(MAILINGS_TO_SEND_KEY, BirtReportSettingsUtils.convertListToString(parameters, MAILINGS_TO_SEND_KEY));
                parameters.put(SORT_MAILINGS_KEY, settingsByType.getOrDefault(SORT_MAILINGS_KEY, SORT_NAME));
                break;
            case RECIPIENT:
            case TOP_DOMAIN:
                parameters.put(MAILINGLISTS_KEY, BirtReportSettingsUtils.convertListToString(parameters, MAILINGLISTS_KEY));
                break;
            default:
                //nothing to do
        }
        
        return parameters;
    }
}
