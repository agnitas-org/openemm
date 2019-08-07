/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.calendar.beans.impl;

import com.agnitas.emm.core.calendar.beans.ComCalendarCommentRecipient;

public class ComCalendarCommentRecipientImpl implements ComCalendarCommentRecipient {
    private String address;
    private int adminId;
    private boolean notified;

    @Override
	public String getAddress() {
        return address;
    }

    @Override
	public void setAddress(String address) {
        this.address = address;
    }

    @Override
	public int getAdminId() {
        return adminId;
    }

    @Override
	public void setAdminId(int adminId) {
        this.adminId = adminId;
    }

    @Override
	public boolean isNotified() {
        return notified;
    }

    @Override
	public void setNotified(boolean notified) {
        this.notified = notified;
    }
}
