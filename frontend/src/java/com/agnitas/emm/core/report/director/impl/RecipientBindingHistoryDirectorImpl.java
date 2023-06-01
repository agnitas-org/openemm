/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.report.director.impl;

import java.util.Date;
import java.util.Objects;

import com.agnitas.emm.core.report.builder.RecipientBindingHistoryBuilder;
import com.agnitas.emm.core.report.director.RecipientBindingHistoryDirector;
import com.agnitas.emm.core.report.enums.fields.RecipientMutableFields;

/**
 * Builds corresponding BindingHistory entry for all possible changed.
 */
public class RecipientBindingHistoryDirectorImpl implements RecipientBindingHistoryDirector {

    private final RecipientBindingHistoryBuilder builder;

    public RecipientBindingHistoryDirectorImpl(RecipientBindingHistoryBuilder builder) {
        this.builder = builder;
    }

    @Override
    public RecipientBindingHistoryBuilder constructChangedType(Date changeTime, String oldValue, String newValue) {

        return builder.setChangedFieldName(RecipientMutableFields.USER_TYPE.getCode())
                .setChangeDate(changeTime)
                .setOldValue(oldValue)
                .setNewValue(newValue);
    }

    @Override
    public RecipientBindingHistoryBuilder constructChangedStatus(Date changeTime, Integer oldValue, Integer newValue) {

        return builder.setChangedFieldName(RecipientMutableFields.USER_STATUS.getCode())
                .setChangeDate(changeTime)
                .setOldValue(Objects.nonNull(oldValue) ? Integer.toString(oldValue) : null)
                .setNewValue(Objects.nonNull(newValue) ? Integer.toString(newValue) : null);
    }

    @Override
    public RecipientBindingHistoryBuilder constructChangedRemark(Date changeTime, String oldValue, String newValue) {

        return builder.setChangedFieldName(RecipientMutableFields.USER_REMARK.getCode())
                .setChangeDate(changeTime)
                .setOldValue(oldValue)
                .setNewValue(newValue);
    }

    @Override
    public RecipientBindingHistoryBuilder constructExitMailingId(Date changeTime, Integer oldValue, Integer newValue) {

        return builder.setChangedFieldName(RecipientMutableFields.EXIT_MAILING_ID.getCode())
                .setChangeDate(changeTime)
                .setOldValue(Objects.nonNull(oldValue) ? Integer.toString(oldValue) : null)
                .setNewValue(Objects.nonNull(newValue) ? Integer.toString(newValue) : null);
    }

    @Override
    public RecipientBindingHistoryBuilder constructDeletedMailinglist(Date changeTime) {

        return builder.setChangedFieldName(RecipientMutableFields.MAILINGLIST_DELETED.getCode())
                .setChangeDate(changeTime)
                .setOldValue(null)
                .setNewValue(null);
    }

    @Override
    public RecipientBindingHistoryBuilder constructBindingDeleted(Date changeTime) {

        return builder.setChangedFieldName(RecipientMutableFields.CUSTOMER_BINDING_DELETED.getCode())
                .setChangeDate(changeTime)
                .setOldValue(null)
                .setNewValue(null);
    }
}
