/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailinglist.form;

public class MailinglistRecipientDeleteForm {
    
    private int mailinglistId;
    
    private String mailinglistShortname;
    
    private boolean onlyActiveUsers;
    
    private boolean noAdminAndTestUsers;
    
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
    
    public boolean isOnlyActiveUsers() {
        return onlyActiveUsers;
    }
    
    public void setOnlyActiveUsers(boolean onlyActiveUsers) {
        this.onlyActiveUsers = onlyActiveUsers;
    }
    
    public boolean isNoAdminAndTestUsers() {
        return noAdminAndTestUsers;
    }
    
    public void setNoAdminAndTestUsers(boolean noAdminAndTestUsers) {
        this.noAdminAndTestUsers = noAdminAndTestUsers;
    }
}
