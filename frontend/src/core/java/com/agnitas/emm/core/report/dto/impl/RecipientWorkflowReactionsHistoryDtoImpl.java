/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.report.dto.impl;

import java.util.Date;

import com.agnitas.emm.core.report.dto.RecipientWorkflowReactionsHistoryDto;

public class RecipientWorkflowReactionsHistoryDtoImpl implements RecipientWorkflowReactionsHistoryDto {

    private int entityId;
    private int caseId;
    private Date date;

    public RecipientWorkflowReactionsHistoryDtoImpl(int entityId, int caseId, Date date) {
        this.entityId = entityId;
        this.caseId = caseId;
        this.date = date;
    }

    @Override
    public int getEntityId() {
        return entityId;
    }

    @Override
    public void setEntityId(int entityId) {
        this.entityId = entityId;
    }

    @Override
    public String getEntityName() {
        return "";
    }

    @Override
    public void setEntityName(String name) {
        // nothing to do
    }

    @Override
    public int getCaseId() {
        return caseId;
    }

    @Override
    public void setCaseId(int caseId) {
        this.caseId = caseId;
    }

    @Override
    public Date getDate() {
        return date;
    }

    @Override
    public void setDate(Date date) {
        this.date = date;
    }
}
