/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtreport.converter;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import com.agnitas.emm.core.birtreport.dto.BirtReportDto;
import com.agnitas.emm.core.birtreport.dto.BirtReportType;
import com.agnitas.emm.core.birtreport.forms.BirtReportForm;
import com.agnitas.emm.core.birtreport.util.BirtReportSettingsUtils;

@Component
public class BirtReportDtoToBirtReportFormConverter implements Converter<BirtReportDto, BirtReportForm> {
    
    @Override
    public BirtReportForm convert(BirtReportDto source) {
        BirtReportForm reportForm = new BirtReportForm();
        
        reportForm.setReportId(source.getId());
        reportForm.setShortname(source.getShortname());
        reportForm.setDescription(source.getDescription());
    
        reportForm.setEmailAddresses(StringUtils.join(source.getEmailRecipientList(), ", "));
        reportForm.setEmailSubject(source.getEmailSubject());
        reportForm.setEmailDescription(source.getEmailDescription());
    
        resolveReportType(reportForm, source.getType());

        Map<Integer, Boolean> weekDays = new HashMap<>();
        String intervalPattern =  source.getIntervalpattern();
        if (StringUtils.isNotEmpty(intervalPattern)) {
            weekDays.put(Calendar.SUNDAY, BirtReportSettingsUtils.isWeekDayActive(intervalPattern, Calendar.SUNDAY));
            weekDays.put(Calendar.MONDAY, BirtReportSettingsUtils.isWeekDayActive(intervalPattern, Calendar.MONDAY));
            weekDays.put(Calendar.TUESDAY, BirtReportSettingsUtils.isWeekDayActive(intervalPattern, Calendar.TUESDAY));
            weekDays.put(Calendar.WEDNESDAY, BirtReportSettingsUtils.isWeekDayActive(intervalPattern, Calendar.WEDNESDAY));
            weekDays.put(Calendar.THURSDAY, BirtReportSettingsUtils.isWeekDayActive(intervalPattern, Calendar.THURSDAY));
            weekDays.put(Calendar.FRIDAY, BirtReportSettingsUtils.isWeekDayActive(intervalPattern, Calendar.FRIDAY));
            weekDays.put(Calendar.SATURDAY, BirtReportSettingsUtils.isWeekDayActive(intervalPattern, Calendar.SATURDAY));
        }
        reportForm.setSendWeekDays(weekDays);
        
        reportForm.setFormat(source.getFormat());
    
        reportForm.setActiveTab(source.getActiveTab());
        reportForm.setSettings(source.getSettings());

        return reportForm;
    }
    
    private void resolveReportType(BirtReportForm reportForm, int reportTypeCode) {
        BirtReportType reportType = BirtReportType.getTypeByCode(reportTypeCode);

        if(reportType != null) {
            switch (reportType) {
                case TYPE_DAILY:
                case TYPE_WEEKLY:
                case TYPE_BIWEEKLY:
                    reportForm.setType(reportTypeCode);
                    break;
                case TYPE_MONTHLY_FIRST:
                case TYPE_MONTHLY_15TH:
                case TYPE_MONTHLY_LAST:
                    reportForm.setType(BirtReportType.TYPE_MONTHLY_FIRST.getKey());
                    reportForm.setReportMonthly(reportTypeCode);
                    break;
                case TYPE_AFTER_MAILING_24HOURS:
                case TYPE_AFTER_MAILING_48HOURS:
                case TYPE_AFTER_MAILING_WEEK:
                    reportForm.setType(BirtReportType.TYPE_AFTER_MAILING_24HOURS.getKey());
                    reportForm.setAfterMailing(reportTypeCode);
                    reportForm.setReportMonthly(reportTypeCode);
                    break;
                default:
                    reportForm.setType(reportTypeCode);
            }
        } else {
            reportForm.setType(reportTypeCode);
        }
    }
}
