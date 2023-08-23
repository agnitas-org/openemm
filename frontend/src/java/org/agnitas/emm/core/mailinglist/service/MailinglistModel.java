/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.mailinglist.service;



public class MailinglistModel {
    public interface AddGroup {
    	// do nothing
    }
    
    public interface UpdateGroup {
    	// do nothing
    }
    
    public interface GetGroup {
    	// do nothing
    }
    
    public interface CompanyGroup {
    	// do nothing
    }

    private int companyId;
    private int mailinglistId;
    private String shortname;
    private String description;

    public int getCompanyId() {
        return companyId;
    }
    public void setCompanyId(int companyId) {
        this.companyId = companyId;
    }

    public int getMailinglistId() {
        return mailinglistId;
    }
    public void setMailinglistId(int mailinglistId) {
        this.mailinglistId = mailinglistId;
    }

    public String getShortname() {
        return shortname;
    }
    public void setShortname(String shortname) {
        this.shortname = shortname;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
}
