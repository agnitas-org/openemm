/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.restful.v2.recipients.dto;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.Objects;

import com.agnitas.emm.common.UserStatus;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Pattern;

public final class RecipientSubscribeDto {

    @Pattern(regexp = "\\*|[1-9]\\d*", message = "mailinglist must be '*' or ID")
    private String mailinglist;
    @Pattern(
            regexp = "EMAIL|0|POST|2|SMS|4",
            message = "Allowed values: [EMAIL(0), POST(2), SMS(4)]"
    )
    private String mediatype;

    @Null(
            groups = Groups.Bulk.class,
            message = "Parameter status is not allowed for bulk creation of recipient. Use the import mode to define which subscription status to set"
    )
    @Pattern(
            groups = Groups.Single.class,
            regexp = "Active|1|Bounce|2|AdminOut|3|UserOut|4|WaitForConfirm|5|Blacklisted|6|Suspend|7",
            message = "Allowed values: [Active(1), Bounce(2), AdminOut(3), UserOut(4), WaitForConfirm(5), Blacklisted(6), Suspend(7)]"
    )
    private String status;

    public String getMailinglist() {
        return mailinglist;
    }

    public void setMailinglist(String mailinglist) {
        this.mailinglist = mailinglist;
    }

    public UserStatus getStatus() {
        if (isBlank(status)) {
            return UserStatus.Active;
        }
        return UserStatus.fromAny(status);
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public MediaTypes getMediatype() {
        if (isBlank(mediatype)) {
            return MediaTypes.EMAIL;
        }
        return MediaTypes.fromAny(mediatype);
    }

    public void setMediatype(String mediatype) {
        this.mediatype = mediatype;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (RecipientSubscribeDto) obj;
        return Objects.equals(this.mailinglist, that.mailinglist) &&
               Objects.equals(this.status, that.status) &&
               Objects.equals(this.mediatype, that.mediatype);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mailinglist, status, mediatype);
    }

    @Override
    public String toString() {
        return "RecipientSubscribeDto[" +
               "mailinglist=" + mailinglist + ", " +
               "status=" + status + ", " +
               "mediatype=" + mediatype + ']';
    }
}
