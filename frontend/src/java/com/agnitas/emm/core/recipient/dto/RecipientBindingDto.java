/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.recipient.dto;

import java.util.Date;

import org.agnitas.beans.BindingEntry;
import org.agnitas.dao.UserStatus;

import com.agnitas.emm.core.mediatypes.common.MediaTypes;

public class RecipientBindingDto {

    private int mailinglistId;

    private MediaTypes mediaType;

    private UserStatus status;

    private String userType = BindingEntry.UserType.World.getTypeCode();

    private Date changeDate;
    private String userRemark;
    private String referrer;
    private int exitMailingId;

    public RecipientBindingDto() {
    }

    public RecipientBindingDto(MediaTypes mediaType) {
        this.mediaType = mediaType;
    }

    public int getMailinglistId() {
        return mailinglistId;
    }

    public void setMailinglistId(int mailinglistId) {
        this.mailinglistId = mailinglistId;
    }

    public MediaTypes getMediaType() {
        return mediaType;
    }

    public void setMediaType(MediaTypes mediaType) {
        this.mediaType = mediaType;
    }

    public boolean isActiveStatus() {
        return UserStatus.Active == status;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public Date getChangeDate() {
        return changeDate;
    }

    public void setChangeDate(Date changeDate) {
        this.changeDate = changeDate;
    }

    public String getUserRemark() {
        return userRemark;
    }

    public void setUserRemark(String userRemark) {
        this.userRemark = userRemark;
    }

    public String getReferrer() {
        return referrer;
    }

    public void setReferrer(String referrer) {
        this.referrer = referrer;
    }

    public int getExitMailingId() {
        return exitMailingId;
    }

    public void setExitMailingId(int exitMailingId) {
        this.exitMailingId = exitMailingId;
    }
}
