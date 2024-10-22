/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtreport.bean;

import java.util.Date;

public class ReportEntry {
    
    private int id;
    private int companyId;
    
    private String shortname;
    
    private String description;
    
    private boolean isHidden;
    
    private Date changeDate;
    
    private Date deliveryDate;
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
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
    
    public boolean isHidden() {
        return isHidden;
    }
    
    public void setHidden(boolean hidden) {
        isHidden = hidden;
    }
    
    public Date getChangeDate() {
        return changeDate;
    }
    
    public void setChangeDate(Date changeDate) {
        this.changeDate = changeDate;
    }
    
    public Date getDeliveryDate() {
        return deliveryDate;
    }
    
    public void setDeliveryDate(Date deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public int getCompanyId() {
        return companyId;
    }

    public void setCompanyId(int companyId) {
        this.companyId = companyId;
    }
}
