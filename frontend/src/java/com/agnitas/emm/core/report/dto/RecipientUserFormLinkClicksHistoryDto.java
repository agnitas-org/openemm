/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.report.dto;

import com.agnitas.emm.core.report.generator.TextColumn;
import com.agnitas.emm.core.report.generator.TextTable;

@TextTable(
        translationKey = "Linkclicks",
        defaultTitle = "USERFORM CLICKS",
        order = {"getDate", "getIpAddress", "getEntityId", "getEntityName", "getLinkId", "getLinkUrl"})
public interface RecipientUserFormLinkClicksHistoryDto extends RecipientLinkClicksHistoryDto {

    @Override
	@TextColumn(width = 20, translationKey = "MailinglistID", defaultValue = "USER FORM ID")
    int getEntityId();

    @Override
	@TextColumn(width = 50, translationKey = "userform.name", defaultValue = "FORM NAME")
    String getEntityName();
}
