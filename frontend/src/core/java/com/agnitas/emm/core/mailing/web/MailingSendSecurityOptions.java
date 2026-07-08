/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailing.web;

public class MailingSendSecurityOptions {
    private boolean enableNoSendCheckNotifications;
    private String clearanceEmail;
    private Integer clearanceThreshold;

    public static Builder builder() {
        return new Builder();
    }

    public static MailingSendSecurityOptions empty() {
        return new Builder().build();
    }

    public boolean isEnableNoSendCheckNotifications() {
        return enableNoSendCheckNotifications;
    }

    public String getClearanceEmail() {
        return clearanceEmail;
    }

    public Integer getClearanceThreshold() {
        return clearanceThreshold;
    }

    public boolean isThresholdEnabled() {
        return clearanceThreshold != null && clearanceThreshold > 0;
    }

    public static class Builder {
        private MailingSendSecurityOptions options = new MailingSendSecurityOptions();

        public Builder setNoSendNotificationEnabled(boolean enableNoSendCheckNotifications) {
            this.options.enableNoSendCheckNotifications = enableNoSendCheckNotifications;
            return this;
        }

        public Builder withNotifications(boolean notificationsEnabled, String clearanceEmail) {
            if (notificationsEnabled) {
                this.options.clearanceEmail = clearanceEmail;
            } else {
                this.options.clearanceEmail = "";
            }
            return this;
        }

        public Builder setClearanceThreshold(Integer clearanceThreshold) {
            this.options.clearanceThreshold = clearanceThreshold;
            return this;
        }

        public MailingSendSecurityOptions build() {
            MailingSendSecurityOptions optionsForReturn = this.options;
            this.options = null;
            return optionsForReturn;
        }
    }
}
