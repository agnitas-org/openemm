/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtreport.bean.impl;

import java.util.HashMap;
import java.util.Map;

import com.agnitas.emm.core.birtreport.bean.BirtReport;
import com.agnitas.emm.core.birtreport.bean.BirtReportFactory;
import com.agnitas.emm.core.birtreport.dto.ReportSettingsType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

@Component("BirtReportFactory")
public class BirtReportFactoryImpl implements BirtReportFactory {
    
    private static final Logger logger = LogManager.getLogger(BirtReportFactoryImpl.class);
    
    @Override
    public BirtReportSettings createReportSettings(ReportSettingsType type) {
        switch (type) {
            case COMPARISON:
                return new BirtReportComparisonSettings();
            case MAILING:
                return new BirtReportMailingSettings();
            case RECIPIENT:
                return new BirtReportRecipientSettings();
            case TOP_DOMAIN:
                return new BirtReportTopDomainsSettings();
            default:
        }
        logger.warn("Unsupported birt report settings type: {}", type);
        return null;
    }
    
    @Override
    public BirtReport createReport() {
        Map<ReportSettingsType, BirtReportSettings> settings = new HashMap<>();
        for (ReportSettingsType type: ReportSettingsType.values()) {
            settings.put(type, createReportSettings(type));
        }
        return new BirtReportImpl(settings);
    }
}
