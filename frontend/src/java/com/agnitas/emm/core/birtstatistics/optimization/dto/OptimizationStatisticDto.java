/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/


package com.agnitas.emm.core.birtstatistics.optimization.dto;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.ListUtils;

public class OptimizationStatisticDto {
    
    private static final String DEFAULT_OPTIMIZATION_REPORT_NAME = "mailing_summary.rptdesign";
    
    private String reportName;
    private String format;
    private int mailingId;
    private int optimizationId;
    private int companyId;
    private List<Integer> targetIds = new ArrayList<>();
    private String recipientType;
    
    public OptimizationStatisticDto() {
        this.reportName = DEFAULT_OPTIMIZATION_REPORT_NAME;
    }
    
    public String getReportName() {
        return reportName;
    }
    
    public void setReportName(String reportName) {
        this.reportName = reportName;
    }
    
    public void setFormat(String format) {
        this.format = format;
    }
    
    public String getFormat() {
        return format;
    }
    
    public void setMailingId(int mailingId) {
        this.mailingId = mailingId;
    }
    
    public int getMailingId() {
        return mailingId;
    }
    
    public void setOptimizationId(int optimizationId) {
        this.optimizationId = optimizationId;
    }
    
    public int getOptimizationId() {
        return optimizationId;
    }
    
    public void setCompanyId(int companyId) {
        this.companyId = companyId;
    }
    
    public int getCompanyId() {
        return companyId;
    }
    
    public void setRecipientType(String recipientType) {
        this.recipientType = recipientType;
    }
    
    public String getRecipientType() {
        return recipientType;
    }
    
    public void setTargetIds(List<Integer> targetIds) {
        this.targetIds = ListUtils.emptyIfNull(targetIds);
    }
    
    public List<Integer> getTargetIds() {
        return targetIds;
    }
}
