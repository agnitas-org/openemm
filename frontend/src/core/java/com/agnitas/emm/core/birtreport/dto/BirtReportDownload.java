/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.birtreport.dto;

public class BirtReportDownload {

    private String birtFileUrl; // TODO: GWUA-7067: Remove after migration
    private String fileName;
    private StatisticsReportRequest reportRequest;
    private int reportId;
    private String tmpFileName;
    private String shortname;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getBirtFileUrl() {
        return birtFileUrl;
    }

    public void setBirtFileUrl(String birtFileUrl) {
        this.birtFileUrl = birtFileUrl;
    }

    public StatisticsReportRequest getReportRequest() {
        return reportRequest;
    }

    public void setReportRequest(StatisticsReportRequest reportRequest) {
        this.reportRequest = reportRequest;
    }
    
    public int getReportId() {
        return reportId;
    }
    
    public void setReportId(int reportId) {
        this.reportId = reportId;
    }
    
    public void setTmpFileName(String tmpFileName) {
        this.tmpFileName = tmpFileName;
    }
    
    public String getTmpFileName() {
        return tmpFileName;
    }
    
    public void setShortname(String shortname) {
        this.shortname = shortname;
    }
    
    public String getShortname() {
        return shortname;
    }
}
