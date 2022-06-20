/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.workflow.beans;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

// Must be kept immutable.
public final class WorkflowReminderRecipient {
    private final String address;
    private final int adminId;

    public WorkflowReminderRecipient(String address, int adminId) {
        if (StringUtils.isEmpty(address) || adminId <= 0) {
            throw new IllegalArgumentException("address is empty || adminId <= 0");
        }

        this.address = address;
        this.adminId = adminId;
    }

    public WorkflowReminderRecipient(String address) {
        if (StringUtils.isEmpty(address)) {
            throw new IllegalArgumentException("address is empty");
        }

        this.address = address;
        this.adminId = 0;
    }

    public WorkflowReminderRecipient(int adminId) {
        if (adminId <= 0) {
            throw new IllegalArgumentException("adminId <= 0");
        }

        this.address = null;
        this.adminId = adminId;
    }

    public String getAddress() {
        return address;
    }

    public int getAdminId() {
        return adminId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("address", address)
            .append("adminId", adminId)
            .toString();
    }
}
