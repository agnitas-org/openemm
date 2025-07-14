/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.recipientsreport.bean;

import java.util.Date;

import com.agnitas.beans.IntEnum;

public class RecipientsReport {

    private int id;
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

    private int entityId;
    private EntityType entityType;
    private EntityExecution entityExecution;
    private EntityData entityData;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public int getEntityId() {
        return entityId;
    }

    public void setEntityId(int entityId) {
        this.entityId = entityId;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public void setEntityType(EntityType entityType) {
        this.entityType = entityType;
    }

    public EntityExecution getEntityExecution() {
        return entityExecution;
    }

    public void setEntityExecution(EntityExecution entityExecution) {
        this.entityExecution = entityExecution;
    }

    public EntityData getEntityData() {
        return entityData;
    }

    public void setEntityData(EntityData entityData) {
        this.entityData = entityData;
    }

    @Deprecated
    public enum RecipientReportType {
        IMPORT_REPORT("recipient.reports.type.import.report"),
        EXPORT_REPORT("recipient.reports.type.export.report");

        private String messageKey;

        RecipientReportType(String messageKey) {
            this.messageKey = messageKey;
        }

        public String getMessageKey() {
            return messageKey;
        }
    }

    public enum EntityType implements IntEnum {
        UNKNOWN(0, "MailType.unknown"),
        IMPORT(1, "recipient.reports.type.import.report"),
        EXPORT(2, "recipient.reports.type.export.report");

        private final int id;
        private final String messageKey;

        EntityType(int id, String messageKey) {
            this.id = id;
            this.messageKey = messageKey;
        }

        @Override
        public int getId() {
            return id;
        }

        public String getMessageKey() {
            return messageKey;
        }
    }

    public enum EntityExecution implements IntEnum {
        UNKNOWN(0),
        MANUAL(1),
        AUTOMATIC(2);

        private final int id;

        EntityExecution(int id) {
            this.id = id;
        }

        @Override
        public int getId() {
            return id;
        }
    }

    public enum EntityData implements IntEnum {
        UNKNOWN(0),
        PROFILE(1),
        REFERENCE_TABLE(2);

        private final int id;

        EntityData(int id) {
            this.id = id;
        }

        @Override
        public int getId() {
            return id;
        }
    }

}
