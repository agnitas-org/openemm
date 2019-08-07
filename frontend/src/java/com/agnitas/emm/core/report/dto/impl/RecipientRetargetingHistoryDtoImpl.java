/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.report.dto.impl;

import java.util.Date;

import com.agnitas.emm.core.report.dto.RecipientRetargetingHistoryDto;

public class RecipientRetargetingHistoryDtoImpl implements RecipientRetargetingHistoryDto {

    private Date date;

    private String mailingTitle;

    private String trackingPoint;

    private String value;

    private String ip;

    @Override
    public Date getDate() {
        return date;
    }

    @Override
    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public String getMailingTitle() {
        return mailingTitle;
    }

    @Override
    public void setMailingTitle(String mailingTitle) {
        this.mailingTitle = mailingTitle;
    }

    @Override
    public String getTrackingPoint() {
        return trackingPoint;
    }

    @Override
    public void setTrackingPoint(String trackingPoint) {
        this.trackingPoint = trackingPoint;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String getIp() {
        return ip;
    }

    @Override
    public void setIp(String ip) {
        this.ip = ip;
    }
}
