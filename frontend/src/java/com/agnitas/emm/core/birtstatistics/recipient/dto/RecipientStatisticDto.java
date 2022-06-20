/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/


package com.agnitas.emm.core.birtstatistics.recipient.dto;

import java.time.LocalDate;

public class RecipientStatisticDto {
    private LocalDate localEndDate;
    private LocalDate localStartDate;
    private String reportName;
    private int mediaType;
    private int targetId;
    private int mailinglistId;
    private boolean hourScale;
    
    public void setLocalEndDate(LocalDate localEndDate) {
        this.localEndDate = localEndDate;
    }
    
    public LocalDate getLocalEndDate() {
        return localEndDate;
    }
    
    public void setLocalStartDate(LocalDate localStartDate) {
        this.localStartDate = localStartDate;
    }
    
    public LocalDate getLocaleStartDate() {
        return localStartDate;
    }
    
    public String getReportName() {
        return reportName;
    }
    
    public void setReportName(String reportName) {
        this.reportName = reportName;
    }
    
    public int getMediaType() {
        return mediaType;
    }
    
    public void setMediaType(int mediaType) {
        this.mediaType = mediaType;
    }
    
    public int getTargetId() {
        return targetId;
    }
    
    public void setTargetId(int targetId) {
        this.targetId = targetId;
    }
    
    public int getMailinglistId() {
        return mailinglistId;
    }
    
    public void setMailinglistId(int mailinglistId) {
        this.mailinglistId = mailinglistId;
    }
    
    public boolean isHourScale() {
        return hourScale;
    }
    
    public void setHourScale(boolean hourScale) {
        this.hourScale = hourScale;
    }
}
