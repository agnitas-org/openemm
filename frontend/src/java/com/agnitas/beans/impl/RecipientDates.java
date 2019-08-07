/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.beans.impl;

import java.util.Date;

import org.agnitas.util.DateUtilities;

public class RecipientDates {
    private Date creationDate;
    private Date lastSendDate;
    private Date lastOpenDate;
    private Date lastClickDate;

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getLastSendDate() {
        return lastSendDate;
    }

    public void setLastSendDate(Date lastSendDate) {
        this.lastSendDate = lastSendDate;
    }

    public Date getLastOpenDate() {
        return lastOpenDate;
    }

    public void setLastOpenDate(Date lastOpenDate) {
        this.lastOpenDate = lastOpenDate;
    }

    public Date getLastClickDate() {
        return lastClickDate;
    }

    public void setLastClickDate(Date lastClickDate) {
        this.lastClickDate = lastClickDate;
    }

    public Date getLastReactionDate() {
        if (lastOpenDate == null) {
            return lastClickDate;
        }

        if (lastClickDate == null) {
            return lastOpenDate;
        }

        return DateUtilities.max(lastClickDate, lastOpenDate);
    }
}
