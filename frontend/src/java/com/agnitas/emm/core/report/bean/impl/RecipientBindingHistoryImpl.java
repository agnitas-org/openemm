/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.report.bean.impl;

import java.io.Serializable;
import java.util.Date;

import com.agnitas.emm.core.report.bean.RecipientBindingHistory;

public class RecipientBindingHistoryImpl implements RecipientBindingHistory, Serializable {

    private static final long serialVersionUID = -5980356392321437197L;

    // form mailinglist entity
    private String shortname;
    private boolean isDeleted;

    // ether from binding or bindingHistory entities
    private int mailinglistId;
    private int mediaType;
    private String changedFieldName;
    private String oldValue;
    private String newValue;
    private Date changeDate;

    @Override
    public int getMailinglistId() {
        return mailinglistId;
    }

    @Override
    public void setMailinglistId(int mailinglistId) {
        this.mailinglistId = mailinglistId;
    }

    @Override
    public int getMediaType() {
        return mediaType;
    }

    @Override
    public void setMediaType(int mediaType) {
        this.mediaType = mediaType;
    }

    @Override
    public String getShortname() {
        return shortname;
    }

    @Override
    public void setShortname(String shortname) {
        this.shortname = shortname;
    }

    @Override
    public boolean isDeleted() {
        return isDeleted;
    }

    @Override
    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    @Override
    public String getChangedFieldName() {
        return changedFieldName;
    }

    @Override
    public void setChangedFieldName(String changedFieldName) {
        this.changedFieldName = changedFieldName;
    }

    @Override
    public String getOldValue() {
        return oldValue;
    }

    @Override
    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    @Override
    public String getNewValue() {
        return newValue;
    }

    @Override
    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    @Override
    public Date getChangeDate() {
        return changeDate;
    }

    @Override
    public void setChangeDate(Date changeDate) {
        this.changeDate = changeDate;
    }
}
