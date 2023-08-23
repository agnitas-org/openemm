/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.beans;

import com.agnitas.emm.core.components.service.MailingSendService.DeliveryType;

import java.util.Date;

public class MailingSendOptions {

    private int adminId;
    private Date date;
    private int maxRecipients;
    private int blockSize;
    private int stepping;
    private int defaultStepping;
    private int followupFor;
    private int overwriteTestRecipientId; // GWUA-5664
    private boolean checkForDuplicateRecords;
    private boolean skipWithEmptyTextContent;
    private int reportSendDayOffset;
    private boolean fromWorkflow;
    private int requiredAutoImport;
    private int generationOptimization;
    private int adminTargetGroupId;
    private DeliveryType deliveryType;
    private boolean reportSendAfter24h;
    private boolean reportSendAfter48h;
    private boolean reportSendAfter1Week;
    private String reportSendEmail;

    public static Builder builder() {
        return new Builder();
    }

    private MailingSendOptions() {
    }

    public int getAdminId() {
        return adminId;
    }

    public Date getDate() {
        return date == null ? null : new Date(date.getTime());
    }

    public int getMaxRecipients() {
        return maxRecipients;
    }

    public int getBlockSize() {
        return blockSize;
    }

    public int getDefaultStepping() {
        return defaultStepping;
    }

    public int getFollowupFor() {
        return followupFor;
    }

    public boolean isCheckForDuplicateRecords() {
        return checkForDuplicateRecords;
    }

    public boolean isSkipWithEmptyTextContent() {
        return skipWithEmptyTextContent;
    }

    public int getReportSendDayOffset() {
        return reportSendDayOffset;
    }

    public boolean isFromWorkflow() {
        return fromWorkflow;
    }

    public int getRequiredAutoImport() {
        return requiredAutoImport;
    }

    public int getStepping() {
        return stepping;
    }

    public int getGenerationOptimization() {
        return generationOptimization;
    }

    public int getAdminTargetGroupId() {
        return adminTargetGroupId;
    }

    public boolean isReportSendAfter24h() {
        return reportSendAfter24h;
    }

    public boolean isReportSendAfter48h() {
        return reportSendAfter48h;
    }

    public boolean isReportSendAfter1Week() {
        return reportSendAfter1Week;
    }

    public String getReportSendEmail() {
        return reportSendEmail;
    }

    public DeliveryType getDeliveryType() {
        return deliveryType;
    }

    public int getOverwriteTestRecipientId() {
        return overwriteTestRecipientId;
    }
    
    public static class Builder {
        private int adminId;
        private Date date;
        private int maxRecipients;
        private int blockSize;
        private int defaultStepping;
        private int followupFor;
        private boolean checkForDuplicateRecords;
        private boolean skipWithEmptyTextContent;
        private int reportSendDayOffset;
        private boolean fromWorkflow;
        private int requiredAutoImport;
        private int adminTargetGroupId;
        private int stepping;
        private int generationOptimization;
        private int overwriteTestRecipientId; // GWUA-5664
        private boolean reportSendAfter24h;
        private boolean reportSendAfter48h;
        private boolean reportSendAfter1Week;
        private String reportSendEmail;
        private DeliveryType deliveryType;

        public Builder setAdminId(int adminId) {
            this.adminId = adminId;
            return this;
        }

        public Builder setDate(Date date) {
            this.date = date;
            return this;
        }

        public Builder setMaxRecipients(int maxRecipients) {
            this.maxRecipients = maxRecipients;
            return this;
        }

        public Builder setBlockSize(int blockSize) {
            this.blockSize = blockSize;
            return this;
        }

        public Builder setDefaultStepping(int defaultStepping) {
            this.defaultStepping = defaultStepping;
            return this;
        }

        public Builder setFollowupFor(int followupFor) {
            this.followupFor = followupFor;
            return this;
        }

        public Builder setCheckForDuplicateRecords(boolean checkForDuplicateRecords) {
            this.checkForDuplicateRecords = checkForDuplicateRecords;
            return this;
        }

        public Builder setSkipWithEmptyTextContent(boolean skipWithEmptyTextContent) {
            this.skipWithEmptyTextContent = skipWithEmptyTextContent;
            return this;
        }

        public Builder setReportSendDayOffset(int reportSendDayOffset) {
            this.reportSendDayOffset = reportSendDayOffset;
            return this;
        }

        public Builder setFromWorkflow(boolean fromWorkflow) {
            this.fromWorkflow = fromWorkflow;
            return this;
        }

        public Builder setRequiredAutoImport(int autoImport) {
            this.requiredAutoImport = autoImport;
            return this;
        }

        public Builder setAdminTargetGroupId(int adminTargetGroupId) {
            this.adminTargetGroupId = adminTargetGroupId;
            return this;
        }

        public Builder setStepping(int stepping) {
            this.stepping = stepping;
            return this;
        }

        public Builder setGenerationOptimization(int generationOptimization) {
            this.generationOptimization = generationOptimization;
            return this;
        }

        public Builder setReportSendAfter24h(boolean reportSendAfter24h) {
            this.reportSendAfter24h = reportSendAfter24h;
            return this;
        }

        public Builder setReportSendAfter48h(boolean reportSendAfter48h) {
            this.reportSendAfter48h = reportSendAfter48h;
            return this;
        }

        public Builder setReportSendAfter1Week(boolean reportSendAfter1Week) {
            this.reportSendAfter1Week = reportSendAfter1Week;
            return this;
        }

        public Builder setReportSendEmail(String reportSendEmail) {
            this.reportSendEmail = reportSendEmail;
            return this;
        }

        public Builder setDeliveryType(DeliveryType deliveryType) {
            this.deliveryType = deliveryType;
            return this;
        }

        public Builder setOverwriteTestRecipientId(int overwriteTestRecipientId) {
            this.overwriteTestRecipientId = overwriteTestRecipientId;
            return this;
        }

        public MailingSendOptions build() {
            MailingSendOptions options = new MailingSendOptions();

            options.adminId = adminId;
            options.date = date;
            options.maxRecipients = maxRecipients;
            options.blockSize = blockSize;
            options.defaultStepping = defaultStepping;
            options.followupFor = followupFor;
            options.checkForDuplicateRecords = checkForDuplicateRecords;
            options.skipWithEmptyTextContent = skipWithEmptyTextContent;
            options.reportSendDayOffset = reportSendDayOffset;
            options.adminTargetGroupId = adminTargetGroupId;
            options.fromWorkflow = fromWorkflow;
            options.requiredAutoImport = requiredAutoImport;
            options.stepping = stepping;
            options.generationOptimization = generationOptimization;
            options.reportSendAfter24h = reportSendAfter24h;
            options.reportSendAfter48h = reportSendAfter48h;
            options.reportSendAfter1Week = reportSendAfter1Week;
            options.reportSendEmail = reportSendEmail;
            options.deliveryType = deliveryType;
            options.overwriteTestRecipientId = overwriteTestRecipientId;

            return options;
        }
    }
}
