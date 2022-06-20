/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.report.converter.impl;

import java.util.Date;

import org.springframework.stereotype.Component;

import com.agnitas.beans.ComRecipientHistory;
import com.agnitas.beans.impl.ComRecipientHistoryImpl;
import com.agnitas.emm.core.report.bean.RecipientBindingHistory;
import com.agnitas.emm.core.report.converter.CollectionConverter;

@Component
public class ComRecipientHistoryConverter implements CollectionConverter<RecipientBindingHistory, ComRecipientHistory> {

    @Override
    public ComRecipientHistory convert(final RecipientBindingHistory bindingHistory) {
        ComRecipientHistory recipientHistory = new ComRecipientHistoryImpl();

        recipientHistory.setChangeDate((Date) bindingHistory.getChangeDate().clone());
        recipientHistory.setMediaType(bindingHistory.getMediaType());
        recipientHistory.setFieldName(bindingHistory.getChangedFieldName());
        recipientHistory.setNewValue(bindingHistory.getNewValue());
        recipientHistory.setOldValue(bindingHistory.getOldValue());

        String description;
        if (bindingHistory.isDeleted()) {
            description = String.format("Deleted Mailinglist (ID: %d)", bindingHistory.getMailinglistId());
        } else {
            description = String.format("\"%s\" (ID: %d)", bindingHistory.getShortname(), bindingHistory.getMailinglistId());
        }
        recipientHistory.setMailingList(description);

        return recipientHistory;
    }
}
