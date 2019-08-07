/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.recipient.forms;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.agnitas.emm.core.recipient.dto.RecipientFieldDto;

public class RecipientBulkForm {
    
    private int targetId;
    private int mailinglistId;
    
    private Map<String, RecipientFieldDto> recipientFieldChanges = new HashMap<>();
    
    public int getTargetId() {
        return targetId;
    }
    
    public void setTargetId(int targetId) {
        this.targetId = targetId;
    }
    
    public int getMailinglistId() {
        return mailinglistId;
    }
    
    public void setMailinglistId(int mailinglistId) {
        this.mailinglistId = mailinglistId;
    }
    
    public Map<String, RecipientFieldDto> getRecipientFieldChanges() {
        return recipientFieldChanges;
    }
    
    public void setRecipientFieldChanges(Map<String, RecipientFieldDto> recipientFieldChanges) {
        this.recipientFieldChanges = recipientFieldChanges;
    }
    
    public void setRecipientFieldChanges(List<String> columnNames) {
        columnNames.forEach(columnName -> recipientFieldChanges.putIfAbsent(columnName, new RecipientFieldDto()));
    }
    
    
}
