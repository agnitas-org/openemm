/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtreport.bean.impl;

import com.agnitas.emm.core.birtreport.bean.ComBirtReport;
import com.agnitas.emm.core.birtreport.dto.ReportSettingsType;
import org.apache.log4j.Logger;

public class ComBirtReportRecipientSettings extends ComBirtReportDateRangedSettings {
    @SuppressWarnings("unused")
	private static final transient Logger logger = Logger.getLogger(ComBirtReportRecipientSettings.class);

    @Override
    public ReportSettingsType getReportSettingsType() {
        return ReportSettingsType.RECIPIENT;
    }

    @Override
    public String getReportName(String reportFormat) {
        if (ComBirtReport.FORMAT_CSV.equals(reportFormat)) {
            return "recipients_statistic_csv.rptdesign";
        } else {
            return "recipients_statistic.rptdesign";
        }
    }
}
