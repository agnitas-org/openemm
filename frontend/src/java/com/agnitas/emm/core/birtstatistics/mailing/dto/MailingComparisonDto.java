/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtstatistics.mailing.dto;

import java.util.List;

public class MailingComparisonDto {
    private static final String DEFAULT_COMPARISON_REPORT_NAME = "mailing_compare.rptdesign";
    
    private String reportName;
    private int targetId;
    private List<Integer> targetIds;
    private String recipientType;
    private List<Integer> mailingIds;
    private String reportFormat;
    
    public MailingComparisonDto() {
        setReportName(DEFAULT_COMPARISON_REPORT_NAME);
    }
    
    public String getReportName() {
        return reportName;
    }
    
    public void setReportName(String reportName) {
        this.reportName = reportName;
    }
    
    public int getTargetId() {
        return targetId;
    }
    
    public void setTargetId(int targetId) {
        this.targetId = targetId;
    }
    
    public List<Integer> getTargetIds() {
        return targetIds;
    }
    
    public void setTargetIds(List<Integer> targetIds) {
        this.targetIds = targetIds;
    }
    
    public String getRecipientType() {
        return recipientType;
    }
    
    public void setRecipientType(String recipientType) {
        this.recipientType = recipientType;
    }
    
    public List<Integer> getMailingIds() {
        return mailingIds;
    }
    
    public void setMailingIds(List<Integer> mailingIds) {
        this.mailingIds = mailingIds;
    }
    
    public String getReportFormat() {
        return reportFormat;
    }
    
    public void setReportFormat(String reportFormat) {
        this.reportFormat = reportFormat;
    }
}
