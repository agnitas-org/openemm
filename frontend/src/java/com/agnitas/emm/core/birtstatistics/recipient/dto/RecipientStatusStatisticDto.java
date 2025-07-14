/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtstatistics.recipient.dto;

// TODO: EMMGUI-714: check usages and remove if redundant after remove of old design
public class RecipientStatusStatisticDto {
    
    private static final String DEFAULT_RECIPIENT_STATUS_REPORT_NAME = "recipient_status.rptdesign";
    
    private String reportName;
    private int mediaType;
    private int mailinglistId;
    private int targetId;
    private String format;
    
    public RecipientStatusStatisticDto() {
        setReportName(DEFAULT_RECIPIENT_STATUS_REPORT_NAME);
    }
    
    public void setReportName(String reportName) {
        this.reportName = reportName;
    }
    
    public String getReportName() {
        return reportName;
    }
    
    public void setMediaType(int mediaType) {
        this.mediaType = mediaType;
    }
    
    public int getMediaType() {
        return mediaType;
    }
    
    public void setMailinglistId(int mailinglistId) {
        this.mailinglistId = mailinglistId;
    }
    
    public int getMailinglistId() {
        return mailinglistId;
    }
    
    public void setTargetId(int targetId) {
        this.targetId = targetId;
    }
    
    public int getTargetId() {
        return targetId;
    }
    
    public void setFormat(String format) {
        this.format = format;
    }
    
    public String getFormat() {
        return format;
    }
}
