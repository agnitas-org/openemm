/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.recipient.service;

import java.util.HashMap;
import java.util.Map;

public class FieldsSaveResults {
    
    private int affectedRecipients;
    
    private Map<String, Object> affectedFields = new HashMap<>();
    
    public FieldsSaveResults() {
    }
    
    public FieldsSaveResults(int affectedRecipients, Map<String, Object> affectedFields) {
        this.affectedRecipients = affectedRecipients;
        this.affectedFields = affectedFields;
    }
    
    public int getAffectedRecipients() {
        return affectedRecipients;
    }
    
    public void setAffectedRecipients(int affectedRecipients) {
        this.affectedRecipients = affectedRecipients;
    }
    
    public Map<String, Object> getAffectedFields() {
        return affectedFields;
    }
    
    public void setAffectedFields(Map<String, Object> affectedFields) {
        this.affectedFields = affectedFields;
    }
}
