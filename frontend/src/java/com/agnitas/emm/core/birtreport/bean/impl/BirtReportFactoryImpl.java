/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtreport.bean.impl;

import java.util.HashMap;

import com.agnitas.emm.core.birtreport.bean.BirtReportFactory;
import com.agnitas.emm.core.birtreport.bean.ComBirtReport;
import com.agnitas.emm.core.birtreport.dto.ReportSettingsType;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

@Component("BirtReportFactory")
public class BirtReportFactoryImpl implements BirtReportFactory {
    
    private static final Logger logger = Logger.getLogger(BirtReportFactoryImpl.class);
    
    @Override
    public ComBirtReportSettings createReportSettings(ReportSettingsType type) {
        switch (type) {
            case COMPARISON:
                return new ComBirtReportComparisonSettings();
            case MAILING:
                return new ComBirtReportMailingSettings();
            case RECIPIENT:
                return new ComBirtReportRecipientSettings();
            case TOP_DOMAIN:
                return new ComBirtReportTopDomainsSettings();
            default:
        }
        logger.warn("Unsupported birt report settings type: " + type);
        return null;
    }
    
    @Override
    public ComBirtReport createReport() {
        HashMap<ReportSettingsType, ComBirtReportSettings> settings = new HashMap<>();
        for (ReportSettingsType type: ReportSettingsType.values()) {
            settings.put(type, createReportSettings(type));
        }
        return new ComBirtReportImpl(settings);
    }
}
