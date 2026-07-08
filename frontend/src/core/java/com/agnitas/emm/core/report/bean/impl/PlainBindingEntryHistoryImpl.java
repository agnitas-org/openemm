/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.report.bean.impl;

import java.util.Date;

import com.agnitas.emm.core.report.bean.PlainBindingEntryHistory;

public class PlainBindingEntryHistoryImpl extends PlainBindingEntryImpl implements PlainBindingEntryHistory {

    private static final long serialVersionUID = 1954133956710594119L;

    private Integer changeType;

    private Date timestampChange;

    private String clientInfo;

    private String email;

    @Override
    public int getChangeType() {
        return changeType;
    }

    @Override
    public void setChangeType(int changeType) {
        this.changeType = changeType;
    }

    @Override
    public Date getTimestampChange() {
        return timestampChange;
    }

    @Override
    public void setTimestampChange(Date timestampChange) {
        this.timestampChange = timestampChange;
    }

    @Override
    public String getClientInfo() {
        return clientInfo;
    }

    @Override
    public void setClientInfo(String clientInfo) {
        this.clientInfo = clientInfo;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public void setEmail(String email) {
        this.email = email;
    }
}
