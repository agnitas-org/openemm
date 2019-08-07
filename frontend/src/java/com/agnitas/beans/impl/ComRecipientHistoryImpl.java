/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.beans.impl;

import java.util.Date;

import org.agnitas.util.DateUtilities;

import com.agnitas.beans.ComRecipientHistory;

/**
 * POJO containing one recipient entry.
 * Default implementation.
 */
public class ComRecipientHistoryImpl implements ComRecipientHistory {
    private Date changeDate;
    private String fieldName;
    private Number mediaType;
    private String mailingList;
    private Object newValue;
    private Object oldValue;

    @Override
    public Date getChangeDate() {
        return changeDate;
    }

    @Override
    public void setChangeDate(Date changeDate) {
        this.changeDate = changeDate;
    }

    @Override
    public String getFieldName() {
        return fieldName;
    }

    @Override
    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    @Override
    public Object getNewValue() {
        return newValue;
    }

    @Override
    public void setNewValue(Object newValue) {
        this.newValue = newValue;
    }

    @Override
    public Object getOldValue() {
        return oldValue;
    }

    @Override
    public void setOldValue(Object oldValue) {
        this.oldValue = oldValue;
    }

    @Override
    public Number getMediaType() {
        return mediaType;
    }

    @Override
    public void setMediaType(Number mediaType) {
        this.mediaType = mediaType;
    }

    @Override
    public String getMailingList() {
        return mailingList;
    }

    @Override
    public void setMailingList(String mailingList) {
        this.mailingList = mailingList;
    }

    @Override
    public int compareTo(ComRecipientHistory otherObject) {
        if (otherObject != null) {
            int result = DateUtilities.compare(changeDate, otherObject.getChangeDate());
            //yuriy.privezentsev: This is not obvious, but the "Deleted" event shall appear always on the top of other
            // events of this list. The only way to check that the event is about deleting list is whether media type is
            // null. The media type shall be null only for "Deleted" events, otherwise it is a bug.
            if (result == 0) {
                if (mediaType == null) {
                    result = 1;
                }
                else if (otherObject.getMediaType() == null) {
                    result = -1;
                }
            }
            return result;
        }
        return 0;
    }
}
