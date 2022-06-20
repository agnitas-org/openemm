/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.report.director;

import java.util.Date;

import com.agnitas.emm.core.report.builder.RecipientBindingHistoryBuilder;

public interface RecipientBindingHistoryDirector {

    RecipientBindingHistoryBuilder constructChangedType(Date changeTime, String oldValue, String newValue);

    RecipientBindingHistoryBuilder constructChangedStatus(Date changeTime, Integer oldValue, Integer newValue);

    RecipientBindingHistoryBuilder constructChangedRemark(Date changeTime, String oldValue, String newValue);

    RecipientBindingHistoryBuilder constructExitMailingId(Date changeTime, Integer oldValue, Integer newValue);

    RecipientBindingHistoryBuilder constructDeletedMailinglist(Date changeTime);

    RecipientBindingHistoryBuilder constructBindingDeleted(Date changeTime);
}
