/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtstatistics.model;

import com.agnitas.emm.core.commons.dto.DateRange;
import com.agnitas.emm.core.mobile.bean.DeviceClass;

public class MailingProgressStatisticFilter {

    private final int entityId;
    private final int companyId;
    private final DateRange dateRange;
    private final String targetSql;
    private final DeviceClass deviceClass;

    private MailingProgressStatisticFilter(Builder builder) {
        this.entityId = builder.entityId;
        this.companyId = builder.companyId;
        this.dateRange = builder.dateRange;
        this.targetSql = builder.targetSql;
        this.deviceClass = builder.deviceClass;
    }

    public int getEntityId() {
        return entityId;
    }

    public int getCompanyId() {
        return companyId;
    }

    public DateRange getDateRange() {
        return dateRange;
    }

    public String getTargetSql() {
        return targetSql;
    }

    public DeviceClass getDeviceClass() {
        return deviceClass;
    }

    public static class Builder {
        private int entityId;
        private int companyId;
        private DateRange dateRange;
        private String targetSql;
        private DeviceClass deviceClass;

        public Builder entityId(int entityId) {
            this.entityId = entityId;
            return this;
        }

        public Builder companyId(int companyId) {
            this.companyId = companyId;
            return this;
        }

        public Builder dateRange(DateRange dateRange) {
            this.dateRange = dateRange;
            return this;
        }

        public Builder targetSql(String targetSql) {
            this.targetSql = targetSql;
            return this;
        }

        public Builder deviceClass(DeviceClass deviceClass) {
            this.deviceClass = deviceClass;
            return this;
        }

        public MailingProgressStatisticFilter build() {
            return new MailingProgressStatisticFilter(this);
        }
    }
}
