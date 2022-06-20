/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.beans;

import java.util.Date;
import java.util.Objects;

import com.agnitas.service.ComMailingSendService.DeliveryType;

public class MailingSendOptions {
    private int adminId;
    private Date date;
    private int maxRecipients;
    private int blockSize;
    private int defaultStepping;
    private int followupFor;
    private boolean doubleChecking;
    private boolean skipEmpty;
    private int reportSendDayOffset;
    private boolean generateAtSendDate;
    private DeliveryType deliveryType;

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

    public boolean isDoubleChecking() {
        return doubleChecking;
    }

    public boolean isSkipEmpty() {
        return skipEmpty;
    }

    public int getReportSendDayOffset() {
        return reportSendDayOffset;
    }

    public boolean isGenerateAtSendDate() {
        return generateAtSendDate;
    }

    public DeliveryType getDeliveryType() {
        return deliveryType;
    }

    public static class Builder {
        private int adminId;
        private Date date;
        private int maxRecipients;
        private int blockSize;
        private int defaultStepping;
        private int followupFor;
        private boolean doubleChecking;
        private boolean skipEmpty;
        private int reportSendDayOffset;
        private boolean generateAtSendDate;
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

        public Builder setDoubleChecking(boolean doubleChecking) {
            this.doubleChecking = doubleChecking;
            return this;
        }

        public Builder setSkipEmpty(boolean skipEmpty) {
            this.skipEmpty = skipEmpty;
            return this;
        }

        public Builder setReportSendDayOffset(int reportSendDayOffset) {
            this.reportSendDayOffset = reportSendDayOffset;
            return this;
        }

        public Builder setGenerateAtSendDate(boolean generateAtSendDate) {
            this.generateAtSendDate = generateAtSendDate;
            return this;
        }

        public Builder setDeliveryType(DeliveryType deliveryType) {
            this.deliveryType = deliveryType;
            return this;
        }

        public MailingSendOptions build() {
            MailingSendOptions options = new MailingSendOptions();

            options.adminId = adminId;
            options.date = Objects.requireNonNull(date);
            options.maxRecipients = maxRecipients;
            options.blockSize = blockSize;
            options.defaultStepping = defaultStepping;
            options.followupFor = followupFor;
            options.doubleChecking = doubleChecking;
            options.skipEmpty = skipEmpty;
            options.reportSendDayOffset = reportSendDayOffset;
            options.generateAtSendDate = generateAtSendDate;
            options.deliveryType = Objects.requireNonNull(deliveryType);

            return options;
        }
    }
}
