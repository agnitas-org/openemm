/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtreport.bean.impl;


import static com.agnitas.emm.core.birtreport.bean.impl.ComBirtReportMailingSettings.DATE_RANGE_WEEK;
import static com.agnitas.emm.core.birtreport.bean.impl.ComBirtReportMailingSettings.PERIOD_TYPE_KEY;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.agnitas.emm.common.MailingType;
import com.agnitas.emm.core.birtreport.bean.ComBirtReport;
import com.agnitas.emm.core.birtreport.dto.ReportSettingsType;
import com.agnitas.emm.core.birtreport.util.BirtReportSettingsUtils;
import com.agnitas.reporting.birt.external.utils.BirtReporUtils;

public class ComBirtReportComparisonSettings extends ComBirtReportSettings {

    @Override
    public void loadDefaults() {
        super.loadDefaults();
        setReportSetting(MAILING_TYPE_KEY, MAILINGS_PREDEFINED);
        setReportSetting(SORT_MAILINGS_KEY, SORT_NAME);
        setReportSetting(PERIOD_TYPE_KEY, DATE_RANGE_WEEK);
        setReportSetting(ARCHIVED_ID, 0);
    }

    @Override
    public ReportSettingsType getReportSettingsType() {
        return ReportSettingsType.COMPARISON;
    }
    
    @Override
    public int getTypeId() {
        return getReportSettingsType().getKey();
    }

    @Override
    public String getReportName(String reportFormat) {
        if (StringUtils.equalsIgnoreCase(ComBirtReport.FORMAT_CSV, reportFormat)) {
            return "mailings_compare_report_csv.rptdesign";
        } else {
            return "mailings_compare_report.rptdesign";
        }
    }

    public String getSortBy() {
        String sortBy = getReportSettingAsString(SORT_MAILINGS_KEY);
        return StringUtils.defaultIfEmpty(StringUtils.trimToEmpty(sortBy), SORT_NAME);
    }

    @Override
    public Map<String, String> getReportUrlParameters() {
		Map<String, String> parameters = new HashMap<>();
		parameters.put(TARGET_GROUPS_KEY, generateExpression(getTargetGroups()));
		parameters.put(MAILINGS_KEY, generateExpression(getMailings()));
		parameters.put(FIGURES_KEY, BirtReporUtils.packFigures(getSettingsMap()));
		if (MailingType.ACTION_BASED.getCode() == getReportSettingAsInt(MAILING_TYPE_KEY)) {
            parameters.put(SORT_BY_KEY, "date");
        } else {
            parameters.put(SORT_BY_KEY, getSortBy());
        }
        parameters.put(MAILING_FILTER_KEY, getReportSettingAsString(MAILING_FILTER_KEY, "0"));
        parameters.put(PREDEFINED_ID_KEY, getReportSettingAsString(PREDEFINED_ID_KEY, "0"));
		return parameters;
    }

    @Override
    public Set<String> getMissingReportParameters(int intervalReportType) {
        Set<String> missing = super.getMissingReportParameters(intervalReportType);
        if (missing.contains(ComBirtReportSettings.TARGET_GROUPS_KEY)) {
            String mailingType = getReportSettingAsString(ComBirtReportSettings.MAILING_TYPE_KEY);
            
            boolean isPredefined = BirtReportSettingsUtils.equalParameter(mailingType, ComBirtReportSettings.MAILINGS_PREDEFINED);
            boolean isCustom = BirtReportSettingsUtils.equalParameter(mailingType, BirtReportSettingsUtils.MAILINGS_CUSTOM);
            
            if (isPredefined || isCustom) {
                missing.remove(ComBirtReportSettings.TARGET_GROUPS_KEY);
            }
        }
        return missing;
    }

    public int getPeriodType() {
        return getReportSettingAsInt(PERIOD_TYPE_KEY);
    }
}
