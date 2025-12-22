/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.beans;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.agnitas.emm.core.components.service.MailingSendService.DeliveryType;

public class MailingSendOptions {

    private Date date;
    private int maxRecipients;
    private int blockSize;
    private int stepping;
    private int followupFor;
    private int overwriteTestRecipientId; // GWUA-5664
    private boolean checkForDuplicateRecords;
    private boolean skipWithEmptyTextContent;
    private boolean cleanupTestsBeforeDelivery;
    private boolean fromWorkflow;
    private int requiredAutoImport;
    private int generationOptimization;
    private int adminTargetGroupId;
    private boolean isActivateAgainToday;
    private DeliveryType deliveryType;
    private boolean reportSendAfter24h;
    private boolean reportSendAfter48h;
    private boolean reportSendAfter1Week;
    private int approvalRequester;
    private List<String> reportEmails;

    public static Builder builder() {
        return new Builder();
    }

    private MailingSendOptions() {
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

    public int getFollowupFor() {
        return followupFor;
    }

    public boolean isCheckForDuplicateRecords() {
        return checkForDuplicateRecords;
    }

    public boolean isSkipWithEmptyTextContent() {
        return skipWithEmptyTextContent;
    }

    public boolean isCleanupTestsBeforeDelivery() {
        return cleanupTestsBeforeDelivery;
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

    public List<String> getReportEmails() {
        return reportEmails;
    }

    public DeliveryType getDeliveryType() {
        return deliveryType;
    }

    public boolean isActivateAgainToday() {
        return isActivateAgainToday;
    }

    public int getOverwriteTestRecipientId() {
        return overwriteTestRecipientId;
    }

    public int getApprovalRequester() {
        return approvalRequester;
    }

    public static class Builder {
        private Date date;
        private boolean isActivateAgainToday;
        private int maxRecipients;
        private int blockSize;
        private int followupFor;
        private boolean checkForDuplicateRecords;
        private boolean skipWithEmptyTextContent;
        private boolean cleanupTestsBeforeDelivery;
        private boolean fromWorkflow;
        private int requiredAutoImport;
        private int adminTargetGroupId;
        private int stepping;
        private int generationOptimization;
        private int overwriteTestRecipientId; // GWUA-5664
        private boolean reportSendAfter24h;
        private boolean reportSendAfter48h;
        private boolean reportSendAfter1Week;
        private DeliveryType deliveryType;
        private int approvalRequester;
        private List<String> reportEmails;

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

        public Builder setCleanupTestsBeforeDelivery(boolean cleanupTestsBeforeDelivery) {
            this.cleanupTestsBeforeDelivery = cleanupTestsBeforeDelivery;
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

        public Builder setReportEmails(List<String> reportEmails) {
            this.reportEmails = reportEmails;
            return this;
        }

        public Builder setDeliveryType(DeliveryType deliveryType) {
            this.deliveryType = deliveryType;
            return this;
        }

        public Builder setActivateAgainToday(boolean activateAgainToday) {
            this.isActivateAgainToday = activateAgainToday;
            return this;
        }

        public Builder setOverwriteTestRecipientId(int overwriteTestRecipientId) {
            this.overwriteTestRecipientId = overwriteTestRecipientId;
            return this;
        }

        public Builder setApprovalRequester(int approvalRequester) {
            this.approvalRequester = approvalRequester;
            return this;
        }

        public MailingSendOptions build() {
            MailingSendOptions options = new MailingSendOptions();

            options.date = date;
            options.maxRecipients = maxRecipients;
            options.blockSize = blockSize;
            options.followupFor = followupFor;
            options.checkForDuplicateRecords = checkForDuplicateRecords;
            options.skipWithEmptyTextContent = skipWithEmptyTextContent;
            options.cleanupTestsBeforeDelivery = cleanupTestsBeforeDelivery;
            options.adminTargetGroupId = adminTargetGroupId;
            options.fromWorkflow = fromWorkflow;
            options.requiredAutoImport = requiredAutoImport;
            options.stepping = stepping;
            options.generationOptimization = generationOptimization;
            options.reportSendAfter24h = reportSendAfter24h;
            options.reportSendAfter48h = reportSendAfter48h;
            options.reportSendAfter1Week = reportSendAfter1Week;
            options.reportEmails = reportEmails == null ? Collections.emptyList() : reportEmails;
            options.deliveryType = deliveryType;
            options.isActivateAgainToday = isActivateAgainToday;
            options.overwriteTestRecipientId = overwriteTestRecipientId;
            options.approvalRequester = approvalRequester;
            return options;
        }
    }
}
