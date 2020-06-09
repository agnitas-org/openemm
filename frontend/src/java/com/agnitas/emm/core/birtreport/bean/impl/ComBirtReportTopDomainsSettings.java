/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtreport.bean.impl;

import java.util.Map;

import com.agnitas.emm.core.birtreport.dto.ReportSettingsType;
import org.apache.log4j.Logger;

import static com.agnitas.emm.core.birtstatistics.service.impl.BirtStatisticsServiceImpl.IS_TOP_LEVEL_DOMAIN;
import static com.agnitas.emm.core.birtstatistics.service.impl.BirtStatisticsServiceImpl.MAX_DOMAINS;

public class ComBirtReportTopDomainsSettings extends ComBirtReportDateRangedSettings {
    @SuppressWarnings("unused")
	private static final transient Logger logger = Logger.getLogger(ComBirtReportTopDomainsSettings.class);

    @Override
    public ReportSettingsType getReportSettingsType() {
        return ReportSettingsType.TOP_DOMAIN;
    }

    @Override
    public String getReportName(String reportFormat) {
        //todo: GWUA-4360  report name for top domains
        return "top_domains_report.rptdesign";
    }
    
    @Override
    public Map<String, String> getReportUrlParameters() {
        Map<String, String> parameters = super.getReportUrlParameters();
        parameters.put(MAX_DOMAINS, getReportSettingAsString(MAX_DOMAINS, "5"));
        parameters.put(IS_TOP_LEVEL_DOMAIN, "false");

        return parameters;
    }
}
