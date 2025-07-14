/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtreport.converter;

import static com.agnitas.emm.core.birtreport.bean.impl.BirtReportSettings.MAILINGLISTS_KEY;
import static com.agnitas.emm.core.birtreport.bean.impl.BirtReportSettings.MAILINGS_KEY;
import static com.agnitas.emm.core.birtreport.bean.impl.BirtReportSettings.TARGETS_KEY;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import com.agnitas.emm.core.birtreport.bean.BirtReport;
import com.agnitas.emm.core.birtreport.bean.impl.BirtReportSettings;
import com.agnitas.emm.core.birtreport.dto.BirtReportDto;
import com.agnitas.emm.core.birtreport.dto.ReportSettingsType;

@Component
public class BirtReportToBirtReportDtoConverter implements Converter<BirtReport, BirtReportDto> {
    
    @Override
    public BirtReportDto convert(BirtReport source) {
        BirtReportDto report = new BirtReportDto();
        
        report.setId(source.getId());
        report.setShortname(source.getShortname());
        report.setDescription(source.getDescription());
        
        report.setEmailRecipientList(source.getEmailRecipientList());
        report.setEmailSubject(source.getEmailSubject());
        report.setEmailDescription(source.getEmailDescription());
        
        report.setType(source.getReportType());
        
        report.setFormat(source.getFormat());
        report.setEndDate(source.getEndDate());
        
        report.setActiveTab(source.getActiveTab());
        
        report.setSettings(convertSettings(source.getSettings()));

        report.setIntervalpattern(source.getIntervalpattern());
        report.setLastresult(source.getLastresult());
        report.setNextStart(source.getNextStart());
        
        return report;
    }
    
    private Map<ReportSettingsType, Map<String, Object>> convertSettings(List<BirtReportSettings> settings) {
        Map<ReportSettingsType, Map<String, Object>> settingsConverted = new HashMap<>();
        for (BirtReportSettings setting : settings) {
            Map<String, Object> settingsMap = setting.getSettingsMap();
            settingsMap.put(TARGETS_KEY, setting.getTargetGroupsAsInt());
            settingsMap.put(MAILINGS_KEY, setting.getMailingsAsInt());
            settingsMap.put(MAILINGLISTS_KEY, setting.getMailinglistsAsInt());
            settingsConverted.put(setting.getReportSettingsType(), settingsMap);
        }
    
        return settingsConverted;
    }
}
