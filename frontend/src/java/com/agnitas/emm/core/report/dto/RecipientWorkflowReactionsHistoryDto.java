/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.report.dto;

import com.agnitas.emm.core.report.generator.TextColumn;
import com.agnitas.emm.core.report.generator.TextTable;

@TextTable(
        defaultTitle = "WORKFLOW REACTIONS HISTORY",
        order = {"getDate", "getEntityId", "getCaseId"})
public interface RecipientWorkflowReactionsHistoryDto extends RecipientHistoryDto {
    
    @Override
	@TextColumn(width = 20, translationKey = "workflow.Reaction", defaultValue = "REACTION ID")
    int getEntityId();
    
    @TextColumn(width = 20, defaultValue = "CASE ID")
    int getCaseId();

    void setCaseId(int caseId);
}
