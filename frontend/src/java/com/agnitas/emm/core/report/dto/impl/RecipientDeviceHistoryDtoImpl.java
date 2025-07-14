/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.report.dto.impl;

import java.util.Date;

import com.agnitas.emm.core.report.dto.RecipientDeviceHistoryDto;

public class RecipientDeviceHistoryDtoImpl implements RecipientDeviceHistoryDto {

    private Date date;

    private String mailingDescription;

    private String actionDescription;

    private String deviceType;

    private String deviceName;

    @Override
    public Date getDate() {
        return date;
    }

    @Override
    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public String getMailingDescription() {
        return mailingDescription;
    }

    @Override
    public void setMailingDescription(String mailingDescription) {
        this.mailingDescription = mailingDescription;
    }

    @Override
    public String getActionDescription() {
        return actionDescription;
    }

    @Override
    public void setActionDescription(String actionDescription) {
        this.actionDescription = actionDescription;
    }

    @Override
    public String getDeviceType() {
        return deviceType;
    }

    @Override
    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    @Override
    public String getDeviceName() {
        return deviceName;
    }

    @Override
    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }
}
