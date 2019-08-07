/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailinglist.form;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

public class MailinglistRecipientForm {
    
    private int mailinglistId;
    
    private String mailinglistShortname;
    
    private Set<Integer> allowedRecipientIds = new HashSet<>();
    
    private int userId;
    
    public int getMailinglistId() {
        return mailinglistId;
    }
    
    public void setMailinglistId(int mailinglistId) {
        this.mailinglistId = mailinglistId;
    }
    
    public String getMailinglistShortname() {
        return mailinglistShortname;
    }
    
    public void setMailinglistShortname(String mailinglistShortname) {
        this.mailinglistShortname = mailinglistShortname;
    }
    
    public Set<Integer> getAllowedRecipientIds() {
        return allowedRecipientIds;
    }
    
    public void setAllowedRecipientIds(Set<Integer> allowedRecipientIds) {
        this.allowedRecipientIds = allowedRecipientIds;
    }
    
    public String getAllowedRecipientId(int id) {
        return allowedRecipientIds.contains(id) ? "on" : "";
    }
    
    public void setAllowedRecipientId(int id, String value) {
        if(StringUtils.equals(value, "on") || StringUtils.equals(value, "yes") || StringUtils.equals(value, "true")) {
            this.allowedRecipientIds.add(id);
        }
    }
    
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
}
