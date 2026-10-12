/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailing.dto;

import com.agnitas.emm.common.FollowUpType;

public record FollowupRecipientsQuery(
        int companyId,
        int followupMailingId,
        int baseMailingId,
        FollowUpType followUpType,
        boolean excludeProxyOpeners
) {
    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private int companyId;
        private int followupMailingId;
        private int baseMailingId;
        private FollowUpType followUpType;
        private boolean excludeProxyOpeners;

        private Builder() {}

        public Builder companyId(int companyId) {
            this.companyId = companyId;
            return this;
        }

        public Builder followupMailingId(int followupMailingId) {
            this.followupMailingId = followupMailingId;
            return this;
        }

        public Builder baseMailingId(int baseMailingId) {
            this.baseMailingId = baseMailingId;
            return this;
        }

        public Builder followUpType(FollowUpType followUpType) {
            this.followUpType = followUpType;
            return this;
        }

        public Builder excludeProxyOpeners(boolean excludeProxyOpeners) {
            this.excludeProxyOpeners = excludeProxyOpeners;
            return this;
        }

        public FollowupRecipientsQuery build() {
            return new FollowupRecipientsQuery(
                    companyId,
                    followupMailingId,
                    baseMailingId,
                    followUpType,
                    excludeProxyOpeners
            );
        }
    }
}
