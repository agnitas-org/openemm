/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.recipientsreport.bean;

import java.util.Date;

public class RecipientsReport {

    private int id;
    private int autoImportID = -1;
    private Date reportDate;
    private String reportDateFormatted;
    /**
     * file name of report file
     */
    private String filename;
    /**
     * datasource ID created by import (if type = {@link RecipientReportType#IMPORT_REPORT})
     */
    private Integer datasourceId;
    /**
     * EMM user (who initiated the import)
     */
    private int adminId;

    /**
     * EMM user name (who initiated the import)
     */
    private String username;

    private RecipientReportType type;

    /**
     * download_tbl.download_id -> used if file can`t be stored in content(CLOB) column of recipients_report_tbl
     */
    private Integer fileId;
    
    private boolean isError;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAutoImportID() {
		return autoImportID;
	}

	public void setAutoImportID(int autoImportID) {
		this.autoImportID = autoImportID;
	}

	public Date getReportDate() {
        return reportDate;
    }

    public void setReportDate(Date reportDate) {
        this.reportDate = reportDate;
    }

    /**
     * file name of report file
     */
    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    /**
     * datasource ID created by import (if type = {@link RecipientReportType#IMPORT_REPORT})
     */
    public Integer getDatasourceId() {
        return datasourceId;
    }

    public void setDatasourceId(Integer datasourceId) {
        this.datasourceId = datasourceId;
    }

    /**
     * EMM user (who initiated the import)
     */
    public int getAdminId() {
        return adminId;
    }

    public void setAdminId(int adminId) {
        this.adminId = adminId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public RecipientReportType getType() {
        return type;
    }

    public void setType(RecipientReportType type) {
        this.type = type;
    }

    public Integer getFileId() {
        return fileId;
    }

    public void setFileId(Integer fileId) {
        this.fileId = fileId;
    }

    public enum RecipientReportType {
        IMPORT_REPORT("recipient.reports.type.import.report"),
        EXPORT_REPORT("recipient.reports.type.export.report");
        
    	private String messageKey;
    	
        private RecipientReportType(String messageKey) {
        	this.messageKey = messageKey;
        }

		public String getMessageKey() {
			return messageKey;
		}
    }

	public void setIsError(boolean isError) {
		this.isError = isError;
	}

	public boolean isError() {
		return isError;
	}

	public String getReportDateFormatted() {
		return reportDateFormatted;
	}

	public void setReportDateFormatted(String reportDateFormatted) {
		this.reportDateFormatted = reportDateFormatted;
	}
}
