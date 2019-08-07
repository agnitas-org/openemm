/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.beans;

import java.util.Date;

import org.agnitas.emm.core.velocity.VelocityCheck;

public interface DatasourceDescription {
    /**
     * Setter for property id.
     *
     * @param id New value of property id.
     */
    public void setId(int id);

    /**
     * Setter for property companyID.
     *
     * @param id New value of property companyID.
     */
    public void setCompanyID( @VelocityCheck int id);
    
    /**
     * Setter for property sourcegroupID.
     *
     * @param title New value of property sourcegroupID.
     */
    public void setSourcegroupID(int title);
    
    /**
     * Setter for property description.
     *
     * @param desc New value of property description.
     */
    public void setDescription(String desc);
    
    /**
     * Setter for property url.
     *
     * @param url New value of property description.
     */
    public void setUrl(String url);

    /**
     * Setter for property description2.
     *
     * @param desc New value of property description2.
     */
    public void setDescription2(String desc);

    /**
     * Setter for property creationDate.
     *
     * @param creationDate New value of property creationDate.
     */
    public void setCreationDate(Date creationDate);

    /**
     * Getter for property id.
     * 
     * @return Value of property id.
     */
    public int getId();

    /**
     * Getter for property companyID.
     * 
     * @return Value of property companyID.
     */
    public int getCompanyID();
    
    /**
     * Getter for property sourcegroupID.
     * 
     * @return Value of property sourcegroupID.
     */
    public int getSourcegroupID();
    
    /**
     * Getter for property description.
     * 
     * @return Value of property description.
     */
    public String getDescription();
    
    /**
     * Getter for property url.
     * 
     * @return Value of property url.
     */
    public String getUrl();

    /**
     * Getter for property description2.
     * 
     * @return Value of property description2.
     */
    public String getDescription2();

    /**
     * Getter for property creationDate.
     * 
     * @return Value of property creationDate.
     */
    public Date getCreationDate();
}
