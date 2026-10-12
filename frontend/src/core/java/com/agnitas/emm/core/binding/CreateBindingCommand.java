/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.binding;

import com.agnitas.beans.BindingEntry;
import com.agnitas.emm.common.UserStatus;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.emm.core.validator.BindingValidationField;
import com.agnitas.emm.core.validator.ValidationException;

public record CreateBindingCommand(
        int mailinglistId,
        int recipientId,
        MediaTypes mediatype,
        UserStatus userStatus,
        BindingEntry.UserType userType,
        String userRemark,
        String referrer,
        int entryMailingId,
        int exitMailingId,
        int triggerId,
        boolean runActionAsynchronous
) {
    public CreateBindingCommand {
        if (recipientId <= 0) {
            throw ValidationException.invalidPath("recipientId");
        }
        if (mailinglistId <= 0) {
            throw ValidationException.invalidPath("mailinglistId");
        }
        if (userStatus == null) {
            throw ValidationException.exact(BindingValidationField.USER_STATUS, "Missing userStatus value");
        }
        if (mediatype == null) {
            mediatype = MediaTypes.EMAIL;
        }
        if (userType == null) {
            userType = BindingEntry.UserType.World;
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private int mailinglistId;
        private int recipientId;
        private MediaTypes mediatype;
        private UserStatus userStatus;
        private BindingEntry.UserType userType;
        private String userRemark;
        private String referrer;
        private int entryMailingId;
        private int exitMailingId;
        private int triggerId;
        private boolean runActionAsynchronous;

        private Builder() {
        }

        public Builder mailinglistId(int mailinglistId) {
            this.mailinglistId = mailinglistId;
            return this;
        }

        public Builder recipientId(int recipientId) {
            this.recipientId = recipientId;
            return this;
        }

        public Builder mediatype(MediaTypes mediatype) {
            this.mediatype = mediatype;
            return this;
        }

        public Builder userStatus(UserStatus userStatus) {
            this.userStatus = userStatus;
            return this;
        }

        public Builder userType(BindingEntry.UserType userType) {
            this.userType = userType;
            return this;
        }

        public Builder userRemark(String userRemark) {
            this.userRemark = userRemark;
            return this;
        }

        public Builder referrer(String referrer) {
            this.referrer = referrer;
            return this;
        }

        public Builder entryMailingId(int entryMailingId) {
            this.entryMailingId = entryMailingId;
            return this;
        }

        public Builder exitMailingId(int exitMailingId) {
            this.exitMailingId = exitMailingId;
            return this;
        }

        public Builder triggerId(int triggerId) {
            this.triggerId = triggerId;
            return this;
        }

        public Builder runActionAsynchronous(boolean runActionAsynchronous) {
            this.runActionAsynchronous = runActionAsynchronous;
            return this;
        }

        public CreateBindingCommand build() {
            return new CreateBindingCommand(
                    mailinglistId,
                    recipientId,
                    mediatype,
                    userStatus,
                    userType,
                    userRemark,
                    referrer,
                    entryMailingId,
                    exitMailingId,
                    triggerId,
                    runActionAsynchronous
            );
        }
    }
}
