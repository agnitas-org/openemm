/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.report.bean;

import java.util.Date;

import org.antlr.v4.runtime.misc.Nullable;

public interface RecipientBindingHistory {

    int getMailinglistId();

    void setMailinglistId(int mailinglistId);

    int getMediaType();

    void setMediaType(int mediaType);

    String getShortname();

    void setShortname(String shortname);

    boolean isDeleted();

    void setDeleted(boolean deleted);

    String getChangedFieldName();

    void setChangedFieldName(String changedFieldName);

    @Nullable
    String getOldValue();

    void setOldValue(String oldValue);

    @Nullable
    String getNewValue();

    void setNewValue(String newValue);

    Date getChangeDate();

    void setChangeDate(Date changeDate);
}
