/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.logon.beans;

import java.util.Objects;

import com.agnitas.beans.ComAdmin;

public class LogonStateBundle {
    private LogonState state;
    private ComAdmin admin;
    private String hostId;

    public LogonStateBundle(LogonState state) {
        this.state = Objects.requireNonNull(state);
    }

    public LogonState getState() {
        return state;
    }

    public void setState(LogonState state) {
        this.state = Objects.requireNonNull(state);
    }

    public ComAdmin getAdmin() {
        return admin;
    }

    public void setAdmin(ComAdmin admin) {
        this.admin = admin;
    }

    public String getHostId() {
        return hostId;
    }

    public void setHostId(String hostId) {
        this.hostId = hostId;
    }
}
