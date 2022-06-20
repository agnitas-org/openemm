/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailinglist.form;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

public class MailinglistUserForm {
    
    private int mailinglistId;
    
    private String mailinglistShortname;
    
    private Set<Integer> allowedUserIds = new HashSet<>();
    
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
    
    public Set<Integer> getAllowedUserIds() {
        return allowedUserIds;
    }
    
    public void setAllowedUserIds(Set<Integer> allowedUserIds) {
        this.allowedUserIds = allowedUserIds;
    }
    
    public String getAllowedUserId(int id) {
        return allowedUserIds.contains(id) ? "on" : "";
    }
    
    public void setAllowedUserId(int id, String value) {
        if(StringUtils.equals(value, "on") || StringUtils.equals(value, "yes") || StringUtils.equals(value, "true")) {
            this.allowedUserIds.add(id);
        }
    }

    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
}
