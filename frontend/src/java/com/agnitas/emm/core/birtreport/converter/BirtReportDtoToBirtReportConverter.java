/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtreport.converter;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import com.agnitas.emm.core.birtreport.bean.BirtReportFactory;
import com.agnitas.emm.core.birtreport.bean.ComBirtReport;
import com.agnitas.emm.core.birtreport.bean.impl.ComBirtReportSettings;
import com.agnitas.emm.core.birtreport.dto.BirtReportDto;
import com.agnitas.emm.core.birtreport.dto.ReportSettingsType;

@Component
public class BirtReportDtoToBirtReportConverter implements Converter<BirtReportDto, ComBirtReport> {
    
    @Autowired
    private BirtReportFactory birtReportSettingsFactory;

    @Override
    public ComBirtReport convert(BirtReportDto source) {
        ComBirtReport report = birtReportSettingsFactory.createReport();
        
        report.setId(source.getId());
        report.setShortname(source.getShortname());
        report.setDescription(source.getDescription());
        report.setReportType(source.getType());
        
        report.setFormat(source.getFormat());
        
        report.setEmailRecipientList(source.getEmailRecipientList());
        report.setEmailSubject(source.getEmailSubject());
        report.setEmailDescription(source.getEmailDescription());
                
        report.setEndDate(source.getEndDate());
        report.setActiveTab(source.getActiveTab());
        
        report.setHidden(false);
        report.setSettings(convertReportSettings(source.getSettings()));
        
        report.setIntervalpattern(source.getIntervalpattern());
        report.setLastresult(source.getLastresult());
        report.setNextStart(source.getNextStart());
        
        return report;
    }
    
    private Map<ReportSettingsType, ComBirtReportSettings> convertReportSettings(Map<ReportSettingsType, Map<String, Object>> settingsMap) {
        Map<ReportSettingsType, ComBirtReportSettings> map = new HashMap<>();
        for (ReportSettingsType type : ReportSettingsType.values()) {
             ComBirtReportSettings settings = birtReportSettingsFactory.createReportSettings(type);
             if (settings != null) {
                 settings.setSettingsMap(settingsMap.getOrDefault(type, new HashMap<>()));
             }
             map.put(type, settings);
        }
        return map;
    }
}
