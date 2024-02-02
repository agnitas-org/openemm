/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.dashboard.bean;

import com.agnitas.emm.core.recipientsreport.bean.RecipientsReport;

import java.util.Date;
import java.util.List;

public class DashboardRecipientReport {

    private int id;
    private Type type;
    private String name;
    private boolean successful;
    private Date lastExecutionDate;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }

    public Date getLastExecutionDate() {
        return lastExecutionDate;
    }

    public void setLastExecutionDate(Date lastExecutionDate) {
        this.lastExecutionDate = lastExecutionDate;
    }

    public boolean isAutomatic() {
        return type.getEntityExecution() == RecipientsReport.EntityExecution.AUTOMATIC;
    }

    public boolean hasImportType() {
        return type == Type.IMPORT || type == Type.AUTO_IMPORT;
    }

    public enum Type {
        IMPORT(RecipientsReport.EntityType.IMPORT, RecipientsReport.EntityExecution.MANUAL, "import"),
        EXPORT(RecipientsReport.EntityType.EXPORT, RecipientsReport.EntityExecution.MANUAL, "Export"),
        AUTO_IMPORT(RecipientsReport.EntityType.IMPORT, RecipientsReport.EntityExecution.AUTOMATIC, "autoImport.autoImport"),
        AUTO_EXPORT(RecipientsReport.EntityType.EXPORT, RecipientsReport.EntityExecution.AUTOMATIC, "autoExport.autoExport");

        private final RecipientsReport.EntityType entityType;
        private final RecipientsReport.EntityExecution entityExecution;
        private final String messageKey;

        Type(RecipientsReport.EntityType entityType, RecipientsReport.EntityExecution entityExecution, String messageKey) {
            this.entityType = entityType;
            this.entityExecution = entityExecution;
            this.messageKey = messageKey;
        }

        public RecipientsReport.EntityType getEntityType() {
            return entityType;
        }

        public RecipientsReport.EntityExecution getEntityExecution() {
            return entityExecution;
        }

        public String getMessageKey() {
            return messageKey;
        }

        public static Type detect(int entityType, int executionType) {
            List<Type> types = List.of(Type.values());

            return types.stream()
                    .filter(t -> t.getEntityType().getId() == entityType && t.getEntityExecution().getId() == executionType)
                    .findAny()
                    .orElse(null);
        }
    }
}
