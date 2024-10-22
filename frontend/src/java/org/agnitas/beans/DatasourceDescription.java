/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.beans;

import java.util.Date;

import org.agnitas.dao.SourceGroupType;

public interface DatasourceDescription {
    /**
     * Setter for property id.
     *
     * @param id New value of property id.
     */
    void setId(int id);

    /**
     * Setter for property companyID.
     *
     * @param id New value of property companyID.
     */
    void setCompanyID(int id);
    
    /**
     * Setter for property sourcegroupID.
     *
     * @param sourceGroupType New value of property sourceGroupType.
     */
    void setSourceGroupType(SourceGroupType sourceGroupType);
    
    /**
     * Setter for property description.
     *
     * @param desc New value of property description.
     */
    void setDescription(String desc);
    
    /**
     * Setter for property url.
     *
     * @param url New value of property description.
     */
    void setUrl(String url);

    /**
     * Setter for property description2.
     *
     * @param desc New value of property description2.
     */
    void setDescription2(String desc);

    /**
     * Setter for property creationDate.
     *
     * @param creationDate New value of property creationDate.
     */
    void setCreationDate(Date creationDate);

    /**
     * Getter for property id.
     * 
     * @return Value of property id.
     */
    int getId();

    /**
     * Getter for property companyID.
     * 
     * @return Value of property companyID.
     */
    int getCompanyID();
    
    /**
     * Getter for property sourcegroupID.
     * 
     * @return Value of property sourcegroupID.
     */
    SourceGroupType getSourceGroupType();
    
    /**
     * Getter for property description.
     * 
     * @return Value of property description.
     */
    String getDescription();
    
    /**
     * Getter for property url.
     * 
     * @return Value of property url.
     */
    String getUrl();

    /**
     * Getter for property description2.
     * 
     * @return Value of property description2.
     */
    String getDescription2();

    /**
     * Getter for property creationDate.
     * 
     * @return Value of property creationDate.
     */
    Date getCreationDate();
}
