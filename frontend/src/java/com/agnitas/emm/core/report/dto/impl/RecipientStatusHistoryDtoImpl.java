/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.report.dto.impl;

import java.util.Date;

import com.agnitas.emm.core.report.dto.RecipientStatusHistoryDto;

public class RecipientStatusHistoryDtoImpl implements RecipientStatusHistoryDto {

    private Date changeDate;

    private String changeDescription;

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
    public String getChangeDescription() {
        return changeDescription;
    }

    @Override
    public void setChangeDescription(String changeDescription) {
        this.changeDescription = changeDescription;
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
}
