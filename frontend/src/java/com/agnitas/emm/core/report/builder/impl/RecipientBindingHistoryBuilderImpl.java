/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.report.builder.impl;

import java.util.Date;
import java.util.Objects;

import org.agnitas.beans.Mailinglist;

import com.agnitas.emm.core.report.bean.PlainBindingEntry;
import com.agnitas.emm.core.report.bean.RecipientBindingHistory;
import com.agnitas.emm.core.report.bean.impl.RecipientBindingHistoryImpl;
import com.agnitas.emm.core.report.builder.RecipientBindingHistoryBuilder;

public class RecipientBindingHistoryBuilderImpl implements RecipientBindingHistoryBuilder {

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

    public RecipientBindingHistoryBuilderImpl(PlainBindingEntry plainBindingEntry, Mailinglist mailinglist) {
        mailinglistId = plainBindingEntry.getMailingListId();
        mediaType = plainBindingEntry.getMediaType();
        isDeleted = Objects.isNull(mailinglist) || mailinglist.isRemoved();
        shortname = Objects.nonNull(mailinglist) ? mailinglist.getShortname() : null;
    }

    @Override
	public RecipientBindingHistoryBuilder setChangeDate(Date changeDate) {
        this.changeDate = changeDate;
        return this;
    }

    @Override
	public RecipientBindingHistoryBuilder setChangedFieldName(String changedFieldName) {
        this.changedFieldName = changedFieldName;
        return this;
    }

    @Override
	public RecipientBindingHistoryBuilder setOldValue(String oldValue) {
        this.oldValue = oldValue;
        return this;
    }

    @Override
	public RecipientBindingHistoryBuilder setNewValue(String newValue) {
        this.newValue = newValue;
        return this;
    }

    @Override
	public RecipientBindingHistory build() {
        RecipientBindingHistory bindingHistory = new RecipientBindingHistoryImpl();
        bindingHistory.setMailinglistId(mailinglistId);
        bindingHistory.setMediaType(mediaType);
        bindingHistory.setDeleted(isDeleted);
        bindingHistory.setShortname(shortname);
        bindingHistory.setChangeDate(changeDate);
        bindingHistory.setChangedFieldName(changedFieldName);
        bindingHistory.setOldValue(oldValue);
        bindingHistory.setNewValue(newValue);

        return bindingHistory;
    }
}
