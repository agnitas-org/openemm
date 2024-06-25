/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtreport.dto;

import java.util.HashMap;
import java.util.Map;

public class BirtReportStatisticDto {
    
    private int reportId;
    private String reportName;
    private String reportFormat;
    private Map<String, String> reportUrlParameters = new HashMap<>();
    
    public int geReportId() {
        return reportId;
    }
    
    public void setReportId(int reportId) {
        this.reportId = reportId;
    }
    
    public String getReportName() {
        return reportName;
    }
    
    public void setReportName(String reportName) {
        this.reportName = reportName;
    }
    
    public String getReportFormat() {
        return reportFormat;
    }
    
    public void setReportFormat(String reportFormat) {
        this.reportFormat = reportFormat;
    }
    
    public Map<String, String> getReportUrlParameters() {
        return reportUrlParameters;
    }
    
    public void setReportUrlParameters(Map<String, String> reportUrlParameters) {
        this.reportUrlParameters = reportUrlParameters;
    }
}
