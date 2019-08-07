/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.report.bean.impl;

import java.io.Serializable;
import java.util.Date;

import com.agnitas.emm.core.report.bean.PlainBindingEntry;

public class PlainBindingEntryImpl implements PlainBindingEntry, Serializable {

    private static final long serialVersionUID = 1832986631686195798L;

    private int customerId;

    private int mailingListId;

    private String userType;

    private int userStatus;

    private String userRemark;

    private Date timestamp;

    private Date creationDate;

    private Integer exitMailingId;

    private int mediaType;

    @Override
    public int getCustomerId() {
        return customerId;
    }

    @Override
    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    @Override
    public int getMailingListId() {
        return mailingListId;
    }

    @Override
    public void setMailingListId(int mailinglistId) {
        this.mailingListId = mailinglistId;
    }

    @Override
    public String getUserType() {
        return userType;
    }

    @Override
    public void setUserType(String userType) {
        this.userType = userType;
    }

    @Override
    public Integer getUserStatus() {
        return userStatus;
    }

    @Override
    public void setUserStatus(Integer userStatus) {
        this.userStatus = userStatus;
    }

    @Override
    public String getUserRemark() {
        return userRemark;
    }

    @Override
    public void setUserRemark(String userRemark) {
        this.userRemark = userRemark;
    }

    @Override
    public Date getTimestamp() {
        return timestamp;
    }

    @Override
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public Date getCreationDate() {
        return creationDate;
    }

    @Override
    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    @Override
    public Integer getExitMailingId() {
        return exitMailingId;
    }

    @Override
    public void setExitMailingId(Integer exitMailingId) {
        this.exitMailingId = exitMailingId;
    }

    @Override
    public int getMediaType() {
        return mediaType;
    }

    @Override
    public void setMediaType(int mediatype) {
        this.mediaType = mediatype;
    }
}
