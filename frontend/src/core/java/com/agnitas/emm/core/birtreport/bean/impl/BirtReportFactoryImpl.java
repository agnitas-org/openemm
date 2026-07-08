/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtreport.bean.impl;

import java.util.EnumMap;
import java.util.Map;

import com.agnitas.emm.core.birtreport.bean.BirtReport;
import com.agnitas.emm.core.birtreport.bean.BirtReportFactory;
import com.agnitas.emm.core.birtreport.dto.ReportSettingsType;
import org.springframework.stereotype.Component;

@Component("birtReportFactory")
public class BirtReportFactoryImpl implements BirtReportFactory {
    
    @Override
    public BirtReportSettings createReportSettings(ReportSettingsType type) {
        return switch (type) {
            case COMPARISON -> new BirtReportComparisonSettings();
            case MAILING -> new BirtReportMailingSettings();
            case RECIPIENT -> new BirtReportRecipientSettings();
            case TOP_DOMAIN -> new BirtReportTopDomainsSettings();
        };
    }
    
    @Override
    public BirtReport createReport() {
        Map<ReportSettingsType, BirtReportSettings> settings = new EnumMap<>(ReportSettingsType.class);
        for (ReportSettingsType type: ReportSettingsType.values()) {
            settings.put(type, createReportSettings(type));
        }
        return new BirtReportImpl(settings);
    }
}
